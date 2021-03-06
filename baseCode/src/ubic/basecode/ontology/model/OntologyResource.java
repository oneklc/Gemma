/*
 * The Gemma project
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
package ubic.basecode.ontology.model;

import java.io.Serializable;

/**
 * @author pavlidis
 * @version $Id: OntologyResource.java,v 1.1 2009/12/18 00:30:03 paul Exp $
 */
public interface OntologyResource extends Serializable, Comparable<OntologyResource> {

    public String getLabel();

    public String getUri();
}
