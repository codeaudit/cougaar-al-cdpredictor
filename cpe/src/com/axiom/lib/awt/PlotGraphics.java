package com.axiom.lib.awt ;
import java.awt.Graphics ;
import java.awt.geom.* ;
import com.axiom.lib.util.ArrayMath ;
import java.awt.*;
import java.awt.event.*;
/**
 *  Simple class for plotting two-dimensional graphics.  This class really awaits
 *  a full-fledged Java2D implementation for things like line width, stroke properties, etc.
 *  At that time, a PlotGraphics2D class will be implemented.
 */
public class PlotGraphics {
    public final static int DEFAULT_X_BORDER = 50;
    public final static int DEFAULT_Y_BORDER = 50;
    public final static int DEFAULT_WIDTH = 320 ;
    public final static int DEFAULT_HEIGHT = 240 ;
    
    //
    // Line styles
    //
    public final static int SOLID_STYLE = 0;
    public final static int DOTTED_STYLE = 1;
    public final static int DASHED_STYLE = 2;
    
    public final static int CIRCLE_SYMBOL = 0 ;
    public final static int PLUS_SYMBOL   = 1;
    public final static int CROSS_SYMBOL  = 2;
    public final static int TRIANGLE_SYMBOL = 3;
    public final static int SQUARE_SYMBOL = 4;
    
    public final static int DRAW_BOX    = 0x0001 ;
    public final static int DRAW_TICKS  = 0x0002 ;
    public final static int DRAW_SCALE  = 0x0004 ;
    public final static int DRAW_XLABEL = 0x0008 ;
    public final static int DRAW_YLABEL = 0x0010 ;
    public final static int DRAW_TITLE  = 0x0020 ;
    public final static int DRAW_GRID   = 0x0040 ;
    public final static int DRAW_XSCALE = 0x0080 ;
    public final static int DRAW_YSCALE = 0x0100 ;
    
    public PlotGraphics() {   
    }
    
    public void setXLabel( String label ) {
        xLabel = label ;
    }
    
    public void setYLabel( String label ) {
        yLabel = label ;    
    }
    
    public void setTitle( String title ) {
        this.title = title ;
    }
    
    public void clearAll() {
        xLabel = yLabel = title = null ;    
    }
    
    /**
     *  Set the bounds for the box.  The values are in the plot coordinate space, which
     *  is the coordinate space of the data being plotted.  As a result, the plot
     *  bounds is the "window" through which the plot looks as the data.
     */
    public void setPlotBounds( double x, double y, double w, double h ) {
        this.x = x ;
        this.y = y ;
        width = w ;
        height = h ;
    }
    
    public void setPlotBounds( Rectangle2D rect ) {
        this.x = rect.getX() ;
        this.y = rect.getY() ;
        width = rect.getWidth() ;
        height = rect.getHeight() ;
    }
    
    /**
     *  Set the window coordinate bounds for the plot area. All bounds are measured in the
     *  default units.  Currently, all units are pixels.
     */
    public void setWindowBounds( double xPlot, double yPlot, double plotWidth, double plotHeight ) {
        this.xPlot = xPlot ;
        this.yPlot = yPlot ;
        this.plotWidth = plotWidth ;
        this.plotHeight = plotHeight ;
    }

    /**
     *  Set the window coordinate bounds for the plot area. All bounds are measured in 
     *  the default units.  Currently, only pixel units are supported.
     */
    public void setWindowBounds( Rectangle2D rect ) {
        this.xPlot = rect.getX() ;
        this.yPlot = rect.getY() ;
        this.plotWidth = rect.getWidth() ;
        this.plotHeight = rect.getHeight() ;
    }
    
    public static Point2D normalizeBounds( Rectangle2D r, Rectangle2D dest ) {
       return normalizeBounds( r.getX(), r.getY(), r.getWidth(), r.getHeight(), dest ) ;   
    }
        
