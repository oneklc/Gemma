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

import org.springframework.security.access.annotation.Secured;

import ubic.gemma.model.expression.arrayDesign.ArrayDesignValueObject;

/**
 * @author paul
 * @version $Id: ArrayDesignReportService.java,v 1.13 2009/11/23 20:28:28 paul Exp $
 */
public interface ArrayDesignReportService {

    /**
     * Report summarizing _all_ array designs.
     */
    public void generateAllArrayDesignReport();

    /**
     * Generate reports for all array designs, as well as the "global" report.
     */
    @Secured( { "GROUP_AGENT" })
    public void generateArrayDesignReport();

    /**
     * @param adVo
     */
    @Secured( { "GROUP_AGENT" })
    public void generateArrayDesignReport( ArrayDesignValueObject adVo );

    /**
     * @param id
     * @return
     */
    @Secured( { "GROUP_AGENT" })
    public ArrayDesignValueObject generateArrayDesignReport( Long id );

    public ArrayDesignValueObject getSummaryObject( Long id );

    public ArrayDesignValueObject getSummaryObject();

    public void fillEventInformation( Collection<ArrayDesignValueObject> adVos );

    public void fillInSubsumptionInfo( Collection<ArrayDesignValueObject> valueObjects );

    public void fillInValueObjects( Collection<ArrayDesignValueObject> adVos );

    public String getLastSequenceUpdateEvent( Long id );

    public String getLastSequenceAnalysisEvent( Long id );

    public String getLastRepeatMaskEvent( Long id );

    public String getLastGeneMappingEvent( Long id );

}