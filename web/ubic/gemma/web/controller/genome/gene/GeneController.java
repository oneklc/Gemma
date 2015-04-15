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
package ubic.gemma.web.controller.genome.gene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import ubic.gemma.analysis.service.ExpressionDataFileService;
import ubic.gemma.association.phenotype.PhenotypeAssociationManagerService;
import ubic.gemma.genome.gene.service.GeneCoreService;
import ubic.gemma.genome.gene.service.GeneService;
import ubic.gemma.genome.gene.service.GeneSetService;
import ubic.gemma.genome.taxon.service.TaxonService;
import ubic.gemma.image.aba.AllenBrainAtlasService;
import ubic.gemma.image.aba.Image;
import ubic.gemma.image.aba.ImageSeries;
import ubic.gemma.loader.genome.gene.ncbi.homology.HomologeneService;
import ubic.gemma.model.common.description.AnnotationValueObject;
import ubic.gemma.model.expression.designElement.CompositeSequence;
import ubic.gemma.model.genome.Gene;
import ubic.gemma.model.genome.Taxon;
import ubic.gemma.model.genome.gene.GeneProductValueObject;
import ubic.gemma.model.genome.gene.GeneValueObject;
import ubic.gemma.model.genome.gene.phenotype.EvidenceFilter;
import ubic.gemma.model.genome.gene.phenotype.valueObject.EvidenceValueObject;
import ubic.gemma.web.controller.BaseController;
import ubic.gemma.web.controller.WebConstants;
import ubic.gemma.web.image.aba.ImageValueObject;
import ubic.gemma.web.view.TextView;

/**
 * @author daq2101
 * @author pavlidis
 * @author joseph
 * @version $Id: GeneController.java,v 1.82 2013/01/25 03:10:19 anton Exp $
 */
@Controller
@RequestMapping(value = { "/gene", "/g" })
public class GeneController extends BaseController {

    @Autowired private AllenBrainAtlasService allenBrainAtlasService;
    @Autowired private HomologeneService homologeneService;
    @Autowired private GeneService geneService;
    @Autowired private TaxonService taxonService;
    @Autowired private GeneSetService geneSetService;
    @Autowired private PhenotypeAssociationManagerService phenotypeAssociationManagerService;
    @Autowired private GeneCoreService geneCoreService;

    /**
     * For ajax
     * @return
     */
    public Collection<AnnotationValueObject> findGOTerms( Long geneId ) {
        return geneService.findGOTerms( geneId );
    }

    /**
     * For ajax.
     * @return
     */
    public Collection<GeneProductValueObject> getProducts( Long geneId ) {
        if ( geneId == null ) throw new IllegalArgumentException( "Null id for gene" );
        return geneService.getProducts( geneId );
    }

    /**
     * AJAX used for gene page
     * 
     * @param geneId
     * @return
     */
    public GeneValueObject loadGeneDetails( Long geneId ) {
        return geneCoreService.loadGeneDetails( geneId );
    }

    /**
     * used to show gene info in the phenotype tab
     */
    public Collection<EvidenceValueObject> loadGeneEvidence( Long taxonId, boolean showOnlyEditable,
            Long geneId, String[] phenotypeValueUris ) {
        return phenotypeAssociationManagerService.findEvidenceByGeneId(
                geneId,
                phenotypeValueUris == null ? new HashSet<String>() : new HashSet<String>( Arrays
                        .asList( phenotypeValueUris ) ), new EvidenceFilter( taxonId, showOnlyEditable ) );
    }

    /**
     * @param geneId
     * @return
     */
    public GeneValueObject loadGenePhenotypes( Long geneId ) {
        return geneService.loadGenePhenotypes( geneId );
    }

