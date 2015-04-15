/*
 * The baseCode project
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
package ubic.basecode.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;

import cern.colt.list.ByteArrayList;

/**
 * Class to convert byte arrays (e.g., Blobs) to and from other types of arrays.
 * 
 * @author Kiran Keshav
 * @author Paul Pavlidis
 * @version $Id: ByteArrayConverter.java,v 1.11 2009/12/18 00:30:07 paul Exp $
 */
public class ByteArrayConverter {

    // sizes are in bytes.

    private static final int CHAR_SIZE = 2;

    private static final int DOUBLE_SIZE = 8;

    private static final int INT_SIZE = 4;

    private static final int LONG_SIZE = 8;

    /**
     * 3.3.4 The boolean Type
     * <p>
     * Although the Java virtual machine defines a boolean type, it only provides very limited support for it. There are
     * no Java virtual machine instructions solely dedicated to operations on boolean values. Instead, expressions in
     * the Java programming language that operate on boolean values are compiled to use values of the Java virtual
     * machine int data type.
     * <p>
     * The Java virtual machine does directly support boolean arrays. Its newarray instruction enables creation of
     * boolean arrays. Arrays of type boolean are accessed and modified using the byte array instructions baload and
     * bastore.2
     * <p>
     * The Java virtual machine encodes boolean array components using 1 to represent true and 0 to represent false.
     * Where Java programming language boolean values are mapped by compilers to values of Java virtual machine type
     * int, the compilers must use the same encoding.
     * 
     * @see http://java.sun.com/docs/books/vmspec/2nd-edition/html/Overview.doc.html#12237
     */
    private static final int BOOL_SIZE = 1; // erm...this seems to work.

    /**
     * @param boolarray
     * @return byte[]
     */
    public byte[] booleanArrayToBytes( boolean[] boolarray ) {
        if ( boolarray == null ) return null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream( bos );
        try {
            for ( boolean element : boolarray ) {
                dos.writeBoolean( element );
            }
        } catch ( IOException e ) {
            // do nothing
        }
        return bos.toByteArray();
    }

    /**
     * Convert a byte array with one-byte-per-character ASCII encoding (aka ISO-8859-1).
     * 
     * @param barray
     * @return
     */
    public String byteArrayToAsciiString( byte[] barray ) {
        if ( barray == null ) return null;
        try {
            return new String( barray, "ISO-8859-1" );
        } catch ( UnsupportedEncodingException e ) {
            throw new RuntimeException( "Conversion error", e );
        }
    }

    /**
     * @param barray
     * @return boolean[]
     */
    public boolean[] byteArrayToBooleans( byte[] barray ) {
        if ( barray == null ) return null;
        ByteArrayInputStream bis = new ByteArrayInputStream( barray );
        DataInputStream dis = new DataInputStream( bis );
        boolean[] iarray = new boolean[barray.length / BOOL_SIZE];
        int i = 0;

        try {
            while ( true ) {
                iarray[i] = dis.readBoolean();
                i++;
            }
        } catch ( IOException e ) {
            // do nothing.
        }

        try {
            dis.close();
            bis.close();
        } catch ( IOException e1 ) {
            throw new RuntimeException( "Conversion error", e1 );
        }

        return iarray;
    }

    /**
     * @param barray
     * @return char[]
     */
    public char[] byteArrayToChars( byte[] barray ) {
        if ( barray == null ) return null;
        ByteArrayInputStream bis = new ByteArrayInputStream( barray );
        DataInputStream dis = new DataInputStream( bis );
        char[] carray = new char[barray.length / CHAR_SIZE];

        int i = 0;
        try {
            while ( true ) {
                carray[i] = dis.readChar();
                i++;
            }
        } catch ( IOException e ) {
            // do nothing.
        }

        try {
            dis.close();
            bis.close();
        } catch ( IOException e ) {
            throw new RuntimeException( "Conversion error", e );
        }

        return carray;
    }

    /**
     * @param barray
     * @return double[]
     */
    public double[] byteArrayToDoubles( byte[] barray ) {
        if ( barray == null ) return null;
        ByteArrayInputStream bis = new ByteArrayInputStream( barray );
        DataInputStream dis = new DataInputStream( bis );

        double[] darray = new double[barray.length / DOUBLE_SIZE];
        int i = 0;
        try {
            while ( true ) {
                darray[i] = dis.readDouble();
                i++;
            }
        } catch ( IOException e ) {
            // do nothing.
        }

        try {
            bis.close();
        } catch ( IOException e1 ) {
            throw new RuntimeException( "Conversion error", e1 );
        }
        return darray;
    }

