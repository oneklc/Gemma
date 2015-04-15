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
package ubic.gemma.web.controller.visualization;

import java.io.Serializable;

import ubic.gemma.model.common.quantitationtype.QuantitationType;

/**
 * Expression experiment command object that wraps expression experiment visualization preferences.
 * 
 * @author keshav
 * @author pavlidis
 * @version $Id: ExpressionExperimentVisualizationCommand.java,v 1.9 2009/11/23 20:28:08 paul Exp $
 */
public class ExpressionExperimentVisualizationCommand implements Serializable {

    private static final long serialVersionUID = 2166768356457316142L;

    private String searchCriteria = null;

    private String name = null;

    private Long expressionExperimentId = null;

    private String searchString = null;

    private boolean viewSampling;

    private boolean maskMissing = false;

    private QuantitationType quantitationType = null;

    public ExpressionExperimentVisualizationCommand() {
        this.quantitationType = QuantitationType.Factory.newInstance();
    }

    /**
     * @return Long
     */
    public Long getExpressionExperimentId() {
        return expressionExperimentId;
    }

    /**
     * @return String
     */
    public String getName() {
        return name;
    }

    /**
     * @return String
     */
    public QuantitationType getQuantitationType() {
        return quantitationType;
    }

    /**
     * @return String
     */
    public String getSearchCriteria() {
        return searchCriteria;
    }

    /**
     * @return String
     */
    public String getSearchString() {
        return searchString;
    }

    /**
     * @return
     */
    public boolean isMaskMissing() {
        return maskMissing;
    }

    /**
     * @return boolean
     */
    public boolean isViewSampling() {
        return viewSampling;
    }

    /**
     * @param id
     */
    public void setExpressionExperimentId( Long id ) {
        this.expressionExperimentId = id;
    }

    /**
     * @param maskMissing
     */
    public void setMaskMissing( boolean maskMissing ) {
        this.maskMissing = maskMissing;
    }

    /**
     * @param name
     */
    public void setName( String name ) {
        this.name = name;
    }

    /**
     * @param standardQuantitationType
     */
    public void setQuantitationType( QuantitationType quantitationType ) {
        this.quantitationType = quantitationType;
    }

    /**
     * @param searchCriteria
     */
    public void setSearchCriteria( String searchCriteria ) {
        this.searchCriteria = searchCriteria;
    }

    /**
     * @param searchString
     */
    public void setSearchString( String searchString ) {
        this.searchString = searchString;
    }

    /**
     * @param viewSampling
     */
    public void setViewSampling( boolean viewSampling ) {
        this.viewSampling = viewSampling;
    }

}
