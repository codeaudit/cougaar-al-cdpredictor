package org.cougaar.cpe.model;

import java.io.Serializable;

public abstract class ZoneSchedule implements Serializable {

    public abstract Object clone() ;

    public abstract long getStartTime() ;

    public abstract long getEndTime() ;

    public abstract Zone getZoneForTime( long time ) ;
}
