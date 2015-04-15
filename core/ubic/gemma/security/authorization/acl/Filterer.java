/* Copyright 2004, 2005, 2006 Acegi Technology Pty Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ubic.gemma.security.authorization.acl;

import java.util.Iterator;

/**
 * Filter strategy interface. See org.springframework.security.acls.afterinvocation. Filterer, which is not part of the
 * public spring-security API so we have to put it here.
 * 
 * @author Ben Alex
 * @author Paulo Neves
 * @version $Id: Filterer.java,v 1.3 2010/03/11 03:41:30 paul Exp $
 */
public interface Filterer<T> {
    // ~ Methods ================================================================

    /**
     * Gets the filtered collection or array.
     * 
     * @return the filtered collection or array
     */
    public Object getFilteredObject();

    /**
     * Returns an iterator over the filtered collection or array.
     * 
     * @return an Iterator
     */
    public Iterator<T> iterator();

    /**
     * Removes the the given object from the resulting list.
     * 
     * @param object the object to be removed
     */
    public void remove( T object );
}
