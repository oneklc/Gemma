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
package ubic.gemma.loader.entrez;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ubic.gemma.loader.entrez.pubmed.XMLUtils;

/**
 * @author paul
 * @version $Id: EutilFetch.java,v 1.9 2012/12/03 19:41:46 paul Exp $
 */
public class EutilFetch {

    private static String ESEARCH = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=";
    // private static String EFETCH = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=";
    private static String EFETCH = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi?db=";

    public enum Mode {
        HTML, TEXT, XML
    }

    static DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    /**
     * @param db e.g., gds.
     * @param searchString
     * @param limit - Maximum number of records to return.
     * @return
     * @throws IOException
     */
    public static String fetch( String db, String searchString, int limit ) throws IOException {
        return fetch( db, searchString, Mode.TEXT, limit );
    }

    /**
     * @param db e.g., gds.
     * @param searchString
     * @param mode HTML,TEXT or XML FIXME only provides XML.
     * @param limit - Maximum number of records to return.
     * @see {@link http://www.ncbi.nlm.nih.gov/corehtml/query/static/esummary_help.html}
     * @return XML
     * @throws IOException
     */
    public static String fetch( String db, String searchString, Mode mode, int limit ) throws IOException {

        try {
            URL searchUrl = new URL( ESEARCH + db + "&usehistory=y&term=" + searchString );
            URLConnection conn = searchUrl.openConnection();
            conn.connect();
            InputStream is = conn.getInputStream();

            factory.setIgnoringComments( true );
            factory.setValidating( false );

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse( is );

            NodeList countNode = document.getElementsByTagName( "Count" );
            Node countEl = countNode.item( 0 );

            int count = 0;
            try {
                count = Integer.parseInt( XMLUtils.getTextValue( ( Element ) countEl ) );
            } catch ( NumberFormatException e ) {
                throw new IOException( "Could not parse count from: " + searchUrl );
            }

            if ( count == 0 ) throw new IOException( "Got no records from: " + searchUrl );

            NodeList qnode = document.getElementsByTagName( "QueryKey" );

            Element queryIdEl = ( Element ) qnode.item( 0 );

            NodeList cknode = document.getElementsByTagName( "WebEnv" );
            Element cookieEl = ( Element ) cknode.item( 0 );

            String queryId = XMLUtils.getTextValue( queryIdEl );
            String cookie = XMLUtils.getTextValue( cookieEl );

            URL fetchUrl = new URL( EFETCH + db + "&mode=" + mode.toString().toLowerCase() + "&query_key=" + queryId
                    + "&WebEnv=" + cookie + "&retmax=" + limit );

            conn = fetchUrl.openConnection();
            conn.connect();
            is = conn.getInputStream();

            BufferedReader br = new BufferedReader( new InputStreamReader( is ) );
            StringBuilder buf = new StringBuilder();
            String line = null;
            while ( ( line = br.readLine() ) != null ) {
                buf.append( line );
                // if ( !line.endsWith( " " ) ) {
                // buf.append( " " );
                // }
            }
            return buf.toString();
        } catch ( MalformedURLException e ) {
            throw new RuntimeException( e );
        } catch ( ParserConfigurationException e ) {
            throw new RuntimeException( e );
        } catch ( SAXException e ) {
            throw new RuntimeException( e );
        }

    }

    static void printElements( Document doc ) {

        NodeList nodelist = doc.getElementsByTagName( "*" );
        Node node;

        for ( int i = 0; i < nodelist.getLength(); i++ ) {
            node = nodelist.item( i );
            System.out.print( node.getNodeName() + " " );
        }

        System.out.println();

    }

}
