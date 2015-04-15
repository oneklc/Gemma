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
package ubic.basecode.dataStructure.graph;

import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.basecode.dataStructure.Visitable;

/**
 * @author Paul Pavlidis
 * @version $Id: AbstractGraph.java,v 1.4 2008/02/13 22:15:47 paul Exp $
 */
public abstract class AbstractGraph<R extends GraphNode<K, V>, K, V> implements Graph<R, K, V> {

    private static Log log = LogFactory.getLog( AbstractGraph.class.getName() );

    public AbstractGraph() {
    }

    /**
     * Create a new graph from a set of nodes. This allows us to easily make subtrees.
     * 
     * @param nodes Set of AbstractGraphNodes.
     */
    public AbstractGraph( Set<R> nodes ) {
        for ( GraphNode<K, V> n : nodes ) {
            this.addNode( n.getKey(), n.getItem() );
        }
    }

    /**
     *  
     */
    public abstract void addNode( K key, V value );

    /**
     * @param node GraphNode
     */
    public abstract void addNode( R node );

    /**
     * @param key Object
     * @return true if the graph contains an item referenced by key, false otherwise.
     */
    public abstract boolean containsKey( K key );

    /**
     * Retrieve a node by key. To get the contents of a node use getNodeContents(key)
     * 
     * @param key Object
     * @see #getNodeContents(Object)
     * @return AbstractGraphNode referenced by the key.
     */
    public R get( K key ) {
        return getItems().get( key );
    }

    /**
     * @return Map
     */
    public abstract Map<K, R> getItems();

    /**
     * Retrieve the contents of a node by key.
     * 
     * @see #get
     * @param key Object
     * @return The object contained by a node, not the node itself.
     */
    public V getNodeContents( K key ) {
        if ( !getItems().containsKey( key ) ) return null;
        return getItems().get( key ).getItem();
    }

    /**
     * Reset the 'visited' marks of the graph to false.
     */
    public void unmarkAll() {
        for ( K item : getItems().keySet() ) {
            if ( !( item instanceof Visitable ) ) {
                log.debug( "Got " + item.getClass().getName() );
                break;
            }
            ( ( Visitable ) item ).unMark();
        }
    }
}