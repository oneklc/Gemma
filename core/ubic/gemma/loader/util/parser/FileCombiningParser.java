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
package ubic.gemma.loader.util.parser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

/**
 * A parser that combines results across files. To be used if parsing a single file does not make any sense.
 * 
 * @author pavlidis
 * @version $Id: FileCombiningParser.java,v 1.2 2006/06/30 14:51:44 paul Exp $
 */
public interface FileCombiningParser {

    public static final int PARSE_ALERT_FREQUENCY = 10000;

    /**
     * Parse a List of files.
     * 
     * @param files
     * @throws IOException
     */
    public void parse( List<File> files ) throws IOException;

    /**
     * Parse a List of InputStreams.
     * 
     * @param streams
     * @throws IOException
     */
    public void parseStreams( List<InputStream> streams ) throws IOException;

    /**
     * @return results
     */
    public Collection<Object> getResults();
}
