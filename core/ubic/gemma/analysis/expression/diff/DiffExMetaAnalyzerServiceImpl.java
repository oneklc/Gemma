/*
 * The Gemma project
 * 
 * Copyright (c) 2012 University of British Columbia
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

package ubic.gemma.analysis.expression.diff;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

// import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ubic.basecode.math.MultipleTestCorrection;
import ubic.basecode.math.metaanalysis.MetaAnalysis;
import ubic.gemma.model.analysis.expression.diff.ContrastResult;
import ubic.gemma.model.analysis.expression.diff.DifferentialExpressionAnalysis;
import ubic.gemma.model.analysis.expression.diff.DifferentialExpressionAnalysisResult;
import ubic.gemma.model.analysis.expression.diff.DifferentialExpressionAnalysisService;
import ubic.gemma.model.analysis.expression.diff.DifferentialExpressionResultService;
import ubic.gemma.model.analysis.expression.diff.ExpressionAnalysisResultSet;
import ubic.gemma.model.analysis.expression.diff.GeneDiffExMetaAnalysisService;
import ubic.gemma.model.analysis.expression.diff.GeneDifferentialExpressionMetaAnalysis;
import ubic.gemma.model.analysis.expression.diff.GeneDifferentialExpressionMetaAnalysisResult;
import ubic.gemma.model.expression.designElement.CompositeSequence;
import ubic.gemma.model.expression.designElement.CompositeSequenceService;
import ubic.gemma.model.expression.experiment.BioAssaySet;
import ubic.gemma.model.expression.experiment.ExperimentalFactor;
import ubic.gemma.model.expression.experiment.ExpressionExperiment;
import ubic.gemma.model.expression.experiment.ExpressionExperimentSubSet;
import ubic.gemma.model.genome.Gene;
import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;

/**
 * @author Paul
 * @version $Id: DiffExMetaAnalyzerServiceImpl.java,v 1.26 2013/04/18 22:52:59 paul Exp $
 */
@Component
public class DiffExMetaAnalyzerServiceImpl implements DiffExMetaAnalyzerService {

    private static Log log = LogFactory.getLog( DiffExMetaAnalyzerServiceImpl.class );

    private static final double QVALUE_FOR_STORAGE_THRESHOLD = 0.1;

    @Autowired
    private GeneDiffExMetaAnalysisService analysisService;

    @Autowired
    private CompositeSequenceService compositeSequenceService;

    @Autowired
    private DifferentialExpressionAnalysisService differentialExpressionAnalysisService;

    @Autowired
    private DifferentialExpressionAnalyzerService differentialExpressionAnalyzerService;

    @Autowired
    private DifferentialExpressionResultService differentialExpressionResultService;

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.analysis.expression.diff.DiffExMetaAnalyserService#analyze(java.util.Collection)
     */
    @Override
    public GeneDifferentialExpressionMetaAnalysis analyze( Collection<Long> analysisResultSetIds ) {

        /*
         * first pass: get full results.
         */
        Collection<ExpressionAnalysisResultSet> updatedResultSets = prepare( analysisResultSetIds );

        /*
         * Second pass. Organize the results by gene
         */
        Collection<DifferentialExpressionAnalysisResult> res2set = new HashSet<DifferentialExpressionAnalysisResult>();
        Map<Gene, Collection<DifferentialExpressionAnalysisResult>> gene2result = organizeResultsByGene(
                updatedResultSets, res2set );

        if ( gene2result == null ) {
            throw new IllegalArgumentException( "There are no genes associated with any of the probes" );
        }

        /*
         * third pass, do the actual meta-analysis.
         */
        GeneDifferentialExpressionMetaAnalysis metaAnalysis = doMetaAnalysis( updatedResultSets, res2set, gene2result );

        if ( metaAnalysis.getResults().isEmpty() ) {
            log.warn( "No results were significant, the analysis will not be completed" );
            return null;
        }

        log.info( "Found " + metaAnalysis.getResults().size() + " results meeting meta-qvalue of "
                + QVALUE_FOR_STORAGE_THRESHOLD );

        return metaAnalysis;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * ubic.gemma.analysis.expression.diff.DiffExMetaAnalyzerService#persist(ubic.gemma.model.analysis.expression.diff
     * .GeneDifferentialExpressionMetaAnalysis)
     */
    @Override
    public GeneDifferentialExpressionMetaAnalysis persist( GeneDifferentialExpressionMetaAnalysis analysis ) {
        for ( ExpressionAnalysisResultSet r : analysis.getResultSetsIncluded() ) {
            // this will only be necessary if there are new results added. FIXME perhaps don't save everything. Just
            // save the ones we need. Remove the unneeded ones. Drawback: could end up redoing this multiple times.
            r.setQvalueThresholdForStorage( 1.0 );
            differentialExpressionResultService.update( r );
        }

        return analysisService.create( analysis );
    }

