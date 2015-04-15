/*
 * The Gemma project
 * 
 * Copyright (c) 2006 University of British Columbia
 * 
 * Licensed under the Apache License; Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing; software
 * distributed under the License is distributed on an "AS IS" BASIS;
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND; either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package ubic.gemma.web.controller.common.auditAndSecurity;

import java.util.Collection;

import ubic.gemma.model.common.auditAndSecurity.AuditTrail;
import ubic.gemma.model.common.auditAndSecurity.JobInfo;
import ubic.gemma.model.common.auditAndSecurity.User;
import ubic.gemma.model.common.auditAndSecurity.UserRole;

/**
 * Just like a regular user; but has 'new password' and 'confirm password' fields. It can be constructed from a user. To
 * convert to a user object, use the asUser() method.
 * 
 * @author pavlidis
 * @version $Id: UserUpdateCommand.java,v 1.10 2011/02/26 17:11:33 paul Exp $
 */
public class UserUpdateCommand {

    private String oldPassword = null;
    private String newPassword = null;
    private String confirmNewPassword = null;
    private Boolean adminUser = false;
    private Collection<UserRole> roles = null;

    // stored so this can be used to modify a persistent instance.
    private User user;

    public UserUpdateCommand() {
        this.user = User.Factory.newInstance();
    }

    public UserUpdateCommand( User user ) {
        fromUser( user );
    }

    /**
     * @return
     */
    public User asUser() {
        return user;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals( Object obj ) {
        if ( this == obj ) return true;
        if ( obj == null ) return false;
        if ( getClass() != obj.getClass() ) return false;
        UserUpdateCommand other = ( UserUpdateCommand ) obj;
        if ( user == null ) {
            if ( other.user != null ) return false;
        } else if ( !user.equals( other.user ) ) return false;
        return true;
    }

    /**
     * @return
     * @see ubic.gemma.model.common.auditAndSecurity.Contact#getAddress()
     */
    public String getAddress() {
        return this.user.getAddress();
    }

    public Boolean getAdminUser() {
        return this.adminUser;
    }

    /**
     * @return
     * @see ubic.gemma.model.common.Auditable#getAuditTrail()
     */
    public AuditTrail getAuditTrail() {
        return this.user.getAuditTrail();
    }

    /**
     * @return the confirmNewPassword
     */
    public String getConfirmNewPassword() {
        return this.confirmNewPassword;
    }

    /**
     * @return
     * @see ubic.gemma.model.common.Describable#getDescription()
     */
    public String getDescription() {
        return this.user.getDescription();
    }

    /**
     * @return
     * @see ubic.gemma.model.common.auditAndSecurity.Contact#getEmail()
     */
    public String getEmail() {
        return this.user.getEmail();
    }

    /**
     * @return
     * @see ubic.gemma.model.common.auditAndSecurity.User#getEnabled()
     */
    public Boolean getEnabled() {
        return this.user.getEnabled();
    }

    /**
     * @return
     * @see ubic.gemma.model.common.auditAndSecurity.Contact#getFax()
     */
    public String getFax() {
        return this.user.getFax();
    }

    /**
     * @return
     * @see ubic.gemma.model.common.auditAndSecurity.PersonImpl#getFullName()
     */
    public String getFullName() {
        return this.user.getFullName();
    }

    /**
     * @return
     * @see ubic.gemma.model.common.auditAndSecurity.Securable#getId()
     */
    public Long getId() {
        return this.user.getId();
    }

    /**
     * @return
     * @see ubic.gemma.model.common.auditAndSecurity.User#getJob()
     */
    public Collection<JobInfo> getJobs() {
        return this.user.getJobs();
    }

    /**
     * @return
     * @see ubic.gemma.model.common.auditAndSecurity.Person#getLastName()
     */
    public String getLastName() {
        return this.user.getLastName();
    }

    /**
     * @return
     * @see ubic.gemma.model.common.Describable#getName()
     */
    public String getName() {
        return this.user.getName();
    }

    /**
     * @return the newPassword
     */
    public String getNewPassword() {
        return this.newPassword;
    }

    /**
     * @return the oldPassword
     */
    public String getOldPassword() {
        return this.oldPassword;
    }

    /**
     * @return
     * @see ubic.gemma.model.common.auditAndSecurity.User#getPassword()
     */
    public String getPassword() {
        return this.user.getPassword();
    }

    /**
     * @return
     * @see ubic.gemma.model.common.auditAndSecurity.User#getPasswordHint()
     */
    public String getPasswordHint() {
        return this.user.getPasswordHint();
    }

    /**
     * @return
     * @see ubic.gemma.model.common.auditAndSecurity.Contact#getPhone()
     */
    public String getPhone() {
        return this.user.getPhone();
    }

    /**
     * @return
     * @see ubic.gemma.model.common.auditAndSecurity.User#getRoles()
     */
    public Collection<UserRole> getRoles() {
        roles = this.user.getRoles();
        return roles;
    }

    /**
     * @return
     * @see ubic.gemma.model.common.auditAndSecurity.Contact#getTollFreePhone()
     */
    public String getTollFreePhone() {
        return this.user.getTollFreePhone();
    }

    /**
     * @return
     * @see ubic.gemma.model.common.auditAndSecurity.Contact#getURI()
     */
    public String getURL() {
        return this.user.getURL();
    }

    /**
     * @return
     * @see ubic.gemma.model.common.auditAndSecurity.User#getUserName()
     */
    public String getUserName() {
        return this.user.getUserName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( user == null ) ? 0 : user.hashCode() );
        return result;
    }

