package org.cougaar.cpe.planning.zplan;

import com.axiom.pspace.search.Strategy;
import com.axiom.pspace.search.GraphNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collections;
import java.util.Comparator;

import org.cougaar.cpe.mplan.WorldStateNode;
import org.cougaar.cpe.model.*;
import org.cougaar.cpe.util.PowerSetEnumeration;

/**
 * This search strategy assumes that the subordinate units commanded must always cover the entire space.
 */
public class AggUnitSearchStrategy implements Strategy
{
    private ArrayList subordinateEntities ;
    private ZoneWorld referenceWorld ;
    private int deltaTPerPhase;

    /**
     * @param subordinateEntities Search only for the subordinate entities.
     * @param refState
     */
    public AggUnitSearchStrategy( ArrayList subordinateEntities, int deltaTPerPhase, ZoneWorld refState ) {
        this.subordinateEntities = subordinateEntities;
        this.deltaTPerPhase = deltaTPerPhase ;
        this.referenceWorld = new ZoneWorld( refState ) ;
        validateZoneAssignments(referenceWorld);
    }

    /**
     * Validate the initial zone assignements to make sure the initial coverage is complete.
     * @param world
     */
    public void validateZoneAssignments(ZoneWorld world)
    {
        ArrayList[] zones = new ArrayList[ world.getNumZones() ] ;
        for (int i = 0; i < zones.length; i++)
        {
            zones[i] = new ArrayList();
        }

        // Validate that the number of deltas per phase is great enough to enable
        // movement

        // Get the subordinate aggregates in this list
        ArrayList aggregates = new ArrayList();
        for (int i=0;i<subordinateEntities.size();i++) {
            String aggName = (String) subordinateEntities.get( i ) ;
            BNAggregate unit = (BNAggregate) world.getAggUnitEntity( aggName ) ;
            if ( unit == null ) {
                throw new RuntimeException( "Unexpected condition: Aggregated unit " + aggName + " does not exist." ) ;
            }

            aggregates.add( unit ) ;

            if ( unit.getCurrentZone() == null ) {
                throw new RuntimeException( "No initial zone set for aggregat unit " + unit ) ;
            }
            IndexedZone z = (IndexedZone) unit.getCurrentZone() ;
            for (int j=z.getStartIndex();j<=z.getEndIndex();j++) {
                zones[j].add( unit ) ;
            }
        }

        for (int i = 0; i < zones.length; i++)
        {
            ArrayList zone = zones[i];
            if ( zone.size() == 0 ) {
               System.err.println("Warning:: Uncovered zone " + i );
            }
            if ( zone.size() > 1 ) {
                System.err.println("Warning:: Overlapping zone " + i + ", zones=" + zone );
            }
        }

        // Now sorting zones.
        Collections.sort( aggregates, new Comparator() {
            public int compare(Object o1, Object o2)
            {
                BNAggregate a1 =(BNAggregate) o1, a2 = (BNAggregate) o2 ;
                IndexedZone z1 = (IndexedZone) a1.getCurrentZone(), z2 = (IndexedZone) a2.getCurrentZone() ;
                if ( z1.getStartIndex() < z2.getStartIndex() ) {
                    return -1 ;
                }
                if ( z1.getStartIndex() > z2.getStartIndex() ) {
                    return 1 ;
                }
                if ( z1.getEndIndex() < z2.getEndIndex() ) {
                    return -1 ;
                }
                if ( z1.getEndIndex() > z2.getEndIndex() ) {
                    return 1 ;
                }
                return 0 ;
            }
        });

        // Now, these are subordinates, from "left" to "right"
        subordinateEntities.clear();
        for (int i = 0; i < aggregates.size(); i++) {
          Aggregate aggregate = (Aggregate)aggregates.get(i);
          subordinateEntities.add( aggregate.getId() ) ;
        }

    }

    public ArrayList getSubordinateEntities() {
        return subordinateEntities;
    }

    /**
     * The number of delta ts to expand per planning phase. This should be a multiple of
     *
     * @return
     */
    public int getDeltaTPerPhase()
    {
        return deltaTPerPhase;
    }

    public void setSubordinateEntities(ArrayList subordinateEntities) {
        this.subordinateEntities = subordinateEntities;
    }