    /**
     * @param res
     * @return
     */
    private Double aggregateFoldChangeForGeneWithinResultSet( Collection<DifferentialExpressionAnalysisResult> res ) {
        assert !res.isEmpty();
        Double bestPvalue = Double.MAX_VALUE;
        DifferentialExpressionAnalysisResult best = null;

        for ( DifferentialExpressionAnalysisResult r : res ) {

            Double pvalue = r.getPvalue();
            if ( pvalue == null ) continue;

            assert r.getContrasts().size() < 2 : "Wrong number of contrasts: " + r.getContrasts().size();

            if ( pvalue < bestPvalue ) {
                bestPvalue = pvalue;
                best = r;
            }
        }

        if ( best == null ) return null;

        if ( best.getContrasts().isEmpty() ) {
            throw new IllegalStateException( "There was no contrast for result with ID=" + best.getId() + " resultset="
                    + best.getResultSet().getId() );
        }

        assert best.getContrasts().size() == 1;

        return best.getContrasts().iterator().next().getLogFoldChange();
    }

    /**
     * For cases where there is more than one result for a gene in a data set (due to multiple probes), we aggregate
     * them. Method: take the best pvalue. Later we correct for multiple testing.
     * <p>
     * The pvalues stored in a DifferentialExpressionAnalysisResult are two-tailed, so we have to divide by two, and
     * then decide which tail to provide.
     * 
     * @param res that are all from the same gene, from a single resultset.
     * @param upperTail if true, the upper tail probability is given, lower tail otehrwise.
     * @return the pvalue that represents the overall results.
     */
    private Double aggregatePvaluesForGeneWithinResultSet( Collection<DifferentialExpressionAnalysisResult> res,
            boolean upperTail ) {
        assert !res.isEmpty();
        Double bestPvalue = Double.MAX_VALUE;

        for ( DifferentialExpressionAnalysisResult r : res ) {

            Double pvalue = r.getPvalue();

            if ( pvalue == null || Double.isNaN( pvalue ) ) {
                continue;
            }

            /*
             * P-values stored with the per-experiment analyses are two-sided. To convert to a one-sided value, we
             * consider just one tail.
             */
            pvalue /= 2.0;

            /*
             * Next decide if we should use the "up" or "down" part of the pvalue. We examine the fold change, and
             * associate the original pvalue with that tail. We then switch tails if necessary.
             */
            assert r.getContrasts().size() < 2 : "Wrong number of contrasts: " + r.getContrasts().size();
            Double logFoldChange = r.getContrasts().iterator().next().getLogFoldChange();

            if ( ( upperTail && logFoldChange < 0 ) || ( !upperTail && logFoldChange > 0 ) ) {
                pvalue = 1.0 - pvalue;
            }

            if ( pvalue < bestPvalue ) {
                bestPvalue = pvalue;
            }
        }

        assert bestPvalue <= 1.0 && bestPvalue >= 0.0;

        /*
         * Because each gene gets two chances to be significant (up and down) we considered to _also_ do a Bonferroni
         * correction. This would be too conservative because the pair of pvalues are correlated. If one pvalue is good
         * the other is bad. There is no need for this correction.
         */
        // return Math.min( 1.0, 2.0 * bestPvalue );
        return bestPvalue;
    }

