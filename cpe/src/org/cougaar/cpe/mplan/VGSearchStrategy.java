package org.cougaar.cpe.mplan;

import com.axiom.pspace.search.GraphNode;
import com.axiom.pspace.search.Strategy;
import org.cougaar.cpe.model.*;
import org.cougaar.cpe.planning.zplan.ZoneTask;
import org.cougaar.cpe.planning.zplan.ZoneWorld;
import org.cougaar.cpe.util.PowerSetEnumeration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class VGSearchStrategy implements Strategy {

    private Plan zoneSchedule ;
    private ManueverPlanner manueverPlanner ;
    private static final double MAX_COVERAGE = 1.4;
    private static final double MAX_COVERAGE_CRITICAL = 2.0 ;

    /**
     * @param subordinateEntities Search only for the subordinate entities.
     */
    public VGSearchStrategy( ArrayList subordinateEntities, ManueverPlanner mp ) {
        setSubordinateEntities( subordinateEntities );
        this.manueverPlanner = mp ;
    }

    public ArrayList getSubordinateEntities() {
        return subordinateEntities;
    }

    public void setSubordinateEntities(ArrayList subordinateEntities) {
        this.subordinateEntities = new ArrayList() ;
        for (int i = 0; i < subordinateEntities.size(); i++) {
            String s = (String)subordinateEntities.get(i);
            this.subordinateEntities.add( s ) ;
        }
    }

    /**
     * This is modified to return the default value (e.g. the initialZone) if we
     * are before the beginning of the schedule or the last zone if we are beyond the end of the schedule.
     *
     * @param wsm
     * @return
     */
    public ZoneTask getCurrentZoneTask( WorldStateModel wsm ) {
        ZoneTask currentTask = null;

        if ( zoneSchedule != null ) {
            currentTask = (ZoneTask) zoneSchedule.getNearestTaskForTime( wsm.getTime() ) ;
        }

        return currentTask ;
    }

    public int compare(Object n1, Object n2) {
        double coverageThreshold = 1E-5 ;

        WorldStateNode wsn1 = (WorldStateNode) n1, wsn2 = (WorldStateNode) n2 ;
        WorldStateModel wsm1 = wsn1.getState(), wsm2 = wsn2.getState() ;
        if ( wsm1.getTime() != wsm2.getTime() ) {
            throw new IllegalArgumentException( "WorldStateNodes have different times " + wsm1.getTime() + " and " + wsm2.getTime() ) ;
        }

        ZoneTask currentTask = getCurrentZoneTask( wsm1 ) ;

        // It is most important for the current zone to be preserved.  This is even more important than having a good score.
        Interval currentZoneInterval = null ;
        if ( currentTask != null ) {
            currentZoneInterval = ZoneWorld.interpolateIntervals(currentTask, wsm1.getTime());
        }

        // Measure how much "outside" we are of the zone.  Penalize out of zone entities more harshly.
        if ( currentZoneInterval != null ) {
            if ( wsn1.getZoneCoverage() == 1 ) {
                wsn1.setZoneCoverage( WorldStateUtils.computeZoneCoverage( wsn1.getState(), subordinateEntities, currentZoneInterval ));
            }
            if ( wsn2.getZoneCoverage() == 1 ) {
                wsn2.setZoneCoverage( WorldStateUtils.computeZoneCoverage( wsn2.getState(), subordinateEntities, currentZoneInterval ));
            }
            float zcd = wsn1.getZoneCoverage() - wsn2.getZoneCoverage() ;
            if ( Math.abs( zcd ) > 0 ) {
                return ( zcd > 0 ) ? -1 : 1 ;
            }
        }

        // Look at the current projected score next,
        // TODO Make this a function only of the current ZoneSchedule we have been given! Ignore all scoring events
        // TODO outside the current zone.  This way, we have less incentive to deviate from the zone schedule.
        if ( wsn1.getScore() < wsn2.getScore() ) {
            return 1 ;
        }
        else if ( wsn1.getScore() > wsn2.getScore() ) {
            return -1 ;
        }

        //
        // Look at critical area coverage
        //
        if ( wsn1.getCriticalCoverage() == -1 ) {
            wsn1.setCriticalCoverage(
                    WorldStateUtils.computeCoverage( wsn1.getState(), subordinateEntities, WorldStateUtils.COVERAGE_CRITICAL,
                            WorldStateUtils.LIMITED_INVERSE_FUNCTION, MAX_COVERAGE_CRITICAL, currentZoneInterval, false ));
        }
        if ( wsn2.getCriticalCoverage() == -1 ) {
            wsn2.setCriticalCoverage(
                    WorldStateUtils.computeCoverage( wsn2.getState(), subordinateEntities, WorldStateUtils.COVERAGE_CRITICAL,
                            WorldStateUtils.LIMITED_INVERSE_FUNCTION, MAX_COVERAGE_CRITICAL, currentZoneInterval, false ));
        }
        double cc = wsn1.getCriticalCoverage() - wsn2.getCriticalCoverage() ;
        if ( Math.abs( cc ) > coverageThreshold ) {
            return ( cc > 0 ) ? -1 : 1 ;
        }

        //
        // Look at overall coverage only within the current zone interval.  This is not exactly accurate but an approximation.
        //
        if ( wsn1.getTargetCoverage() == -1 )
        {
            wsn1.setTargetCoverage( WorldStateUtils.computeCoverage( wsn1.getState(), subordinateEntities, WorldStateUtils.COVERAGE_IN_RANGE,
                            WorldStateUtils.LIMITED_INVERSE_FUNCTION, MAX_COVERAGE, currentZoneInterval, true ) ) ;
        }
        if ( wsn2.getTargetCoverage() == -1 ) {
            wsn2.setTargetCoverage(
                    WorldStateUtils.computeCoverage( wsn2.getState(), subordinateEntities, WorldStateUtils.COVERAGE_IN_RANGE,
                            WorldStateUtils.LIMITED_INVERSE_FUNCTION, MAX_COVERAGE, currentZoneInterval, true  ));
        }
        double cd = wsn1.getTargetCoverage() - wsn2.getTargetCoverage() ;

        if ( Math.abs( cd ) > coverageThreshold ) {
            return ( cd > 0 ) ? -1 : 1 ;
            // return  -cd ;  // Prefer arrangements with high coverage over low.
        }

        if ( wsn1.getInternalZoneCoverage() == 1 ) {
            wsn1.setInternalZoneCoverage( -WorldStateUtils.computeInternalZoneCoverage( wsn1.getState(), subordinateEntities, currentZoneInterval ) );
        }
        if ( wsn2.getInternalZoneCoverage() == 1 ) {
            wsn2.setInternalZoneCoverage( -WorldStateUtils.computeInternalZoneCoverage( wsn2.getState(), subordinateEntities, currentZoneInterval ) );
        }
        float izcd = wsn1.getInternalZoneCoverage() - wsn2.getInternalZoneCoverage() ;
        if ( izcd > 0 ) {
            return -1 ;
        }
        else if ( izcd < 0 ) {
            return 1 ;
        }

        // Look at projected fuel consumption.  This will minimize motion by favoring units with less fuel consumption.
        double h2 = ( wsn1.getFuelConsumption() - wsn2.getFuelConsumption() ) ;
        if ( h2 != 0 ) {
            if ( h2 > 0 ) {
                return 1 ;
            }
            else if ( h2 < 0 ) {
                return -1;
            }
        }

        // Finally, look at ammo consumption?
        return wsn1.ammoConsumption - wsn2.ammoConsumption ;
    }


//    /**
//     * Sort in ascending order based on score. Higher scores are better.
//     *
//     * @param n1
//     * @param n2
//     * @return
//     */
//    public int compare2(Object n1, Object n2) {
//        WorldStateNode wsn1 = (WorldStateNode) n1, wsn2 = (WorldStateNode) n2 ;
//        if ( wsn1.score < wsn2.score ) {
//            return 1 ;
//        }
//        else if ( wsn1.score > wsn2.score ) {
//            return -1 ;
//        }
//
//        if ( wsn1.getCriticalCoverage() == -1 ) {
//            wsn1.setCriticalCoverage( WorldStateUtils.computeCriticalCoverage( wsn1.getState()) );
//        }
//        if ( wsn2.getCriticalCoverage() == -1 ) {
//            wsn2.setCriticalCoverage( WorldStateUtils.computeCriticalCoverage( wsn2.getState() ));
//        }
//        double cc = wsn1.getCriticalCoverage() - wsn2.getCriticalCoverage() ;
//        if ( cc != 0 ) {
//            return ( cc > 0 ) ? -1 : 1 ;
//        }
//
//        if ( wsn1.getOverallCoverage() == -1 )
//        {
//            wsn1.setOverallCoverage( WorldStateUtils.computeInRangeCoverage( wsn1.getState()) ) ;
//        }
//        if ( wsn2.getOverallCoverage() == -1 ) {
//            wsn2.setCriticalCoverage(
//                    WorldStateUtils.computeCoverage( wsn2.getState(), WorldStateUtils.COVERAGE_IN_RANGE,
//                            WorldStateUtils.LIMITED_INVERSE_FUNCTION, 1.4 ));
//            wsn2.setOverallCoverage( WorldStateUtils.computeInRangeCoverage( wsn2.getState()) ) ;
//        }
//        double cd = wsn1.getOverallCoverage() - wsn2.getOverallCoverage() ;
//
//        if ( cd != 0 ) {
//            return ( cd > 0 ) ? -1 : 1 ;
//            // return  -cd ;  // Prefer arrangements with high coverage over low.
//        }
//        else { // Preference for ammoConsumption over fuel Consumption.
//            int h2 = (int) ( wsn1.fuelConsumption - wsn2.fuelConsumption) ;
//            if ( h2 != 0 ) {
//                return h2 ;
//            }
//            else {
//                return wsn1.ammoConsumption - wsn2.ammoConsumption ;
//            }
//        }
//    }

    public int getNumDeltasPerTask() {
        return numDeltas;
    }

    /**
     * The granularity of the planning.  Should always be a multiple of the
     * delta per task.
     * @return
     */
    public int getNumDeltaIncrements() {
        return numDeltaIncrements;
    }

    public void setDeltaValues(int numDeltasPerTask, int numDeltaIncrements) {
        if ( numDeltasPerTask % numDeltaIncrements != 0 ) {
            System.err.println("Warning numDeltaIncrements " + numDeltaIncrements +
                    " is not a multiple of numDeltas" + numDeltas );
        }

        this.numDeltas = numDeltasPerTask ;
        this.numDeltaIncrements = numDeltaIncrements;
    }

    private static ArrayList listOfVirtualUnitLists = new ArrayList() ;
    private static ArrayList allVirtualUnits = new ArrayList() ;
    private static ArrayList tuples = new ArrayList() ;

    public GraphNode[] expand(GraphNode n) {
        // Based on the current world state, generate a new (discrete) task which
        // can include movement to locations, engaging a target etc.

        WorldStateNode wsn = (WorldStateNode) n ;
        WorldStateModel ws = wsn.getState() ;
        //System.out.println("EXPANDING node index " + wsn.id + ",depth=" + wsn.getDepth() + " with total score " + wsn.getState().getScore() );
        // System.out.print( "E(id=" + wsn.id + ",d=" + wsn.getDepth() + ")" );

        ZoneTask currentTask = getCurrentZoneTask( ws ) ;
        // Interpolate the
        Interval currentZoneInterval = null ;
        if ( currentTask != null ) {
            currentZoneInterval = ZoneWorld.interpolateIntervals(currentTask, ws.getTime() );
        }

        // Clear all data structures!
        // Always clear these guys!
        allVirtualUnits.clear();
        if ( listOfVirtualUnitLists.size() < subordinateEntities.size() ) {
            for (int i=listOfVirtualUnitLists.size();i<subordinateEntities.size();i++) {
                listOfVirtualUnitLists.add( new ArrayList() ) ;
            }
        }
        else if ( listOfVirtualUnitLists.size() > subordinateEntities.size() ) {
            // Remove stuff from the list.
            for (int i=listOfVirtualUnitLists.size()-1;i>=subordinateEntities.size();i--) {
                listOfVirtualUnitLists.remove( i )  ;
            }
        }
        for (int i = 0; i < listOfVirtualUnitLists.size(); i++) {
            ArrayList list = (ArrayList)listOfVirtualUnitLists.get(i);
            list.clear();
        }


        // Iterate through the subordinates and generate potential actions.
        for (int i=0;i<subordinateEntities.size();i++) {
            EntityInfo info = ws.getEntityInfo( ( String )  subordinateEntities.get(i) )  ;
            if ( info == null ) {
                continue ;
            }

            UnitEntity e = (UnitEntity) info.getEntity() ;

            ArrayList virtualUnits = (ArrayList) listOfVirtualUnitLists.get(i) ;
            //= new ArrayList() ;
            //listOfVirtualUnitLists.add( virtualUnits );

            // Generate a task for move to different positions for each entity
            // independently, i.e. create tasks that move to 0, x + dx, x + 2dx, etc.
            float maxDistanceMovedPerDeltaT = ( float ) ( VGWorldConstants.getUnitNormalMovementRate() *
                    ws.getDeltaT() ) ;

            // Consider all possible movements less than the max distance moved.
            boolean addedZero = false ;

            // This is the end time for all the tasks.
            long endTime = ( long ) ( ws.getTime() + numDeltas *
                    ws.getDeltaT() * VGWorldConstants.MILLISECONDS_PER_SECOND ) ;

            for ( int j=-numDeltas;j<=numDeltas;j+= numDeltaIncrements) {
                if ( j == 0 ) {
                    addedZero = true ;
                }
                float targetX = ( e.getX() + j * maxDistanceMovedPerDeltaT ) ;

                //
                // Handle the zone assignment
                //
                if ( currentZoneInterval != null ) {
                    // Handle zone assignments.

                    // If I am currently outside the zone, only move towards the zone or hold my position!
//                    if ( e.getX() < currentZoneInterval.getXLower() && targetX < e.getX() ) {
//                        break ;
//                    }
//                    if ( e.getX() > currentZoneInterval.getXUpper() && targetX > e.getX() ) {
//                        break ;
//                    }
//
//                    // If I am currently inside the zone, do not move outside the zone!
//                    if ( e.getX() >= currentZoneInterval.getXLower() && e.getY() <= currentZoneInterval.getXUpper() ) {
//                       if ( targetX < currentZoneInterval.getXLower() || targetX > currentZoneInterval.getXUpper() ) {
//                           break ;
//                       }
//                    }
                }

                double fuelConsumption = j * maxDistanceMovedPerDeltaT * VGWorldConstants.getUnitFuelConsumptionRate() ;

                // If fuel falls below 0, can't move this task unless there is a resupply
                // but we search anyway, under the presumption that resupply might
                // occur during the task execution.
                //if ( fuelConsumption >= e.getFuelQuantity() ) {
                    // Don't try to move outside the board or to exhaust the units fuel
                    // supplies.
                    if ( targetX >= 0 && targetX <= ws.getUpperX() ||
                         fuelConsumption > e.getFuelQuantity() )
                    {
                        // This is an empty task for just movement.
                        UnitTask t = new UnitTask( ws.getTime(), endTime, targetX,
                           e.getY() ) ;
                        UnitEntity ue = (UnitEntity) e.clone() ;
                        ue.setManueverPlan( new Plan(t));
                        virtualUnits.add( ue ) ;
                        allVirtualUnits.add( ue ) ;
                    }
                //} End fuel consumption block.
            }
            if ( !addedZero ) {
                UnitTask t = new UnitTask( ws.getTime(), endTime, e.getX(), e.getY() ) ;
                UnitEntity ue = (UnitEntity) e.clone() ;
                ue.setManueverPlan( new Plan(t));
                virtualUnits.add( ue ) ;
                allVirtualUnits.add( ue ) ;
            }

        }

        // Remove any zero length lists.
//        for (Iterator iterator = listOfUnitTaskLists.iterator(); iterator.hasNext();) {
//            ArrayList arrayList = (ArrayList) iterator.next();
//            if ( arrayList.size() == 0 ) {
//                iterator.remove() ;
//            }
//        }

        // Now, consider targets in range for each task (and each virtual unit associated with each task.)
        // Always assume that targets are engaged.
        WorldStateModel projectWorldState = new WorldStateModel( ws ) ;
        projectWorldState.projectTargets( numDeltas, allVirtualUnits ) ;

        // Reset all the plans within each of the virtual units.
        for (int i = 0; i < allVirtualUnits.size(); i++) {
            UnitEntity unitEntity = (UnitEntity) allVirtualUnits.get(i);
            Plan p = unitEntity.getManueverPlan() ;
            UnitTask t = (UnitTask) p.getTask( 0 ) ;
            t.setDisposition( Task.FUTURE );
            t.setEngageByFire( true );
            t.setExecutionResult( null );
        }

        // Now find all combinations of actions by enumerating each and evey tuple.
        PowerSetEnumeration pe = new PowerSetEnumeration( listOfVirtualUnitLists ) ;
        int tupleCount = 0 ;
        try {
            while ( pe.hasMoreElements() ) {
                pe.nextElement();
                Object[] tuple = null ;
                // Reuse these arrays to avoid generating garbage.
                if ( tupleCount  < tuples.size() ) {
                    tuple = (Object[]) tuples.get( tupleCount ) ;
                    // Just copy into the current tuple.
                    pe.getTuple( tuple ) ;
                }
                else {
                    tuple = pe.getTuple() ;
                    tuples.add( tuple ) ;
                }
                tupleCount ++ ;
            }
        }
        catch ( NoSuchElementException e ) {
            e.printStackTrace();
            System.out.println("ListOfVirtualUnitLists=" + listOfVirtualUnitLists );
        }

        // Now, execute simulated combinations of tasks (e.g. enumerate each set of tasks)
        // and target selections and see which score best.
        GraphNode[] successors = new GraphNode[tupleCount] ;
        for (int i = 0; i < tupleCount; i++) {
            WorldStateModel futureWorldState = new WorldStateModel( ws, true ) ;
            Object[] objects = ( Object[] ) tuples.get(i);
            // Set the plans for each.
            for (int j = 0; j < objects.length; j++) {
                UnitEntity unitEntity = (UnitEntity) objects[j];
                EntityInfo info = futureWorldState.getEntityInfo( unitEntity.getId() ) ;
                UnitEntity ue = (UnitEntity) info.getEntity() ;
                ue.setManueverPlan( ( Plan ) unitEntity.getManueverPlan().clone() );
            }

            // Project to the next state for the number of deltas.
            for (int j=0;j<numDeltas;j++) {
                futureWorldState.updateWorldState();
            }

            // Compute the total fuel/ammo cost.
            double fuelConsumed = 0;
            int ammoConsumed = 0 ;
            for (int j = 0; j < subordinateEntities.size(); j++) {
                String s = (String)subordinateEntities.get(j);
                EntityInfo info = futureWorldState.getEntityInfo( s ) ;
                UnitEntity ui = (UnitEntity) info.getEntity() ;
                // Get the one step unit task.
                UnitTask uit = (UnitTask) ui.getManueverPlan().getTask( 0 ) ;
                // Get the ammo and execution result.
                ExecutionResult er = (ExecutionResult) uit.getObservedResult() ;

                // This should never happen.
                if ( er == null ) {
                    System.err.println("\nNull ER result encountered!" );
                    System.err.println("Tuples=" );
                    for (int k = 0; k < objects.length; k++) {
                        UnitEntity object = (UnitEntity) objects[k];
                        System.out.println( object  );
                    }
                    System.err.println(" Projected ws " + futureWorldState );
                    Iterator iter = futureWorldState.getUnits() ;
                    while (iter.hasNext()) {
                        UnitEntity unitEntity = (UnitEntity) iter.next();
                        System.out.println( unitEntity + "," + unitEntity.getManueverPlan() );
                    }
                }
                fuelConsumed += er.getFuelConsumption() ;

                // Get all ammo consumption.
                for  (int k=0; k < er.getNumResults();k++) {
                    EngageByFireResult efr = er.getResult(k) ;
                    ammoConsumed += efr.getAmmoConsumed() ;
                }
            }

            // TODO Tracking fuel consumption has becoming redundant since the WorldState itself tracks it now
            WorldStateNode nwsn = new WorldStateNode( ( WorldStateNode ) n, futureWorldState ) ;
            nwsn.ammoConsumption = wsn.ammoConsumption + ammoConsumed ;
            nwsn.fuelConsumption = wsn.fuelConsumption + fuelConsumed ;
            // nwsn.recomputeScore();
            successors[i] = nwsn ;
        }
        n.addSuccessors( successors );
        return successors ;
    }

    public GraphNode expand(GraphNode n, int i) {
        return null;
    }

    public int getNumDescendants(GraphNode n) {
        return 0;
    }

    public void initNode(GraphNode n) {
    }

    public boolean isEqual(GraphNode n1, GraphNode n2) {
        return false;
    }

    public boolean isGoalNode(GraphNode n) {
        return false;
    }

    public GraphNode makeNode() {
        return null;
    }

    public void updateParent(GraphNode n1, GraphNode n2) {
    }

    /**
     * Number of deltas for each ply of search depth and hence the actual
     * granularity is at the level of 1 deltas.
     */
    protected int numDeltas = 8 ;
    protected int numDeltaIncrements = 2 ;

    /**
     * The entities for which this planner is actually planning.
     */
    ArrayList subordinateEntities ;

    /**
     * The zone schedule for this planning round.
     * @param sz
     */
    public void setZoneSchedule( Plan plan)
    {
        zoneSchedule = plan ;
    }
}
