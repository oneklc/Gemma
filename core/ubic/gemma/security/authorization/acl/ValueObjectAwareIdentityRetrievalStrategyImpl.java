/*
 * The Gemma project
 * 
 * Copyright (c) 2013 University of British Columbia
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

package ubic.gemma.security.authorization.acl;

import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.ObjectIdentityRetrievalStrategy;

import ubic.gemma.model.common.auditAndSecurity.SecureValueObject;

/**
 * Customized to know how to deal with SecureValueObject, makes it easier to share code in SecurityService.
 * 
 * @author Paul
 * @version $Id: ValueObjectAwareIdentityRetrievalStrategyImpl.java,v 1.1 2013/03/28 23:59:03 paul Exp $
 */
public class ValueObjectAwareIdentityRetrievalStrategyImpl implements ObjectIdentityRetrievalStrategy {

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.security.acls.model.ObjectIdentityRetrievalStrategy#getObjectIdentity(java.lang.Object)
     */
    @Override
    public ObjectIdentity getObjectIdentity( Object domainObject ) {
        if ( SecureValueObject.class.isAssignableFrom( domainObject.getClass() ) ) {
            SecureValueObject svo = ( SecureValueObject ) domainObject;
            return new ObjectIdentityImpl( svo.getSecurableClass(), svo.getId() );
        } else {
            return new ObjectIdentityImpl( domainObject );
        }

    }
}
