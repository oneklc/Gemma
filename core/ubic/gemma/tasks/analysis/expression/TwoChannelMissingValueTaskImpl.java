package ubic.gemma.tasks.analysis.expression;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ubic.gemma.analysis.preprocess.TwoChannelMissingValues;
import ubic.gemma.job.TaskResult;
import ubic.gemma.model.expression.bioAssayData.RawExpressionDataVector;
import ubic.gemma.model.expression.experiment.ExpressionExperiment;

import java.util.Collection;

/**
 * Space task for computing two channel missing values.
 *
 * @author paul
 * @version $Id: TwoChannelMissingValueTaskImpl.java,v 1.5 2013/01/25 03:10:08 anton Exp $
 */
@Component
@Scope("prototype")
public class TwoChannelMissingValueTaskImpl implements TwoChannelMissingValueTask {

    @Autowired private TwoChannelMissingValues twoChannelMissingValues;

    private TwoChannelMissingValueTaskCommand command;

    @Override
    public void setCommand(TwoChannelMissingValueTaskCommand command) {
        this.command = command;
    }

    @Override
    public TaskResult execute() {

        ExpressionExperiment ee = command.getExpressionExperiment();

        Collection<RawExpressionDataVector> missingValueVectors = twoChannelMissingValues.computeMissingValues( ee,
                command.getS2n(), command.getExtraMissingValueIndicators() );

        TaskResult result = new TaskResult( command, missingValueVectors.size() );

        return result;
    }

}
