package org.cougaar.cpe.model;

import java.awt.geom.Point2D;
import java.io.Serializable;

public abstract class Entity extends CPEObject {

    /**
     * Whether this is being a remote or local.  A local entity is
     * the ground truth, a remote entity is the representation of a
     * entity from the point of view of another entity.
     */
    protected boolean isRemote = false ;
    protected double strength = VGWorldConstants.getTargetFullStrength() ;

    public Entity(boolean remote, String worldId, double x, double y) {
        super( worldId ) ;
        isRemote = remote;
        this.x = ( float ) x;
        this.y = ( float ) y;
    }

    public abstract Object clone() ;

    public void toString( StringBuffer buf ) {
        super.toString( buf );
        buf.append( ",x=").append(x).append( ",y=").append(y) ;
    }

    public Entity(String id, double x, double y) {
        super( id ) ;
        this.x = ( float ) x ;
        this.y = ( float ) y ;
    }

    public boolean isRemote() {
        return isRemote;
    }

    public void setX(double x) {
        this.x = ( float ) x;
    }

    public void setY(double y) {
        this.y = ( float ) y;
    }

    public void setPosition( Point2D p2d ) {
        x = ( float ) p2d.getX() ;
        y = ( float ) p2d.getY() ;
    }

    public Point2D getPosition() {
        return new Point2D.Double( x, y ) ;
    }

    public void setPosition( double x, double y ) {
        this.x = ( float ) x ;
        this.y = ( float ) y ;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    protected float x;
    protected float y ;
    protected boolean isActive = true ;
    public abstract void update( WorldState ws ) ;

    public double getStrength() {
        return strength;
    }

    public void setStrength(double strength) {
        this.strength = strength;
    }
}
