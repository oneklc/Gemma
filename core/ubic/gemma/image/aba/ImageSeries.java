/*
 * The Gemma project
 * 
 * Copyright (c) 2009 University of British Columbia
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
package ubic.gemma.image.aba;

import java.util.Collection;
import java.util.HashSet;

/**
 * A class that represents the ImageSeries information gotten from the alan brain atals
 * 
 * @version $Id: ImageSeries.java,v 1.3 2009/12/20 07:50:11 paul Exp $ @author kelsey
 */

public class ImageSeries {

    Integer imageSeriesId;
    String plane;
    Collection<Image> images;

    public ImageSeries( Integer imageSeriesId, String plane ) {
        this.imageSeriesId = imageSeriesId;
        this.plane = plane;

    }

    public void addImage( Image image ) {

        if ( this.images == null ) this.images = new HashSet<Image>();

        this.images.add( image );
    }

    public Collection<Image> getImages() {
        return images;
    }

    public Integer getImageSeriesId() {
        return imageSeriesId;
    }

    public String getPlane() {
        return plane;
    }

    public void setImages( Collection<Image> images ) {
        this.images = images;
    }

    public void setImageSeriesId( Integer imageSeriesId ) {
        this.imageSeriesId = imageSeriesId;
    }

    public void setPlane( String plane ) {
        this.plane = plane;
    }
}
