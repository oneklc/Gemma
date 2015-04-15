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
package ubic.gemma.web.taglib.arrayDesign;

import java.util.Collection;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import ubic.gemma.model.expression.arrayDesign.ArrayDesign;

/**
 * For display of information about array designs that subsume or are subsumed.
 * 
 * @jsp.tag name="arrayDesignGrouping" body-content="empty"
 * @author pavlidis
 * @version $Id: ArrayDesignGroupingTag.java,v 1.3 2009/11/23 20:28:39 paul Exp $
 */
public class ArrayDesignGroupingTag extends TagSupport {

    private static final long serialVersionUID = -5646614364631502667L;
    private ArrayDesign subsumer;
    private Collection<ArrayDesign> subsumees;

    @Override
    public int doEndTag() {
        return EVAL_PAGE;
    }

    /**
     * @param subsumer
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setSubsumer( ArrayDesign subsumer ) {
        this.subsumer = subsumer;
    }

    /**
     * @param subsumeees
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setSubsumees( Collection<ArrayDesign> subsumees ) {
        this.subsumees = subsumees;
    }

    @Override
    public int doStartTag() throws JspException {

        StringBuilder buf = new StringBuilder();

        if ( this.subsumer == null && ( this.subsumees == null || this.subsumees.size() == 0 ) ) {
            buf.append( "[None]" );
        } else if ( this.subsumer != null && this.subsumees != null && this.subsumees.size() > 0 ) {
            buf.append( "[Invalid parameters passed]" ); // don't throw an exception, just warn
        } else if ( this.subsumer != null ) {
            buf.append( arrayDesignLink( subsumer ) );
        } else if ( this.subsumees != null && this.subsumees.size() > 0 ) {
            for ( ArrayDesign subsumee : subsumees ) {
                buf.append( arrayDesignLink( subsumee ) );
                Collection<ArrayDesign> m = subsumee.getSubsumedArrayDesigns();
                if ( m.size() > 0 ) {
                    for ( ArrayDesign ms : m ) {
                        buf.append( " &#187; subsumes " + arrayDesignShortLink( ms ) ); // FIXME, recurse to go down
                                                                                        // even further.
                    }
                }
                buf.append( "<br />" );
            }
        }

        try {
            pageContext.getOut().print( buf.toString() );
        } catch ( Exception ex ) {
            throw new JspException( "arrayDesignGroupingTag: " + ex.getMessage() );
        }
        return SKIP_BODY;
    }

    private String arrayDesignShortLink( ArrayDesign ad ) {
        return "<a href=\"/Gemma/arrays/showArrayDesign.html?id=" + ad.getId() + "\">" + ad.getShortName() + "</a>";
    }

    private String arrayDesignLink( ArrayDesign ad ) {
        return "<a href=\"/Gemma/arrays/showArrayDesign.html?id=" + ad.getId() + "\">" + ad.getShortName() + "</a> ("
                + ad.getName() + ")";
    }
}
