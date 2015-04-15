/*
 * The Gemma project
 * 
 * Copyright (c) 2012 University of British Columbia
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
package ubic.gemma.loader.pazar;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author paul
 * @version $Id: PazarLoader.java,v 1.6 2012/03/11 22:48:46 paul Exp $
 */
public interface PazarLoader {

    /**
     * @param is
     * @return
     * @throws IOException
     */
    public abstract int load( InputStream is ) throws IOException;

    /**
     * @param file
     * @return
     * @throws IOException
     */
    public abstract int load( File file ) throws IOException;

}