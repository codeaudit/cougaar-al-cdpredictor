package com.axiom.lib.image ;

import java.io.* ;

public class GIFBitmap extends Bitmap{
 
    /** Read a header of a file.*/
    public int readBitmapHeader( InputStream is, Header header ) 
                        throws java.io.IOException 
    {
        throw new RuntimeException("Not supported.") ; 
    }
    
    /** Read the image data and header. */
    public  ImageData readData( InputStream is ) 
                         throws IOException, BitmapFormatException 
    {
        throw new RuntimeException("Not supported.") ;      
    }
    
    /** Write an image file with default parameters.  Override to implement functionality.*/
    public void writeData( OutputStream os, ImageData data ) 
                         throws java.io.IOException 
    {
        throw new RuntimeException("Not supported.") ;        
    }
    
    /**
     *  Bundle describing format information.
     */
    public static final Format FORMAT ;
        
    static {
       FORMAT = new Format();
       FORMAT.shortName = "GIF";
       FORMAT.longName = "CompuServe Graphics Interchange Format";
       FORMAT.extensions = new String[1] ;
       FORMAT.extensions[0] = "gif" ;
       FORMAT.module = new GIFBitmap() ;
    }
}