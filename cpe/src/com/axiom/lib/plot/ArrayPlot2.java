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
public class ArrayPlot2 extends AbstractPlot {

    public static final String STYLE_PROPERTY = "ArrayPlotStyleProperty";

    public static final String LINEPLOT_STYLE = "LinePlot";

    public static final String SCATTERPLOT_STYLE = "ScatterPlot" ;


    public ArrayPlot2() {
        setProperty( STYLE_PROPERTY, LINEPLOT_STYLE ) ;
    }
    
    /**
     *  Plot y as a function of x.
     */
    public ArrayPlot2( NumberArray y, NumberArray x ) {
        if ( y == null )
            throw new IllegalArgumentException( "Y array must be non-null." ) ;
        yarray = new DoubleArray( y ) ;

        if ( x != null ) {
            xarray = new DoubleArray( x ) ;
        }

        if ( y.getSize() != x.getSize() )
            throw new IllegalArgumentException( "Arrays do not match in size.");
        updateGeometry() ;
    }

    /**
     *  Plot x as a function of its index.
     */
    public ArrayPlot2( NumberArray y ) {
        if ( y == null )
            throw new IllegalArgumentException( "Y array must be non-null." ) ;
        xarray = null ;
        yarray = new DoubleArray( y ) ;
        updateGeometry() ;
    }

    private void updateGeometry() {
        path = new GeneralPath() ;
        if ( xarray != null ) {
            path.moveTo( ( float ) xarray.valueAt(0), ( float ) yarray.valueAt(0) );
            for (int i=1;i<xarray.getSize();i++) {
                path.lineTo( ( float ) xarray.valueAt(i), ( float ) yarray.valueAt(i) );
            }
        }
        else {
            path.moveTo( 0, ( float ) yarray.valueAt(0) );
            for (int i=1;i<yarray.getSize();i++) {
                path.lineTo( i, ( float ) yarray.valueAt(i) ) ;
            }
        }
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
        Object o = getProperty( Plottable.COLOR_PROPERTY ) ;
        if ( o instanceof Color ) {
            g.setColor( ( Color ) o );
        }
        g.draw( path );
    }

    public Rectangle2D getBounds() {
        return path.getBounds2D() ;
    }

    GeneralPath path ;
    DoubleArray xarray, yarray ;
}