package org.cougaar.cpe.model.events;

import org.cougaar.cpe.model.events.CPEEvent;

public class TimeAdvanceEvent extends CPEEvent {
    public TimeAdvanceEvent(long oldTime, long newTime)
    {
        super ( newTime ) ;
        this.oldTime = oldTime;
    }

    public long getOldTime()
    {
        return oldTime;
    }

    public long getNewTime()
    {
        return getTime();
    }

    private long oldTime ;
}
