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
package ubic.gemma.loader.expression.geo.model;

import java.util.Collection;
import java.util.HashSet;

import ubic.gemma.loader.expression.geo.model.GeoVariable.VariableType;

/**
 * Represents a subset of samples.
 * 
 * @author pavlidis
 * @version $Id: GeoSubset.java,v 1.2 2011/02/17 01:20:31 paul Exp $
 */
public class GeoSubset extends GeoData {

    private static final long serialVersionUID = 2392295178038874633L;
    private GeoDataset owningDataset;
    private String description = "";
    private Collection<GeoSample> samples;

    private VariableType type;

    public GeoSubset() {
        this.samples = new HashSet<GeoSample>();
    }

    public void addToDescription( String s ) {
        this.description = this.description + " " + s;
    }

    /**
     * @return Returns the description.
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * @param description The description to set.
     */
    public void setDescription( String description ) {
        this.description = description;
    }

    /**
     * @return Returns the sample.
     */
    public Collection<GeoSample> getSamples() {
        return this.samples;
    }

    /**
     * @param sample The sample to set.
     */
    public void setSample( Collection<GeoSample> samples ) {
        this.samples = samples;
    }

    public void addSample( GeoSample sample ) {
        this.samples.add( sample );
    }

    /**
     * @return Returns the type.
     */
    public VariableType getType() {
        return this.type;
    }

    /**
     * @param type The type to set.
     */
    public void setType( VariableType type ) {
        this.type = type;
    }

    /**
     * @return Returns the owningDataset.
     */
    public GeoDataset getOwningDataset() {
        return this.owningDataset;
    }

    /**
     * @param owningDataset The owningDataset to set.
     */
    public void setOwningDataset( GeoDataset owningDataset ) {
        this.owningDataset = owningDataset;
    }

}
