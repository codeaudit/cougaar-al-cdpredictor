package org.cougaar.cpe.model;

import org.cougaar.cpe.model.Entity;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

public class TargetEntity extends Entity {

    public TargetEntity( String uid, double x, double y, double dx, double dy, double strength ) {
        super( uid, x, y ) ;
        this.dx = dx ;
        this.dy = dy ;
        this.strength = strength ;
    }

    public Object clone()  {
        TargetEntity te = new TargetEntity( worldId, x, y, dx, dy, strength ) ;
        te.isSuppressed = isSuppressed ;
        te.suppressionTime = suppressionTime ;
        te.isActive = isActive ;
        te.strength = strength ;
        return te ;
    }

    public boolean isSuppressed() {
        return isSuppressed;
    }

    public void setSuppressed(boolean suppressed, long time ) {
        isSuppressed = suppressed;
        this.suppressionTime = time ;
    }

    public boolean isRouted()
    {
        return isRouted;
    }

    public static Rectangle2D.Double getRangeShape()
    {
        if ( rangeShape == null ) {
            new Rectangle2D.Double( - VGWorldConstants.TARGET_RANGE_WIDTH/2, -VGWorldConstants.TARGET_RANGE_HEIGHT, VGWorldConstants.TARGET_RANGE_WIDTH,
                                    VGWorldConstants.TARGET_RANGE_HEIGHT ) ;
        }
        return rangeShape;
    }

    static ArrayList results = new ArrayList() ;

    public void update(WorldState ws) {
        if (!isActive()) {
            return ;
        }
        double tx = 0 , ty = 0 ;
        if ( ws.getTime() > suppressionTime + VGWorldConstants.getTargetSuppressedTime() ) {
            setSuppressed( false, 0 );
        }

        if ( strength < VGWorldConstants.getTargetRoutStrength() ) {
            isRouted = true ;
        }

        // Engage the nearest unit.  This is commented out for now
//        if (!isRouted() ) {
//           ws.getUnitsInRange( getX(), getY(), rangeShape, results );
//        }

        if ( !isRouted || ws.isTargetRoutEnabled() ) {
            if (!isSuppressed) {
                tx = getX() + dx * ws.getDeltaT() ;
                ty = getY() + dy * ws.getDeltaT() ;
            }
            else {
                tx = getX() + dx * ws.getDeltaT() * VGWorldConstants.getTargetSuppressedMovementFactor() ;
                ty = getY() + dy * ws.getDeltaT() * VGWorldConstants.getTargetSuppressedMovementFactor() ;
            }
        }
        else { // Move in the opposition direction if the target is routed.
            if (!isSuppressed) {
                tx = getX() - dx * ws.getDeltaT() ;
                ty = getY() - dy * ws.getDeltaT() ;
            }
            else {
                tx = getX() - dx * ws.getDeltaT() * VGWorldConstants.getTargetSuppressedMovementFactor() ;
                ty = getY() - dy * ws.getDeltaT() * VGWorldConstants.getTargetSuppressedMovementFactor() ;
            }
        }
        ws.moveTarget( this, tx, ty  ) ;
//        // If I have reached the target area,
//        if ( !ws.isOnBoard(x,y) && x < ws.getLowerX() ) {
//
//        }
    }

    public double getDx() {
        return dx;
    }

    public double getDy() {
        return dy;
    }

    protected double dx;
    protected double dy;

    protected long suppressionTime = 0 ;
    protected boolean isSuppressed = false ;
    protected boolean isRouted = false ;
    protected int scoreMultiplier = 1 ;

    protected static Rectangle2D.Double rangeShape ;
}
