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

import java.text.NumberFormat;

/**
 * Implements comparable, which sorts by the 'x' coordinate and then secondarily by the 'y' coordinate. (This behavior
 * is important for some applications).
 * 
 * @author Paul Pavlidis
 * @version $Id: Link.java,v 1.5 2009/12/16 06:02:48 paul Exp $
 */
public class Link extends Point implements Comparable<Link> {

    private double weight;

    /**
     * @param i int
     * @param j int
     * @param weight double
     */
    public Link( int i, int j, double weight ) {
        super( i, j );
        this.weight = weight;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo( Link arg ) {
        if ( arg.getx() < this.getx() ) {
            return -1;
        } else if ( arg.getx() > this.getx() ) {
            return 1;
        } else {
            if ( arg.gety() < this.gety() ) {
                return -1;
            } else if ( arg.gety() > this.gety() ) {
                return 1;
            }
            return 0;
        }
    }

    /**
     * @return double
     */
    public double getWeight() {
        return weight;
    }

    /**
     * @return java.lang.String
     */
    @Override
    public String toString() {
        return super.toString() + "\t" + NumberFormat.getInstance().format( this.weight );
    }

}