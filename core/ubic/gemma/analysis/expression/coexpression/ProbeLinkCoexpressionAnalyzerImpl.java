/*
 * The Gemma project
 * 
 * Copyright (c) 2007-2011 University of British Columbia
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
package ubic.gemma.analysis.expression.coexpression;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ubic.basecode.dataStructure.BitUtil;
import ubic.gemma.expression.experiment.service.ExpressionExperimentService;
import ubic.gemma.genome.gene.service.GeneService;
import ubic.gemma.model.analysis.expression.coexpression.CoexpressedGenePairValueObject;
import ubic.gemma.model.analysis.expression.coexpression.QueryGeneCoexpression;
import ubic.gemma.model.association.coexpression.Probe2ProbeCoexpressionService;
import ubic.gemma.model.expression.experiment.BioAssaySet;
import ubic.gemma.model.expression.experiment.ExpressionExperimentValueObject;
import ubic.gemma.model.genome.Gene;
import ubic.gemma.util.EntityUtils;

/**
 * Perform gene-to-gene coexpression link analysis ("TMM-style"), based on stored probe-probe coexpression.
 * 
 * @author paul
 * @version $Id: ProbeLinkCoexpressionAnalyzerImpl.java,v 1.17 2012/07/07 06:25:34 paul Exp $
 */
@Component
public class ProbeLinkCoexpressionAnalyzerImpl implements ProbeLinkCoexpressionAnalyzer {

    private static Log log = LogFactory.getLog( ProbeLinkCoexpressionAnalyzerImpl.class.getName() );
    private static final int MAX_GENES_TO_COMPUTE_EESTESTEDIN = 200;

    /**
     * Provide a consistent mapping of experiments that can be used to index a vector. The actual ordering is not
     * guaranteed to be anything in particular, just that repeated calls to this method with the same experiments will
     * yield the same mapping.
     * 
     * @param experiments
     * @return Map of EE IDs to an index, where the index values are from 0 ... N-1 where N is the number of
     *         experiments.
     */
    public static Map<Long, Integer> getOrderingMap( Collection<? extends BioAssaySet> experiments ) {
        List<Long> eeIds = new ArrayList<Long>();
        for ( BioAssaySet ee : experiments ) {
            eeIds.add( ee.getId() );
        }
        Collections.sort( eeIds );
        Map<Long, Integer> result = new HashMap<Long, Integer>();
        int index = 0;
        for ( Long id : eeIds ) {
            result.put( id, index );
            index++;
        }
        assert result.size() == experiments.size();
        return result;
    }

    @Autowired
    private GeneService geneService;

    @Autowired
    private Probe2ProbeCoexpressionService probe2ProbeCoexpressionService;

    @Autowired
    private ExpressionExperimentService expressionExperimentService;

    /**
     * Gene->EEs tested in. This is a cache that _should_ be safe to use, as it is only used in batch processing, which
     * uses a 'short-lived' spring context build on the command line -- not the web application.
     */
    private Map<Long, List<Boolean>> genesTestedIn = new ConcurrentHashMap<Long, List<Boolean>>();

