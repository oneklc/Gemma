package ubic.gemma.web.jetty;

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
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.jetty.Server;

/**
 * Start Jetty from a run configuration
 * 
 * @author keshav
 * @version $Id: Jetty.java,v 1.5 2008/01/24 18:47:08 keshav Exp $
 */
public class Jetty {
    /**
     * Used for logging.
     */
    private static Log log = LogFactory.getLog( Jetty.class );

    /**
     * Construct.
     */
    Jetty() {
        super();
    }

    /**
     * Main function, starts the jetty server.
     * 
     * @param args
     */
    public static void main( String[] args ) {
        Server jettyServer = null;
        try {
            URL jettyConfig = new URL( "file:gemma-web/etc/conf/jetty.xml" );

            log.debug( "jetty config:" + jettyConfig );

            jettyServer = new Server( jettyConfig );
            jettyServer.start();
        } catch ( Exception e ) {
            log.fatal( "Could not start the Jetty server.  The error is: " + e );
            if ( jettyServer != null ) {
                try {
                    jettyServer.stop();
                } catch ( Exception e1 ) {
                    log.fatal( "Unable to stop the jetty server: " + e1 );
                }
            }
            System.exit( 0 );
        }
    }
}
