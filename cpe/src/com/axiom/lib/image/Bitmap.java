package com.axiom.lib.image ;

import java.util.Vector ;
import java.awt.Image ;
import java.io.* ;

/**
 *  A general bitmap class for reading and writing bitmap files.  
 *  Currently, it has very sparse support for formats, but could 
 *  be extended over time to include more formats.
 */
public abstract class Bitmap {
    
    /**
     *   Error flag indicating a bad magic id.
     */
    public static final int BAD_MAGIC = 0x0001 ;
    
    /**
     *  Size paramter is corrupt.
     */
    public static final int BAD_SIZE = 0x0002 ;
    
    public static final int BAD_MAX_INTENSITY = 0x0004 ;
    
    public static String getNoExtName( String s ) {
       int i = s.lastIndexOf('.' );
       if ( i == -1 ) {
          return s ;
       }
       return s.substring( 0, i ) ;
    }
    
    public static String getExtension( File file ) {
       String s = file.getName() ;
       return getExtension( s ) ;
    }
    
    public static String getExtension( String s ) {
       int i = s.lastIndexOf('.' );
       if ( i == -1 ) {
          return null ;
       }
       String ext = s.substring( i + 1 ) ;
       return ext ;
    }
    
    public static Format[] getSupportedTypes() {
       return ( Format[] ) formats.clone() ;
    }
        
    /**
     *  Guesses the type of bitmap using a file's extension.
     */
    public static Format guessType( String filename ) {
        String ext = getExtension( filename ) ;
        if ( ext == null )
            return null ;
        for (int i=0;i<formats.length;i++) {
            Format format = formats[i] ;
            for (int j=0;j<format.extensions.length;j++) {
                if ( format.extensions[j].equals( ext ) )
                   return format ;
            }
        }
        return null ;
    }
    
    public static int[] grey( int[] data ) {
        int[] result = new int[data.length] ;
    
        for (int i=0;i<data.length;i++) {
            result[i] = ( int ) grey( data[i] ) & 0xFF ;
        }
        return result ;
    }
    
    public static int[] color( int[] grey ) {
        int[] result = new int[ grey.length ] ;
        
        for (int i=0;i<result.length;i++) {
            result[i] = color( grey[i] ) ;   
        }
        
        return result ;
    }
    
    public static byte grey( int color ) {
        return ( byte ) ( ( ( color >> 16 & 0xFF ) + 
    	                    ( color >> 8 & 0xFF ) + 
    	                    ( color & 0xFF ) ) / 3 ) ;        
    }
    
    public static int color( int gray ) {
        return 255 << 24 | 
	 	       ( gray << 16 ) & ( 255 << 16 ) | 
               ( gray << 8) & ( 255 << 8 ) | 
                 gray & 255  ; 
    }
    
    /**
     * Read an image of type fileType, using the filename extension to guess the
     * type of file.
     */
    public static ImageData readImage( File file ) throws IOException, BitmapFormatException {
        
        Format format = guessType( file.getName() ) ;
        
        if ( format == null ) {
            throw new BitmapFormatException( "File " + file.getName() + " format not recognized." ) ;   
        }
        
        //try {
            FileInputStream fis = new FileInputStream( file ) ;
            ImageData data = format.module.readData( fis ) ;
            return data ;
        //}
    }
    
    /**
     *  Read a file using format.
     */
    public static ImageData readImage( Format format, File file ) throws IOException {
        
        return null ;
    }
    
    public static boolean writeImage( String fileType, File file, Header header, ImageData data ) {
        return false ;
    }
    
    /** Read a header of a file.*/
    public abstract int readBitmapHeader( InputStream is, Header header ) 
                        throws java.io.IOException ;
    
    /** Read the image data and header. */
    public abstract ImageData readData( InputStream is ) 
                         throws IOException, BitmapFormatException ;
    
    /** Write an image file with default parameters.  Override to implement functionality.*/
    public abstract void writeData( OutputStream os, ImageData data ) 
                         throws java.io.IOException ;
    
    /**
     *  An array of Format objects representing the supported formats.
     */     
    protected static Format[] formats ;
    
    static {
        formats = new Format[3] ;
        formats[0] = PPMBitmap.FORMAT ;
        formats[1] = PGMBitmap.FORMAT ;
        formats[2] = GIFBitmap.FORMAT ;
    }
}