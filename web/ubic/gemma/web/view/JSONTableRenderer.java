/*
 * The Gemma project
 * 
 * Copyright (c) 2007 University of British Columbia
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
package ubic.gemma.web.view;

import java.util.List;

import com.sdicons.json.mapper.JSONMapper;
import com.sdicons.json.mapper.MapperException;
import com.sdicons.json.model.JSONValue;

/**
 * @spring.bean id="JSONTableRenderer"
 * @author pavlidis
 * @version $Id: JSONTableRenderer.java,v 1.2 2007/05/02 23:59:44 paul Exp $
 */
public class JSONTableRenderer {

    public String render( List<Object> tableObjects ) {
        try {
            StringBuilder b = new StringBuilder();
            for ( Object o : tableObjects ) {
                JSONValue v;
                v = JSONMapper.toJSON( o );
                b.append( v.render( true ) );
            }
            return b.toString();
        } catch ( MapperException e ) {
            throw new RuntimeException( e );
        }
    }

}
