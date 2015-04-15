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
package ubic.basecode.bio.geneset;

import hep.aida.IHistogram1D;
import hep.aida.ref.Histogram1D;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.basecode.util.StatusViewer;

/**
 * Methods to 'clean' a set of geneSets - to remove redundancies, for example.
 * 
 * @author Paul Pavlidis
 * @version $Id: GeneSetMapTools.java,v 1.8 2009/12/18 18:55:01 paul Exp $
 */
public class GeneSetMapTools {

    protected static final Log log = LogFactory.getLog( GeneSetMapTools.class );

    /**
     * Identify classes which are absoluely identical to others. This isn't superfast, because it doesn't know which
     * classes are actually relevant in the data.
     */
    public static void collapseGeneSets( GeneAnnotations geneData, StatusViewer messenger ) {
        Collection<String> geneSets = geneData.getGeneSets();
        Map<String, Collection<String>> classesToRedundantMap = geneData.geneSetToRedundantMap();
        Map<String, String> seenClasses = new LinkedHashMap<String, String>();
        Map<String, String> sigs = new LinkedHashMap<String, String>();

        Map<String, Boolean> seenit = new HashMap<String, Boolean>();

        if ( messenger != null ) {
            messenger.showStatus( "There are " + geneData.numGeneSets()
                    + " classes represented on the chip (of any size). Redundant classes are being removed..." );
        }

        // sort each arraylist in for each go and create a string that is a signature for this class.
        int ignored = 0;
        for ( Iterator<String> iter = geneSets.iterator(); iter.hasNext(); ) {
            String classId = iter.next();
            Collection<String> classMembers = geneData.getActiveGeneSetGenes( classId );
            if ( classMembers.size() == 0 ) continue;
            if ( classMembers.contains( null ) ) {
                classMembers.remove( null ); // FIXME why do we need to do this?
                // throw new IllegalStateException(classId + " contains null.");
            }

            // This is a bit of a hack : Skip classes that are huge. It's too slow
            // otherwise. This is a total heuristic. Note that this
            // doesn't mean the class won't get analyzed, it just
            // means we don't bother looking for redundancies. Big
            // classes are less likely to be identical to others,
            // anyway. In tests, the range shown below has no effect
            // on the results, but it _could_ matter.
            if ( classMembers.size() > 250 || classMembers.size() < 2 ) {
                continue;
            }

            List<String> cls = new Vector<String>( classMembers );

            Collections.sort( cls );
            String signature = "";
            seenit.clear();
            Iterator<String> classit = cls.iterator();
            while ( classit.hasNext() ) {
                String probeid = classit.next();
                if ( !seenit.containsKey( probeid ) ) {
                    signature = signature + "__" + probeid;
                    seenit.put( probeid, new Boolean( true ) );
                }
            }
            sigs.put( classId, signature );
        }

        // look at the signatures for repeats.
        for ( Iterator<String> iter = sigs.keySet().iterator(); iter.hasNext(); ) {
            String classId = iter.next();
            String signature = sigs.get( classId );

            // if the signature has already been seen, add it to the redundant
            // list, and remove this class from the classToProbeMap.
            if ( seenClasses.containsKey( signature ) ) {
                if ( !classesToRedundantMap.containsKey( seenClasses.get( signature ) ) ) {
                    classesToRedundantMap.put( seenClasses.get( signature ), new ArrayList<String>() );

                }
                classesToRedundantMap.get( seenClasses.get( signature ) ).add( classId );
                ignored++;
                geneData.removeClassFromMaps( classId );
                // System.err.println(classId + " is the same as an existing class, " + seenClasses.get(signature));
            } else {
                // add string to hash
                seenClasses.put( signature, classId );
            }
        }

        geneData.resetSelectedSets();
        geneData.sortGeneSets();

        if ( messenger != null ) {
            messenger.showStatus( "There are now " + geneData.numGeneSets() + " classes represented on the chip ("
                    + ignored + " were removed)" );
        }
    }

    public static IHistogram1D geneSetSizeDistribution( GeneAnnotations ga, int numBins, int minSize, int maxSize ) {
        Histogram1D hist = new Histogram1D( "Distribution of gene set sizes", numBins, minSize, maxSize );

        Collection<String> geneSets = ga.getGeneSets();
        for ( Iterator<String> iter = geneSets.iterator(); iter.hasNext(); ) {
            String geneSet = iter.next();

            Collection<String> element = ga.getActiveGeneSetGenes( geneSet );
            hist.fill( element.size() );
        }
        return hist;
    }

    /**
     * @param classId
     * @param classesToRedundantMap
     * @return
     */
    public static Collection<String> getRedundancies( String classId,
            Map<String, Collection<String>> classesToRedundantMap ) {
        if ( classesToRedundantMap != null && classesToRedundantMap.containsKey( classId ) ) {
            return classesToRedundantMap.get( classId );
        }
        return null;
    }

