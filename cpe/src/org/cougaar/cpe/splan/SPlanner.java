package org.cougaar.cpe.splan;

import org.cougaar.cpe.model.*;
import org.cougaar.cpe.util.ScheduleUtils;

import java.util.*;
import java.awt.geom.Point2D;

import org.cougaar.planning.ldm.plan.ScheduleImpl;
import org.cougaar.planning.ldm.plan.ScheduleElement;
import org.cougaar.core.util.UID;
import org.cougaar.core.service.UIDService;

/**
 * Take a manuever plan (or set of plans), generate demand, and create a sustainment
 * plan.
 *
 * <p> Current approach.  Randomize priorities and run simulation forward.  Observe
 * log state and optimal score.
 */
public class SPlanner {
    private static EngageByFireResult er = new EngageByFireResult();
    private String clusterName = "Unknown";

    public SPlanner(String clusterName) {
        this.clusterName = clusterName;
    }

    public SPlanner(double minLoadFraction) {
        this.minLoadFraction = minLoadFraction;
    }

    public SPlanner() {
    }

    public UID getNextUID() {
        return new UID( clusterName, count++ ) ;
    }

    private static int count = 0 ;

    protected long estimateRemainingTime( WorldState state,  SupplyVehicleEntity s, SupplyTask t ) {
        if ( t.getType() == SupplyTask.SUPPLY_AMMO || t.getType() == SupplyTask.SUPPLY_FUEL ) {
            return ( long ) Math.ceil( ( - s.getY() / s.getMovementRate() )
                    * VGWorldConstants.MILLISECONDS_PER_SECOND ) ;
        }
        else if ( t.getType() == SupplyTask.ACTION_RECOVERY ) {
            return ( long )
                    Math.ceil( ( ( s.getY() - state.getRecoveryLine() ) / s.getMovementRate() )
                    * VGWorldConstants.MILLISECONDS_PER_SECOND ) ;
        }
        else throw new IllegalArgumentException( "Unknown action type.") ;
    }

    /**
     *  Run the current plan from the current time to the endTime  Prioritize (e.g.
     *  find the customers with the most critical demand.  Assign a bunch of tasks
     *  to each customer.
     */
    public WorldStateModel projectInventory( WorldState ws, UnitEntity[] customers,
                                  SupplyVehicleEntity[] supplyEntities,
                                  ScheduleImpl[] supplyPlans, float[][] fuelInventory,
                                  int[][] ammoInventory, long endTime )
    {
        WorldStateModel wsm = new WorldStateModel( ws, true, true ) ;
        copyManueverPlans( ws, wsm );
        if ( customers.length == 0 ) {
            return wsm ;
        }

        for (int j=0;j<supplyEntities.length;j++) {
            SupplyVehicleEntity entity = (SupplyVehicleEntity) wsm.getEntity( supplyEntities[j].getId() ) ;
            ArrayList planElements = new ArrayList( supplyPlans[j] ) ;
            for (int i = 0; i < planElements.size(); i++) {
                SupplyTask supplyTask = (SupplyTask)planElements.get(i);
                supplyTask.setComplete( false );
                supplyTask.setEstimatedResult( null );
                supplyTask.setObservedResult( null );
            }
            Collections.sort( planElements, new ScheduleElementComparator());

            entity.setSupplyPlan( new Plan( planElements ) );
        }

        long numDeltas = (long)
                Math.ceil( ( ( endTime - ws.getTime() ) *
                VGWorldConstants.SECONDS_PER_MILLISECOND ) / ws.getDeltaT() ) ;
        for (int i=0;i<numDeltas;i++) {
            wsm.updateWorldState();

// Don't bother checking this right now since we aren't keeping a running tally anyways.
//            for (int j = 0; j < customers.length; j++) {
//                UnitEntity customer = customers[j];
//                UnitEntity simCustomer = (UnitEntity) wsm.getEntity( customer.getId() ) ;
//                //fuelInventory[j][i] = simCustomer.getFuelQuantity() ;
//                //ammoInventory[j][i] = simCustomer.getAmmoQuantity() ;
//            }
        }

        return wsm ;
    }

    static class DemandRecord {
        public DemandRecord(int demandType, UnitEntity entity) {
            this.demandType = demandType;
            this.entity = entity;
        }

        public int getDemandType() {
            return demandType;
        }

        public UnitEntity getEntity() {
            return entity;
        }

