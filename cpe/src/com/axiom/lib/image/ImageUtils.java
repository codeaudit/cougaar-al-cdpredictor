package com.axiom.lib.image ;
import java.awt.* ;
import java.awt.image.* ;
import java.awt.color.* ;
import java.util.* ;

public abstract class ImageUtils {

    /**
     *  Smooth with a 3x3 Gaussian kernel.
     *
     *  @param arr An array of ARGB pels of size at least w * h.
     */
    public static void filterGaussian( int w, int h, int[] arr, float[] result ) {

        // Just do the interior of the data
        int index ;
        for (int i=1; i<h-1 ;i++) {
            index = i * w ;
            for (int j=1;j<w-1;j++) {
                ++index ;

                float value = 0 ;

                value += arr[index - w -1]*g[0][0];
                value += arr[index - w ] * g[1][0];
                value += arr[index - w +1] * g[2][0];
                value += arr[index - 1] * g[0][1];
                value += arr[index] * g[1][1];
                value += arr[index + 1] * g[1][2];
                value += arr[index + w - 1] *g[2][0];
                value += arr[index + w ] * g[2][1];
                value += arr[index + w + 1] * g[2][2] ;
                result[index] = value / sum ;
            }
        }

        // Fill the top and bottom rows
        for (int i=1;i<w-1;i++) {
            float value = 0 ;
            value += arr[i - 1] * g[0][1];
            value += arr[i ] * g[1][1];
            value += arr[i + 1] * g[1][2];
            value += arr[i + w - 1] *g[2][0];
            value += arr[i + w ] * g[2][1];
            value += arr[i + w + 1] * g[2][2] ;
            result[i] = value / sum1;

            value = 0 ;
            int j = arr.length - w + i ;
            value += arr[j - w -1]*g[0][0];
            value += arr[j - w ] * g[1][0];
            value += arr[j - w +1] * g[2][0];
            value += arr[j - 1] * g[0][1];
            value += arr[j] * g[1][1];
            value += arr[j + 1] * g[1][2];
            result[j] = value / sum1 ;
        }

        // Fill the leftmost and rightmost columns
        for (int i=1;i<h-1;i++) {
            int j = i * w ;
            float value = 0 ;

            value += arr[j - w ] * g[1][0];
            value += arr[j - w + 1] * g[2][0];
            value += arr[j ] * g[1][1];
            value += arr[j + 1] * g[1][2];
            value += arr[j + w ] * g[2][1];
            value += arr[j + w + 1] * g[2][2] ;
            result[j] = value / sum1 ;
            j = j + w - 1 ;

            value = 0 ;
            value += arr[j - w - 1] * g[0][0];
            value += arr[j - w ] * g[1][0];
            value += arr[j - 1] * g[1][0];
            value += arr[j ] * g[1][1];
            value += arr[j + w - 1] * g[2][0] ;
            value += arr[j + w ] * g[2][1];
            result[j] = value / sum1 ;
        }
    }

    /**
     *   Not yet implemented.
     */
    public static void scale( int w1, int h1, int[] arr, int w2, int h2, int[] result ) {

        if ( w2 * h2 != result.length ) {
            throw new RuntimeException( "Illegal target image parameter: w " + w1 + " h " + h1 +
                                        " bufsize " + result.length ) ;
        }

        float xscale = ( float ) w1 / ( float ) w2 ;
        float yscale = ( float ) h1 / ( float ) h2 ;

        for (int i=0;i<w2;i++) {
            for (int j=0;j<h2;j++) {
            
            }
        }

    }

