package com.axiom.lib.plot ;
import java.awt.* ;
import java.awt.geom.* ;
import com.axiom.lib.util.NumberArray ;
import java.awt.geom.Rectangle2D ;
import com.axiom.lib.util.DoubleArray ;
import java.util.Enumeration ;
import com.axiom.lib.util.ArrayMath ;

/**
 *  Represents arrays of data to be plotted as scatter or line styles.
 */
public class ArrayPlot extends AbstractPlot {

    public static final String STYLE_PROPERTY = "ArrayPlotStyleProperty";

    public static final String LINEPLOT_STYLE = "LinePlot";

    public static final String SCATTERPLOT_STYLE = "ScatterPlot" ;

    /**
     *  Plot y as a function of x.
     */
    public ArrayPlot( NumberArray y, NumberArray x ) {
        this( y ) ;

        if ( x != null ) {
            xarray = new DoubleArray( x ) ;
        }

        if ( y.getSize() != x.getSize() )
            throw new IllegalArgumentException( "Arrays do not match in size.");

        setProperty( STYLE_PROPERTY, LINEPLOT_STYLE ) ;
    }

    /**
     *  Plot x as a function of its index.
     */
    public ArrayPlot( NumberArray y ) {
        if ( y == null )
            throw new IllegalArgumentException( "X array must be non-null." ) ;
        yarray = new DoubleArray( y ) ;
    }

    public DoubleArray getXArray() {
        return xarray ;
    }

    public DoubleArray getYArray() {
        return yarray ;
    }

    public boolean hasChildren() {
        return false ;
    }

    public Enumeration elements() {
        return null ;
    }

    public void render( Graphics2D g ) {
       NumberArray x = xarray ;
       NumberArray y = yarray ;
       AffineTransform tx = g.getTransform() ;
       Point2D src = new Point2D.Float(), dest = new Point2D.Float();
       int[] xp = new int[y.getSize()];
       int[] yp = new int[y.getSize()];

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

       String s = ( String ) getProperty( ArrayPlot.STYLE_PROPERTY ) ;
       if ( s== null || s.equals( ArrayPlot.LINEPLOT_STYLE ) ) {
         g.drawPolyline( xp, yp, xp.length ) ;
         //for (int j=0;j<xp.length-1;j++) {
         //  g.drawPolyline(
         //  g.drawLine( ( int ) xp[j], (int) yp[j], (int) xp[j+1], (int) yp[j+1] ) ;
         //}
       }
       else {
            for (int j=0;j<xp.length;j++) {
                Plot2DContext.drawSymbol( g, xp[j], yp[j] ) ;
            }
       }

    }

    public Rectangle2D getBounds() {
        double x, y, h, w ;

        if ( xarray == null ) {
           x = 1;
           w = yarray.getSize() ;
        }
        else {
           x = ArrayMath.min( xarray ) ;
           w = ArrayMath.max( xarray ) - x ;
        }

        y = ArrayMath.min( yarray );
        h = ArrayMath.max( yarray ) - y ;

        return new Rectangle2D.Double(x, y, w, h) ;
    }

    DoubleArray xarray, yarray ;
}