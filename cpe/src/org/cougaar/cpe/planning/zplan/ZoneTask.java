package org.cougaar.cpe.planning.zplan;

import org.cougaar.cpe.model.*;

public class ZoneTask extends Task
{
    public ZoneTask(long startTime, long endTime, Zone startZone, Zone endZone )
    {
        super(startTime, endTime);
        this.startZone = startZone;
        this.endZone = endZone;
    }

    public void toString(StringBuffer buf)
    {
        super.toString(buf);
        buf.append( ",startZone=" ).append( startZone ) ;
        buf.append( ",endZone=" ).append( endZone ) ;
    }

    public Object clone()
    {
        ZoneTask newZoneTask = new ZoneTask( getStartTime(), getEndTime(), getStartZone(), getEndZone() ) ;
        newZoneTask.disposition = disposition ;
        if ( result != null ) {
            newZoneTask.result = (ZoneExecutionResult) result.clone() ;
        }
        return newZoneTask ;
    }

    public Zone getStartZone() {
        return startZone;
    }

    public Zone getEndZone() {
        return endZone;
    }

    public TaskResult getObservedResult()
    {
        return result ;
    }

    Zone startZone ;
    Zone endZone ;
    ZoneExecutionResult result ;

    public void setObservedResult(ZoneExecutionResult zoneExecutionResult) {
        result = zoneExecutionResult ;
    }
}