    /**
     * @param classId
     * @param classesToSimilarMap
     * @return
     */
    public static Collection<String> getSimilarities( String classId,
            Map<String, Collection<String>> classesToSimilarMap ) {
        if ( classesToSimilarMap != null && classesToSimilarMap.containsKey( classId ) ) {
            return classesToSimilarMap.get( classId );
        }
        return null;
    }

    /**
     * <p>
     * Remove classes which are too similar to some other class. In addition, the user can select a penalty for large
     * gene sets. Thus when two gene sets are found to be similar, the decision of which one to keep can be tuned based
     * on the size penalty. We find it useful to penalize large gene sets so we tend to keep smaller ones (but not too
     * small). Useful values of the penalty are above 1 (a value of 1 will result in the larger class always being
     * retained).
     * </p>
     * <p>
     * The amount of similarity to be tolerated is set by the parameter fractionSameThreshold, representing the fraction
     * of genes in the smaller class which are also found in the larger class. Thus, setting this threshold to be 0.0
     * means that no overlap is tolerated. Setting it to 1 means that classes will never be discarded.
     * </p>
     * 
     * @param fractionSameThreshold A value between 0 and 1, indicating how similar a class must be before it gets
     *        ditched.
     * @param ga
     * @param messenger For updating a log.
     * @param maxClassSize Large class considered. (that doesn't mean they are removed)
     * @param minClassSize Smallest class considered. (that doesn't mean they are removed)
     * @param bigClassPenalty A value greater or equal to one, indicating the cost of retaining a larger class in favor
     *        of a smaller one. The penalty is scaled with the difference in sizes of the two classes being considered,
     *        so very large classes are more heavily penalized.
     */
    public static void ignoreSimilar( double fractionSameThreshold, GeneAnnotations ga, StatusViewer messenger,
            int maxClassSize, int minClassSize, double bigClassPenalty ) {

        Map<String, Collection<String>> classesToSimilarMap = new LinkedHashMap<String, Collection<String>>();
        Collection<String> seenit = new HashSet<String>();
        Collection<String> deleteUs = new HashSet<String>();

        if ( messenger != null ) {
            messenger.showStatus( "...Highly (" + fractionSameThreshold * 100
                    + "%)  similar classes are being removed..." + ga.numGeneSets() + " to start..." );
        }

        // iterate over all the classes, starting from the smallest one.
        // List sortedList = ga.sortGeneSetsBySize();
        List<String> sortedList = new ArrayList<String>( ga.getGeneSets() );
        Collections.shuffle( sortedList );

        // OUTER - compare all classes to each other.
        for ( Iterator<String> iter = sortedList.iterator(); iter.hasNext(); ) {
            String queryClassId = iter.next();
            Collection<String> queryClassMembers = ga.getActiveGeneSetGenes( queryClassId );

            int querySize = queryClassMembers.size();

            if ( seenit.contains( queryClassId ) || querySize > maxClassSize || querySize < minClassSize ) {
                continue;
            }

            seenit.add( queryClassId );

            // INNER
            for ( Iterator<String> iterb = sortedList.iterator(); iterb.hasNext(); ) {
                String targetClassId = iterb.next();

                // / skip self comparisons and also symmetric comparisons.
                if ( seenit.contains( targetClassId ) || targetClassId.equals( queryClassId ) ) {
                    continue;
                }

                Collection<String> targetClass = ga.getActiveGeneSetGenes( targetClassId );

                int targetSize = targetClass.size();
                if ( targetSize < querySize || targetSize > maxClassSize || targetSize < minClassSize ) {
                    continue;
                }

                double sizeScore;

                if ( areSimilarClasses( targetClass, queryClassMembers, fractionSameThreshold ) ) {

                    sizeScore = ( ( double ) targetClass.size() / ( double ) queryClassMembers.size() )
                            / bigClassPenalty;

                    if ( sizeScore < 1.0 ) { // delete the larget class.
                        deleteUs.add( targetClassId );
                        seenit.add( targetClassId );
                    } else {
                        deleteUs.add( queryClassId );
                        seenit.add( queryClassId );
                        break; // query is no longer relevant, go to the next one.
                    }

                    storeSimilarSets( classesToSimilarMap, queryClassId, targetClassId );
                }

            } /* inner while */
        }
        /* end while ... */

        /* remove the ones we don't want to keep */
        Iterator<String> itrd = deleteUs.iterator();
        while ( itrd.hasNext() ) {
            String deleteMe = itrd.next();
            ga.removeClassFromMaps( deleteMe );
        }

        ga.resetSelectedSets();
        ga.sortGeneSets();

        if ( messenger != null ) {
            messenger.showStatus( "There are now " + ga.numGeneSets() + " classes represented on the chip ("
                    + deleteUs.size() + " were ignored)" );
        }
    }

