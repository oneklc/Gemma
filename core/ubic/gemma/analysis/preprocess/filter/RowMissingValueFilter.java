/*
 * The Gemma project
 * 
 * Copyright (c) 2007 University of British Columbia
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
package ubic.gemma.analysis.preprocess.filter;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.gemma.analysis.preprocess.ExpressionDataMatrixBuilder;
import ubic.gemma.datastructure.matrix.ExpressionDataBooleanMatrix;
import ubic.gemma.datastructure.matrix.ExpressionDataDoubleMatrix;
import ubic.gemma.model.expression.designElement.CompositeSequence;
import cern.colt.list.IntArrayList;

/**
 * Filter out rows that have "too many" missing values.
 * 
 * @author pavlidis
 * @version $Id: RowMissingValueFilter.java,v 1.8 2012/05/27 02:58:33 paul Exp $
 */
public class RowMissingValueFilter implements Filter<ExpressionDataDoubleMatrix> {

    private static Log log = LogFactory.getLog( RowMissingValueFilter.class.getName() );
    private int minPresentCount = 5;
    private static final int ABSOLUTEMINPRESENT = 1;
    private double maxFractionRemoved = 0.0;
    private double minPresentFraction = 1.0;
    private boolean minPresentFractionIsSet = false;
    private boolean minPresentIsSet = false;

    ExpressionDataBooleanMatrix absentPresentCalls = null;

    /**
     * Set the minimum number of values that must be present in each row. The default value is 5. This is always
     * overridden by a hard-coded value (currently 2) that must be present for a row to be kept; but this value is in
     * turn overridden by the maxfractionRemoved.
     * 
     * @param m int
     */
    public void setMinPresentCount( int m ) {
        if ( m < 0 ) {
            throw new IllegalArgumentException( "Minimum present count must be > 0." );
        }
        minPresentIsSet = true;
        minPresentCount = m;
    }

    /**
     * @param k double the fraction of values to be removed.
     */
    public void setMinPresentFraction( double k ) {
        if ( k < 0.0 || k > 1.0 )
            throw new IllegalArgumentException( "Min present fraction must be between 0 and 1, got " + k );
        minPresentFractionIsSet = true;
        minPresentFraction = k;
    }

    /**
     * Set the maximum fraction of rows which will be removed from the data set. The default value is 0.3 Set it to 1.0
     * to remove this restriction.
     * 
     * @param f double
     */
    public void setMaxFractionRemoved( double f ) {
        if ( f < 0.0 || f > 1.0 )
            throw new IllegalArgumentException( "Max fraction removed must be between 0 and 1, got " + f );
        maxFractionRemoved = f;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.analysis.preprocess.filter.Filter#filter(ubic.gemma.datastructure.matrix.ExpressionDataMatrix)
     */
    @Override
    public ExpressionDataDoubleMatrix filter( ExpressionDataDoubleMatrix data ) {
        int numRows = data.rows();
        int numCols = data.columns();
        IntArrayList present = new IntArrayList( numRows );

        List<CompositeSequence> kept = new ArrayList<CompositeSequence>();

        /*
         * Do not allow minpresentfraction to override minpresent if minpresent is higher.
         */
        if ( minPresentFractionIsSet ) {
            int proposedMinimumNumberOfSamples = ( int ) Math.ceil( minPresentFraction * numCols );
            if ( !minPresentIsSet ) {
                setMinPresentCount( proposedMinimumNumberOfSamples );
            } else if ( proposedMinimumNumberOfSamples > minPresentCount ) {
                log.info( "The minimum number of samples is already set to " + this.minPresentCount
                        + " but computed missing threshold from fraction of " + minPresentFraction + " is higher ("
                        + proposedMinimumNumberOfSamples + ")" );
                setMinPresentCount( proposedMinimumNumberOfSamples );
            } else {
                log.info( "The minimum number of samples is already set to " + this.minPresentCount
                        + " and computed missing threshold from fraction of " + minPresentFraction + " is lower ("
                        + proposedMinimumNumberOfSamples + "), keeping higher value." );
            }
        }

        if ( minPresentCount > numCols ) {
            throw new IllegalStateException( "Minimum present count is set to " + minPresentCount
                    + " but there are only " + numCols + " columns in the matrix." );
        }

        if ( !minPresentIsSet ) {
            log.info( "No filtering was requested" );
            return data;
        }

        /* first pass - determine how many missing values there are per row */
        for ( int i = 0; i < numRows; i++ ) {
            CompositeSequence designElementForRow = data.getDesignElementForRow( i );

            /* allow for the possibility that the absent/present matrix is not in the same order, etc. */
            int absentPresentRow = absentPresentCalls == null ? -1 : absentPresentCalls
                    .getRowIndex( designElementForRow );

            int presentCount = 0;
            for ( int j = 0; j < numCols; j++ ) {
                boolean callIsPresent = true;
                if ( absentPresentRow >= 0 ) {
                    callIsPresent = absentPresentCalls.get( absentPresentRow, j );
                }
                if ( !Double.isNaN( data.get( i, j ) ) && callIsPresent ) {
                    presentCount++;
                }
            }
            present.add( presentCount );
            if ( presentCount >= ABSOLUTEMINPRESENT && presentCount >= minPresentCount ) {
                kept.add( designElementForRow );
            }
        }

        /* decide whether we need to invoke the 'too many removed' clause, to avoid removing too many rows. */
        if ( maxFractionRemoved != 0.0 && kept.size() < numRows * ( 1.0 - maxFractionRemoved ) ) {
            IntArrayList sortedPresent = new IntArrayList( numRows );
            sortedPresent = present.copy();
            sortedPresent.sort();
            sortedPresent.reverse();

            log.info( "There are " + kept.size() + " rows that meet criterion of at least " + minPresentCount
                    + " non-missing values, but that's too many given the max fraction of " + maxFractionRemoved
                    + "; minpresent adjusted to " + sortedPresent.get( ( int ) ( numRows * ( maxFractionRemoved ) ) ) );

            minPresentCount = sortedPresent.get( ( int ) ( numRows * ( maxFractionRemoved ) ) );

            // Do another pass to add rows we missed before.
            for ( int i = 0; i < numRows; i++ ) {
                if ( present.get( i ) >= minPresentCount && present.get( i ) >= ABSOLUTEMINPRESENT ) {
                    CompositeSequence designElementForRow = data.getDesignElementForRow( i );
                    if ( kept.contains( designElementForRow ) ) continue; // FIXME SLOW because it is a
                    // list.
                    kept.add( designElementForRow );
                }
            }

        }

        log.info( "Retaining " + kept.size() + " rows that meet criterion of at least " + minPresentCount
                + " non-missing values" );

        return new ExpressionDataDoubleMatrix( data, kept );

    }

    /**
     * Supply a separate matrix of booleans. This is not necessary if the input matrix is already 'masked' for missing
     * values.
     * 
     * @param absentPresentCalls
     * @see ExpressionDataMatrixBuilder.maskMissing
     */
    public void setAbsentPresentCalls( ExpressionDataBooleanMatrix absentPresentCalls ) {
        this.absentPresentCalls = absentPresentCalls;
    }
}
