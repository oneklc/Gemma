
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

package ubic.gemma.genome.gene;

import java.util.Collection;

import ubic.gemma.model.genome.gene.GeneSetValueObject;
import ubic.gemma.session.GemmaSessionBackedValueObject;
/**
 * TODO Document Me
 * 
 * @author tvrossum
 * @version $Id: SessionBoundGeneSetValueObject.java,v 1.11 2012/05/08 17:38:59 paul Exp $
 */
public class SessionBoundGeneSetValueObject extends GeneSetValueObject implements GemmaSessionBackedValueObject {

    private static final long serialVersionUID = 5073203626044664184L;
    private boolean modified;

    /**
     * default constructor to satisfy java bean contract
     */
    public SessionBoundGeneSetValueObject() {
        super();
        this.setModified( false );
    }

    /**
     * @param modified the modified to set
     */
    @Override
    public void setModified( boolean modified ) {
        this.modified = modified;
    }
    /**
     * @return the modified
     */
    @Override
    public boolean isModified() {
        return modified;
    }

    @Override
    public boolean equals( GemmaSessionBackedValueObject ervo ) {
        if(ervo.getClass().equals( this.getClass() ) && ervo.getId().equals( this.getId() )){
            return true;
        }
       return false;
    }
    @Override
    public Collection<Long> getMemberIds() {
        return this.getGeneIds();
    } 

}
