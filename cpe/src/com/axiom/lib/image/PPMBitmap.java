package com.axiom.lib.image;

import java.io.*;
/**
 *  Utility class containing methods to read PPM files.
 * 
 */
public class PPMBitmap extends Bitmap {
    private PPMBitmap() {
    }   

    /**
     *  Bundle describing format information.
     */
    public static final Format FORMAT ;
    
    /**
     * Reads the header associated with a PPM file and returns the result.
     */
    public static int readHeader( InputStream fs, Header header ) throws IOException {
        char c1 = ( char ) fs.read();
        char c2 = ( char ) fs.read();  // Read P6
   
        int w = readInt( fs );
        int h = readInt( fs );
        int m = readInt( fs ) ;

        // Validate and return any format errors.
        int result = 0 ;
   
        if ( c1 != 'P' || c2 != '6' ) {
            result |= Bitmap.BAD_MAGIC ;
        }
   
        if ( w <= 0 || h <= 0 ) {
            result |= Bitmap.BAD_SIZE ;
        }
   
        if ( result == 0 ) {
            header.w = w ;
            header.h = h ;
            header.bpp = 24 ;  // 24 bits per pixel
        }
   
        return result ;
    }

    public int readBitmapHeader( InputStream is, Header header ) 
                        throws java.io.IOException 
    {
        return readHeader( is, header ) ;
    }

    public static ImageData read( InputStream fr ) throws IOException {
		
	    Header header = new Header();
	    int result = 0 ;
	    try {
	        result = readHeader( fr, header ) ;
	    }
        catch ( IOException e ) {
	        throw e ;
        }
    
        // Indicates some error reading the file.  A more structured error handling
        // system could be substituted
        if ( result != 0 )
            return null ;
		
        int w = header.w ;
        int h = header.h ;
        byte[] barray = new byte[w*h*3];
        int[] array = new int[w*h];

        // Read the array as a set of 3-byte RGB triples. After reading the header, 
        // we are positioned exactly at the beginning of the byte area.
        try {
            fr.read(barray);
            int j = 0;            
            for (int i=0;i<array.length;i++) {
	 	        array[i] = 255 << 24 | 
                           ( barray[j++] << 16 ) & ( 255 << 16 ) |
                           ( barray[j++] << 8) & ( 255 << 8 ) |
                           barray[j++] & 255  ;
	 	    }
        }
	    catch ( IOException e ) {
	        throw e ;
        }	  	
		 
	    return new ImageData( w, h, array ) ;
    }

    /** Write header.
     */
    public static byte[] magic = {'P','6'}; 
 
    /** Write P6 format header. Maxvalue of 255 is always assumed.*/

    private static void writeHeader( OutputStream fs, ImageData data ) {
        try {
            fs.write( magic );
            fs.write( '\n' );
            String temp = Integer.toString( data.w ) + " " + Integer.toString( data.h ) + "\n255\n" ;
            byte[] b = temp.getBytes();
            fs.write( b ) ;
        }
        catch ( IOException e ) {   
        }
    }

    public void writeData( OutputStream fs, ImageData data ) throws IOException {
        write( fs, data ) ;
    }

    public ImageData readData( InputStream is ) throws IOException {
        return read( is ) ;
    }

    /**
     *  Write an image data to an output stream
     */
    public static void write( OutputStream fs, ImageData data ) throws IOException {
        try {
            writeHeader( fs, data );

            byte[] b = new byte[data.w*3];

            Object o = data.getData() ;

            if ( o instanceof int[] ) {
                int[] arr = (int[]) o ;
                int k = 0 ;
                for (int i=0;i<data.h;i++) {
                    int l = 0 ;
                    for (int j=0;j<data.w;j++) {
                        int rgba = data.colorModel.getRGB( arr[k++] ) ;
                        b[l++] = (byte) ( rgba & ( 255 << 16 ) >> 16 );
                        b[l++] = (byte) ( rgba & ( 255 << 8 ) >> 8 ) ;
                        b[l++] = (byte) ( rgba & ( 255 ) );
                    }
                    fs.write( b );
                }
            }
            else if ( o instanceof byte[] ) {
                byte[] arr = (byte[]) o ;
                int k = 0 ;
                for (int i=0;i<data.h;i++) {
                    int l = 0 ;
                    for (int j=0;j<data.w;j++) {
                        int rgba = data.colorModel.getRGB( arr[k++] ) ;
                        b[l++] = (byte) ( rgba & ( 255 << 16 ) >> 16 );
                        b[l++] = (byte) ( rgba & ( 255 << 8 ) >> 8 ) ;
                        b[l++] = (byte) ( rgba & ( 255 ) );
                    }
                    fs.write( b ); // After each scan line, write
                }
            }
        }
        catch ( IOException e ) {
            throw e ;
        }
    
    }

    /**
     * Read in integer expressed in decimal notation.
     * @return int
     * @param fs java.io.FileInputStream
     * @exception IOException Thrown if a numerical format is not found.
     */
    public static int readInt( InputStream fs ) throws IOException {
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

   /**
    * Test program for reader. 
    * @param args java.lang.String[]
    */
    public static void main(String args[]) {
	    File file = null ;
	
	    if ( args.length > 0 ) {
	        file = new File( args[0] );	
	    }
        else {
	        System.out.println("Usage: PPMBitmap <filename>");	
	        System.exit(0);
        } 

        if ( !file.exists() )
   	        System.out.println("File " + file + " does not exist.");

        ImageData image = null ;
        
        try {
            FileInputStream fs = new FileInputStream( file ) ;
            image = PPMBitmap.read( fs ) ;
        }
        catch ( IOException e ){
            e.printStackTrace() ;
        }


        if ( image == null ) {
            System.out.println("Possible error reading " + file );
        }
	    return;
    }

    static {
        FORMAT = new Format();
        FORMAT.shortName = "Pixmap";
        FORMAT.longName = "Portable Pixel-map (binary P6 type)";
        FORMAT.extensions = new String[1] ;
        FORMAT.extensions[0] = "ppm" ;
        FORMAT.module = new PPMBitmap() ;
        FORMAT.capabilities = Format.READ_24BPP | Format.WRITE_24BPP ;
    }

}