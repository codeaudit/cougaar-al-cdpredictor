
package org.cougaar.tools.alf;

import org.cougaar.tools.castellan.util.MultiHashSet;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Find where this agent is executing.
 */
public class AgentNodeSchedule
{
    /**
     * Describes the lifecycle of an agent on a given node.
     */
    public static class AgentNodeScheduleRecord implements Schedulable {
        public AgentNodeScheduleRecord( String nodeName, long startTime) {
            this.nodeName = nodeName;
            this.creationTime = creationTime;
        }

        public boolean isInstantaneous() {
            return false;
        }

        /**
         * Creation time of this agent on this node.
         */
        public long getStartTime() {
            return creationTime ;
        }

        public long getEndTime() {
            return stopTime ;
        }

        /**
         * The end time can be changed as observations are made.
         */
        public void setEndTime( long time ) {
            stopTime = time ;
        }

        String nodeName ;

        /**
         * When this agent was created.
         */
        long creationTime;
        /**
         * Default values is Long.MAX_VALUE.  This indicates the stopTime is unknown.
         */
        long stopTime = Long.MAX_VALUE ;
    }

    public Schedule getSchedule() {
        return schedule ;
    }

    public void addNodeRecord( String nodeName, long startTime ) {
        if ( nodeName == null ) {
            throw new IllegalArgumentException( "Node name must be non-null." ) ;
        }
        AgentNodeScheduleRecord an = new AgentNodeScheduleRecord( nodeName, startTime ) ;
        schedule.addScheduledItem( an );
        records.put( nodeName, an ) ;
    }

    public Object[] getRecords( String nodeName ) {
        return records.getObjects( nodeName ) ;
    }

    public int getNumRecords() {
        return schedule.getSize() ;
    }

    protected MultiHashSet records = new MultiHashSet() ;

    /**
     * An assignment of agentNodeScheduleRecords to non-overlapping times.
     */
    protected Schedule schedule = new Schedule( false ) ;


}