    /**
     * @param address
     * @see ubic.gemma.model.common.auditAndSecurity.Contact#setAddress(java.lang.String)
     */
    public void setAddress( String address ) {
        this.user.setAddress( address );
    }

    public void setAdminUser( Boolean adminUser ) {
        this.adminUser = adminUser;
    }

    /**
     * @param auditTrail
     * @see ubic.gemma.model.common.Auditable#setAuditTrail(ubic.gemma.model.common.auditAndSecurity.AuditTrail)
     */
    public void setAuditTrail( AuditTrail auditTrail ) {
        this.user.setAuditTrail( auditTrail );
    }

    /**
     * @param confirmNewPassword the confirmNewPassword to set
     */
    public void setConfirmNewPassword( String confirmNewPassword ) {
        this.confirmNewPassword = confirmNewPassword;
    }

    /**
     * @param description
     * @see ubic.gemma.model.common.Describable#setDescription(java.lang.String)
     */
    public void setDescription( String description ) {
        this.user.setDescription( description );
    }

    /**
     * @param email
     * @see ubic.gemma.model.common.auditAndSecurity.Contact#setEmail(java.lang.String)
     */
    public void setEmail( String email ) {
        this.user.setEmail( email );
    }

    /**
     * @param enabled
     * @see ubic.gemma.model.common.auditAndSecurity.User#setEnabled(java.lang.Boolean)
     */
    public void setEnabled( Boolean enabled ) {
        this.user.setEnabled( enabled );
    }

    /**
     * @param fax
     * @see ubic.gemma.model.common.auditAndSecurity.Contact#setFax(java.lang.String)
     */
    public void setFax( String fax ) {
        this.user.setFax( fax );
    }

    /**
     * @param id
     * @see ubic.gemma.model.common.auditAndSecurity.Securable#setId(java.lang.Long)
     */
    public void setId( Long id ) {
        this.user.setId( id );
    }

    /**
     * @param job
     * @see ubic.gemma.model.common.auditAndSecurity.User#setJob(java.util.Collection)
     */
    public void setJobs( Collection<JobInfo> jobs ) {
        this.user.setJobs( jobs );
    }

    /**
     * @param lastName
     * @see ubic.gemma.model.common.auditAndSecurity.Person#setLastName(java.lang.String)
     */
    public void setLastName( String lastName ) {
        this.user.setLastName( lastName );
    }

    /**
     * @param name
     * @see ubic.gemma.model.common.Describable#setName(java.lang.String)
     */
    public void setName( String name ) {
        this.user.setName( name );
    }

    /**
     * @param newPassword the newPassword to set
     */
    public void setNewPassword( String newPassword ) {
        this.newPassword = newPassword;
    }

    /**
     * @param oldPassword the oldPassword to set
     */
    public void setOldPassword( String oldPassword ) {
        this.oldPassword = oldPassword;
    }

    /**
     * @param password
     * @see ubic.gemma.model.common.auditAndSecurity.User#setPassword(java.lang.String)
     */
    public void setPassword( String password ) {
        this.user.setPassword( password );
    }

    /**
     * @param passwordHint
     * @see ubic.gemma.model.common.auditAndSecurity.User#setPasswordHint(java.lang.String)
     */
    public void setPasswordHint( String passwordHint ) {
        this.user.setPasswordHint( passwordHint );
    }

    /**
     * @param phone
     * @see ubic.gemma.model.common.auditAndSecurity.Contact#setPhone(java.lang.String)
     */
    public void setPhone( String phone ) {
        this.user.setPhone( phone );
    }

    /**
     * @param roles
     * @see ubic.gemma.model.common.auditAndSecurity.User#setRoles(java.util.Collection)
     */
    public void setRoles( Collection<UserRole> roles ) {
        this.roles = roles;
        this.user.setRoles( this.roles );
    }

    /**
     * @param tollFreePhone
     * @see ubic.gemma.model.common.auditAndSecurity.Contact#setTollFreePhone(java.lang.String)
     */
    public void setTollFreePhone( String tollFreePhone ) {
        this.user.setTollFreePhone( tollFreePhone );
    }

    /**
     * @param URL
     * @see ubic.gemma.model.common.auditAndSecurity.Contact#setURI(java.lang.String)
     */
    public void setURL( String URL ) {
        this.user.setURL( URL );
    }

    /**
     * @param userName
     * @see ubic.gemma.model.common.auditAndSecurity.User#setUserName(java.lang.String)
     */
    public void setUserName( String userName ) {
        this.user.setUserName( userName );
    }

    /**
     * @return
     * @see ubic.gemma.model.common.DescribableImpl#toString()
     */
    @Override
    public String toString() {
        return this.user.toString();
    }

    /**
     * @param user
     */
    private void fromUser( User newUser ) {
        this.user = newUser;

        this.setUserName( user.getUserName() );
        this.setPassword( user.getPassword() );
        this.setPasswordHint( user.getPasswordHint() );
        this.setEnabled( user.getEnabled() );
        this.setLastName( user.getLastName() );
        this.setName( user.getName() );
        this.setURL( user.getURL() );
        this.setAddress( user.getAddress() );
        this.setPhone( user.getPhone() );
        this.setTollFreePhone( user.getTollFreePhone() );
        this.setEmail( user.getEmail() );
        this.setFax( user.getFax() );
        this.setName( user.getName() );
        this.setDescription( user.getDescription() );
        this.setRoles( user.getRoles() );
        this.setJobs( user.getJobs() );
        this.setAuditTrail( user.getAuditTrail() );

        this.setId( user.getId() );
    }
}
