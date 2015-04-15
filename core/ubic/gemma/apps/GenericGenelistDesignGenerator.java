/*
 * The Gemma project
 * 
 * Copyright (c) 2010 University of British Columbia
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
package ubic.gemma.apps;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.lang.StringUtils;
import ubic.gemma.analysis.report.ArrayDesignReportService;
import ubic.gemma.analysis.service.ArrayDesignAnnotationService;
import ubic.gemma.genome.gene.service.GeneService;
import ubic.gemma.genome.taxon.service.TaxonService;
import ubic.gemma.model.common.auditAndSecurity.eventType.AnnotationBasedGeneMappingEvent;
import ubic.gemma.model.common.description.DatabaseEntry;
import ubic.gemma.model.common.description.ExternalDatabase;
import ubic.gemma.model.common.description.ExternalDatabaseService;
import ubic.gemma.model.expression.arrayDesign.ArrayDesign;
import ubic.gemma.model.expression.arrayDesign.ArrayDesignService;
import ubic.gemma.model.expression.arrayDesign.TechnologyType;
import ubic.gemma.model.expression.designElement.CompositeSequence;
import ubic.gemma.model.expression.designElement.CompositeSequenceService;
import ubic.gemma.model.genome.Gene;
import ubic.gemma.model.genome.Taxon;
import ubic.gemma.model.genome.biosequence.BioSequence;
import ubic.gemma.model.genome.biosequence.BioSequenceService;
import ubic.gemma.model.genome.biosequence.PolymerType;
import ubic.gemma.model.genome.biosequence.SequenceType;
import ubic.gemma.model.genome.gene.GeneProduct;
import ubic.gemma.model.genome.gene.GeneProductType;
import ubic.gemma.model.genome.sequenceAnalysis.AnnotationAssociation;
import ubic.gemma.model.genome.sequenceAnalysis.AnnotationAssociationService;
import ubic.gemma.util.AbstractCLIContextCLI;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Creates an array design based on the current set of transcripts for a taxon.
 * <p>
 * This is used to create a 'platform' for linking non-array based data to the system, or data for which we have only
 * gene or transcript-level information.
 * <p>
 * See also: To generate annotation files for all genes in a taxon, this can also accomplished by
 * ArrayDesignAnnotationFileCli. The difference here is that an array design is actually created.
 * 
 * @author paul
 * @version $Id: GenericGenelistDesignGenerator.java,v 1.30 2013/02/18 18:36:46 anton Exp $
 */
public class GenericGenelistDesignGenerator extends AbstractCLIContextCLI {
    public static void main( String[] args ) {
        GenericGenelistDesignGenerator b = new GenericGenelistDesignGenerator();
        b.doWork( args );
    }

    private AnnotationAssociationService annotationAssociationService;
    private ArrayDesignAnnotationService arrayDesignAnnotationService;
    private ArrayDesignService arrayDesignService;
    private BioSequenceService bioSequenceService;
    private CompositeSequenceService compositeSequenceService;
    private ExternalDatabaseService externalDatabaseService;
    private ArrayDesignReportService arrayDesignReportService;
    private GeneService geneService;

    private Taxon taxon = null;

    private TaxonService taxonService;

    private boolean useNCBIIds = false;
    private boolean useEnsemblIds = false;

    @SuppressWarnings("static-access")
    @Override
    protected void buildOptions() {
        Option taxonOption = OptionBuilder.hasArg().withDescription( "taxon name" )
                .withDescription( "Taxon of the genes" ).withLongOpt( "taxon" ).isRequired().create( 't' );
        addOption( taxonOption );

        addOption( OptionBuilder.withDescription( "use NCBI numeric IDs as the identifiers instead of gene symbols" )
                .create( "ncbiids" ) );

        addOption( OptionBuilder.withDescription( "use Ensembl identifiers instead of gene symbols" )
                .create( "ensembl" ) );

    }

