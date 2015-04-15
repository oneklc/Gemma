package ubic.basecode.util;

import junit.framework.TestCase;

import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <hr>
 * <p>
 * Copyright (c) 2004-2006 University of British Columbia
 * 
 * @author pavlidis
 * @version $Id: TestStringUtil.java,v 1.5 2008/02/13 22:16:43 paul Exp $
 */
public class TestStringUtil extends TestCase {

    private static Log log = LogFactory.getLog( TestStringUtil.class.getName() );

    // simple case
    public void testCsvSplitA() {
        String i = "foo,bar,aloo,balloo";
        String[] actualReturn = StringUtil.csvSplit( i );
        String[] expectedReturn = new String[] { "foo", "bar", "aloo", "balloo" };
        assertEquals( expectedReturn[0], actualReturn[0] );
        assertEquals( expectedReturn[1], actualReturn[1] );
        assertEquals( expectedReturn[2], actualReturn[2] );
        assertEquals( expectedReturn[3], actualReturn[3] );
    }

    // field has comma
    public void testCsvSplitB() {
        String i = "foo,bar,aloo,\"bal,loo\"";
        String[] actualReturn = StringUtil.csvSplit( i );
        String[] expectedReturn = new String[] { "foo", "bar", "aloo", "bal,loo" };
        assertEquals( expectedReturn[0], actualReturn[0] );
        assertEquals( expectedReturn[1], actualReturn[1] );
        assertEquals( expectedReturn[2], actualReturn[2] );
        assertEquals( expectedReturn[3], actualReturn[3] );
    }

    // two fields have commas
    public void testCsvSplitC() {
        String i = "\"f,oo\",bar,aloo,\"bal,loo\"";
        String[] actualReturn = StringUtil.csvSplit( i );
        String[] expectedReturn = new String[] { "f,oo", "bar", "aloo", "bal,loo" };
        assertEquals( expectedReturn[3], actualReturn[3] );
        assertEquals( expectedReturn[1], actualReturn[1] );
        assertEquals( expectedReturn[0], actualReturn[0] );
    }

    // two commas in one field.
    public void testCsvSplitD() {
        String i = "foo,\"b,a,r\",aloo,balloo";
        String[] actualReturn = StringUtil.csvSplit( i );
        String[] expectedReturn = new String[] { "foo", "b,a,r", "aloo", "balloo" };
        assertEquals( expectedReturn[3], actualReturn[3] );
        assertEquals( expectedReturn[1], actualReturn[1] );
    }

    // comma at start of field
    public void testCsvSplitE() {
        String i = "foo,\",bar\",aloo,balloo";
        String[] actualReturn = StringUtil.csvSplit( i );
        String[] expectedReturn = new String[] { "foo", ",bar", "aloo", "balloo" };
        assertEquals( expectedReturn[3], actualReturn[3] );
        assertEquals( expectedReturn[1], actualReturn[1] );
        assertEquals( expectedReturn[0], actualReturn[0] );
    }

    // empty quoted field
    public void testCsvSplitF() {
        String i = "foo,\"\",aloo,balloo";
        String[] actualReturn = StringUtil.csvSplit( i );
        String[] expectedReturn = new String[] { "foo", "", "aloo", "balloo" };
        assertEquals( expectedReturn[0], actualReturn[0] );
        assertEquals( expectedReturn[1], actualReturn[1] );
        assertEquals( expectedReturn[2], actualReturn[2] );
        assertEquals( expectedReturn[3], actualReturn[3] );
    }

    public void testSpeedTwoStringHashKey() {
        String a = "barblyfoo";
        String b = "fooblybar";
        StopWatch timer = new StopWatch();
        timer.start();
        int iters = 100000;
        for ( int i = 0; i < iters; i++ ) {
            StringUtil.twoStringHashKey( a, b );
        }
        timer.stop();
        log.debug( "Bitwise hash took " + timer.getTime() + " milliseconds" );
        timer.reset();
        timer.start();
//        for ( int i = 0; i < iters; i++ ) {
//            String r;
//            if ( a.hashCode() < b.hashCode() ) {
//                r = b + "___" + a;
//            } else {
//                r = a + "___" + b;
//            }
//            // if ( log.isTraceEnabled() ) log.trace( r ); // avoid compiler warning about r not being used - no big
//            // time.
//        }
        timer.stop();
        log.debug( "String concat " + timer.getTime() + " milliseconds" );
    }

    public void testTwoStringHashKey() throws Exception {
        String i = "foo";
        String j = "bar";

        Long icode = new Long( i.hashCode() );
        Long jcode = new Long( j.hashCode() );

        log.debug( Long.toBinaryString( jcode.longValue() ) + " " + Long.toBinaryString( icode.longValue() << 32 ) );

        long expectedResult = jcode.longValue() | icode.longValue() << 32;

        Long result = ( Long ) StringUtil.twoStringHashKey( j, i );

        log.debug( Long.toBinaryString( expectedResult ) );

        assertEquals( Long.toBinaryString( expectedResult ), Long.toBinaryString( result.longValue() ) );

    }

    public void testTwoStringHashKeyB() {
        String i = "foo";
        String j = "bar";
        assertEquals( StringUtil.twoStringHashKey( i, j ), StringUtil.twoStringHashKey( j, i ) );
    }

    /*
     * @see TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    /*
     * @see TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
