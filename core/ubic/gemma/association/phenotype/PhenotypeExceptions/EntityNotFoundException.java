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
package ubic.gemma.association.phenotype.PhenotypeExceptions;

/**
 * Used to signifity that an entity was not found in the system.
 * <hr>
 * <p>
 * Copyright (c) 2004-2006 University of British Columbia
 * 
 * @author pavlidis
 * @version $Id: EntityNotFoundException.java,v 1.1 2011/09/23 18:17:38 nicolas Exp $
 */
public class EntityNotFoundException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = -4361183252269974829L;

    /**
     * @param string
     */
    public EntityNotFoundException( String string ) {
        super( string );
    }

}
