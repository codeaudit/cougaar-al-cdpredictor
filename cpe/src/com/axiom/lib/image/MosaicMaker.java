package com.axiom.lib.image ;
import java.awt.* ;
import java.awt.image.* ;
import java.io.* ;
import com.axiom.lib.io.* ;
import java.util.* ;

public class MosaicMaker {

    /**
     *  @param images An array of images
     *  @param numColumns Number of columns
     *  @param tileSize Size of each square tile
     */
    public MosaicMaker( ImageData[] images, int numColumns, int tileSize ) {
        if ( numColumns <= 0 ) throw new IllegalArgumentException( "numColumns must be > 0" ) ;
        if ( tileSize <= 0 ) throw new IllegalArgumentException( "tileSize must be > 0" ) ;
        this.images = images ;
        this.numColumns = numColumns ;
        this.tileSize = tileSize ;
    }

    public ImageData makeMosaic() {
        int numRows = ( int ) Math.ceil( ( float ) images.length / ( float ) numColumns ) ;

        byte[] data = new byte[ tileSize * tileSize * numColumns * numRows ] ;
        for (int i=0;i<images.length;i++) {
           ImageData tmpData = rescale( images[i], tileSize, tileSize ) ;
           Object pixels = tmpData.getData() ;

           // Convert to byte data
           if ( pixels instanceof int[] ) {
              int[] srctmp = ( int[] ) pixels ;
              byte[] tmp = new byte[ tileSize * tileSize ] ;
              for (int j=0;j<srctmp.length;j++) {
                  tmp[j] = Bitmap.grey( srctmp[j] ) ;
              }
              pixels = tmp ;
           }

           int columnIndex = i % numColumns ;
           int rowIndex = (int) Math.floor( ( float ) i / ( float ) numColumns  ) ;
           int destoff= rowIndex * tileSize * tileSize * numColumns + columnIndex * tileSize ;
           System.out.println( "Row " + rowIndex + " Column " + columnIndex + " Destination offset: " + destoff ) ;

           //  Blt into location
           if ( pixels instanceof byte[] ) {
              int srcoff = 0 ;
              for (int j=0;j<tileSize;j++) {
                  System.arraycopy( pixels, srcoff, data, destoff, tileSize ) ;
                  srcoff += tileSize ;
                  destoff += tileSize * numColumns ;
              }
           }
           
        }

        // Return a grayscale image
        return new ImageData( tileSize * numColumns, tileSize * numRows, data ) ;
    }

    private static ImageData rescale( ImageData data, int w, int h ) {
        Image img = data.makeImage() ;
        AreaAveragingScaleFilter filter = new AreaAveragingScaleFilter( w, h ) ;
        FilteredImageSource src = new FilteredImageSource( img.getSource(), filter ) ;
        Image rescaled = Toolkit.getDefaultToolkit().createImage( src ) ;
        PixelGrabber grabber = new PixelGrabber( rescaled, 0, 0, w, h, false ) ;
        try {
           grabber.grabPixels() ;
        }
        catch ( InterruptedException e ) {
            System.out.println( e ) ;
        }
        Object pixels = grabber.getPixels() ;
        ColorModel cm = grabber.getColorModel() ;
        return new ImageData( w, h, pixels, cm ) ;
    }

    public static final void main( String[] args ) {
        if ( args.length < 0 ) {
            System.out.println( "Usage: src dest filter");
            return ;
        }

        String src = args[0] ;
        String dest = args[1] ;
        String filterString = args[2] ;

        File srcDest = new File( src ) ;
        if ( !srcDest.isDirectory() ) {
            System.out.println( "Source " + srcDest + " is not a directory." ) ;
        }

        Vector images = new Vector() ;
        System.out.println( "Converting from " + src + " to " + dest + " using " + filterString) ;
        StandardFilenameFilter filter = new StandardFilenameFilter(filterString) ;
        String[] names = srcDest.list() ;
        for (int i=0;i<names.length;i++) {
            if ( filter.accept( null, names[i] ) ) {
                File tfile = new File( srcDest, names[i] ) ;
                ImageData tmpData ;
                try {
                   tmpData = Bitmap.readImage( tfile ) ;
                }
                catch ( Exception e ) {
                   System.out.println( e ) ;
                   continue ;
                }
                tmpData = rescale( tmpData, 64, 64 ) ;
                images.addElement( tmpData ) ;
            }
        }
        ImageData[] id = new ImageData[ images.size() ] ;
        for (int i=0;i<images.size();i++) {
          id[i] = ( ImageData ) images.elementAt(i);
        }
        
        MosaicMaker maker = new MosaicMaker( id, 10,
                                            64 ) ; // Number of columns
        ImageData result = maker.makeMosaic() ;
        try {
           PGMBitmap bmp = ( PGMBitmap ) PGMBitmap.getImpl();
           bmp.write( new FileOutputStream( dest ), result );
        }
        catch ( Exception e ) {
            System.out.println( e ) ;
        }

        TestWindow window = new TestWindow( "Moose", result ) ;
        window.setVisible( true );
    }

    ImageData[] result ;
    ImageData[] images ;
    /** Bits per pixel.  Defaults to 8. */
    int bpp = 8 ;
    int numColumns, tileSize ;
}