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

import junit.framework.TestCase;

import ubic.basecode.util.RegressionTesting;

/**
 * @author pavlidis
 * @version $Id: LinearModelTest.java,v 1.9 2008/01/14 21:54:26 paul Exp $
 */
public class LinearModelTest extends TestCase {

    /*
     * Test method for 'basecode.math.LinearModel.fitNoInteractions()'
     */
    public void testFitNoInteractions() {

        double[] x = new double[] { 2, 1, 2, 4, 6, 4 };
        double[] a = new double[] { 0, 0, 0, 1, 1, 1 };
        double[] b = new double[] { 1, 0, 1, 0, 1, 0 };
        LinearModel lm = new LinearModel( x, a, b );
        lm.fitNoInteractions();
        double[] c = lm.getCoefficients();
        assertNotNull( c );
        assertTrue( RegressionTesting.closeEnough( new double[] { 0.6666667, 3.5000000, 1.5000000 }, c, 0.0001 ) );
    }
}
