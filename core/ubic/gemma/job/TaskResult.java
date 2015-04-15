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
package ubic.gemma.job;

import java.io.Serializable;

/**
 * This class describes the result of long-running task.
 * 
 * @author keshav
 * @version $Id: TaskResult.java,v 1.6 2013/02/18 18:36:42 anton Exp $
 */
public class TaskResult implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     *
     */
    private Object answer;

    /**
     * Set if failed.
     */
    private Throwable exception;

    /**
     * The task id
     */
    private String taskID;

    public TaskResult( String taskId ) {
        assert taskId != null;
        this.taskID = taskId;
    }

    public TaskResult( TaskCommand command, Object answer ) {
        assert command != null;
        assert command.getTaskId() != null;
        this.taskID = command.getTaskId();
        this.answer = answer;
    }

    public Object getAnswer() {
        return answer;
    }

    /**
     * @return the exception
     */
    public Throwable getException() {
        return exception;
    }

    public String getTaskId() {
        return taskID;
    }

    /**
     * @param exception the exception to set
     */
    public void setException( Throwable exception ) {
        this.exception = exception;
    }

}
