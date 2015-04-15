/*
 * The Gemma project
 * 
 * Copyright (c) 2013 University of British Columbia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package ubic.gemma.analysis.preprocess;

import ubic.gemma.model.expression.experiment.ExpressionExperiment;

/**
 * TODO Document Me
 * 
 * @author Paul
 * @version $Id: PreprocessorService.java,v 1.8 2013/05/01 19:23:17 paul Exp $
 */
public interface PreprocessorService {

    /**
     * @param ee
     * @throws PreprocessingException
     */
    public abstract void process( ExpressionExperiment ee ) throws PreprocessingException;

    /**
     * @param ee
     * @param light if true, just do the bare minimum. The following are skipped: two-channel missing values; redoing
     *        differential expression.
     */
    public abstract void process( ExpressionExperiment ee, boolean light ) throws PreprocessingException;

}