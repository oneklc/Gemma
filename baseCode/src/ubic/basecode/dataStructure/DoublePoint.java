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
package ubic.basecode.dataStructure;

/**
 * @author klc
 * @version $Id: DoublePoint.java,v 1.2 2010/04/29 10:37:01 paul Exp $
 */
public class DoublePoint {

    private double x, y;

    /**
     * @param i
     * @param j
     */
    public DoublePoint( double i, double j ) {
        set( i, j );
    }

    /**
     * @return array containing the coordinates x,y.
     */
    public double[] get() {
        return new double[] { x, y };
    }

    /**
     * @return x the x value.
     */
    public double getx() {
        return x;
    }

    /**
     * @return y the y value.
     */
    public double gety() {
        return y;
    }

    /**
     * @param i
     * @param j
     */
    public void set( double i, double j ) {
        x = i;
        y = j;
    }

    /**
     * @return string representation of the point.
     */
    @Override
    public String toString() {
        return x + "\t" + y;
    }

}