    @Override
    protected Exception doWork( String[] args ) {
        super.processCommandLine( "Update or create a 'platform' based on the genes for the organism", args );

        ExternalDatabase genbank = externalDatabaseService.find( "Genbank" );
        ExternalDatabase ensembl = externalDatabaseService.find( "Ensembl" );
        assert genbank != null;
        assert ensembl != null;

        /*
         * Create the stub array design for the organism. The name and etc. are generated automatically. If the design
         * exists, we update it.
         */

        String shortName = generateShortName();

        ArrayDesign arrayDesign = ArrayDesign.Factory.newInstance();
        arrayDesign.setShortName( shortName );

        // common name
        arrayDesign.setPrimaryTaxon( taxon );
        // fixme this is ugly just use one variable
        String ncbiIDNameExtra = useNCBIIds ? ", indexed by NCBI IDs" : "";
        String ensemblNameExtra = useEnsemblIds ? ", indexed by Ensembl IDs" : "";

        arrayDesign.setName( "Generic platform for " + taxon.getScientificName() + ncbiIDNameExtra + ensemblNameExtra );
        arrayDesign.setDescription( "Created by Gemma" );
        arrayDesign.setTechnologyType( TechnologyType.NONE ); // this is key

        if ( arrayDesignService.find( arrayDesign ) != null ) {
            log.info( "Platform for " + taxon + " already exists, will update" );
            arrayDesign = arrayDesignService.find( arrayDesign );
            arrayDesignService.deleteGeneProductAssociations( arrayDesign );
            arrayDesign = arrayDesignService.load( arrayDesign.getId() );

        } else {
            log.info( "Creating new 'generic' platform" );
            arrayDesign = arrayDesignService.create( arrayDesign );
        }
        arrayDesign = arrayDesignService.thaw( arrayDesign );

        // temporary: making sure we set it, as it is new.
        arrayDesign.setTechnologyType( TechnologyType.NONE );

        /*
         * Load up the genes for the organism.
         */
        Collection<Gene> knownGenes = geneService.loadAll( taxon );
        log.info( "Taxon has " + knownGenes.size() + " genes" );

        // Map<Gene, CompositeSequence> existingGeneMap = getExistingGeneMap( arrayDesign );
        Map<String, CompositeSequence> existingSymbolmap = getExistingProbeNameMap( arrayDesign );

        int count = 0;
        int numWithNoTranscript = 0;
        // int hasGeneAlready = 0;
        // int numNewGenes = 0;
        int numNewElements = 0;
        int numUpdatedElements = 0;
        for ( Gene gene : knownGenes ) {
            gene = geneService.thaw( gene );

            Collection<GeneProduct> products = gene.getProducts();

            if ( products.isEmpty() ) {
                numWithNoTranscript++;
                log.debug( "No transcript for " + gene );
                continue;
            }

            count++;

            CompositeSequence csForGene = null;

            if ( useNCBIIds ) {
                if ( gene.getNcbiGeneId() == null ) {
                    log.debug( "No NCBI ID for " + gene + ", skipping" );
                    continue;
                }
                if ( existingSymbolmap.containsKey( gene.getNcbiGeneId() ) ) {
                    csForGene = existingSymbolmap.get( gene.getNcbiGeneId() );
                }
            } else if ( useEnsemblIds ) {
                if ( gene.getEnsemblId() == null ) {
                    log.debug( "No Ensembl ID for " + gene + ", skipping" );
                    continue;
                }
                if ( existingSymbolmap.containsKey( gene.getEnsemblId() ) ) {
                    csForGene = existingSymbolmap.get( gene.getEnsemblId() );
                }
            } else {
                if ( existingSymbolmap.containsKey( gene.getOfficialSymbol() ) ) {
                    csForGene = existingSymbolmap.get( gene.getOfficialSymbol() );
                }
            }

            assert csForGene == null || csForGene.getId() != null : "Null id for " + csForGene;

            /*
             * We arbitrarily link the "probe" to one of the gene's RNA transcripts. We could consider other strategies
             * to pick the representative, but it generally doesn't matter.
             */
            for ( GeneProduct geneProduct : products ) {
                if ( !GeneProductType.RNA.equals( geneProduct.getType() ) ) {
                    continue;
                }

                /*
                 * Name is usually the genbank or ensembl accession
                 */
                String name = geneProduct.getName();
                BioSequence bioSequence = BioSequence.Factory.newInstance();
                Collection<DatabaseEntry> accessions = geneProduct.getAccessions();
                bioSequence.setName( name );
                bioSequence.setTaxon( taxon );

                bioSequence.setPolymerType( PolymerType.RNA );

                // FIXME miRNAs (though, we don't really use this field.)
                bioSequence.setType( SequenceType.mRNA );
                BioSequence existing = null;

                if ( accessions.isEmpty() ) {
                    // this should not be hit.
                    log.warn( "No accession for " + name );
                    DatabaseEntry de = DatabaseEntry.Factory.newInstance();
                    de.setAccession( name );
                    if ( name.startsWith( "ENS" ) && name.length() > 10 ) {
                        de.setExternalDatabase( ensembl );
                    } else {
                        if ( name.matches( "^[A-Z]{1,2}(_?)[0-9]+(\\.[0-9]+)?$" ) ) {
                            de.setExternalDatabase( genbank );
                        } else {
                            log.info( "Name doesn't look like genbank or ensembl, skipping: " + name );
                            continue;
                        }
                    }
                    bioSequence.setSequenceDatabaseEntry( de );
                } else {
                    bioSequence.setSequenceDatabaseEntry( accessions.iterator().next() );
                    existing = bioSequenceService.findByAccession( accessions.iterator().next() );

                    // FIXME It is possible that this sequence will have been aligned to the genome, which is a bit
                    // confusing. So it will map to a gene. Worse case: it maps to more than one gene ...

                }

                if ( existing == null ) {
                    bioSequence = ( BioSequence ) getPersisterHelper().persist( bioSequence );
                } else {
                    bioSequence = existing;
                }

                assert bioSequence != null && bioSequence.getId() != null;

                if ( bioSequence.getSequenceDatabaseEntry() == null ) {
                    log.info( "No DB entry for " + bioSequence + "(" + gene
                            + "), will look for a better sequence to use ..." );
                    continue;
                }

                if ( csForGene == null ) {
                    if ( log.isDebugEnabled() ) log.debug( "New element " + " with " + bioSequence + " for " + gene );
                    csForGene = CompositeSequence.Factory.newInstance();
                    if ( useNCBIIds ) {
                        if ( gene.getNcbiGeneId() == null ) {
                            continue;
                        }
                        csForGene.setName( gene.getNcbiGeneId().toString() );
                    } else if ( useEnsemblIds ) {
                        if ( gene.getEnsemblId() == null ) {
                            continue;
                        }
                        csForGene.setName( gene.getEnsemblId() );
                    } else {
                        csForGene.setName( gene.getOfficialSymbol() );
                    }

                    csForGene.setArrayDesign( arrayDesign );
                    csForGene.setBiologicalCharacteristic( bioSequence );
                    csForGene.setDescription( "Generic expression element for " + gene );
                    csForGene = compositeSequenceService.create( csForGene );
                    assert csForGene.getId() != null : "No id for " + csForGene + " for " + gene;
                    arrayDesign.getCompositeSequences().add( csForGene );
                    numNewElements++;
                } else {
                    if ( log.isDebugEnabled() )
                        log.debug( "Updating existing element: " + csForGene + " with " + bioSequence + " for " + gene );
                    csForGene.setArrayDesign( arrayDesign );
                    csForGene.setBiologicalCharacteristic( bioSequence );
                    csForGene.setDescription( "Generic expression element for " + gene );
                    assert csForGene.getId() != null : "No id for " + csForGene + " for " + gene;
                    compositeSequenceService.update( csForGene );

                    // making sure ...
                    csForGene = compositeSequenceService.load( csForGene.getId() );
                    assert csForGene.getId() != null;
                    arrayDesign.getCompositeSequences().add( csForGene );

                    numUpdatedElements++;
                }

                assert bioSequence.getId() != null;
                assert geneProduct.getId() != null;
                assert csForGene.getBiologicalCharacteristic() != null
                        && csForGene.getBiologicalCharacteristic().getId() != null;

                AnnotationAssociation aa = AnnotationAssociation.Factory.newInstance();
                aa.setGeneProduct( geneProduct );
                aa.setBioSequence( bioSequence );
                annotationAssociationService.create( aa );

                break;
            }

            if ( count % 100 == 0 )
                log.info( count + " genes processed; " + numNewElements + " new elements; " + numUpdatedElements
                        + " updated elements; " + numWithNoTranscript + " genes had no transcript and were skipped." );
        }

        arrayDesignService.update( arrayDesign );

        log.info( "Array design has " + arrayDesignService.numCompositeSequenceWithGenes( arrayDesign )
                + " 'probes' associated with genes." );

        arrayDesignReportService.generateArrayDesignReport( arrayDesign.getId() );
        auditTrailService.addUpdateEvent( arrayDesign, AnnotationBasedGeneMappingEvent.Factory.newInstance(), count
                + " genes processed; " + numNewElements + " new elements; " + numUpdatedElements
                + " updated elements; " + numWithNoTranscript + " genes had no transcript and were skipped." );
        try {
            arrayDesignAnnotationService.deleteExistingFiles( arrayDesign );
        } catch ( IOException e ) {
            log.error( "Problem deleting old annotation files: " + e.getMessage() );
        }

        log.info( "Don't forget to update the annotation files" );

        return null;

    }

