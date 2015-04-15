/*
 * The Gemma project
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

package ubic.gemma.web.controller.visualization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.gemma.analysis.expression.diff.DifferentialExpressionValueObject;
import ubic.gemma.model.expression.bioAssay.BioAssayValueObject;
import ubic.gemma.model.expression.bioAssayData.DoubleVectorValueObject;
import ubic.gemma.model.expression.experiment.ExperimentalFactor;
import ubic.gemma.model.expression.experiment.ExpressionExperimentValueObject;
import ubic.gemma.model.genome.gene.GeneValueObject;
import ubic.gemma.util.EntityUtils;

/**
 * Stores expression profile data from one expression experiment for plotting.
 * 
 * @author kelsey, paul
 * @version $Id: VisualizationValueObject.java,v 1.35 2013/04/18 22:53:12 paul Exp $
 */
public class VisualizationValueObject {

    private static String[] colors = new String[] { "red", "black", "blue", "green", "orange" };

    private static Log log = LogFactory.getLog( VisualizationValueObject.class );

    private Map<Long, String> colorMap = new HashMap<Long, String>();

    private ExpressionExperimentValueObject eevo = null;

    private Collection<FactorProfile> factorProfiles;

    private Collection<GeneExpressionProfile> profiles;

    private List<String> sampleNames;

    /**
     * used for displaying factor info in heatmap
     */
    private List<List<String>> factorValues;
    private LinkedHashMap<String, LinkedHashMap<String, String>> factorNames; // map of factor name to value-colour map
                                                                              // (for labels)
    private ArrayList<LinkedHashMap<String, String[]>> factorValueMaps; // list of factor name to value-colour maps (for
                                                                        // colouring column headers)

    public VisualizationValueObject() {
        super();
        this.profiles = new HashSet<GeneExpressionProfile>();
    }

    /**
     * @param Vectors to be plotted (should come from a single expression experiment)
     * @param genes Is list so that order is guaranteed. Need this so that colors are consistent.
     * @param validatedProbeList Probes which are flagged as 'valid' in some sense. For example, in coexpression plots
     *        these are probes that provided the coexpression evidence, to differentiate them from the ones which are
     *        just being displayed because they assay the same gene.
     * @throws IllegalArgumentException if vectors are mixed between EEs.
     */
    public VisualizationValueObject( Collection<DoubleVectorValueObject> vectors, List<GeneValueObject> genes,
            Collection<Long> validatedProbeList ) {
        this( vectors, genes, validatedProbeList, null );
    }

    /**
     * @param vectors
     * @param genes
     * @param validatedProbeList
     * @param minPvalue
     */
    public VisualizationValueObject( Collection<DoubleVectorValueObject> vectors, List<GeneValueObject> genes,
            Collection<Long> validatedProbeIdList, Double minPvalue ) {
        this();

        Map<Long, GeneValueObject> idMap = EntityUtils.getIdMap( genes );
        populateColorMap( new ArrayList<Long>( idMap.keySet() ) );

        for ( DoubleVectorValueObject vector : vectors ) {
            if ( this.eevo == null ) {
                setEEwithPvalue( vector.getExpressionExperiment(), minPvalue );
            } else if ( !( this.eevo.getId().equals( vector.getExpressionExperiment().getId() ) ) ) {
                throw new IllegalArgumentException( "All vectors have to have the same ee for this constructor. ee1: "
                        + this.eevo.getId() + "  ee2: " + vector.getExpressionExperiment().getId() );
            }

            Collection<Long> vectorGeneids = vector.getGenes();
            Collection<GeneValueObject> vectorGenes = new HashSet<GeneValueObject>();

            String color = "black";
            if ( genes != null && vectorGeneids != null ) {
                for ( GeneValueObject g : genes ) {
                    // This seems inefficient. We should just pass in the genes for this vector.
                    if ( !vectorGeneids.contains( g.getId() ) ) {
                        continue;
                    }
                    vectorGenes.add( g );
                    color = colorMap.get( g.getId() );
                }
            }

            int valid = 2; // default. // FIXME Actually, this might not work.
            if ( validatedProbeIdList != null && !validatedProbeIdList.contains( vector.getDesignElement().getId() ) ) {
                valid = 1;
            }

            GeneExpressionProfile profile = new GeneExpressionProfile( vector, vectorGenes, color, valid,
                    vector.getPvalue() );

            if ( !profile.isAllMissing() ) profiles.add( profile );

        }
    }

