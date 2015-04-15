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
package ubic.basecode.math;

/**
 * @author pavlidis
 * @version $Id: MathUtil.java,v 1.3 2008/02/13 22:15:46 paul Exp $
 */
public class MathUtil {

    /**
     * Create an array filled with the integer values from a to b, inclusive. Works if a<b, a>b or a==b.
     * 
     * @param a
     * @param b
     * @return
     */
    public static int[] fillRange( int a, int b ) {
        int[] result = new int[Math.abs( b - a ) + 1];
        for ( int i = 0; i <= Math.abs( b - a ); i++ ) {
            if ( a < b ) {
                result[i] = a + i;
            } else {
                result[i] = a - i;
            }
        }
        return result;
    }

    /**
     * @param array of integers
     * @return The sum of the values in the array.
     */
    public static int sumArray( int[] array ) {
        if ( array == null ) return 0;
        int result = 0;
        for ( int element : array ) {
            result += element;
        }
        return result;
    }

}