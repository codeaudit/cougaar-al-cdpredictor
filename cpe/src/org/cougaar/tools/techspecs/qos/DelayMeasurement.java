package org.cougaar.tools.techspecs.qos;

import org.cougaar.core.mts.MessageAddress;

/**
 * User: wpeng
 * Date: May 30, 2003
 * Time: 12:02:48 PM
 */
public class DelayMeasurement extends TimestampMeasurementImpl {
    public DelayMeasurement(String action, String name, MessageAddress source, long timestamp, long localTime ) {
        super(action, name, source, timestamp);
        this.localTime = localTime ;
        if ( localTime < timestamp ) {
            throw new IllegalArgumentException(
                    "Local time precedes timestamp. Negative delays are not allowed." ) ;
        }
    }

    public void toString(StringBuffer buf) {
        super.toString(buf);
        buf.append( ",localTime=").append( localTime/1000 ).append( " secs." ) ;
    }

    public long getDelay() {
        return localTime - timestamp ;
    }

    /**
     * The local time at which an event was measured.
     * @return
     */
    public long getLocalTime() {
        return localTime;
    }

    long localTime ;
}
