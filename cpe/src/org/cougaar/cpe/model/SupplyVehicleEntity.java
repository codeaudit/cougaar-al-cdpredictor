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


        SupplyTask t = (SupplyTask) currentPlan.getCurrentTask() ;

        while ( t != null && t.isComplete() ) {
            t = (SupplyTask) currentPlan.nextTask() ;
        }

        // Recovery tasks are always done. Delay all new tasks into the future until the
        // recovery is complete. Also, if a task is in progress it cannot be cancelled.
        if ( t != null &&
             ( ( t.getDisposition() == Task.ACTIVE && !t.isComplete() )  // I am in progress
               || ( t.getType() == SupplyTask.ACTION_RECOVERY && !t.isComplete() ) ) ) // I am a recovery task
        {
            int index = currentPlan.getCurrentActionIndex() ;

            // Cannot preempt this task or the next one if the next one is a recovery task.
            SupplyTask currentTask = (SupplyTask) currentPlan.getCurrentTask() ;
            if ( currentTask.getType() != SupplyTask.ACTION_RECOVERY &&
                currentPlan.getCurrentActionIndex() < currentPlan.getNumTasks() - 1 ) {
                SupplyTask nextTask = (SupplyTask) currentPlan.getTask( index + 1 ) ;
                if ( nextTask.getType() == SupplyTask.ACTION_RECOVERY ) {
                    index++ ;
                }
            }

            int delayedTasks = ScheduleUtils.delayPlanUntilTime( newSupplyPlan, t.getEndTime() );
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
