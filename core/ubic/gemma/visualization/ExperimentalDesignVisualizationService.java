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
package ubic.gemma.visualization;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import ubic.gemma.model.expression.bioAssay.BioAssayValueObject;
import ubic.gemma.model.expression.bioAssayData.DoubleVectorValueObject;
import ubic.gemma.model.expression.experiment.BioAssaySet;
import ubic.gemma.model.expression.experiment.ExperimentalFactor;
import ubic.gemma.model.expression.experiment.ExpressionExperiment;

/**
 * @author Paul
 * @version $Id: ExperimentalDesignVisualizationService.java,v 1.35 2013/04/18 22:53:01 paul Exp $
 */
public interface ExperimentalDesignVisualizationService {

    /**
     * For an experiment, spit out (this is only used for testing)
     * 
     * @param e, experiment; should be lightly thawed.
     * @return Map of bioassays to factors to values for plotting. If there are no Factors, a dummy value is returned.
     * @deprecated as it is not used anywhere at the moment except a test.
     */
    @Deprecated
    public abstract LinkedHashMap<BioAssayValueObject, LinkedHashMap<ExperimentalFactor, Double>> getExperimentalDesignLayout(
            ExpressionExperiment e );

    /**
     * Put data vectors in the order you'd want to display the experimental design. This causes the "isReorganized" flag
     * of the dedvs to be set to true.
     * 
     * @param dedvs
     * @return Map of EE ids to Map of BioAssays ...
     */
    public abstract Map<Long, LinkedHashMap<BioAssayValueObject, LinkedHashMap<ExperimentalFactor, Double>>> sortVectorDataByDesign(
            Collection<DoubleVectorValueObject> dedvs );

    /**
     * Sorts the layouts passed in by factor with factors ordered by their number of values, from fewest values to most.
     * The LinkedHashMap<BioAssay, {value}> and LinkedHashMap<ExperimentalFactor, Double>> portions of each layout are
     * both sorted.
     * 
     * @param layouts
     * @return sorted layouts
     */
    public abstract Map<Long, LinkedHashMap<BioAssayValueObject, LinkedHashMap<ExperimentalFactor, Double>>> sortLayoutSamplesByFactor(
            Map<Long, LinkedHashMap<BioAssayValueObject, LinkedHashMap<ExperimentalFactor, Double>>> layouts );

    /**
     * removed the cached layouts and cached BioAssayDimensions for this experiment (could be a subset?)
     * 
     * @param eeId
     */
    public void clearCaches( Long eeId );

    /**
     * removed the cached layouts and cached BioAssayDimensions for this experiment
     * 
     * @param ee
     * @deprecated not used
     */
    @Deprecated
    public void clearCaches( BioAssaySet ee );

    /**
     * removed all cached layouts and cached BioAssayDimensions
     */
    public void clearCaches();
}