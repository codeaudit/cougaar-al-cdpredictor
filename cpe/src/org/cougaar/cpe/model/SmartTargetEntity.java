package org.cougaar.cpe.model;

import java.util.Random;

/**
 * This adds some (random) manuvering.
 */
public class SmartTargetEntity extends TargetEntity
{
    public SmartTargetEntity(String uid, double x, double y, double dx, double dy, double strength)
    {
        super(uid, x, y, dx, dy, strength);
        random = new Random( uid.hashCode() ) ;
    }

    public Object clone()
    {
        SmartTargetEntity te = new SmartTargetEntity( worldId, x, y, dx, dy, strength ) ;
        te.isSuppressed = isSuppressed ;
        te.suppressionTime = suppressionTime ;
        te.isActive = isActive ;
        te.strength = strength ;
        te.random = random ;
        return te ;
    }

    public void update(WorldState ws)
    {
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

        if ( !ws.isModel() && random != null && VGWorldConstants.getTargetXMoveRate() != 0 ) {
            // Perturb the velocity of the target by some delta.
            double pdx = random.nextGaussian() * VGWorldConstants.getTargetXMoveRate() ;
//            System.out.println( getId() + ":: Tx=" + tx + ",dx=" + pdx);
            tx += pdx ;
        }

        ws.moveTarget( this, tx, ty  ) ;
    }

    transient Random random ;
}
