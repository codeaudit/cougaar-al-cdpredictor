package org.cougaar.tools.techspecs.events;

import org.cougaar.tools.techspecs.ActionEventSpec;

/**
 * A timer event specification.
 */
public class TimerEvent extends ActionEvent
{
    private long time;
    private String alarmId;

    public TimerEvent( String alarmId, long time )
    {
        super(ActionEventSpec.EVENT_TIMER);
        this.time = time ;
        this.alarmId = alarmId ;
    }

    public String getAlarmId()
    {
        return alarmId;
    }

    public long getTime()
    {
        return time;
    }

}
