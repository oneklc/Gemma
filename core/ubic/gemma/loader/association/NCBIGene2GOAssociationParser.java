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
package ubic.gemma.loader.association;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.lang.StringUtils;

import ubic.gemma.loader.util.QueuingParser;
import ubic.gemma.loader.util.parser.BasicLineParser;
import ubic.gemma.model.association.GOEvidenceCode;
import ubic.gemma.model.association.Gene2GOAssociation;
import ubic.gemma.model.common.description.DatabaseType;
import ubic.gemma.model.common.description.ExternalDatabase;
import ubic.gemma.model.common.description.VocabCharacteristic;
import ubic.gemma.model.genome.Gene;
import ubic.gemma.model.genome.Taxon;
import ubic.gemma.ontology.providers.GeneOntologyService;
import ubic.gemma.util.ConfigUtils;

/**
 * This parses GO annotations from NCBI. See {@link ftp://ftp.ncbi.nih.gov/gene/DATA/README}.
 * 
 * <pre>
 * tax_id:
 * the unique identifier provided by NCBI Taxonomy
 * for the species or strain/isolate
 * 
 * GeneID:
 * the unique identifier for a gene
 * --note:  for genomes previously available from LocusLink,
 * the identifiers are equivalent
 * 
 * GO ID:
 * the GO ID, formatted as GO:0000000
 * 
 * Evidence:
 * the evidence code in the gene_association file
 * 
 * Qualifier: 
 * a qualifier for the relationship between the gene
 * and the GO term
 * 
 * GO term:
 * the term indicated by the GO ID
 * 
 * PubMed:
 * pipe-delimited set of PubMed uids reported as evidence
 * for the association
 * 
 * Category:
 * the GO category (Function, Process, or Component)
 * </pre>
 * 
 * @author keshav
 * @author pavlidis
 * @version $Id: NCBIGene2GOAssociationParser.java,v 1.16 2012/05/27 02:58:38 paul Exp $
 */
public class NCBIGene2GOAssociationParser extends BasicLineParser<Gene2GOAssociation> implements
        QueuingParser<Gene2GOAssociation> {

    private static final String COMMENT_INDICATOR = "#";

    private final int TAX_ID = ConfigUtils.getInt( "gene2go.tax_id" );

    private final int EVIDENCE_CODE = ConfigUtils.getInt( "gene2go.evidence_code" );

    private final int GENE_ID = ConfigUtils.getInt( "gene2go.gene_id" );

    private final int GO_ID = ConfigUtils.getInt( "gene2go.go_id" );

    private static Set<String> ignoredEvidenceCodes = new HashSet<String>();

    static {
        // these are 'NOT association' codes, or (ND) one that means "nothing known", which we don't use. See
        // http://www.geneontology.org/GO.evidence.shtml.
        ignoredEvidenceCodes.add( "IMR" );
        ignoredEvidenceCodes.add( "IKR" );
        ignoredEvidenceCodes.add( "IRD" );
        ignoredEvidenceCodes.add( "ND" );
    }

    BlockingQueue<Gene2GOAssociation> queue;

    private int count = 0;
    ExternalDatabase goDb;

    int i = 0;

    private ExternalDatabase ncbiGeneDb;

    /**
     * NCBI Ids of available taxa.
     */
    private Map<Integer, Taxon> taxaNcbiIds;

    /**
     * @param taxa to consider (usually we pass in all)
     */
    public NCBIGene2GOAssociationParser( Collection<Taxon> taxa ) {
        goDb = ExternalDatabase.Factory.newInstance();
        goDb.setName( "GO" );
        goDb.setType( DatabaseType.ONTOLOGY );

        ncbiGeneDb = ExternalDatabase.Factory.newInstance();
        ncbiGeneDb.setName( "Entrez Gene" );

        this.taxaNcbiIds = new HashMap<Integer, Taxon>();
        for ( Taxon taxon : taxa ) {
            this.taxaNcbiIds.put( taxon.getNcbiId(), taxon );
            if ( taxon.getSecondaryNcbiId() != null ) {
                this.taxaNcbiIds.put( taxon.getSecondaryNcbiId(), taxon );
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.loader.util.parser.BasicLineMapParser#getResults()
     */
    @Override
    public Collection<Gene2GOAssociation> getResults() {
        return null;
    }

    /**
     * Note that "-" means a missing value, which in practice only occurs in the "qualifier" and "pubmed" columns.
     * 
     * @param line
     * @param taxa to use
     * @return Object
     */
    public Gene2GOAssociation mapFromGene2GO( String line ) {

        String[] values = StringUtils.splitPreserveAllTokens( line, "\t" );

        if ( line.startsWith( COMMENT_INDICATOR ) ) return null;

        if ( values.length < 8 ) return null;

        Integer taxonId = null;
        try {
            taxonId = Integer.parseInt( values[TAX_ID] );
        } catch ( NumberFormatException e ) {
            throw new RuntimeException( e );
        }

        if ( !taxaNcbiIds.containsKey( taxonId ) ) {
            return null;
        }

        Gene2GOAssociation g2GOAss = Gene2GOAssociation.Factory.newInstance();

        Gene gene = Gene.Factory.newInstance();
        gene.setNcbiGeneId( Integer.parseInt( values[GENE_ID] ) );

        gene.setTaxon( taxaNcbiIds.get( taxonId ) );
        VocabCharacteristic oe = VocabCharacteristic.Factory.newInstance();
        String value = values[GO_ID].replace( ":", "_" );
        oe.setValueUri( GeneOntologyService.BASE_GO_URI + value );
        oe.setValue( value );

        // g2GOAss.setSource( ncbiGeneDb );

        g2GOAss.setGene( gene );
        g2GOAss.setOntologyEntry( oe );

        String evidenceCode = values[EVIDENCE_CODE];

        if ( !( StringUtils.isBlank( evidenceCode ) || evidenceCode.equals( "-" ) ) ) {

            if ( ignoredEvidenceCodes.contains( evidenceCode ) ) {
                return null;
            }

            g2GOAss.setEvidenceCode( GOEvidenceCode.fromString( evidenceCode ) );
        }

        try {
            queue.put( g2GOAss );
        } catch ( InterruptedException e ) {
            throw new RuntimeException( e );
        }

        return g2GOAss;
    }

    @Override
    public Gene2GOAssociation parseOneLine( String line ) {
        return this.mapFromGene2GO( line );
    }

    @Override
    public void parse( InputStream inputStream, BlockingQueue<Gene2GOAssociation> aqueue ) throws IOException {
        if ( inputStream == null ) throw new IllegalArgumentException( "InputStream was null" );
        this.queue = aqueue;
        super.parse( inputStream );

    }

    @Override
    protected void addResult( Gene2GOAssociation obj ) {
        count++;
    }

    /**
     * @return
     */
    public int getCount() {
        return count;
    }

}