    /**
     * @param barray
     * @return int[]
     */
    public int[] byteArrayToInts( byte[] barray ) {
        if ( barray == null ) return null;
        ByteArrayInputStream bis = new ByteArrayInputStream( barray );
        DataInputStream dis = new DataInputStream( bis );
        int[] iarray = new int[barray.length / INT_SIZE];
        int i = 0;

        try {
            while ( true ) {
                iarray[i] = dis.readInt();
                i++;
            }
        } catch ( IOException e ) {
            // do nothing.
        }

        try {
            dis.close();
            bis.close();
        } catch ( IOException e1 ) {
            throw new RuntimeException( "Conversion error", e1 );
        }

        return iarray;
    }

    /**
     * @param barray
     * @return long[] resulting from parse of the bytes.
     */
    public long[] byteArrayToLongs( byte[] barray ) {
        if ( barray == null ) return null;
        ByteArrayInputStream bis = new ByteArrayInputStream( barray );
        DataInputStream dis = new DataInputStream( bis );
        long[] iarray = new long[barray.length / LONG_SIZE];
        int i = 0;

        try {
            while ( true ) {
                iarray[i] = dis.readLong();
                i++;
            }
        } catch ( IOException e ) {
            // do nothing.
        }

        try {
            dis.close();
            bis.close();
        } catch ( IOException e1 ) {
            throw new RuntimeException( "Conversion error", e1 );
        }

        return iarray;
    }

    /**
     * Convert a byte array into a array of Strings. It is assumed that separate strings are delimited by '\u0000'
     * (NUL). Note that this method cannot differentiate between empty strings and null strings. A string that is empty
     * will be returned as an empty string, not null.
     * 
     * @param bytes
     * @return
     */
    public String[] byteArrayToStrings( byte[] bytes ) {
        List<String> strings = new ArrayList<String>();
        ByteArrayList buf = new ByteArrayList();
        for ( byte element : bytes ) {
            if ( element == '\u0000' ) {
                String newString = new String( buf.elements() );
                newString = newString.trim();
                strings.add( newString );
                buf = new ByteArrayList();
            } else {
                buf.add( element );
            }
        }

        String[] result = new String[strings.size()];
        for ( int i = 0; i < strings.size(); i++ ) {
            result[i] = strings.get( i );
        }
        return result;
    }

    /**
     * Convert a byte array to a tab-delimited string.
     * 
     * @param bytes
     * @param type The Class of primitives the bytes are to be interpreted as. If this is String, then the bytes are
     *        directly interpreted as tab-delimited string (e.g., no extra tabs are added).
     * @return
     * @throws UnsupportedOperationException if Class is a type that can't be converted by this.
     */
    public String byteArrayToTabbedString( byte[] bytes, Class<?> type ) {
        if ( bytes == null ) return null;

        if ( type.equals( Double.class ) ) {
            Double[] array = ArrayUtils.toObject( byteArrayToDoubles( bytes ) );
            return formatAsString( array );
        } else if ( type.equals( Integer.class ) ) {
            Integer[] array = ArrayUtils.toObject( byteArrayToInts( bytes ) );
            return formatAsString( array );
        } else if ( type.equals( Long.class ) ) {
            Long[] array = ArrayUtils.toObject( byteArrayToLongs( bytes ) );
            return formatAsString( array );
        } else if ( type.equals( String.class ) ) {
            return byteArrayToAsciiString( bytes );
        } else if ( type.equals( Boolean.class ) ) {
            Boolean[] array = ArrayUtils.toObject( byteArrayToBooleans( bytes ) );
            return formatAsString( array );
        } else if ( type.equals( Character.class ) ) {
            Character[] array = ArrayUtils.toObject( byteArrayToChars( bytes ) );
            return formatAsString( array );
        } else {
            throw new UnsupportedOperationException( "Can't convert " + type.getName() );
        }

    }

    /**
     * @param array
     * @return
     */
    private String formatAsString( Object[] array ) {
        StringBuffer buf = new StringBuffer();
        for ( int i = 0; i < array.length; i++ ) {
            buf.append( array[i] );
            if ( i != array.length - 1 ) buf.append( "\t" ); // so we don't have a trailing tab.
        }
        return buf.toString();
    }

