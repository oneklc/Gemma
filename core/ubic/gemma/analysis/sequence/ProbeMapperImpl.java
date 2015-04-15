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
package ubic.gemma.analysis.sequence;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;
import ubic.gemma.apps.Blat;
import ubic.gemma.externalDb.GoldenPathSequenceAnalysis;
import ubic.gemma.model.genome.Taxon;
import ubic.gemma.model.genome.biosequence.BioSequence;
import ubic.gemma.model.genome.biosequence.SequenceType;
import ubic.gemma.model.genome.sequenceAnalysis.BlatAssociation;
import ubic.gemma.model.genome.sequenceAnalysis.BlatResult;
import ubic.gemma.model.genome.sequenceAnalysis.ThreePrimeDistanceMethod;

/**
 * Provides methods for mapping sequences to genes and gene products. Some methods accept a configuration object that
 * allows threshold etc. to be modified.
 * 
 * @author pavlidis
 * @version $Id: ProbeMapperImpl.java,v 1.5 2012/06/11 19:05:54 paul Exp $
 */
@Component
public class ProbeMapperImpl implements ProbeMapper {

    private Log log = LogFactory.getLog( ProbeMapperImpl.class.getName() );
    private ThreePrimeDistanceMethod threeprimeMethod = ThreePrimeDistanceMethod.RIGHT;

