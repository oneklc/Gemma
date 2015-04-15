/*
 * The baseCode project
 * 
 * Copyright (c) 2006 University of British Columbia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package ubic.basecode.math;

import ubic.basecode.math.distribution.ProbabilityComputer;
import cern.colt.list.DoubleArrayList;
import cern.colt.list.IntArrayList;
import cern.jet.stat.Descriptive;

/**
 * Class to perform the Kolmogorov-Smirnov test. Ported from R.
 * 
 * @author pavlidis
 * @version $Id: KSTest.java,v 1.5 2009/10/11 20:56:55 paul Exp $
 */
public class KSTest {

    /**
     * @param x
     * @param pg
     * @return pvalue
     */
    public static double oneSample( DoubleArrayList x, ProbabilityComputer pg ) {

        DoubleArrayList xs = x.copy();
        int n = xs.size();

        DoubleArrayList z = new DoubleArrayList( 2 * n );

        // x <- y(sort(x), ...) - (0 : (n-1)) / n
        xs.sort();
        for ( int i = 0; i < n; i++ ) {
            z.add( pg.probability( xs.getQuick( i ) ) - ( double ) i / n );
        }

        // c(x, 1/n - x)
        for ( int i = 0; i < n; i++ ) {
            z.add( 1.0 / n - z.getQuick( i ) );
        }

        double statistic = Descriptive.max( z );

        // 1 - pkstwo(sqrt(n) * STATISTIC)
        return 1.0 - pkstwo( Math.sqrt( n ) * statistic );

    }

    /**
     * @param x
     * @param y
     * @return pvalue
     */
    public static double twoSample( DoubleArrayList x, DoubleArrayList y ) {
        int nx = x.size();
        int ny = y.size();
        if ( ny < 1 || nx < 1 ) {
            throw new IllegalStateException( "Can't do test" );
        }

        DoubleArrayList w = new DoubleArrayList( x.elements() );
        w.addAllOf( y );

        IntArrayList orderw = Rank.order( w );

        // z <- cumsum(ifelse(order(w) <= n.x, 1 / n.x, - 1 / n.y)) // tricky...
        DoubleArrayList z = new DoubleArrayList( w.size() );

        for ( int i = 0; i < orderw.size(); i++ ) {
            int ww = orderw.getQuick( i );

            if ( ww <= nx ) {
                z.add( 1.0 / nx + ( i > 0 ? z.getQuick( i - 1 ) : 0 ) );
            } else {
                z.add( -1.0 / ny + ( i > 0 ? z.getQuick( i - 1 ) : 0 ) );
            }
        }

        // FIXME do something about ties here //

        // take absolute value of w.
        for ( int i = 1; i < z.size(); i++ ) {
            z.setQuick( i, Math.abs( z.getQuick( i ) ) );
        }

        double statistic = Descriptive.max( z );

        return 1.0 - psmirnov2x( statistic, nx, ny );

    }

    private static double pkstwo( double x ) {
        double tol = 1e-6;
        double[] p = new double[] { x };
        pkstwo( 1, p, tol );
        return p[0];
    }

    /*
     * Compute the asymptotic distribution of the one- and two-sample two-sided Kolmogorov-Smirnov statistics, and the
     * exact distribution in the two-sided two-sample case.
     */

    /**
     * From R code.
     * 
     * <pre>
     * 
     * 
     *        Compute
     *                       \sum_{k=-\infty}&circ;\infty (-1)&circ;k e&circ;{-2 k&circ;2 x&circ;2}
     *                       = 1 + 2 \sum_{k=1}&circ;\infty (-1)&circ;k e&circ;{-2 k&circ;2 x&circ;2}
     *                       = \sqrt{2\pi/x} \sum_{k=1}&circ;\infty \exp(-(2k-1)&circ;2\pi&circ;2/(8x&circ;2))
     * 
     * 
     * 
     * </pre>
     * <p>
     * See e.g. J. Durbin (1973), Distribution Theory for Tests Based on the Sample Distribution Function. SIAM.
     * <p>
     * The 'standard' series expansion obviously cannot be used close to 0; we use the alternative series for x < 1, and
     * a rather crude estimate of the series remainder term in this case, in particular using that ue^(-lu^2) \le
     * e^(-lu^2 + u) \le e^(-(l-1)u^2 - u^2+u) \le e^(-(l-1)) provided that u and l are >= 1.
     * <p>
     * (But note that for reasonable tolerances, one could simply take 0 as the value for x < 0.2, and use the standard
     * expansion otherwise.)
     * <hr>
     * 
     * @param x[1:n] is input and output
     * @param n Number of items in the data
     * @param tol Tolerance; 1e-6 is used by R.
     */
    private static void pkstwo( int n, double[] x, double tol ) {

        double newV, old, s, w, z;
        int i, k, k_max;

        k_max = ( int ) Math.sqrt( 2.0 - Math.log( tol ) );

        for ( i = 0; i < n; i++ ) {
            if ( x[i] < 1 ) {
                z = -( Constants.M_PI_2 * Constants.M_PI_4 ) / ( x[i] * x[i] );
                w = Math.log( x[i] );
                s = 0;
                for ( k = 1; k < k_max; k += 2 ) {
                    s += Math.exp( k * k * z - w );
                }
                x[i] = s / Constants.M_1_SQRT_2PI;
            } else {
                z = -2 * x[i] * x[i];
                s = -1;
                k = 1;
                old = 0;
                newV = 1;
                while ( Math.abs( old - newV ) > tol ) {
                    old = newV;
                    newV += 2 * s * Math.exp( z * k * k );
                    s *= -1;
                    k++;
                }
                x[i] = newV;
            }
        }
    }

    private static double psmirnov2x( double x, int m, int n ) {
        int i, j;

        if ( m > n ) {
            i = n;
            n = m;
            m = i;
        }
        double md = m;
        double nd = n;
        double q = Math.floor( x * md * nd - 1e-7 ) / ( md * nd );
        double[] u = new double[n + 1];

        for ( j = 0; j <= n; j++ ) {
            u[j] = j / nd > q ? 0 : 1;
        }
        for ( i = 1; i <= m; i++ ) {
            double w = ( double ) i / ( double ) ( i + n );
            if ( i / md > q )
                u[0] = 0;
            else
                u[0] = w * u[0];
            for ( j = 1; j <= n; j++ ) {
                if ( Math.abs( i / md - j / nd ) > q )
                    u[j] = 0;
                else
                    u[j] = w * u[j] + u[j - 1];
            }

            x = u[n];
        }
        return x;
    }
}
