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

import java.lang.reflect.Constructor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.basecode.dataStructure.matrix.Matrix2D;

/**
 * Base implementation of the filter interface. Subclasses must implement the filter() method.
 * 
 * @author Paul Pavlidis
 * @version $Id: AbstractFilter.java,v 1.7 2008/08/15 00:10:46 paul Exp $
 */
public abstract class AbstractFilter<M extends Matrix2D<R, C, V>, R, C, V> implements Filter<M, R, C, V> {

    protected static final Log log = LogFactory.getLog( AbstractFilter.class );

    @SuppressWarnings("unchecked")
    protected M getOutputMatrix( M data, int numRows, int numCols ) {
        Matrix2D<R, C, V> returnval = null;

        try {
            Constructor<? extends Matrix2D> cr = data.getClass().getConstructor(
                    new Class[] { int.class, int.class } );
            returnval = cr.newInstance( new Object[] { new Integer( numRows ), new Integer( numCols ) } );
        } catch ( Exception e ) {
            e.printStackTrace();
        }

        return ( M ) returnval;
    }

}