    /**
     * @param resultsToUse
     * @param probes
     * @param rs
     */
    private void checkAndAddResultSet( Collection<DifferentialExpressionAnalysisResult> resultsToUse,
            Collection<CompositeSequence> probes, ExpressionAnalysisResultSet rs ) {
        Collection<DifferentialExpressionAnalysisResult> results = rs.getResults();

        log.info( results.size() + " results to check ..." );
        for ( DifferentialExpressionAnalysisResult r : results ) {
            assert r != null;
            CompositeSequence probe = r.getProbe();
            assert probe != null;
            probes.add( probe );

            boolean added = resultsToUse.add( r );
            assert added : "Failed to add: " + r;
        }
        log.info( results.size() + " results checked for resultset with ID=" + rs.getId() + ", found " + probes.size()
                + " probes/elements so far for " + resultsToUse.size() + " results in total ..." );
    }

    /**
     * Bonferonni correct across multiple pvalues. and clip really small pvalues to avoid them taking over.
     * <p>
     * FIXME make clipping adjustable.
     * 
     * @param usedResults tells us how many results we used to obtain the pvalue, for the purposes of multiple testing.
     * @param pvalue
     * @return adjusted and clipped pvalues.
     */
    private Double correctAndClip( Collection<DifferentialExpressionAnalysisResult> usedResults, Double pvalue ) {
        pvalue = usedResults.size() == 1 ? pvalue : Math.min( pvalue * usedResults.size(), 1.0 );
        pvalue = Math.max( pvalue, GeneDifferentialExpressionService.PVALUE_CLIP_THRESHOLD );
        return pvalue;
    }

    /**
     * spit out a bunch of debugging information.
     * 
     * @param g
     * @param pvalues4geneUp
     * @param pvalues4geneDown
     * @param resultsUsed
     * @param fisherPvalueUp
     * @param fisherPvalueDown
     */
    private void debug( Gene g, DoubleArrayList pvalues4geneUp, DoubleArrayList pvalues4geneDown,
            Collection<DifferentialExpressionAnalysisResult> resultsUsed, double fisherPvalueUp, double fisherPvalueDown ) {

        // System.err.println( "Up\t" + g.getOfficialSymbol() + "\t" + StringUtils.join( pvalues4geneUp.toList(), '\t' )
        // );
        // System.err.println( "Down\t" + g.getOfficialSymbol() + "\t"
        // + StringUtils.join( pvalues4geneDown.toList(), '\t' ) );

        // System.err.print( "InputP\t" + g.getOfficialSymbol() );
        // for ( DifferentialExpressionAnalysisResult w : resultsUsed ) {
        // System.err.print( "\t" + w.getPvalue() );
        // }
        // System.err.println();
        // System.err.print( "InputT\t" + g.getOfficialSymbol() );
        // for ( DifferentialExpressionAnalysisResult w : resultsUsed ) {
        // System.err.print( "\t" + w.getContrasts().iterator().next().getTstat() );
        // }
        // System.err.println();

        if ( log.isDebugEnabled() )
            log.debug( String.format( "Meta-results for %s: pUp=%.4g pdown=%.4g", g.getOfficialSymbol(),
                    fisherPvalueUp, fisherPvalueDown ) );
    }

