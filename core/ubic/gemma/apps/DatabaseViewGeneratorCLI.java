/*
 * The Gemma project
 * 
 * Copyright (c) 2009 University of British Columbia
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
package ubic.gemma.apps;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import ubic.gemma.analysis.report.DatabaseViewGenerator;
import ubic.gemma.util.AbstractCLIContextCLI;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Simple driver of DatabaseViewGenerator. Developed to support NIF and other external data consumers.
 * 
 * @author paul
 * @version $Id: DatabaseViewGeneratorCLI.java,v 1.13 2013/02/18 18:36:44 anton Exp $
 * @see DatabaseViewGenerator.
 */
public class DatabaseViewGeneratorCLI extends AbstractCLIContextCLI {

    /**
     * @param args
     */
    public static void main( String[] args ) {
        DatabaseViewGeneratorCLI o = new DatabaseViewGeneratorCLI();
        o.doWork( args );
    }

    private boolean generateDiffExpressionSummary = false;
    private boolean generateDatasetSummary = false;
    private boolean generateTissueSummary = false;

    private int limit = 0;

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.util.AbstractSpringAwareCLI#getShortDesc()
     */
    @Override
    public String getShortDesc() {
        return "Generate views of the database in flat files";
    }

    @SuppressWarnings("static-access")
    @Override
    protected void buildOptions() {
        super.buildStandardOptions();

        Option datasetSummary = OptionBuilder
                .withDescription( "Will generate a zip file containing a summary of all accessible datasets in gemma" )
                .withLongOpt( "dataset" ).create( 'd' );

        Option datasetTissueSummary = OptionBuilder
                .withDescription(
                        "Will generate a zip file containing a summary of all the tissues in accesable datasets" )
                .withLongOpt( "tissue" ).create( 't' );

        Option diffExpSummary = OptionBuilder
                .withDescription(
                        "Will generate a zip file containing a summary of all the differential expressed genes in accesable datasets" )
                .withLongOpt( "diffexpression" ).create( 'x' );

        Option limitOpt = OptionBuilder.hasArg().withArgName( "Limit number of datasets" )
                .withDescription( "will impose a limit on how many datasets to process" ).withLongOpt( "limit" )
                .create( 'l' );

        addOption( datasetSummary );
        addOption( datasetTissueSummary );
        addOption( diffExpSummary );
        addOption( limitOpt );

    }

    @Override
    protected Exception doWork( String[] args ) {
        Exception err = super.processCommandLine( "DatabaseViewGeneratorCLI", args );
        if ( err != null ) return err;

        DatabaseViewGenerator v = getBean( DatabaseViewGenerator.class );

        try {
            if ( generateDatasetSummary ) v.generateDatasetView( limit );
            if ( generateTissueSummary ) v.generateDatasetTissueView( limit );
            if ( generateDiffExpressionSummary ) v.generateDifferentialExpressionView( limit );

        } catch ( FileNotFoundException e ) {
            throw new RuntimeException( e );
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }
        return null;
    }

    @Override
    protected void processOptions() {

        if ( this.hasOption( 'x' ) ) {
            this.generateDiffExpressionSummary = true;
        }

        if ( this.hasOption( 'd' ) ) {
            this.generateDatasetSummary = true;
        }

        if ( this.hasOption( 't' ) ) {
            this.generateTissueSummary = true;
        }

        if ( this.hasOption( 'l' ) ) {
            try {
                this.limit = Integer.parseInt( this.getOptionValue( 'l' ) );
            } catch ( NumberFormatException nfe ) {
                log.warn( "Unable to process limit parameter. Processing all availiable experiments." );
                this.limit = 0;
            }
        }

        super.processOptions();

    }

}
