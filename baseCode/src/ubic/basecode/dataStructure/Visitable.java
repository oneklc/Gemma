/*
 * The baseCode project
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
package ubic.basecode.dataStructure;

/**
 * @author pavlidis
 * @version $Id: Visitable.java,v 1.3 2008/02/13 22:15:47 paul Exp $
 */
public abstract class Visitable {

    private boolean mark;

    public boolean isVisited() {
        return mark;
    }

    public void mark() {
        mark = true;
    }

    public void unMark() {
        mark = false;
    }

}
