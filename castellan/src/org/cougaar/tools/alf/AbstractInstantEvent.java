package org.cougaar.tools.alf;

public abstract class AbstractInstantEvent implements Schedulable {
    public AbstractInstantEvent(long time) {
        this.time = time;
    }

    public boolean isInstantaneous() {
        return true ;
    }

    public long getStartTime() {
        return time;
    }

    public long getEndTime() {
        return time ;
    }

    protected long time ;
}
