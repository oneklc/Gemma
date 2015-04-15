/*
 * The Gemma project
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
package ubic.gemma.datastructure.matrix;

import java.util.Collection;
import java.util.List;

import ubic.gemma.model.common.quantitationtype.QuantitationType;
import ubic.gemma.model.expression.bioAssay.BioAssay;
import ubic.gemma.model.expression.bioAssayData.BioAssayDimension;
import ubic.gemma.model.expression.biomaterial.BioMaterial;
import ubic.gemma.model.expression.designElement.CompositeSequence;
import ubic.gemma.model.expression.experiment.ExpressionExperiment;

/**
 * Represents a matrix of data from an expression experiment.
 * <p>
 * Expression data is rather complex, so we have to handle some messy cases.
 * <p>
 * The key problem is how to unambiguously identify rows and columns in the matrix. This is greatly complicated by the
 * fact that experiments can combine data from multiple array designs in various ways.
 * <p>
 * Put it together, and the result is that there can be more than one BioAssay per column; the same BioMaterial can be
 * used in multiple columns (supported implictly). There can also be more than on BioMaterial in one column (we don't
 * support this yet either). The same BioSequence can be found in multiple rows. A row can contain data from more than
 * one DesignElement.
 * <p>
 * There are a few constraints: a particular DesignElement can only be used once, in a single row. At the moment we do
 * not directly support technical replicates, though this should be possible. A BioAssay can only appear in a single
 * column.
 * <p>
 * For some operations a ExpressionDataMatrixRowElement object is offered, which encapsulates a combination of
 * DesignElements, a BioSequence, and an index. The list of these can be useful for iterating over the rows of the
 * matrix.
 * 
 * @author pavlidis
 * @author keshav
 * @version $Id: ExpressionDataMatrix.java,v 1.32 2013/05/18 18:14:50 paul Exp $
 */
public interface ExpressionDataMatrix<T> {

    /**
     * Return the expression experiment this matrix is holding data for.
     * 
     * @return
     */
    public ExpressionExperiment getExpressionExperiment();

    /**
     * Return a row that 'came from' the given design element.
     * 
     * @param designElement
     * @return
     */
    public T[] getRow( CompositeSequence designElement );

    /**
     * Access a single row of the matrix, by index. A complete row is returned.
     * 
     * @param index
     * @return
     */
    public T[] getRow( Integer index );

    /**
     * Access a single column of the matrix.
     * 
     * @param bioAssay
     * @return T[]
     */
    public T[] getColumn( BioAssay bioAssay );

    public int getColumnIndex( BioMaterial bioMaterial );

    public int getRowIndex( CompositeSequence designElement );

    /**
     * Access a single column of the matrix.
     * 
     * @param column index
     * @return T[]
     */
    public T[] getColumn( Integer column );

    /**
     * Access a single value of the matrix. Note that because there can be multiple bioassays per column and multiple
     * designelements per row, it is possible for this method to retrieve a data that does not come from the bioassay
     * and/or designelement arguments.
     * 
     * @param designElement
     * @param bioAssay
     * @return T
     */
    public T get( CompositeSequence designElement, BioAssay bioAssay );

    /**
     * Access a single value of the matrix. This is generally the easiest way to do it.
     * 
     * @param row
     * @param column
     * @return
     */
    public T get( int row, int column );

    /**
     * Set a value in the matrix, by index
     * 
     * @param row
     * @param column
     * @param value
     */
    public void set( int row, int column, T value );

    /**
     * Access a submatrix
     * 
     * @param designElements
     * @param bioAssays
     * @return T[][]
     */
    public T[][] get( List<CompositeSequence> designElements, List<BioAssay> bioAssays );

    /**
     * Access a submatrix
     * 
     * @param designElements
     * @return T[][]
     */
    public T[][] getRows( List<CompositeSequence> designElements );

    /**
     * Access a submatrix slice by columns
     * 
     * @param bioAssays
     * @return
     */
    public T[][] getColumns( List<BioAssay> bioAssays );

    /**
     * Access the entire matrix.
     * 
     * @return T[][]
     */
    public T[][] getRawMatrix();

    /**
     * @return list of elements representing the row 'labels'.
     */
    public List<ExpressionDataMatrixRowElement> getRowElements();

    /**
     * @param index
     * @return BioMaterial. Note that if this represents a subsetted data set, the BioMaterial may be a lightweight
     *         'fake'.
     */
    public BioMaterial getBioMaterialForColumn( int index );

    /**
     * @param index
     * @return
     */
    public CompositeSequence getDesignElementForRow( int index );

    /**
     * @param index
     * @return bioassays that contribute data to the column. There can be multiple bioassays if more than one array was
     *         used in the study.
     */
    public Collection<BioAssay> getBioAssaysForColumn( int index );

    /**
     * Produce a BioAssayDimension representing the matrix columns for a specific row. The designelement argument is
     * needed because a matrix can combine data from multiple array designs, each of which will generate its own
     * bioassaydimension. Note that if this represents a subsetted data set, the return value may be a lightweight
     * 'fake'.
     * 
     * @param designElement
     * @return
     */
    public BioAssayDimension getBioAssayDimension( CompositeSequence designElement );

    /**
     * Total number of columns.
     * 
     * @return int
     */
    public int columns();

    /**
     * Number of columns that use the given design element. Useful if the matrix includes data from more than one array
     * design.
     * 
     * @param el
     * @return
     */
    public int columns( CompositeSequence el );

    /**
     * @return int
     */
    public int rows();

    /**
     * Return the quantitation types for this matrix. Often (usually) there will be just one.
     * 
     * @return
     */
    public Collection<QuantitationType> getQuantitationTypes();

    /**
     * @return The bioassaydimension that covers all the biomaterials in this matrix.
     * @throws IllegalStateException if there isn't a single bioassaydimension that encapsulates all the biomaterials
     *         used in the experiment.
     */
    public BioAssayDimension getBestBioAssayDimension();

}
