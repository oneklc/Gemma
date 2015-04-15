/*
 * The basecode project
 * 
 * Copyright (c) 2005 University of British Columbia
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */
package ubic.basecode.bio;

import ubic.basecode.dataStructure.OntologyEntry;

/**
 * @author pavlidis
 * @version $Id: GOEntry.java,v 1.3 2008/02/14 18:19:51 paul Exp $
 */
public class GOEntry extends OntologyEntry {

    private String aspect;

    public GOEntry( String id, String name, String def, String aspect ) {
        super( id, name, def );
        this.aspect = aspect;
    }

    public String getAspect() {
        return aspect;
    }

    public void setAspect( String aspect ) {
        this.aspect = aspect;
    }

}