    /**
     * @param updatedResultSets
     * @param res2set
     * @param gene2result
     * @return
     */
    private GeneDifferentialExpressionMetaAnalysis doMetaAnalysis(
            Collection<ExpressionAnalysisResultSet> updatedResultSets,
            Collection<DifferentialExpressionAnalysisResult> res2set,
            Map<Gene, Collection<DifferentialExpressionAnalysisResult>> gene2result ) {
        log.info( "Computing pvalues ..." );
        DoubleArrayList pvaluesUp = new DoubleArrayList();
        DoubleArrayList pvaluesDown = new DoubleArrayList();

        // third pass: collate to get p-values. First we have to aggregate within result set for genes which have more
        // than one probe
        List<GeneDifferentialExpressionMetaAnalysisResult> metaAnalysisResultsUp = new ArrayList<GeneDifferentialExpressionMetaAnalysisResult>();
        List<GeneDifferentialExpressionMetaAnalysisResult> metaAnalysisResultsDown = new ArrayList<GeneDifferentialExpressionMetaAnalysisResult>();
        for ( Gene g : gene2result.keySet() ) {

            Map<ExpressionAnalysisResultSet, Collection<DifferentialExpressionAnalysisResult>> resultSet2Results4Gene = getResults4GenePerResultSet(
                    g, res2set, gene2result );

            if ( g.getOfficialSymbol().equals( "GUK1" ) ) {
                log.info( g );
            }

            /*
             * Compute the pvalues for each resultset.
             */
            DoubleArrayList pvalues4geneUp = new DoubleArrayList();
            DoubleArrayList pvalues4geneDown = new DoubleArrayList();
            DoubleArrayList foldChanges4gene = new DoubleArrayList();
            Collection<DifferentialExpressionAnalysisResult> resultsUsed = new HashSet<DifferentialExpressionAnalysisResult>();
            for ( ExpressionAnalysisResultSet rs : resultSet2Results4Gene.keySet() ) {
                Collection<DifferentialExpressionAnalysisResult> res = resultSet2Results4Gene.get( rs );

                if ( res.isEmpty() ) {
                    // shouldn't happen?
                    log.warn( "Unexpectedly no results in resultSet for gene " + g );
                    continue;
                }

                Double foldChange4GeneInOneResultSet = aggregateFoldChangeForGeneWithinResultSet( res );

                if ( foldChange4GeneInOneResultSet == null ) {
                    // we can't go on.
                    continue;
                }

                // we use the pvalue for the 'best' direction, and set the other to be 1- that. An alternative would be
                // to use _only_ the best one.
                Double pvalue4GeneInOneResultSetUp;
                Double pvalue4GeneInOneResultSetDown;
                if ( foldChange4GeneInOneResultSet < 0 ) {
                    pvalue4GeneInOneResultSetDown = aggregatePvaluesForGeneWithinResultSet( res, false );
                    assert pvalue4GeneInOneResultSetDown != null;
                    pvalue4GeneInOneResultSetUp = 1.0 - pvalue4GeneInOneResultSetDown;
                } else {
                    pvalue4GeneInOneResultSetUp = aggregatePvaluesForGeneWithinResultSet( res, true );
                    assert pvalue4GeneInOneResultSetUp != null;
                    pvalue4GeneInOneResultSetDown = 1.0 - pvalue4GeneInOneResultSetUp;
                }

                // If we have missing values, skip them. (this should be impossible!)
                if ( Double.isNaN( pvalue4GeneInOneResultSetUp ) || Double.isNaN( pvalue4GeneInOneResultSetDown ) ) {
                    continue;
                }

                /*
                 * Now when we correct, we have to 1) bonferroni correct for multiple probes and 2) clip really small
                 * pvalues. We do this now, so that we don't skew the converse pvalues (up vs. down).
                 */
                pvalue4GeneInOneResultSetUp = correctAndClip( res, pvalue4GeneInOneResultSetUp );
                pvalue4GeneInOneResultSetDown = correctAndClip( res, pvalue4GeneInOneResultSetDown );

                // results used for this one gene's meta-analysis.
                boolean added = resultsUsed.addAll( res );
                assert added;

                pvalues4geneUp.add( pvalue4GeneInOneResultSetUp );
                pvalues4geneDown.add( pvalue4GeneInOneResultSetDown );
                foldChanges4gene.add( foldChange4GeneInOneResultSet );

                if ( log.isDebugEnabled() )
                    log.debug( String.format( "%s %.4f %.4f %.1f", g.getOfficialSymbol(), pvalue4GeneInOneResultSetUp,
                            pvalue4GeneInOneResultSetDown, foldChange4GeneInOneResultSet ) );
            } // loop over results for one gene
            assert resultsUsed.size() >= pvalues4geneUp.size();

            /*
             * FIXME what to do if there is just one pvalue for the gene? Is this good enough?
             */
            if ( pvalues4geneUp.size() < 2 ) {
                continue;
            }

            /*
             * Note that this value can be misleading. It should not be used to determine the change that was
             * "looked for". Use the 'upperTail' field instead.
             */
            Double meanLogFoldChange = Descriptive.mean( foldChanges4gene );
            assert meanLogFoldChange != null;

            double fisherPvalueUp = MetaAnalysis.fisherCombinePvalues( pvalues4geneUp );
            double fisherPvalueDown = MetaAnalysis.fisherCombinePvalues( pvalues4geneDown );

            if ( Double.isNaN( fisherPvalueUp ) || Double.isNaN( fisherPvalueDown ) ) {
                continue;
            }

            pvaluesUp.add( fisherPvalueUp );
            GeneDifferentialExpressionMetaAnalysisResult metaAnalysisResultUp = makeMetaAnalysisResult( g, resultsUsed,
                    meanLogFoldChange, fisherPvalueUp, Boolean.TRUE );
            metaAnalysisResultsUp.add( metaAnalysisResultUp );

            pvaluesDown.add( fisherPvalueDown );
            GeneDifferentialExpressionMetaAnalysisResult metaAnalysisResultDown = makeMetaAnalysisResult( g,
                    resultsUsed, meanLogFoldChange, fisherPvalueDown, Boolean.FALSE );
            metaAnalysisResultsDown.add( metaAnalysisResultDown );

            debug( g, pvalues4geneUp, pvalues4geneDown, resultsUsed, fisherPvalueUp, fisherPvalueDown );
        } // end loop over genes.

        assert metaAnalysisResultsUp.size() == metaAnalysisResultsDown.size();

        if ( metaAnalysisResultsDown.isEmpty() ) {
            // can happen if platforms don't have any genes that match etc.
            log.warn( "No meta-analysis results were obtained" );
            return null;
        }

        log.info( metaAnalysisResultsUp.size() + " initial meta-analysis results" );

        DoubleArrayList qvaluesUp = MultipleTestCorrection.benjaminiHochberg( pvaluesUp );
        assert qvaluesUp.size() == metaAnalysisResultsUp.size();

        DoubleArrayList qvaluesDown = MultipleTestCorrection.benjaminiHochberg( pvaluesDown );
        assert qvaluesDown.size() == metaAnalysisResultsDown.size();

        return makeMetaAnalysisObject( updatedResultSets, metaAnalysisResultsUp, metaAnalysisResultsDown, qvaluesUp,
                qvaluesDown );
    }

