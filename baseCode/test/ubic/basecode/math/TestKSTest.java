package ubic.basecode.math;

import junit.framework.TestCase;
import ubic.basecode.math.distribution.ProbabilityComputer;
import ubic.basecode.math.distribution.UniformProbabilityComputer;
import cern.colt.list.DoubleArrayList;

/**
 * <hr>
 * <p>
 * Copyright (c) 2004 University of British Columbia
 * 
 * @author pavlidis
 * @version $Id: TestKSTest.java,v 1.2 2008/02/13 22:16:43 paul Exp $
 */
public class TestKSTest extends TestCase {

    // uniform (r)
    DoubleArrayList x = new DoubleArrayList( new double[] { 0.42084388, 0.08428030, 0.51525081, 0.02165163, 0.99627802,
            0.79237273, 0.52478154, 0.21394388, 0.19654006, 0.88131869 } );

    // normal (n)
    DoubleArrayList y = new DoubleArrayList( new double[] { -0.09503411, 2.33677197, 0.61934707, 0.83549049,
            0.09643316, -0.57449861, -1.40573974, 0.51279445, -0.09593008, 1.48125008 } );

    public void testKSTestOneSample() {
        // ks.test(n, punif, 0, 1)

        // x<-punif(sort(n)) - (0 : (10-1)) / 10
        // max(c(x, 1/10 - x))

        ProbabilityComputer ps = new UniformProbabilityComputer();
        double actualReturn = KSTest.oneSample( y, ps );
        double expectedReturn = 0.07698; // D is 0.4036
        assertEquals( "return value", expectedReturn, actualReturn, 0.0001 );
    }

    public void testKSTestTwoSample() {
        double actualReturn = KSTest.twoSample( x, y );
        double expectedReturn = 0.4175; // from R ks.test(x,y)
        assertEquals( "return value", expectedReturn, actualReturn, 0.0001 );
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