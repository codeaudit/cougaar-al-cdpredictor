package com.axiom.lib.plot ;
import java.util.Enumeration ;
import com.axiom.lib.util.* ;
import com.axiom.lib.util.ArrayMath;
import com.axiom.lib.util.NumberArray;

import java.awt.geom.* ;
import java.util.ArrayList ;
import java.awt.* ;
import java.text.* ;
import java.util.* ;

/**
 *  A simple "retained" mode plot context for rendering plots to a standard
 *  AWT graphics context.
 */

public class Plot2DContext {

    /**
     * "Matrix" mode.
     */
    public final static int AXES_IJ = 0 ;

    /**
     * "Normal" mode.
     */
    public final static int AXES_NORMAL = 1 ;

    public final static String BACKGROUND_COLOR_PROPERTY = "BackgroundColorProperty" ;

    public final static String FOREGROUND_COLOR_PROPERTY = "ForegroundColorProperty" ;

    public final static String COLOR_PROPERTY = "ColorProperty" ;

    public final static String LINE_STYLE_PROPERTY = "LineStyle" ;

    public final static String LINE_STYLE_SOLID = "Solid" ;

    public final static String LINE_STYLE_DOTTED = "Dotted" ;

    public final static String LINE_STYLE_DASHED = "Dashed" ;

    public static final String LINE_STYLE_DOT_DASH = "DotDashed" ;

    private static final float[] DOTTED_STROKE = { 3.0f, 3.0f } ;

    private static final float[] DASHED_STROKE = { 6.0f, 3.0f } ;

    private static final float[] DOTDASH_STROKE = { 3.0f, 3.0f, 6.0f, 3.0f } ;

    public Plot2DContext() {
        font = new Font( "Sans Serif", Font.PLAIN, 12 ) ;
        fm = Toolkit.getDefaultToolkit().getFontMetrics( font ) ;

        setProperty( BACKGROUND_COLOR_PROPERTY, Color.white ) ;
        setProperty( FOREGROUND_COLOR_PROPERTY, Color.black ) ;
    }

    public void setProperty( String key, Object o ) {
        try {
        properties.put( key, o ) ;
        }
        catch ( Exception e ) {
            throw new IllegalArgumentException( "Key is null." ) ;
        }
    }

    public Object getProperty( String key ) {
        try {
        return properties.get( key ) ;
        }
        catch ( Exception e ) {
            return null ;
        }
    }

    public Object removeProperty( String key ) {
        return properties.get( key ) ;
    }

    // AxisModel getXAxisModel() ;
    // AxisModel getYAxisModel() ;

    public boolean getDrawXScale() {
        return drawXScale ;
    }

    public void setDrawXScale( boolean state ) {
        drawXScale = state ;
    }

    public boolean getDrawYScale() {
        return drawXScale ;
    }

