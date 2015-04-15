/* Copyright (c) 2006-2010 University of British Columbia
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
package ubic.gemma.job.progress;

import ubic.gemma.model.common.auditAndSecurity.JobInfo;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Implementation of the JobProgress interface. Used by the client to add hooks for providing feedback to any users
 * 
 * @author klc
 * @author pavlidis
 * @version $Id: JobProgressImpl.java,v 1.2 2013/03/04 17:56:38 anton Exp $
 */
public class JobProgressImpl implements JobProgress {

    protected Queue<ProgressData> pData;
    protected JobInfo jInfo; // this obj is persisted to DB
    protected String forwardingURL;
    protected String taskId;
    boolean forwardWhenDone = true;

    @Override
    public String toString() {
        return "TaskID: " + jInfo.getTaskId();
    }

    /**
     * The factory create method in ProgressManager is the advised way to create a JobProgress
     * 
     * @param description
     */
    JobProgressImpl(JobInfo info, String description) {
        this.pData = new ConcurrentLinkedQueue<ProgressData>();
        this.pData.add( new ProgressData( info.getTaskId(), 0, description, false ) );
        this.jInfo = info;
        assert info.getTaskId() != null;
        this.taskId = info.getTaskId();
    }

    /**
     * @return Returns the pData, which can be cleaned out. (this isn't ideal..)
     */
//    @Override
//    public Queue<ProgressData> getProgressData() {
//        return pData;
//    }
//
//    @Override
//    public String getUser() {
//        if ( jInfo.getUser() == null ) return null;
//
//        return jInfo.getUser().getUserName();
//    }


    /**
     * Updates the progress job by a complete progressData. Used if more than the percent needs to be updates. Updating
     * the entire datapack causes the underlying dao to update its database entry for desciption only
     * 
     * @param pd
     */
//    @Override
//    public void updateProgress( ProgressData pd ) {
//        this.pData.add( pd );
//        updateDescriptionHistory(pd.getDescription());
//    }


    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.util.progress.JobProgress#updateProgress(java.lang.String)
     */
//    @Override
//    public void updateProgress( String newDescription ) {
//        ProgressData d = new ProgressData( this.taskId, 0, newDescription, false );
//        pData.add( d );
//        updateDescriptionHistory( newDescription );
//    }

    /**
     * Signal completion
     */
//    @Override
//    public void done() {
//        Calendar cal = new GregorianCalendar();
//        jInfo.setEndTime( cal.getTime() );
//        ProgressData d = new ProgressData( this.taskId, 100, "", true );
//        pData.add( d );
//    }

//    @Override
//    public void failed( Throwable cause ) {
//        Calendar cal = new GregorianCalendar();
//        jInfo.setEndTime( cal.getTime() );
//        ProgressData d = new ProgressData( this.taskId, 0, cause.getMessage(), true );
//        d.setFailed( true );
//        d.setDescription( cause.getMessage() );
//        this.pData.add( d );
//    }

//    @Override
//    public JobInfo getJobInfo() {
//        return this.jInfo;
//    }

    /**
     * @return the forwardingURL
     */
//    @Override
//    public String getForwardingURL() {
//        return forwardingURL;
//    }

    /**
     * @param forwardingURL the forwardingURL to set
     */
//    @Override
//    public void setForwardingURL( String forwardingURL ) {
//        this.forwardingURL = forwardingURL;
//    }

    private void updateDescriptionHistory( String message ) {
        if ( this.jInfo.getMessages() == null )
            this.jInfo.setMessages( message );
        else
            this.jInfo.setMessages( this.jInfo.getMessages() + '\n' + message );
    }

//    @Override
//    public String getTaskId() {
//        return this.taskId;
//    }

//    @Override
//    public boolean forwardWhenDone() {
//        return this.forwardWhenDone;
//    }

//    @Override
//    public void setForwardWhenDone( boolean value ) {
//        this.forwardWhenDone = value;
//    }

}
