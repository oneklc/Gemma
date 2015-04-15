/*
 * The Gemma project
 * 
 * Copyright (c) 2010 University of British Columbia
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
package ubic.gemma.job;

import org.apache.commons.lang.RandomStringUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * @author paul
 * @version $Id: TaskUtils.java,v 1.5 2013/03/04 17:56:39 anton Exp $
 */
public class TaskUtils {

    private static final int KEY_LENGTH = 16;

    private static Set<String> usedIds = new HashSet<String>();

    private static int MAX_ATTEMPTS = 1000;

    /**
     * @return a unique (since the JVM was started) task id.
     */
    public static String generateTaskId() {
        /*
         * Ensure we have a unique id.
         */
        int keepTrying = 0;
        while ( ++keepTrying < MAX_ATTEMPTS ) {
            String id = RandomStringUtils.randomAlphanumeric( KEY_LENGTH ).toUpperCase();
            if ( usedIds.isEmpty() || !usedIds.contains( id ) ) {
                usedIds.add( id );
                return id;
            }
        }

        /*
         * Just in case ...
         */
        throw new IllegalStateException( "Failed to find a unique task id in " + MAX_ATTEMPTS + " iterations" );
    }

}
