/*
 * The Gemma project
 * 
 * Copyright (c) 2007 University of British Columbia
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

import ubic.gemma.expression.experiment.service.ExpressionExperimentService;
import ubic.gemma.loader.entrez.pubmed.ExpressionExperimentBibRefFinder;
import ubic.gemma.model.common.description.BibliographicReference;
import ubic.gemma.model.expression.experiment.ExpressionExperiment;
import ubic.gemma.persistence.PersisterHelper;
import ubic.gemma.util.AbstractCLIContextCLI;

import java.util.Collection;

/**
 * Update the primary publication for experiments.
 * 
 * @author paul
 * @version $Id: ExpressionExperimentPrimaryPubCli.java,v 1.8 2013/02/18 18:36:44 anton Exp $
 */
public class ExpressionExperimentPrimaryPubCli extends AbstractCLIContextCLI {

    public static void main( String[] args ) {
        ExpressionExperimentPrimaryPubCli p = new ExpressionExperimentPrimaryPubCli();
        try {
            Exception ex = p.doWork( args );
            if ( ex != null ) {
                ex.printStackTrace();
            }
        } catch ( Exception e ) {
            throw new RuntimeException( e );
        }
    }

    @Override
    protected void buildOptions() {
    }

    @Override
    protected Exception doWork( String[] args ) {
        Exception err = processCommandLine( "Expression experiment bibref finder ", args );
        if ( err != null ) return err;
        ExpressionExperimentService ees = this.getBean( ExpressionExperimentService.class );
        PersisterHelper ph = this.getBean( PersisterHelper.class );
        Collection<ExpressionExperiment> experiments = ees.loadAll();

        ExpressionExperimentBibRefFinder finder = new ExpressionExperimentBibRefFinder();
        for ( ExpressionExperiment experiment : experiments ) {
            if ( experiment.getPrimaryPublication() != null ) continue;
            experiment = ees.thawLite( experiment );
            BibliographicReference ref = finder.locatePrimaryReference( experiment );
            if ( ref == null ) {
                System.err.println( "No ref for " + experiment );
                continue;
            }

            System.err.println( "Found " + ref + " for " + experiment );
            ref = ( BibliographicReference ) ph.persist( ref );
            experiment.setPrimaryPublication( ref );
            ees.update( experiment );

        }

        return null;
    }

    @Override
    protected void processOptions() {
        super.processOptions();
    }
}
