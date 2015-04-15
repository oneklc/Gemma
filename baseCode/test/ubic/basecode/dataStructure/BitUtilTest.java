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
package ubic.basecode.dataStructure;

import junit.framework.TestCase;

/**
 * @author paul
 * @version $Id: BitUtilTest.java,v 1.3 2008/02/22 04:15:13 paul Exp $
 */
public class BitUtilTest extends TestCase {

    public void testGet() {
        byte[] v = new byte[4];
        for ( int i = 0, j = v.length; i < j; i++ ) {
            v[i] = 0x0;
        }
        BitUtil.set( v, 4 );
        BitUtil.set( v, 14 );
        BitUtil.set( v, 10 );
        BitUtil.set( v, 3 );
        BitUtil.set( v, 11 );
        BitUtil.set( v, 30 );

        assertTrue( BitUtil.get( v, 4 ) );
        assertTrue( BitUtil.get( v, 14 ) );

    }

    public void testSet() throws Exception {
        byte[] v = new byte[4];
        for ( int i = 0, j = v.length; i < j; i++ ) {
            v[i] = 0x0;
        }
        BitUtil.set( v, 4 );
        BitUtil.set( v, 14 );
        BitUtil.set( v, 10 );
        BitUtil.set( v, 3 );
        BitUtil.set( v, 11 );
        BitUtil.set( v, 30 );

        StringBuilder buf = prettyprint( v );
        // System.err.println(buf.toString());
        assertEquals( "00011000 00110010 00000000 00000010 ", buf.toString() );
    }

    public void testClear() {
        byte[] v = new byte[4];
        for ( int i = 0, j = v.length; i < j; i++ ) {
            v[i] = 0x0;
        }
        BitUtil.set( v, 4 );
        BitUtil.set( v, 14 );
        BitUtil.set( v, 10 );
        BitUtil.set( v, 3 );
        BitUtil.set( v, 11 );
        BitUtil.set( v, 30 );
        BitUtil.clear( v, 14 );
        assertFalse( BitUtil.get( v, 14 ) );
        assertTrue( BitUtil.get( v, 4 ) );
        assertTrue( BitUtil.get( v, 30 ) );
    }

    public void testCount() {
        byte[] v = new byte[4];
        for ( int i = 0, j = v.length; i < j; i++ ) {
            v[i] = 0x0;
        }
        assertEquals( 0, BitUtil.count( v ) );
        BitUtil.set( v, 4 );
        BitUtil.set( v, 14 );
        assertEquals( 2, BitUtil.count( v ) );
        BitUtil.set( v, 10 );
        BitUtil.set( v, 3 );
        BitUtil.set( v, 11 );
        BitUtil.set( v, 30 );
        assertEquals( 6, BitUtil.count( v ) );
        BitUtil.clear( v, 14 );
        assertEquals( 5, BitUtil.count( v ) );
    }

    /**
     * @param v
     * @return
     */
    private StringBuilder prettyprint( byte[] v ) {
        StringBuilder buf = new StringBuilder();

        for ( byte b : v ) {
            for ( int k = 7; k >= 0; k-- ) {
                int u = b >> k & 0x00000001;
                buf.append( u + "" );
            }
            buf.append( " " );
        }
        return buf;
    }
}
