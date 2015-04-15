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
package ubic.gemma.security;

import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.Sid;

import ubic.gemma.util.AuthorityConstants;

/**
 * Database-independent methods for ACLs
 * 
 * @author Paul
 * @version $Id: SecurityUtil.java,v 1.5 2013/03/28 23:59:07 paul Exp $
 */
public class SecurityUtil {

    /**
     * @param acl
     * @return
     */
    public static boolean isPrivate( Acl acl ) {

        /*
         * If the given Acl has anonymous permissions on it, then it can't be private.
         */
        for ( AccessControlEntry ace : acl.getEntries() ) {

            if ( !ace.getPermission().equals( BasePermission.READ ) ) continue;

            Sid sid = ace.getSid();
            if ( sid instanceof GrantedAuthoritySid ) {
                String grantedAuthority = ( ( GrantedAuthoritySid ) sid ).getGrantedAuthority();
                if ( grantedAuthority.equals( AuthorityConstants.IS_AUTHENTICATED_ANONYMOUSLY ) && ace.isGranting() ) {
                    return false;
                }
            }
        }

        /*
         * Even if the object is not private, it's parent might be and we might inherit that. Recursion happens here.
         */
        Acl parentAcl = acl.getParentAcl();
        if ( parentAcl != null && acl.isEntriesInheriting() ) {
            return isPrivate( parentAcl );
        }

        /*
         * We didn't find a granted authority on IS_AUTHENTICATED_ANONYMOUSLY
         */
        return true;

    }

    /**
     * @param acl
     * @return true if the ACL grants READ authority to at least one group that is not admin or agent.
     */
    public static boolean isShared( Acl acl ) {
        for ( AccessControlEntry ace : acl.getEntries() ) {

            if ( !ace.getPermission().equals( BasePermission.READ ) ) continue;

            Sid sid = ace.getSid();
            if ( sid instanceof GrantedAuthoritySid ) {
                String grantedAuthority = ( ( GrantedAuthoritySid ) sid ).getGrantedAuthority();
                if ( grantedAuthority.startsWith( "GROUP_" ) && ace.isGranting() ) {

                    if ( grantedAuthority.equals( AuthorityConstants.AGENT_GROUP_AUTHORITY )
                            || grantedAuthority.equals( AuthorityConstants.ADMIN_GROUP_AUTHORITY ) ) {
                        continue;
                    }
                    return true;

                }
            }
        }

        /*
         * Even if the object is not private, it's parent might be and we might inherit that. Recursion happens here.
         */
        Acl parentAcl = acl.getParentAcl();
        if ( parentAcl != null && acl.isEntriesInheriting() ) {
            return isShared( parentAcl );
        }

        /*
         * We didn't find a granted authority for any group.
         */
        return false;
    }
}
