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
package ubic.basecode.bio.geneset;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.tree.DefaultTreeModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

import ubic.basecode.bio.GOEntry;
import ubic.basecode.dataStructure.graph.DirectedGraph;
import ubic.basecode.dataStructure.graph.DirectedGraphNode;
import ubic.basecode.util.FileTools;
import ubic.basecode.xml.GOParser;

/**
 * Read data from GO XML file, store in easy-to-use data structure.
 * 
 * @author Paul Pavlidis
 * @author Homin Lee
 * @version $Id: GONames.java,v 1.12 2010/05/07 00:35:52 paul Exp $
 */
public class GONames {

    /**
     * Name for aspect when none is defined.
     */
    static public final String NO_ASPECT_AVAILABLE = "[No aspect available]";

    /**
     * Name for root of tree representing user-defined gene sets.
     */
    public static final String USER_DEFINED = "User-defined or modified";

    /**
     * 
     */
    private static final String NO_DESCRIPTION_AVAILABLE = "[No description available]";

    protected static final Log log = LogFactory.getLog( GONames.class );

    private Map<String, String> goNameMap;
    private Set<String> newGeneSets = new HashSet<String>();
    private String NO_DEFINITION_AVAILABLE = "[No definition available]";

    private GOParser parser;

    private Map<String, String> oldNameMap;

    /**
     * @param inputStream
     * @throws IOException
     * @throws SAXException
     */
    public GONames( InputStream inputStream ) throws IOException, SAXException {
        if ( inputStream == null ) {
            throw new IOException( "Input stream was null" );
        }
        this.initialize( inputStream );
    }

    /**
     * @param goIds
     * @param goTerms
     */
    public GONames( List<String> goIds, List<String> goTerms ) {
        if ( goIds == null || goTerms == null || goIds.size() != goTerms.size() ) {
            throw new IllegalArgumentException( "Invalid arguments" );
        }
        goNameMap = new HashMap<String, String>();
        for ( int i = 0; i < goIds.size(); i++ ) {
            goNameMap.put( goIds.get( i ), goTerms.get( i ) );
        }
    }

    /**
     * @param filename <code>String</code> The XML file containing class to name mappings. First column is the class id,
     *        second is a description that will be used int program output.
     * @throws IOException
     * @throws SAXException
     */
    public GONames( String fileName ) throws SAXException, IOException {
        if ( fileName == null || fileName.length() == 0 ) {
            throw new IllegalArgumentException( "Invalid filename " + fileName + " or no filename was given" );
        }

        InputStream i = FileTools.getInputStreamFromPlainOrCompressedFile( fileName );
        this.initialize( i );
    }

    /**
     * @param id String
     * @param name String
     */
    public void addGeneSet( String id, String name ) {
        goNameMap.put( id, name );
        newGeneSets.add( id );
        addClassToUserDefined( id, name );
    }

    /**
     * @param classID
     */
    public void deleteGeneSet( String classID ) {
        newGeneSets.remove( classID );
        goNameMap.remove( classID );
        if ( this.getGraph() == null ) return;
        this.getGraph().deleteChildFrom( USER_DEFINED, classID );
    }

    /**
     * @param classID
     */
    public void resetGeneSet( String classID ) {
        goNameMap.put( classID, oldNameMap.get( classID ) );
        newGeneSets.remove( classID );
        // removeClassFromUserDefined( classID );
    }

    /**
     * @param go_ID
     * @return the aspect (molecular_function etc) for an id.
     */
    public String getAspectForId( String geneSetId ) {
        if ( !goNameMap.containsKey( geneSetId ) ) {
            return NO_ASPECT_AVAILABLE;
        }

        if ( getGraph() == null ) return NO_ASPECT_AVAILABLE;
        if ( getGraph().getNodeContents( geneSetId ) == null ) {
            log.debug( "No node for " + geneSetId );
            return NO_ASPECT_AVAILABLE;
        }

        GOEntry node = getGraph().getNodeContents( geneSetId );
        if ( node.getAspect() == null || node.getAspect().equals( NO_ASPECT_AVAILABLE ) ) {
            Collection<String> parents = getParents( geneSetId );
            if ( parents == null ) return NO_ASPECT_AVAILABLE;
            for ( String parent : parents ) {
                String aspect = getAspectForId( parent );
                if ( aspect != null && !aspect.equals( NO_ASPECT_AVAILABLE ) ) {
                    node.setAspect( aspect );
                    return aspect;
                }
            }
        }
        return node.getAspect();
    }

    /**
     * @param id
     * @return a Set containing the ids of geneSets which are immediately below the selected one in the hierarchy.
     */
    public Set<String> getChildren( String id ) {
        if ( getGraph() == null ) return null;
        Set<String> returnVal = new HashSet<String>();
        Set<DirectedGraphNode<String, GOEntry>> children = getGraph().get( id ).getChildNodes();
        for ( Iterator<DirectedGraphNode<String, GOEntry>> it = children.iterator(); it.hasNext(); ) {
            DirectedGraphNode<String, GOEntry> child = it.next();
            String childKey = child.getItem().getId();
            returnVal.add( childKey );
        }
        return returnVal;
    }