    /**
     * Gene->ees tested in, in a faster access format - preordered by the ees. As for the genesTestedIn, this should not
     * be used in webapps.
     */
    private Map<Long, byte[]> geneTestStatusByteCache = new ConcurrentHashMap<Long, byte[]>();

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.analysis.expression.coexpression.ProbeLinkCoexpressionAnalyzer#linkAnalysis(java.util.Collection,
     * java.util.Collection, int, boolean, int)
     */
    @Override
    public Map<Gene, QueryGeneCoexpression> linkAnalysis( Collection<Gene> genes,
            Collection<? extends BioAssaySet> ees, int stringency, boolean interGenesOnly, int limit ) {

        /*
         * Start with raw results
         */
        Map<Gene, QueryGeneCoexpression> result = geneService.getCoexpressedGenes( genes, ees, stringency,
                interGenesOnly );

        /*
         * Perform postprocessing gene by gene. It is possible this could be sped up by doing batches.
         */
        for ( Gene gene : genes ) {

            QueryGeneCoexpression coexpressions = result.get( gene );

            /*
             * Identify data sets the query gene is expressed in - this is fast (?) and provides an upper bound for EEs
             * we need to search in the first place.
             */
            Collection<BioAssaySet> eesQueryTestedIn = probe2ProbeCoexpressionService
                    .getExpressionExperimentsLinkTestedIn( gene, ees, false );

            /*
             * Finish the postprocessing.
             */
            if ( coexpressions.getAllGeneCoexpressionData( stringency ).size() == 0 ) {
                continue;
            }

            // don't fill in the gene info etc if we're in batch mode.
            if ( limit > 0 ) {
                filter( coexpressions, limit, stringency ); // remove excess
                fillInEEInfo( coexpressions ); // do first...
                fillInGeneInfo( stringency, coexpressions );
            }

            computeEesTestedIn( ees, coexpressions, eesQueryTestedIn, stringency, limit );
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * ubic.gemma.analysis.expression.coexpression.ProbeLinkCoexpressionAnalyzer#linkAnalysis(ubic.gemma.model.genome
     * .Gene, java.util.Collection, int, int)
     */
    @Override
    public QueryGeneCoexpression linkAnalysis( Gene gene, Collection<? extends BioAssaySet> ees, int inputStringency,
            int limit ) {

        int stringency = inputStringency <= 0 ? 1 : inputStringency;

        if ( log.isDebugEnabled() ) log.debug( "Link query for " + gene.getName() + " stringency=" + stringency );

        /*
         * Identify data sets the query gene is expressed in - this is fast (?) and provides an upper bound for EEs we
         * need to search in the first place.
         */
        Collection<BioAssaySet> eesQueryTestedIn = probe2ProbeCoexpressionService.getExpressionExperimentsLinkTestedIn(
                gene, ees, false );

        if ( eesQueryTestedIn.size() == 0 ) {
            QueryGeneCoexpression r = new QueryGeneCoexpression( gene.getId(), stringency );
            r.setErrorState( "No experiments have coexpression data for  " + gene.getOfficialSymbol() );
            return r;
        }

        /*
         * Perform the coexpression search, some postprocessing done. If eesQueryTestedIn is empty, this returns real
         * quick.
         */
        QueryGeneCoexpression coexpressions = geneService.getCoexpressedGenes( gene, eesQueryTestedIn, stringency );

        /*
         * Finish the postprocessing.
         */
        StopWatch timer = new StopWatch();
        timer.start();
        if ( coexpressions.getAllGeneCoexpressionData( stringency ).size() == 0 ) {
            return coexpressions;
        }

        // don't fill in the gene info etc if we're in batch mode.
        if ( limit > 0 ) {
            filter( coexpressions, limit, stringency ); // remove excess
            fillInEEInfo( coexpressions ); // do first...
            fillInGeneInfo( stringency, coexpressions );
        }

        computeEesTestedIn( ees, coexpressions, eesQueryTestedIn, stringency, limit );

        timer.stop();
        if ( timer.getTime() > 1000 ) {
            log.info( "All Post-Postprocessing: " + timer.getTime() + "ms" );
        }

        log.debug( "Analysis completed" );

        return coexpressions;
    }

    /**
     * For each experiment, get the genes it tested and populate a map. This is slow; but once we've seen a gene, we
     * don't have to repeat it. We used to cache the ee->gene relationship, but doing it the other way around yields
     * must faster code.
     * 
     * @param ees
     * @param eeIndexMap
     */
    private void cacheEesGeneTestedIn( Collection<? extends BioAssaySet> ees, Map<Long, Integer> eeIndexMap ) {

        assert ees != null;
        assert eeIndexMap != null;
        assert !ees.isEmpty();
        assert !eeIndexMap.isEmpty();

        StopWatch timer = new StopWatch();
        timer.start();
        int count = 0;
        for ( BioAssaySet ee : ees ) {
            Collection<Long> genes = probe2ProbeCoexpressionService.getGenesTestedBy( ee, false );

            // inverted map of gene -> ees tested in.
            Integer indexOfEEInAr = eeIndexMap.get( ee.getId() );

            assert indexOfEEInAr != null : "No index for EEID=" + ee.getId() + " in the eeIndexMap";

            for ( Long geneId : genes ) {
                if ( !genesTestedIn.containsKey( geneId ) ) {

                    // initialize the boolean array for this gene.
                    genesTestedIn.put( geneId, new ArrayList<Boolean>() );
                    for ( int i = 0; i < ees.size(); i++ ) {
                        genesTestedIn.get( geneId ).add( Boolean.FALSE );
                    }

                }

                // flip to true since the gene was tested in the ee
                genesTestedIn.get( geneId ).set( indexOfEEInAr, Boolean.TRUE );
            }

            if ( ++count % 100 == 0 ) {
                log.info( "Got EEs gene tested in map for " + count + " experiments ... " + timer.getTime()
                        + "ms elapsed" );
            }

        }
    }

    /**
     * Fill in gene tested information for genes coexpressed with the query.
     * 
     * @param ees ExpressionExperiments, including all that were used at the start of the query (including those the
     *        query gene is NOT expressed in)
     * @param coexpressions
     * @param eesQueryTestedIn
     * @param stringency
     * @param limit if zero, they are all collected and a batch-mode cache is used. This is MUCH slower if you are
     *        analyzing a single CoexpressionCollectionValueObject, but faster (and more memory-intensive) if many are
     *        going to be looked at (as in the case of a bulk Gene2Gene analysis).
     */
    private void computeEesTestedIn( Collection<? extends BioAssaySet> ees, QueryGeneCoexpression coexpressions,
            Collection<? extends BioAssaySet> eesQueryTestedIn, int stringency, int limit ) {

        List<CoexpressedGenePairValueObject> coexpressionData = coexpressions.getCoexpressionData( stringency );

        if ( limit == 0 ) {
            // when we expect to be analyzing many query genes. Note that we pass in the full set of experiments, not
            // just the ones in which the query gene was tested in.
            computeEesTestedInBatch( ees, coexpressionData );
        } else {
            // for when we are looking at just one gene at a time
            computeEesTestedIn( eesQueryTestedIn, coexpressionData );
        }

        /*
         * We can add this analysis to the predicted and pars if we want. I'm leaving it out for now.
         */
    }

    /**
     * For the genes that the query is coexpressed with. This is limited to the top MAX_GENES_TO_COMPUTE_EESTESTEDIN.
     * This is not very fast if MAX_GENES_TO_COMPUTE_EESTESTEDIN is large. We use this version for on-line requests.
     * 
     * @param eesQueryTestedIn, limited to the ees that the query gene is tested in.
     * @param coexpressionData
     * @see ProbeLinkCoexpressionAnalyzerImpl.computeEesTestedInBatch for the version used when requests are going to be
     *      done for many genes, so cache is built first time.
     */
    private void computeEesTestedIn( Collection<? extends BioAssaySet> eesQueryTestedIn,
            List<CoexpressedGenePairValueObject> coexpressionData ) {
        Collection<Long> coexGeneIds = new HashSet<Long>();

        StopWatch timer = new StopWatch();
        timer.start();
        int i = 0;
        Map<Long, CoexpressedGenePairValueObject> gmap = new HashMap<Long, CoexpressedGenePairValueObject>();

        for ( CoexpressedGenePairValueObject o : coexpressionData ) {
            coexGeneIds.add( o.getCoexpressedGeneId() );
            gmap.put( o.getCoexpressedGeneId(), o );
            i++;
            if ( i >= MAX_GENES_TO_COMPUTE_EESTESTEDIN ) break;
        }

        log.debug( "Computing EEs tested in for " + coexGeneIds.size() + " genes." );
        Map<Long, Collection<BioAssaySet>> eesTestedIn = probe2ProbeCoexpressionService
                .getExpressionExperimentsTestedIn( coexGeneIds, eesQueryTestedIn, false );
        for ( Long g : eesTestedIn.keySet() ) {
            CoexpressedGenePairValueObject cvo = gmap.get( g );
            assert cvo != null;
            assert eesTestedIn.get( g ).size() <= eesQueryTestedIn.size();

            Collection<Long> ids = EntityUtils.getIds( eesTestedIn.get( g ) );
            cvo.setDatasetsTestedIn( ids );
        }
        timer.stop();
        if ( timer.getTime() > 1000 ) {
            log.info( "computeEesTestedIn: " + timer.getTime() + "ms" );
        }
    }

    /**
     * For the genes that the query is coexpressed with; this retrieves the information for all the coexpressionData
     * passed in (no limit) - all of them have to be for the same query gene!
     * 
     * @param ees, including ALL experiments that were initially started with, NOT just the ones that the query gene was
     *        tested in.
     * @param coexpressionData to check, the query gene must be the same for each of them.
     */
    private void computeEesTestedInBatch( Collection<? extends BioAssaySet> ees,
            List<CoexpressedGenePairValueObject> coexpressionData ) {

        if ( coexpressionData.isEmpty() ) return;

        StopWatch timer = new StopWatch();
        timer.start();

        if ( log.isDebugEnabled() ) {
            log.debug( "Computing EEs tested in for " + coexpressionData.size() + " genes coexpressed with query." );
        }

        /*
         * Note: we assume this is actually constant as we build a cache based on it. Small risk.
         */
        Map<Long, Integer> eeIndexMap = getOrderingMap( ees );
        assert eeIndexMap.size() == ees.size();

        /*
         * Save some computation in the inner loop by assuming the query gene is constant.
         */
        CoexpressedGenePairValueObject initializer = coexpressionData.iterator().next();
        Long queryGeneId = initializer.getQueryGene();

        byte[] queryGeneEETestStatusBytes;
        if ( geneTestStatusByteCache.containsKey( queryGeneId ) ) {
            queryGeneEETestStatusBytes = geneTestStatusByteCache.get( queryGeneId );
        } else {
            queryGeneEETestStatusBytes = computeTestedDatasetVector( queryGeneId, ees, eeIndexMap );
            geneTestStatusByteCache.put( queryGeneId, queryGeneEETestStatusBytes );
        }

        /*
         * This is a potential bottleneck, because there are often >500 ees and >1000 cvos, and each EE tests >20000
         * genes. Therefore we try hard not to iterate over all the CVOEEs more than we need to. So this is coded very
         * carefully. Once the cache is warmed up, this still can take over 500ms to run (Feb 2010). The total number of
         * loops _easily_ exceeds a million. (June 2012: this is rarely taking long enough to trigger the logging, after
         * it gets warmed up)
         */
        int loopcount = 0; // for performance statistics
        for ( CoexpressedGenePairValueObject cvo : coexpressionData ) {

            Long coexGeneId = cvo.getCoexpressedGeneId();
            if ( !queryGeneId.equals( cvo.getQueryGene() ) ) {
                throw new IllegalArgumentException( "All coexpression value objects must have the same query gene here" );
            }

            /*
             * Get the target gene info, from the cache if possible.
             */
            byte[] targetGeneEETestStatus;
            if ( geneTestStatusByteCache.containsKey( coexGeneId ) ) {
                targetGeneEETestStatus = geneTestStatusByteCache.get( coexGeneId );
            } else {
                targetGeneEETestStatus = computeTestedDatasetVector( coexGeneId, ees, eeIndexMap );
                geneTestStatusByteCache.put( coexGeneId, targetGeneEETestStatus );
            }

            assert targetGeneEETestStatus.length == queryGeneEETestStatusBytes.length;

            byte[] answer = new byte[targetGeneEETestStatus.length];

            for ( int i = 0; i < answer.length; i++ ) {
                answer[i] = ( byte ) ( targetGeneEETestStatus[i] & queryGeneEETestStatusBytes[i] );
                loopcount++;
            }
            cvo.setDatasetsTestedInBytes( answer );
        }

        if ( timer.getTime() > 100 ) {
            // It usually doesn't take this long.
            log.info( "Compute EEs tested in (batch ): " + timer.getTime() + "ms; " + loopcount + " loops  " );

            // provide a cache status update at the same time. Worried about memory. 1500 data sets * 50000 genes =
            // 75megabytes, so this
            // shouldn't be a big deal.
            log.info( geneTestStatusByteCache.size() + " gene test status stored in cache, approx "
                    + ( geneTestStatusByteCache.get( queryGeneId ).length * geneTestStatusByteCache.size() )
                    + " bytes total" );
        }

    }

    /**
     * @param datasetsTestedIn
     * @param eeIdOrder
     * @return
     */
    private byte[] computeTestedDatasetVector( Long geneId, Collection<? extends BioAssaySet> ees,
            Map<Long, Integer> eeIdOrder ) {
        /*
         * This condition is, pretty much only true once in practice. That's because the first time through populates
         * genesTestedIn for all the genes tested in any of the data sets.
         */
        if ( !genesTestedIn.containsKey( geneId ) ) {
            cacheEesGeneTestedIn( ees, eeIdOrder );
        }

        assert eeIdOrder.size() == ees.size();

        assert genesTestedIn.containsKey( geneId );

        List<Boolean> eesTestingGene = genesTestedIn.get( geneId );

        assert eesTestingGene.size() == ees.size();

        // initialize.
        byte[] result = new byte[( int ) Math.ceil( eeIdOrder.size() / ( double ) Byte.SIZE )];
        for ( int i = 0, j = result.length; i < j; i++ ) {
            result[i] = 0x0;
        }

        for ( BioAssaySet ee : ees ) {
            Long eeid = ee.getId();
            Integer index = eeIdOrder.get( eeid );
            if ( eesTestingGene.get( index ) ) {
                BitUtil.set( result, index );
            }
        }
        return result;
    }

    /**
     * @param coexpressions
     */
    private void fillInEEInfo( QueryGeneCoexpression coexpressions ) {
        Collection<Long> eeIds = new HashSet<Long>();
        log.debug( "Filling in EE info" );

        fillInEEInfo( coexpressions, eeIds );

    }

    /**
     * @param coexpressions
     * @param eeIds
     * @param coexps
     */
    private void fillInEEInfo( QueryGeneCoexpression coexpressions, Collection<Long> eeIds ) {

        eeIds.addAll( coexpressions.getDataSetsWithCoexpressionForQueryGene() );
        Collection<ExpressionExperimentValueObject> ees = expressionExperimentService.loadValueObjects( eeIds, false );
        Map<Long, ExpressionExperimentValueObject> em = new HashMap<Long, ExpressionExperimentValueObject>();
        for ( ExpressionExperimentValueObject evo : ees ) {
            em.put( evo.getId(), evo );
        }

    }

    /**
     * @param stringency
     * @param coexpressions
     * @param coexp
     */
    private void fillInGeneInfo( int stringency, QueryGeneCoexpression coexpressions ) {
        StopWatch timer = new StopWatch();
        timer.start();
        List<CoexpressedGenePairValueObject> coexpressionData = coexpressions.getCoexpressionData( stringency );
        Collection<Long> geneIds = new HashSet<Long>();
        for ( CoexpressedGenePairValueObject cod : coexpressionData ) {
            geneIds.add( cod.getCoexpressedGeneId() );
        }

        Collection<Gene> genes = geneService.loadMultiple( geneIds ); // this can be slow if there are a lot.
        Map<Long, Gene> gm = new HashMap<Long, Gene>();
        for ( Gene g : genes ) {
            gm.put( g.getId(), g );
        }

        for ( CoexpressedGenePairValueObject cod : coexpressionData ) {
            coexpressions.add( cod );
        }
        timer.stop();
        if ( timer.getTime() > 1000 ) {
            log.info( "Filled in gene info: " + timer.getTime() + "ms" );
        }
    }

    /**
     * @param coexpressions
     * @param limit
     * @param stringency
     */
    private void filter( QueryGeneCoexpression coexpressions, int limit, int stringency ) {
        coexpressions.filter( limit, stringency );
    }

}
