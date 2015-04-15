/*
 * The Gemma project
 * 
 * Copyright (c) 2012 University of British Columbia
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
package ubic.gemma.loader.expression.geo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ubic.basecode.dataStructure.matrix.DenseDoubleMatrix;
import ubic.basecode.dataStructure.matrix.DoubleMatrix;
import ubic.basecode.io.ByteArrayConverter;
import ubic.basecode.math.DescriptiveWithMissing;
import ubic.gemma.analysis.expression.AnalysisUtilService;
import ubic.gemma.analysis.preprocess.PreprocessingException;
import ubic.gemma.analysis.preprocess.PreprocessorService;
import ubic.gemma.datastructure.matrix.ExpressionDataDoubleMatrix;
import ubic.gemma.expression.experiment.service.ExpressionExperimentService;
import ubic.gemma.loader.expression.AffyPowerToolsProbesetSummarize;
import ubic.gemma.loader.expression.geo.fetcher.RawDataFetcher;
import ubic.gemma.loader.expression.geo.service.GeoService;
import ubic.gemma.model.common.auditAndSecurity.AuditTrailService;
import ubic.gemma.model.common.auditAndSecurity.eventType.AuditEventType;
import ubic.gemma.model.common.auditAndSecurity.eventType.DataAddedEvent;
import ubic.gemma.model.common.auditAndSecurity.eventType.DataReplacedEvent;
import ubic.gemma.model.common.auditAndSecurity.eventType.ExpressionExperimentPlatformSwitchEvent;
import ubic.gemma.model.common.description.LocalFile;
import ubic.gemma.model.common.quantitationtype.GeneralType;
import ubic.gemma.model.common.quantitationtype.PrimitiveType;
import ubic.gemma.model.common.quantitationtype.QuantitationType;
import ubic.gemma.model.common.quantitationtype.ScaleType;
import ubic.gemma.model.common.quantitationtype.StandardQuantitationType;
import ubic.gemma.model.expression.arrayDesign.ArrayDesign;
import ubic.gemma.model.expression.arrayDesign.ArrayDesignService;
import ubic.gemma.model.expression.bioAssay.BioAssay;
import ubic.gemma.model.expression.bioAssay.BioAssayService;
import ubic.gemma.model.expression.bioAssayData.BioAssayDimension;
import ubic.gemma.model.expression.bioAssayData.BioAssayDimensionService;
import ubic.gemma.model.expression.bioAssayData.RawExpressionDataVector;
import ubic.gemma.model.expression.biomaterial.BioMaterial;
import ubic.gemma.model.expression.designElement.CompositeSequence;
import ubic.gemma.model.expression.experiment.ExpressionExperiment;
import ubic.gemma.model.genome.Taxon;
import cern.colt.list.DoubleArrayList;

/**
 * Update the data associated with an experiment. Primary designed for filling in data that we can't or don't want to
 * get from GEO. For loading experiments from flat files, see SimpleExpressionDataLoaderService
 * 
 * @author paul
 * @version $Id: DataUpdater.java,v 1.28 2013/05/01 23:08:14 paul Exp $
 */
@Component
public class DataUpdater {

    private static Log log = LogFactory.getLog( DataUpdater.class );

    @Autowired
    private AnalysisUtilService analysisUtilService;

    @Autowired
    private ArrayDesignService arrayDesignService;

    @Autowired
    private BioAssayDimensionService assayDimensionService;

    @Autowired
    private AuditTrailService auditTrailService;

    @Autowired
    private BioAssayService bioAssayService;

    @Autowired
    private ExpressionExperimentService experimentService;

    @Autowired
    private GeoService geoService;

    @Autowired
    private PreprocessorService preprocessorService;

    public ExpressionExperiment addAffyExonArrayData( ExpressionExperiment ee ) {
        Collection<ArrayDesign> ads = experimentService.getArrayDesignsUsed( ee );
        if ( ads.size() > 1 ) {
            throw new IllegalArgumentException( "Can't handle experiments with more than one platform" );
        }
        return addAffyExonArrayData( ee, ads.iterator().next() );
    }

