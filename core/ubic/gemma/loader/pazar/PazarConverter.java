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
package ubic.gemma.loader.pazar;

import ubic.gemma.loader.pazar.model.PazarRecord;
import ubic.gemma.loader.util.converter.Converter;
import ubic.gemma.model.association.PazarAssociation;

/**
 * @author paul
 * @version $Id: PazarConverter.java,v 1.3 2012/01/26 00:32:25 paul Exp $
 */
public interface PazarConverter extends Converter<PazarRecord, PazarAssociation> {

}