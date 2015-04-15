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
package ubic.basecode.io.reader;

import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

/**
 * @author Paul Pavlidis
 * @version $Id: TestMapReader.java,v 1.2 2008/02/13 22:16:43 paul Exp $
 */
public class TestMapReader extends TestCase {
    private MapReader mapReader = null;

    public void testRead() throws IOException {
        InputStream m = TestMapReader.class.getResourceAsStream( "/data/testmap.txt" );
        int expectedReturn = 100;
        int actualReturn = mapReader.read( m, true ).size(); // file has header
        assertEquals( "return value", expectedReturn, actualReturn );
    }

    public void testReadNoHeader() throws IOException {
        InputStream m = TestMapReader.class.getResourceAsStream( "/data/testmap.txt" );
        int expectedReturn = 101;
        int actualReturn = mapReader.read( m ).size(); // file has header
        assertEquals( "return value", expectedReturn, actualReturn );
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mapReader = new MapReader();
    }

    @Override
    protected void tearDown() throws Exception {
        mapReader = null;
        super.tearDown();
    }

}