    /**
     * Method for computing more usable bounds for a graph, falling on the boundary of
     * an integral number of base 10 units.
     * 
     * @param x  X coordinate of input bounds 
     * @param y  Y coordinate of input bounds
     * @param w  Width of input bounds
     * @param h  Height of input bounds
     * @param dest  Target for normalized bounds
     * @return A point containing the x and y "tick" scales.
     */
    public static Point2D normalizeBounds( double x, double y, double w, double h, Rectangle2D dest ) {
        double logwidth = Math.log( w ) / Math.log( 10 );
        double logheight = Math.log( h ) / Math.log( 10 );
        
        double scaleWidth = Math.ceil( logwidth );
        double scaleHeight = Math.ceil( logheight );
        // Calculate log-scaled w and h between 0.1 and 1.0 
        double logScaledWidth = w / Math.pow( 10, scaleWidth ) ;
        double logScaledHeight = h / Math.pow( 10, scaleHeight );
        double numTicks = 10 ;
        
        boolean foundX = false , foundY = false ;
        double tickX = 0 , lowerX, upperX, tickY = 0 , lowerY, upperY ; 
        // Try satisfying the tick count for 0.01, 0.02, 0.025, 0.05, 0.1, 0.2.
        
        for (int i=0;i<ticks.length;i++) {
           if ( logScaledWidth / ticks[i] < numTicks ) {
              tickX = ticks[i] * Math.pow( 10, scaleWidth ) ;
              foundX = true ;
              break ;
           }
        }
        
        for (int i=0;i<ticks.length;i++) {
           if ( logScaledHeight / ticks[i] < numTicks ) {
              tickY = ticks[i] * Math.pow( 10, scaleHeight ) ;
              foundY = true ;
              break ;
           }
        }
                
        lowerX = Math.floor( x / tickX ) * tickX ;
        upperX = Math.ceil( ( x + w ) / tickX ) * tickX;
        lowerY = Math.floor( y / tickY ) * tickY ;
        upperY = Math.ceil( ( y + h ) / tickY ) * tickY ;
        
        dest.setRect( lowerX, lowerY, upperX - lowerX, upperY - lowerY ) ;
        
        return new Point2D.Double( tickX, tickY ) ;
    }
    
    /** Draw the horizontal label on the appropriate location on the current plot rectangle.
     *  The position of the x label is always relative to plot area window bounds.
     */
    public void drawXLabel( Graphics g, String label ) {
        if ( label == null || label.length() == 0 )
           return ;
        g.setColor( foregroundColor ) ;

        FontMetrics fm = g.getFontMetrics() ; // Font metrics assoc. with g.
        int textWidth = fm.stringWidth( label );
        int height = fm.getHeight() ;
        int xpos = (int) ( xg + widthg / 2 - textWidth / 2f ) ;
        int ypos = (int) ( yg + heightg + height * 2 );
        g.drawString( label, xpos, ypos );
    }
    
    /**
     *  Currently, the y label is not rotated counterclockwise by 90 degrees due to
     *  lack of support in Graphics.  As a result, the y label may not be clipped properly.
     *
     */
    public void drawYLabel( Graphics g, String label ) {
        if ( label == null || label.length() == 0 )
           return ;
        g.setColor( foregroundColor ) ;

        FontMetrics fm = g.getFontMetrics() ; // Font metrics assoc. with g.  Should be replaced.
        int textWidth = fm.stringWidth( label );
        int height = fm.getHeight() ;
        int xpos = (int) ( xg - textWidth * 2 ) ;
        int ypos = (int) ( yg + heightg / 2 );
        g.drawString( label, xpos, ypos );        
    }
    
