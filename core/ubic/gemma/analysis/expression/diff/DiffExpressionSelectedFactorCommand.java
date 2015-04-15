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
package ubic.gemma.analysis.expression.diff;

import java.io.Serializable;

/**
 * A command object with a selected factor and associated experiment.
 * 
 * @author keshav
 * @version $Id: DiffExpressionSelectedFactorCommand.java,v 1.1 2008/11/12 05:35:31 paul Exp $
 */
public class DiffExpressionSelectedFactorCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long eeId = null;

    private Long efId = null;

    public DiffExpressionSelectedFactorCommand() {
    }

    public DiffExpressionSelectedFactorCommand( Long eeId, Long efId ) {
        this.eeId = eeId;
        this.efId = efId;
    }

    public Long getEeId() {
        return eeId;
    }

    public void setEeId( Long eeId ) {
        this.eeId = eeId;
    }

    public Long getEfId() {
        return efId;
    }

    public void setEfId( Long efId ) {
        this.efId = efId;
    }

}
