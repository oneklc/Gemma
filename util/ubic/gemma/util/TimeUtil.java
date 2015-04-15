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
package ubic.gemma.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.apache.commons.lang.time.StopWatch;

/**
 * @author paul
 * @version $Id: TimeUtil.java,v 1.1 2007/12/22 08:23:40 paul Exp $
 */
public class TimeUtil {

    /**
     * @param overallWatch
     * @return
     */
    public static String getMinutesElapsed( StopWatch overallWatch ) {
        Long overallElapsed = overallWatch.getTime();
        NumberFormat nf = new DecimalFormat();
        nf.setMaximumFractionDigits( 2 );
        String minutes = nf.format( overallElapsed / ( 60.0 * 1000.0 ) );
        return minutes;
    }
}
