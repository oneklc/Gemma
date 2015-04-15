/*
 * The baseCode project
 * 
 * Copyright (c) 2008 University of British Columbia
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
package ubic.basecode.dataStructure.matrix;

import java.io.InputStream;

import junit.framework.TestCase;
import ubic.basecode.io.reader.SparseRaggedMatrixReader;
import ubic.basecode.io.reader.TestSparseDoubleMatrixReader;
import ubic.basecode.util.RegressionTesting;
import cern.colt.list.DoubleArrayList;
import cern.colt.matrix.DoubleMatrix1D;

/**
 * @author pavlidis
 * @version $Id: TestSparseRaggedDoubleMatrix2DNamed.java,v 1.4 2009/12/18 18:55:02 paul Exp $
 */
public class TestSparseRaggedDoubleMatrix2DNamed extends TestCase {
    SparseRaggedDoubleMatrix<String, String> matrix = null;
    InputStream is = null;
    InputStream isa = null;
    SparseRaggedMatrixReader reader = null;

    public void testColumns() {
        int actualReturn = matrix.columns();
        int expectedReturn = 3;
        assertEquals( "return value", expectedReturn, actualReturn );
    }

    /* getRow returns a double[] */
    public void testGetRow() {
        DoubleArrayList actualReturn = new DoubleArrayList( matrix.getRow( 2 ) );
        DoubleArrayList expectedReturn = new DoubleArrayList( new double[] { 0.3, 0.0, 0.8 } );
        assertTrue( RegressionTesting.closeEnough( expectedReturn, actualReturn, 0.0001 ) );
    }

    public void testGetRowArrayList() {

        DoubleArrayList actualReturn = matrix.getRowArrayList( 2 );

        DoubleArrayList expectedReturn = new DoubleArrayList( new double[] { 0.3, 0.8 } );

        assertTrue( RegressionTesting.closeEnough( expectedReturn, actualReturn, 0.0001 ) );

    }

    //
    public void testGetRowMatrix1D() {
        DoubleMatrix1D actualReturn = matrix.viewRow( 2 );
        DoubleMatrix1D expectedReturn = new RCDoubleMatrix1D( new double[] { 0.3, 0.0, 0.8 } );
        assertTrue( RegressionTesting.closeEnough( expectedReturn, actualReturn, 0.0001 ) );
    }

    public void testRows() {

        int actualReturn = matrix.rows();
        int expectedReturn = 3;

        assertEquals( "return value", expectedReturn, actualReturn );
    }

    /*
     * @see TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        reader = new SparseRaggedMatrixReader();
        is = TestSparseDoubleMatrixReader.class.getResourceAsStream( "/data/JW-testmatrix.txt" );
        isa = TestSparseDoubleMatrixReader.class.getResourceAsStream( "/data/adjacencylist-testmatrix.txt" );
        matrix = ( SparseRaggedDoubleMatrix<String, String> ) reader.read( is, 1 );
    }

}