    /**
     * Replaces any existing "preferred" dat.
     * 
     * @param ee
     * @param ad
     */
    public ExpressionExperiment addAffyExonArrayData( ExpressionExperiment ee, ArrayDesign ad ) {

        RawDataFetcher f = new RawDataFetcher();
        Collection<LocalFile> files = f.fetch( ee.getAccession().getAccession() );

        if ( files.isEmpty() ) {
            throw new RuntimeException( "Data was apparently not available" );
        }
        ad = arrayDesignService.thaw( ad );
        ee = experimentService.thawLite( ee );

        Taxon primaryTaxon = ad.getPrimaryTaxon();

        ArrayDesign targetPlatform = prepareTargetPlatformForExonArrays( primaryTaxon );

        assert !targetPlatform.getCompositeSequences().isEmpty();

        AffyPowerToolsProbesetSummarize apt = new AffyPowerToolsProbesetSummarize();

        Collection<RawExpressionDataVector> vectors = apt.processExonArrayData( ee, targetPlatform, files );

        if ( vectors.isEmpty() ) {
            throw new IllegalStateException( "No vectors were returned for " + ee );
        }

        ee = experimentService.replaceVectors( ee, targetPlatform, vectors );

        if ( !targetPlatform.equals( ad ) ) {
            AuditEventType eventType = ExpressionExperimentPlatformSwitchEvent.Factory.newInstance();
            auditTrailService.addUpdateEvent( ee, eventType,
                    "Switched in course of updating vectors using AffyPowerTools (from " + ad.getShortName() + " to "
                            + targetPlatform.getShortName() + ")" );
        }

        audit( ee, "Data vector computation from CEL files using AffyPowerTools for " + targetPlatform, true );

        postprocess( ee );
        return ee;
    }

    /**
     * Use when we want to avoid downloading the CEL files etc. For example if GEO doesn't have them and we ran
     * apt-probeset-summarize ourselves.
     * 
     * @param ee
     * @param pathToAptOutputFile
     * @throws IOException
     * @throws FileNotFoundException
     */
    public void addAffyExonArrayData( ExpressionExperiment ee, String pathToAptOutputFile )
            throws FileNotFoundException, IOException {

        Collection<ArrayDesign> ads = experimentService.getArrayDesignsUsed( ee );
        if ( ads.size() > 1 ) {
            throw new IllegalArgumentException( "Can't handle experiments with more than one platform" );
        }

        ArrayDesign ad = ads.iterator().next();

        ad = arrayDesignService.thaw( ad );
        ee = experimentService.thawLite( ee );

        Taxon primaryTaxon = ad.getPrimaryTaxon();

        ArrayDesign targetPlatform = prepareTargetPlatformForExonArrays( primaryTaxon );
        AffyPowerToolsProbesetSummarize apt = new AffyPowerToolsProbesetSummarize();

        Collection<RawExpressionDataVector> vectors = apt
                .processExonArrayData( ee, pathToAptOutputFile, targetPlatform );

        if ( vectors.isEmpty() ) {
            throw new IllegalStateException( "No vectors were returned for " + ee );
        }

        experimentService.replaceVectors( ee, targetPlatform, vectors );

        if ( !targetPlatform.equals( ad ) ) {
            AuditEventType eventType = ExpressionExperimentPlatformSwitchEvent.Factory.newInstance();
            auditTrailService.addUpdateEvent( ee, eventType,
                    "Switched in course of updating vectors using AffyPowerTools (from " + ad.getShortName() + " to "
                            + targetPlatform.getShortName() + ")" );
        }

        audit( ee, "Data vector input from APT output file " + pathToAptOutputFile + " on " + targetPlatform, true );

        postprocess( ee );

    }

