package org.cougaar.tools.techspecs.events;

import org.cougaar.tools.techspecs.ActionEventSpec;

/**
 * User: wpeng
 * Date: Mar 11, 2004
 * Time: 6:35:10 PM
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
