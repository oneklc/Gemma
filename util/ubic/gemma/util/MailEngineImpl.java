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
package ubic.gemma.util;

import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.VelocityException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.ui.velocity.VelocityEngineUtils;

/**
 * @author pavlidis
 * @author Matt Raible
 * @version $Id: MailEngineImpl.java,v 1.2 2013/01/25 03:10:11 anton Exp $
 */
@Component
public class MailEngineImpl implements MailEngine {
    protected static final Log log = LogFactory.getLog( MailEngineImpl.class );

    @Autowired private MailSender mailSender;
    @Autowired private VelocityEngine velocityEngine;

    /**
     * @param msg
     * @param templateName
     * @param model
     */
    @Override
    public void sendMessage( SimpleMailMessage msg, String templateName, Map<String, Object> model ) {
        String result = null;

        try {
            result = VelocityEngineUtils.mergeTemplateIntoString( velocityEngine, templateName, model );
        } catch ( VelocityException e ) {
            e.printStackTrace();
        }

        msg.setText( result );
        send( msg );
    }

    /**
     * @param msg
     */
    @Override
    public void send( SimpleMailMessage msg ) {
        try {
            mailSender.send( msg );
        } catch ( MailException ex ) {
            // log it and go on
            log.error( ex.getMessage() );
            log.debug( ex, ex );
        }
    }

    /**
     * Convenience method for sending messages with attachments.
     * 
     * @param emailAddresses
     * @param resource to be attached
     * @param bodyText
     * @param subject
     * @param attachmentName
     * @throws MessagingException
     * @author Ben Gill
     */
    @Override
    public void sendMessage( String[] emailAddresses, ClassPathResource resource, String bodyText, String subject,
            String attachmentName ) throws MessagingException {
        MimeMessage message = ( ( JavaMailSenderImpl ) mailSender ).createMimeMessage();

        // use the true flag to indicate you need a multipart message
        MimeMessageHelper helper = new MimeMessageHelper( message, true );

        helper.setTo( emailAddresses );
        helper.setText( bodyText );
        helper.setSubject( subject );

        helper.addAttachment( attachmentName, resource );

        ( ( JavaMailSenderImpl ) mailSender ).send( message );
    }

    /**
     * Sends a message to the gemma administrator as defined in the Gemma.properties file
     * 
     * @param bodyText
     * @param subject
     */
    @Override
    public void sendAdminMessage( String bodyText, String subject ) {

        if ( ( bodyText == null ) && ( subject == null ) ) {
            log.warn( "Not sending empty email, both subject and body are null" );
            return;
        }

        log.info( "Sending email notification to administrator regarding: " + subject );
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo( ConfigUtils.getAdminEmailAddress() );
        msg.setFrom( ConfigUtils.getAdminEmailAddress() );
        msg.setSubject( subject );
        msg.setText( bodyText );
        this.send( msg );
    }
}