    /**
     * Replaces data.
     * 
     * @param ee
     * @param targetArrayDesign
     * @param countMatrix Representing 'raw' counts (added after rpkm, if provided), which is treated as the 'preferred'
     *        data. If this is provided, all the other data will be removed.
     * @param rpkmMatrix Representing per-gene normalized data, optional.
     * @param readLength
     * @param isPairedReads
     * @param allowMissingSamples if true, samples that are missing data will be deleted from the experiment.
     */
    public void addCountData( ExpressionExperiment ee, ArrayDesign targetArrayDesign,
            DoubleMatrix<String, String> countMatrix, DoubleMatrix<String, String> rpkmMatrix, Integer readLength,
            Boolean isPairedReads, boolean allowMissingSamples ) {

        if ( countMatrix == null )
            throw new IllegalArgumentException( "You must provide count matrix (rpkm is optional)" );

        targetArrayDesign = arrayDesignService.thaw( targetArrayDesign );

        ee = experimentService.thawLite( ee );

        ee = dealWithMissingSamples( ee, countMatrix, allowMissingSamples );

        /*
         * Treat this as the preferred data, so we have to do it first.
         */
        DoubleMatrix<CompositeSequence, BioMaterial> properCountMatrix = matchElementsToRowNames( targetArrayDesign,
                countMatrix );
        matchBioMaterialsToColNames( ee, countMatrix, properCountMatrix );

        assert !properCountMatrix.getColNames().isEmpty();
        assert !properCountMatrix.getRowNames().isEmpty();

        QuantitationType countqt = makeCountQt();
        ExpressionDataDoubleMatrix countEEMatrix = new ExpressionDataDoubleMatrix( ee, countqt, properCountMatrix );

        ee = replaceData( ee, targetArrayDesign, countEEMatrix );

        addTotalCountInformation( ee, countEEMatrix, readLength, isPairedReads );

        if ( rpkmMatrix != null ) {

            DoubleMatrix<CompositeSequence, BioMaterial> properRPKMMatrix = matchElementsToRowNames( targetArrayDesign,
                    rpkmMatrix );
            matchBioMaterialsToColNames( ee, rpkmMatrix, properRPKMMatrix );

            assert !properRPKMMatrix.getColNames().isEmpty();
            assert !properRPKMMatrix.getRowNames().isEmpty();

            QuantitationType rpkmqt = makeRPKMQt();
            ExpressionDataDoubleMatrix rpkmEEMatrix = new ExpressionDataDoubleMatrix( ee, rpkmqt, properRPKMMatrix );

            ee = addData( ee, targetArrayDesign, rpkmEEMatrix );
        }

        assert !experimentService.getProcessedDataVectors( ee ).isEmpty();
    }

    /**
     * @param ee
     * @param countMatrix
     * @param allowMissingSamples
     * @return
     */
    private ExpressionExperiment dealWithMissingSamples( ExpressionExperiment ee,
            DoubleMatrix<String, String> countMatrix, boolean allowMissingSamples ) {
        if ( ee.getBioAssays().size() > countMatrix.columns() ) {
            if ( allowMissingSamples ) {

                Map<String, BioMaterial> bmMap = makeBioMaterialNameMap( ee );
                List<BioAssay> usedBioAssays = new ArrayList<BioAssay>();
                List<BioMaterial> newColNames = new ArrayList<BioMaterial>();
                for ( String colName : countMatrix.getColNames() ) {
                    BioMaterial bm = bmMap.get( colName );
                    if ( bm == null ) {
                        throw new IllegalStateException( "Could not match a column name to a biomaterial: " + colName );
                    }
                    newColNames.add( bm );
                    usedBioAssays.addAll( bm.getBioAssaysUsedIn() );
                }

                assert usedBioAssays.size() == countMatrix.columns();

                Collection<BioAssay> toRemove = new HashSet<BioAssay>();
                for ( BioAssay ba : ee.getBioAssays() ) {
                    if ( !usedBioAssays.contains( ba ) ) {
                        toRemove.add( ba );
                        log.info( "Will remove unused bioassay from experiment: " + ba );
                    }
                }

                if ( !toRemove.isEmpty() ) {
                    ee.getBioAssays().removeAll( toRemove );
                    experimentService.update( ee );
                    ee = experimentService.load( ee.getId() );
                    ee = experimentService.thawLite( ee );

                    if ( ee.getBioAssays().size() != countMatrix.columns() ) {
                        throw new IllegalStateException( "Something went wrong, could not remove unused samples" );
                    }

                    // this should already be done...
                    for ( BioAssay b : toRemove ) {
                        bioAssayService.remove( b );
                    }

                }

            } else {
                throw new IllegalArgumentException( "Too little data provided: The experiment has "
                        + ee.getBioAssays().size() + " samples but the data has " + countMatrix.columns() + " columns." );
            }
        } else if ( ee.getBioAssays().size() < countMatrix.columns() ) {
            throw new IllegalArgumentException( "Extra data provided: The experiment has " + ee.getBioAssays().size()
                    + " samples but the data has " + countMatrix.columns() + " columns." );
        }
        return ee;
    }

