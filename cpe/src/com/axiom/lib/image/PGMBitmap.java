package com.axiom.lib.image;
import java.awt.image.* ;

import java.io.*;

/**
 *  Utility class for reading PGM (Portable Grey Map) files.
 *  The stream is parsed into an ImageData object with byte[] internal representation.
 */
public class PGMBitmap extends Bitmap {
    
    public static final Format FORMAT ;

    private static ColorModel greyScaleModel ;

    private PGMBitmap() {

    }

    public int readBitmapHeader( InputStream is, Header header ) throws IOException {
        int res = readHeader( is, header ) ;
        return res ;
    }

    public static Bitmap getImpl() {
        return FORMAT.module ;
    }
    
    public void write( OutputStream os, ImageData data ) throws IOException {
        DataOutputStream ds = new DataOutputStream( os ) ;
        
    	String header = "P5\n" + data.w + " " + data.h + "\n255\n" ;
        ds.writeBytes( header ) ;
    	
    	byte[] buffer = new byte[ data.w ] ;
    	
    	int j = 0;
        Object o = data.getData() ;

    	// Write out the data line by line, converting it to grey scale
        if ( o instanceof int[] ) {
            int[] arr = ( int[] ) o ;

    	    for ( int i=0;i<data.h;i++) {
    	        for ( int k=0;k<data.w;k++) {
                    int rgba = data.colorModel.getRGB( arr[j] ) ;
    	            buffer[k] = Bitmap.grey( rgba ) ;
    	            j++ ;
    	        }
                ds.write( buffer, 0, buffer.length ) ;
    	    }
            ds.flush() ;
        }
        else if ( o instanceof byte[] ) { // Going to assume that this is a simple gray scale!
            ds.write( ( byte[] ) o, 0, data.w * data.h ) ;
            ds.flush() ;
        }
    }

   /**
    * Reads the header associated with a PPM file and returns the result.
    */
    protected static int readHeader( InputStream fs, Header header ) 
                         throws IOException
    {
       char c1 = (char) fs.read( ) ;
       char c2 = (char) fs.read( ) ;
       int w = readInt( fs );
       int h = readInt( fs ) ;
       int m = readInt( fs ) ;  // Maximum pixel value
       
       int result = 0;
       
       if ( c1 != 'P' || c2 != '5' ) {
          result |= Bitmap.BAD_MAGIC ;
       }
       
       if ( w <= 0 || h <=0 ) {
          result |= Bitmap.BAD_SIZE ;
       }
       
       header.w = w ;
       header.h = h ;
       header.bpp = 8 ;
       
       return result ;
    }

    public ImageData readData( InputStream is ) throws IOException, BitmapFormatException {
        return read( is ) ;
    }

    public void writeData( OutputStream os, ImageData data ) throws IOException {
        write( os, data ) ;
    }
    
    /**
     *  Read the image data from an input stream.
     *
     *  @return null if error has occurred, ImageData object otherwise.
     */
    public static ImageData read( InputStream fr ) throws IOException {

	    Header header = new Header();
	    int result = 0 ;

        result = readHeader( fr, header ) ;

        // Indicates some error reading the file header. (Should this throw
        // an IOException?
        if ( result != 0 )
            return null ;

        int w = header.w ;
        int h = header.h ;
        byte[] barray = new byte[w*h];

        try {
            fr.read(barray);  // Assume gray scale format
        }
	    catch ( IOException e ) {
	        throw e ;
        }
	    return new ImageData( w, h, barray ) ;
    }

    /**
     * Read in integer expressed in decimal notation, including an additional
     * character.
     *
     * @return int
     * @param fs java.io.FileInputStream
     * @exception IOException Thrown if a numerical format is not found.
     */
    protected static int readInt( InputStream fs ) throws IOException {
        char c = ' ';
        int numval = 0;
        while ( Character.isWhitespace(c)  ) {
	       c = readChar(fs);
        }

        if (!Character.isDigit(c))  // Whoops, this is not a number!
	        throw new IOException("Error reading integer:  no digits found.") ;

        while ( Character.isDigit( c ) ) {
	        numval = numval * 10 + ( c - '0' ) ;
	        c = readChar( fs ) ;
        }
        return numval ;
    }

   /** 'Magic' header.
    */
    public static byte[] magic = {'P','5'};

    /**
     * Read character, discarding all chracters up to next newline if a '#'
     * is found.
     * @return char
     * @param f java.io.FileReader
     */
    private static char readChar( InputStream f ) throws IOException {
        char c = ( char ) f.read() ;
        while ( c == '#' ) {
	        while ( c != '\n' ) {
	            c = ( char ) f.read();
	        }
        }
        return c;
    }

    private static ColorModel makeGreyScaleModel() {
        DirectColorModel model = new DirectColorModel( 8, 0xFF, 0xFF, 0xFF ) ;
        return model ;
    }

    static {
       FORMAT = new Format();
       FORMAT.shortName = "Greymap";
       FORMAT.longName = "Portable Greyscale-map (binary P5 type)";
       FORMAT.extensions = new String[1] ;
       FORMAT.extensions[0] = "pgm" ;
       FORMAT.module = new PGMBitmap() ;
       greyScaleModel = makeGreyScaleModel() ;
    }
}