    public void setDrawYScale( boolean state ) {
        drawYScale = state ;
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

    /**
     *  Sets the axis mode to <code>AXIS_AUTO</code> or <code>AXIS_MANUAL</code>.
     *  In manual mode, the axis bounds are used, otherwise, the min and max
     *  values for all plot objects being displayed are used.
     *
     *  TODO Actually implement this!  Right now, nothing happens.
     */
    public void setAxisManual( boolean value ) {
        this.axisManual = value ;

        // Fire events?  Not sure whether we want a push or pull model
        // Right now we have a push model, e.g. the owner calls plot to
        // render into a graphics.
    }

    public boolean getAxisManual() {
        return axisManual ;
    }

    /**
     *  Set the current axis transform in one of LEFTHAND_XY_TRANSFORM,
     *  RIGHTHAND_XY_TRANSFORM, MATRIX_TRANSFORM, or ROW_COLUMN_TRANSFORM.
     */
    public void setAxisType( int type ) {
        switch ( type ) {
            case AXES_IJ :
            case AXES_NORMAL :
            this.axisType = type ;
            break ;
            default :
            break ;
        }
    }

    /**
     *  Set the axis bounds for display.  Ignored if the current axis mode is
     *  <code>AXIS_AUTO</code>.
     */
    public void setAxisBounds( double x, double y, double w, double h ) {
        raxis.setRect( x, y, w, h ) ;
        updateTransform() ;
    }

    public Rectangle2D getAxisBounds() {
        return ( Rectangle2D ) raxis.clone() ;
    }

    /**
     *  Set window size in device coordinates.
     */
    public void setPlotBounds( float x, float y, float w, float h ) {
        rplot.setRect( x, y, w, h ) ;
        updateTransform() ;
        doLayout() ;
    }

    public void setPlotBounds( Rectangle2D r ) {
        rplot.setRect( r );
        updateTransform();
        doLayout();
    }

    /** Get the current plot boundary.
     */
    public Rectangle2D getPlotBounds() {
        return ( Rectangle2D ) rplot.clone() ;
    }

    /** Get the area actually used for plotting, e.g. not including
     * scales, borders, axis lables, etc.
     */
    public Rectangle2D getPlotArea() {
        return this.getPlotArea() ;
    }

    public AffineTransform getPlotTransform() {
        return plotTransform ;
    }

    public Enumeration getPlotObjects() {
        return null ;
    }

    /**
     *  Add some arbitrary plot object and updates the current xform.
     */
    public void addPlottable( Plottable p ) {
        plotObjects.add( p ) ;
        this.updateTransform() ;
    }

    public boolean removePlottable( Plottable p ) {
        return plotObjects.remove( p ) ;
    }

    public void removeAllPlottable() {
        plotObjects.clear() ;
    }

    /**
     *  Plot n as a function of index.
     */
    public Plottable addPlot( NumberArray n ) {

        Plottable result = new ArrayPlot( n ) ;
        addPlottable( result ) ;
        return result ;
    }


    /**
     *  Convenience method for plotting y as a function of x.
     *
     *  @return A new <code>PlotObject</code>.
     */
    public Plottable addPlot(NumberArray x, NumberArray y) {

        Plottable result = new ArrayPlot( y, x ) ;
        addPlottable( result ) ;
        return result ;
    }

    public Plottable addBarChart( int[] x ) {
        throw new UnsupportedOperationException( "Not supported." ) ;
    }

    public Plottable addPsuedoColorPlot( FloatMatrix fm ) {
        throw new RuntimeException( "Not supported.") ;
        // return null ;
    }

    /**
     *  Pseudocolor plot of floating point matrix.
     */
    public Plottable addPseudoColorPlot( FloatMatrix fm, int xslice, int yslice, int[] sliceIndex ) {
        throw new RuntimeException( "Not supported.") ;
    }

    /**
     *  Pseudocolor plot of integer matrix.
     */
    public Plottable addPseudoColorPlot( IntMatrix fm ) {

        return null ;
    }

    /** Draw a symbol at screen coordinates x and y.  Not yet device independent.
     *  TODO Make the symbols device independent.
     */
    protected static void drawSymbol( Graphics g, int x, int y ) {
        g.drawLine( x - 5, y, x + 5, y ) ;
        g.drawLine( x, y - 5, x, y + 5 ) ;
    }

    /**
     *   Update the view transform and render to a simple graphics context.
     */
    public void plot( java.awt.Graphics g ) {
        Graphics2D g2 = ( Graphics2D ) g ;
        drawAxesScales(g) ;

        for (int i=0;i<plotObjects.size();i++) {
            Plottable p = ( Plottable ) plotObjects.get(i) ;
            Object prop = p.getProperty( COLOR_PROPERTY ) ;
            if ( prop instanceof Color ) {
                g.setColor( ( Color ) prop ) ;
            }
            else {
                Object o = getProperty( FOREGROUND_COLOR_PROPERTY ) ;
                if ( o == null || !( o instanceof Color ) ) {
                    Object o1 = getProperty( BACKGROUND_COLOR_PROPERTY ) ;
                    Color backColor = ( Color ) o1 ;
                    Color newColor = new Color( 0xFFFFFFFF ^ backColor.getRGB() ) ;
                    g.setColor( newColor );
                }
                else
                    g.setColor( ( Color ) o ) ;
            }

            g.setClip( plotArea ) ;
            //g.setClip( (int) plotArea.getX(), (int) plotArea.getY(),
            //           (int) plotArea.getWidth(), (int) plotArea.getHeight() ) ;

            // Handle arrays
            if ( p instanceof ArrayPlot ) {
                drawArrayPlot(p, g);
            }
            else {
                // The stroke is of width 1 in screen coordinates
                AffineTransform oldTransform = g2.getTransform() ;
                AffineTransform transform ;
                g2.setTransform( transform = getPlotTransform() );
                g2.setStroke( new BasicStroke((float) ( 1 /transform.getScaleX() ) ) );
                p.render( g2 );
                g2.setTransform( oldTransform );
            }
        }
    }

    private void drawArrayPlot(Plottable p, Graphics g) {
        Graphics2D g2 = (Graphics2D) g ;
        ArrayPlot ap = ( ArrayPlot ) p ;
        NumberArray x = ap.getXArray() ;
        NumberArray y = ap.getYArray() ;
        AffineTransform tx = getPlotTransform() ;
        Point2D src = new Point2D.Float(), dest = new Point2D.Float();
        int[] xp = new int[y.getSize()];
        int[] yp = new int[y.getSize()];

        Object prop = ap.getProperty( LINE_STYLE_PROPERTY ) ;
        BasicStroke stroke = null ;
        if ( prop == null || prop.equals(LINE_STYLE_SOLID)) {
            stroke = new BasicStroke( 1.0f ) ;
        }
        else if ( prop.equals(LINE_STYLE_DOTTED) ) {
            stroke = new BasicStroke( 1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 11f, DOTTED_STROKE, 0f ) ;
        }
        else if ( prop.equals(LINE_STYLE_DASHED) ) {
            stroke = new BasicStroke( 1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 11f, DASHED_STROKE, 0f ) ;
        }
        else if ( prop.equals(LINE_STYLE_DOT_DASH) ) {
            stroke = new BasicStroke( 1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 11f, DOTDASH_STROKE, 0f ) ;
        }

        g2.setStroke( stroke );

        // Compute transform
        if ( x == null ) { // Assume linear index starting from 0
           for (int j=0;j<y.getSize();j++) {
              src.setLocation( j, y.valueAt(j) ) ;
              tx.transform( src, dest ) ;
              xp[j] = ( int ) dest.getX() ;
              yp[j] = ( int ) dest.getY() ;
           }
        }
        else {
           for (int j=0;j<y.getSize();j++) {
              src.setLocation( x.valueAt(j), y.valueAt(j) ) ;
              tx.transform( src, dest ) ;
              xp[j] = ( int ) dest.getX() ;
              yp[j] = ( int ) dest.getY() ;
           }
        }

        String s = ( String ) ap.getProperty( ArrayPlot.STYLE_PROPERTY ) ;
        if ( s== null || s.equals( ArrayPlot.LINEPLOT_STYLE ) ) {
          g.drawPolyline( xp, yp, xp.length ) ;
          //for (int j=0;j<xp.length-1;j++) {
          //  g.drawPolyline(
          //  g.drawLine( ( int ) xp[j], (int) yp[j], (int) xp[j+1], (int) yp[j+1] ) ;
          //}
        }
        else {
             for (int j=0;j<xp.length;j++) {
                 drawSymbol( g, xp[j], yp[j] ) ;
             }
        }
    }

    public void fitToBounds() {
        Rectangle2D bounds = null ;

        for (int i = 0; i < plotObjects.size(); i++) {
            Plottable plottable = (Plottable)plotObjects.get(i);
            double xmin, width, ymin, height ;
            if ( plottable instanceof ArrayPlot ) {
                ArrayPlot ap = (ArrayPlot) plottable ;
                NumberArray yarr = ap.getYArray() ;
                if ( ap.getXArray() != null ) {
                    NumberArray xarr = ap.getXArray() ;
                    xmin = ArrayMath.min( xarr ) ;
                    width = ArrayMath.max( xarr ) - xmin ;
                }
                else {
                    xmin = 0; width = yarr.getSize() ;
                }
                ymin = ArrayMath.min( yarr ) ;
                height = ArrayMath.max( yarr ) - ymin ;

                Rectangle2D dest = new Rectangle2D.Double( xmin, ymin, width, height ) ;
                if ( bounds == null ) {
                    bounds = dest ;
                }
                else {
                    bounds = dest.createUnion( bounds ) ;
                }
            }
            else if ( plottable instanceof GeometryPlot ) { // Update with arbitrary geometry.
                GeometryPlot gp = (GeometryPlot) plottable ;
                if ( bounds == null ) {
                    bounds = gp.getBounds() ;
                }
                else {
                    bounds = bounds.createUnion( gp.getBounds() ) ;
                }
            }
        }

        // Last step is to normalize bounds.
        if ( bounds != null ) {
            normalizeBounds( bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight(), bounds ) ;
            setPlotBounds( bounds );
        }
    }

    /**
     *  Given a rectangle, this method finds "reasonable" bounds for plotting various values.
     *
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

    /**
     *  Draw a normal x scale.
     */
    protected void drawXScale( Graphics g ) {

        // Align the minimum tick on
        double mintick = Math.ceil( axis.getX() / tickSize.getX() ) * tickSize.getX() ;
        int numticks =
            (int) Math.floor( ( axis.getMaxX() - mintick ) / tickSize.getX() ) + 1;


        int factor = (int) Math.floor( Math.log( axis.getWidth() ) / Math.log(10 ) );
        double range = Math.max( Math.abs( axis.getX() ), Math.abs( axis.getMaxX() ) ) / axis.getWidth() ;
        int precision = (int) Math.abs( Math.floor( Math.log( range ) / Math.log(10) ) ) + 1;

        double divisor = 1 ;

        if ( ( factor >= 2 || factor <= -1 ) && precision <= 1) {
          divisor = Math.pow( 10, factor ) ;
        }

        DecimalFormat format = new DecimalFormat() ;
        format.setMaximumFractionDigits( precision ) ;
        format.setMinimumFractionDigits( precision ) ;

        double tickPos = mintick;
        Point2D src = new Point2D.Float() ;
        Point2D dest = new Point2D.Float() ;
        for( int i = 0; i < numticks ; i++ ) {
            src.setLocation( tickPos, 0 );
            plotTransform.transform( src, dest ) ;
            g.drawLine( (int) dest.getX(), ( int ) plotArea.getMaxY(),
                        (int) dest.getX(), ( int ) plotArea.getMaxY() - 5 ) ;
            String s= format.format( tickPos / divisor ).toString() ;
            int len = fm.stringWidth(s) ;
            g.drawString( s, (int) dest.getX() - len/2, ( int ) plotArea.getMaxY() + fm.getHeight() );

            // Go to next tick.
            tickPos += tickSize.getX() ;
        }

        // Draw exponent
        if ( divisor != 1 ) {
          g.drawString( "10", (int) plotArea.getMaxX() - 15,
                              (int) plotArea.getMaxY() + fm.getHeight() ) ;
          g.drawString( Integer.toString( factor ),
                      (int) plotArea.getMaxX() - 7, (int) plotArea.getMaxY() + fm.getHeight() /2 ) ;
        }
    }

    protected void drawYScale( Graphics g ) {
        // Align the minimum tick on
        double mintick = Math.ceil( axis.getY() / tickSize.getY() ) * tickSize.getY() ;
        int numticks =
            (int) Math.floor( ( axis.getMaxY() - mintick ) / tickSize.getY() ) + 1;


        int factor = (int) Math.floor( Math.log( axis.getHeight() ) / Math.log(10 ) );
        double range = Math.max( Math.abs( axis.getY() ), Math.abs( axis.getMaxY() ) ) / axis.getHeight() ;
        int precision = (int) Math.abs( Math.floor( Math.log( range ) / Math.log(10) ) ) + 1;

        double divisor = 1 ;

        if ( ( factor >= 3 || factor <= -3 ) && precision <= 1) {
          divisor = Math.pow( 10, factor ) ;
        }

        DecimalFormat format = new DecimalFormat() ;
        format.setMaximumFractionDigits( precision ) ;
        format.setMinimumFractionDigits( precision ) ;

        double tickPos = mintick;
        Point2D src = new Point2D.Float() ;
        Point2D dest = new Point2D.Float() ;
        for( int i = 0; i < numticks ; i++ ) {
            src.setLocation( 0, tickPos );
            plotTransform.transform( src, dest ) ;
            g.drawLine( (int) plotArea.getX(), ( int ) dest.getY(),
                        (int) plotArea.getX() + 5, ( int ) dest.getY() ) ;
            String s= format.format( tickPos / divisor ).toString() ;
            int len = fm.stringWidth(s) ;
            g.drawString( s, (int) plotArea.getX() - len, ( int ) dest.getY() + fm.getHeight()/2 );

            // Go to next tick in y direction
            tickPos += tickSize.getY() ;
        }

        // Draw exponent if necessary
        if ( divisor != 1 ) {
          g.drawString( "10", (int) plotArea.getX() - 15,
                              (int) plotArea.getY() - fm.getHeight() ) ;
          g.drawString( Integer.toString( factor ),
                        (int) plotArea.getX() - 7, (int) plotArea.getY() - ( 3 * fm.getHeight() ) /2 ) ;
        }

    }

    protected void drawAxesScales( Graphics g ) {

        g.setFont( font ) ;  // Set the current font

        // Fill the background
        g.setColor( ( Color ) getProperty( BACKGROUND_COLOR_PROPERTY ) ) ;
        g.fillRect( (int) rplot.getX(), (int) rplot.getY(),
                    (int) rplot.getWidth(), (int) rplot.getHeight() ) ;

        // Draw axes
        g.setColor( ( Color ) getProperty( FOREGROUND_COLOR_PROPERTY ) ) ;
        g.drawRect( (int) plotArea.getX(), (int) plotArea.getY(),
                        (int) plotArea.getWidth(), (int) plotArea.getHeight() );

        // Draw scales
        drawXScale( g ) ;
        drawYScale( g ) ;

        // Draw labels
        if ( xLabel != null )
            g.drawString( xLabel, (int) xLabelPos.getX(), (int) xLabelPos.getY() ) ;

        if ( yLabel != null )
            g.drawString( yLabel, (int) yLabelPos.getX(), (int) yLabelPos.getY() ) ;

        if ( title != null )
            g.drawString( title, (int) titlePos.getX(), (int) titlePos.getY() ) ;


    }

    /**
     *   Updates the affine plot transform.
     *
     *  <p> First the axis origin location values are subtracted off, then
     *  we apply a scaling and translation to the plot area.
     */
    protected void updateTransform() {
        double xScale, yScale ;
        // Rectangle2D axis ;

        doLayout() ;

        if ( !getAxisManual() ) {
            Rectangle2D tmp = findBounds() ;
            axis = new Rectangle2D.Double() ;
            // Automatic update of ticksize
            tickSize = this.normalizeBounds( tmp.getX(), tmp.getY(), tmp.getWidth(), tmp.getHeight(),
                                             axis ) ;
        }
        else {
            axis = ( Rectangle2D ) raxis.clone() ;
            tickSize = this.normalizeBounds( axis.getX(), axis.getY(), axis.getWidth(), axis.getHeight(),
                                             axis ) ;
            axis = raxis ;
        }

        AffineTransform a = new AffineTransform() ;
        a.translate( -axis.getX(), -axis.getY() ) ;

        switch ( axisType ) {
            case AXES_IJ :
            {
                xScale = plotArea.getWidth() / axis.getWidth() ;
                yScale = plotArea.getHeight() / axis.getHeight() ;

                double[] array = { xScale, 0.0, 0.0, yScale, plotArea.getX(), plotArea.getY() } ;
                plotTransform = new AffineTransform( array ) ;
            }
            break ;
            case AXES_NORMAL : // X and Y
            {
                xScale = plotArea.getWidth() / axis.getWidth() ;
                yScale = plotArea.getHeight() / axis.getHeight() ;

                double[] array = { xScale, 0.0, 0.0, -yScale, plotArea.getX(), plotArea.getMaxY() };

                plotTransform = new AffineTransform( array ) ;
            }
            break ;
        }

        plotTransform.concatenate( a );  // Translate to axis origin firs
    }

    protected Rectangle2D findBounds() {
        if ( plotObjects.size() == 0 ) {
            return new Rectangle2D.Double(0,0,1,1) ;
        }

        Rectangle2D result = ( ( Plottable ) plotObjects.get(0) ).getBounds() ;
        for ( int i =1 ;i<this.plotObjects.size();i++) {
            Plottable p = ( Plottable ) plotObjects.get(i) ;
            Rectangle2D.union( result, p.getBounds(), result ) ;
        }
        return result ;
    }

    /**
     *   Calculate layout and construct a transform from the plot
     *   space to java.awt.Graphics screen coordinate system using
     *   all the current plot and axis bounds.
     */
    protected void doLayout() {

        //Point2D titlePos = new Point2D.Float() ;
        //Point2D xLabelPos = new Point2D.Float() ;
        //Point2D yLabelPos = new Point2D.Float() ;

        double xTitlePos = 0 , yTitlePos = 0 ;
        double xXLabelPos = 0 , yXLabelPos = 0 ;
        double xYLabelPos = 0 , yYLabelPos = 0 ;
        double plotAreaWidth = this.rplot.getWidth(),
               plotAreaHeight = this.rplot.getHeight() ;
        double tickSize = 0 ;  // Size of "external" ticks
        double xg, yg ;
        
        //
        // Process vertical layout
        //
        
        double offset = borderSize ;  // Offset from xPlot and yPlot
        plotAreaHeight -= borderSize ;
        
        if ( title != null ) {
            plotAreaHeight -= fm.getHeight() + space;
            offset += fm.getHeight() + space ;
            yTitlePos = space + fm.getHeight() ;
        }

        yg = offset ;
        
        if ( getDrawXScale() ) {
            plotAreaHeight -= fm.getHeight() + space + tickSize ;
        }
        
        if ( xLabel != null ) {
            yXLabelPos = rplot.getHeight() - space - fm.getHeight() + rplot.getY() ;
            plotAreaHeight -= fm.getHeight() + space;
        }

        if ( yLabel != null ) {
            yYLabelPos = yg + ( plotAreaHeight / 2 ) - fm.getHeight() / 2.0 ;
        }

        // The bottom-most border
        plotAreaHeight -= borderSize ;

        //
        // Process horizontal layout.
        //
        offset = borderSize ;
        plotAreaWidth -= borderSize ;

        // Layout Y Label
        if ( yLabel != null ) {
            double temp = fm.stringWidth( yLabel ) + space ;
            plotAreaWidth -= temp ;
            offset += temp ;
            xYLabelPos = rplot.getX() + borderSize ;
        }

        // Y-Scale and ticks  Not sure how wide to make the scale, so just
        // improvise
        if ( getDrawYScale() ) {
            double temp = space + fm.charWidth( '0' ) * 3 + tickSize ; 
            offset += temp ;
            plotAreaWidth -= temp ;
        }

        // Update graph location
        xg = offset ;

        plotAreaWidth -= borderSize ;

        if ( xLabel != null) {
            int temp = fm.stringWidth( xLabel ) ;
            xXLabelPos = xg + ( plotArea.getWidth() / 2.0 ) - temp / 2.0 ;
        }

        if ( title != null ) {
            int temp = fm.stringWidth( title ) ;
            xTitlePos = xg + ( plotArea.getWidth() / 2.0 ) - temp / 2.0 ;
        }
        
        // Set the plot area size, which will serve as a clipping boundary
        plotArea.setRect( xg, yg, plotAreaWidth, plotAreaHeight );
        xLabelPos.setLocation( xXLabelPos, yXLabelPos ) ;
        yLabelPos.setLocation( xYLabelPos, yYLabelPos ) ;
        titlePos.setLocation( xTitlePos, yTitlePos ) ;
    }

    public static void main( String[] s ) {
        Frame frame = new Frame() ;
        frame.setSize( 400, 300 ) ;

        frame.setLayout( new BorderLayout() ) ;
        PlotComponent gc = new PlotComponent() ;
        frame.add( gc ) ;

        double[] moose = new double[ 16 ];
        for (int i=0;i<moose.length;i++) {
            moose[i] = Math.random() * 100 ;
        }
        double[] meese = new double[ 16 ];
        for (int i=0;i<meese.length;i++) {
            meese[i] = i ;
        }

        Plottable ptble ;
        // Plottable p1 = gc.getPlotContext().addPlot( new DoubleArray( moose ) ) ;
        Plottable p1 = gc.getPlotContext().addPlot( new DoubleArray(meese), new DoubleArray( moose ) ) ;
        //gc.getPlotContext().addPlottable( ptble = new ArrayPlot2( new DoubleArray(meese) ) ) ;
        p1.setProperty( Plottable.COLOR_PROPERTY, Color.blue );
        p1.setProperty( LINE_STYLE_PROPERTY, LINE_STYLE_DASHED );

        GeometryPlot gp = new GeometryPlot( new Rectangle2D.Double( 5, 20, 5, 20 ) ) ;
        gp.setProperty( GeometryPlot.FILLED_PROPERTY, Boolean.TRUE );
        gc.getPlotContext().addPlottable( gp );

        GeometryPlot gp2 = new GeometryPlot( new Rectangle2D.Double( 15, 30, 7, 15 ) ) ;
        // gp2.setProperty( GeometryPlot.FILLED_PROPERTY, Boolean.TRUE );
        gp2.setProperty( Plottable.COLOR_PROPERTY, Color.RED );
        gc.getPlotContext().addPlottable( gp2 );

        //Plottable p1 = gc.getPlotContext().addPlot( new DoubleArray( moose ), new DoubleArray( meese ) ) ;
        //Plottable p2 = gc.getPlotContext().addPlot( new DoubleArray( moose ), new DoubleArray( meese ) ) ;
        //p2.setProperty( ArrayPlot.STYLE_PROPERTY, ArrayPlot.SCATTERPLOT_STYLE ) ;
        gc.getPlotContext().setTitle( "Moose" ) ;
        gc.getPlotContext().setXLabel( "x" ) ; 
        gc.getPlotContext().setYLabel( "y" ) ;
        gc.getPlotContext().setAxisType( Plot2DContext.AXES_NORMAL ) ;

        frame.setVisible( true ) ;
    }

    protected boolean drawXScale = true ;
    protected boolean drawYScale = true ;
    protected boolean isRetained = false ;

    protected boolean axisManual = false ;
    protected int axisType = this.AXES_NORMAL;

    /**
     *  Final drawing axis.
     */
    protected Rectangle2D axis ;

    /** Plot rectangle in window coordinates into which the plot will be rendered.
     */
    protected Rectangle2D rplot = new Rectangle2D.Double( 0, 0, 400, 300 ) ;

    /** User specified rectangle in world coordinates.
     */
    protected Rectangle2D raxis = new Rectangle2D.Float() ;

    protected ArrayList plotObjects = new ArrayList() ;

    protected String xLabel ;
    protected String yLabel ;
    protected String title ;

    // Internal layout state stuff

    /**
     */
    private Rectangle2D plotArea = new Rectangle2D.Float();
    private AffineTransform plotTransform ;
    private Point2D titlePos = new Point2D.Float() ;
    private Point2D xLabelPos = new Point2D.Float() ;
    private Point2D yLabelPos = new Point2D.Float() ;
    private Point2D tickSize ;

    private Properties properties = new Properties() ;
    private Font font ;
    private FontMetrics fm ;

    private static double ticks[] = { 0.01, 0.02, 0.025, 0.05, 0.1, 0.2 };

    boolean isBorderProportional = false ;
    double borderProportion = 0.05 ;

    /**
     * The border size in pixels.
     */
    double borderSize = 15 ;
    double space = 5 ;
}


