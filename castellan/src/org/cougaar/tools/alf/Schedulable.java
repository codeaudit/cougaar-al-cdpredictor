package org.cougaar.tools.alf;

/**
 * Interface for events take take a period of time.
 */
public interface Schedulable {

    public boolean isInstantaneous() ;

    public long getStartTime() ;

    public long getEndTime() ;
}

