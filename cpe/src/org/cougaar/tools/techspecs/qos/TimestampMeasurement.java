package org.cougaar.tools.techspecs.qos;

/**
 * A timestamp is associated with an event that occurs at a particular time.
 */
public interface TimestampMeasurement extends Measurement {

    public static final long UNKNOWN_TIMESTAMP = -1 ;

    public long getTimestamp() ;

}