    /**
     *  Legacy method for plotting arrays.
     */
    public static void plot( Graphics g, int x, int y, int w, int h, float[] array, Color color ) {
        g.setColor( color );
        int upperx = x + w ;
        int uppery = y + h ;
        float maxValue = ArrayMath.max( array );
        float divPerElement = ( float ) w / ( float ) ( array.length - 1 );
        float yScale = ( float ) h / ( float ) maxValue ;
        
        g.drawRect( x, y, w, h );
        int[] xPoints = new int[ array.length ];
        int[] yPoints = new int[ array.length ];
        for (int i=0;i<array.length;i++) {
            xPoints[i] = x + (int) ( divPerElement * i );
            yPoints[i] = uppery - (int) ( array[i] * yScale );
        }
        g.drawPolyline( xPoints, yPoints, xPoints.length );
    }
        
    /**
     *  Plots an entire window.  This calculates the variables xg, yg, widthg, and heightg.
     *
     *
       <pre>
                   
                             <Border>
                               Title
                              <Space>
                 xg,yg-------------------------------
                    |                                |
                    |                                |
                    |                                |
   YLabel  YScale   |                                | <Border>
                    |                                |
                    |                                |
                    ----------------------------------
                                 <Space>
                                 XScale
                                 <Space>
                                 XLabel
                                 <Border>
       </pre>
     *
     *
     */
    
    public synchronized void plot( Graphics g, float[] array ) {
        double xTitle = 0 , yTitle = 0 ;
        double xXLabelPos = 0 , yXLabelPos = 0 ;
        double xYLabelPos = 0 , yYLabelPos = 0 ;
        double plotAreaWidth = this.plotWidth, plotAreaHeight = this.plotHeight ;
        FontMetrics fm = g.getFontMetrics( );
        double border = 12 ;
        double space = 5 ;
        double tickSize = 4 ;
        
        //
        // Process vertical layout
        //
        
        double offset = border ;  // Offset from xPlot and yPlot
        plotAreaHeight -= border ;
        
        if ( ( flags & DRAW_TITLE ) != 0 && title != null ) {
            plotAreaHeight -= fm.getHeight() + space;
            offset += fm.getHeight() + space ;
            yTitle = space + fm.getHeight() ;
        }

        yg = offset ;
        
        if ( ( flags & DRAW_XSCALE ) != 0 ) {
            plotAreaHeight -= fm.getHeight() + space + tickSize ;
        }
        
        if ( ( flags & DRAW_XLABEL ) != 0 && xLabel != null ) {
            yXLabelPos = this.plotHeight - space - fm.getHeight() + yPlot ;
            plotAreaHeight -= fm.getHeight() + space;
        }
        
        if ( ( flags & DRAW_YLABEL ) != 0 && yLabel != null ) {
            yYLabelPos = yg + ( plotAreaHeight / 2 ) - fm.getHeight() / 2.0 ; 
        }
        
        // The bottom-most border
        plotAreaHeight -= border ;
        heightg = plotAreaHeight ;
        
        //
        // Process horizontal layout.
        //
        offset = border ;
        plotAreaWidth -= border ;
        
        // Layout Y Label
        if ( ( flags & DRAW_YLABEL ) != 0 && yLabel != null ) {
            double temp = fm.stringWidth( yLabel ) + space ;
            plotAreaWidth -= temp ;
            offset += temp ;
            xYLabelPos = xPlot + border ;
        }
                
        // Y-Scale and ticks  Not sure how wide to make the scale, so just
        // improvise
        if ( ( flags & DRAW_YSCALE ) != 0 ) {
            double temp = space + tickSize + fm.charWidth( '0' ) * 3 ;
            offset += temp ;
            plotAreaWidth -= temp ;
        }
        
        // Update graph location
        xg = offset ;
        
        plotAreaWidth -= border ; 
        
        widthg = plotAreaWidth ;

        if ( ( flags & DRAW_XLABEL ) != 0 && xLabel != null) {
            int temp = fm.stringWidth( xLabel ) ;
            xXLabelPos = xg + ( widthg / 2.0 ) - temp / 2.0 ;
        }
        
        if ( ( flags & DRAW_TITLE ) != 0 && title != null ) {
            int temp = fm.stringWidth( title ) ;
            xTitle = xg + ( widthg / 2.0 ) - temp / 2.0 ;            
        }
        
        //
        // Having layed everything out, we actually go ahead and plot here.
        //
                
        if ( ( flags & DRAW_BOX ) != 0 ) {
            drawBox( g ) ;
        }

        plotArray( g, array ) ;
        
        if ( ( flags & DRAW_TITLE ) != 0 && title != null ) {
            g.drawString( title, ( int ) xTitle, ( int ) yTitle ) ;
        }
        
        if ( ( flags & DRAW_XLABEL ) != 0 && xLabel != null) {
            g.drawString( xLabel, (int) xXLabelPos, (int) yXLabelPos ) ;
            //drawXLabel( g, xLabel );
        }
        
        if ( ( flags & DRAW_YLABEL ) != 0 && yLabel != null ) {
            g.drawString( yLabel, (int) xYLabelPos, (int) yYLabelPos ) ;
            // drawYLabel( g, yLabel );
        }
        
        drawScale( g, 1, 1 ) ;
    }
    