    /**
     * Redo analyses for the resultset, with the altered threshold, but don't persist the results.
     * 
     * @param rs
     * @param analysis
     * @return
     */
    private ExpressionAnalysisResultSet extendAnalysis( ExpressionAnalysisResultSet rs,
            DifferentialExpressionAnalysis analysis ) {

        /*
         * FIXME if we're going to persist it anyway, we might as well do it here? Otherwise we could end up doing it
         * over and over.
         */
        BioAssaySet experimentAnalyzed = analysis.getExperimentAnalyzed();
        ExpressionExperiment ee;

        if ( experimentAnalyzed instanceof ExpressionExperimentSubSet ) {
            ee = ( ( ExpressionExperimentSubSet ) experimentAnalyzed ).getSourceExperiment();
        } else {
            ee = ( ExpressionExperiment ) experimentAnalyzed;
        }

        for ( ExpressionAnalysisResultSet s : differentialExpressionAnalyzerService.extendAnalysis( ee, analysis ) ) {
            if ( s.getId().equals( rs.getId() ) ) {
                return s;
            }
        }
        throw new IllegalStateException();
    }

    /**
     * This is necessary to deal with the case of more than one probe for a gene in a given resultset.
     * 
     * @param g
     * @param res2set
     * @param gene2result
     * @return a map of result sets to the results from that resultset, for gene g.
     */
    private Map<ExpressionAnalysisResultSet, Collection<DifferentialExpressionAnalysisResult>> getResults4GenePerResultSet(
            Gene g, Collection<DifferentialExpressionAnalysisResult> res2set,
            Map<Gene, Collection<DifferentialExpressionAnalysisResult>> gene2result ) {

        Collection<DifferentialExpressionAnalysisResult> res4gene = gene2result.get( g );

        Map<ExpressionAnalysisResultSet, Collection<DifferentialExpressionAnalysisResult>> resultSet2Results4Gene = new HashMap<ExpressionAnalysisResultSet, Collection<DifferentialExpressionAnalysisResult>>();

        for ( DifferentialExpressionAnalysisResult r : res4gene ) {
            Collection<ContrastResult> contrasts = r.getContrasts();
            if ( contrasts.isEmpty() ) {
                // defensive; could indicate failed model fit, etc. - but shouldn't happen this late?
                continue;
            }
            assert contrasts.size() == 1;

            ExpressionAnalysisResultSet rs = r.getResultSet();

            if ( !resultSet2Results4Gene.containsKey( rs ) ) {
                resultSet2Results4Gene.put( rs, new HashSet<DifferentialExpressionAnalysisResult>() );
            }
            resultSet2Results4Gene.get( rs ).add( r );
        }
        return resultSet2Results4Gene;
    }

