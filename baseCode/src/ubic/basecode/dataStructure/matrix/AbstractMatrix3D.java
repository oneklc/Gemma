/*
 * The baseCode project
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
package ubic.basecode.dataStructure.matrix;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * TODO Document Me
 * 
 * @author Raymond
 * @version $Id: AbstractMatrix3D.java,v 1.1 2008/08/15 00:10:45 paul Exp $
 */
public abstract class AbstractMatrix3D<R, C, S, V> implements Matrix3D<R, C, S, V> {
    public LinkedHashMap<C, Integer> colMap;
    public LinkedHashMap<R, Integer> rowMap;
    public LinkedHashMap<S, Integer> sliceMap;
    public List<C> colNames;
    public List<R> rowNames;
    public List<S> sliceNames;

    public AbstractMatrix3D() {
        colMap = new LinkedHashMap<C, Integer>();
        rowMap = new LinkedHashMap<R, Integer>();
        sliceMap = new LinkedHashMap<S, Integer>();
        colNames = new ArrayList<C>();
        rowNames = new ArrayList<R>();
        sliceNames = new ArrayList<S>();
    }

    public final void addColumnName( C s, int index ) {
        if ( colNames.contains( s ) ) return;
        colMap.put( s, new Integer( index ) );
        colNames.add( s );
    }

    public final void addRowName( R s, int index ) {
        if ( rowNames.contains( s ) ) return;
        rowMap.put( s, new Integer( index ) );
        rowNames.add( s );

    }

    public final void addSliceName( S s, int index ) {
        if ( sliceNames.contains( s ) ) return;
        sliceMap.put( s, new Integer( index ) );
        sliceNames.add( s );
    }

    public abstract int columns();

    public final boolean containsColumnName( Object columnName ) {
        return colMap.containsKey( columnName );
    }

    public final boolean containsRowName( Object rowName ) {
        return rowMap.containsKey( rowName );
    }

    public final boolean containsSliceName( Object sliceName ) {
        return sliceMap.containsKey( sliceName );
    }

    public final int getColIndexByName( Object s ) {
        Integer index = colMap.get( s );
        if ( index == null ) throw new IllegalArgumentException( s + " not found" );
        return index.intValue();
    }

    public C getColName( int i ) {
        return colNames.get( i );
    }

    public Iterator<C> getColNameIterator() {
        return colNames.iterator();
    }

    public List<C> getColNames() {
        return colNames;
    }

    public int getRowIndexByName( R s ) {
        Integer index = rowMap.get( s );
        if ( index == null ) throw new IllegalArgumentException( s + " not found" );
        return index.intValue();
    }

    public R getRowName( int i ) {
        return rowNames.get( i );
    }

    public Iterator<R> getRowNameIterator() {
        return rowMap.keySet().iterator();
    }

    public List<R> getRowNames() {
        return rowNames;
    }

    public int getSliceIndexByName( S s ) {
        Integer index = sliceMap.get( s );
        if ( index == null ) throw new IllegalArgumentException( s + " not found" );
        return index.intValue();
    }

    public S getSliceName( int i ) {
        return sliceNames.get( i );
    }

    public Iterator<S> getSliceNameIterator() {
        return sliceNames.iterator();
    }

    public List<S> getSliceNames() {
        return sliceNames;
    }

    public boolean hasColNames() {
        return columns() == colNames.size();
    }

    public boolean hasRow( Object r ) {
        return rowMap.containsKey( r );
    }

    public boolean hasRowNames() {
        return rows() == rowNames.size();
    }

    public boolean hasSliceNames() {
        return slices() == sliceNames.size();
    }

    public abstract boolean isMissing( int slice, int row, int col );

    public abstract int numMissing();

    public abstract int rows();

    public void setColumnNames( List<C> v ) {
        colNames = v;
        for ( int i = 0; i < v.size(); i++ )
            colMap.put( v.get( i ), new Integer( i ) );
    }

    public void setRowNames( List<R> v ) {
        rowNames = v;
        for ( int i = 0; i < v.size(); i++ )
            rowMap.put( v.get( i ), new Integer( i ) );
    }

    public void setSliceNames( List<S> v ) {
        sliceNames = v;
        for ( int i = 0; i < v.size(); i++ )
            sliceMap.put( v.get( i ), new Integer( i ) );
    }

    public abstract int slices();

}
