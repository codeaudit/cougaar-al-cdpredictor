package org.cougaar.tools.techspecs.qos;

import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.adaptivity.OMCRange;

public class OMCMeasurement extends MeasurementImpl {
    private OMCRange range ;

    public OMCMeasurement(String eventName, String actionName, MessageAddress source, OMCRange range ) {
        super(eventName, actionName, source);
        this.range = range ;
    }

    public OMCRange getRange() {
        return range;
    }

    public void toString( StringBuffer buf ) {
        super.toString();
        buf.append( ",range=").append( range ) ;
    }
}
