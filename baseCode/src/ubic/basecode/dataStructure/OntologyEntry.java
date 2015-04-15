/*
 * The baseCode project
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
package ubic.basecode.dataStructure;

/**
 * A class representing a descriptive term that can be associated with things.
 * 
 * @author Paul Pavlidis
 * @version $Id: OntologyEntry.java,v 1.4 2010/04/29 10:37:01 paul Exp $
 */
public class OntologyEntry {

    private String id = "";
    private String name = "";
    private String definition = "";

    /**
     * @param id
     */
    public OntologyEntry( String id ) {
        this.id = id;
    }

    /**
     * @param id
     * @param name
     * @param def
     */
    public OntologyEntry( String id, String name, String def ) {
        this.id = id;
        this.name = name;
        this.definition = def;
    }

    /**
     * @return
     */
    public String getDefinition() {
        return definition;
    }

    /**
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     * @return
     */
    public String getName() {
        return name.intern();
    }

    /**
     * @param d
     */
    public void setDefinition( String d ) {
        definition = d;
    }

    /**
     * @param n
     */
    public void setName( String n ) {
        name = n;
    }

    @Override
    public String toString() {
        return id + ": \t" + name;
    }

}