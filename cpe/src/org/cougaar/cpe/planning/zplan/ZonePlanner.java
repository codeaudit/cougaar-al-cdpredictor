package org.cougaar.cpe.planning.zplan;

import org.cougaar.cpe.model.*;
import org.cougaar.cpe.mplan.BoundedBranchSearch;
import org.cougaar.cpe.mplan.WorldStateNode;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Collection;
import java.util.Iterator;

public class ZonePlanner
{

    /**
     *
     * @param subordinateUnits
     * @param reference
     * @param numDeltaTPerPhase  The time granularity of zone-based planning.
     */
    public ZonePlanner(ArrayList subordinateUnits, ZoneWorld reference, int numDeltaTPerPhase ) {
        this.subordinateUnits = (ArrayList) subordinateUnits.clone();
        this.deltaTPerPhase = numDeltaTPerPhase ;
        strategy = new AggUnitSearchStrategy( subordinateUnits, numDeltaTPerPhase, reference ) ;

        // Estimated minimum time to transit between zones.
        double transitionTime = reference.getZoneGridSize() / (float) VGWorldConstants.getUnitNormalMovementRate() ;

        /**
         * Check if the number of delta t elements per planning cycle is less than the transition Time
         */
        if ( numDeltaTPerPhase * reference.getDeltaT() < transitionTime ) {
            throw new IllegalArgumentException( "Length of zone phase " +
                    numDeltaTPerPhase * reference.getDeltaT() + " is less than the transition time " + transitionTime ) ;
        }

    }

    public ArrayList getSubordinateUnits()
    {
        return subordinateUnits;
    }

    public void setMaxDepth(int maxDepth)
    {
        this.maxDepth = maxDepth;
    }

    public void setMaxBranchFactor(int maxBranchFactor)
    {
        this.maxBranchFactor = maxBranchFactor;
    }

    public int getMaxBranchFactor()
    {
        return maxBranchFactor;
    }

    public int getMaxDepth()
    {
        return maxDepth;
    }

    public int getDeltaTPerPhase()
    {
        return deltaTPerPhase;
    }

    /**
     * Start planning for zones on the basis of the current setup.
     *
     * @param ws The initial ZoneWorld to start with.
     * @param delay  The number of milliseconds to delay.
     */
    public synchronized void plan( ZoneWorld ws, long delay ) {

        int numDeltasPerDelay = (int) ( Math.floor( ( Math.floor( delay / (double) ws.getDeltaTInMS() ) / ( float) deltaTPerPhase ) ) * deltaTPerPhase ) ;

        // Clone so we don't disturb the real world.
        ws = (ZoneWorld) ws.clone() ;

        // Execute using the current zone plans for numDeltasPerDelay time units.
        for (int i=0;i<numDeltasPerDelay;i++) {
            ws.updateWorldState();
        }

        // Reset the zone plans to null.
        for (int i=0;i<ws.getNumAggUnitEntities();i++) {
            BNAggregate agg =(BNAggregate) ws.getAggUnitEntity(i) ;
            agg.setZonePlan( null );
        }

        s = new BoundedBranchSearch( strategy ) ;
        s.setMaxDepth( maxDepth );
        s.setMaxBranchingFactorPerPly( maxBranchFactor );
        WorldStateNode wsn = new WorldStateNode( null, ws ) ;
        s.init( wsn );
        s.run();

        System.out.println("\nFinished one planning cycle...");
        System.out.println("#Number of open nodes=" + s.getNumExpandedOpenNodes() );
        System.out.println("#Number of closed nodes=" + s.getNumClosedNodes() );
    }

    public synchronized Object[][] getPlans( boolean convertZonesToIntervals ) {
        Object[][] result = new Object[ subordinateUnits.size() ][2] ;

        WorldStateNode bestNode = (WorldStateNode) s.getBestGraphNode() ;
        ZoneWorld finalWorld = (ZoneWorld) bestNode.getState() ;
        long startTime = Long.MAX_VALUE;

        for (int i = 0; i < subordinateUnits.size(); i++) {
            String s = (String)subordinateUnits.get(i);
            result[i][0] = s ;
            result[i][1] = new LinkedList() ;
        }

        // Go backwards from the best node and reconsider.
        WorldStateNode current = bestNode ;
        while ( current != null ) {
            ZoneWorld ws = (ZoneWorld) current.getState() ;

            // Find out what the start time is.
            startTime = Math.min( ws.getTime(), startTime ) ;

            for (int i=0;i<subordinateUnits.size();i++) {
                BNAggregate entity = (BNAggregate) ws.getAggUnitEntity( ( String ) subordinateUnits.get(i) ) ;
                if ( entity.getZonePlan() != null ) {
                    Plan p = entity.getZonePlan() ;
                    ZoneTask zt = (ZoneTask) ( p.getTask(0) ).clone();
                    zt.setDisposition( Task.FUTURE ) ;
                    zt.setObservedResult( null ) ;
                    // Note All of these are converted back into real intervals.
                    if ( convertZonesToIntervals ) {
                        zt.startZone = ws.getIntervalForZone( ( IndexedZone ) zt.getStartZone() ) ;
                        zt.endZone = ws.getIntervalForZone( ( IndexedZone ) zt.getEndZone() ) ;
                    }
                    LinkedList tasks = (LinkedList) result[i][1] ;
                    tasks.addFirst( zt  );
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
                    //result[i][1] = new PhasedZoneSchedule( lowerValue, numZones, zoneGridSize, startTime, new ArrayList( ( Collection ) result[i][1]), timeUnitsPerIndex ) ;
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

    /**
     * Release any resources.
     */
    public void release() {
       s = null ;
    }

    protected int maxDepth= 5, maxBranchFactor = 30;
    protected BoundedBranchSearch s ;
    protected int deltaTPerPhase ;
    protected AggUnitSearchStrategy strategy ;
    protected ArrayList subordinateUnits ;
}
