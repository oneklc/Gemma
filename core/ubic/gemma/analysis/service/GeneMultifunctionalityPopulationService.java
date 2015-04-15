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
package ubic.gemma.analysis.service;

import ubic.gemma.model.genome.Taxon;

/**
 * Populate/update the gene multifunctionality information in the system.
 * 
 * @author paul
 * @version $Id: GeneMultifunctionalityPopulationService.java,v 1.1 2012/05/08 03:27:45 paul Exp $
 */
public interface GeneMultifunctionalityPopulationService {

    /**
     * @param t
     */
    public void updateMultifunctionality( Taxon t );

    /**
     * Update for all taxa
     */
    public void updateMultifunctionality();

}
