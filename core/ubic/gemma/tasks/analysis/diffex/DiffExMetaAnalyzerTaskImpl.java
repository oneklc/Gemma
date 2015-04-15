package ubic.gemma.tasks.analysis.diffex;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ubic.gemma.analysis.expression.diff.DiffExMetaAnalyzerService;
import ubic.gemma.analysis.expression.diff.GeneDiffExMetaAnalysisHelperService;
import ubic.gemma.job.TaskResult;
import ubic.gemma.model.analysis.expression.diff.GeneDifferentialExpressionMetaAnalysis;
import ubic.gemma.model.analysis.expression.diff.GeneDifferentialExpressionMetaAnalysisDetailValueObject;

/**
 * A differential expression meta-analysis space task
 *
 * @author frances
 * @version $Id: DiffExMetaAnalyzerTaskImpl.java,v 1.6 2013/01/25 03:10:11 anton Exp $
 */
@Component
@Scope("prototype")
public class DiffExMetaAnalyzerTaskImpl implements DiffExMetaAnalyzerTask {

    @Autowired
    private DiffExMetaAnalyzerService diffExMetaAnalyzerService;
    @Autowired private GeneDiffExMetaAnalysisHelperService geneDiffExMetaAnalysisHelperService;

    private DiffExMetaAnalyzerTaskCommand command;

    @Override
    public void setCommand(DiffExMetaAnalyzerTaskCommand command) {
        this.command = command;
    }

    @Override
    public TaskResult execute() {
        GeneDifferentialExpressionMetaAnalysis metaAnalysis = this.diffExMetaAnalyzerService.analyze( command
                .getAnalysisResultSetIds() );

        if (metaAnalysis != null) {
        	metaAnalysis.setName( command.getName() );
        	metaAnalysis.setDescription( command.getDescription() );

	        if ( command.isPersist() ) {
	            metaAnalysis = this.diffExMetaAnalyzerService.persist( metaAnalysis );
	        }
        }

        GeneDifferentialExpressionMetaAnalysisDetailValueObject metaAnalysisVO = ( metaAnalysis == null ? null
                : this.geneDiffExMetaAnalysisHelperService.convertToValueObject( metaAnalysis ) );

        return new TaskResult( command, metaAnalysisVO );
    }
}
