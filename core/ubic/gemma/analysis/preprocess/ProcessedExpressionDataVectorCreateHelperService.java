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

import java.util.Collection;

import ubic.gemma.datastructure.matrix.ExpressionDataDoubleMatrix;
import ubic.gemma.model.expression.bioAssayData.ProcessedExpressionDataVector;
import ubic.gemma.model.expression.experiment.ExpressionExperiment;

/**
 * @author Paul
 * @version $Id: ProcessedExpressionDataVectorCreateHelperService.java,v 1.3 2013/05/11 00:37:10 ptan Exp $
 */
public interface ProcessedExpressionDataVectorCreateHelperService {

    /**
     * @param ee
     * @return
     */
    public Collection<ProcessedExpressionDataVector> createProcessedExpressionData( ExpressionExperiment ee );

    /**
     * If possible, update the ranks for the processed data vectors. For data sets with only ratio expression values
     * provided, ranks will not be computable.
     * 
     * @param ee
     * @param processedVectors
     * @return The vectors after updating them, or just the original vectors if ranks could not be computed. (The
     *         vectors may be thawed in the process)
     */
    public Collection<ProcessedExpressionDataVector> updateRanks( ExpressionExperiment ee,
            Collection<ProcessedExpressionDataVector> processedVectors );

    /**
     * @param ee
     * @param processedVectors
     * @return intensities See {@link ExpressionDataMatrixBuilder#getIntensity()}
     */
    public ExpressionDataDoubleMatrix loadIntensities( ExpressionExperiment ee,
            Collection<ProcessedExpressionDataVector> processedVectors );

    /**
     * @param eeId
     */
    public void reorderByDesign( Long eeId );
}