    /*
     * (non-Javadoc)
     * 
     * @see
     * ubic.gemma.analysis.sequence.ProbeMapper#processBlatResults(ubic.gemma.externalDb.GoldenPathSequenceAnalysis,
     * java.util.Collection, ubic.gemma.analysis.sequence.ProbeMapperConfig)
     */
    @Override
    public Map<String, Collection<BlatAssociation>> processBlatResults( GoldenPathSequenceAnalysis goldenPathDb,
            Collection<BlatResult> blatResults, ProbeMapperConfig config ) {

        if ( log.isDebugEnabled() ) {
            log.debug( blatResults.size() + " Blat results to map " );
        }

        assert goldenPathDb != null;
        Map<String, Collection<BlatAssociation>> allRes = new HashMap<String, Collection<BlatAssociation>>();
        int count = 0;
        int skipped = 0;
        int skippedDueToRepeat = 0;
        int skippedDueToNonSpecific = 0;

        Map<BioSequence, Collection<BlatResult>> biosequenceToBlatResults = groupBlatResultsByBioSequence( blatResults );

        /*
         * Temporary code!!
         */
        if ( goldenPathDb.getDatabaseName().equals( "hg19" ) ) {
            log.debug( "Disabling  acembly as hg19 doesn't have them" );
            config.setUseAcembly( false );
        }

        // Do them one sequence at a time.
        for ( BioSequence sequence : biosequenceToBlatResults.keySet() ) {
            Collection<BlatResult> blatResultsForSequence = biosequenceToBlatResults.get( sequence );

            if ( log.isDebugEnabled() ) {
                log.debug( blatResultsForSequence.size() + " Blat results for " + sequence );
            }

            if ( blatResultsForSequence.size() == 0 ) continue;

            /*
             * Screen out likely repeats, where cross-hybridization is a problem.
             */
            Double fractionRepeats = sequence.getFractionRepeats();
            if ( fractionRepeats != null && fractionRepeats > config.getMaximumRepeatFraction()
                    && blatResultsForSequence.size() >= config.getNonSpecificSiteCountThreshold() ) {
                skippedDueToRepeat++;
                skipped++;
                continue;
            }

            /*
             * Filter out sequences that have many high-quality alignments to the genome, even if it isn't a repeat. It
             * just seems like a good idea to reject probes that have too many alignment sites, even if only one of them
             * is to the site of a known gene.
             */
            if ( blatResultsForSequence.size() >= config.getNonRepeatNonSpecificSiteCountThreshold() ) {
                skippedDueToNonSpecific++;
                skipped++;
                continue;
            }

            /*
             * Sanity check to make sure user is paying attention to what they are putting in.
             */
            if ( blatResultsForSequence.size() > 25 ) {
                log.warn( sequence + " has " + blatResultsForSequence.size() + " raw blat associations" );
            }

            Collection<BlatAssociation> blatAssociationsForSequence = new HashSet<BlatAssociation>();

            // map each blat result.
            for ( BlatResult blatResult : blatResultsForSequence ) {
                assert blatResult.score() >= 0 : "Score was " + blatResult.score();
                assert blatResult.identity() >= 0 : "Identity was " + blatResult.identity();
                if ( blatResult.score() < config.getBlatScoreThreshold()
                        || blatResult.identity() < config.getIdentityThreshold() ) {
                    if ( log.isDebugEnabled() )
                        log.debug( "Result for " + sequence + " skipped with score=" + blatResult.score()
                                + " identity=" + blatResult.identity() );
                    skipped++;
                    continue;
                }

                if ( blatResult.getQuerySequence().getTaxon() == null ) {
                    Taxon taxon = goldenPathDb.getTaxon();
                    assert taxon != null;
                    blatResult.getQuerySequence().setTaxon( taxon );
                }

                // here's the key line! Find gene products that map to the given blat result.
                Collection<BlatAssociation> resultsForOneBlatResult = processBlatResult( goldenPathDb, blatResult,
                        config );

                if ( resultsForOneBlatResult != null && resultsForOneBlatResult.size() > 0 ) {

                    if ( resultsForOneBlatResult.size() > 100 ) {
                        log.warn( blatResult + " for " + sequence + " has " + resultsForOneBlatResult.size()
                                + " blat associations" );
                    }

                    blatAssociationsForSequence.addAll( resultsForOneBlatResult );
                }

                // there are rarely this many, but it does happen.
                if ( ++count % 100 == 0 && log.isDebugEnabled() )
                    log.debug( "Annotations computed for " + count + " blat results for " + sequence );

            } // end of iteration over results for this sequence.

            if ( log.isDebugEnabled() ) {
                log.debug( blatAssociationsForSequence.size() + " associations for " + sequence );
            }

            if ( blatAssociationsForSequence.size() == 0 ) continue;

            // Another important step: fill in the specificity, remove duplicates
            BlatAssociationScorer.scoreResults( blatAssociationsForSequence );

            // Remove hits not meeting criteria
            blatAssociationsForSequence = filterOnScores( blatAssociationsForSequence, config );

            if ( log.isDebugEnabled() ) {
                log.debug( blatAssociationsForSequence.size() + " associations for " + sequence
                        + " after redundancy reduction and score filtering" );
            }

            String queryName = sequence.getName();
            assert StringUtils.isNotBlank( queryName );
            if ( !allRes.containsKey( queryName ) ) {
                allRes.put( queryName, new HashSet<BlatAssociation>() );
            }

            allRes.get( queryName ).addAll( blatAssociationsForSequence );

        } // end of iteration over sequence

        if ( log.isDebugEnabled() && skipped > 0 ) {
            log.debug( "Skipped " + skipped + "/" + blatResults.size()
                    + " individual blat results that didn't meet criteria; " + skippedDueToRepeat
                    + " were skipped due to repeat or low complexity content; " + skippedDueToNonSpecific
                    + " were skipped because they align to too many places in the genome (even if not a repeat)" );
        }

        return allRes;
    }

