package org.cougaar.cpe.util;

import org.cougaar.core.agent.service.alarm.Alarm;

/**
 * User: wpeng
 * Date: May 28, 2003
 * Time: 11:19:49 PM
 */
public abstract class StandardAlarm implements Alarm{
    private boolean expired;

    public StandardAlarm( long expirationTime ) {
        this.expirationTime = expirationTime ;
    }

    public boolean cancel() {
        return false;
    }

    public void reset(long currentTime) {
        expired = false ;
        expirationTime = currentTime + period ;
    }

    public void expire() {
        expired = true ;
        processExpire() ;
    }

    protected abstract void processExpire() ;


    public long getExpirationTime() {
        return expirationTime ;
    }

    public boolean hasExpired() {
        return expired;
    }

    public long getPeriod() {
        return period;
    }

    public void setPeriod(long period) {
        this.period = period;
    }

    protected long expirationTime ;
    protected long period ;

}
