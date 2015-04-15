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

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A graph node that has the concept of parents and children. Keys can be anything, but probably Strings or Integers.
 * 
 * @author Paul Pavlidis
 * @version $Id: DirectedGraphNode.java,v 1.5 2009/11/11 02:03:56 paul Exp $
 */
public class DirectedGraphNode<K, V> extends AbstractGraphNode<K, V> implements Comparable<DirectedGraphNode<K, V>> {

    protected Set<K> parents;
    // immeddiate parents, references to other GraphNodes by keys.
    protected Set<K> children;
    // immediate children, references to other GraphNodes by keys.

    protected int topoSortOrder = 0;

    DirectedGraph<K, V> graph;

    /**
     * @param key Object
     * @param value Object
     * @param graph Graph
     */
    public DirectedGraphNode( K key, V value, DirectedGraph<K, V> graph ) {
        super( key, value );
        parents = new LinkedHashSet<K>();
        children = new LinkedHashSet<K>();
        this.graph = graph;
    }

    /**
     * @param newChildKey Object
     */
    public void addChild( K newChildKey ) {
        children.add( newChildKey );
    }

    /**
     * @param newParentKey Object
     */
    public void addParent( K newParentKey ) {
        parents.add( newParentKey );
    }

    /**
     * Makes a copy of this node. It does not make a deep copy of the contents. This should be used when making
     * subgraphs.
     * 
     * @return Object
     */
    @Override
    public DirectedGraphNode<K, V> clone() {
        DirectedGraphNode<K, V> r = new DirectedGraphNode<K, V>( key, item, graph );
        for ( Iterator<K> it = this.getParentIterator(); it.hasNext(); ) {
            K j = it.next();
            r.addParent( j );
        }

        for ( Iterator<K> it = this.getChildIterator(); it.hasNext(); ) {
            K j = it.next();
            r.addChild( j );
        }
        return r;
    }

    /**
     * Uses the topological sort order.
     * 
     * @param o Object
     * @return int
     */
    public int compareTo( DirectedGraphNode<K, V> o ) {
        int ord = o.getTopoSortOrder();
        if ( ord < this.topoSortOrder ) {
            return 1;
        } else if ( ord > this.topoSortOrder ) {
            return -1;
        }
        return 0;
    }

    /**
     * Get all the children of this node, recursively.
     */
    public Set<DirectedGraphNode<K, V>> getAllChildNodes() {
        return this.getAllChildNodes( null );
    }

    /**
     * Get all the parents of this node, recursively.
     * 
     * @return
     */
    public Set<DirectedGraphNode<K, V>> getAllParentNodes() {
        return this.getAllParentNodes( null );
    }

    /**
     * @return int how many children this node has, determined recursively.
     */
    public int getChildCount() {
        return getAllChildNodes( null ).size();
    }

    /**
     * Get the subgraph starting from this node, including this node.
     * 
     * @return Graph
     */
    public DirectedGraph<K, V> getChildGraph() {
        Set<DirectedGraphNode<K, V>> k = this.getAllChildNodes();
        k.add( this );

        DirectedGraph<K, V> returnVal = new DirectedGraph<K, V>();
        for ( DirectedGraphNode<K, V> m : k ) {
            returnVal.addNode( m.clone() );
        }
        returnVal.prune(); // failing to do this will cause all kinds of problems
        return returnVal;
    }

    /**
     * @return Object
     */
    public Set<K> getChildKeys() {
        return children;
    }

    /**
     * Get the immediate children of this node. References to the DirectedGraphNodes are given, as opposed to key
     * values.
     * 
     * @return Set containing the child nodes of this node.
     */
    public Set<DirectedGraphNode<K, V>> getChildNodes() {
        Set<DirectedGraphNode<K, V>> f = new LinkedHashSet<DirectedGraphNode<K, V>>();
        for ( Iterator<K> i = this.getChildIterator(); i.hasNext(); ) {
            K k = i.next();
            f.add( getGraph().get( k ) );
        }
        return f;
    }