        public void setQuantity(int quantity)
        {
            this.quantity = quantity;
        }

        public int getQuantity()
        {
            return quantity;
        }

        public String toString()
        {
            return "[Demand " + entity.getId() + ",demandType=" + SupplyTask.getStringForAction(demandType) + "]";
        }

        UnitEntity entity ;
        int demandType ;
        int quantity ;
    }


    static class ConsumerDemandComparator implements Comparator {
        /**
         * A stupid prioritization method for customers.
         * @param o1
         * @param o2
         * @return
         */
        public int compare(Object o1, Object o2) {
            DemandRecord d1 = (DemandRecord) o1, d2 = (DemandRecord) o2 ;
            UnitEntity ue1 = d1.getEntity(), ue2 = d2.getEntity() ;


            if ( d1.getDemandType() == SupplyTask.SUPPLY_AMMO && d2.getDemandType() == SupplyTask.SUPPLY_AMMO ) {

                if ( ue1.getAmmoQuantity() < ue2.getAmmoQuantity() ) {
                    return -1 ;
                }
                else if ( ue2.getAmmoQuantity() < ue1.getAmmoQuantity() ) {
                    return 1 ;
                }
            }
            else if ( d1.getDemandType() == SupplyTask.SUPPLY_AMMO && d2.getDemandType() == SupplyTask.SUPPLY_FUEL ) {
                int ammoDemand1 = ue1.getMaxUnitAmmoLoad() - ue1.getAmmoQuantity() ;
                float fuelDemand2 = ue2.getMaxUnitFuelLoad() - ue2.getFuelQuantity() ;
                if ( ammoDemand1 == 0 && fuelDemand2 > 0 ) {
                    return 1 ;
                }
                else {
                    return -1 ;
                }
            }
            else if ( d1.getDemandType() == SupplyTask.SUPPLY_FUEL && d2.getDemandType() == SupplyTask.SUPPLY_AMMO ) {
                int ammoDemand2 = ue2.getMaxUnitAmmoLoad() - ue2.getAmmoQuantity() ;
                float fuelDemand1 = ue1.getMaxUnitFuelLoad() - ue1.getFuelQuantity() ;
                if ( ammoDemand2 == 0 && fuelDemand1 > 0 ) {
                    return -1 ;
                }
                else {
                    return 1 ;
                }
            }
            else if ( d2.getDemandType() == SupplyTask.SUPPLY_FUEL && d2.getDemandType() == SupplyTask.SUPPLY_FUEL ) {
                if ( ue1.getFuelQuantity() < ue2.getFuelQuantity() ) {
                    return -1 ;
                }
                else if ( ue2.getFuelQuantity() < ue1.getFuelQuantity() ) {
                    return 1 ;
                }
            }

            return 0 ;
        }
    }

    private void printSlots( SupplyTask[][] task, StringBuffer buf ) {
        for (int i = 0; i < task.length; i++) {
            SupplyTask[] supplyTasks = task[i];
            buf.append( "[" ) ;
            for (int j = 0; j < supplyTasks.length; j++) {
                SupplyTask supplyTask = supplyTasks[j];
                if ( supplyTask != null ) {
                    buf.append( supplyTask.getId() ) ;
                }
                else {
                    buf.append( "null" ) ;
                }
                if ( j < supplyTasks.length - 1 ) {
                    buf.append( "," ) ;
                }
            }
            buf.append( "]\n" ) ;
        }
    }

    /**
     * Returns an array (entityIndex,startIndex) where (entityIndex is the index
     * of the entity
     */

    protected int[] findNextAvailableSlot( SupplyTask[][] task, int start, int end, int slotSize ) {
        System.out.println("Finding next available slot between " + start + " and " + end );
        for ( int i=start;i<end;i++ ) {
            for (int j=0;j<task.length;j++ ) {
                if ( task[j][i] == null ) {
                    boolean found = true ;
                    for (int k=i;k<i+slotSize;k++) {
                        if ( task[j][k] != null ) {
                            found = false ;
                            break ;
                        }
                    }

                    if (found) {
                        return new int[] { j, i } ;
                    }
                }
            }
        }
        return null ;
    }

    protected double minLoadFraction = 1.0 ;

