package org.cougaar.tools.techspecs.qos;

import org.cougaar.core.mts.MessageAddress;

/**
 * User: wpeng
 * Date: May 30, 2003
 * Time: 12:02:48 PM
 */
public class DelayMeasurement extends TimestampMeasurementImpl {

    /**
     *
     * @param actionName
     * @param eventName The event name
     * @param source
     * @param timestamp
     * @param localTime
     */
    public DelayMeasurement(String eventName, String actionName, MessageAddress source, long timestamp, long localTime ) {
        super(eventName, actionName, source, timestamp);
        this.localTime = localTime ;
//        if ( localTime < timestamp ) {
//            throw new IllegalArgumentException(
//                    "Local time precedes timestamp. Negative delays are not allowed." ) ;
//        }
    }

    public DelayMeasurement(String eventName, String actionName, MessageAddress source, long timestamp, long localTime,
                            long simulationTime ) {
        super(eventName, actionName, source, timestamp);
        this.localTime = localTime ;
        this.simulationTime = simulationTime ;
//        if ( localTime < timestamp ) {
//            throw new IllegalArgumentException(
//                    "Local time precedes timestamp. Negative delays are not allowed." ) ;
//        }
    }

    public void toString(StringBuffer buf) {
        super.toString(buf);
        buf.append( ",localTime=").append( localTime/1000 ).append( " secs." ) ;
    }

    public long getDelay() {
        return localTime - timestamp ;
    }

    public long getSimulationTime() {
        return simulationTime;
    }

    /**
     * The local time at which an event was measured.
     * @return
     */
    public long getLocalTime() {
        return localTime;
    }

    long localTime ;
    long simulationTime = -1 ;
}
