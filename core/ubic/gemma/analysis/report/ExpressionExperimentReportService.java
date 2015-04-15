/*
 * The Gemma_sec1 project
 * 
 * Copyright (c) 2009 University of British Columbia
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
package ubic.gemma.analysis.report;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.springframework.security.access.annotation.Secured;

import ubic.gemma.model.expression.experiment.ExpressionExperimentValueObject;

/**
 * Methods for reading and creating reports on ExpressinExperiments. Reports are typically updated either on demand or
 * after an analysis; and retrieval is usually from the web interface.
 * 
 * @author paul
 * @version $Id: ExpressionExperimentReportService.java,v 1.68 2012/06/21 17:28:58 paul Exp $
 */
public interface ExpressionExperimentReportService {

    /**
     * Invalidate the cached 'report' for the experiment with the given id. If it is not cached nothing happens.
     * 
     * @param id
     */
    public void evictFromCache( Long id );

    /**
     * Generate a value object that contain summary information about links, biomaterials, and datavectors
     */
    public abstract ExpressionExperimentValueObject generateSummary( Long id );

    /**
     * Generates reports on ALL experiments, including 'private' ones. This should only be run by administrators as it
     * takes a while to run.
     * 
     * @return
     */
    @Secured({ "GROUP_AGENT" })
    public abstract void generateSummaryObjects();

    /**
     * generates a collection of value objects that contain summary information about links, biomaterials, and
     * datavectors
     * 
     * @return
     */
    public abstract Collection<ExpressionExperimentValueObject> generateSummaryObjects( Collection<Long> ids );

    /**
     * @param vos
     */
    public void getAnnotationInformation( Collection<ExpressionExperimentValueObject> vos );

    /**
     * @param vos
     * @return
     */
    public Map<Long, Date> getEventInformation( Collection<ExpressionExperimentValueObject> vos );

    /**
     * @param vos
     * @return
     */
    public Map<Long, Date> getReportInformation( Collection<ExpressionExperimentValueObject> vos );

    /**
     * retrieves a collection of cached value objects containing summary information
     * 
     * @return a collection of cached value objects
     */
    public abstract Collection<ExpressionExperimentValueObject> retrieveSummaryObjects( Collection<Long> ids );

}