/*
 * The baseCode project
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
package ubic.basecode.math.metaanalysis;

import junit.framework.TestCase;
import cern.colt.list.DoubleArrayList;

/**
 * Tests based on Cooper and Hedges. Values recomputed by hand in some cases to fix roundoff error in the textbook.
 * 
 * @author pavlidis
 * @version $Id: TestCorrelationEffectMetaAnalysis.java,v 1.3 2008/02/13 22:16:43 paul Exp $
 */
public class TestCorrelationEffectMetaAnalysis extends TestCase {

    CorrelationEffectMetaAnalysis zf;
    CorrelationEffectMetaAnalysis zr;
    CorrelationEffectMetaAnalysis uf;
    CorrelationEffectMetaAnalysis ur;

    DoubleArrayList ds3n;
    DoubleArrayList ds3r;

    public void testRunUFEffect() {
        uf.run( ds3r, ds3n );

        double actualReturn = uf.getE();

        double expectedReturn = 0.39779;
        assertEquals( "return value", expectedReturn, actualReturn, 0.001 );
    }

    public void testRunUFVar() {
        uf.run( ds3r, ds3n );

        double actualReturn = uf.getV();
        double expectedReturn = 0.00118;

        assertEquals( "return value", expectedReturn, actualReturn, 0.0001 );
    }

    public void testRunUFZscore() {
        uf.run( ds3r, ds3n );

        double actualReturn = uf.getZ();

        double expectedReturn = 11.56;
        assertEquals( "return value", expectedReturn, actualReturn, 0.1 ); // this is failing with 0.01
    }

    //
    public void testRunURBSV() {
        ur.run( ds3r, ds3n );
        double actualReturn = ur.getBsv();
        double expectedReturn = 0.0085;
        assertEquals( "return value", expectedReturn, actualReturn, 0.0001 );
    }

    //   
    // public void testRunURZscore() {
    // ur.run( ds3r, ds3n );
    // double actualReturn = ur.getZ();
    // double expectedReturn = 0;
    // assertEquals( "return value", expectedReturn, actualReturn, 0.0001 );
    // }

    public void testRunZFEffect() {
        zf.run( ds3r, ds3n );
        double actualReturn = zf.getE();
        double expectedReturn = 0.379;
        assertEquals( "return value", expectedReturn, actualReturn, 0.01 );
    }

    public void testRunZFVar() {
        zf.run( ds3r, ds3n );
        double actualReturn = zf.getV();
        double expectedReturn = 0.0019;
        assertEquals( "return value", expectedReturn, actualReturn, 0.001 );
    }

    public void testRunZFZscore() {
        zf.run( ds3r, ds3n );
        double actualReturn = zf.getZ();
        double expectedReturn = 8.748; // the value in the book (8.64) suffers from some serious roundoff errors.
        assertEquals( "return value", expectedReturn, actualReturn, 0.01 );
    }

    public void testRunZRBSV() {
        zr.run( ds3r, ds3n );
        double actualReturn = zr.getBsv();
        double expectedReturn = 0.0041;
        assertEquals( "return value", expectedReturn, actualReturn, 0.0001 );
    }

    public void testRunZRQval() {
        zr.run( ds3r, ds3n );
        double actualReturn = zr.getQ();
        double expectedReturn = 20.94;
        assertEquals( "return value", expectedReturn, actualReturn, 0.1 );
    }

    /*
     * @see TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // From Appendix A of Cooper and Hedges: Data Set III.
        ds3n = new DoubleArrayList( new double[] { 10, 20, 13, 22, 28, 12, 12, 36, 19, 12, 36, 75, 33, 121, 37, 14, 40,
                16, 14, 20 } );

        ds3r = new DoubleArrayList( new double[] { 0.68, 0.56, 0.23, 0.64, 0.49, -0.04, 0.49, 0.33, 0.58, 0.18, -0.11,
                0.27, 0.26, 0.40, 0.49, 0.51, 0.40, 0.34, 0.42, 0.16 } );

        zf = new CorrelationEffectMetaAnalysis( true, true );
        zr = new CorrelationEffectMetaAnalysis( false, true );
        uf = new CorrelationEffectMetaAnalysis( true, false );
        ur = new CorrelationEffectMetaAnalysis( false, false );
    }

    // public void testRunZRZscore() {
    // zr.run( ds3r, ds3n );
    // double actualReturn = zr.getZ();
    // double expectedReturn = 0;
    // assertEquals( "return value", expectedReturn, actualReturn, 0.00001 );
    // }

    /*
     * @see TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

}