    /**
     * @param request
     * @param response
     * @return ModelAndView
     */
    @RequestMapping(value = { "/showGene.html", "/" }, method = RequestMethod.GET)
    public ModelAndView show( HttpServletRequest request, HttpServletResponse response ) {

        String idString = request.getParameter( "id" );
        String ncbiId = request.getParameter( "ncbiid" );
        String geneName = request.getParameter( "name" );
        String taxonName = request.getParameter( "taxon" );        
        String ensemblId = request.getParameter( "ensemblId" );        

        GeneValueObject geneVO = null;

        try {
        	if (StringUtils.isNotBlank( idString )) {
        		Long id = Long.parseLong( idString );
        		assert id != null;
        		
        		geneVO = geneService.loadValueObject( id );
        		
        		if ( geneVO == null ) {
        			addMessage( request, "object.notfound", new Object[] { "Gene " + id } );
        			return new ModelAndView( "index" );
        		}
        	} else if ( StringUtils.isNotBlank( ncbiId ) ) {
        		
        		geneVO = geneService.findByNCBIIdValueObject( Integer.parseInt( ncbiId ) );
        	
        	} else if (StringUtils.isNotBlank( ensemblId )) {    
        		Collection<Gene> foundGenes = (Collection<Gene>) geneService.findByEnsemblId(ensemblId);
        	
        		if (foundGenes.size() == 1) {
        			Gene gene = foundGenes.iterator().next();
        			if (gene != null) {
        				geneVO = geneService.loadValueObject( gene.getId() );                        	
        			}                    	
        		}
        	} else if (StringUtils.isNotBlank( geneName ) && StringUtils.isNotBlank( taxonName )) {
        		Taxon taxon = taxonService.findByCommonName( taxonName );
        		if (taxon != null) {
        			Gene gene = geneService.findByOfficialSymbol( geneName, taxon );
        			if (gene != null) {
        				geneVO = geneService.loadValueObject( gene.getId() );                        	
        			}
        		}
        	}

        } catch ( NumberFormatException e ) {
        	addMessage( request, "object.notfound", new Object[] { "Gene" } );
        	return new ModelAndView( "index" );        	
        }

        if ( geneVO == null ) {
        	addMessage( request, "object.notfound", new Object[] { "Gene"} );
        	return new ModelAndView( "index" );
        }

        Long id = geneVO.getId();

        assert id != null;
        ModelAndView mav = new ModelAndView( "gene.detail" );
        mav.addObject( "geneId", id );
        mav.addObject( "geneOfficialSymbol", geneVO.getOfficialSymbol() );
        mav.addObject( "geneOfficialName", geneVO.getOfficialName() );
        mav.addObject( "geneNcbiId", geneVO.getNcbiId() );
        mav.addObject( "geneTaxonCommonName", geneVO.getTaxonCommonName() );
        mav.addObject( "geneTaxonId", geneVO.getTaxonId() );

        return mav;
    }

    /**
     * @param request
     * @param response
     * @return ModelAndView
     */
    @RequestMapping(value = { "/showGenes.html", "/show.html" }, method = RequestMethod.GET)
    public ModelAndView showMultiple( HttpServletRequest request, HttpServletResponse response ) {

        String sId = request.getParameter( "id" );
        Collection<GeneValueObject> genes = new ArrayList<GeneValueObject>();
        // if no IDs are specified, then show an error message
        if ( sId == null ) {
            addMessage( request, "object.notfound", new Object[] { "All genes cannot be listed. Genes " } );
        }

        // if ids are specified, then display only those genes
        else {
            String[] idList = StringUtils.split( sId, ',' );

            for ( int i = 0; i < idList.length; i++ ) {
                Long id = Long.parseLong( idList[i] );
                GeneValueObject gene = geneService.loadValueObject( id );
                if ( gene == null ) {
                    addMessage( request, "object.notfound", new Object[] { "Gene " + id } );
                }
                genes.add( gene );
            }
        }
        /*
         * FIXME this view doesn't exist!
         */
        return new ModelAndView( "genes" ).addObject( "genes", genes );

    }

    /**
     * Used to display the probe browser, when starting by gene.
     * 
     * @param request
     * @param response
     * @return ModelAndView
     */
    @RequestMapping("/showCompositeSequences.html")
    public ModelAndView showCompositeSequences( HttpServletRequest request, HttpServletResponse response ) {

        // gene id.
        Long id = Long.parseLong( request.getParameter( "id" ) );
        GeneValueObject gene = geneService.loadValueObject( id );
        if ( gene == null ) {
            addMessage( request, "object.notfound", new Object[] { "Gene with id: " + request.getParameter( "id" ) } );
            StringBuffer requestURL = request.getRequestURL();
            log.info( requestURL );
            return new ModelAndView( WebConstants.HOME_PAGE );
        }
        Collection<CompositeSequence> compositeSequences = geneService.getCompositeSequencesById( id );

        ModelAndView mav = new ModelAndView( "compositeSequences.geneMap" );
        mav.addObject( "numCompositeSequences", compositeSequences.size() );

        // fill in by ajax instead.
        // Collection<Object[]> rawSummaries = compositeSequenceService.getRawSummary( compositeSequences, 0 );
        // Collection<CompositeSequenceMapValueObject> summaries = arrayDesignMapResultService
        // .getSummaryMapValueObjects( rawSummaries );
        //
        // if ( summaries == null || summaries.size() == 0 ) {
        // // / FIXME, return error or do something else intelligent.
        // }
        // mav.addObject( "sequenceData", summaries );

        StringBuilder buf = new StringBuilder();
        for ( CompositeSequence sequence : compositeSequences ) {
            buf.append( sequence.getId() );
            buf.append( "," );
        }
        mav.addObject( "compositeSequenceIdList", buf.toString().replaceAll( ",$", "" ) );

        mav.addObject( "gene", gene );

        return mav;
    }

