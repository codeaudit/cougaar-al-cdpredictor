package org.cougaar.cpe.relay;

import org.cougaar.core.agent.service.alarm.Alarm;

import java.lang.reflect.Method;

public class QueuedAlarm implements Alarm
{

    public QueuedAlarm( GenericRelayMessageTransport gmrt, String alarmId, String callback, long delay ) {
        this.delay = delay;
        this.gmrt = gmrt ;
        this.alarmId = alarmId ;
        this.callback = callback ;
        this.expirationTime = delay + System.currentTimeMillis() ;
    }

    public QueuedAlarm( GenericRelayMessageTransport gmrt, String alarmId, String callback, long delay, boolean periodic)
    {
        this( gmrt, alarmId, callback, delay ) ;
        isPeriodic = periodic;
    }

    public Object clone()
    {
        QueuedAlarm result = new QueuedAlarm( gmrt, alarmId, callback, delay, isPeriodic ) ;
        result.expirationTime = expirationTime ;
        result.callbackMethod = callbackMethod ;
        result.stopped = stopped ;
        return result ;
    }

    public String toString()
    {
        return "[QueuedAlarm id=" + alarmId + ",callback=" + callback + ",delay=" + delay + ",isPeriodic=" + isPeriodic + "]" ;
    }

    public Method getCallbackMethod()
    {
        return callbackMethod;
    }

    public String getAlarmId()
    {
        return alarmId;
    }

    public boolean cancel() {
        return false;
    }

    public boolean isExpired()
    {
        return expired;
    }

    /**
     * Reset the (periodic) alarm starting with currentTime as the time base.
     * @param currentTime
     */
    public void reset(long currentTime) {
        expired = false ;
        expirationTime = currentTime + delay ;
    }

    public void expire() {
        gmrt.sendMessage( gmrt.getParentAgent(), new TimerMessage( this.getAlarmId(), System.currentTimeMillis(), this ));
        expired = true ;
    }

    public boolean hasExpired() {
        return expired;
    }

    public long getExpirationTime() {
        return expirationTime ;
    }

    public String getCallbackMethodName()
    {
        return callback;
    }

    public boolean isPeriodic()
    {
        return isPeriodic;
    }

    public void setCallbackMethod(Method m)
    {
        callbackMethod = m ;
    }

    protected void setStopped(boolean b)
    {
        stopped = true ;
    }

    protected boolean isStopped()
    {
        return stopped;
    }

    private String alarmId;
    private Method callbackMethod;
    private boolean stopped;
    protected GenericRelayMessageTransport gmrt ;
    protected long expirationTime ;
    protected long delay ;
    private boolean expired;
    protected String callback ;
    protected boolean isPeriodic = false ;


}