    /**
     * Allocate a pair of supply/recovery tasks within a specific start
     * and end time.
     *
     * @param endState  The world state simulated to the end point.
     * @param custEntities  Customers I am providing for.
     * @param suppEntities  Suppliers I am planning for.
     * @param schedules
     * @param fuelInventory
     * @param ammoInventory
     * @param startTime
     * @param endTime
     * @return Whether any new tasks were allocated.
     */
    protected boolean allocateNewTasksForDemand( WorldStateModel endState,
                                                UnitEntity[] custEntities,
                                                SupplyVehicleEntity[] suppEntities,
                                                ScheduleImpl[] schedules,
                                                float[][] fuelInventory,
                                                int[][] ammoInventory,
                                                long startTime, long endTime )
    {
        ArrayList priorityList = new ArrayList() ;

        long deliveryTime = (long) Math.ceil( ( Math.abs( endState.getRecoveryLine() ) / VGWorldConstants.getSupplyVehicleMovementRate() ) *
                VGWorldConstants.MILLISECONDS_PER_SECOND ) ;

        long[] slot = ScheduleUtils.findNextEmptySlot( schedules, endTime-deliveryTime, endTime+deliveryTime, deliveryTime * 2 ) ;

        if ( slot == null ) {
            return false ;  // No viable slots within the schedules exist in any case.
        }

        for (int i=0;i<custEntities.length;i++) {
            UnitEntity customer = ( UnitEntity ) endState.getEntity( custEntities[i].getId() ) ;

            if ( customer.getAmmoQuantity() < customer.getMaxUnitAmmoLoad() ) {
                priorityList.add( new DemandRecord( SupplyTask.SUPPLY_AMMO, customer ) ) ;
            }
            if ( customer.getFuelQuantity() < customer.getMaxUnitFuelLoad() ) {
                priorityList.add( new DemandRecord( SupplyTask.SUPPLY_FUEL, customer ) ) ;
            }
        }

        // There are no supplies which need to be allocated.
        if ( priorityList.size() == 0 ) {
            return false ;
        }

        Collections.sort( priorityList, new ConsumerDemandComparator() );

        boolean allocatedNewTasks = false ;

        for (int i = 0; i < priorityList.size(); i++) {
            DemandRecord demandRecord = (DemandRecord)priorityList.get(i);
            UnitEntity entity = (UnitEntity) endState.getEntity( demandRecord.getEntity().getId() ) ;
            double demand = 0 ;
            int type = -1 ;

            if ( demandRecord.getDemandType() == SupplyTask.SUPPLY_AMMO ) {
                if ( entity.getMaxUnitAmmoLoad() - entity.getAmmoQuantity() > SupplyVehicleEntity.MAX_UNITS * minLoadFraction ) {
                    demand = Math.min( SupplyVehicleEntity.MAX_UNITS, entity.getMaxUnitAmmoLoad() - entity.getAmmoQuantity() ) ;
                    //demandRecord.setQuantity( demand );
                    type = SupplyTask.SUPPLY_AMMO ;
                }
            }
            else if ( demandRecord.getDemandType() == SupplyTask.SUPPLY_FUEL ) {
                if ( ( entity.getMaxUnitFuelLoad() - entity.getFuelQuantity() ) > SupplyVehicleEntity.MAX_UNITS * minLoadFraction ) {
                    demand = Math.min( SupplyVehicleEntity.MAX_UNITS, entity.getMaxUnitFuelLoad() - entity.getFuelQuantity() ) ;
                    type = SupplyTask.SUPPLY_FUEL ;
                }
            }

            // Actually fill in the slots.
            if ( type != -1 ) {
                int supplyEntityIndex = (int) slot[0];
                long startDeliveryTime = slot[1] ;
                SupplyTask outgoingTask =
                        new SupplyTask( entity.getId(), null,
                                startDeliveryTime,
                                startDeliveryTime + deliveryTime,
                                ( float ) demand, type ) ;
                outgoingTask.setId( getNextUID() );
                SupplyTask recoveryTask = new SupplyTask( null, null,
                        startDeliveryTime + deliveryTime,
                        startDeliveryTime + deliveryTime * 2,
                        0, SupplyTask.ACTION_RECOVERY ) ;
                recoveryTask.setId( getNextUID() );

                // DEBUG
                //System.out.println("SPLANNER:: ADDING TASK AT START= " + startTime + " AND PROJECTED END TIME="
                //        + endTime + " TASK=" + outgoingTask +
                //        " to slot " + suppEntities[supplyEntityIndex].getId() );

                schedules[ supplyEntityIndex].addScheduleElement( outgoingTask );
                schedules[ supplyEntityIndex].addScheduleElement( recoveryTask );
                allocatedNewTasks = true ;

                // System.out.println("Allocation " + outgoingTask + " for " + suppEntities[supplyEntityIndex].getId() + " satifying demand " + demandRecord + " of type=" + SupplyTask.getStringForAction(type) ) ;
                // Remove this break and allow multiple allocations.
                // break ;
                slot = ScheduleUtils.findNextEmptySlot( schedules, endTime-deliveryTime, endTime+deliveryTime, deliveryTime * 2 ) ;
                if ( slot == null ) {
                    break ;
                }
            }
        }
        return allocatedNewTasks ;
    }

