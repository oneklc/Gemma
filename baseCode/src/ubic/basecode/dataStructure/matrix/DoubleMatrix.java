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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cern.colt.list.DoubleArrayList;
import cern.colt.matrix.DoubleMatrix1D;

/**
 * Abstract base class for 2D matrices of double values with named columns and rows.
 * 
 * @author pavlidis
 * @version $Id: DoubleMatrix.java,v 1.4 2010/04/07 15:52:35 paul Exp $
 */
public abstract class DoubleMatrix<R, C> extends AbstractMatrix<R, C, Double> implements PrimitiveMatrix<R, C, Double> {

    protected static Log log = LogFactory.getLog( DoubleMatrix.class.getName() );

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public abstract double[][] asArray();

    public abstract DoubleMatrix<R, C> copy();

    /**
     * @param j
     * @param i
     * @return
     */
    public abstract double get( int j, int i );

    public Double getByKeys( R r, C c ) {
        return this.get( getRowIndexByName( r ), getColIndexByName( c ) );
    }

    public abstract double[] getColumn( int j );

    /**
     * @param s String
     * @return double[]
     */
    public double[] getColumnByName( C s ) {
        return getColumn( getColIndexByName( s ) );
    }

    public Double getEntry( int row, int column ) {
        return get( row, column );
    }

    public double[][] getRawMatrix() {
        double[][] result = new double[this.rows()][];
        for ( int i = 0; i < this.rows(); i++ ) {
            result[i] = this.getRow( i );
        }
        return result;
    }

    public abstract double[] getRow( int i );

    public abstract DoubleArrayList getRowArrayList( int i );

    /**
     * @param s String
     * @return double[]
     */
    public double[] getRowByName( R s ) {
        return getRow( getRowIndexByName( s ) );
    }

    public void setByKeys( R r, C c, Double v ) {
        this.set( getRowIndexByName( r ), getColIndexByName( c ), v );
    }

    /**
     * @param startRow inclusive
     * @param endRow inclusive
     * @return
     */
    public abstract DoubleMatrix<R, C> getRowRange( int startRow, int endRow );

    /*
     * For more advanced matrix writing see the MatrixWriter class (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     * 
     * @see ubic.basecode.io.writer.MatrixWriter
     */
    @Override
    public final String toString() {
        int rows = this.rows();
        int columns = this.columns();
        StringBuffer buf = new StringBuffer();
        int stop = 0;
        buf.append( "# " + rows + "x" + columns + " matrix: showing up to " + MAX_ROWS_TO_PRINT + " rows\n" );
        buf.append( "label" );

        for ( int i = 0; i < columns; i++ ) {
            if ( this.hasColNames() ) {
                buf.append( "\t" + this.getColName( i ) );
            } else {
                buf.append( "\t" + i );
            }
        }
        buf.append( "\n" );
        for ( int j = 0; j < rows; j++ ) {

            if ( this.hasRowNames() ) {
                buf.append( this.getRowName( j ) );
            } else {
                buf.append( j );
            }
            for ( int i = 0; i < columns; i++ ) {
                double value = this.get( j, i );
                if ( Double.isNaN( value ) ) {
                    buf.append( "\t" );
                } else {
                    buf.append( "\t" + String.format( "%.4g", value ) );
                }
            }
            buf.append( "\n" );
            if ( stop > MAX_ROWS_TO_PRINT ) {
                buf.append( "...\n" );
                break;
            }
            stop++;
        }
        return buf.toString();
    }

    /**
     * @param j
     * @return
     */
    public abstract DoubleMatrix1D viewRow( int j );
}