    public int compare(Object n1, Object n2)
    {
        WorldStateNode wsn1 = (WorldStateNode) n1, wsn2 = (WorldStateNode) n2 ;

        // Maximize score
        if ( wsn1.getScore() > wsn2.getScore() ) {
            return -1 ;
        }
        else if ( wsn1.getScore() < wsn2.getScore() ) {
            return 1 ;
        }

        // Minimize fuel consumption
        if ( wsn1.getFuelConsumption() < wsn2.getFuelConsumption() ) {
            return -1 ;
        }
        else if ( wsn1.getFuelConsumption() > wsn2.getFuelConsumption() ) {
            return 1 ;
        }

        return 0 ;
    }


//    /**
//     * Find the set of zones associated with the current units.
//     *
//     * @param zw The zoneworld.
//     * @param units The names of the units.
//     * @param zones The assignment from unit names to zones.
//     */
//    public static void findZoneAssignements( ZoneWorld zw, ArrayList units, HashMap zones ) {
//
//        for (int i = 0; i < units.size(); i++) {
//            String id = (String)units.get(i);
//            Aggregate aue = zw.getAggUnitEntity( id ) ;
//
//            if ( aue == null ) {
//                throw new RuntimeException( "Aggregate entity " + id + " does not exist." ) ;
//            }
//
//            for (int j=0;j<aue.getNumSubEntities();j++) {
//                String sid = aue.getSubEntityName(j) ;
//                // Now, find what zone we belong to.
//            }
//        }
//
//    }
//
//    /**
//     * Take a current zone world and find deconflicted zones based on it.
//     * @param zw
//     * @param deconflictedZones
//     */
//    public static void deconflict( ZoneWorld zw, ArrayList deconflictedZones ) {
//
//    }

    public GraphNode[] expand(GraphNode n)
    {
        WorldStateNode wsn = (WorldStateNode) n ;
        ZoneWorld zw = (ZoneWorld) wsn.getState() ;

        // Create a list of lists.
        ArrayList listOfZoneLists = new ArrayList() ;
        for (int i = 0; i < subordinateEntities.size(); i++) {
            listOfZoneLists.add( new ArrayList() ) ;
        }
        // System.out.println("\nEXPANDING node at depth " + wsn.getDepth() + ", world=" + zw + ", id=" + wsn.getId() );

        // Now, consider each subordiwnate entity. It can do the following set of actions.
        // 0. No action
        // 1. Grow 1 left
        // 2. Grow 1 right/
        // 3. Shrink 1 left
        // 4. Shrink 1 right
        // 5. 1 + 2  ( Expand both ways )
        // 6. 1 + 4  ( Shift to the left )
        // 7. 2 + 3  ( Shift to the right )
        // 8. 3 + 4  ( Shrink both sides. )

        for (int i=0;i<subordinateEntities.size();i++) {
            BNAggregate ag = (BNAggregate) zw.getAggUnitEntity( ( String ) subordinateEntities.get(i) ) ;
            ArrayList zoneList = (ArrayList) listOfZoneLists.get(i) ;

            PhasedZoneSchedule psz = (PhasedZoneSchedule) ag.getZoneSchedule() ;
            if ( ag != null ) {
                IndexedZone currentZone = (IndexedZone) ag.getCurrentZone() ;
                // Case 0
                zoneList.add( currentZone.clone() ) ;

                // Case 1 Grow on the left
                if ( currentZone.getStartIndex() >= 1 ) {
                    IndexedZone newZone = (IndexedZone) currentZone.clone() ;
                    newZone.setStartIndex( currentZone.getStartIndex() - 1 );
                    zoneList.add( newZone ) ;

                    // Case 6 Shift to the left.
                    newZone = (IndexedZone) newZone.clone() ;
                    newZone.setEndIndex( currentZone.getEndIndex() - 1 ) ;
                }

                // Case 2 Grow on the right
                if ( currentZone.getEndIndex() < zw.getNumZones() - 1 ) {
                    IndexedZone newZone = (IndexedZone) currentZone.clone() ;
                    newZone.setEndIndex( currentZone.getEndIndex() + 1 ) ;
                    zoneList.add( newZone ) ;

                    // Case 7 Shift to the left.
                    newZone = (IndexedZone) newZone.clone() ;
                    newZone.setStartIndex( currentZone.getStartIndex() + 1 );
                }

                if ( currentZone.getNumZone() > 2 ) {
                    // Case 3 (Shrink on the left)
                    IndexedZone newZone = (IndexedZone) currentZone.clone() ;
                    newZone.setStartIndex( currentZone.getStartIndex() + 1 );
                    zoneList.add( newZone ) ;

                    // Case 4 (Shrink on the right)
                    newZone = ( IndexedZone ) currentZone.clone() ;
                    newZone.setEndIndex( currentZone.getEndIndex() - 1 );
                    zoneList.add( newZone ) ;
                }

                // Case 5 Expand both ways.
                if ( currentZone.getStartIndex() >= 1 &&  currentZone.getEndIndex() < currentZone.getNumZone() - 1 )
                {
                    IndexedZone newZone = (IndexedZone) currentZone.clone() ;
                    newZone.setStartIndex( currentZone.getStartIndex() - 1 );
                    newZone.setEndIndex( currentZone.getEndIndex() + 1 ) ;
                    zoneList.add( newZone ) ;
                }

                // Case 8 Shrink both sides. Must at least be of zone size 3
                if ( currentZone.getEndIndex() - currentZone.getStartIndex() >= 2 ) {
                    IndexedZone newZone = (IndexedZone) currentZone.clone() ;
                    newZone.setStartIndex( currentZone.getStartIndex() + 1 );
                    newZone.setEndIndex( currentZone.getEndIndex() - 1 );
                }

            }
        }

        // However, these are _not_ indepedent since zones cannot overlap. Eliminate all overlapping zones!
        // (Are empty zones permissible?)

        // Now find all combinations of actions by enumerating each and evey tuple.
        PowerSetEnumeration pe = new PowerSetEnumeration( listOfZoneLists ) ;

        Object[] tuple = new Object[ listOfZoneLists.size() ];
        IndexedZone[] zones = new IndexedZone[ tuple.length ] ;

        ArrayList nextNodes = new ArrayList() ;
        while ( pe.hasMoreElements() ) {
            pe.nextElement();
            pe.getTuple( tuple ) ;
            for (int i = 0; i < tuple.length; i++)
            {
                IndexedZone z  = (IndexedZone) tuple[i];
                zones[i] = z ;
            }

            // Eliminate layouts which have holes or overlap, or swapped zones.
            if ( !checkZoneValidity( zw, zones ) ) {
                continue ;
            }

//            System.out.print( "\n\tExpanding new target zones:" );
//            for (int i = 0; i < zones.length; i++)
//            {
//                IndexedZone zone = zones[i];
//                System.out.print( subordinateEntities.get(i) + "=" + zone + " " );
//            }

            // Create the next world state by assigning the above zones and advancing time by requisite amount.
            ZoneWorld nextWorld = new ZoneWorld( zw ) ;
            for (int i=0;i<subordinateEntities.size();i++) {
                BNAggregate agg = (BNAggregate) nextWorld.getAggUnitEntity( ( String ) subordinateEntities.get(i) ) ;
                ZoneTask zt = new ZoneTask( zw.getTime(), deltaTPerPhase * zw.getDeltaTInMS() + zw.getTime(),
                        (IndexedZone) agg.getCurrentZone() , zones[i] ) ;
                Plan p = new Plan( zt ) ;
                agg.setZonePlan( p ) ;
            }
            for (int i=0;i<getDeltaTPerPhase();i++) {
                nextWorld.updateWorldState();
            }

            float fuelConsumption = 0 ;
            int ammoConsumption = 0 ;
            for (int i=0;i<subordinateEntities.size();i++) {
                BNAggregate agg = (BNAggregate) nextWorld.getAggUnitEntity( ( String ) subordinateEntities.get(i) ) ;
                Plan p = agg.getZonePlan() ;
                ZoneTask t = (ZoneTask) p.getTask( 0 ) ;
                ZoneExecutionResult zer = (ZoneExecutionResult) t.getObservedResult() ;
                fuelConsumption += zer.getFuelConsumption() ;
                ammoConsumption += zer.getAmmoConsumption() ;
            }

            WorldStateNode nwsn = new WorldStateNode( wsn, nextWorld ) ;
            nwsn.setFuelConsumption( wsn.getFuelConsumption() + fuelConsumption );
            nwsn.setAmmoConsumption( wsn.getAmmoConsumption() + ammoConsumption );
            // System.out.println("\tAdding node " + nextWorld + ", score=" + nextWorld.getScore() + ",fuel=" + nwsn.getFuelConsumption() + ",amno=" + nwsn.getAmmoConsumption() );
            nextNodes.add( nwsn ) ;
        }

        GraphNode[] result = new GraphNode[ nextNodes.size() ] ;
        result = (GraphNode[]) nextNodes.toArray( result ) ;
        n.setSuccessors( result ) ;
        return result ;
    }

