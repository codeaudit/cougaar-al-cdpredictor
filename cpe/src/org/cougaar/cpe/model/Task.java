package org.cougaar.cpe.model;

import org.cougaar.planning.ldm.plan.ScheduleElement;
import org.cougaar.core.util.UID;

import java.util.Date;

public abstract class Task implements java.io.Serializable, ScheduleElement {
    public static final int FUTURE = 0 ;
    public static final int ACTIVE = 1 ;
    public static final int PAST = 2 ;
    protected UID id ;

    public boolean abutSchedule(ScheduleElement se) {
        long tstime = se.getStartTime();
        long tetime = se.getEndTime();

        return ( tstime == getEndTime() ||
                 tetime == getStartTime() );
    }

    public void resetTask() {
        disposition = FUTURE ;
    }

    public int getDisposition() {
        return disposition;
    }

    public void setDisposition(int disposition) {
        this.disposition = disposition;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public Date getEndDate() {
        return new Date( getStartTime() ) ;
    }

    public Date getStartDate() {
        return new Date( getEndTime() ) ;
    }

    public boolean included(Date date) {
        return included( date.getTime() ) ;
    }

    public boolean included(long time) {
        return time >= startTime && time < endTime ;
    }

    public boolean isTimeBeforeTask( long time ) {
        return time < startTime ;
    }

    public boolean isTimeAfterTask( long time ) {
        return time >= endTime ;
    }

    public boolean overlapSchedule(ScheduleElement se) {
        long tstime = se.getStartTime();
        long tetime = se.getEndTime();

        return ( tstime < getEndTime() &&
                 tetime > getStartTime() );
    }

    public Task(long startTime, long endTime) {
        this.endTime = endTime;
        this.startTime = startTime;
        if ( endTime < startTime ) {
            throw new RuntimeException( "Start time " + startTime + " cannot be after end time " + endTime ) ;
        }
    }

    public abstract TaskResult getObservedResult() ;

    public abstract Object clone() ;

    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append( "[" ) ;
        toString( result );
        result.append( "]" ) ;
        return result.toString() ;
    }

    public String getStringForDisposition() {
        switch ( disposition ) {
            case FUTURE :
                return "Future" ;
            case ACTIVE :
                return "Active" ;
            case PAST :
                return "Past" ;
            default:
                return "???" ;
        }
    }

    public void toString( StringBuffer buf ) {
        String cn = getClass().getName() ;
        buf.append( cn.substring( cn.lastIndexOf('.') + 1) );
        buf.append(",start=").append( startTime/1000.0 ) ;
        buf.append(",end=").append( endTime/1000.0 ) ;
        buf.append(",status=").append( getStringForDisposition() ) ;
    }

     public long getEndTime() {
         return endTime;
     }

     public long getStartTime() {
         return startTime;
     }

     long startTime;
     long endTime ;
     protected int disposition = FUTURE ;

    public UID getId() {
        return id;
    }

    public void setId(UID id) {
        this.id = id;
    }
}
