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
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ubic.basecode.dataStructure.matrix.DoubleMatrix;
import ubic.basecode.math.Rank;
import ubic.gemma.analysis.expression.diff.DiffExAnalyzer;
import ubic.gemma.analysis.expression.diff.DifferentialExpressionAnalysisConfig;
import ubic.gemma.analysis.preprocess.svd.SVDServiceHelper;
import ubic.gemma.datastructure.matrix.ExpressionDataDoubleMatrix;
import ubic.gemma.expression.experiment.service.ExpressionExperimentService;
import ubic.gemma.model.expression.bioAssay.BioAssay;
import ubic.gemma.model.expression.bioAssayData.ProcessedExpressionDataVector;
import ubic.gemma.model.expression.bioAssayData.ProcessedExpressionDataVectorService;
import ubic.gemma.model.expression.experiment.ExperimentalFactor;
import ubic.gemma.model.expression.experiment.ExpressionExperiment;
import cern.colt.list.DoubleArrayList;

/**
 * Methods to (attempt to) detect outliers in data sets.
 * 
 * @author paul
 * @version $Id: OutlierDetectionServiceImpl.java,v 1.4 2012/06/11 19:05:53 paul Exp $
 */
@Component
public class OutlierDetectionServiceImpl implements OutlierDetectionService {

    private static final int DEFAULT_QUANTILE = 15;

    private static final double DEFAULT_FRACTION = 0.9;

    private static Log log = LogFactory.getLog( OutlierDetectionServiceImpl.class );

    @Autowired
    private ExpressionExperimentService eeService;

    @Autowired
    private DiffExAnalyzer lma;

    @Autowired
    private ProcessedExpressionDataVectorService processedExpressionDataVectorService;

    @Autowired
    private SVDServiceHelper svdService;

    /*
     * (non-Javadoc)
     * 
     * @see
     * ubic.gemma.analysis.preprocess.OutlierDetectionService#identifyOutliers(ubic.gemma.model.expression.experiment
     * .ExpressionExperiment)
     */
    @Override
    public Collection<OutlierDetails> identifyOutliers( ExpressionExperiment ee ) {
        return this.identifyOutliers( ee, false, DEFAULT_QUANTILE, DEFAULT_FRACTION );
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * ubic.gemma.analysis.preprocess.OutlierDetectionService#identifyOutliers(ubic.gemma.model.expression.experiment
     * .ExpressionExperiment, boolean, int, double)
     */
    @Override
    public Collection<OutlierDetails> identifyOutliers( ExpressionExperiment ee, boolean useRegression,
            int quantileThreshold, double fractionThreshold ) {

        /*
         * Get the experimental design
         */
        ee = eeService.thawLite( ee );

        /*
         * Get the data matrix
         */
        Collection<ProcessedExpressionDataVector> vectos = processedExpressionDataVectorService
                .getProcessedDataVectors( ee );

        ExpressionDataDoubleMatrix mat = new ExpressionDataDoubleMatrix( vectos );

        /*
         * Optional: Regress out any 'major' factors; work with residuals only.
         */
        if ( useRegression && !ee.getExperimentalDesign().getExperimentalFactors().isEmpty() ) {
            double importanceThreshold = 0.01;
            Set<ExperimentalFactor> importantFactors = svdService.getImportantFactors( ee, ee.getExperimentalDesign()
                    .getExperimentalFactors(), importanceThreshold );
            if ( !importantFactors.isEmpty() ) {
                log.info( "Regressing out covariates" );
                DifferentialExpressionAnalysisConfig config = new DifferentialExpressionAnalysisConfig();
                config.setFactorsToInclude( importantFactors );
                mat = lma.regressionResiduals( mat, config, true );
            }
        }

        /*
         * Determine the correlation of samples.
         */
        DoubleMatrix<BioAssay, BioAssay> cormat = SampleCoexpressionMatrixServiceImpl.getMatrix( mat );

        return identifyOutliers( ee, cormat, quantileThreshold, fractionThreshold );

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * ubic.gemma.analysis.preprocess.OutlierDetectionService#identifyOutliers(ubic.gemma.model.expression.experiment
     * .ExpressionExperiment, ubic.basecode.dataStructure.matrix.DoubleMatrix, int, double)
     */
    @Override
    public Collection<OutlierDetails> identifyOutliers( ExpressionExperiment ee,
            DoubleMatrix<BioAssay, BioAssay> cormat, int quantileThreshold, double fractionThreshold ) {

        /*
         * Raymond's algorithm: "A sample which has a correlation of less than a threshold with more than 80% of the
         * other samples. The threshold is just the first quartile of sample correlations."
         */

        /*
         * First pass: Determine the threshold (quartile?)
         */
        assert cormat.rows() == cormat.columns();
        DoubleArrayList cors = new DoubleArrayList();
        for ( int i = 0; i < cormat.rows(); i++ ) {
            for ( int j = i + 1; j < cormat.rows(); j++ ) {
                double d = cormat.get( i, j );
                cors.add( d );
            }
        }

        /*
         * TODO sanity checks to make sure correlations aren't all the same, etc.
         */

        DoubleArrayList ranks = Rank.rankTransform( cors );
        assert ranks != null;
        int desiredQuantileIndex = ( int ) Math.ceil( cors.size() * ( quantileThreshold / 100.0 ) );
        double valueAtDesiredQuantile = Double.MIN_VALUE;
        for ( int i = 0; i < ranks.size(); i++ ) {
            if ( ranks.get( i ) == desiredQuantileIndex ) {
                valueAtDesiredQuantile = cors.get( i );
            }
        }

        if ( valueAtDesiredQuantile == Double.MIN_VALUE ) {
            throw new IllegalStateException( "Could not determine desired quantile" );
        }

        log.info( "Threshold correlation is " + String.format( "%.2f", valueAtDesiredQuantile ) );

        // second pass; for each sample, how many correlations does it have which are below the selected quantile.
        Collection<OutlierDetails> outliers = new HashSet<OutlierDetails>();
        for ( int i = 0; i < cormat.rows(); i++ ) {
            BioAssay ba = cormat.getRowName( i );
            int countBelow = 0;
            for ( int j = 0; j < cormat.rows(); j++ ) {
                double cor = cormat.get( i, j );
                if ( cor < valueAtDesiredQuantile ) {
                    countBelow++;
                }
            }

            // if it has more than the threshold fraction of low correlations, we flag it.
            if ( countBelow > fractionThreshold * cormat.columns() ) {
                OutlierDetails outlier = new OutlierDetails( ba, countBelow / ( double ) ( cormat.columns() - 1 ),
                        valueAtDesiredQuantile );
                outliers.add( outlier );
            }
        }

        if ( outliers.size() == 0 ) {
            log.info( "No outliers for " + ee );
            return outliers;
        }

        /*
         * TODO additional checks: does it 'agree' with replicates of the same condition? (esp. if we didn't work with
         * residuals)
         */

        return outliers;
    }

}
