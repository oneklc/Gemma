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
package ubic.gemma.loader.expression.geo.fetcher;

import ubic.gemma.util.ConfigUtils;

/**
 * Retrieve GEO GDS files from the NCBI FTP server.
 * 
 * @author pavlidis
 * @version $Id: DatasetFetcher.java,v 1.8 2008/11/17 01:20:01 paul Exp $
 */
public class DatasetFetcher extends GeoFetcher {

    /**
     * @throws ConfigurationException
     */
    public DatasetFetcher() {
        super();

    }

    /*
     * Pointing to directory: ftp://ftp.ncbi.nih.gov/pub/geo/DATA/SOFT/GDS/
     * @see ubic.gemma.loader.util.fetcher.AbstractFetcher#formRemoteFilePath(java.lang.String)
     */
    @Override
    protected String formRemoteFilePath( String identifier ) {
        return remoteBaseDir + identifier + SOFT_GZ;
    }

    /*
     * (non-Javadoc)
     * @see ubic.gemma.loader.util.fetcher.AbstractFetcher#initConfig()
     */
    @Override
    protected void initConfig() {
        this.localBasePath = ConfigUtils.getString( "geo.local.datafile.basepath" );
        this.remoteBaseDir = ConfigUtils.getString( "geo.remote.datasetDir" );
    }

}
