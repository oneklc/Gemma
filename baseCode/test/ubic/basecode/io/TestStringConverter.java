package ubic.basecode.io;

import junit.framework.TestCase;

//import javax.sql.rowset.serial;

/**
 * $Id: TestStringConverter.java,v 1.3 2008/02/13 22:16:43 paul Exp $
 */
public class TestStringConverter extends TestCase {
    StringConverter sc;
    ByteArrayConverter bac;

    /**
     * 
     *
     */
    public void testStringToBytes() {
        // System.err.println(" CONVERTING STRING TO BYTES ");

        // byte[] actualReturn = sc.stringArrayToBytes( null );
        // byte[] expectedValue = actualReturn;
        // for ( int i = 0; i < actualReturn.length; i++ ) {
        // //System.err.println("actualReturn:["+i+"]"+ actualReturn[i]);//new
        // // Integer(actualReturn[i]).toBinaryString(actualReturn[i]));
        // assertEquals( "return value", expectedValue[i], actualReturn[i], 0 );
        // }
    }

    /*
     * @see TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        sc = new StringConverter();
        bac = new ByteArrayConverter();
    }

    /*
     * @see TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    // /**
    // *
    // *
    // */
    // public void testStringToDoubles() {
    // // System.err.println(" CONVERTING STRING TO DOUBLES ");
    //
    // double[] actualReturn = sc.stringToDoubles( null );
    // double[] expectedValue = actualReturn;
    // // for ( int i = 0; i < actualReturn.length; i++ ) {
    // // //System.err.println("actualReturn:["+i+"]"+ actualReturn[i]);//new
    // // // Integer(actualReturn[i]).toBinaryString(actualReturn[i]));
    // // //assertEquals( "return value", expectedValue[i], actualReturn[i], 0);
    // // }
    // }

}