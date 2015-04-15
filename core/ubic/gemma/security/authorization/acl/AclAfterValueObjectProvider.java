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
package ubic.gemma.security.authorization.acl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.AclService;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.core.Authentication;

import ubic.gemma.model.common.auditAndSecurity.SecureValueObject;
import ubic.gemma.security.SecurityService;
import ubic.gemma.security.SecurityServiceImpl;
import ubic.gemma.security.SecurityUtil;

/**
 * Security check for reading value objects. Also overrides default behaviour by returning null, rather than throwing an
 * access denied exception.
 * <p>
 * As a side effect, it fills in security status information in the value objects to which permission was granted.
 * 
 * @author paul
 * @version $Id: AclAfterValueObjectProvider.java,v 1.6 2013/04/18 22:53:10 paul Exp $
 * @see AclAfterFilterValueObjectCollectionProvider for the same thing but for collections.
 */
public class AclAfterValueObjectProvider extends
        org.springframework.security.acls.afterinvocation.AclEntryAfterInvocationProvider {

    private static Log log = LogFactory.getLog( AclAfterValueObjectProvider.class );

    public AclAfterValueObjectProvider( AclService aclService, List<Permission> requirePermission ) {
        super( aclService, "AFTER_ACL_VALUE_OBJECT_READ", requirePermission );
    }

    @Autowired
    private SecurityService securityService;

    @Override
    public Object decide( Authentication authentication, Object object, Collection<ConfigAttribute> config,
            Object returnedObject ) throws AccessDeniedException {
        try {

            if ( returnedObject == null || !SecureValueObject.class.isAssignableFrom( returnedObject.getClass() ) ) {
                // nothing to do here.
                return returnedObject;
            }

            /*
             * Populate optional fields in the ValueObject. Problem: some of these hit the database. Make this optional.
             */
            SecureValueObject svo = ( SecureValueObject ) returnedObject;

            boolean hasPermission = securityService.hasPermission( svo, requirePermission, authentication );

            if ( !hasPermission ) return false;

            if ( SecurityServiceImpl.isUserLoggedIn() ) {
                Acl acl = securityService.getAcl( svo );
                svo.setIsPublic( !SecurityUtil.isPrivate( acl ) );
                svo.setIsShared( SecurityUtil.isShared( acl ) );
                svo.setUserOwned( securityService.isOwnedByCurrentUser( svo ) );

                if ( svo.getUserOwned() || SecurityServiceImpl.isUserAdmin()
                        || requirePermission.contains( BasePermission.WRITE ) ) {
                    svo.setUserCanWrite( true );
                } else {
                    List<Permission> writePermissions = new ArrayList<Permission>();
                    writePermissions.add( BasePermission.WRITE );
                    svo.setUserCanWrite( securityService.hasPermission( svo, writePermissions, authentication ) );
                }
            }
            return svo;
        } catch ( AccessDeniedException e ) {
            log.warn( e.getMessage() + ": returning null" );
            return null;
        }
    }

}
