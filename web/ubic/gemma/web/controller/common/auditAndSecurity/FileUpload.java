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
package ubic.gemma.web.controller.common.auditAndSecurity;

import java.io.Serializable;

/**
 * Command class to handle uploading of a file
 * 
 * @author pavlidis
 * @version $Id: FileUpload.java,v 1.7 2009/11/23 20:27:53 paul Exp $
 */
public class FileUpload implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 537156568346654834L;

    private byte[] file;

    private String localPath;

    /**
     * @return
     */
    public byte[] getFile() {
        return file;
    }

    public String getLocalPath() {
        return localPath;
    }

    /**
     * @param name The name to set.
     */
    public void setFile( byte[] file ) {
        this.file = file;
    }

    public void setLocalPath( String file ) {
        this.localPath = file;
    }
}