    /**
     * Add an additional data (with associated quantitation type) to the selected experiment. Will do postprocessing if
     * the data quantitationtype is 'preferred', but if there is already a preferred quantitation type, an error will be
     * thrown.
     * 
     * @param ee
     * @param targetPlatform
     * @param data
     */
    public ExpressionExperiment addData( ExpressionExperiment ee, ArrayDesign targetPlatform,
            ExpressionDataDoubleMatrix data ) {
        Collection<ArrayDesign> ads = experimentService.getArrayDesignsUsed( ee );
        if ( ads.size() > 1 ) {
            throw new IllegalArgumentException( "Can only replace data for an experiment that uses one platform; "
                    + "you must switch/merge first and then provide appropriate replacement data." );
        }

        if ( data.rows() == 0 ) {
            throw new IllegalArgumentException( "Data had no rows" );
        }

        ArrayDesign originalArrayDesign = ads.iterator().next();
        if ( !targetPlatform.equals( originalArrayDesign ) ) {
            throw new IllegalArgumentException(
                    "You can only add data for a platform that already is used for the experiment: "
                            + originalArrayDesign + " != targeted " + targetPlatform );
        }

        Collection<QuantitationType> qts = data.getQuantitationTypes();

        if ( qts.size() > 1 ) {
            throw new IllegalArgumentException( "Only support a single quantitation type" );
        }

        if ( qts.isEmpty() ) {
            throw new IllegalArgumentException( "Please supply a quantitation type with the data" );
        }

        QuantitationType qt = qts.iterator().next();

        if ( qt.getIsPreferred() ) {
            for ( QuantitationType existingQt : ee.getQuantitationTypes() ) {
                if ( existingQt.getIsPreferred() ) {
                    throw new IllegalArgumentException(
                            "You cannot add 'preferred' data to an experiment that already has it. You should first make the existing data non-preferred." );
                }
            }
        }

        Collection<RawExpressionDataVector> vectors = makeNewVectors( ee, targetPlatform, data, qt );

        if ( vectors.isEmpty() ) {
            throw new IllegalStateException( "no vectors!" );
        }

        ee = experimentService.addVectors( ee, originalArrayDesign, vectors );

        audit( ee, "Data vectors added for " + targetPlatform + ", " + qt, false );

        // debug code.
        for ( BioAssay ba : ee.getBioAssays() ) {
            assert ba.getArrayDesignUsed().equals( targetPlatform );
        }

        experimentService.update( ee );

        if ( qt.getIsPreferred() ) {
            postprocess( ee );
        }

        return ee;
    }

    /**
     * @param ee
     * @param qt
     * @return
     */
    public int deleteData( ExpressionExperiment ee, QuantitationType qt ) {
        return this.experimentService.removeData( ee, qt );
    }

    /**
     * @param ee
     */
    private void postprocess( ExpressionExperiment ee ) {
        // several transactions
        try {
            preprocessorService.process( ee );
        } catch ( PreprocessingException e ) {
            log.error( "Error during postprocessing", e );
        }
    }

    /**
     * Replace the data associated with the experiment (or add it if there is none). These data become the 'preferred'
     * quantitation type.
     * <p>
     * Similar to AffyPowerToolsProbesetSummarize.convertDesignElementDataVectors and code in
     * SimpleExpressionDataLoaderService.
     * 
     * @param ee the experiment to be modified
     * @param targetPlatform the platform for the new data
     * @param data the data to be used
     */
    public ExpressionExperiment replaceData( ExpressionExperiment ee, ArrayDesign targetPlatform,
            ExpressionDataDoubleMatrix data ) {

        Collection<ArrayDesign> ads = experimentService.getArrayDesignsUsed( ee );
        if ( ads.size() > 1 ) {
            throw new IllegalArgumentException( "Can only replace data for an experiment that uses one platform; "
                    + "you must switch/merge first and then provide appropriate replacement data." );
        }

        if ( data.rows() == 0 ) {
            throw new IllegalArgumentException( "Data had no rows" );
        }

        ArrayDesign originalArrayDesign = ads.iterator().next();

        Collection<QuantitationType> qts = data.getQuantitationTypes();

        if ( qts.size() > 1 ) {
            throw new IllegalArgumentException( "Only support a single quantitation type" );
        }

        if ( qts.isEmpty() ) {
            throw new IllegalArgumentException( "Please supply a quantitation type with the data" );
        }

        QuantitationType qt = qts.iterator().next();
        qt.setIsPreferred( true );

        Collection<RawExpressionDataVector> vectors = makeNewVectors( ee, targetPlatform, data, qt );

        if ( vectors.isEmpty() ) {
            throw new IllegalStateException( "no vectors!" );
        }

        /*
         * delete all analyses, etc.
         */
        analysisUtilService.deleteOldAnalyses( ee );

        ee = experimentService.replaceVectors( ee, targetPlatform, vectors );

        // audit if we switched platforms.
        if ( !targetPlatform.equals( originalArrayDesign ) ) {
            AuditEventType eventType = ExpressionExperimentPlatformSwitchEvent.Factory.newInstance();
            auditTrailService.addUpdateEvent(
                    ee,
                    eventType,
                    "Switched in course of updating vectors using data input (from "
                            + originalArrayDesign.getShortName() + " to " + targetPlatform.getShortName() + ")" );
        }

        audit( ee, "Data vector replacement for " + targetPlatform, true );

        postprocess( ee );

        // debug code.
        for ( BioAssay ba : ee.getBioAssays() ) {
            assert ba.getArrayDesignUsed().equals( targetPlatform );
        }

        experimentService.update( ee );

        return ee;
    }

