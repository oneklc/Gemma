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
package ubic.gemma.loader.expression.geo;

/**
 * Can be thrown to indicate an invalid input for GEO loading.
 * 
 * @author pavlidis
 * @version $Id: InvalidAccessionException.java,v 1.2 2006/10/22 17:30:26 paul Exp $
 */
public class InvalidAccessionException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = -9078403276805996725L;

    /**
     * 
     */
    public InvalidAccessionException() {
        super();
    }

    /**
     * @param message
     * @param cause
     */
    public InvalidAccessionException( String message, Throwable cause ) {
        super( message, cause );
    }

    /**
     * @param message
     */
    public InvalidAccessionException( String message ) {
        super( message );
    }

    /**
     * @param cause
     */
    public InvalidAccessionException( Throwable cause ) {
        super( cause );
    }

}