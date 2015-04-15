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

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ubic.gemma.datastructure.matrix.ExpressionDataDoubleMatrix;
import ubic.gemma.expression.experiment.service.ExpressionExperimentService;
import ubic.gemma.model.expression.bioAssayData.MeanVarianceRelation;
import ubic.gemma.model.expression.bioAssayData.ProcessedExpressionDataVector;
import ubic.gemma.model.expression.experiment.ExpressionExperiment;

/**
 * @author ptan
 * @version $Id: MeanVarianceServiceHelperImpl.java,v 1.2 2013/05/13 18:52:00 ptan Exp $
 */
@Service
public class MeanVarianceServiceHelperImpl implements MeanVarianceServiceHelper {

    @Autowired
    private ExpressionExperimentService expressionExperimentService;

    @Autowired
    private ProcessedExpressionDataVectorCreateHelperService helperService;

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.analysis.preprocess.MeanVarianceServiceHelper#getIntensities(ExpressionExperiment)
     */
    @Override
    public ExpressionDataDoubleMatrix getIntensities( ExpressionExperiment ee ) {

        Collection<ProcessedExpressionDataVector> processedVectors = expressionExperimentService
                .getProcessedDataVectors( ee );

        return helperService.loadIntensities( ee, processedVectors );
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * ubic.gemma.analysis.preprocess.MeanVarianceServiceHelper#createMeanVariance(ubic.gemma.model.expression.experiment
     * .ExpressionExperiment, ubic.gemma.model.expression.bioAssayData.MeanVarianceRelation)
     */
    @Override
    public void createMeanVariance( ExpressionExperiment ee, MeanVarianceRelation mvr ) {
        ee.setMeanVarianceRelation( mvr );
        expressionExperimentService.update( ee );
    }

}
