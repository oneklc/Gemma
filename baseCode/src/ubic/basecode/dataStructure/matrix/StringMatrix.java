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
package ubic.basecode.dataStructure.matrix;

import org.apache.commons.lang.StringUtils;

import cern.colt.matrix.ObjectMatrix1D;
import cern.colt.matrix.impl.DenseObjectMatrix2D;

/**
 * A NamedMatrix containing String objects.
 * 
 * @author Paul Pavlidis
 * @version $Id: StringMatrix.java,v 1.2 2010/04/05 06:31:17 paul Exp $
 */
public class StringMatrix<R, C> extends AbstractMatrix<R, C, String> implements ObjectMatrix<R, C, String> {

    /**
     * 
     */
    private static final long serialVersionUID = -7369003979104984162L;
    private DenseObjectMatrix2D matrix;

    public StringMatrix( int x, int y ) {
        super();
        matrix = new DenseObjectMatrix2D( x, y );
    }

    public String getEntry( int row, int column ) {
        return get( row, column );
    }

    /**
     * @return
     */
    public int columns() {
        return matrix.columns();
    }

    /**
     * @param row
     * @param column
     * @return
     */
    public String get( int row, int column ) {
        return ( String ) matrix.get( row, column );
    }

    public String[] getColObj( int col ) {
        String[] result = new String[rows()];
        for ( int i = 0; i < rows(); i++ ) {
            result[i] = get( i, col );
        }
        return result;
    }

    public String[] getColumn( int col ) {
        String[] result = new String[rows()];
        for ( int i = 0; i < rows(); i++ ) {
            result[i] = get( i, col );
        }
        return result;
    }

    public String getObject( int row, int col ) {
        return get( row, col );
    }

    public String[] getRow( int row ) {
        String[] result = new String[columns()];
        for ( int i = 0; i < columns(); i++ ) {
            result[i] = get( row, i );
        }
        return result;
    }

    /**
     * Strings are considered missing if they are whitespace, null or empty.
     */
    public boolean isMissing( int i, int j ) {
        return StringUtils.isBlank( get( i, j ) );
    }

    /**
     * @return
     */
    public int rows() {
        return matrix.rows();
    }

    /**
     * @param row
     * @param column
     * @param value
     */
    public void set( int row, int column, String value ) {
        matrix.set( row, column, value );
    }

    /**
     * @return
     */
    public int size() {
        return matrix.size();
    }

    /**
     * @return java.lang.String
     */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append( "label" );
        for ( int i = 0; i < columns(); i++ ) {
            buf.append( "\t" + getColName( i ) );
        }
        buf.append( "\n" );

        for ( int i = 0; i < rows(); i++ ) {
            buf.append( getRowName( i ) );
            for ( int j = 0; j < columns(); j++ ) {
                buf.append( "\t" + get( i, j ) );
            }
            buf.append( "\n" );
        }
        return buf.toString();
    }

    /**
     * @param column
     * @return
     */
    public ObjectMatrix1D viewColumn( int column ) {
        return matrix.viewColumn( column );
    }

    /**
     * @param row
     * @return
     */
    public ObjectMatrix1D viewRow( int row ) {
        return matrix.viewRow( row );
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.basecode.dataStructure.matrix.NamedObjectMatrix#get(java.lang.Object, java.lang.Object)
     */
    public String get( R row, C column ) {
        return this.get( this.getRowIndexByName( row ), this.getColIndexByName( column ) );
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.basecode.dataStructure.matrix.NamedMatrix#set(java.lang.Object, java.lang.Object, java.lang.Object)
     */
    public void setByKeys( R r, C c, String v ) {
        this.set( getRowIndexByName( r ), getColIndexByName( c ), v );
    }

    public String getByKeys( R r, C c ) {
        return this.get( getRowIndexByName( r ), getColIndexByName( c ) );
    }

}