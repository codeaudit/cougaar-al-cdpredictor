package org.cougaar.cpe.relay;

import org.cougaar.tools.techspecs.events.TimerEvent;

import java.io.Serializable;

/**
 * User: wpeng
 * Date: Mar 23, 2004
 * Time: 5:07:36 PM
 */
public class TimerMessage extends TimerEvent
{
    private QueuedAlarm queuedAlarm;

    protected TimerMessage(String alarmId, long time, QueuedAlarm queuedAlarm)
    {
        super(alarmId, time);
        this.queuedAlarm = queuedAlarm;
    }

    public QueuedAlarm getQueuedAlarm()
    {
        return queuedAlarm;
    }
}