    /**
     * AJAX NOTE: this method updates the value object passed in
     */
    public Collection<ImageValueObject> loadAllenBrainImages( Long geneId ) {
        Collection<ImageValueObject> images = new ArrayList<ImageValueObject>();
        GeneValueObject gene = geneService.loadValueObject( geneId );

        String queryGeneSymbol = gene.getOfficialSymbol();
        GeneValueObject mouseGene = gene;
        boolean usingHomologue = false;
        if ( !gene.getTaxonCommonName().equals( "mouse" ) ) {
            mouseGene = this.homologeneService.getHomologueValueObject( geneId, "mouse" );
            usingHomologue = true;
        }

        if ( mouseGene != null ) {
            Collection<ImageSeries> imageSeries = null;

            try {
                imageSeries = allenBrainAtlasService.getRepresentativeSaggitalImages( mouseGene.getOfficialSymbol() );
                String abaGeneUrl = allenBrainAtlasService.getGeneUrl( mouseGene.getOfficialSymbol() );

                Collection<Image> representativeImages = allenBrainAtlasService.getImagesFromImageSeries( imageSeries );
                images = ImageValueObject.convert2ValueObjects( representativeImages, abaGeneUrl, new GeneValueObject(
                        mouseGene ), queryGeneSymbol, usingHomologue );

            } catch ( IOException e ) {
                log.warn( "Could not get ABA data: " + e );
            }
        }
        return images;
    }

    /**
     * Returns a collection of {@link Long} ids from strings.
     * 
     * @param idString
     * @return
     */
    protected Collection<Long> extractIds( String idString ) {
        Collection<Long> ids = new ArrayList<Long>();
        if ( idString != null ) {
            for ( String s : idString.split( "," ) ) {
                try {
                    ids.add( Long.parseLong( s.trim() ) );
                } catch ( NumberFormatException e ) {
                    log.warn( "invalid id " + s );
                }
            }
        }
        return ids;
    }

    /*
     * Handle case of text export of a list of genes
     * 
     * @see org.springframework.web.servlet.mvc.AbstractFormController#handleRequestInternal(javax.servlet.http.
     * HttpServletRequest, javax.servlet.http.HttpServletResponse) Called by /Gemma/gene/downloadGeneList.html
     */
    @RequestMapping("/downloadGeneList.html")
    protected ModelAndView handleRequestInternal( HttpServletRequest request ) {

        StopWatch watch = new StopWatch();
        watch.start();

        Collection<Long> geneIds = extractIds( request.getParameter( "g" ) ); // might not be any
        Collection<Long> geneSetIds = extractIds( request.getParameter( "gs" ) ); // might not be there
        String geneSetName = request.getParameter( "gsn" ); // might not be there

        ModelAndView mav = new ModelAndView( new TextView() );
        if ( ( geneIds == null || geneIds.isEmpty() ) && ( geneSetIds == null || geneSetIds.isEmpty() ) ) {
            mav.addObject( "text", "Could not find genes to match gene ids: {" + geneIds + "} or gene set ids {"
                    + geneSetIds + "}" );
            return mav;
        }
        Collection<GeneValueObject> genes = new ArrayList<GeneValueObject>();
        if ( geneIds != null ) {
            for ( Long id : geneIds ) {
                genes.add( geneService.loadValueObject( id ) );
            }
        }
        if ( geneSetIds != null ) {
            for ( Long id : geneSetIds ) {
                genes.addAll( geneSetService.getGenesInGroup( id ) );
            }
        }

        mav.addObject( "text", format4File( genes, geneSetName ) );
        watch.stop();
        Long time = watch.getTime();

        if ( time > 100 ) {
            log.info( "Retrieved and Formated" + genes.size() + " genes in : " + time + " ms." );
        }
        return mav;

    }

    private String format4File( Collection<GeneValueObject> genes, String geneSetName ) {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append( "# Generated by Gemma\n# " + ( new Date() ) + "\n" );
        strBuff.append( ExpressionDataFileService.DISCLAIMER + "#\n" );

        if ( geneSetName != null && geneSetName.length() != 0 ) strBuff.append( "# Gene Set: " + geneSetName + "\n" );
        strBuff.append( "# " + genes.size() + ( ( genes.size() > 1 ) ? " genes" : " gene" ) + "\n" );

        // add header
        strBuff.append( "Gene Symbol\tGene Name\tNCBI ID\n" );
        for ( GeneValueObject gene : genes ) {
            strBuff.append( gene.getOfficialSymbol() + "\t" + gene.getOfficialName() + "\t" + gene.getNcbiId() );
            strBuff.append( "\n" );
        }

        return strBuff.toString();
    }

}