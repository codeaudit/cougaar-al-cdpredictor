package com.axiom.escript ;
import java.io.* ;

public class ClassReaderUtils {

    public static String convertFromStoredClassName( String name ) {
        StringBuffer buf = new StringBuffer() ;

        for (int i=0;i<name.length();i++) {
           char c = name.charAt(i) ;
           if (  c == '/' ) {
              buf.append( '.' ) ;
           }
           else
              buf.append( c ) ;
        }
        return buf.toString() ;
    }

    public static String convertToStoredClassName( String name ) {
        StringBuffer buf = new StringBuffer() ;

        for (int i=0;i<name.length();i++) {
           char c = name.charAt(i) ;
           if (  c == '.' ) {
              buf.append( '/' ) ;
           }
           else
              buf.append( c ) ;
        }
        return buf.toString() ;
    }

    public static void writeUTF( DataOutput dos, String s ) throws java.io.IOException {
        for (int i=0;i<s.length();i++) {
            char c = s.charAt(i) ;
            if ( ( c & 0xF800 ) > 0 ) {
                dos.writeByte( ( byte ) ( c >>> 12 & 0xF ) | 0xE0 );
                dos.writeByte( ( byte ) ( c >>> 6 & 0x3F ) | 0x80 ) ;
                dos.writeByte( ( byte ) ( c & 0x3F ) | 0x80 ) ;
            }
            else if ( ( c & 0x0780 ) > 0 ) {
                dos.writeByte( ( byte ) ( c >>> 6 & 0x1F ) | 0xC0 ) ;
                dos.writeByte( ( byte ) ( c & 0x3F ) | 0x80 ) ;
            }
            else {
                dos.writeByte( ( byte ) c ) ;
            }
        }
    }

    /**
     * Reads a UTF string of fixed length.
     */
    public static String readUTF( DataInput ds, int length ) throws IOException {
        char[] buffer = new char[ length ];

        int c = 0 ;
        int i = 0 ;
        while ( i < length ) {
           int b = ds.readByte() ;
           if ( ( b & 0x80 ) == 0 ) {
              buffer[c++] = ( char ) b ;
              i++;
           }
           else if ( ( b & 0xE0 ) == 0xC0 ) {
              int b2 = ds.readByte() ;
              buffer[c++] = ( char ) ( ( b & 0x1F ) << 6 + ( b2 & 0x3F ) );
              i+=2 ;
           }
           else if ( ( b & 0xF0 ) == 0xE0 ) {
              int b2 = ds.readByte() ;
              int b3 = ds.readByte() ;
              buffer[c++] = ( char ) (((b & 0xf) << 12) + ((b2 & 0x3f) << 6) + (b3 & 0x3f) );
              i+= 3 ;
           }
        }
        return new String( buffer, 0, c ) ;
    }

    /**
     *  Read an unsigned 4-byte value.
     */
    public static long readU4( DataInput ds ) throws IOException {
        long result ;
        result = ((( long ) ds.readUnsignedShort() ) << 16 ) + ds.readUnsignedShort() ; 
        return result ;
    }

}
