/*
 * The Gemma project
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
package ubic.gemma.analysis.preprocess;

import ubic.basecode.dataStructure.matrix.DoubleMatrix;

/**
 * Represents probe-level summary methods for Affymetrix microarrays.
 * 
 * @author pavlidis
 * @version $Id: ProbeSummarizer.java,v 1.6 2008/08/15 00:11:18 paul Exp $
 */
public interface ProbeSummarizer {

    /**
     * @param dataMatrix
     * @return
     */
    public DoubleMatrix<String, String> summarize( DoubleMatrix<String, String> dataMatrix );
}