    /**
     * @param ga
     * @param countEmpty if false, gene sets that have no members are not counted in the total.
     * @return The average size of the gene sets.
     */
    public static double meanGeneSetSize( GeneAnnotations ga, boolean countEmpty ) {
        double sum = 0.0;
        int n = 0;

        Collection<String> geneSets = ga.getGeneSets();

        for ( Iterator<String> iter = geneSets.iterator(); iter.hasNext(); ) {
            String geneSet = iter.next();

            Collection<String> element = ga.getActiveGeneSetGenes( geneSet );

            if ( !countEmpty && element.size() == 0 ) {
                continue;
            }

            sum += element.size();
            n++;
        }

        return sum / n;

    }

    /* ignoreSimilar */

    /**
     * @param sum
     * @param ga
     * @param countEmpty if false ,genes that have no gene sets assigned to them are not counted in the total.
     * @return The average number of gene sets per gene (per probe actually). This is a measure of gene set overlap. If
     *         the value is 1, it means that each gene is (on average) in only one set. Large values indicate larger
     *         amounts of overelap between gene sets.
     */
    public static double meanSetsPerGene( GeneAnnotations ga, boolean countEmpty ) {
        double sum = 0.0;
        int n = 0;

        Map<String, Collection<String>> probeToSetMap = ga.getProbeToGeneSetMap();

        for ( Iterator<String> iter = probeToSetMap.keySet().iterator(); iter.hasNext(); ) {
            String probe = iter.next();

            Collection<String> element;

            element = probeToSetMap.get( probe );

            if ( !countEmpty && element.size() == 0 ) {
                continue;
            }

            sum += element.size();
            n++;

        }
        return sum / n;
    }

    /**
     * @param ga
     * @param gon
     * @param messenger
     * @param aspect
     */
    public static void removeAspect( GeneAnnotations ga, GONames gon, StatusViewer messenger, String aspect ) {
        if ( !( aspect.equals( "molecular_function" ) || aspect.equals( "biological_process" ) || aspect
                .equals( "cellular_component" ) ) ) {
            throw new IllegalArgumentException( "Unknown aspect requested" );
        }

        Collection<String> geneSets = ga.getGeneSets();

        Collection<String> removeUs = new HashSet<String>();
        for ( Iterator<String> iter = geneSets.iterator(); iter.hasNext(); ) {
            String geneSet = iter.next();
            if ( gon.getAspectForId( geneSet ).equals( aspect ) ) {
                removeUs.add( geneSet );
            }
        }

        for ( String geneSet : removeUs ) {
            ga.removeClassFromMaps( geneSet );
        }

        ga.resetSelectedSets();
        ga.sortGeneSets();

        if ( messenger != null ) {
            messenger.showStatus( "There are now " + ga.numGeneSets() + " sets remaining after removing aspect "
                    + aspect );
        }
    }

    /**
     * Remove gene sets that don't meet certain criteria.
     * 
     * @param ga
     * @param messenger
     * @param minClassSize
     * @param maxClassSize
     */
    public static void removeBySize( GeneAnnotations ga, StatusViewer messenger, int minClassSize, int maxClassSize ) {

        Collection<String> geneSets = ga.getGeneSets();

        Collection<String> removeUs = new HashSet<String>();
        for ( String geneSet : geneSets ) {
            Collection<String> element = ga.getActiveGeneSetGenes( geneSet );
            if ( element.size() < minClassSize || element.size() > maxClassSize ) {
                removeUs.add( geneSet );
            }

        }

        for ( String geneSet : removeUs ) {
            ga.removeClassFromMaps( geneSet );
        }

        ga.resetSelectedSets();
        ga.sortGeneSets();

        if ( messenger != null ) {
            messenger.showStatus( "There are now " + ga.numGeneSets()
                    + " sets remaining after removing sets with excluded sizes." );
        }
    }

    /**
     * Helper function for ignoreSimilar.
     */
    private static boolean areSimilarClasses( Collection<String> biggerClass, Collection<String> smallerClass,
            double fractionSameThreshold ) {

        if ( biggerClass.size() < smallerClass.size() ) {
            throw new IllegalArgumentException( "Invalid sizes" );
        }

        /*
         * Threshold of how many items from the smaller class must NOT be in the bigger class, before we consider the
         * classes different.
         */
        int notInThresh = ( int ) Math.ceil( fractionSameThreshold * smallerClass.size() );

        int notin = 0;

        int overlap = 0;
        for ( Iterator<String> iter = smallerClass.iterator(); iter.hasNext(); ) {

            String gene = iter.next();
            if ( !biggerClass.contains( gene ) ) {
                notin++;
            } else {
                overlap++;
            }
            if ( notin > notInThresh ) {
                // return false;
            }
        }

        if ( ( double ) overlap / ( double ) smallerClass.size() > fractionSameThreshold ) {
            // log.warn( "Small class of size " + smallerClass.size()
            // + " too much contained (overlap = " + overlap
            // + ") in large class of size " + biggerClass.size() );
            return true;
        }

        /* return true is the count is high enough */
        // return true;
        return false;
    }

