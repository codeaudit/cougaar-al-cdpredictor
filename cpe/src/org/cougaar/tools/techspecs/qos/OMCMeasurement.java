package org.cougaar.tools.techspecs.qos;

import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.adaptivity.OMCRange;

public class OMCMeasurement extends MeasurementImpl {
    private Comparable value ;
    private long simTime;
    private long time;

    public OMCMeasurement(String eventName, String actionName, MessageAddress source, Comparable value,
                          long simTime, long time ) {
        super(eventName, actionName, source);
        this.value = value ;
        this.simTime = simTime ;
        this.time = time ;
    }

    public Comparable getValue() {
        return value;
    }

    public long getSimTime() {
        return simTime;
    }

    public long getTime() {
        return time;
    }

    public void toString( StringBuffer buf ) {
        super.toString();
        buf.append( ",time=").append( simTime ) ;
        buf.append( ",value=").append( value ) ;
    }

}
