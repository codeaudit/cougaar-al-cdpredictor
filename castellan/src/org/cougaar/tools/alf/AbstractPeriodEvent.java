package org.cougaar.tools.alf;

public abstract class AbstractPeriodEvent implements Schedulable {
    protected AbstractPeriodEvent(long start, long end) {
        this.start = start;
        this.end = end;
    }

    public boolean isInstantaneous() {
        return false;
    }

    public long getStartTime() {
        return 0;
    }

    public long getEndTime() {
        return 0;
    }

    long start, end ;
}
