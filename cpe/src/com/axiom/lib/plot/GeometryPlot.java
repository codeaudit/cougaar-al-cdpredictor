package com.axiom.lib.plot;

import java.util.Enumeration;
import java.awt.geom.Rectangle2D;
import java.awt.geom.GeneralPath;
import java.awt.*;

/**
 * User: wpeng
 * Date: Sep 20, 2003
 * Time: 2:31:24 PM
 */
public class GeometryPlot extends AbstractPlot
{
    public static final String FILLED_PROPERTY = "Filled" ;
    public static final String FILL_COLOR = "FillColor" ;

    protected Shape path ;

    public GeometryPlot(Shape path)
    {
        this.path = path;
    }

    public Shape getPath()
    {
        return path;
    }

    public Enumeration elements()
    {
        return null;
    }

    public Rectangle2D getBounds()
    {
        return path.getBounds2D() ;
    }

    public boolean hasChildren()
    {
        return false;
    }

    public void render(Graphics2D g)
    {
        Color c = (Color) getProperty( COLOR_PROPERTY ) ;
        if ( c != null ) {
            g.setColor( c );
        }
        else {
            g.setColor( Color.BLUE );
        }

        Boolean b = (Boolean) getProperty( FILLED_PROPERTY ) ;
        if ( b != null ) {
            g.fill( path );
        }
        else {
            g.draw( path );
        }
    }
}