    /**
     * @param ee
     * @param countEEMatrix
     * @param readLength
     * @param isPairedReads
     */
    private void addTotalCountInformation( ExpressionExperiment ee, ExpressionDataDoubleMatrix countEEMatrix,
            Integer readLength, Boolean isPairedReads ) {
        for ( BioAssay ba : ee.getBioAssays() ) {
            Double[] col = countEEMatrix.getColumn( ba );
            double librarySize = DescriptiveWithMissing.sum( new DoubleArrayList( ArrayUtils.toPrimitive( col ) ) );

            log.info( ba + " total library size=" + librarySize );

            ba.setDescription( ba.getDescription() + " totalCounts=" + Math.floor( librarySize ) );
            ba.setSequenceReadLength( readLength );
            ba.setSequencePairedReads( isPairedReads );
            ba.setSequenceReadCount( ( int ) Math.floor( librarySize ) );

            bioAssayService.update( ba );

        }
    }

    /**
     * @param ee
     * @param note
     * @param replace if true, use a DataReplacedEvent; otherwise DataAddedEvent.
     */
    private void audit( ExpressionExperiment ee, String note, boolean replace ) {
        AuditEventType eventType = null;

        if ( replace ) {
            eventType = DataReplacedEvent.Factory.newInstance();
        } else {
            eventType = DataAddedEvent.Factory.newInstance();
        }

        auditTrailService.addUpdateEvent( ee, eventType, note );
    }

    /**
     * @return
     */
    private QuantitationType makeCountQt() {
        QuantitationType countqt = makeQt( true );
        countqt.setName( "Counts" );
        countqt.setType( StandardQuantitationType.COUNT );
        countqt.setScale( ScaleType.COUNT );
        countqt.setDescription( "Read counts for gene model" );
        countqt.setIsBackgroundSubtracted( false );
        countqt.setIsNormalized( false );
        return countqt;
    }

    /**
     * @param ee
     * @param targetPlatform
     * @param data
     * @param qt
     * @return
     */
    private Collection<RawExpressionDataVector> makeNewVectors( ExpressionExperiment ee, ArrayDesign targetPlatform,
            ExpressionDataDoubleMatrix data, QuantitationType qt ) {
        ByteArrayConverter bArrayConverter = new ByteArrayConverter();

        Collection<RawExpressionDataVector> vectors = new HashSet<RawExpressionDataVector>();

        BioAssayDimension bioAssayDimension = data.getBestBioAssayDimension();

        assert bioAssayDimension != null;
        assert !bioAssayDimension.getBioAssays().isEmpty();

        bioAssayDimension = assayDimensionService.findOrCreate( bioAssayDimension );

        assert !bioAssayDimension.getBioAssays().isEmpty();

        for ( int i = 0; i < data.rows(); i++ ) {
            byte[] bdata = bArrayConverter.doubleArrayToBytes( data.getRow( i ) );

            RawExpressionDataVector vector = RawExpressionDataVector.Factory.newInstance();
            vector.setData( bdata );

            CompositeSequence cs = data.getRowElement( i ).getDesignElement();

            if ( cs == null ) {
                continue;
            }

            if ( !cs.getArrayDesign().equals( targetPlatform ) ) {
                throw new IllegalArgumentException( "Input data must use the target platform (was: "
                        + cs.getArrayDesign() + ", expected: " + targetPlatform );
            }

            vector.setDesignElement( cs );
            vector.setQuantitationType( qt );
            vector.setExpressionExperiment( ee );
            vector.setBioAssayDimension( bioAssayDimension );
            vectors.add( vector );

        }
        return vectors;
    }