    /**
     * @param analysisResultSetIds
     * @return
     */
    private Collection<ExpressionAnalysisResultSet> loadAnalysisResultSets( Collection<Long> analysisResultSetIds ) {
        Collection<ExpressionAnalysisResultSet> resultSets = new HashSet<ExpressionAnalysisResultSet>();

        for ( Long analysisResultSetId : analysisResultSetIds ) {
            ExpressionAnalysisResultSet expressionAnalysisResultSet = this.differentialExpressionResultService
                    .loadAnalysisResultSet( analysisResultSetId );

            if ( expressionAnalysisResultSet == null ) {
                log.warn( "No diff ex result set with ID=" + analysisResultSetId );
                throw new IllegalArgumentException( "No diff ex result set with ID=" + analysisResultSetId );
            }

            resultSets.add( expressionAnalysisResultSet );
        }
        return resultSets;
    }

    /**
     * @param updatedResultSets
     * @param metaAnalysisResultsUp
     * @param metaAnalysisResultsDown
     * @param qvaluesUp
     * @param qvaluesDown
     * @return
     */
    private GeneDifferentialExpressionMetaAnalysis makeMetaAnalysisObject(
            Collection<ExpressionAnalysisResultSet> updatedResultSets,
            List<GeneDifferentialExpressionMetaAnalysisResult> metaAnalysisResultsUp,
            List<GeneDifferentialExpressionMetaAnalysisResult> metaAnalysisResultsDown, DoubleArrayList qvaluesUp,
            DoubleArrayList qvaluesDown ) {
        /*
         * create the analysis object
         */
        GeneDifferentialExpressionMetaAnalysis metaAnalysis = GeneDifferentialExpressionMetaAnalysis.Factory
                .newInstance();
        metaAnalysis.setNumGenesAnalyzed( metaAnalysisResultsUp.size() ); // should be the same for both.
        metaAnalysis.setQvalueThresholdForStorage( QVALUE_FOR_STORAGE_THRESHOLD );
        metaAnalysis.getResultSetsIncluded().addAll( updatedResultSets );

        // reject values that don't meet the threshold
        selectValues( metaAnalysisResultsUp, qvaluesUp, metaAnalysis );
        selectValues( metaAnalysisResultsDown, qvaluesDown, metaAnalysis );
        resolveConflicts( metaAnalysis );
        return metaAnalysis;
    }

