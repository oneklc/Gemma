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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

/**
 * @author pavlidis
 * @version $Id: NetUtilsTest.java,v 1.7 2008/02/13 22:16:43 paul Exp $
 */
public class NetUtilsTest extends TestCase {

    private static Log log = LogFactory.getLog( NetUtilsTest.class.getName() );

    final public void testCheckForFile() throws Exception {
        FTPClient f;
        try {
            f = NetUtils.connect( FTP.BINARY_FILE_TYPE, "ftp.ncbi.nlm.nih.gov", "anonymous", "paul@ubic.ca" );
        } catch ( IOException ignore ) {
            log.warn( "Could not connect to ftp.ncbi.nlm.nih.gov, skipping test" );
            return;
        }

        long checkForFile = NetUtils.checkForFile( f, "genomes/README" );
        assertTrue( checkForFile > 0 );

    }

    final public void testDownloadFile() throws Exception {
        FTPClient f;
        try {
            f = NetUtils.connect( FTP.BINARY_FILE_TYPE, "ftp.ncbi.nlm.nih.gov", "anonymous", "paul@ubic.ca" );
        } catch ( IOException ignore ) {
            log.warn( "Could not connect to ftp.ncbi.nlm.nih.gov, skipping test" );
            return;
        }

        File temp = File.createTempFile( "ftptest", "txt" );
        NetUtils.ftpDownloadFile( f, "genomes/README", temp, false );
        assertTrue( temp.canRead() );

    }

    final public void testFtpFileSize() throws Exception {
        FTPClient f;
        try {
            f = NetUtils.connect( FTP.BINARY_FILE_TYPE, "ftp.ncbi.nlm.nih.gov", "anonymous", "paul@ubic.ca" );
        } catch ( IOException ignore ) {
            log.warn( "Could not connect to ftp.ncbi.nlm.nih.gov, skipping test" );
            return;
        }
        long actualValue = NetUtils.ftpFileSize( f, "genomes/Pan_troglodytes/WGS_12Dec2003/WIBR.seq007.fa" );
        long expectedValue = 131446617;
        assertEquals( expectedValue, actualValue, 100000 ); // don't really care if they change the file size....
    }

    final public void testFtpFileSizeDoesntExist() throws Exception {
        FTPClient f;
        try {
            f = NetUtils.connect( FTP.BINARY_FILE_TYPE, "ftp.ncbi.nlm.nih.gov", "anonymous", "paul@ubic.ca" );
        } catch ( IOException ignore ) {
            log.warn( "Could not connect to ftp.ncbi.nlm.nih.gov, skipping test" );
            return;
        }
        try {
            NetUtils.ftpFileSize( f, "genomes/Pan_trogl_____odytes/WGS_12Dec2003/WIBR.seq007.fa" );
            fail( "Should have gotten a FileNotFoundException" );
        } catch ( FileNotFoundException e ) {
            // ok
        }
    }

}
