package org.cougaar.cpe.planning.zplan;

import org.cougaar.cpe.model.*;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class BNAggregate extends Aggregate
{
    /**
     * The next zone.
     */
    IndexedZone targetZone ;
    /**
     * Current zone to be moved to.
     */
    Zone currentZone ;
    /**
     * Previous zone that was used.
     */
    IndexedZone previousZone ;

    // Plan zonePlan ;

    int totalAmmo ;
    float totalFuel ;
    protected Plan zonePlan ;

    public BNAggregate( String id, String[] entities )
    {
        super( Aggregate.TYPE_BN, id, entities);
        totalFuel = entities.length * VGWorldConstants.getMaxUnitFuelLoad() ;
        totalAmmo = entities.length * VGWorldConstants.getMaxUnitAmmoLoad() ;
    }

    public String toString()
    {
        return "[id=" + getId() + ",zone=" + currentZone + "]" ;
    }

    public Plan getZonePlan() {
        return zonePlan;
    }

    public Object clone()
    {
        BNAggregate entity = new BNAggregate( getId(), this.entities ) ;
        entity.totalAmmo = totalAmmo ;
        entity.totalFuel = totalFuel ;

        // Clone the zone schedule.
        if ( zs != null ) {
            entity.zs = (ZoneSchedule) zs.clone() ;
        }
        if ( zonePlan != null ) {
            entity.zonePlan = (Plan) zonePlan.clone() ;
        }

        if ( targetZone != null ) {
            entity.targetZone = (IndexedZone) targetZone.clone();
        }

        if ( currentZone != null ) {
            entity.currentZone = ( Zone ) currentZone.clone() ;
        }
        if ( previousZone != null ) {
            entity.previousZone = (IndexedZone) previousZone.clone() ;
        }
        return entity ;
    }

    /**
     * Update for a single cycle.
     * @param zw
     */
    public void update(ZoneWorld zw)
    {
        Task lastExecutedTask = null ;
        if ( zonePlan != null && zonePlan.getNumTasks() > 0 ) {
              lastExecutedTask = executeZonePlan( zw );
        }

        // Now do engagement
        // BNEntityEngagementModel model = zw.getEngagementModel() ;
        EngageByFireResult er = zw.engage( this );
        if ( zonePlan != null && lastExecutedTask != null) {
            if ( lastExecutedTask != null ) {
                ZoneExecutionResult zer = (ZoneExecutionResult) lastExecutedTask.getObservedResult() ;
                zer.setAmmoConsumption( zer.getAmmoConsumption() + er.getAmmoConsumed() );
                zer.setAttritionValue( zer.getAttritionValue() + er.getAttritValue() );
            }
        }

        // Ignore fuel consumption for now.
//        ZoneExecutionResult zer = zs.getResult( targetZoneScheduleIndex ) ;
//        if ( zer != null ) {
//            zer.setAttritionValue( zer.getAttritionValue() + er.getAttritValue() );
//        }
    }

    /**
     * Execute the zone plan for one timestep.
     *
     * @param zw
     * @return The last task executed.
     */
    private Task executeZonePlan( ZoneWorld zw ) {
        // Find the first zone.
        if ( zonePlan.isAfterLastTask() ) {
            return null ;
        }
        if ( zonePlan.isBeforeFirstTask() ) {
            zonePlan.nextTask() ;
        }

        ZoneTask task = (ZoneTask) zonePlan.getCurrentTask() ;
        if ( zw.getTime() < task.getStartTime() ) {
            return null ;
        }

        task.setDisposition( Task.ACTIVE );
        if ( task.getObservedResult() == null ) {
            ZoneExecutionResult zr;
            task.setObservedResult( zr = new ZoneExecutionResult( task.getStartTime(), currentZone ) ) ;
        }

        // Transition to the next task immediately.
        if ( zw.getTime() >= ( task.getEndTime() - zw.getDeltaTInMS() ) ) {
            task.setDisposition( Task.PAST );
            ZoneExecutionResult result = (ZoneExecutionResult) task.getObservedResult() ;
            currentZone = (IndexedZone) task.getEndZone() ;
            result.setEndZone( task.getEndZone() );
            result.setFuelConsumption( result.getFuelConsumption() +
                    ZoneWorld.calculateAdditionalFuelConsumption( zw.getIntervalForZone( ( IndexedZone ) task.getStartZone() ) ,
                                                                  zw.getIntervalForZone( ( IndexedZone ) task.getEndZone() ),
                                                                  getNumSubEntities() ) );
            zonePlan.nextTask() ;
        }
        return task ;
    }

    // Calculate the average distance travelled between two zones, equally spaced.
//    /**
//     * We make the following assumptions:
//     * - Once a commitment is made to change zones, the commitment must be carried out.
//     * - When deciding to change the target zones, we always try to catch up by going to the appropriate
//     *   scheduled zone.
//     *
//     * @param zs
//     * @param zw
//     */
//    private void moveBNUnit( PhasedZoneSchedule zs, ZoneWorld zw)
//    {
//        // First, consider the location of the BN units and whether they are correctly placed in the current and/or target zone.
//        // If they are too far outside the zone, insert some delay slots.
//
//        // Always finish the transition.
//        if ( isTransiting ) {
//            double transitionTime = WorldStateUtils.calculateMaxDistance( zw, currentZone, targetZone ) / VGWorldConstants.UNIT_NORMAL_MOVEMENT_RATE ;
//            double timeElapsed = zw.getTimeInSeconds() - zoneTransitionTime * VGWorldConstants.SECONDS_PER_MILLISECOND ;
//            if ( timeElapsed >= transitionTime ) {
//                previousZone = currentZone ;
//                currentZone = targetZone ;
//
//                // Calculate the fuel at the end.
//                if ( targetZoneScheduleIndex != -1 ) {
//                    ZoneExecutionResult zer = zs.getResult( targetZoneScheduleIndex ) ;
//                    float fc = ZoneWorld.calculateAdditionalFuelConsumption( zw.getIntervalForZone( previousZone ) ,
//                            zw.getIntervalForZone( currentZone ), getNumSubEntities() ) ;
//                    zer.setFuelConsumption( fc ) ;
//                }
//                targetZoneScheduleIndex = -1 ;
//                isTransiting = false ;
//                targetZone = null ;
//            }
//            else {
//                // We are still transiting between zones. Do nothing.
//                return ;
//            }
//        }
//
//        int currentIndex = zs.getIndexForTime( zw.getTime() ), nextScheduledIndex = -1 ;
//        IndexedZone currentScheduledZone = null, nextScheduledZone = null ;
//        double nextScheduledZoneTime = -1;
//
//        if ( currentIndex == -1 ) {
//            // We are after the scheduled time
//            if ( zw.getTime() >= zs.getEndTime() ) {
//                // Try to get to the last zone if we haven't already been there.
//                IndexedZone iz = (IndexedZone) zs.getZone( zs.getNumIndices() - 1 ) ;
//                ZoneExecutionResult zr = zs.getResult( zs.getNumIndices() - 1 ) ;
//                if ( !iz.equals(currentZone) && zr == null ) {
//                    nextScheduledZone = iz ;
//                    nextScheduledZoneTime = zw.getTime() ;  // Do this immediately.
//                    nextScheduledIndex = zs.getNumIndices() - 1 ;
//                }
//            }
//            else if ( zw.getTime() < zs.getStartTime() ) { // We are before the current time, see if we need to start making a transion.
//                nextScheduledZone = (IndexedZone) zs.getZone( 0 ) ;
//                nextScheduledZoneTime = zs.getStartTime() ;
//                nextScheduledIndex = 0 ;
//            }
//            else {
//                throw new RuntimeException( "Unexpected condition time=" + zw.getTime() + ",schedule=" + zs ) ;
//            }
//        }
//        else { // The current zone is on the schedule
//            //currentScheduledZone = (IndexedZone) zs.getZone( currentIndex ) ;
//
//            // Get the next zone scheduled for the given time.
//            if ( currentIndex < zs.getNumIndices() - 1 ) {
//                nextScheduledZone = (IndexedZone) zs.getZone( currentIndex + 1 ) ;
//                nextScheduledZoneTime = zs.getStartTimeForZone( currentIndex + 1 ) ;
//                nextScheduledIndex = currentIndex + 1 ;
//            }
//            else {
//                // We are on the last index. Already, but the current zone is not the same as the
//                // last zone.
//                int lastIndex = zs.getNumIndices() - 1 ;
//                if ( !currentZone.equals( zs.getZone( lastIndex ) ) ) {
//                    nextScheduledZone =  (IndexedZone) zs.getZone( lastIndex ) ;
//                    nextScheduledZoneTime = zs.getStartTimeForZone( lastIndex ) ;
//                    nextScheduledIndex = lastIndex ;
//                }
//            }
//        }
//
//        // We don't know where to go next. Do nothing.
//        if ( nextScheduledZone == null ) {
//            return ;
//        }
//
//        if ( nextScheduledZoneTime == -1 ) {
//            throw new RuntimeException("Unexpected condition: nextScheduledZoneTime is not initialized.");
//        }
//
//        // We are already at the correct zone.  Just create the zer regardless.
//        if ( currentZone.equals(nextScheduledZone) ) {
//            targetZoneScheduleIndex = nextScheduledIndex ;
//            ZoneExecutionResult zer = new ZoneExecutionResult( currentZone, targetZone, zoneTransitionTime ) ;
//            zs.setResult( targetZoneScheduleIndex, zer );
//            return ;
//        }
//
//        double timeToChangeZones = WorldStateUtils.calculateMaxDistance( zw, nextScheduledZone, currentZone ) /
//                 VGWorldConstants.UNIT_NORMAL_MOVEMENT_RATE ;
//
//        if ( timeToChangeZones >=
//             ( nextScheduledZoneTime * VGWorldConstants.SECONDS_PER_MILLISECOND - zw.getTimeInSeconds() - zw.getDeltaT() ) )
//        {
//            targetZone = nextScheduledZone ;
//            targetZoneScheduleIndex = nextScheduledIndex ;
//            isTransiting = true ;
//            zoneTransitionTime = zw.getTime() ;
//            ZoneExecutionResult zer = new ZoneExecutionResult( currentZone, targetZone, zoneTransitionTime ) ;
//            zs.setResult( targetZoneScheduleIndex, zer );
//            return ;
//        }
//
//    }


    boolean isTransiting = false ;
    long zoneTransitionTime = -1 ;

    /**
     * Which zone we are transiting to.
     */
    int targetZoneScheduleIndex = -1;

    public void setTotalAmmo(int totalAmmo)
    {
        this.totalAmmo = totalAmmo;
    }

    public void setTotalFuel(float totalFuel)
    {
        this.totalFuel = totalFuel;
    }

    public int getTotalAmmo()
    {
        return totalAmmo;
    }

    public float getTotalFuel()
    {
        return totalFuel;
    }

    public Zone getCurrentZone()
    {
        return currentZone;
    }

    public void setCurrentZone(Zone currentZone)
    {
        this.currentZone = currentZone;
    }

    public void setPreviousZone(IndexedZone previousZone)
    {
        this.previousZone = previousZone;
    }

    public Zone getPreviousZone()
    {
        return previousZone;
    }

    public void setZonePlan(Plan p) {
        zonePlan = p ;
    }

}