    public boolean checkZoneValidity( ZoneWorld zw, IndexedZone[] zones ) {
        for (int i = 0; i < zones.length; i++)
        {
            IndexedZone zone = zones[i];
            // Check for left holes or out of bounds.
            if ( i == 0 && zone.getStartIndex() != 0 ) {
                return false ;
            }

            // Check for left continuity.
            if ( i > 0 ) {
                IndexedZone prev = zones[i-1] ;
                // There is an overlap.
                if ( prev.getEndIndex() >= zone.getStartIndex() ) {
                    return false ;
                }
                // There is a hole.
                if ( prev.getEndIndex() < zone.getStartIndex() - 1 ) {
                    return false ;
                }
            }

            // Check for right continuity.
            if ( i < zones.length - 1 ) {
                IndexedZone next = zones[i+1] ;
                if ( zone.getEndIndex() >= next.getStartIndex() ) {
                    return false ;
                }
                if ( zone.getEndIndex() < next.getStartIndex() - 1 ) {
                    return false ;
                }
            }

            // Check for rightmost hole or out of bounds.
            if ( i == zones.length - 1 ) {
                if ( zone.getEndIndex() != zw.getNumZones() - 1 ) {
                    return false ;
                }
            }
        }
        return true ;
    }

    public GraphNode expand(GraphNode n, int i)
    {
        return null;
    }

    public int getNumDescendants(GraphNode n)
    {
        return 0;
    }

    public void initNode(GraphNode n)
    {
    }

    public boolean isEqual(GraphNode n1, GraphNode n2)
    {
        return false;
    }

    public boolean isGoalNode(GraphNode n)
    {
        return false;
    }

    public GraphNode makeNode()
    {
        return null;
    }

    public void updateParent(GraphNode n1, GraphNode n2)
    {
    }

}