    public DirectedGraph<K, V> getGraph() {
        return graph;
    }

    /**
     * @return Object
     */
    public Set<K> getParentKeys() {
        return parents;
    }

    /**
     * Get the immediate parents of this node. References to the DirectedGraphNodes are given, as opposed to key values.
     * 
     * @return Set
     */
    public Set<DirectedGraphNode<K, V>> getParentNodes() {
        Set<DirectedGraphNode<K, V>> f = new LinkedHashSet<DirectedGraphNode<K, V>>();
        for ( Iterator<K> i = this.getParentIterator(); i.hasNext(); ) {
            K k = i.next();
            f.add( getGraph().get( k ) );
        }
        return f;
    }

    /**
     * @return int
     */
    public int getTopoSortOrder() {
        return topoSortOrder;
    }

    /**
     * Check to see if this node has a particular immediate child.
     * 
     * @param j Object
     * @return boolean
     */
    public boolean hasChild( Object j ) {
        return children.contains( j );
    }

    /**
     * Check to see if this node has a particular immediate parent.
     * 
     * @param j Object
     * @return boolean
     */
    public boolean hasParent( Object j ) {
        return parents.contains( j );
    }

    /**
     * @return int number of immediate parents this node has.
     */
    public int inDegree() {
        return parents.size();
    }

    /**
     * @return
     */
    public boolean isLeaf() {
        return children.size() == 0;
    }

    /**
     * @return int how many parents this node has, determined recursively.
     */
    public int numParents() {
        return getAllParentNodes( null ).size();
    }

    /**
     * @return int number of immediate children this node has.
     */
    public int outDegree() {
        return children.size();
    }

    /**
     * Remove connections that are to nodes not contained in this graph
     */
    public void prune() {
        for ( Iterator<K> it = this.getChildIterator(); it.hasNext(); ) {
            K j = it.next();
            DirectedGraphNode<K, V> k = getGraph().get( j );
            if ( k == null ) {
                if ( log.isDebugEnabled() ) {
                    log.debug( "Pruned child " + j + " from " + this );
                }
                children.remove( j );
            }

        }

        for ( Iterator<K> it = this.getParentIterator(); it.hasNext(); ) {
            K j = it.next();
            DirectedGraphNode<K, V> k = getGraph().get( j );
            if ( k == null ) {
                if ( log.isDebugEnabled() ) {
                    log.debug( "Pruned parent " + j + " from " + this );
                }
                parents.remove( j );
            }

        }

    }

    public void setGraph( DirectedGraph<K, V> graph ) {
        this.graph = graph;
    }

    /**
     * @param i int
     */
    public void setTopoSortOrder( int i ) {
        topoSortOrder = i;
    }

    @Override
    public String toString() {
        return this.getItem().toString();
    }

    /** ************* private methods *************** */

    private Set<DirectedGraphNode<K, V>> getAllChildNodes( Set<DirectedGraphNode<K, V>> list ) {
        if ( list == null ) {
            list = new LinkedHashSet<DirectedGraphNode<K, V>>();
        }

        for ( Iterator<K> it = this.getChildIterator(); it.hasNext(); ) {
            K j = it.next();
            list.add( getGraph().get( j ) );
            getGraph().get( j ).getAllChildNodes( list );
        }
        return list;
    }

    private Set<DirectedGraphNode<K, V>> getAllParentNodes( Set<DirectedGraphNode<K, V>> list ) {
        if ( list == null ) {
            list = new LinkedHashSet<DirectedGraphNode<K, V>>();
        }

        for ( Iterator<K> it = this.getParentIterator(); it.hasNext(); ) {
            K j = it.next();
            list.add( getGraph().get( j ) );
            getGraph().get( j ).getAllParentNodes( list );
        }
        return list;
    }

    private Iterator<K> getChildIterator() {
        return children.iterator();
    }

    private Iterator<K> getParentIterator() {
        return parents.iterator();
    }

}