    /**
     * @param classid
     * @return
     */
    public String getDefinitionForId( String classid ) {
        if ( !goNameMap.containsKey( classid ) ) {
            return NO_DEFINITION_AVAILABLE;
        }
        if ( getGraph() == null ) return NO_DEFINITION_AVAILABLE;
        if ( getGraph().getNodeContents( classid ) == null ) {
            log.debug( "No node for " + classid );
            return NO_DEFINITION_AVAILABLE;
        }
        GOEntry node = getGraph().getNodeContents( classid );
        return node.getDefinition();
    }

    /**
     * @return graph representation of the GO hierarchy
     */
    public DirectedGraph<String, GOEntry> getGraph() {
        if ( parser == null ) return null;
        return parser.getGraph();
    }

    /**
     * @return Map representation of the GO id - name associations.
     */
    public Map<String, String> getMap() {
        return goNameMap;
    }

    /**
     * @param go_ID
     * @return name of gene set
     */
    public String getNameForId( String go_ID ) {
        assert goNameMap != null;
        if ( !goNameMap.containsKey( go_ID ) ) {
            return NO_DESCRIPTION_AVAILABLE;
        }

        return goNameMap.get( go_ID );
    }

    /**
     * @param id
     * @return a Set containing the ids of geneSets which are immediately above the selected one in the hierarchy.
     */
    public Collection<String> getParents( String id ) {
        if ( getGraph() == null ) return null;
        Collection<String> returnVal = new HashSet<String>();

        if ( !getGraph().containsKey( id ) ) {
            log.debug( "GeneSet " + id + " doesn't exist in graph" ); // this is not really a problem.
            return returnVal;
        }

        Set<DirectedGraphNode<String, GOEntry>> parents = getGraph().get( id ).getParentNodes();
        for ( Iterator<DirectedGraphNode<String, GOEntry>> it = parents.iterator(); it.hasNext(); ) {
            DirectedGraphNode<String, GOEntry> parent = it.next();
            if ( parent == null ) continue;
            GOEntry goEntry = parent.getItem();
            if ( goEntry == null ) continue;
            String parentKey = ( goEntry ).getId();
            returnVal.add( parentKey );
        }
        return returnVal;
    }

    /*
     * 
     */
    public DefaultTreeModel getTreeModel() {
        if ( getGraph() == null ) return null;
        return getGraph().getTreeModel();
    }

    /**
     * Return the Set of all new gene sets (ones which were added/modified after loading the file)
     * 
     * @return
     */
    public Set<String> getUserDefinedGeneSets() {
        return newGeneSets;
    }

    /**
     * Check if a gene set is already defined.
     * 
     * @param id
     * @return
     */
    public boolean isUserDefined( String id ) {
        return newGeneSets.contains( id );
    }

    /**
     * @param id String
     * @param name String
     */
    public void modifyGeneSet( String id, String newName ) {
        oldNameMap.put( id, goNameMap.get( id ) );
        log.debug( "Old description is '" + oldNameMap.get( id ) + "'" );
        this.addGeneSet( id, newName );
    }

    /**
     * @param id
     * @param name
     */
    private void addClassToUserDefined( String id, String name ) {
        if ( getGraph() == null ) return;
        if ( this.getGraph().get( USER_DEFINED ) == null ) {
            log.error( "No user-defined root node!" );
            return;
        }
        goNameMap.put( USER_DEFINED, USER_DEFINED ); // make sure this is set up.
        String definition = getDefinitionForId( id ) == null ? "No definition" : getDefinitionForId( id );
        String aspect = getAspectForId( id ) == null ? USER_DEFINED : getAspectForId( id );
        log.debug( "Adding user-defined gene set to graph: " + id + ", Name:" + name + ", Definition: "
                + definition.substring( 0, Math.min( definition.length(), 30 ) ) + "..., Aspect: " + aspect );
        this.getGraph().addChildTo( USER_DEFINED, id, new GOEntry( id, name, definition, aspect ) );
    }

    /**
     * @param id
     * @deprecated
     */
    @Deprecated
    void removeClassFromUserDefined( String id ) {
        if ( getGraph() == null ) return;
        if ( this.getGraph().get( USER_DEFINED ) == null ) {
            log.error( "No user-defined root node!" );
            return;
        }
        log.debug( "Removing user-defined gene set from graph" );
        this.getGraph().deleteChildFrom( USER_DEFINED, id );
    }

    /**
     * 
     */
    private void initialize( InputStream inputStream ) throws IOException, SAXException {
        this.parser = new GOParser( inputStream );
        goNameMap = parser.getGONameMap();
        oldNameMap = new HashMap<String, String>();
        if ( this.getGraph() == null ) return;
        DirectedGraphNode<String, GOEntry> root = this.getGraph().getRoot();
        this.getGraph().addChildTo( root.getKey(), USER_DEFINED,
                new GOEntry( USER_DEFINED, "", "Gene sets modified or created by the user", USER_DEFINED ) );
    }

}