    /**
     * TODO implement checking the score, not just the exon overlap.
     * 
     * @param blatAssociationsForSequence
     * @param config
     * @return filtered collection
     */
    private Collection<BlatAssociation> filterOnScores( Collection<BlatAssociation> blatAssociationsForSequence,
            ProbeMapperConfig config ) {
        double minimumExonOverlapFraction = config.getMinimumExonOverlapFraction();
        if ( minimumExonOverlapFraction == 0 ) return blatAssociationsForSequence;

        Collection<BlatAssociation> result = new HashSet<BlatAssociation>();

        for ( BlatAssociation ba : blatAssociationsForSequence ) {
            if ( BlatAssociationScorer.computeOverlapFraction( ba ) < minimumExonOverlapFraction ) {
                log.debug( "Result failed to meet exon overlap threshold" );
                continue;
            }
            result.add( ba );
        }

        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * ubic.gemma.analysis.sequence.ProbeMapper#processBlatResults(ubic.gemma.externalDb.GoldenPathSequenceAnalysis,
     * java.util.Collection)
     */
    @Override
    public Map<String, Collection<BlatAssociation>> processBlatResults( GoldenPathSequenceAnalysis goldenPathDb,
            Collection<BlatResult> blatResults ) {
        return this.processBlatResults( goldenPathDb, blatResults, new ProbeMapperConfig() );
    }

    /**
     * group results together by BioSequence
     * 
     * @param blatResults
     * @return
     */
    private Map<BioSequence, Collection<BlatResult>> groupBlatResultsByBioSequence( Collection<BlatResult> blatResults ) {

        Map<BioSequence, Collection<BlatResult>> biosequenceToBlatResults = new HashMap<BioSequence, Collection<BlatResult>>();

        for ( BlatResult blatResult : blatResults ) {
            if ( !biosequenceToBlatResults.containsKey( blatResult.getQuerySequence() ) ) {
                biosequenceToBlatResults.put( blatResult.getQuerySequence(), new HashSet<BlatResult>() );
            }
            biosequenceToBlatResults.get( blatResult.getQuerySequence() ).add( blatResult );
        }
        return biosequenceToBlatResults;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.analysis.sequence.ProbeMapper#processGbId(ubic.gemma.externalDb.GoldenPathSequenceAnalysis,
     * java.lang.String)
     */
    @Override
    public Map<String, Collection<BlatAssociation>> processGbId( GoldenPathSequenceAnalysis goldenPathDb,
            String genbankId ) {

        log.debug( "Entering processGbId with " + genbankId );

        Collection<BlatResult> blatResults = goldenPathDb.findSequenceLocations( genbankId );

        if ( blatResults == null || blatResults.size() == 0 ) {
            log.warn( "No results obtained for " + genbankId );
        }

        return processBlatResults( goldenPathDb, blatResults );

    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.analysis.sequence.ProbeMapper#processGbIds(ubic.gemma.externalDb.GoldenPathSequenceAnalysis,
     * java.util.Collection)
     */
    @Override
    public Map<String, Collection<BlatAssociation>> processGbIds( GoldenPathSequenceAnalysis goldenPathDb,
            Collection<String[]> genbankIds ) {
        Map<String, Collection<BlatAssociation>> allRes = new HashMap<String, Collection<BlatAssociation>>();
        int count = 0;
        int skipped = 0;
        for ( String[] genbankIdAr : genbankIds ) {

            if ( genbankIdAr == null || genbankIdAr.length == 0 ) {
                continue;
            }

            if ( genbankIdAr.length > 1 ) {
                throw new IllegalArgumentException( "Input file must have just one genbank identifier per line" );
            }

            String genbankId = genbankIdAr[0];

            Map<String, Collection<BlatAssociation>> res = processGbId( goldenPathDb, genbankId );
            allRes.putAll( res );

            count++;
            if ( count % 100 == 0 ) log.info( "Annotations computed for " + count + " genbank identifiers" );
        }
        log.info( "Annotations computed for " + count + " genbank identifiers" );
        if ( log.isInfoEnabled() && skipped > 0 )
            log.info( "Skipped " + skipped + " results that didn't meet criteria" );
        return allRes;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.analysis.sequence.ProbeMapper#processSequence(ubic.gemma.externalDb.GoldenPathSequenceAnalysis,
     * ubic.gemma.model.genome.biosequence.BioSequence)
     */
    @Override
    public Collection<BlatAssociation> processSequence( GoldenPathSequenceAnalysis goldenPath, BioSequence sequence ) {

        Blat b = new Blat();
        b.setBlatScoreThreshold( ( new ProbeMapperConfig() ).getBlatScoreThreshold() );
        Collection<BlatResult> results;
        try {
            results = b.blatQuery( sequence, goldenPath.getTaxon(), false );
        } catch ( IOException e ) {
            throw new RuntimeException( "Error running blat", e );
        }
        Map<String, Collection<BlatAssociation>> allRes = processBlatResults( goldenPath, results );
        assert allRes.keySet().size() == 1;
        return allRes.values().iterator().next();
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.analysis.sequence.ProbeMapper#processSequences(ubic.gemma.externalDb.GoldenPathSequenceAnalysis,
     * java.util.Collection, ubic.gemma.analysis.sequence.ProbeMapperConfig)
     */
    @Override
    public Map<String, Collection<BlatAssociation>> processSequences( GoldenPathSequenceAnalysis goldenpath,
            Collection<BioSequence> sequences, ProbeMapperConfig config ) {
        Blat b = new Blat();
        b.setBlatScoreThreshold( config.getBlatScoreThreshold() );

        try {
            Map<BioSequence, Collection<BlatResult>> results = b.blatQuery( sequences, goldenpath.getTaxon() );
            Collection<BlatResult> blatres = new HashSet<BlatResult>();
            for ( Collection<BlatResult> coll : results.values() ) {
                blatres.addAll( coll );
            }
            Map<String, Collection<BlatAssociation>> allRes = processBlatResults( goldenpath, blatres );
            return allRes;
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }
    }

