package com.axiom.lib.math ;
import com.axiom.lib.image.* ;
import java.awt.* ;
import java.awt.image.* ;
import com.axiom.lib.awt.* ;
import com.axiom.lib.util.* ;
import javax.swing.* ;

class PlotPanel extends JPanel {
    
    public PlotPanel( float[] data ) {
        this.data = data ;
        p.setPlotBounds( 0, 0, data.length, ArrayMath.max( data ) + 10  ) ;
    }
    
    public void paint( Graphics g ) {
        Dimension d = getSize() ;
        g.setColor( Color.white ) ;
        g.fillRect( 0, 0, d.width, d.height ) ;
        p.plot( g, data ) ;
    }
    
    
    float[] data ;
    
    PlotGraphics p = new PlotGraphics() ;
}

public abstract class MathTests {

    public static void main( String[] args ) {

        ImageData data = null ;

        try {
          data = Bitmap.readImage( new java.io.File( "F:\\vistex\\test\\Tile.0001.003.pgm" ) );
        }
        catch ( java.io.IOException e ) {
            System.err.println( e ) ;
            e.printStackTrace() ;
        }
        catch ( BitmapFormatException e ) {
            System.err.println( e ) ;
            e.printStackTrace() ;
        }

        int[] rgbBuffer = new int[ data.getWidth() * data.getHeight() ] ;
        data.getARGB( rgbBuffer ) ;
        int[] gscale = Bitmap.grey( rgbBuffer ) ;

        int[] h = MathUtils.hist( gscale, 0, 255, 256 ) ;
        float[] hp = new float[ h.length ];
        for (int i=0;i<h.length;i++) {
            hp[i] = h[i] ;
        }

        // Normalize the histogram into a normalizing transform
        int[] htrans = MathUtils.histNorm( h, 256 ) ;
        float[] htransp = new float[ h.length ];
        for (int i=0;i<htrans.length;i++) {
            htransp[i] = htrans[i] ;
        }

        // Filter the gray scale data.
        float[] fdata = new float[ gscale.length ];
        com.axiom.lib.image.ImageUtils.filterGaussian( data.getWidth(), data.getHeight(), gscale, fdata ) ;

        int[] ffdata = new int[ fdata.length ];
        for (int i=0;i<ffdata.length;i++) {
            ffdata[i] = ( int ) fdata[i] ;
        }

        ImageData filtData = new ImageData( data.getWidth(), data.getHeight(), Bitmap.color( ffdata ) ) ;

        // Perform histogram-equalizing transform with floating pt data
        int[] tdata = new int[ rgbBuffer.length ] ;
        MathUtils.histTrans( fdata, tdata, htrans ) ;

        // Get the new histogram
        int[] hnew = MathUtils.hist( tdata, 0, 255, 256 ) ;
        float[] hnewf = new float[ hnew.length ];
        for (int i=0;i<hnew.length;i++) {
            hnewf[i] = hnew[i] ;
        }

        ImageData timgdata = new ImageData( data.getWidth(), data.getWidth(), Bitmap.color( tdata ) ) ;

        JFrame frame = new JFrame("Moose") ;
        frame.setSize( 400, 300 ) ;
        frame.getContentPane().setLayout( new BorderLayout() ) ;
        frame.getContentPane().add( new PlotPanel( hp ) ) ;
        frame.setVisible( true ) ;

        JFrame newHist = new JFrame("After Histogram Equalization");
        newHist.setSize( 400, 300 );
        newHist.getContentPane().setLayout( new BorderLayout() ) ;
        newHist.getContentPane().add( new PlotPanel( hnewf ) ) ;
        newHist.setVisible( true ) ;

        JFrame frame2 = new JFrame("Moose2") ;
        frame2.setSize( 400, 300 ) ;
        frame2.getContentPane().setLayout( new BorderLayout() );
        frame2.getContentPane().add( new PlotPanel( htransp ) ) ;
        frame2.setVisible( true ) ;

        Frame frame3 = new TestWindow( "Before", data.makeImage() ) ;
        frame3.setVisible( true ) ;

        Frame frame5 = new TestWindow( "Filtered", filtData.makeImage() ) ;
        frame5.setVisible( true ) ;

        Frame frame4 = new TestWindow( "After", timgdata.makeImage() ) ;
        frame4.setVisible( true ) ;

        // Try and scale the image

        Image before = data.makeImage() ;

        ImageFilter filter = new AreaAveragingScaleFilter( 468, 468 ) ;

        ImageProducer filterSource = new FilteredImageSource( before.getSource() , filter ) ;

        Image scaledImage = Toolkit.getDefaultToolkit().createImage( filterSource ) ;
        
        Frame frame6 = new TestWindow( "Scaled Image", scaledImage ) ;
        frame6.setVisible( true ) ;

        int width = scaledImage.getWidth( null ) ;
        int height = scaledImage.getHeight( null ) ;
        
        PixelGrabber pg = new PixelGrabber( scaledImage, 0, 0, width, height,
                                           false ) ;

        boolean result ;

        try {
            result = pg.grabPixels() ;
        }
        catch ( InterruptedException e ) {
            System.err.println( e ) ;
            e.printStackTrace() ;
        }

        Object pels = pg.getPixels() ;

        if ( pels instanceof int[] ) {
            System.out.println( "Color image") ;
        }
        else if ( pels instanceof byte[] ) {
            System.out.println( "Byte image" ) ;
        }
    }


}