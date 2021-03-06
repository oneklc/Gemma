/*
 * The Gemma project
 * 
 * Copyright (c) 2008 University of British Columbia
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import ubic.basecode.util.NetUtils;
import ubic.gemma.model.common.description.LocalFile;
import ubic.gemma.util.NetDatasourceUtil;

/**
 * Download files by FTP.
 * 
 * @author paul
 * @version $Id: FtpFetcher.java,v 1.24 2012/05/27 02:58:35 paul Exp $
 */
public abstract class FtpFetcher extends AbstractFetcher {

    protected FTPClient ftpClient;

    protected NetDatasourceUtil netDataSourceUtil;

    protected boolean avoidDownload = false;

    /**
     * 
     */
    public FtpFetcher() {
        super();
        setNetDataSourceUtil();
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.loader.util.fetcher.Fetcher#fetch(java.lang.String)
     */
    @Override
    public Collection<LocalFile> fetch( String identifier ) {

        String seekFile = formRemoteFilePath( identifier );

        return fetch( identifier, seekFile );
    }

    /**
     * @return the netDataSourceUtil
     */
    public NetDatasourceUtil getNetDataSourceUtil() {
        return this.netDataSourceUtil;
    }

    /**
     * Set to true to avoid download if possible and simply use existing files if they are available. This skips the
     * usual checks for the correct file size compared to the remote one. Not all fetchers support setting this to
     * 'true'.
     * 
     * @param avoidDownload
     */
    public void setAvoidDownload( boolean avoidDownload ) {
        this.avoidDownload = avoidDownload;
    }

    /**
     * @param netDataSourceUtil the netDataSourceUtil to set
     */
    public abstract void setNetDataSourceUtil();

    /**
     * @param outputFileName
     * @param seekFile
     * @return
     */
    protected FutureTask<Boolean> defineTask( final String outputFileName, final String seekFile ) {
        FutureTask<Boolean> future = new FutureTask<Boolean>( new Callable<Boolean>() {
            @Override
            public Boolean call() throws FileNotFoundException, IOException {
                File existing = new File( outputFileName );
                if ( existing.exists() && avoidDownload ) {
                    log.info( "A local file exists, skipping download." );
                    ftpClient.disconnect();
                    return Boolean.TRUE;
                } else if ( existing.exists() && allowUseExisting ) {
                    log.info( "Checking validity of existing local file: " + outputFileName );
                } else {
                    log.info( "Fetching " + seekFile + " to " + outputFileName );
                }
                boolean status = NetUtils.ftpDownloadFile( ftpClient, seekFile, outputFileName, force );
                ftpClient.disconnect();
                return new Boolean( status );
            }
        } );
        return future;
    }

    /**
     * @param future
     * @param expectedSize
     * @param outputFileName
     * @param seekFileName
     * @return
     */
    protected Collection<LocalFile> doTask( FutureTask<Boolean> future, long expectedSize, String seekFileName,
            String outputFileName ) {

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute( future );
        executor.shutdown();

        try {

            File outputFile = new File( outputFileName );
            boolean ok = waitForDownload( future, expectedSize, outputFile );

            if ( !ok ) {
                // cancelled, probably.
                log.info( "Download failed, was it cancelled?" );
                return null;
            } else if ( future.get().booleanValue() ) {
                if ( log.isInfoEnabled() ) log.info( "Done: local file is " + outputFile );
                LocalFile file = fetchedFile( seekFileName, outputFile.getAbsolutePath() );
                Collection<LocalFile> result = new HashSet<LocalFile>();
                result.add( file );
                return result;
            }
        } catch ( ExecutionException e ) {
            throw new RuntimeException( "Couldn't fetch " + seekFileName, e );
        } catch ( InterruptedException e ) {
            log.warn( "Interrupted: Couldn't fetch " + seekFileName, e );
            return null;
        } catch ( CancellationException e ) {
            log.info( "Cancelled" );
            return null;
        }
        throw new RuntimeException( "Couldn't fetch file for " + seekFileName );
    }

    /**
     * @param identifier
     * @param seekFile
     * @return
     */
    protected Collection<LocalFile> fetch( String identifier, String seekFile ) {
        File existingFile = null;
        try {
            File newDir = mkdir( identifier );
            String outputFileName = formLocalFilePath( identifier, newDir );

            existingFile = new File( outputFileName );
            if ( this.avoidDownload || ( existingFile.canRead() && allowUseExisting ) ) {
                // log.info( outputFileName + " already exists." );
            }

            if ( ftpClient == null || !ftpClient.isConnected() ) {
                ftpClient = this.getNetDataSourceUtil().connect( FTP.BINARY_FILE_TYPE );
                assert ftpClient != null; // otherwise should have gotten an exception from connect()
            }

            long expectedSize = getExpectedSize( seekFile );

            FutureTask<Boolean> future = this.defineTask( outputFileName, seekFile );
            Collection<LocalFile> result = this.doTask( future, expectedSize, seekFile, outputFileName );
            return result;
        } catch ( UnknownHostException e ) {
            if ( force || !allowUseExisting || existingFile == null ) throw new RuntimeException( e );

            if ( !avoidDownload ) throw new RuntimeException( e );

            log.warn( "Could not connect to " + this.getNetDataSourceUtil().getHost() + " to check size of " + seekFile
                    + ", using existing file" );
            Collection<LocalFile> fallback = getExistingFile( existingFile, seekFile );
            return fallback;
        } catch ( IOException e ) {

            /*
             * Note: this block can trigger if you cancel.
             */

            if ( force || !allowUseExisting || existingFile == null ) {
                /*
                 * Printing to log here because runtime error does not deliver message when passed through
                 * java.util.concurrent.FutureTask (only throws InterruptedException and ExecutionException)
                 */
                log.error( "Runtime exception thrown: " + e.getMessage() + ". \n Stack trace follows:", e );
                throw new RuntimeException( "Cancelled, or couldn't fetch " + seekFile
                        + ", make sure the file exists on the remote server and permissions are granted.", e );

            }

            if ( Thread.currentThread().isInterrupted() ) {
                throw new CancellationException();
            }

            log.warn( "Cancelled, or couldn't fetch " + seekFile + ", make sure the file exists on the remote server.,"
                    + e + ", using existing file" );
            Collection<LocalFile> fallback = getExistingFile( existingFile, seekFile );
            return fallback;

        } finally {
            try {
                if ( ftpClient != null && ftpClient.isConnected() ) ftpClient.disconnect();
            } catch ( IOException e ) {
                throw new RuntimeException( "Could not disconnect: " + e.getMessage() );
            }
        }
    }

    /**
     * @param seekFile
     * @return
     * @throws IOException
     * @throws SocketException
     */
    protected long getExpectedSize( final String seekFile ) throws IOException, SocketException {
        return NetUtils.ftpFileSize( ftpClient, seekFile );
    }
}