    /**
     *  Plot a single array using the current parameters.
     */  
    public synchronized void plotArray( Graphics g, float[] array ) {
        double xScale = widthg / width ;
        double yScale = heightg / height ;
        double upperxg = xg + widthg ;
        double upperyg = yg + heightg ; 

        g.setColor( foregroundColor ) ;
        java.awt.Shape s = g.getClip();
        g.setClip( (int) xg, (int) yg, (int) widthg, (int) heightg ) ;
        int[] xPoints = new int[ array.length ];
        int[] yPoints = new int[ array.length ];
        for (int i=0;i<array.length;i++) {
            xPoints[i] = (int) ( xg + ( i - x ) * xScale ) ;
            yPoints[i] = (int) ( upperyg - ( array[i] - y ) * yScale );
        }
        g.drawPolyline( xPoints, yPoints, xPoints.length );
        g.setClip( s ) ;
    }

    /**
     *  Plot a single array using the current PlotGraphics parameters.
     */  
    public void plotArray( Graphics g, double[] array ) {
        double xScale = widthg / width ;
        double yScale = heightg / height ;
        double upperxg = xg + widthg ;
        double upperyg = yg + heightg ; 

        g.setColor( foregroundColor ) ;
        java.awt.Shape s = g.getClip();
        g.setClip( (int) xg, (int) yg, (int) widthg, (int) heightg ) ;
        int[] xPoints = new int[ array.length ];
        int[] yPoints = new int[ array.length ];
        for (int i=0;i<array.length;i++) {
            xPoints[i] = (int) ( xg + ( i - x ) * xScale ) ;
            yPoints[i] = (int) ( upperyg - ( array[i] - y ) * yScale );
        }
        g.drawPolyline( xPoints, yPoints, xPoints.length );
        g.setClip( s ) ;
    }
        
    /**
     * Draw the box at the current plot position.
     */
    public void drawBox( Graphics g ) {
        g.setColor( foregroundColor ) ;
        g.drawRect( (int) xg, (int) yg, (int) widthg, (int) heightg );        
    }
    
    /**
     *  Draw scales, including labels.
     *  @param unitsPerTick  The number of units per large tick mark.
     */
    public void drawScale( Graphics g, float unitsPerTickX, float unitsPerTickY ) {
        
        // Make it simple and just draw the stuff at the 
        // Draw x scale
        
        FontMetrics fm = g.getFontMetrics() ;
        String s = Double.toString( x );
        int xpos = ( int ) ( xg - fm.stringWidth( s ) / 2.0 ) ;
        int ypos = ( int ) ( yg + heightg + fm.getHeight() * 1.1 ) ;
        g.drawString( s, xpos, ypos );
        
        s = Double.toString( x + width ) ;
        xpos = ( int ) ( xg + widthg - fm.stringWidth( s ) / 2.0 ) ;
        ypos = ( int ) ( yg + heightg + fm.getHeight() * 1.1 ) ;
        g.drawString( s, xpos, ypos );
        
        // Draw y scale
        
        s = Double.toString( y ) ;
        xpos = ( int ) ( xg - fm.stringWidth( s ) ) ;
        ypos = ( int ) ( yg + heightg - fm.getHeight() / 2.0 ) ;
        g.drawString( s, xpos, ypos );
        
        s = Double.toString( y + height ) ;
        xpos = ( int ) ( xg - fm.stringWidth( s ) );
        ypos = ( int ) ( yg + fm.getHeight() / 2.0 ) ;
        g.drawString( s, xpos, ypos );
    }
    
