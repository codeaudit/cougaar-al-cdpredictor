package com.axiom.lib.plot ;
import java.awt.geom.Rectangle2D ;
import java.util.* ;

/**
 *  A Plottable object is described in world (e.g. plot coordinates.)  A Plottable
 *  object renders its contents to a PlotContext using a transform from the
 *  plot space to the device space.
 *
 *  <p> Plot properties can be associated with Plottable, although this has not
 *  yet been implemented.
 */
public interface Plottable {

    public static final String COLOR_PROPERTY = "ColorProperty" ;

    public boolean hasChildren() ;

    public Enumeration elements() ;

    public void render( java.awt.Graphics2D g ) ;

    /**
     *  Returns bounds in world coordinates.
     */
    public Rectangle2D getBounds() ;

    public Enumeration getProperties() ;

    public void setProperty( String key, Object value ) ;

    public Object getProperty( String key ) ;

    public Object removeProperty( String key ) ;
}