    protected static void copyManueverPlans( WorldState source, WorldState dest ) {
        Iterator units = dest.getUnits() ;
        while ( units.hasNext() ) {
            UnitEntity destEntity = (UnitEntity) units.next();
            UnitEntity sourceEntity = (UnitEntity) source.getEntity( destEntity.getId() ) ;
            if ( sourceEntity.getManueverPlan() != null ) {
                destEntity.setManueverPlan( ( Plan ) sourceEntity.getManueverPlan().clone() ) ;
            }
        }
    }

    /**
     *
     * @param ws The current world state
     * @param delay The delay value after which planning starts.
     * @param entities
     * @param newSchedules
     */
    protected HashMap addExistingAndDelayedTasks( WorldState ws, long delay, SupplyVehicleEntity[] entities,
                                               ScheduleImpl[] newSchedules )
    {
        System.out.println("SPLANNER:: Adding existing tasks between " + ( ws.getTime() ) + " and " + (ws.getTime() + delay ) );
        HashMap result = new HashMap() ;
         long startTime = ws.getTime() + delay ;
         for (int i = 0; i < entities.length; i++) {
             SupplyVehicleEntity entity = entities[i];
             Plan p = entity.getSupplyPlan() ;
             if ( p != null ) {
                 int lastIndex = -1 ;

                 // DEBUG
                 int lastAction = -1 ;
                 for (int j=0;j<p.getNumTasks();j++) {
                     SupplyTask t = (SupplyTask) p.getTask(j) ;
                     if ( t.getStartTime() < startTime && !t.isComplete() ) {
                         newSchedules[i].addScheduleElement( ( SupplyTask ) t.clone() );
                         // System.out.println("ADDING EXISTING task to " + entity.getId() + ", " + t );
                         if ( t.getId() != null ) {
                            result.put( t.getId(), t.getId() ) ;
                         }
                         lastIndex = j ;
                         // DEBUG
                         lastAction = t.getType() ;
                     }
                 }

                 // DEBUG
//                 if ( lastAction != -1 && lastAction != SupplyTask.ACTION_RECOVERY ) {
//                     System.err.println("DEBUG Need to add next recovery task.");
//                 }

                 // Always add recovery tasks since recovery must always occur.
                 if ( lastIndex > 0 && lastIndex < p.getNumTasks() - 1 ) {
                     SupplyTask t = (SupplyTask) p.getTask( lastIndex + 1 ) ;
                     if ( t.getType() == SupplyTask.ACTION_RECOVERY ) {
                         newSchedules[i].addScheduleElement( ( SupplyTask ) t.clone() );
                         // System.out.println("ADDING EXISTING task " + entity.getId() + ", " + t );
                         if ( t.getId() != null ) {
                            result.put( t.getId(), t.getId() ) ;
                         }
                     }
                 }
             }
         }
        return result ;
    }

    public void plan( WorldState state, ArrayList customers, ArrayList supplyEntities ) {
        plan( state, 0, customers, supplyEntities );
    }

