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
 * Credit:
 *   If you're nice, you'll leave this bit:
 *
 *   Class by Pierre-Alexandre Losson -- http://www.telio.be/blog
 *   email : plosson@users.sourceforge.net
 */
package ubic.gemma.web.util.upload;

import java.io.File;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;

/**
 * @author Original : plosson on 05-janv.-2006 10:46:33 - Last modified by Author: plosson on Date: 2006/01/05 10:09:38
 * @author pavlidis
 * @version  $Id: MonitoredDiskFileItemFactory.java,v 1.2 2006/03/17 00:18:09 pavlidis Exp $
 */
public class MonitoredDiskFileItemFactory extends DiskFileItemFactory {
    private OutputStreamListener listener = null;

    public MonitoredDiskFileItemFactory( OutputStreamListener listener ) {
        super();
        this.listener = listener;
    }

    public MonitoredDiskFileItemFactory( int sizeThreshold, File repository, OutputStreamListener listener ) {
        super( sizeThreshold, repository );
        this.listener = listener;
    }

    @Override
    public FileItem createItem( String fieldName, String contentType, boolean isFormField, String fileName ) {
        return new MonitoredDiskFileItem( fieldName, contentType, isFormField, fileName, getSizeThreshold(),
                getRepository(), listener );
    }
}