    @Override
    protected void processOptions() {
        super.processOptions();

        geneService = this.getBean( GeneService.class );
        arrayDesignAnnotationService = this.getBean( ArrayDesignAnnotationService.class );
        taxonService = getBean( TaxonService.class );
        bioSequenceService = getBean( BioSequenceService.class );
        arrayDesignService = getBean( ArrayDesignService.class );
        compositeSequenceService = getBean( CompositeSequenceService.class );
        annotationAssociationService = getBean( AnnotationAssociationService.class );
        externalDatabaseService = getBean( ExternalDatabaseService.class );
        arrayDesignReportService = getBean( ArrayDesignReportService.class );

        if ( hasOption( 't' ) ) {
            String taxonName = getOptionValue( 't' );
            this.taxon = taxonService.findByCommonName( taxonName );
            if ( taxon == null ) {
                log.error( "ERROR: Cannot find taxon " + taxonName );
            }
        }
        if ( hasOption( "ncbiids" ) ) {
            this.useNCBIIds = true;
        } else if ( hasOption( "ensembl" ) ) {
            this.useEnsemblIds = true;
        }

        if ( useNCBIIds && useEnsemblIds ) {
            throw new IllegalArgumentException( "Choose one of ensembl or ncbi ids or gene symbols" );
        }
    }

