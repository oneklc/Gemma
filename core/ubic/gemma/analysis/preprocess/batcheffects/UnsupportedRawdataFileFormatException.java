/*
 * The Gemma project
 * 
 * Copyright (c) 2011 University of British Columbia
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
package ubic.gemma.analysis.preprocess.batcheffects;

/**
 * Used to indicate failure was due to the format being unusable, but the files are available.
 * 
 * @author paul
 * @version $Id: UnsupportedRawdataFileFormatException.java,v 1.1 2011/02/17 05:52:25 paul Exp $
 */
public class UnsupportedRawdataFileFormatException extends RuntimeException {

    public UnsupportedRawdataFileFormatException( String string ) {
        super( string );
    }

    private static final long serialVersionUID = 1L;

}
