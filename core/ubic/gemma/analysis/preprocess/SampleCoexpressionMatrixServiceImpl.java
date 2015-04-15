/*
 * The Gemma project
 * 
 * Copyright (c) 2011 University of British Columbia
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
package ubic.gemma.analysis.preprocess;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ubic.basecode.dataStructure.matrix.DenseDoubleMatrix;
import ubic.basecode.dataStructure.matrix.DoubleMatrix;
import ubic.basecode.math.MatrixStats;
import ubic.gemma.analysis.preprocess.filter.FilterConfig;
import ubic.gemma.analysis.service.ExpressionDataMatrixService;
import ubic.gemma.datastructure.matrix.ExpressionDataDoubleMatrix;
import ubic.gemma.datastructure.matrix.ExpressionDataMatrixColumnSort;
import ubic.gemma.model.expression.bioAssay.BioAssay;
import ubic.gemma.model.expression.bioAssayData.ProcessedExpressionDataVector;
import ubic.gemma.model.expression.bioAssayData.ProcessedExpressionDataVectorService;
import ubic.gemma.model.expression.biomaterial.BioMaterial;
import ubic.gemma.model.expression.designElement.CompositeSequence;
import ubic.gemma.model.expression.experiment.ExpressionExperiment;

/**
 * Manage the "sample correlation/coexpression" matrices.
 * 
 * @author paul
 * @version $Id: SampleCoexpressionMatrixServiceImpl.java,v 1.7 2013/05/01 19:23:17 paul Exp $
 */
@Component
public class SampleCoexpressionMatrixServiceImpl implements SampleCoexpressionMatrixService {

    /**
     * @param matrix
     * @return
     */
    public static DoubleMatrix<BioAssay, BioAssay> getMatrix( ExpressionDataDoubleMatrix matrix ) {

        DoubleMatrix<BioMaterial, CompositeSequence> transposeR = matrix.getMatrix().transpose();

        DoubleMatrix<BioAssay, CompositeSequence> transpose = new DenseDoubleMatrix<BioAssay, CompositeSequence>(
                transposeR.getRawMatrix() );
        transpose.setColumnNames( transposeR.getColNames() );
        for ( int i = 0; i < transpose.rows(); i++ ) {
            BioAssay s = transposeR.getRowName( i ).getBioAssaysUsedIn().iterator().next();
            transpose.setRowName( s, i );
        }

        DoubleMatrix<BioAssay, BioAssay> mat = MatrixStats.correlationMatrix( transpose );

        return mat;
    }

    @Autowired
    private ExpressionDataMatrixService expressionDataMatrixService;

    @Autowired
    private ProcessedExpressionDataVectorService processedExpressionDataVectorService;

    @Autowired
    private SampleCoexpressionMatrixHelperService sampleCoexpressionMatrixHelperService;

    /*
     * (non-Javadoc)
     * 
     * @see
     * ubic.gemma.analysis.preprocess.SampleCoexpressionMatrixService#create(ubic.gemma.model.expression.experiment.
     * ExpressionExperiment, boolean)
     */
    @Override
    public DoubleMatrix<BioAssay, BioAssay> create( ExpressionExperiment ee, boolean forceRecompute ) {
        DoubleMatrix<BioAssay, BioAssay> mat = sampleCoexpressionMatrixHelperService.load( ee );

        if ( forceRecompute || mat == null ) {

            Collection<ProcessedExpressionDataVector> processedVectors = processedExpressionDataVectorService
                    .getProcessedDataVectors( ee );

            if ( processedVectors.isEmpty() ) {
                throw new IllegalArgumentException( "Must have processed vectors created first" );
            }

            mat = create( ee, processedVectors );
        }

        mat = ExpressionDataMatrixColumnSort.orderByExperimentalDesign( mat );
        mat = mat.subsetRows( mat.getColNames() ); // enforce same order on rows.
        return mat;

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * ubic.gemma.analysis.preprocess.SampleCoexpressionMatrixService#create(ubic.gemma.model.expression.experiment.
     * ExpressionExperiment, java.util.Collection)
     */
    @Override
    public DoubleMatrix<BioAssay, BioAssay> create( ExpressionExperiment ee,
            Collection<ProcessedExpressionDataVector> processedVectors ) {

        FilterConfig fconfig = new FilterConfig();
        fconfig.setIgnoreMinimumRowsThreshold( true );
        fconfig.setIgnoreMinimumSampleThreshold( true );
        ExpressionDataDoubleMatrix datamatrix = expressionDataMatrixService.getFilteredMatrix( ee, fconfig,
                processedVectors );

        DoubleMatrix<BioAssay, BioAssay> mat = getMatrix( datamatrix );
        assert mat != null;

        sampleCoexpressionMatrixHelperService.create( mat, datamatrix.getBestBioAssayDimension(),
                datamatrix.getExpressionExperiment() );

        return mat;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * ubic.gemma.analysis.preprocess.SampleCoexpressionMatrixService#delete(ubic.gemma.model.expression.experiment.
     * ExpressionExperiment)
     */
    @Override
    public void delete( ExpressionExperiment ee ) {
        sampleCoexpressionMatrixHelperService.removeForExperiment( ee );
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * ubic.gemma.analysis.preprocess.SampleCoexpressionMatrixService#findOrCreate(ubic.gemma.model.expression.experiment
     * .ExpressionExperiment)
     */
    @Override
    public DoubleMatrix<BioAssay, BioAssay> findOrCreate( ExpressionExperiment expressionExperiment ) {
        return create( expressionExperiment, false );
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * ubic.gemma.analysis.preprocess.SampleCoexpressionMatrixService#hasMatrix(ubic.gemma.model.expression.experiment
     * .ExpressionExperiment)
     */
    @Override
    public boolean hasMatrix( ExpressionExperiment ee ) {
        return sampleCoexpressionMatrixHelperService.load( ee ) != null;
    }
}