    /**
     * @param classesToSimilarMap
     * @param queryClassId
     * @param targetClassId
     */
    private static void storeSimilarSets( Map<String, Collection<String>> classesToSimilarMap, String queryClassId,
            String targetClassId ) {
        if ( !classesToSimilarMap.containsKey( targetClassId ) ) {
            classesToSimilarMap.put( targetClassId, new HashSet<String>() );
        }
        if ( !classesToSimilarMap.containsKey( queryClassId ) ) {
            classesToSimilarMap.put( queryClassId, new HashSet<String>() );
        }
        classesToSimilarMap.get( queryClassId ).add( targetClassId );
        classesToSimilarMap.get( targetClassId ).add( queryClassId );
    }

    /**
     * Add the parents of each term to the association for each gene.
     * 
     * @param ga
     * @param goNames
     */
    public static void addParents( GeneAnnotations ga, GONames gon, StatusViewer messenger ) {
        Collection<String> genes = ga.getGenes();
        if ( messenger != null ) {
            messenger.showStatus( "Adding parent terms (" + ga.numGeneSets() + " gene sets now)" );
        }
        Map<String, Collection<String>> toBeAdded = new HashMap<String, Collection<String>>();
        Map<String, Collection<String>> parentCache = new HashMap<String, Collection<String>>();
        int count = 0;
        for ( Iterator<String> iter = genes.iterator(); iter.hasNext(); ) {
            String gene = iter.next();

            Collection<String> geneSets = ga.getGeneGeneSets( gene );

            for ( String geneSet : geneSets ) {
                Collection<String> parents = new HashSet<String>();
                getAllParents( gon, parentCache, geneSet, parents );

                setParentsToBeAdded( toBeAdded, gene, parents );
                log.debug( "Added " + parents + " to " + geneSet + " for " + gene );
            }
            count++;
            if ( count % 1000 == 0 && messenger != null ) {
                messenger.showStatus( count + " genes examined" );
            }
        }
        parentCache = null;
        for ( Iterator<String> iter = toBeAdded.keySet().iterator(); iter.hasNext(); ) {
            String gene = iter.next();
            Collection<String> parents = toBeAdded.get( gene );
            ga.addGoTermsToGene( gene, parents );
        }

        ga.resetSelectedSets();
        ga.sortGeneSets();

        if ( messenger != null ) {
            messenger.showStatus( "Added parents to all terms - now have " + ga.numGeneSets() + " usable gene sets." );
        }
    }

    /**
     * @param toBeAdded
     * @param gene
     * @param parents
     */
    private static void setParentsToBeAdded( Map<String, Collection<String>> toBeAdded, String gene,
            Collection<String> parents ) {
        if ( parents.size() == 0 ) return;
        if ( !toBeAdded.containsKey( gene ) ) {
            toBeAdded.put( gene, parents );
        } else {
            toBeAdded.get( gene ).addAll( parents );
        }
    }

    /**
     * @param gon
     * @param parentCache
     * @param geneSet
     * @return
     */
    private static void getAllParents( GONames gon, Map<String, Collection<String>> parentCache, String geneSet,
            Collection<String> parents ) {
        // if ( parentCache.containsKey( geneSet ) ) {
        // parents = ( Set ) parentCache.get( geneSet );
        // } else {
        // recursively add all the parents.
        Collection<String> newParents = gon.getParents( geneSet );
        for ( Iterator<String> it = newParents.iterator(); it.hasNext(); ) {
            String parent = it.next();
            if ( parent.equals( "all" ) ) continue;
            if ( parents.contains( parent ) ) continue;
            getAllParents( gon, parentCache, parent, parents );
        }
        parents.addAll( newParents );
    }

    /**
     * @param classId
     * @param classesToRedundantMap
     * @return
     */
    public String getRedundanciesString( String classId, Map<String, List<String>> classesToRedundantMap ) {
        if ( classesToRedundantMap != null && classesToRedundantMap.containsKey( classId ) ) {
            List<String> redundant = classesToRedundantMap.get( classId );
            Iterator<String> it = redundant.iterator();
            String returnValue = "";
            while ( it.hasNext() ) {
                returnValue = returnValue + ", " + it.next();
            }
            return returnValue;
        }
        return "";
    }

} // end of class
