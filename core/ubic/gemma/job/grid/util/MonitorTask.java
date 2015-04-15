/*
 * The Gemma project
 * 
 * Copyright (c) 2010 University of British Columbia
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
package ubic.gemma.job.grid.util;

import ubic.gemma.job.TaskResult;
import ubic.gemma.tasks.Task;

/**
 * @author paul
 * @version $Id: MonitorTask.java,v 1.2 2013/01/25 03:10:01 anton Exp $
 */
@Deprecated
public interface MonitorTask extends Task<TaskResult, MonitorTaskCommand> {
}
