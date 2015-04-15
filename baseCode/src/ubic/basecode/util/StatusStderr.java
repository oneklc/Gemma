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
package ubic.basecode.util;

/**
 * Prints status info to stderr
 * 
 * @author Paul Pavlidis
 * @version $Id: StatusStderr.java,v 1.2 2006/03/29 23:27:28 paul Exp $
 */
public class StatusStderr implements StatusViewer {

    public StatusStderr() {
    }

    public void showStatus( String s ) {
        if ( s.equals( "" ) ) return;
        System.err.println( s );
    }

    public void showError( String s ) {
        if ( s.equals( "" ) ) return;
        System.err.println( "Error:" + s );
    }

    public void clear() {
        // don't need to do anything.
    }

    /*
     * (non-Javadoc)
     * 
     * @see basecode.util.StatusViewer#setError(java.lang.Throwable)
     */
    public void showError( Throwable e ) {
        e.printStackTrace();
    }

    /*
     * (non-Javadoc)
     * 
     * @see basecode.util.StatusViewer#setError(java.lang.String, java.lang.Throwable)
     */
    public void showError( String message, Throwable e ) {
        this.showError( message );
        e.printStackTrace();
    }

}