    /**
     *
     * @param customers A list of customer entities ids.
     * @param supplyEntities A list of supply entities ids being planned for.
     * @param delay Delay from the current start time.
     */
    public void plan( WorldState state, long delay, ArrayList customers, ArrayList supplyEntities ) {

        UnitEntity[] custEntities = new UnitEntity[ customers.size()] ;
        SupplyVehicleEntity[] supplyEnts = new SupplyVehicleEntity[ supplyEntities.size() ] ;

        for (int i=0;i<customers.size();i++) {
            custEntities[i] = (UnitEntity) state.getEntity( ( String ) customers.get(i) ) ;
        }
        for (int i = 0; i < supplyEntities.size(); i++) {
            String s = (String)supplyEntities.get(i);
            supplyEnts[i] = ( SupplyVehicleEntity ) state.getEntity( s ) ;
        }

        Plan[] manueverPlans = new Plan[ customers.size() ] ;

        long horizon = 0, baseTime = state.getTime() ;
        // Find the current planning horizon for the manuever plan.
        for (int i = 0; i < customers.size(); i++) {
            UnitEntity ue = custEntities[i] ;
            manueverPlans[i] = ue.getManueverPlan() ;
            if ( manueverPlans[i] != null && manueverPlans[i].getNumTasks() > 0 ) {
                for (int j = 0; j < manueverPlans[i].getNumTasks(); j++) {
                    Task t = manueverPlans[i].getTask( j ) ;
                    if ( t.getEndTime() > horizon ) {
                        horizon = t.getEndTime() ;
                    }
                }
            }
        }

        // The number of slots into the future we are going to plan.
        long timeIncrement = state.getDeltaTInMS() ;
        long slotSize =  (long) Math.ceil( ( Math.abs( state.getRecoveryLine() ) / VGWorldConstants.getSupplyVehicleMovementRate() ) *
                VGWorldConstants.MILLISECONDS_PER_SECOND ) ;
        int numSPlanDeltas = (int) Math.ceil( ( horizon-state.getTime() + slotSize ) / timeIncrement ) ;

        if ( numSPlanDeltas < 0 ) {
            System.out.println("Horizon " + ( horizon + slotSize ) / 1000.0 + " is less than current time " + state.getTimeInSeconds() + " secs." );
            return ;
        }

//        System.out.println( "SPlanner::Plan: MANUEVER PLAN HORIZON="
//             + horizon * VGWorldConstants.SECONDS_PER_MILLISECOND +
//             " secs., #deltas= " + numSPlanDeltas +
//             ", timeIncrement=" + timeIncrement +
//             ", resupply slot time=" + slotSize + " ms.")  ;

        // Make a bunch of new schedules.
        ScheduleImpl[] newSchedules = new ScheduleImpl[ supplyEnts.length ] ;
        for (int i = 0; i < newSchedules.length; i++) {
            newSchedules[i] = new ScheduleImpl();
        }

        float[][] fuelInventory = new float[ customers.size() ][numSPlanDeltas] ;
        int[][] ammoInventory = new int[ customers.size() ][numSPlanDeltas];

        // The starting lookahead is 2x the slotSize
        long lookahead = slotSize * 2 ;
        long numLookaheads = (long) Math.ceil( horizon / lookahead );
        long splanHorizon = numLookaheads * lookahead ;
        long numDeltasDelay = (long) Math.ceil( delay / ( double ) state.getDeltaTInMS() ) ;

        // Make a new model the current state.
        WorldStateModel currentState = new WorldStateModel( state, true, true ) ;

        // Do this because manuever plans are NOT cloned by default.
        copyManueverPlans( state, currentState );

        // Now, project forward by delay
        if ( numDeltasDelay > 0 ) {
            for (int i=0;i<numDeltasDelay;i++) {
                currentState.updateWorldState();
            }
        }

        // Find all non-completed supply tasks with an end time >= startTime and attach their corresponding recovery task
        // if necessary.  Add these to the schedule and plan around them.

        HashMap existingTaskIds = addExistingAndDelayedTasks( state, delay, supplyEnts, newSchedules ) ;

        //
        // This is where the planning starts.
        // Start filling in the schedules by iteratively projecting forward, adding
        // SupplyTasks and projecting forward again.
        //
        long endTime = state.getTime() + lookahead ;

        // First, check to see if we need to allocate any tasks based on the current demand.
        while ( true ) {
            boolean isAllocated = allocateNewTasksForDemand( currentState, custEntities, supplyEnts, newSchedules,
                fuelInventory, ammoInventory, state.getTime(), endTime ) ;
            if ( !isAllocated ) {
                break ;
            }
        }

        while ( endTime <= splanHorizon ) {
            //System.out.println("Projecting inventory to " + endTime );
            WorldStateModel endState = projectInventory( currentState,
                    custEntities, supplyEnts, newSchedules,
                    fuelInventory, ammoInventory, endTime + state.getDeltaTInMS()  );
            //System.out.println("End State=" + endState );

            // Prioritize based on final inventory levels. Ammo takes precedence over
            // fuel unless fuel falls below a critical threshold. Add items until the demand
            // is satisfied.
            // System.out.println("ALLOCATING FOR START TIME= " + state.getTime() + " ENDTIME=" + endTime );
            boolean isAllocated = allocateNewTasksForDemand( endState,
                    custEntities, supplyEnts, newSchedules,
                    fuelInventory, ammoInventory, state.getTime(),
                    endTime  ) ;

            if ( !isAllocated ) {
                endTime += state.getDeltaTInMS() ;
            }
        }

        plans = new HashMap() ;
        for (int i = 0; i < newSchedules.length; i++) {
            ScheduleImpl newSchedule = newSchedules[i];
            Enumeration e = newSchedule.getAllScheduleElements() ;
            ArrayList tasks = new ArrayList() ;
            while (e.hasMoreElements()) {
                Task task = (Task) e.nextElement();
                // Add only tasks which don't already exist.
                if ( existingTaskIds.get( task.getId() ) == null ) {
                    tasks.add( task ) ;
                }
                else {
                    //System.out.println("SPlanner:: DEBUG:: Skipping existing plan "
                    //        + task + " for entity " + supplyEnts[i].getId() );
                }
            }
            Collections.sort( tasks, new ScheduleElementComparator() );
            Plan p = new Plan( tasks ) ;
            plans.put( supplyEnts[i].getId(), p ) ;
        }
    }

