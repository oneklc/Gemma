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
package ubic.gemma.web.feed;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.social.twitter.api.StatusDetails;
import org.springframework.social.twitter.api.Twitter;
import org.springframework.social.twitter.api.impl.TwitterTemplate;
import org.springframework.stereotype.Component;

import ubic.gemma.analysis.report.WhatsNew;
import ubic.gemma.analysis.report.WhatsNewService;
import ubic.gemma.expression.experiment.service.ExpressionExperimentService;
import ubic.gemma.model.common.auditAndSecurity.Securable;
import ubic.gemma.model.expression.experiment.ExpressionExperiment;
import ubic.gemma.security.SecurityService;
import ubic.gemma.util.ConfigUtils;

import com.ibm.icu.util.Calendar;

/**
 * @author sshao
 * @version $Id: TwitterOutbound.java,v 1.3 2013/05/02 04:48:26 paul Exp $
 */
@Component
public class TwitterOutbound {
    private static Log log = LogFactory.getLog( TwitterOutbound.class.getName() );

    @Autowired
    private WhatsNewService whatsNewService;

    @Autowired
    private ExpressionExperimentService expressionExperimentService;

    @Autowired
    private SecurityService securityService;

    /**
     * Send Tweet.
     */
    @Secured({ "GROUP_AGENT" })
    public void sendDailyFeed() {
        log.debug( "Checking if Twitter is enabled" );
        if ( !ConfigUtils.getBoolean( "gemma.twitter.enabled" ) ) {
            return;
        }

        String feed = generateDailyFeed();
        log.info( "Twitter is enabled. Checking if Twitter feed is empty." );

        if ( !feed.isEmpty() ) {
            log.info( "Sending out tweet: " + feed );
            String consumerKey = ConfigUtils.getString( "gemma.twitter.consumer-key" );
            String consumerSecret = ConfigUtils.getString( "gemma.twitter.consumer-secret" );
            String accessToken = ConfigUtils.getString( "gemma.twitter.access-token" );
            String accessTokenSecret = ConfigUtils.getString( "gemma.twitter.access-token-secret" );

            Twitter twitter = new TwitterTemplate( consumerKey, consumerSecret, accessToken, accessTokenSecret );
            StatusDetails metadata = new StatusDetails();
            metadata.setWrapLinks( true );
            twitter.timelineOperations().updateStatus( feed, metadata );
        }

    }

    /**
     * Generate content for the tweet
     * 
     * @return
     */
    protected String generateDailyFeed() {

        Calendar c = Calendar.getInstance();
        Date date = c.getTime();
        date = DateUtils.addDays( date, -1 );
        WhatsNew whatsNew = whatsNewService.getReport( date );

        Collection<ExpressionExperiment> experiments = new ArrayList<ExpressionExperiment>();
        int updatedExperimentsCount = 0;
        int newExperimentsCount = 0;

        Random rand = new Random();

        // Query for all updated / new expression experiments to store into a experiments collection
        if ( whatsNew != null ) {
            Collection<ExpressionExperiment> updatedExperiments = whatsNew.getUpdatedExpressionExperiments();
            Collection<ExpressionExperiment> newExperiments = whatsNew.getNewExpressionExperiments();
            experiments.addAll( updatedExperiments );
            experiments.addAll( newExperiments );
            updatedExperimentsCount = updatedExperiments.size();
            newExperimentsCount = newExperiments.size();
        }

        ExpressionExperiment experiment = null;

        // Query latest experiments if there are no updated / new experiments
        if ( updatedExperimentsCount == 0 && newExperimentsCount == 0 ) {
            Collection<ExpressionExperiment> latestExperiments = expressionExperimentService.findByUpdatedLimit( 10 );
            Collection<Securable> publicExperiments = securityService.choosePublic( latestExperiments );

            if ( publicExperiments.isEmpty() ) {
                throw new IllegalStateException( "There are no valid experiments to tweet about" );
            }

            experiment = ( ExpressionExperiment ) publicExperiments.toArray()[rand.nextInt( publicExperiments.size() )];
        } else {
            if ( experiments.isEmpty() ) {
                throw new IllegalStateException( "There are no valid experiments to tweet about" );
            }

            experiment = ( ExpressionExperiment ) experiments.toArray()[rand.nextInt( experiments.size() )];
        }

        assert experiment != null;

        String status = statusWithExperiment( StringUtils.abbreviate( experiment.getName(), 90 ),
                updatedExperimentsCount, newExperimentsCount );

        return StringUtils.abbreviate( status, 140 ); // this will look a bit weird, and might chop off the url...but
                                                      // have to ensure.
    }

    /**
     * @param experimentName
     * @param updatedExperimentsCount
     * @param newExperimentsCount
     * @return a status that provides the number of updated and new experiments, a "randomly" chosen experiment and a
     *         link back to Gemma
     */
    private String statusWithExperiment( String experimentName, int updatedExperimentsCount, int newExperimentsCount ) {
        if ( updatedExperimentsCount == 0 && newExperimentsCount == 0 ) {
            return "Experiment of the day: " + experimentName + "; See all latest at www.chibi.ubc.ca/Gemma/rssfeed";
        } else {
            return "Experiment of the day: " + experimentName + "; See all " + updatedExperimentsCount
                    + " updated and " + newExperimentsCount + " new at www.chibi.ubc.ca/Gemma/rssfeed";
        }
    }
}
