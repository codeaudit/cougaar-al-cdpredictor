package org.cougaar.cpe.model;

import org.cougaar.cpe.model.ManueverPlanExecutor;
import org.cougaar.cpe.model.Entity;
import org.cougaar.cpe.model.Plan;

import java.awt.geom.Rectangle2D;
import java.awt.*;

/**
 * Contains state for each unit entity.
 */
public class UnitEntity extends Entity {

    public UnitEntity(String uid, double x, double y) {
        super(uid, x, y);
        this.fuelQuantity = getMaxUnitFuelLoad() ;
        this.ammoQuantity = getMaxUnitAmmoLoad() ;
    }

    public UnitEntity( double x, double y, String uid, int ammoQuantity,
                       float fuelQuantity, boolean isRemote ) {
        super(isRemote,uid,x,y);
        this.ammoQuantity = ammoQuantity;
        if ( ammoQuantity > getMaxUnitAmmoLoad() ) {
            throw new IllegalArgumentException( "Ammo value " + ammoQuantity + " greater than maxLoad " + getMaxUnitAmmoLoad() ) ;
        }
        this.fuelQuantity = fuelQuantity;
        if ( fuelQuantity > getMaxUnitFuelLoad() ) {
            throw new IllegalArgumentException( "Fuel quantity " + fuelQuantity + " excceds max " + getMaxUnitFuelLoad() ) ;
        }
    }

    public void toString( StringBuffer buf ) {
        buf.append( "[UnitEntity ").append( getId() ) ;
        buf.append( ",x=" ).append( getX() ).append( ",y=" ).append(getY()) ;
        buf.append( ",fuelQuantity=").append( getFuelQuantity() ).append(",ammo=" ) ;
        buf.append( getAmmoQuantity() );
        buf.append( ",zone=").append( getZoneSchedule() ) ;
        buf.append( "]" ) ;
    }

    public ZoneSchedule getZoneSchedule() {
        return zoneSchedule;
    }

    public void setZoneSchedule(ZoneSchedule zoneSchedule) {
        this.zoneSchedule = zoneSchedule;
    }

    public int getMaxUnitAmmoLoad() {
        return VGWorldConstants.getMaxUnitAmmoLoad() ;
    }

    public float getMaxUnitFuelLoad() {
        return VGWorldConstants.getMaxUnitFuelLoad() ;
    }

    public UnitEntity( double x, double y, String uid, int ammoQuantity,
                       double fuelQuantity ) {
        super(uid,x,y);
        this.ammoQuantity = ammoQuantity;
        this.fuelQuantity = ( float ) fuelQuantity;
    }

    public void update(WorldState ws) {
        // Update based on the manuever plan.
        ManueverPlanExecutor executor = ManueverPlanExecutor.getInstance() ;
        if ( !isRemote ) {
           if ( executor != null ) {
               executor.execute( this, ws );
           }
        }
    }

    public Object clone()  {
        UnitEntity entity =
                new UnitEntity( x, y, getId(),
                ammoQuantity,
                fuelQuantity,
                isRemote ) ;
        entity.zoneSchedule = zoneSchedule ;

        // Don't try to clone this.
        // entity.executor = executor ;
        return entity ;
    }

    public Shape getRangeShape() {
        if ( rbox == null ) {
            rbox = new Rectangle2D.Double( -VGWorldConstants.getUnitRangeWidth()/2,0,
                    VGWorldConstants.getUnitRangeWidth(), VGWorldConstants.getUnitRangeHeight() );
        }

        return rbox ;
    }

    public Shape getSensorShape() {
        if ( sbox == null ) {
            new Rectangle2D.Double( -VGWorldConstants.getUnitSensorWidth()/2, 0,
                    VGWorldConstants.getUnitSensorWidth(), VGWorldConstants.getUnitSensorHeight() ) ;
        }
        return sbox ;
    }

    public Plan getManueverPlan() {
        return manueverPlan;
    }

    public void setManueverPlan(Plan manueverPlan) {
        this.manueverPlan = manueverPlan;
    }

    /**
     * This is a dumb update which simply overlays the new plan on top of
     * the old plan and discards the rest.
     *
     * @param newPlan
     */
    public void updateManeuverPlan( Plan newPlan ) {
        // Clear any tasks before the current index.
        if ( manueverPlan != null ) {
            manueverPlan.clearCompletedTasks();
        }

        if ( manueverPlan == null || manueverPlan.getNumTasks() == 0 ) {
            manueverPlan = (Plan) newPlan.clone() ;
        }
        else if ( newPlan.getNumTasks() > 0 ) {
            UnitTask firstTask = (UnitTask) newPlan.getTask(0) ;
            for (int i=0;i<manueverPlan.getNumTasks();i++) {
                UnitTask t = (UnitTask) manueverPlan.getTask(i) ;
                // Overlay directly on top of the manueverPlan by chopping off
                // any tasks that end after the first plan. If the current task
                // is chopped off, just find the next non-complete task.
                if ( t.getEndTime() > firstTask.startTime ) {
                    int newIndex = -1 ;
                    if ( manueverPlan.getCurrentActionIndex() > i ) {
                        newIndex = i ;
                    }
                    else {
                        newIndex = manueverPlan.getCurrentActionIndex() ;
                    }
                    manueverPlan.replan( i, newPlan );
                    manueverPlan.setCurrentActionIndex( newIndex );
                    return ;
                }
            }

            // Just append it to the existing tasks.
            manueverPlan.replan( manueverPlan.getNumTasks(), newPlan );
        }
    }

    public void setAmmoQuantity(int ammoQuantity) {
        this.ammoQuantity = ammoQuantity;
    }

    public int getAmmoQuantity() {
        return ammoQuantity;
    }

    public float getFuelQuantity() {
        return fuelQuantity;
    }

    public void setFuelQuantity(double fuelQuantity) {
        this.fuelQuantity = ( float ) fuelQuantity;
    }

    public int getCriticalAmmoThreshold() {
        return criticalAmmoThreshold;
    }

    protected Plan manueverPlan ;

    // Not yet implemented.
    // protected Plan sustainmentPlan ;
    protected int ammoQuantity ;
    protected float fuelQuantity ;
    protected ZoneSchedule zoneSchedule ;

    /**
     * This is the default range shape.
     */
    protected static Rectangle2D rbox ;

    protected static Rectangle2D sbox ;

    /**
     * Critical threshold in ammo units.
     */
    protected static final int criticalAmmoThreshold = 10 ;

}
