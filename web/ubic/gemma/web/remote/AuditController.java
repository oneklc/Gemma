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
package ubic.gemma.web.remote;

import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ubic.gemma.expression.experiment.service.ExpressionExperimentService;
import ubic.gemma.genome.gene.service.GeneService;
import ubic.gemma.model.common.Auditable;
import ubic.gemma.model.common.auditAndSecurity.AuditEvent;
import ubic.gemma.model.common.auditAndSecurity.AuditEventService;
import ubic.gemma.model.common.auditAndSecurity.AuditEventValueObject;
import ubic.gemma.model.common.auditAndSecurity.AuditTrailService;
import ubic.gemma.model.common.auditAndSecurity.eventType.AuditEventType;
import ubic.gemma.model.expression.arrayDesign.ArrayDesign;
import ubic.gemma.model.expression.arrayDesign.ArrayDesignService;
import ubic.gemma.model.expression.experiment.ExpressionExperiment;
import ubic.gemma.model.genome.Gene;

/**
 * This is required soley for exposing auditables to remote services would try to marshall the abstract class Auditable.
 * 
 * @author pavlidis
 * @version $Id: AuditController.java,v 1.17 2012/06/18 22:58:30 paul Exp $
 */
@Component
public class AuditController {

    private static Log log = LogFactory.getLog( AuditController.class.getName() );

    @Autowired
    private ArrayDesignService arrayDesignService;

    @Autowired
    private AuditEventService auditEventService;

    @Autowired
    private AuditTrailService auditTrailService;

    @Autowired
    private ExpressionExperimentService expressionExperimentService;

    @Autowired
    private GeneService geneService;

    /**
     * AJAX
     * 
     * @param e
     * @param auditEventType
     * @param comment
     * @param detail
     */
    @SuppressWarnings("unchecked")
    public void addAuditEvent( EntityDelegator e, String auditEventType, String comment, String detail ) {
        Auditable entity = getAuditable( e );
        if ( entity == null ) {
            log.warn( "Couldn't find Auditable represented by " + e );
            return;
        }

        if ( auditEventType.equals( "CommentedEvent" ) ) {
            auditTrailService.addComment( entity, comment, detail );
        } else if ( auditEventType.equals( "TroubleStatusFlagEvent" ) ) {
            auditTrailService.addTroubleFlag( entity, comment, detail );
        } else if ( auditEventType.equals( "OKStatusFlagEvent" ) ) {
            auditTrailService.addOkFlag( entity, comment, detail );
        } else if ( auditEventType.equals( "ValidatedFlagEvent" ) ) {
            auditTrailService.addValidatedFlag( entity, comment, detail );
        } else {
            Class<?> clazz;
            try {
                clazz = Class.forName( "ubic.gemma.model.common.auditAndSecurity.eventType." + auditEventType );
            } catch ( ClassNotFoundException e1 ) {
                throw new RuntimeException( "Unknown event type: " + auditEventType );
            }
            auditTrailService.addUpdateEvent( entity, ( Class<? extends AuditEventType> ) clazz, comment, detail );
        }

    }

    /**
     * AJAX
     * 
     * @param e
     * @return
     */
    public Auditable getAuditable( EntityDelegator e ) {
        if ( e == null || e.getId() == null ) return null;
        if ( e.getClassDelegatingFor() == null ) return null;

        Class<?> clazz;
        Auditable result = null;
        try {
            clazz = Class.forName( e.getClassDelegatingFor() );
        } catch ( ClassNotFoundException e1 ) {
            throw new RuntimeException( e1 );
        }
        if ( ExpressionExperiment.class.isAssignableFrom( clazz ) ) {
            result = expressionExperimentService.load( e.getId() );
        } else if ( ArrayDesign.class.isAssignableFrom( clazz ) ) {
            result = arrayDesignService.load( e.getId() );
        } else if ( Gene.class.isAssignableFrom( clazz ) ) {
            result = geneService.load( e.getId() );
        } else {
            log.warn( "We don't support that class yet, sorry" );
            return null;
        }

        if ( result == null ) {
            log.warn( "Entity with id = " + e.getId() + " not found" );
        }
        return result;
    }

    /**
     * AJAX
     * 
     * @param e
     * @return
     */
    public Collection<AuditEventValueObject> getEvents( EntityDelegator e ) {
        Collection<AuditEventValueObject> result = new HashSet<AuditEventValueObject>();

        Auditable entity = getAuditable( e );

        if ( entity == null ) {
            return result;
        }
        assert entity.getAuditTrail().getId() != null;

        Collection<AuditEvent> events = auditEventService.getEvents( entity );
        for ( AuditEvent ev : events ) {
            if ( ev == null ) continue;
            result.add( new AuditEventValueObject( ev ) );
        }

        return result;
    }
}