    /** Convert standard img to a buffered image.
     */
    public static BufferedImage convertImage( Image img ) {
        int height = img.getHeight(null), width = img.getWidth(null) ;
        // FloatMatrix fm = new FloatMatrix( height, width ) ;
        PixelGrabber grabber = new PixelGrabber( img, 0, 0, width, height, false ) ;
        try {
        grabber.grabPixels() ;
        }
        catch ( InterruptedException e ) {
        System.out.println( e ) ;
        }
        Object pixels = grabber.getPixels() ;
        ColorModel cm = grabber.getColorModel() ;

        BufferedImage bimg = null ;

        // REVISIT  Makes some unwarranted assumptions about the layout of the PixelGrabber data
        // as being either int (ARGB?) or byte (gray scale?)
        if ( pixels instanceof int[] ) {
            int[] arr = ( int[] ) pixels ;
            byte[] barray = new byte[ arr.length * 3 ] ;
            int k = 0 ;
            for (int j=0;j<arr.length;j++) {
               int l = arr[j] ;
               barray[k++] = ( byte ) ( l & 0xFF ) ;
               barray[k++] = ( byte ) ( ( l >>> 8 ) & 0xFF ) ;
               barray[k++] = ( byte ) ( ( l >>> 16 ) & 0xFF );
            }
            ColorModel ccm = new ComponentColorModel( ICC_ColorSpace.getInstance( ICC_ColorSpace.CS_sRGB ),
                                                      new int[] { 8, 8, 8 }, false, false, Transparency.OPAQUE,
                                                      DataBuffer.TYPE_BYTE ) ;
            DataBuffer bbuf = new DataBufferByte( barray, barray.length ) ;
            SampleModel bmodel = new PixelInterleavedSampleModel( DataBuffer.TYPE_BYTE, width, height, 3,
                                     3 * width, new int[] { 2, 1, 0 } ) ;

            WritableRaster raster = Raster.createWritableRaster( bmodel, bbuf, new Point(0,0) ) ;
            bimg = new BufferedImage( ccm, raster, false, new Hashtable() ) ;
        }
        else if ( pixels instanceof byte[] ) { // Assume gray scale model?
            byte[] arr = ( byte[] ) pixels ;
            byte[] barray = new byte[ arr.length * 3 ] ;
            int k = 0 ;
            for (int j=0;j<arr.length;j++) {
               byte l = arr[j] ;
               barray[k++] = l ; barray[k++] = l; barray[k++] = l ;
            }
            ColorModel ccm = new ComponentColorModel( ICC_ColorSpace.getInstance( ICC_ColorSpace.CS_sRGB ),
                                                      new int[] { 8, 8, 8 }, false, false, Transparency.OPAQUE,
                                                      DataBuffer.TYPE_BYTE ) ;
            DataBuffer bbuf = new DataBufferByte( barray, barray.length ) ;
            SampleModel bmodel = new PixelInterleavedSampleModel( DataBuffer.TYPE_BYTE, width, height, 3,
                                     3 * width, new int[] { 2, 1, 0 } ) ;

            WritableRaster raster = Raster.createWritableRaster( bmodel, bbuf, new Point(0,0) ) ;
            bimg = new BufferedImage( ccm, raster, false, new Hashtable() ) ;
        }
        else {
            throw new RuntimeException("Unexpected data.") ;
        }
        return bimg ;
    }

    public static BufferedImage convertImageData( ImageData data ) {
        BufferedImage bimg = null ;
        int height = data.getHeight(), width = data.getWidth() ;
        Object pixels = data.getData() ;
        if ( pixels instanceof int[] && data.getElementWidth()==1) {
            int[] arr = ( int[] ) pixels ;
            byte[] barray = new byte[ arr.length * 3 ] ;
            int k = 0 ;
            for (int j=0;j<arr.length;j++) {
               int l = arr[j] ;
               barray[k++] = ( byte ) ( l & 0xFF ) ;
               barray[k++] = ( byte ) ( ( l >>> 8 ) & 0xFF ) ;
               barray[k++] = ( byte ) ( ( l >>> 16 ) & 0xFF );
            }
            ColorModel ccm = new ComponentColorModel( ICC_ColorSpace.getInstance( ICC_ColorSpace.CS_sRGB ),
                                                      new int[] { 8, 8, 8 }, false, false, Transparency.OPAQUE,
                                                      DataBuffer.TYPE_BYTE ) ;
            DataBuffer bbuf = new DataBufferByte( barray, barray.length ) ;
            SampleModel bmodel = new PixelInterleavedSampleModel( DataBuffer.TYPE_BYTE, width, height, 3,
                                     3 * width, new int[] { 2, 1, 0 } ) ;

            WritableRaster raster = Raster.createWritableRaster( bmodel, bbuf, new Point(0,0) ) ;
            bimg = new BufferedImage( ccm, raster, false, new Hashtable() ) ;
        }
        else if ( pixels instanceof byte[] && data.getElementWidth() == 1 ) { // Assume gray scale model?
            byte[] arr = ( byte[] ) pixels ;
            byte[] barray = new byte[ arr.length * 3 ] ;
            int k = 0 ;
            for (int j=0;j<arr.length;j++) {
               byte l = arr[j] ;
               barray[k++] = l ; barray[k++] = l; barray[k++] = l ;
            }
            ColorModel ccm = new ComponentColorModel( ICC_ColorSpace.getInstance( ICC_ColorSpace.CS_sRGB ),
                                                      new int[] { 8, 8, 8 }, false, false, Transparency.OPAQUE,
                                                      DataBuffer.TYPE_BYTE ) ;
            DataBuffer bbuf = new DataBufferByte( barray, barray.length ) ;
            SampleModel bmodel = new PixelInterleavedSampleModel( DataBuffer.TYPE_BYTE, width, height, 3,
                                     3 * width, new int[] { 2, 1, 0 } ) ;

            WritableRaster raster = Raster.createWritableRaster( bmodel, bbuf, new Point(0,0) ) ;
            bimg = new BufferedImage( ccm, raster, false, new Hashtable() ) ;
        }
        else {
            throw new RuntimeException("Unexpected data.") ;
        }
        return bimg ;
    }

    static float[][] g ;

    static float sum, sum1 ;

    static {
        g = new float[3][3] ;
        sum = 0 ;
        for (int i=0;i<3;i++) {
            for (int j=0;j<3;j++) {
                int x = i - 1 ;
                int y = j - 1 ;
                g[i][j] = ( float ) Math.exp( - ( x * x + y * y ) / ( 2.0 * 0.8 ));
                sum += g[i][j];
            }
        }

        sum1 = g[0][0] + g[0][1] + g[0][2] + g[1][0] + g[1][1] + g[1][2] ;
    }

}