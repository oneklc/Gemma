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
package ubic.basecode.datafilter;

import ubic.basecode.dataStructure.matrix.DoubleMatrix;

/**
 * @author pavlidis
 * @version $Id: TestItemLevelFilter.java,v 1.7 2009/12/18 18:55:03 paul Exp $
 */
public class TestItemLevelFilter extends AbstractTestFilter {

    ItemLevelFilter<String, String> f = null;

    public final void testFilter() {
        f.setLowCut( 0.0 );
        DoubleMatrix<String, String> result = f.filter( testdata );
        int expectedReturn = 283;
        int actualReturn = result.rows() * result.columns() - result.numMissing();
        assertEquals( "return value", expectedReturn, actualReturn );
    }

    /*
     * @see TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        f = new ItemLevelFilter<String, String>();
    }

    /*
     * @see TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

    }

}