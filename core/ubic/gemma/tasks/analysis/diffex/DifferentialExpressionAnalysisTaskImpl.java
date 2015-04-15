/*
 * The Gemma project
 * 
 * Copyright (c) 2013 University of British Columbia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package ubic.gemma.tasks.analysis.diffex;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ubic.gemma.analysis.expression.diff.AnalysisSelectionAndExecutionService;
import ubic.gemma.analysis.expression.diff.DifferentialExpressionAnalysisConfig;
import ubic.gemma.analysis.expression.diff.DifferentialExpressionAnalyzerService;
import ubic.gemma.analysis.expression.diff.DifferentialExpressionAnalyzerServiceImpl;
import ubic.gemma.analysis.preprocess.batcheffects.BatchInfoPopulationServiceImpl;
import ubic.gemma.expression.experiment.service.ExpressionExperimentService;
import ubic.gemma.job.TaskResult;
import ubic.gemma.model.analysis.expression.diff.DifferentialExpressionAnalysis;
import ubic.gemma.model.analysis.expression.diff.DifferentialExpressionAnalysisService;
import ubic.gemma.model.expression.experiment.ExperimentalFactor;
import ubic.gemma.model.expression.experiment.ExpressionExperiment;

import java.util.Collection;
import java.util.HashSet;

/**
 * A differential expression analysis spaces task
 * 
 * @author keshav
 * @version $Id: DifferentialExpressionAnalysisTaskImpl.java,v 1.23 2013/03/28 23:59:05 paul Exp $
 */
@Component
@Scope("prototype")
public class DifferentialExpressionAnalysisTaskImpl implements DifferentialExpressionAnalysisTask {

    private static Log log = LogFactory.getLog( DifferentialExpressionAnalysisTask.class.getName() );

    @Autowired
    private DifferentialExpressionAnalyzerService differentialExpressionAnalyzerService;
    @Autowired
    private ExpressionExperimentService expressionExperimentService;
    @Autowired
    private AnalysisSelectionAndExecutionService analysisSelectionAndExecutionService;
    @Autowired
    private DifferentialExpressionAnalysisService differentialExpressionAnalysisService;

    private DifferentialExpressionAnalysisTaskCommand command;

    @Override
    public void setCommand( DifferentialExpressionAnalysisTaskCommand command ) {
        this.command = command;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.gemma.grid.javaspaces.task.diff.DifferentialExpressionAnalysisTask#execute(ubic.gemma.grid
     * .javaspaces.task .diff. SpacesDifferentialExpressionAnalysisCommand)
     */
    @Override
    public TaskResult execute() {

        if ( command instanceof DifferentialExpressionAnalysisRemoveTaskCommand ) {
            DifferentialExpressionAnalysis toRemove = ( ( DifferentialExpressionAnalysisRemoveTaskCommand ) command )
                    .getToRemove();

            if ( toRemove == null ) {
                throw new IllegalArgumentException( "Analysis to remove must not be null" );
            }

            log.info( "Removing analysis ..." );
            this.differentialExpressionAnalysisService.delete( toRemove );

            return new TaskResult( command, true );
        }

        Collection<DifferentialExpressionAnalysis> results = doAnalysis();

        Collection<DifferentialExpressionAnalysis> minimalResults = new HashSet<DifferentialExpressionAnalysis>();
        for ( DifferentialExpressionAnalysis r : results ) {

            /* Don't send the full analysis to the space. Instead, create a minimal result. */
            DifferentialExpressionAnalysis minimalResult = DifferentialExpressionAnalysis.Factory.newInstance();
            minimalResult.setName( r.getName() );
            minimalResult.setDescription( r.getDescription() );
            minimalResult.setAuditTrail( r.getAuditTrail() );
            minimalResults.add( minimalResult );
        }

        TaskResult result = new TaskResult( command, minimalResults );

        return result;
    }

    /**
     * @param command
     * @return
     */
    private Collection<DifferentialExpressionAnalysis> doAnalysis() {
        ExpressionExperiment ee = command.getExpressionExperiment();

        if ( command.getToRedo() != null ) {
            if ( command.isUpdateStatsOnly() ) {
                log.info( "Refreshing stats" );
                differentialExpressionAnalyzerService.updateSummaries( command.getToRedo() );
                Collection<DifferentialExpressionAnalysis> result = new HashSet<DifferentialExpressionAnalysis>();
                result.add( command.getToRedo() );
                return result;
            } else {
                log.info( "Redoing analysis" );
                ee = expressionExperimentService.thawLite( ee );
                return differentialExpressionAnalyzerService.redoAnalysis( ee, command.getToRedo(),
                        command.getQvalueThreshold() );
            }
        }

        ee = expressionExperimentService.thawLite( ee );

        Collection<DifferentialExpressionAnalysis> diffAnalyses = differentialExpressionAnalysisService
                .getAnalyses( ee );

        if ( !diffAnalyses.isEmpty() ) {
            log.info( "This experiment has some existing analyses; if they overlap with the new analysis they will be deleted after the run." );
        }

        Collection<DifferentialExpressionAnalysis> results;

        Collection<ExperimentalFactor> factors = command.getFactors();
        DifferentialExpressionAnalyzerServiceImpl.AnalysisType analyzer = analysisSelectionAndExecutionService
                .determineAnalysis( ee, factors, command.getSubsetFactor(), command.isIncludeInteractions() );

        if ( analyzer == null ) {
            throw new IllegalStateException( "Data set cannot be analyzed" );
        }

        assert factors != null;
        DifferentialExpressionAnalysisConfig config = new DifferentialExpressionAnalysisConfig();

        config.setAnalysisType( analyzer );
        config.setFactorsToInclude( factors );
        config.setQvalueThreshold( command.getQvalueThreshold() );
        config.setSubsetFactor( command.getSubsetFactor() );

        /*
         * We only support interactions when there are exactly two factors.
         */
        if ( command.isIncludeInteractions() && factors.size() == 2 ) {

            /*
             * We should not include 'batch' in an interaction. But I don't want to enforce that here.
             */
            for ( ExperimentalFactor ef : factors ) {
                if ( BatchInfoPopulationServiceImpl.isBatchFactor( ef ) ) {
                    log.warn( "Batch is included in the interaction!" );
                }
            }

            config.addInteractionToInclude( factors );
            config.setAnalysisType( DifferentialExpressionAnalyzerServiceImpl.AnalysisType.TWA );
        } else {
            config.setInteractionsToInclude( new HashSet<Collection<ExperimentalFactor>>() );
        }

        results = differentialExpressionAnalyzerService.runDifferentialExpressionAnalyses( ee, config );

        return results;
    }
}
