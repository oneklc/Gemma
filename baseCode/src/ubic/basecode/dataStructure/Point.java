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
package ubic.basecode.dataStructure;

/**
 * @author Paul Pavlidis
 * @version $Id: Point.java,v 1.4 2010/04/29 10:37:01 paul Exp $
 */
public class Point {

    private int x, y;

    /**
     * @param i
     * @param j
     */
    public Point( int i, int j ) {
        set( i, j );
    }

    /**
     * @return array containing the coordinates x,y.
     */
    public int[] get() {
        return new int[] { x, y };
    }

    /**
     * @return x the x value.
     */
    public int getx() {
        return x;
    }

    /**
     * @return y the y value.
     */
    public int gety() {
        return y;
    }

    /**
     * @param i
     * @param j
     */
    public void set( int i, int j ) {
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