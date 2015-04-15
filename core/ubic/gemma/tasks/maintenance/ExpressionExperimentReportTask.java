package ubic.gemma.tasks.maintenance;

import ubic.gemma.job.TaskResult;
import ubic.gemma.tasks.Task;

/**
 * Handles delegation of report generation (to the space, or run locally)
 * 
 * @author klc
 * @version $Id: ExpressionExperimentReportTask.java,v 1.2 2013/01/25 03:10:15 anton Exp $
 */

public interface ExpressionExperimentReportTask extends Task<TaskResult, ExpressionExperimentReportTaskCommand> {
}


