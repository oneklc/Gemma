/*
 * The Gemma project
 * 
 * Copyright (c) 2012 University of British Columbia
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

import java.util.Collection;

import ubic.gemma.model.expression.bioAssayData.ProcessedExpressionDataVector;
import ubic.gemma.model.expression.experiment.ExpressionExperiment;

/**
 * @author paul
 * @version $Id: ProcessedExpressionDataVectorCreateService.java,v 1.34 2013/05/01 19:23:17 paul Exp $
 */
public interface ProcessedExpressionDataVectorCreateService {

    /**
     * This method should not be called on its own, if possible. Use the PreprocessorService to do all necessary
     * refreshing.
     * 
     * @param ee
     * @return the vectors that were modified.
     * @see PreprocessorService
     */
    public abstract Collection<ProcessedExpressionDataVector> computeProcessedExpressionData( ExpressionExperiment ee );

    /**
     * Creates new bioAssayDimensions to match the experimental design, reorders the data to match, updates.
     * 
     * @param ee
     */
    public abstract void reorderByDesign( Long eeId );

}