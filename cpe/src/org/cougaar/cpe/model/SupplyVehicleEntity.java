package org.cougaar.cpe.model;

import org.cougaar.cpe.util.ScheduleUtils;

/**
 * User: wpeng
 * Date: Apr 21, 2003
 * Time: 4:36:48 PM
 */
public class SupplyVehicleEntity extends Entity
{
    public static final int MAX_UNITS = 20 ;

    public SupplyVehicleEntity(String uid, double x, double y) {
        super(uid, x, y);
    }

    public void toString(StringBuffer buf) {
        super.toString(buf);
        buf.append( ",fuel=").append( fuelQuantity ) ;
        buf.append( ",ammo=").append( ammoQuantity ) ;
    }

    public Plan getSupplyPlan() {
        return supplyPlan;
    }

    public void setSupplyPlan(Plan supplyPlan) {
        this.supplyPlan = supplyPlan;
    }

    public void updateSupplyPlan( Plan newSupplyPlan ) {
        if ( supplyPlan != null ) {
            supplyPlan.clearCompletedTasks();
        }

        if ( newSupplyPlan == null ) {
            newSupplyPlan = Plan.getNullPlan();
        }

        Plan currentPlan = getSupplyPlan() ;
        if ( currentPlan == null || currentPlan.getNumTasks() == 0 ) {
            supplyPlan = newSupplyPlan ;
            return ;
        }


        // Scan through the plan and find the next incomplete task.
        SupplyTask t = (SupplyTask) currentPlan.getCurrentTask() ;
        int index = currentPlan.getCurrentActionIndex() ;

        while ( t != null && t.isComplete() ) {
            t = (SupplyTask) currentPlan.nextTask() ;
            index++ ;
        }

        // If we find an incomplete task, see if it is active.  If so, we must delay
        // unitl it and its recovery tasks are done. Delay all new tasks into the future until the
        // active task and its recovery is complete.
        if ( t != null &&
             ( ( t.getDisposition() == Task.ACTIVE && !t.isComplete() )  // I am in progress
               || ( t.getType() == SupplyTask.ACTION_RECOVERY && !t.isComplete() ) ) ) // I am a recovery task
        {
            // Cannot preempt this task or the next one if the next one is a recovery task.
            long lastTime = t.getEndTime() ;
            SupplyTask nextTask ;
            if ( t.getType() != SupplyTask.ACTION_RECOVERY &&
                 index < currentPlan.getNumTasks() - 1 )
            {
                nextTask = (SupplyTask) currentPlan.getTask( index + 1 ) ;
                if ( nextTask.getType() == SupplyTask.ACTION_RECOVERY ) {
                    lastTime = nextTask.getEndTime() ;
                    index++ ;
                }
            }

            int delayedTasks = ScheduleUtils.delayPlanUntilTime( newSupplyPlan, lastTime );
            System.out.println( "UPDATE SUPPLY PLAN " + this + ":: Delayed " + delayedTasks + " tasks.");
            currentPlan.replan( index + 1 , newSupplyPlan );
        }
        else {
            setSupplyPlan( newSupplyPlan );
        }
    }

    public double getMaxCapacity() {
        return MAX_UNITS ;
    }

    public double getCapacity() {
        return MAX_UNITS - totalUnits ;
    }

    public int getAmmoQuantity() {
        return ammoQuantity;
    }

    public float getFuelQuantity() {
        return fuelQuantity;
    }

    public void setAmmoQuantity(int ammoQuantity) {
        this.ammoQuantity = ammoQuantity;
    }

    public void setFuelQuantity(float fuelQuantity) {
        this.fuelQuantity = fuelQuantity;
    }

    public double getMovementRate() {
        return VGWorldConstants.getSupplyVehicleMovementRate() ;
    }

    public void addAmmoQuantity( int quantity ) {
        int newAmmoQuantity = ammoQuantity + quantity ;
        float newTotalQuantity = fuelQuantity + newAmmoQuantity ;
        if ( newTotalQuantity > MAX_UNITS ) {
            throw new RuntimeException( "Quantity " + quantity + " exceeds space capacity " + ( MAX_UNITS - totalUnits ) ) ;
        }
        ammoQuantity = newAmmoQuantity ;
        totalUnits = newTotalQuantity ;
    }

    public void addFuelQuantity( float quantity ) {
        float newFuelQuantity = fuelQuantity + quantity ;
        float newTotalQuantity = ammoQuantity + newFuelQuantity ;
        if ( newTotalQuantity > MAX_UNITS ) {
            throw new RuntimeException( "Quantity " + quantity + " exceeds space capacity " + ( MAX_UNITS - totalUnits ) ) ;
        }
        fuelQuantity = newFuelQuantity ;
        totalUnits = newTotalQuantity ;
    }

    public void removeAmmoQuantity( int quantity ) {
        if ( quantity > ammoQuantity ) {
           throw new RuntimeException( "Quantity " + quantity + " does not exist, found ammoQuantity=" + ammoQuantity ) ;
        }
        ammoQuantity -= quantity ;
        totalUnits = ammoQuantity + fuelQuantity ;
    }

    public void removeFuelQuantity( double quantity ) {
        if ( quantity > fuelQuantity ) {
           throw new RuntimeException( "Quantity " + quantity + " does not exist, found fuelQuantity=" + fuelQuantity ) ;
        }
        ammoQuantity -= quantity ;
        totalUnits = ammoQuantity + fuelQuantity ;
    }

    public Object clone() {
        SupplyVehicleEntity result = new SupplyVehicleEntity( getId(), getX(), getY() ) ;
        result.totalUnits = totalUnits ;
        result.ammoQuantity = ammoQuantity ;
        result.fuelQuantity = fuelQuantity ;
        if ( supplyPlan != null ) {
            result.supplyPlan = (Plan) supplyPlan.clone() ;
        }
        return result ;
    }

    public Plan getPlan() {
        return supplyPlan ;
    }

    public void update( WorldState ws ) {
        SustainmentPlanExecutor executor = SustainmentPlanExecutor.getInstance() ;
        executor.execute( this, ws );
    }

    float totalUnits ;
    int ammoQuantity ;
    float fuelQuantity ;

    Plan supplyPlan ;

}