    public HashMap getPlans() {
        return plans;
    }

    private static class ScheduleElementComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            ScheduleElement se1 = (ScheduleElement) o1, se2 = (ScheduleElement) o2 ;
            if ( se1.getStartTime() < se2.getStartTime() ) {
                return -1 ;
            }
            else if ( se2.getStartTime() < se1.getStartTime() ) {
                return 1 ;
            }
            return 0 ;
        }
    }

    protected HashMap plans ;

//        //
//        // Do a complete replan of all tasks which have not entered the commitment
//        // phase.
//        //
//        for (int i = 0; i < manueverPlans.length; i++) {
//            Plan manueverPlan = manueverPlans[i];
//            if ( manueverPlan != null ) {
//                EntityInfo info = state.getInfo( ( String ) customers.get(i) ) ;
//                UnitEntity entity = ( UnitEntity ) info.getEntity() ;
//                for (int j=0;j<manueverPlan.getNumActions();j++) {
//                    UnitTask task = (UnitTask) manueverPlan.getAction(j) ;
//
//                    // Estimate ammo demand for a single timeIncrement.
//                    if ( task.isEngageByFire() && !task.getTargets().isEmpty() ) {
//                        int startIndex = (int) Math.floor( ( task.getStartTime() - baseTime ) / timeIncrement ) ;
//                        int endIndex = (int) Math.ceil( ( task.getEndTime() - baseTime ) / timeIncrement ) ;
//                        for (int k=startIndex;k<endIndex;k++) {
//                            info.getModel().estimateAttritionValue( null, er, 0, timeIncrement ) ;
//                            ammoDemand[i][k] += er.getAmmoConsumed() ; ;
//                        }
//                    }
//
//                    // Estimate fuel demand for a single timeIncrement
//                    if ( task.isMove() )
//                    {
//                        double distance = Point2D.distance( task.getDestY(), task.getDestY(), entity.getX(), entity.getY() ) ;
//                        if ( distance > 1E-5) {
//                            double maxDistance = VGWorldConstants.UNIT_NOMINAL_MOVEMENT_RATE *
//                                    ( task.getEndTime() - task.getStartTime() ) * VGWorldConstants.SECONDS_PER_MILLISECOND  ;
//                            double fuelConsumption = Math.min( maxDistance, distance ) * VGWorldConstants.FUEL_CONSUMPTION_RATE ;
//                            // fuelDemand[i][k] ;
//                            int startIndex = (int) Math.floor( ( task.getStartTime() - baseTime ) / timeIncrement ) ;
//                            int endIndex = (int) Math.ceil( ( task.getEndTime() - baseTime ) / timeIncrement ) ;
//                            for (int k=startIndex;k<endIndex;k++) {
//                                // Estimated fuel consumption.
//                                fuelDemand[i][k] += fuelConsumption / ( endIndex - startIndex ) ;
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        // For each supply entity, create a set of deltaT slots. (e.g. NxMxdeltaTHorizon)
//        // Fill the slots with in progress tasks and
//        // mark them as non-changable.
//
//        for (int i=0;i<numSPlanDeltas;i++) {
//            for (int j=0;j<customers.size();j++) {
//
//
//
//
//            }
//        }

}