    /**
     * @param carray
     * @return byte[]
     */
    public byte[] charArrayToBytes( char[] carray ) {
        if ( carray == null ) return null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream( bos );

        try {
            for ( char element : carray ) {
                dos.writeChar( element );
            }
            dos.close();
            bos.close();

        } catch ( IOException e ) {
            // do nothing.
        }

        return bos.toByteArray();
    }

    /**
     * @param darray
     * @return byte[]
     */
    public byte[] doubleArrayToBytes( double[] darray ) {
        if ( darray == null ) return null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream( bos );
        try {
            for ( double element : darray ) {
                dos.writeDouble( element );
            }
        } catch ( IOException e ) {
            // do nothing
        }
        return bos.toByteArray();
    }

    /**
     * @param iarray
     * @return byte[]
     */
    public byte[] intArrayToBytes( int[] iarray ) {
        if ( iarray == null ) return null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream( bos );
        try {
            for ( int element : iarray ) {
                dos.writeInt( element );
            }
            dos.close();
            bos.close();
        } catch ( IOException e ) {
            // do nothing
        }
        return bos.toByteArray();
    }

    /**
     * @param larray
     * @return byte[]
     */
    public byte[] longArrayToBytes( long[] larray ) {
        if ( larray == null ) return null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream( bos );
        try {
            for ( long element : larray ) {
                dos.writeLong( element );
            }
            dos.close();
            bos.close();
        } catch ( IOException e ) {
            // do nothing
        }
        return bos.toByteArray();
    }

    /**
     * Note that this method cannot differentiate between empty strings and null strings. A string that is empty will be
     * returned as an empty string, not null, while a null string will be stored as an empty string.
     * 
     * @param stringArray
     * @return byte[]
     */
    public byte[] stringArrayToBytes( Object[] stringArray ) {
        if ( stringArray == null ) return null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream( bos );

        try {
            for ( Object element : stringArray ) {
                String string = ( String ) element;
                if ( string != null ) {
                    dos.write( string.getBytes() );
                }
                dos.write( '\u0000' );
            }
            dos.close();
            bos.close();

        } catch ( IOException e ) {
            // do nothing.
        }

        return bos.toByteArray();
    }

    /**
     * @param data
     */
    public byte[] toBytes( Object data ) {
        return toBytes( new Object[] { data } );
    }

    /**
     * Convert an array of Objects into an array of bytes. If the array contains Strings, it is converted to a
     * tab-delimited string, and then converted to bytes.
     * 
     * @param array of Objects to be converted to bytes.
     * @return
     * @throws UnsupportedOperationException if Objects are a type that can't be converted by this.
     */
    public byte[] toBytes( Object[] array ) {
        if ( array == null ) return null;
        if ( array.length == 0 ) return new byte[] {};

        // sanity check, catches obvious errors.
        if ( array[0] == null ) throw new IllegalArgumentException( "Null values cannot be converted" );

        if ( array[0] instanceof Boolean ) {
            boolean[] toConvert = new boolean[array.length];
            for ( int i = 0; i < array.length; i++ ) {
                boolean object = ( ( Boolean ) array[i] ).booleanValue();
                toConvert[i] = object;
            }
            return booleanArrayToBytes( toConvert );
        } else if ( array[0] instanceof Double ) {
            double[] toConvert = new double[array.length];
            for ( int i = 0; i < array.length; i++ ) {
                double object = ( ( Double ) array[i] ).doubleValue();
                toConvert[i] = object;
            }
            return doubleArrayToBytes( toConvert );
        } else if ( array[0] instanceof Character ) {
            char[] toConvert = new char[array.length];
            for ( int i = 0; i < array.length; i++ ) {
                char object = ( ( Character ) array[i] ).charValue();
                toConvert[i] = object;
            }
            return charArrayToBytes( toConvert );
        } else if ( array[0] instanceof String ) {
            return stringArrayToBytes( array );
        } else if ( array[0] instanceof Integer ) {
            int[] toConvert = new int[array.length];
            for ( int i = 0; i < array.length; i++ ) {
                int object = ( ( Integer ) array[i] ).intValue();
                toConvert[i] = object;
            }
            return intArrayToBytes( toConvert );
        } else if ( array[0] instanceof Long ) {
            long[] toConvert = new long[array.length];
            for ( int i = 0; i < array.length; i++ ) {
                int object = ( ( Long ) array[i] ).intValue();
                toConvert[i] = object;
            }
            return longArrayToBytes( toConvert );
        } else {
            throw new UnsupportedOperationException( "Can't convert " + array[0].getClass() + " to bytes" );
        }

    }

}