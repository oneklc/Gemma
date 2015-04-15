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
package ubic.gemma.analysis.expression.diff;

import org.springframework.beans.factory.annotation.Autowired;

import ubic.gemma.analysis.service.ExpressionDataMatrixService;
import ubic.gemma.model.expression.designElement.CompositeSequenceService;

/**
 * Analyzer base class.
 * 
 * @author keshav
 * @version $Id: AbstractAnalyzer.java,v 1.17 2012/12/07 15:46:21 paul Exp $
 */
public abstract class AbstractAnalyzer {

    @Autowired
    protected ExpressionDataMatrixService expressionDataMatrixService = null;

    @Autowired
    protected CompositeSequenceService compositeSequenceService;

    // needed for tests.
    public void setExpressionDataMatrixService( ExpressionDataMatrixService expressionDataMatrixService ) {
        this.expressionDataMatrixService = expressionDataMatrixService;
    }

    /*
     * TODO This used to contain code pertaining to R, it's no longer necessary so this class may eventually be removed.
     */
}
