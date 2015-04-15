/*
 * The baseCode project
 * 
 * Copyright (c) 2008 University of British Columbia
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
package ubic.basecode.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * See http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html
 * 
 * @author pavlidis
 * @version $Id: GenericStreamConsumer.java,v 1.2 2009/12/04 13:59:26 paul Exp $
 */
public class GenericStreamConsumer extends Thread {
    InputStream is;

    public GenericStreamConsumer( InputStream is ) {
        this.is = is;
    }

    @Override 
    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader( is );
            BufferedReader br = new BufferedReader( isr );

            String line = null;
            while ( ( line = br.readLine() ) != null ) {
                System.err.println( line );
            }
        } catch ( IOException ioe ) {
            ioe.printStackTrace();
        }
    }
}