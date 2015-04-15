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

import ubic.basecode.util.r.RClient;
import ubic.basecode.util.r.RConnectionFactory;

/**
 * @author pavlidis
 * @version $Id: LinearModel.java,v 1.5 2010/04/29 11:04:07 paul Exp $
 */
public class LinearModel {

    double[] coefficients;
    double[] pValues;
    double[][] designMatrix;
    double[] variable;

    private RClient rc;

    public LinearModel( double[] variable, double[] a, double[] b ) {
        rc = RConnectionFactory.getRConnection();
        rc.assign( "y", variable );
        rc.assign( "a", a );
        rc.assign( "b", b );
    }

    public void fitNoInteractions() {
        rc.voidEval( "m<-lm(y~a+b)" );

        coefficients = rc.doubleArrayEval( "coefficients(m)" );

    }

    public double[] getCoefficients() {
        return this.coefficients;
    }

}
