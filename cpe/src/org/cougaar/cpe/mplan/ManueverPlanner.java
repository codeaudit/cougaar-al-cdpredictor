package org.cougaar.cpe.mplan;

import org.cougaar.cpe.*;
import org.cougaar.cpe.model.*;
import com.axiom.pspace.search.GraphSearch;
import com.axiom.pspace.search.SimpleSearch;

import java.util.*;
import java.io.PrintWriter;

public class ManueverPlanner extends Planner {
    private String id;
    private Plan zonePlan;

    public ManueverPlanner( String unit, ArrayList subordinateUnits) {
        this.id = unit ;
        this.subordinateUnits = subordinateUnits;
        vss = new VGSearchStrategy( subordinateUnits, this ) ;
    }

    public String getId()
    {
        return id;
    }

    public synchronized void setSubordinateUnits(ArrayList subordinateUnits) {
        this.subordinateUnits = (ArrayList) subordinateUnits.clone();
        vss.subordinateEntities = subordinateUnits ;
    }

    public ArrayList getSubordinateUnits() {
        return (ArrayList) subordinateUnits.clone();
    }

    /**
     * Release resources and reinitialize for another run.
     */
    public void release() {
        s.release() ;
        s = null ;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public int getNumDeltas() {
        return vss.getNumDeltasPerTask() ;
    }

    public void setInitialZone( Interval zone )
    {
       this.initialZone = zone ;
    }

    public Interval getInitialZone()
    {
        return initialZone;
    }

    public VGSearchStrategy getSearchStrategy()
    {
        return vss;
    }

    /**
     * Each task consists of an integral number of deltaT units.
     *
     * @param numDeltasPerTask  Number of deltaT units per task.
     * @param numDeltaIncrements Number of increments for planning movement.  This must be
     * less than numDeltasPerTask.  For example, if numDeltasPerTask is 4 and numDeltaIncrements
     * is 3, there will be a total of 5 possible future states for each entity (i.e. 0 movement
     * +/- 3 deltas of movement and +/- 4 deltas of movement.
     */
    public void setDeltaValues(int numDeltasPerTask, int numDeltaIncrements ) {
        vss.setDeltaValues( numDeltasPerTask, numDeltaIncrements );
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public int getMaxBranchFactor() {
        return maxBranchFactor;
    }

    public void setMaxBranchFactor(int maxBranchFactor) {
        this.maxBranchFactor = maxBranchFactor;
    }

    public BoundedBranchSearch getLastBoundedBranchSearch() {
        return s ;
    }

    public synchronized void plan( HashMap currentPlans, WorldState ws) {
        s = new BoundedBranchSearch( vss ) ;
        s.setMaxDepth( maxDepth );
        s.setMaxBranchingFactorPerPly( maxBranchFactor );
        WorldStateNode wsn = new WorldStateNode( null, new WorldStateModel( ws, true) ) ;
        s.init( wsn );
        s.run();
//        System.out.println("\nDUMPING PLANNER NODES at " + ws.getTime() );
//        s.dump();
        System.out.println("\nFinished one planning cycle...");
        System.out.println("#Number of open nodes=" + s.getNumExpandedOpenNodes() );
        System.out.println("#Number of closed nodes=" + s.getNumClosedNodes() );
    }

    /**
     *
     * @param currentPlans A map from ids to manuever plans corresponding to the current
     *  state of the world.
     * @param ws
     * @param delay A delay for the introduction of a new plan.
     */
    public synchronized void plan( WorldState ws, long delay ) {
        plan( ws, delay, zonePlan ) ;
    }

    /**
     * Plan with an externally zone schedule.
     *
     * @param ws
     * @param delay
     * @param psz
     */
    public synchronized void plan( WorldState ws, long delay, Plan zp ) {

        // Set the assumed zone schedule for this set of subordinates.
        // The zone schedule is intended to
        vss.setZoneSchedule( zp) ;

        WorldStateModel wsm = new WorldStateModel( ws, true ) ;
        int numDelayDeltas = (int) Math.floor( delay / ws.getDeltaTInMS() ) ;

        // Reset the plans and set the entities with them.
//        if ( currentPlans != null ) {
//            for (Iterator iterator = currentPlans.entrySet().iterator(); iterator.hasNext();) {
//                Map.Entry entry = (Map.Entry) iterator.next();
//                UnitEntity entity = (UnitEntity) wsm.getEntity( ( String ) entry.getKey() ) ;
//                entity.setManueverPlan( ( Plan ) entry.getValue() );
//            }
//        }

        // Update for delay deltas before conducting planning. The assumption here is that there is still a
        // plan that is supposed to be active.

        for (int i=0;i<numDelayDeltas;i++) {
            wsm.updateWorldState();
        }

        plan( null, wsm ) ;
    }

    public void dumpPlanNodes() {
        WorldStateNode bestNode = (WorldStateNode) s.getBestGraphNode() ;
        WorldStateNode current = bestNode ;
        ArrayList nodes = new ArrayList() ;
        while ( current != null ) {
            nodes.add( current ) ;
            current = (WorldStateNode) current.getParent() ;
        }

        for (int i = nodes.size()-1; i >= 0; i--) {
            WorldStateNode worldStateNode = (WorldStateNode)nodes.get(i);
            System.out.println( worldStateNode.toString() + "," + worldStateNode.getState()  );
            WorldState ws = worldStateNode.getState() ;
            for (int j=0;j<subordinateUnits.size();j++) {
                EntityInfo info = ws.getEntityInfo( ( String ) subordinateUnits.get(j) ) ;
                if ( info != null ) {
                    UnitEntity entity = (UnitEntity) info.getEntity() ;
                    if ( entity.getManueverPlan() != null && entity.getManueverPlan().getNumTasks() > 0) {
                        UnitTask task = (UnitTask) entity.getManueverPlan().getTask(0) ;
                        System.out.println("\tEntity=" + entity.getId() + ",task=" + task );
                    }
                }
            }
        }
    }


    public synchronized Object[][] getPlans() {
        Object[][] result = new Object[ subordinateUnits.size() ][2] ;

        WorldStateNode bestNode = (WorldStateNode) s.getBestGraphNode() ;

        for (int i = 0; i < subordinateUnits.size(); i++) {
            String s = (String)subordinateUnits.get(i);
            result[i][0] = s ;
            result[i][1] = new LinkedList() ;
        }

        // Go backwards from the best node and reconsider.
        WorldStateNode current = bestNode ;
        while ( current != null ) {
            WorldState ws = current.getState() ;
            for (int i=0;i<subordinateUnits.size();i++) {
                EntityInfo info = ws.getEntityInfo( ( String ) subordinateUnits.get(i) ) ;
                if ( info != null ) {
                    UnitEntity entity = (UnitEntity) info.getEntity() ;
                    if ( entity.getManueverPlan() != null && entity.getManueverPlan().getNumTasks() > 0) {
                        UnitTask task = (UnitTask) entity.getManueverPlan().getTask(0) ;
                        task.setDisposition( Task.FUTURE );
                        task.setExecutionResult( null );
                        LinkedList tasks = (LinkedList) result[i][1] ;
                        tasks.addFirst( task );
                    }
                }
            }
            current = (WorldStateNode) current.getParent() ;
        }

        boolean nonNullPlan = false ;
        for (int i=0;i<result.length;i++) {
            if ( result[i][1] != null ) {
                Collection c = (Collection) result[i][1] ;
                if ( !c.isEmpty() ) {
                    result[i][1] = new Plan( new ArrayList( ( Collection ) result[i][1]) ) ;
                    nonNullPlan = true ;
                }
            }
        }

        // This is a null plan.
        if ( !nonNullPlan) {
            return null ;
        }

        return result ;
    }

    public void dump( PrintWriter w ) {
        ArrayList openListByDepth = s.getOpenListByDepth() ;

        ArrayList[] closedListByDepth = new ArrayList[ openListByDepth.size() ] ;
        WorldStateNode bestNode = (WorldStateNode) s.getBestGraphNode() ;
        w.println("\n\nBEST NODE=");
        w.println(bestNode) ;

        w.println( "\n\n****************************\nCLOSED NODES:");
        for (int i = 0; i < closedListByDepth.length; i++) {
            closedListByDepth[i] = new ArrayList() ;
        }

        ArrayList closedList = s.getClosedList() ;
        for (int i = 0; i < closedList.size(); i++) {
            WorldStateNode wsn = (WorldStateNode) closedList.get(i);
            closedListByDepth[wsn.getDepth()].add( wsn) ;
        }

        for (int i = 0; i < closedListByDepth.length; i++) {
            ArrayList arrayList = closedListByDepth[i];
            for (int j = 0; j < arrayList.size(); j++) {
                WorldStateNode worldStateNode = (WorldStateNode) arrayList.get(j);
                w.println( worldStateNode.toString() ) ;
            }
        }

        w.println( "\n\n****************************\nOPEN NODES:");
        for (int i=0;i<openListByDepth.size();i++) {
            w.println( "\nOpen Nodes at Depth=" + i );
            ArrayList nodes = (ArrayList) openListByDepth.get(i) ;
            for (int j = 0; j < nodes.size(); j++) {
                WorldStateNode worldStateNode = (WorldStateNode) nodes.get(j);
                w.println( worldStateNode ) ;
            }
        }


    }


    Interval initialZone ;
    BoundedBranchSearch s ;
    VGSearchStrategy vss ;
    ArrayList subordinateUnits = new ArrayList() ;
    int maxDepth = 2 ;
    int maxBranchFactor = 60 ;

    public void setZonePlan(Plan plan)
    {
        zonePlan = plan ;
    }

}
