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
 * Intended use is to display 'status' information or other messages to users in a non-disruptive fashion (though the
 * actual use is up to the implementer). Copyright (c) 2004 University of British Columbia
 * 
 * @author Paul Pavlidis
 * @version $Id: StatusViewer.java,v 1.3 2008/02/13 22:15:47 paul Exp $
 */
public interface StatusViewer {

    /**
     * Print the status to the location appropriate for this application.
     * 
     * @param s
     */
    public abstract void showStatus( String s );

    /**
     * Print an error status messge.
     * 
     * @param s
     */
    public abstract void showError( String s );

    /**
     * @param e
     */
    public abstract void showError( Throwable e );

    /**
     * @param e
     */
    public abstract void showError( String message, Throwable e );

    /**
     * Clear the status dislay. Implementers that do not write to GUI elements probably don't need to do anything.
     */
    public abstract void clear();
}