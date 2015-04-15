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
package ubic.gemma.loader.util.fetcher;

/**
 * Interface defining a class that downloads archives and unpacks them.
 * 
 * @author pavlidis
 * @version $Id: ArchiveFetcher.java,v 1.2 2007/08/24 01:21:28 paul Exp $
 */
public interface ArchiveFetcher extends Fetcher {

    /**
     * Should the downloaded archive be deleted after unpacking?
     * 
     * @param doDelete
     */
    public void setDeleteAfterUnpack( boolean doDelete );

}
