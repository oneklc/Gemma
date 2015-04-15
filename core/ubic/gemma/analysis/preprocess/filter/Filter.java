/*
 * The Gemma project
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
package ubic.gemma.analysis.preprocess.filter;

import ubic.gemma.datastructure.matrix.ExpressionDataMatrix;

/**
 * @author pavlidis
 * @version $Id: Filter.java,v 1.2 2009/10/03 18:11:46 paul Exp $
 */
public interface Filter<T extends ExpressionDataMatrix<Double>> {

    /**
     * Remove some rows from the input matrix, return the revised matrix.
     * 
     * @param dataMatrix
     * @return
     */
    public T filter( T dataMatrix );

}