    /**
     * @param vectors
     * @param genes
     * @param validatedProbeList
     * @param minPvalue
     */
    public VisualizationValueObject( Collection<DoubleVectorValueObject> vectors, Collection<GeneValueObject> genes,
            Double minPvalue, Collection<DifferentialExpressionValueObject> validatedProbes ) {
        this();

        Map<Long, GeneValueObject> idMap = EntityUtils.getIdMap( genes );
        populateColorMap( new ArrayList<Long>( idMap.keySet() ) );

        Collection<Long> validatedProbeIdList = new ArrayList<Long>();
        if ( validatedProbes != null && !validatedProbes.isEmpty() ) {
            for ( DifferentialExpressionValueObject devo : validatedProbes ) {
                validatedProbeIdList.add( devo.getProbeId() );
            }
        }

        for ( DoubleVectorValueObject vector : vectors ) {
            if ( this.eevo == null ) {
                setEEwithPvalue( vector.getExpressionExperiment(), minPvalue );
            } else if ( !( this.eevo.getId().equals( vector.getExpressionExperiment().getId() ) ) ) {
                throw new IllegalArgumentException( "All vectors have to have the same ee for this constructor. ee1: "
                        + this.eevo.getId() + "  ee2: " + vector.getExpressionExperiment().getId() );
            }

            String color = null;
            Collection<Long> vectorGeneids = vector.getGenes();
            Collection<GeneValueObject> vectorGenes = new HashSet<GeneValueObject>();
            for ( Long g : idMap.keySet() ) {
                if ( !vectorGeneids.contains( g ) ) {
                    continue;
                }
                vectorGenes.add( idMap.get( g ) );
                color = colorMap.get( g );
            }

            int valid = 1;
            Double pValue = vector.getPvalue();

            if ( validatedProbes != null ) {
                for ( DifferentialExpressionValueObject devo : validatedProbes ) {
                    if ( devo.getProbeId().equals( vector.getDesignElement().getId() ) ) {
                        // pValue = devo.getP();
                        valid = 2;
                        break;
                    }
                }
            }
            GeneExpressionProfile profile = new GeneExpressionProfile( vector, vectorGenes, color, valid, pValue );

            if ( !profile.isAllMissing() ) profiles.add( profile );

        }
    }

    /**
     * @param dvvo
     */
    public VisualizationValueObject( DoubleVectorValueObject dvvo ) {
        this();
        setEevo( dvvo.getExpressionExperiment() );
        GeneExpressionProfile profile = new GeneExpressionProfile( dvvo );
        profiles.add( profile );
    }

    public ExpressionExperimentValueObject getEevo() {
        return eevo;
    }

    /**
     * @return the factorProfiles
     */
    public Collection<FactorProfile> getFactorProfiles() {
        return factorProfiles;
    }

    public Collection<GeneExpressionProfile> getProfiles() {
        return profiles;
    }

    /**
     * @return the sampleNames
     */
    public List<String> getSampleNames() {
        return sampleNames;
    }

    public List<List<String>> getFactorValues() {
        return factorValues;
    }

    public LinkedHashMap<String, LinkedHashMap<String, String>> getFactorNames() {
        return factorNames;
    }

    public ArrayList<LinkedHashMap<String, String[]>> getFactorValuesToNames() {
        return factorValueMaps;
    }

    public void setFactorValuesToNames( ArrayList<LinkedHashMap<String, String[]>> factorValueMaps2 ) {
        this.factorValueMaps = factorValueMaps2;
    }

    public void setFactorValues( List<List<String>> factorValues ) {
        this.factorValues = factorValues;
    }

    public void setFactorNames( LinkedHashMap<String, LinkedHashMap<String, String>> factorNames2 ) {
        this.factorNames = factorNames2;
    }

    public void setEevo( ExpressionExperimentValueObject eevo ) {
        this.eevo = eevo;
    }

    // ---------------------------------
    // Getters and Setters
    // ---------------------------------

    public void setEEwithPvalue( ExpressionExperimentValueObject ee, Double minP ) {
        setEevo( ee );
        this.eevo.setMinPvalue( minP );
    }

    /**
     * @param factorProfiles the factorProfiles to set
     */
    public void setFactorProfiles( Collection<FactorProfile> factorProfiles ) {
        this.factorProfiles = factorProfiles;
    }

    public void setProfiles( Collection<GeneExpressionProfile> profiles ) {
        this.profiles = profiles;
    }

    /**
     * @param sampleNames the sampleNames to set
     */
    public void setSampleNames( List<String> sampleNames ) {
        this.sampleNames = sampleNames;
    }

    /**
     * Initialize the factor profiles.
     * 
     * @param layout
     */
    public void setUpFactorProfiles(
            LinkedHashMap<BioAssayValueObject, LinkedHashMap<ExperimentalFactor, Double>> layout ) {
        if ( layout == null ) {
            log.warn( "Null layout, ignoring" );
            return;
        }

        Collection<ExperimentalFactor> efs = new HashSet<ExperimentalFactor>();
        for ( LinkedHashMap<ExperimentalFactor, Double> maps : layout.values() ) {
            efs.addAll( maps.keySet() );
        }

        this.factorProfiles = new ArrayList<FactorProfile>();
        for ( ExperimentalFactor experimentalFactor : efs ) {
            this.factorProfiles.add( new FactorProfile( experimentalFactor, layout ) );
        }
    }

    /**
     * @param genes
     */
    private void populateColorMap( List<Long> genes ) {
        int i = 0;
        if ( genes.size() > colors.length ) {
            // / FIXME -- we just cycle through for now.
        }
        for ( Long g : genes ) {
            colorMap.put( g, colors[i % colors.length] );
            i++;
        }
    }

}
