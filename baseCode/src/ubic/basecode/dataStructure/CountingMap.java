/*
 * The Gemma project
 * 
 * Copyright (c) 2008 University of British Columbia
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author luke
 * @version $Id: CountingMap.java,v 1.2 2009/12/08 20:07:19 lfrench Exp $
 * @param <K>
 */
public class CountingMap<K> implements Map<K, Integer> {
    private Map<K, Integer> map;

    /**
     * Constructs a CountingMap backed by a simple HashMap.
     */
    public CountingMap() {
        this( new HashMap<K, Integer>() );
    }

    /**
     * Constructs a CountingMap backed by the specified Map.
     * 
     * @param map the backing Map
     */
    public CountingMap( Map<K, Integer> map ) {
        this.map = map;
    }

    /**
     * Increments the count associated with the specified key and returns the incremented count. If the key doesn't
     * already exist in the map, it will be added.
     * 
     * @param key the key whose associated count is to be incremented
     * @return the incremented value associated with the specified key
     */
    public int increment( K key ) {
        Integer i = map.get( key );
        if ( i == null ) i = new Integer( 0 );
        map.put( key, ++i );
        return i;
    }

    /**
     * Returns the count associated with the specified key, or zero if the key has never been incremented.
     * 
     * @param key the key whose associated count is to be returned
     * @return the count associated with the specified key, or zero if the key has never been incremented
     */
    public int count( K key ) {
        Integer i = map.get( key );
        return i == null ? 0 : i;
    }

    /**
     * Returns true if the specified key has ever been incremented, false otherwise.
     * 
     * @param key the key whose presence is to be tested
     * @return true if the specified key has ever been incremented, false otherwise
     */
    public boolean seen( K key ) {
        return map.containsKey( key );
    }

    /**
     * Returns a list of the keys in this map, sorted by ascending count.
     * 
     * @return a list of the keys in this map, sorted by ascending count
     */
    public List<K> sortedKeyList() {
        return sortedKeyList( false );
    }

    /**
     * Returns a list of the keys in this map, sorted as specified.
     * 
     * @param sortDescending true to sort by descending count, false to sort by ascending count
     * @return a list of the keys in this map, sorted as specified.
     */
    public List<K> sortedKeyList( boolean sortDescending ) {
        List<K> keys = new ArrayList<K>( keySet() );
        Collections.sort( keys, sortDescending ? new DescendingCountComparator() : new AscendingCountComparator() );
        return keys;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Map#clear()
     */
    public void clear() {
        map.clear();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    public boolean containsKey( Object key ) {
        return map.containsKey( key );
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    public boolean containsValue( Object value ) {
        return map.containsValue( value );
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Map#entrySet()
     */
    public Set<java.util.Map.Entry<K, Integer>> entrySet() {
        return map.entrySet();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Map#get(java.lang.Object)
     */
    public Integer get( Object key ) {
        return map.containsKey( key ) ? map.get( key ) : 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Map#isEmpty()
     */
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Map#keySet()
     */
    public Set<K> keySet() {
        return map.keySet();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    public Integer put( K key, Integer value ) {
        return map.put( key, value );
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Map#putAll(java.util.Map)
     */
    public void putAll( Map<? extends K, ? extends Integer> t ) {
        map.putAll( t );
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Map#remove(java.lang.Object)
     */
    public Integer remove( Object key ) {
        return map.remove( key );
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Map#size()
     */
    public int size() {
        return map.size();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Map#values()
     */
    public Collection<Integer> values() {
        return map.values();
    }

    /**
     * Returns the sum of all counts in the map.
     */
    public int summation() {
        int summation = 0;
        for ( int value : map.values() ) {
            summation += value;
        }
        return summation;
    }

    @Override
    public boolean equals( Object o ) {
        return map.equals( o );
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }

    private abstract class CountComparator implements Comparator<K> {
    }

    private class AscendingCountComparator extends CountComparator {
        /*
         * (non-Javadoc)
         * 
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare( K key1, K key2 ) {
            return map.get( key1 ).compareTo( map.get( key2 ) );
        }
    }

    private class DescendingCountComparator extends CountComparator {
        /*
         * (non-Javadoc)
         * 
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare( K key1, K key2 ) {
            return map.get( key2 ).compareTo( map.get( key1 ) );
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder( "[" );
        boolean first = true;
        for ( K key : keySet() ) {
            if ( !first ) sb.append( ", " );
            sb.append( key.toString() + "=" + map.get( key ) );
            first = false;
        }
        return sb.toString() + "]";
    }

}
