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
package ubic.gemma.web.controller.common.description.bibref;

/**
 * Supports seaching of pubmed references
 * 
 * @author pavlidis
 * @version $Id: PubMedSearchCommand.java,v 1.3 2010/06/08 20:28:13 paul Exp $
 */
public class PubMedSearchCommand {
    String accession;

    public PubMedSearchCommand( String accession ) {
        super();
        this.accession = accession;
    }

    /**
     * @return the accession
     */
    public String getAccession() {
        return this.accession;
    }

    /**
     * @param accession the accession to set
     * @spring.validator type="positiveNonZeroInteger"
     */
    public void setAccession( String accession ) {
        this.accession = accession;
    }
}
