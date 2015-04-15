/*
 * The Gemma-1.0 project
 * 
 * Copyright (c) 2008 University of British Columbia
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
package ubic.gemma.web.controller.expression.experiment;

import ubic.gemma.job.TaskCommand;

/**
 * @author paul
 * @version $Id: ExpressionExperimentDataFetchCommand.java,v 1.6 2011/10/23 19:39:36 paul Exp $
 */
public class ExpressionExperimentDataFetchCommand extends TaskCommand {

    private static final long serialVersionUID = 1L;
    private Long quantitationTypeId;
    private Long expressionExperimentId;
    private boolean filter;
    private Long experimentalDesignId;
    private String format;
    private boolean forceRewrite = false;

    private Long analysisId;

    public Long getAnalysisId() {
        return analysisId;
    }

    public Long getExperimentalDesignId() {
        return experimentalDesignId;
    }

    public Long getExpressionExperimentId() {
        return expressionExperimentId;
    }

    public String getFormat() {
        return format;
    }

    public Long getQuantitationTypeId() {
        return quantitationTypeId;
    }

    public boolean isFilter() {
        return filter;
    }

    public boolean isForceRewrite() {
        return forceRewrite;
    }

    public void setAnalysisId( Long analysisId ) {
        this.analysisId = analysisId;
    }

    public void setExperimentalDesignId( Long experimentalDesignId ) {
        this.experimentalDesignId = experimentalDesignId;
    }

    public void setExpressionExperimentId( Long expressionExperimentId ) {
        this.expressionExperimentId = expressionExperimentId;
    }

    public void setFilter( boolean filter ) {
        this.filter = filter;
    }

    public void setForceRewrite( boolean forceRewrite ) {
        this.forceRewrite = forceRewrite;
    }

    public void setFormat( String format ) {
        this.format = format;
    }

    public void setQuantitationTypeId( Long quantitationTypeId ) {
        this.quantitationTypeId = quantitationTypeId;
    }

}
