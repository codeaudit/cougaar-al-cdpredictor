/**
 * User: wpeng
 * Date: May 1, 2003
 * Time: 7:03:47 PM
 */
package org.cougaar.cpe.mplan;

import com.axiom.pspace.search.Strategy;
import com.axiom.pspace.search.GraphNode;
import org.cougaar.cpe.model.*;
import org.cougaar.cpe.util.PowerSetEnumeration;
import org.cougaar.cpe.util.Counter;

import java.util.*;

public class TrackTargetSearchStrategy implements Strategy {
    private WorldStateInfo info;
    private ArrayList subordinates;

    /**
     * Whether we should add a secondary target or not. Default is true
     */
    private boolean addSecondary = true ;
    private double zoneSize;
    private int numZones ;
    private ArrayList[] zoneMap;

    public TrackTargetSearchStrategy(WorldStateInfo info, ArrayList subordinates ) {
        this.info = info;
        if ( subordinates == null ) {
            throw new IllegalArgumentException( "Subordinates must be non-null." ) ;
        }
        this.subordinates = (ArrayList) subordinates.clone();
        zoneSize = VGWorldConstants.getUnitRangeWidth() / 2 ;
        numZones = (int) Math.ceil( info.getBoardWidth() / zoneSize ) ;

        zoneMap = new ArrayList[ getNumZones() ] ;
        for (int i = 0; i < zoneMap.length; i++)
        {
            zoneMap[i] = new ArrayList();
        }
    }

    public int getNumZones() {
        return numZones ;
    }

    public double getMinXForZone( int i ) {
        return i * zoneSize ;
    }

    public double getUpperBoundXForZone( int i ) {
        return ( i + 1 ) * zoneSize ;
    }

    public int getZoneForLocation( double x ) {
        return (int) Math.floor( x / zoneSize ) ;
    }

    public int getZoneMultiplier() {
        return zoneMultiplier;
    }

    public void setZoneMultiplier(int zoneMultiplier) {
        this.zoneMultiplier = zoneMultiplier;
    }

    public int getMaxTargetsConsideredPerUnit() {
        return maxTargetsConsideredPerUnit;
    }

    public void setMaxTargetsConsideredPerUnit(int maxTargetsConsideredPerUnit) {
        this.maxTargetsConsideredPerUnit = maxTargetsConsideredPerUnit;
    }

    public int compare(Object n1, Object n2) {
        return 0;
    }