    /**
     * @param g
     * @param resultsUsed
     * @param meanLogFoldChange
     * @param fisherPvalue
     * @param upperTail
     * @return
     */
    private GeneDifferentialExpressionMetaAnalysisResult makeMetaAnalysisResult( Gene g,
            Collection<DifferentialExpressionAnalysisResult> resultsUsed, Double meanLogFoldChange,
            double fisherPvalue, boolean upperTail ) {

        GeneDifferentialExpressionMetaAnalysisResult metaAnalysisResult = GeneDifferentialExpressionMetaAnalysisResult.Factory
                .newInstance();
        metaAnalysisResult.setMetaPvalue( fisherPvalue );
        for ( DifferentialExpressionAnalysisResult w : resultsUsed ) {
            boolean added = metaAnalysisResult.getResultsUsed().add( w );
            assert added;
        }
        metaAnalysisResult.setGene( g );
        metaAnalysisResult.setUpperTail( upperTail );
        return metaAnalysisResult;
    }

    /**
     * Organize the results by gene. Results that have more than one gene (or no gene) are skipped.
     * 
     * @param resultSets
     * @param res2set
     * @return a map of genes to the usable results for that gene. There can be more than one result for one resultset.
     */
    private Map<Gene, Collection<DifferentialExpressionAnalysisResult>> organizeResultsByGene(
            Collection<ExpressionAnalysisResultSet> resultSets, Collection<DifferentialExpressionAnalysisResult> res2set ) {
        Collection<CompositeSequence> probes = new HashSet<CompositeSequence>();

        for ( ExpressionAnalysisResultSet rs : resultSets ) {
            validate( rs );
            checkAndAddResultSet( res2set, probes, rs );
        }
        log.info( "Matching up by genes ..." );
        Map<CompositeSequence, Collection<Gene>> cs2genes = compositeSequenceService.getGenes( probes );
        Map<Gene, Collection<DifferentialExpressionAnalysisResult>> gene2result = new HashMap<Gene, Collection<DifferentialExpressionAnalysisResult>>();

        int numWithGenes = 0;
        int numWithoutGenes = 0;
        int numWithMultipleGenes = 0;
        int numWithoutPvalues = 0;
        assert !resultSets.isEmpty();

        for ( ExpressionAnalysisResultSet rs : resultSets ) {

            Collection<DifferentialExpressionAnalysisResult> results = rs.getResults();
            assert !results.isEmpty();
            for ( DifferentialExpressionAnalysisResult r : results ) {

                if ( r.getPvalue() == null || Double.isNaN( r.getPvalue() ) ) {
                    numWithoutPvalues++;
                    continue;
                }

                assert r != null;
                CompositeSequence probe = r.getProbe();
                Collection<Gene> genes = cs2genes.get( probe );

                if ( genes == null || genes.isEmpty() ) {
                    numWithoutGenes++;
                    continue;
                }
                if ( genes.size() > 1 ) {
                    numWithMultipleGenes++;
                    continue;
                }

                Gene gene = genes.iterator().next();

                if ( !gene2result.containsKey( gene ) ) {
                    gene2result.put( gene, new HashSet<DifferentialExpressionAnalysisResult>() );
                }
                boolean added = gene2result.get( gene ).add( r );
                assert added : "Failed to add " + r;
                numWithGenes++;
            }
        }

        if ( numWithGenes == 0 ) {
            log.warn( "No probes were associated with genes (or all had more than one gene; " + numWithMultipleGenes
                    + ")" );
            return null;
        }

        log.info( numWithGenes + " of the results had genes; " + numWithoutGenes + " had no gene; "
                + numWithMultipleGenes + " had more than one gene" );
        if ( numWithoutPvalues > 0 ) {
            log.info( numWithoutPvalues
                    + " of the results had no pvalue stored (typically indicates failed model fits) " );
        }
        return gene2result;
    }