    /**
     * @param preferred
     * @return
     */
    private QuantitationType makeQt( boolean preferred ) {
        QuantitationType qt = QuantitationType.Factory.newInstance();
        qt.setGeneralType( GeneralType.QUANTITATIVE );
        qt.setScale( ScaleType.LINEAR );
        qt.setIsBackground( false );
        qt.setIsRatio( false );
        qt.setIsBackgroundSubtracted( true );
        qt.setIsNormalized( true );
        qt.setIsMaskedPreferred( true );
        qt.setIsPreferred( preferred );
        qt.setIsBatchCorrected( false );
        qt.setType( StandardQuantitationType.AMOUNT );
        qt.setRepresentation( PrimitiveType.DOUBLE );
        return qt;
    }

    /**
     * @return
     */
    private QuantitationType makeRPKMQt() {
        QuantitationType rpkmqt = makeQt( false );
        rpkmqt.setIsRatio( false );
        rpkmqt.setName( "RPKM" );
        rpkmqt.setDescription( "Reads (or fragments) per kb of gene model per million reads" );
        rpkmqt.setIsBackgroundSubtracted( false );
        rpkmqt.setIsNormalized( true );
        return rpkmqt;
    }

    /**
     * @param ee
     * @param rawMatrix
     * @param finalMatrix
     * @param allowMissingSamples set to true if you know some samples in the experiment lack data in the input.
     */
    private void matchBioMaterialsToColNames( ExpressionExperiment ee, DoubleMatrix<String, String> rawMatrix,
            DoubleMatrix<CompositeSequence, BioMaterial> finalMatrix ) {
        // match column names to the samples. can have any order so be careful.
        List<String> colNames = rawMatrix.getColNames();
        Map<String, BioMaterial> bmMap = makeBioMaterialNameMap( ee );

        List<BioAssay> usedBioAssays = new ArrayList<BioAssay>();
        List<BioMaterial> newColNames = new ArrayList<BioMaterial>();
        for ( String colName : colNames ) {
            BioMaterial bm = bmMap.get( colName );
            if ( bm == null ) {
                throw new IllegalStateException( "Could not match a column name to a biomaterial: " + colName
                        + "; Available keys were:\n" + StringUtils.join( bmMap.keySet(), "\n" ) );
            }
            newColNames.add( bm );
            usedBioAssays.addAll( bm.getBioAssaysUsedIn() );
        }

        finalMatrix.setColumnNames( newColNames );
    }

    /**
     * @param ee
     * @return map of strings to biomaterials, where the keys are likely column names used in the input files.
     */
    private Map<String, BioMaterial> makeBioMaterialNameMap( ExpressionExperiment ee ) {
        Map<String, BioMaterial> bmMap = new HashMap<String, BioMaterial>();

        Collection<BioAssay> bioAssays = ee.getBioAssays();
        for ( BioAssay bioAssay : bioAssays ) {

            BioMaterial bm = bioAssay.getSampleUsed();
            if ( bmMap.containsKey( bm.getName() ) ) {
                // this might not actually be an error - but just in case...
                throw new IllegalStateException( "Two biomaterials from the same experiment with the same name " );
            }

            bmMap.put( bm.getName(), bm );

            if ( bioAssay.getAccession() != null ) {
                // e.g. GSM123455
                String accession = bioAssay.getAccession().getAccession();
                if ( bmMap.containsKey( accession ) ) {
                    throw new IllegalStateException( "Two bioassays with the same accession" );
                }
                bmMap.put( accession, bm );
            }

            // I think it will always be null, if it is from GEO anyway.
            if ( bm.getExternalAccession() != null ) {
                if ( bmMap.containsKey( bm.getExternalAccession().getAccession() ) ) {
                    throw new IllegalStateException( "Two biomaterials with the same accession" );
                }
                bmMap.put( bm.getExternalAccession().getAccession(), bm );
            }

        }
        return bmMap;
    }

