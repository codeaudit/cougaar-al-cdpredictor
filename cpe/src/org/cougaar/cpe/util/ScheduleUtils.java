package org.cougaar.cpe.util;

import org.cougaar.planning.ldm.plan.ScheduleImpl;
import org.cougaar.planning.ldm.plan.ScheduleElement;
import org.cougaar.tools.alf.Schedule;

import java.util.Collection;
import java.util.Iterator;

import org.cougaar.cpe.model.SupplyTask;
import org.cougaar.cpe.model.Plan;
import org.cougaar.cpe.model.Task;

/**
 * User: wpeng
 * Date: Apr 28, 2003
 * Time: 5:25:35 PM
 */
public class ScheduleUtils {

    /**
     * Delay all elements in the plan until at least the minimum time.
     * @param plan
     * @param minimumTime
     */
    public static int delayPlanUntilTime( Plan plan, long minimumTime ) {
        Task previousTask = null ;
        int delayed = 0 ;
        for (int i=0;i<plan.getNumTasks();i++) {
            Task t = plan.getTask(i) ;
            if ( previousTask == null && t.getStartTime() < minimumTime ) {
                delayed++ ;
                long duration = t.getEndTime() - t.getStartTime() ;
                t.setStartTime( minimumTime );
                t.setEndTime( minimumTime + duration );
                previousTask = t ;
            }
            else if ( previousTask != null ) {
                if ( t.getStartTime() < previousTask.getEndTime() ) {
                    delayed++ ;
                    long duration = t.getEndTime() - t.getStartTime() ;
                    t.setStartTime( previousTask.getEndTime() );
                    t.setEndTime( t.getStartTime() + duration );
                    previousTask = t ;
                }
            }
        }
        return delayed ;
    }

    public static long findNextEmptySlot( ScheduleImpl schedule, long start, long end, long slotSize ) {
        Collection c = schedule.getOverlappingScheduleElements(start,end) ;
        if ( c.isEmpty() ) {
            return start ;
        }
        long minOpenSlotTime = Long.MAX_VALUE;
        for (Iterator iter = c.iterator(); iter.hasNext(); ) {
            ScheduleElement se = (ScheduleElement) iter.next() ;

            // Look before the se and see if a slot is present.
            if ( se.getStartTime() > start && ( se.getStartTime() - slotSize > start ) ) {
                Collection c2 =
                        schedule.getOverlappingScheduleElements( se.getStartTime() - slotSize, se.getStartTime()) ;
                if ( c2.isEmpty() ) {
                    minOpenSlotTime = Math.min( se.getStartTime() - slotSize, minOpenSlotTime ) ;
                }
            }
            // Look after the se and see if a slot is present.
            else if ( se.getEndTime() < end && se.getEndTime() + slotSize < end ) {
                Collection c2 =
                        schedule.getOverlappingScheduleElements( se.getEndTime(), se.getEndTime() + slotSize ) ;
                if ( c2.isEmpty() ) {
                    minOpenSlotTime = Math.min( se.getEndTime(), minOpenSlotTime ) ;
                }
            }
        }
        return minOpenSlotTime ;
    }

    /**
     * Find a slot that falls within start and end i.e. the slot must fall
     * withint the half open interval [start, end).
     *
     * @param schedules
     * @param start
     * @param end
     * @param slotSize
     * @return
     */
    public static long[] findNextEmptySlot( ScheduleImpl[] schedules, long start, long end, long slotSize ) {
        long[] startTimes = new long[ schedules.length ] ;
        for (int i=0;i<startTimes.length;i++) {
            startTimes[i] = Long.MAX_VALUE ;
        }

        for (int i = 0; i < schedules.length; i++) {
            ScheduleImpl schedule = schedules[i];
            startTimes[i] = findNextEmptySlot( schedule,  start, end, slotSize ) ;
        }
        long minStartTime = Long.MAX_VALUE ;
        int minIndex = -1 ;
        for (int i = 0; i < startTimes.length; i++) {
            long startTime = startTimes[i];
            if ( startTime < minStartTime ) {
                minIndex = i ;
                minStartTime = startTime ;
            }
        }

        if ( minStartTime < Long.MAX_VALUE ) {
            return new long[] { minIndex, minStartTime } ;
        }

        return null ;
    }

    public static boolean isNonOverlappingSchedule( ScheduleImpl schedule ) {
        for (Iterator iterator = schedule.iterator(); iterator.hasNext();) {
            ScheduleElement scheduleElement = (ScheduleElement) iterator.next();
            Collection c = schedule.getOverlappingScheduleElements( scheduleElement.getStartTime(), scheduleElement.getEndTime() ) ;
            if ( !c.isEmpty() ) {
                return false ;
            }
        }
        return true ;
    }

    public static final void main( String[] args ) {

        ScheduleImpl s = new ScheduleImpl() ;
        s.add( new SupplyTask( null, null, 5000, 10000, 10, SupplyTask.SUPPLY_AMMO ) ) ;
        s.add( new SupplyTask( null, null, 10000, 15000, 0, SupplyTask.ACTION_RECOVERY ) ) ;
        s.add( new SupplyTask( null, null, 25000, 30000, 10, SupplyTask.SUPPLY_FUEL ) ) ;

        long slotTime = findNextEmptySlot( s, 10000, 30000, 10000 ) ;
        long slotTime2 = findNextEmptySlot( s, 10000, 30000, 15000 ) ;
        long slotTime3 = findNextEmptySlot( s, 10000, 50000, 15000 ) ;
        long slotTime4 = findNextEmptySlot( s, 25000, 35000, 10000 ) ;

        System.out.println("Schedule=" + s );
        System.out.println("Slot time=" + slotTime );
        System.out.println("Slot time2=" + slotTime2 );
        System.out.println("Slot time3=" + slotTime3 );
        System.out.println("Slot time4=" + slotTime4 );


    }
}
