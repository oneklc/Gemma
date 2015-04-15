/*
 * The Gemma project
 * 
 * Copyright (c) 2012 University of British Columbia
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
package ubic.gemma.analysis.expression.diff;

import java.util.Collection;

import ubic.gemma.model.analysis.expression.diff.DifferentialExpressionAnalysis;
import ubic.gemma.model.analysis.expression.diff.ExpressionAnalysisResultSet;

/**
 * Service methods to do database-related work for differential expression analysis
 * 
 * @author Paul
 * @version $Id: DifferentialExpressionAnalysisHelperService.java,v 1.25 2013/04/18 22:52:59 paul Exp $
 */
public interface DifferentialExpressionAnalysisHelperService {

    /**
     * @param entity
     * @return
     */
    public DifferentialExpressionAnalysis persistStub( DifferentialExpressionAnalysis entity );

    /**
     * @param entity
     * @param resultSets
     */
    public void addResults( DifferentialExpressionAnalysis entity, Collection<ExpressionAnalysisResultSet> resultSets );

    /**
     * Create a result set. The expectation is this would be a stub.S
     * 
     * @param rs
     * @return
     */
    public ExpressionAnalysisResultSet create( ExpressionAnalysisResultSet rs );

}