    /**
     * @param analysisResultSetIds
     * @return
     */
    private Collection<ExpressionAnalysisResultSet> prepare( Collection<Long> analysisResultSetIds ) {
        Collection<ExpressionAnalysisResultSet> resultSets = loadAnalysisResultSets( analysisResultSetIds );

        if ( resultSets.size() < 2 ) {
            throw new IllegalArgumentException( "Must have at least two result sets to meta-analyze" );
        }

        /*
         * 1. Thaw the result sets and do some validation. 2. Get all the probes for all the results sets that will be
         * used. 3. Build a map of result to the source result set.
         */
        log.info( "Preparing to meta-analyze " + resultSets.size() + " resultSets ..." );

        Collection<ExpressionAnalysisResultSet> updatedResultSets = new HashSet<ExpressionAnalysisResultSet>();
        for ( ExpressionAnalysisResultSet rs : resultSets ) {
            DifferentialExpressionAnalysis analysis = differentialExpressionResultService.getAnalysis( rs );
            analysis = differentialExpressionAnalysisService.thawFully( analysis );

            if ( rs.getQvalueThresholdForStorage() != null && rs.getQvalueThresholdForStorage() < 1.0 ) {

                /*
                 * We have to extend the analysis to include all probes, not just 'significant' ones.
                 */

                rs = extendAnalysis( rs, analysis );
                updatedResultSets.add( rs );

            } else {
                // updatedResultSets.add( differentialExpressionResultService.thaw( rs ) );
                updatedResultSets.add( rs );
            }

        }

        assert !updatedResultSets.isEmpty();
        return updatedResultSets;
    }

    /**
     * Reject data for genes that show up as both up and down. This can happen, but we just reject data from such cases.
     * 
     * @param analysis
     */
    private void resolveConflicts( GeneDifferentialExpressionMetaAnalysis analysis ) {

        Collection<Gene> genesToRemove = new HashSet<Gene>();
        Collection<Gene> seenGenes = new HashSet<Gene>();
        for ( GeneDifferentialExpressionMetaAnalysisResult r : analysis.getResults() ) {
            if ( seenGenes.contains( r.getGene() ) ) {
                genesToRemove.add( r.getGene() );
            }
            seenGenes.add( r.getGene() );
        }

        if ( genesToRemove.isEmpty() ) return;

        int removed = 0;
        for ( Iterator<GeneDifferentialExpressionMetaAnalysisResult> it = analysis.getResults().iterator(); it
                .hasNext(); ) {
            GeneDifferentialExpressionMetaAnalysisResult r = it.next();
            if ( genesToRemove.contains( r.getGene() ) ) {
                it.remove();
                removed++;
            }

        }

        assert removed >= genesToRemove.size() * 2;
        log.info( "Data for " + genesToRemove.size() + " genes was removed because of conflicting results." );

    }

    /**
     * Extract the results we keep, that meet the threshold for qvalue
     * 
     * @param metaAnalysisResults
     * @param qvalues
     * @param analysis
     */
    private void selectValues( List<GeneDifferentialExpressionMetaAnalysisResult> metaAnalysisResults,
            DoubleArrayList qvalues, GeneDifferentialExpressionMetaAnalysis analysis ) {
        //
        int i = 0;
        assert metaAnalysisResults.size() == qvalues.size();
        for ( GeneDifferentialExpressionMetaAnalysisResult r : metaAnalysisResults ) {
            double metaQvalue = qvalues.get( i );
            r.setMetaQvalue( metaQvalue );

            if ( metaQvalue < QVALUE_FOR_STORAGE_THRESHOLD ) {
                analysis.getResults().add( r );
                if ( log.isDebugEnabled() )
                    log.debug( "Keeping " + r.getGene().getOfficialSymbol() + ", q=" + metaQvalue );
            }

            i++;
        }
    }

    /**
     * @param rs
     */
    private void validate( ExpressionAnalysisResultSet rs ) {
        if ( rs.getExperimentalFactors().size() > 1 ) {
            throw new IllegalArgumentException( "Cannot do a meta-analysis on interaction terms" );
        }

        ExperimentalFactor factor = rs.getExperimentalFactors().iterator().next();
        if ( factor.getFactorValues().size() > 2 ) {
            /*
             * Note that this doesn't account for continuous factors.
             */
            throw new IllegalArgumentException(
                    "Cannot do a meta-analysis including a factor that has more than two levels: " + factor + " has "
                            + factor.getFactorValues().size() + " levels" );
        }
    }
}