    public static class RangeComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            TargetEntity t1 = (TargetEntity) o1, t2 = (TargetEntity) o2;
            if (t1.getY() < t2.getY()) {
                return -1;
            } else if (t1.getY() > t2.getY()) {
                return 1;
            }
            return 0;
        }
    }

    public static class HorizontalDistanceComparator implements Comparator {

        public int compare(Object o1, Object o2)
        {
            return 0;
        }

        protected float xlocation ;
    }

    RangeComparator rangeComparatorInstance = new RangeComparator();


    /**
     * Get the sorted targets within a zone sorted by height.  The zone is defined
     * as the rectangular half open interval between lower and upper [lower,upper) and [0,height]
     *
     * @param lower
     * @param upper
     * @param list
     */
    private void getTargetsInZone( WorldStateModel wsm, double lower, double upper, double height, ArrayList list ) {
        Iterator iter = wsm.getTargets() ;

        list.clear();
        while (iter.hasNext()) {
            TargetEntity targetEntity = (TargetEntity) iter.next();
            if ( targetEntity.getX() >= lower && targetEntity.getX() < upper && targetEntity.getY() < height ) {
                list.add( targetEntity ) ;
            }
        }

        // Now, sort.
        Collections.sort( list, rangeComparatorInstance );
    }

    public void fillInZoneMap( WorldStateModel ws ) {
        for (int i=0;i<zoneMap.length;i++) {
            double xLower = getMinXForZone( i ) ;
            double xUpper = getUpperBoundXForZone( i ) ;
            ArrayList list = zoneMap[i] ;
            Collections.sort( list, rangeComparatorInstance );
            getTargetsInZone( ws, xLower, xUpper, ws.getBoardHeight(), list );
        }
    }

    public GraphNode[] expand2( GraphNode n ) {
        WorldStateNode wsn = (WorldStateNode) n;
        WorldStateModel ws = wsn.getState();

        if (subordinates == null || subordinates.size() == 0) {
            return null;
        }

        //
        // IMPORTANT!  Must do this first.
        //
        initLists() ;

        fillInZoneMap( ws ) ;

        long nextPlanningTime = ws.getTime() + numDeltas * ws.getDeltaTInMS() ;

        // Find assignments of units to zone map,  Choose only zones within the zone
        // of the current unit.  (Note that the current unit's zone is larger than its
        // share of the map.)
        for (int i = 0; i < subordinates.size(); i++) {
            String id = (String) subordinates.get(i);
            UnitEntity unitEntity = (UnitEntity) ws.getEntity(id);
            if (unitEntity != null) {
                // Identify the zone for this unit.
                ZoneSchedule zs = unitEntity.getZoneSchedule() ;
                Interval z = (Interval) zs.getZoneForTime( ws.getTime() ) ;

                // Find all zones within the unit zone.
                ArrayList taskList = (ArrayList) listOfUnitTaskLists.get(i) ;
                int lz = getZoneForLocation( z.getXLower() ) ;
                int uz = getZoneForLocation( z.getXUpper() ) ;

                ArrayList subZone = getSortedZones(uz, lz);

                if ( subZone.size() > 0 ) {
                    for (int j=0;j<maxTargetsConsideredPerUnit;j++) {
                        ArrayList list = (ArrayList) subZone.get(j) ;
                        TargetEntity tentity = (TargetEntity) list.get(0) ;
                        taskList.add( new UnitTask( ws.getTime(),
                                nextPlanningTime, tentity.getId()
                                 ) ) ;
                    }
                }

                // Search the expanded unit zone for any zones about to be critical and add them.

            }
        }

        // Now, find all combinations of the above and set them to be the next
        //

        return null ;
    }

    private ArrayList getSortedZones(int uz, int lz)
    {
        ArrayList subZone = new ArrayList( uz- lz + 1 ) ;
        for (int j=lz;j<=uz;j++ ) {
            // Add a unit task for covering the zone and the lowest target
            // within the zone,
            ArrayList targets = zoneMap[j] ;
            if ( targets.size() > 0 ) {
                subZone.add( targets ) ;
            }
        }
        Collections.sort( subZone, new Comparator() {
            public int compare(Object o1, Object o2)
            {
                ArrayList l1 = (ArrayList) o1, l2 = (ArrayList) o2 ;
                if ( l1.size() == 0 ) {
                    return 1 ;
                }
                if ( l2.size() == 0 ) {
                    return 0 ;
                }
                TargetEntity t1 = (TargetEntity) l1.get(0), t2 = (TargetEntity) l2.get(0) ;
                double diff = ( t1.getY() - t2.getY() ) ;
                if ( diff < 0 ) {
                    return -1 ;
                }
                if ( diff > 0 ) {
                    return 1 ;
                }
                return 0 ;
            }
        });
        return subZone;
    }

    /**
     * Expand based on an assignment of targets to entities.
     *
     * <p> Future versions can find groups of agents, e.g. agents which can be
     * covered by a single unit and which are in range.
     *
     * @param n
     * @return
     */
    public GraphNode[] expand(GraphNode n) {
        WorldStateNode wsn = (WorldStateNode) n;
        WorldStateModel ws = wsn.getState();

        if (subordinates == null || subordinates.size() == 0) {
            return null;
        }

        //
        // IMPORTANT!  Must do this first.
        //
        initLists() ;

//        Iterator iter = ws.getTargets();
//        ArrayList sortedTargets = new ArrayList();
//        while (iter.hasNext()) {
//            TargetEntity te = (TargetEntity) iter.next();
//        }
//        Collections.sort(sortedTargets, instance);

        // Identify the set of feasible targets in the zone < threshold
        // and assign them for all units.

        long nextPlanningTime = ws.getTime() + numDeltas * ws.getDeltaTInMS() ;

        for (int i = 0; i < subordinates.size(); i++) {
            String id = (String) subordinates.get(i);
            UnitEntity unitEntity = (UnitEntity) ws.getEntity(id);
            if (unitEntity != null) {
                // Identify the zone for this unit.
                ZoneSchedule zs = unitEntity.getZoneSchedule() ;

                ArrayList taskList = (ArrayList) listOfUnitTaskLists.get(i) ;

                if ( zs != null ) {
                    Interval z = (Interval) zs.getZoneForTime( ws.getTime() ) ;
                    double maxRange = unitEntity.getRangeShape().getBounds2D().getHeight() ;
                    feasibleTargets.clear();
                    getTargetsInZone( ws, z.getXLower(), z.getXUpper(),
                            maxRange + ( VGWorldConstants.getTargetMoveRate() * numDeltas ) ,
                            feasibleTargets );
                    if ( feasibleTargets.size() == 0 ) {
                        // neighboring zones.  Maximum zone size is a mulitple of the current
                        // zone, e.g.
                        double width2 = z.getXUpper() - z.getXLower() /2 ;
                        double center = z.getXCenter() ;
                        double newLower = center - width2 * maxExtendedZoneMultipler ;
                        double newUpper = center + width2 * maxExtendedZoneMultipler ;
                        getTargetsInZone( ws, newLower, newUpper,
                                maxRange + ( VGWorldConstants.getTargetMoveRate() * numDeltas ), feasibleTargets );
                    }

                    if ( feasibleTargets.size() > 0 ) {
                        int numTargetsConsidered = Math.min( feasibleTargets.size(),
                            maxTargetsConsideredPerUnit ) ;
                        for (int j = 0; j < numTargetsConsidered ; j++)
                        {
                            TargetEntity targetEntity =
                                    (TargetEntity) feasibleTargets.get(j);
                            UnitTask task = null ;
                            taskList.add( task = new UnitTask( ws.getTime(),
                                    nextPlanningTime,
                                    targetEntity.getId() ) ) ;

                            // Add a secondary target simply finding the "next" target
                            // to be considered.
                            if ( addSecondary && j < numTargetsConsidered - 1 ) {
                                targetEntity = (TargetEntity) feasibleTargets.get(j+1)   ;
                                task.addTarget( targetEntity.getId() );
                            }
                        }
                    }
                    else {
                        // Move back towards the center of the zone if fuel state is okay and I am
                        // currently outside the zone.   Otherwise, just stay put.
                        if ( ( unitEntity.getX() < z.getXLower() ||
                             unitEntity.getX() > z.getXUpper() ) &&
                             unitEntity.getFuelQuantity() > unitEntity.getMaxUnitFuelLoad() * 0.1 )
                        {
                            taskList.add( new UnitTask( ws.getTime(), nextPlanningTime, z.getXCenter(), 0 ) ) ;
                        }
                    }
                }
                else {
                    System.err.println("No zone for " + unitEntity );
                }
            }
        }

        // Enumerate the possible tasks and store the results.
        PowerSetEnumeration pe = new PowerSetEnumeration( listOfUnitTaskLists ) ;

        int tupleCount = 0 ;
        while ( pe.hasMoreElements() ) {
            pe.nextElement();
            tupleCount ++ ;
            // Reuse these arrays to avoid generating garbage.
            if ( ( tupleCount - 1 ) < tuples.size() ) {
                Object[] tuple = (Object[]) tuples.get( tupleCount - 1 ) ;
                // Just copy into the tuple size
                pe.getTuple( tuple ) ;
            }
            else {
                Object[] tuple = pe.getTuple() ;
                tuples.add( tuple ) ;
            }
        }

        for (int j = 0; j < tuples.size(); j++)
        {
            // Find how many elements are covering which targets,
            coveredUnits.clear() ;
            Object[] tasks = (Object[]) tuples.get(j) ;

            for (int i = 0; i < tasks.length; i++) {
                UnitTask task = (UnitTask) tasks[i];
                if ( task != null && task.getNumTargets() > 0) {
                    String targetId = task.getTarget(0) ;
                    coveredUnits.add( targetId );
                }
            }

            // If there are targets entering the critical range which are _not_ covered,
            // at most two units to engage them.  Consider only the N+2 lowest uncovered targets
            // where N== the number of unit entities.
            // getTargetsInZone( ws, 0, ws.getUpperX(), ws.getPenaltyHeight() +
            //        VGWorldConstants.getTargetMoveRate(), feasibleTargets );

            // If there are uncovered targets coming into range, consider moving
            // the nearest units to engage them (adds 1 action to 2 targets)

            // If any unit is unallocated, consider sending them to their adjacent zone
            // iff there are targets entering in range, otherwise, send them back to
            // their home zone.

            // If there are many targets in range, consider choosing a LOWEST_PRIORITY
            // mode.  This will spread out targeting for a large number of targets and
            // potentially suppress them.
        }

        return null;
    }

    private void initLists() {

        // Resize iff necessary.
        if ( listOfUnitTaskLists.size() < subordinates.size() ) {
            for (int i=listOfUnitTaskLists.size();i<subordinates.size();i++) {
                listOfUnitTaskLists.add( new ArrayList() ) ;
            }
        }
        else if ( listOfUnitTaskLists.size() > subordinates.size() ) {
            // Remove stuff from the list.
            for (int i=listOfUnitTaskLists.size()-1;i>=subordinates.size();i--) {
                listOfUnitTaskLists.remove( i )  ;
            }
        }

        // Clear the lists.
        for (int i = 0; i < listOfUnitTaskLists.size(); i++) {
            ArrayList list = (ArrayList)listOfUnitTaskLists.get(i);
            list.clear();
        }


        coveredUnits.clear();
    }

    /**
     * Consider maxTargets/ unit during each search within the target's
     * zone.  Only consider combinations of units which are seperated by
     * one zone or less.
     */
    protected int maxTargetsConsideredPerUnit = 3;

    /**
     * If there are N units, then the zone is of size
     * ( boardWidth / N ) * zoneMultiplier ;
     */
    protected int zoneMultiplier = 2;


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

    private ArrayList feasibleTargets = new ArrayList( 10 ) ;
    private ArrayList listOfUnitTaskLists = new ArrayList() ;
    private Counter coveredUnits = new Counter() ;
    private static ArrayList tuples = new ArrayList() ;

    /**
     * Number of deltas for each ply of search depth and hence the actual
     * granularity is at the level of 1 deltas.
     */
    protected int numDeltas = 8 ;
    protected int numDeltaIncrements = 2 ;

    /**
     * Look for targets in neighboring zones up to this factor * the zone size.
     */
    protected double maxExtendedZoneMultipler = 2 ;

}
