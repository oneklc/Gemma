/*
 * The Gemma21 project
 * 
 * Copyright (c) 2007 University of British Columbia
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
package ubic.basecode.ontology.providers;

import ubic.basecode.ontology.Configuration;
import ubic.basecode.ontology.OntologyLoader;

import com.hp.hpl.jena.ontology.OntModel;

/**
 * Holds a copy of the OBO Disese Ontology on disk. This gets loaded on startup.
 * 
 * @author klc 
 * @version $Id: DiseaseOntologyService.java,v 1.1 2009/12/18 00:30:07 paul Exp $
 */
public class DiseaseOntologyService extends AbstractOntologyService {

    private static final String DISEASE_ONTOLOGY_URL = "url.diseaseOntology";

    /*
     * (non-Javadoc)
     * @see ubic.gemma.ontology.AbstractOntologyService#getOntologyName()
     */
    @Override
    protected String getOntologyName() {
        return "diseaseOntology";
    }

    @Override
    protected String getOntologyUrl() {
        return Configuration.getString( DISEASE_ONTOLOGY_URL );
    }

    @Override
    protected OntModel loadModel( String url ) {
        return OntologyLoader.loadPersistentModel( url, false );
    }

}