    /**
     * Process a single BlatResult, identifying gene products it maps to.
     * 
     * @param goldenPathDb
     * @param blatResult
     * @param config
     * @return BlatAssociations between the queried biosequence and one or more gene products.
     */
    private Collection<BlatAssociation> processBlatResult( GoldenPathSequenceAnalysis goldenPathDb,
            BlatResult blatResult, ProbeMapperConfig config ) {
        assert blatResult.getTargetChromosome() != null : "Chromosome not filled in for blat result";

        boolean ignoreStrand = determineStrandTreatment( blatResult );

        String strand = ignoreStrand == true ? null : blatResult.getStrand();

        Collection<BlatAssociation> blatAssociations = goldenPathDb.findAssociations( blatResult.getTargetChromosome()
                .getName(), blatResult.getTargetStart(), blatResult.getTargetEnd(), blatResult.getTargetStarts(),
                blatResult.getBlockSizes(), strand, threeprimeMethod, config );

        if ( blatAssociations != null && blatAssociations.size() > 0 ) {
            for ( BlatAssociation association : blatAssociations ) {
                association.setBlatResult( blatResult );
                association.setBioSequence( blatResult.getQuerySequence() );
            }
            return blatAssociations;
        }

        return new HashSet<BlatAssociation>();
    }

    /**
     * It is assume that strand should only be used if the sequence type is AFFY_{PROBE,COLLAPSED,TARGET} or OLIGO. In
     * all other cases (ESTs etc) the strand is ignored.
     * 
     * @param blatResult
     * @return boolean indicating, essentially, if the sequence on the array is double-stranded.
     */
    private boolean determineStrandTreatment( BlatResult blatResult ) {
        boolean ignoreStrand = true;

        SequenceType type = blatResult.getQuerySequence().getType();
        if ( type == null ) {
            return true;
        }
        if ( type.equals( SequenceType.OLIGO ) ) {
            ignoreStrand = false;
        } else if ( type.equals( SequenceType.AFFY_COLLAPSED ) ) {
            ignoreStrand = false;
        } else if ( type.equals( SequenceType.AFFY_PROBE ) ) {
            ignoreStrand = false;
        } else if ( type.equals( SequenceType.AFFY_TARGET ) ) {
            ignoreStrand = false;
        }
        return ignoreStrand;
    }

}
