package com.axiom.lib.image ;
import java.awt.Color ;
import java.io.*;

/**
 *  Take images represented as ImageData objects and transforms them into smaller 
 *  ImageData.  The tiler makes subimages of uniform
 *  size.  It takes either images or ImageData objects as inputs.
 *
 *  <p>  All tilings begin in the location specified by the offset values, which
 *  default to 0 for both the x and y dimensions.
 */

public class ImageTiler {
    /**
     *  Make an image tiler with various parameters.
     *  @param width  Width of tile
     *  @param height Height of tile
     *  @param xstride X increment per tile
     *  @param ystride Y increment per tile
     *  @param fullsize  Allow only full-sized tiles to be generated.
     */
    public ImageTiler( int width, int height, int xstride, int ystride, boolean fullsize ) {
        this.w = width ;
        this.h = height ;
        this.xstride = xstride ;
        this.ystride = ystride ;
        this.fullsize = fullsize ;
    }
    
    /** Make an individual tile from the data. (Sort of a bitlt.*/
    public static ImageData makeTile( ImageData data, int x, int y, int w, int h ) {
        int lowerx = x, lowery = y, upperx = x + w, uppery = y + h ;
        if ( lowerx < 0 )
           lowerx = 0 ;
        if ( lowerx > data.w ) 
           lowerx = data.w ;
        if ( lowery < 0 )
           lowery = 0 ;
        if ( lowery > data.h ) 
           lowery = data.h ;
        if ( upperx > data.w )
           upperx = data.w ;
        if ( uppery > data.h ) 
           uppery = data.h ;
           
        int start = lowerx + lowery * data.w ;  // Starting array index
        int length = upperx - lowerx ;          // Length of run ( determined by clipping to data ).
        int count = uppery - lowery ;           // Number of iterations
        int srcindex = start ;
        int destindex = 0 ;

        Object o = data.getData() ;
        Object array = null ;
        if ( o instanceof int[] ) {
            array = new int[ w * h ] ;
        }
        else if ( o instanceof byte[] ) {
            array = new byte[ w * h ] ;
        }

        // int[] array = new int[ w * h ] ;
        
        // Fill the array with the background color
        for (int i=0;i<count;i++) {
            System.arraycopy( o, srcindex, array, destindex, length ) ;
            srcindex += data.w ;
            destindex += w ; 
        }
        
        return new ImageData( w, h, array, data.getColorModel() ) ;
    }
    
    /** Make an array of tiles from an existing linear array of data. */    
    public ImageData[] makeTiles( ImageData data ) {
        int xsize = ( data.w - xstart ) / xstride ;
        int ysize = ( data.h - ystart ) / ystride ;

        // Ensure full rank array ;

        if ( !data.isFull() ) 
        {
            return null ;
        }
        
        // Zero case, check to see if only fullsized images are allowed. 
        if ( xsize == 0 || ysize == 0 ) {
            if ( fullsize ) 
               return new ImageData[0] ;
            if ( xsize == 0 ) {
               xsize = 1 ;   
            }
            if ( ysize == 0 ) {
               ysize = 1 ;   
            }
        }
        
        ImageData[] result = new ImageData[ xsize * ysize ] ;
        int x = xstart ;
        int y ;
        int k = 0 ;
        
        for ( int i=0;i<xsize;i++) {
           y = ystart ;
           for ( int j=0;j<ysize;j++) {
              result[k++] = makeTile( data, x, y, w, h ) ;  // Actually make the tile.  Fill in the
                                                      // background with the background color
              y += ystride ;
           }
           x += xstride ;
        }
        
        return result ;
    }
    
    int w, h ;
    int xstart = 0, ystart = 0;
    int xstride, ystride ;
    
    /** Guarantees all images are fullsized. */
    boolean fullsize ;
    Color background = Color.black ;
    
    /** Test program for this class. */
    public static void main( String[] argv ) {
    File file = null ;
	
	if ( argv.length > 0 ) {
	   file = new File( argv[0] );	
	}
    else {
	  System.out.println("Usage: ImageTiler <filename>");	
	  System.exit(0);
    } 

    if ( !file.exists() ) {
   	  System.out.println("File " + file + " does not exist.");
      System.exit(0);
    }

    try {
    FileInputStream fis = new FileInputStream( file ) ;

    ImageData data = PPMBitmap.read( fis ) ;

    ImageTiler tiler = new ImageTiler( 128, 128, 64, 64, true ) ;

    ImageData[] tiles = tiler.makeTiles( data ) ;
    }
    catch (IOException e ) {
        e.printStackTrace() ;
    }

    }
}