package com.axiom.lib.image;
import java.awt.Image ;
import java.awt.image.*;

/**
 * Generic image data structure.  Only a few formats are supported at this time.  This is
 * a class which will be supplanted by classes in JDK 1.2's java.awt.image package.
 *
 * Currently, arrays of int and byte are supported.
 * <p>
 * Color models supported include direct and indexed color models.  Both models are constrained to
 * produce pixels with less bits than bpp.
 * <p>
 * To be implemented: arrays of short and packed RGB byte arrays.
 */
public class ImageData {

	protected int w;
	protected int h;
    protected int elementWidth ;

    protected Object data ;
    protected ColorModel colorModel ;

    /**
     *  Create image data with default width per pel corresponding to size of primitive.  Object
     *  should be big enough to accomodate w * h pels.  Currently, only int[] and byte[] formats
     *  are supported.
     */
	public ImageData( int w, int h, Object data, ColorModel model ) {
        if ( !( data instanceof int[] ) && !( data instanceof byte[] ) ) {
           throw new IllegalArgumentException( "Data supported include int[] and byte[]." ) ;
        }
        this.w = w ;
        this.h = h ;
        elementWidth = 1 ;
        this.data = data ;
        this.colorModel = model ;
	}

    /**
    * Constructor for image data structure for full color (32bit)
    * images.
    *
    * @param w Width of image
    * @param h Height of image
    * @param data An array of integer values of length at least w * h.
    */
    public ImageData ( int w, int h, int[] data) {
        this.w = w ;
        this.h = h ;
        this.elementWidth = 1 ;
        this.data = data ;
        this.colorModel = ColorModel.getRGBdefault() ;
    }

    /**
    * Constructor for image data structure for gray-scale colormap.
    *  The ColorModel assumes that byte is a 8 bit gray intensity value.
    *  <p> REVISIT.
    *
    * @param w Width of image
    * @param h Height of image
    * @param data An array of integer values of length at least w * h.
    */
    public ImageData ( int w, int h, byte[] data) {
        this.w = w ;
        this.h = h ;
        this.elementWidth = 1 ;
        this.data = data ;
        this.colorModel = new DirectColorModel( 8, 255, 255, 255 ) ;
    }

    public Image makeImage() {
        if ( data == null || w <= 0 || h <=0 )
           return null ;

        Image image = null ;

        if ( data instanceof int[] && getElementWidth() == 1) {
            int[] arr = ( int[] ) data ;
            image = java.awt.Toolkit.getDefaultToolkit().createImage(
                         new MemoryImageSource( w, h, colorModel, arr, 0, w ) ) ;
        }
        else if ( data instanceof byte[] && getElementWidth() == 1 ) {
            byte[] arr = ( byte[] ) data ;
            image = java.awt.Toolkit.getDefaultToolkit().createImage(
                         new MemoryImageSource( w, h, colorModel, arr, 0, w ) ) ;
        }
        // Other combinations are not yet supported!
        else {
            throw new RuntimeException( "Unsupported data format." ) ;
        }

        return image ;
    }

    public static ImageData rescale( ImageData data, int w, int h ) {
        Image img = data.makeImage() ;
        AreaAveragingScaleFilter filter = new AreaAveragingScaleFilter( w, h ) ;
        FilteredImageSource src = new FilteredImageSource( img.getSource(), filter ) ;
        Image rescaled = java.awt.Toolkit.getDefaultToolkit().createImage( src ) ;
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


    public void getARGB( int[] array ) {
        if ( data instanceof int[] && getElementWidth() == 1 ) {
            int[] arr = ( int[] ) data ;
            for ( int i=0;i<array.length;i++) {
                array[i] = colorModel.getRGB( arr[i] ) ;
            }
        }
        else if ( data instanceof byte[] && getElementWidth() == 1 ) {
            byte[] arr = ( byte[] ) data ;
            for ( int i=0;i<array.length;i++) {
                array[i] = colorModel.getRGB( arr[i] ) ;
            }
        }
    }

    public boolean isFull() {
        if ( data instanceof int[] ) {
            int[] arr = ( int[] ) data ;
            return  arr.length >= getWidth() * getHeight() * getElementWidth() ;
        }
        else if ( data instanceof byte[] ) {
            byte[] arr = ( byte [] ) data ;
            return arr.length >= getWidth() * getHeight() * getElementWidth() ;
        }
        throw new RuntimeException("Huh? Data is not valid." ) ;
    }

    public ColorModel getColorModel() { return colorModel ; }

    public Object getData() { return data ; } 

    public int getHeight() { return h ; }
    
    public int getWidth() { return w ; }

    public int getElementWidth() { return elementWidth ; }

    public static void main( String[] args ) {

        int w = 256, h = 256 ;
        int[] b = new int[ w * h ] ;

        int count = 0 ;
        for (int i=0;i<w;i++) {
            for (int j=0;j<w;j++) {
                b[count++] = i ;
                b[count++] = j ;
            }
        }
        DirectColorModel cm = new DirectColorModel( 16, 0, 255 << 8, 255, 255 << 24 ) ;

        Image img = java.awt.Toolkit.getDefaultToolkit().createImage(
                       new MemoryImageSource( w, h, b, 0, w ) ) ;

        TestWindow tw = new TestWindow( "Moose", img ) ;
        tw.setSize( 128, 128 ) ;
        tw.setVisible( true ) ; 

    }
}
