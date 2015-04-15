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
package ubic.gemma.annotation.geommtx;

import ubic.gemma.model.common.description.VocabCharacteristic;

/**
 * @author paul
 * @version $Id: PredictedCharacteristicFactory.java,v 1.12 2012/01/26 00:32:18 paul Exp $
 */
public interface PredictedCharacteristicFactory {

    /**
     * Infer the category
     * 
     * @param URI
     * @return
     */
    public abstract String getCategory( String URI );

    /**
     * @param URI
     * @return
     */
    public abstract VocabCharacteristic getCharacteristic( String URI );

    public abstract String getLabel( String uri );

    public abstract boolean hasLabel( String uri );

    public abstract void init();

}