package org.cougaar.tools.techspecs.qos;

/**
 *  A measurement point for timestamps.  Useful for tracking the arrival of
 *  individual events.
 */
public class TimestampMeasurementPoint extends MeasurementPoint {

    public TimestampMeasurementPoint(String id) {
        super(id);
    }

    public boolean isValidMeasurement(Measurement m) {
        if ( !( m instanceof TimestampMeasurement) ) {
            return false ;
        }
        return super.isValidMeasurement(m);
    }

//    /**
//     * The length (in ms) for which measurements will be retained.
//     * Default is Long.MAX_VALUE.
//     * @return
//     */
//    public abstract long getMaximumHistoryPeriod() ;
//
//    /**
//     *
//     * @param value The length in time for which measurements will be retained.
//     */
//    public abstract void setMaximumHistoryPeriod( long value ) ;

    //public abstract long getInstantaneousDelayMeasurement() ;

    //public abstract long getMaximumDelayMeasurement() ;
}
