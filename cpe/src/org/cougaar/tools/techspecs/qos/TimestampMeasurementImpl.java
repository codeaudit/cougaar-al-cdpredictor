package org.cougaar.tools.techspecs.qos;

import org.cougaar.core.mts.MessageAddress;

/**
 * User: wpeng
 * Date: May 18, 2003
 * Time: 6:01:35 PM
 */
public class TimestampMeasurementImpl extends MeasurementImpl implements TimestampMeasurement {

    public TimestampMeasurementImpl(String eventName, String actionName, MessageAddress source, long timestamp) {
        super(eventName, actionName, source);
        this.timestamp = timestamp;
    }

    public void toString(StringBuffer buf) {
        super.toString(buf);
        if ( timestamp >= 0 ) {
            buf.append( ",time=" ).append( ( timestamp / 1000 ) ).append( " sec." ) ;
        }
        else {
            buf.append( ",time=UNKNOWN" ) ;
        }
    }

    public long getTimestamp() {
        return timestamp;
    }

    protected long timestamp ;
}