    /**
     * @return
     */
    private String generateShortName() {
        String ncbiIdSuffix = useNCBIIds ? "_ncbiIds" : "";
        String ensemblIdSuffix = useEnsemblIds ? "_ensemblIds" : "";
        String shortName = "";
        if ( StringUtils.isBlank( taxon.getCommonName() ) ) {
            shortName = "Generic_" + StringUtils.strip( taxon.getScientificName() ).replaceAll( " ", "_" )
                    + ncbiIdSuffix;
        } else {
            shortName = "Generic_" + StringUtils.strip( taxon.getCommonName() ).replaceAll( " ", "_" ) + ncbiIdSuffix
                    + ensemblIdSuffix;
        }
        return shortName;
    }

    /**
     * @param arrayDesign
     * @return
     */
    private Map<String, CompositeSequence> getExistingProbeNameMap( ArrayDesign arrayDesign ) {

        Map<String, CompositeSequence> existingElements = new HashMap<String, CompositeSequence>();

        if ( arrayDesign.getCompositeSequences().isEmpty() ) return existingElements;

        for ( CompositeSequence cs : arrayDesign.getCompositeSequences() ) {
            assert cs.getId() != null : "Null id for " + cs;
            existingElements.put( cs.getName(), cs );
        }
        return existingElements;
    }

}