    public void clear( Graphics g ) {
        Rectangle c = g.getClipBounds();
        g.setColor( backgroundColor ) ;
        g.fillRect( c.x, c.y, c.width, c.height );
    }
    
    /**
     *  Automatically scale to the min and max data values.
     */
    protected boolean autoscale = false ;

    protected int lineStyle = SOLID_STYLE ;

    protected int plotSymbol = CIRCLE_SYMBOL ;

    protected int flags = 0xFFFF ;

    /**
     *  Floating point coordinates in the world coordinates.
     *
     */
    protected double x = 0, y = 0, width = 1, height = 1;

    /**
     *  Title of graph.
     */
    protected String title ;

    /**
     *  String on x label.
     */
    protected String xLabel ;

    /**
     *  String on y label.
     */
    protected String yLabel ;

    /** Subrectangle of the plot rectangle which contains the actual graph.)
     */
    protected double xg, yg, widthg, heightg ;

    /**
     *  Window location of the composite plot (title, scales, etc)
     */
    protected double xPlot = DEFAULT_X_BORDER, yPlot = DEFAULT_Y_BORDER, plotWidth = DEFAULT_WIDTH, plotHeight = DEFAULT_HEIGHT;

    public Color foregroundColor = Color.black ;

    public Color backgroundColor = Color.white ;

    private static double ticks[] = { 0.01, 0.02, 0.025, 0.05, 0.1, 0.2 };

   /** Test program.
    */
    public static void main( String[] argv ) {
       Frame frame = new Frame("PlotGraphicsTest");
       frame.setSize( 500, 400 );
       frame.add( new TestPanel() );
       frame.addWindowListener( new WindowAdapter() { public void windowClosing( WindowEvent e ) {
                                     System.exit(0) ; } } );
       frame.setVisible( true );
    }
}

class TestPanel extends java.awt.Canvas {
    public TestPanel() {
        ypoints = new float[65];
        for (int i=0;i<ypoints.length;i++) {
           ypoints[i] = (float) Math.sin( 2 * Math.PI * ( ( float) i / ( ypoints.length - 1 ) ) );
        }
        plotGraphics = new PlotGraphics() ;
        
        Rectangle2D normBounds = new Rectangle2D.Double() ;
        Point2D tickSizes = PlotGraphics.normalizeBounds( 0, -1, ypoints.length - 1, 1.8, normBounds ) ;
        plotGraphics.setPlotBounds( normBounds ) ;
        plotGraphics.setXLabel( "x" ) ;
        plotGraphics.setYLabel( "sin(x)" ) ;
        plotGraphics.setTitle( "Plot of sin(x) vs x" ) ;
    }

    public void paint( Graphics g ) {
        plotGraphics.clear( g ) ;
        Rectangle r = getBounds() ;  // Bounds for this
        plotGraphics.setWindowBounds( 0, 0, r.width, r.height ) ;
        plotGraphics.plot( g, ypoints ) ;
        
        /**
        plotGraphics.drawBox( g ) ;
        plotGraphics.plotArray( g, ypoints );
        plotGraphics.drawScale( g, 1, 0.1f );
        plotGraphics.drawXLabel( g, "x" );
        plotGraphics.drawYLabel( g, "sin(x)" );
        */
    }
    
    float[] ypoints ;
        
    PlotGraphics plotGraphics ;
}