    /**
     * @param targetArrayDesign
     * @param rawMatrix
     * @return matrix with row names fixed up. ColumnNames still need to be done.
     */
    private DoubleMatrix<CompositeSequence, BioMaterial> matchElementsToRowNames( ArrayDesign targetArrayDesign,
            DoubleMatrix<String, String> rawMatrix ) {

        Map<String, CompositeSequence> pnmap = new HashMap<String, CompositeSequence>();

        for ( CompositeSequence cs : targetArrayDesign.getCompositeSequences() ) {
            pnmap.put( cs.getName(), cs );
        }
        int failedMatch = 0;
        int timesWarned = 0;
        List<CompositeSequence> newRowNames = new ArrayList<CompositeSequence>();
        List<String> usableRowNames = new ArrayList<String>();
        assert !rawMatrix.getRowNames().isEmpty();
        for ( String rowName : rawMatrix.getRowNames() ) {
            CompositeSequence cs = pnmap.get( rowName );
            if ( cs == null ) {
                /*
                 * This might be okay, but not too much
                 */
                failedMatch++;
                if ( timesWarned < 20 ) {
                    log.warn( "No platform match to element named: " + rowName );
                }
                if ( timesWarned == 20 ) {
                    log.warn( "Further warnings suppressed" );
                }
                timesWarned++;
                continue;
            }
            usableRowNames.add( rowName );
            newRowNames.add( cs );
        }

        if ( usableRowNames.isEmpty() || newRowNames.isEmpty() ) {
            throw new IllegalArgumentException( "None of the rows matched the given platform elements" );
        }
        DoubleMatrix<CompositeSequence, BioMaterial> finalMatrix;
        if ( failedMatch > 0 ) {
            log.warn( failedMatch + "/" + rawMatrix.rows()
                    + " elements could not be matched to the platform. Lines that did not match will be ignored." );
            DoubleMatrix<String, String> useableData = rawMatrix.subsetRows( usableRowNames );
            finalMatrix = new DenseDoubleMatrix<CompositeSequence, BioMaterial>( useableData.getRawMatrix() );

        } else {
            finalMatrix = new DenseDoubleMatrix<CompositeSequence, BioMaterial>( rawMatrix.getRawMatrix() );

        }
        finalMatrix.setRowNames( newRowNames );
        if ( finalMatrix.getRowNames().isEmpty() ) {
            throw new IllegalStateException( "Failed to get row names" );
        }

        return finalMatrix; // not actually final.
    }

    /**
     * determine the target array design. We use filtered versions of these platforms from GEO.
     * 
     * @param primaryTaxon
     * @return
     */
    private ArrayDesign prepareTargetPlatformForExonArrays( Taxon primaryTaxon ) {

        /*
         * Unfortunately there is no way to get around hard-coding this, in some way; there are specific platforms we
         * need to use.
         */
        String targetPlatformAcc = "";
        if ( primaryTaxon.getCommonName().equals( "mouse" ) ) {
            targetPlatformAcc = "GPL6096";
        } else if ( primaryTaxon.getCommonName().equals( "human" ) ) {
            targetPlatformAcc = "GPL5175"; // [HuEx-1_0-st] Affymetrix Human Exon 1.0 ST Array [transcript (gene)
                                           // version]
        } else if ( primaryTaxon.getCommonName().equals( "rat" ) ) {
            targetPlatformAcc = "GPL6543";
        } else {
            throw new IllegalArgumentException( "Exon arrays only supported for mouse, human and rat" );
        }

        ArrayDesign targetPlatform = arrayDesignService.findByShortName( targetPlatformAcc );

        if ( targetPlatform != null ) {
            targetPlatform = arrayDesignService.thaw( targetPlatform );

            if ( targetPlatform.getCompositeSequences().isEmpty() ) {
                /*
                 * Ok, we have to 'reload it' and add the compositeSequences.
                 */
                geoService.addElements( targetPlatform );
            }
        } else {
            log.warn( "The target platform " + targetPlatformAcc + " could not be found in the system. Loading it ..." );

            Collection<?> r = geoService.fetchAndLoad( targetPlatformAcc, true, false, false, false );

            if ( r.isEmpty() ) throw new IllegalStateException( "Loading target platform failed." );

            targetPlatform = ( ArrayDesign ) r.iterator().next();

        }

        return targetPlatform;
    }

}
