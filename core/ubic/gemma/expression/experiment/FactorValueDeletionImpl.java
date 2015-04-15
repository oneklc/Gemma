/*
 * The Gemma project
 * 
 * Copyright (c) 2009 University of British Columbia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 */
/*
 * The Gemma project
 * 
 * Copyright (c) 2011 University of British Columbia
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

package ubic.gemma.expression.experiment;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ubic.gemma.model.analysis.expression.diff.DifferentialExpressionAnalysis;
import ubic.gemma.model.analysis.expression.diff.DifferentialExpressionAnalysisService;
import ubic.gemma.model.expression.biomaterial.BioMaterialService;
import ubic.gemma.model.expression.experiment.ExperimentalFactor;
import ubic.gemma.model.expression.experiment.ExperimentalFactorService;
import ubic.gemma.model.expression.experiment.FactorValue;
import ubic.gemma.model.expression.experiment.FactorValueService;

/**
 * Handles deletions of a factor values. This process includes: 1. Determining if there are any biomaterials that use
 * the factor value 2. If so, delete any differential expression analysis results that use this factor 3. delete the
 * factor value
 * 
 * @author tvrossum
 * @version $Id: FactorValueDeletionImpl.java,v 1.2 2011/11/05 05:25:21 paul Exp $
 */
@Service
public class FactorValueDeletionImpl implements FactorValueDeletion {

    @Autowired
    private BioMaterialService bioMaterialService = null;

    @Autowired
    private DifferentialExpressionAnalysisService differentialExpressionAnalysisService;

    @Autowired
    private ExperimentalFactorService experimentalFactorService = null;

    @Autowired
    private FactorValueService factorValueService = null;

    /*
     * (non-Javadoc)
     * 
     * @see
     * ubic.gemma.expression.experiment.FactorValueDeletion#deleteFactorValue(ubic.gemma.model.expression.experiment
     * .FactorValue)
     */
    @Override
    public void deleteFactorValues( Collection<Long> fvIds ) {

        Collection<FactorValue> fvsToDelete = new ArrayList<FactorValue>();

        for ( Long fvId : fvIds ) {

            FactorValue fv = factorValueService.load( fvId );

            if ( fv == null ) {
                throw new IllegalArgumentException( "No factor value with id=" + fvId + " could be loaded" );
            }

            if ( fv.getExperimentalFactor() == null ) {
                throw new IllegalStateException( "No experimental factor for factor value " + fv.getId() );
            }

            /*
             * Determine if there are any biomaterials that use the factor value in question.
             */
            if ( !bioMaterialService.findByFactorValue( fv ).isEmpty() ) {
                /*
                 * If so, check to see if there are any diff results that use this factor. FIXME This might have to run
                 * in a background thread
                 */
                ExperimentalFactor ef = experimentalFactorService.load( fv.getExperimentalFactor().getId() );
                Collection<DifferentialExpressionAnalysis> analyses = differentialExpressionAnalysisService
                        .findByFactor( ef );
                for ( DifferentialExpressionAnalysis a : analyses ) {
                    differentialExpressionAnalysisService.delete( a );
                }
            }

            fvsToDelete.add( fv );

        }

        for ( FactorValue fv : fvsToDelete ) {
            factorValueService.delete( fv );
        }

    }

}
