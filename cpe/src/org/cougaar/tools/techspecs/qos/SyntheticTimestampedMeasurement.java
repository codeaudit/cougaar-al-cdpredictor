package org.cougaar.tools.techspecs.qos;

import org.cougaar.core.mts.MessageAddress;

public class SyntheticTimestampedMeasurement extends TimestampMeasurementImpl
{
    /**
     * A generic measurement that takes arbitary timestamped measurement values.
     *
     * @param eventName
     * @param actionName
     * @param source
     * @param timestamp
     * @param value
     */
    public SyntheticTimestampedMeasurement( String eventName, String actionName,
                                          MessageAddress source, long timestamp, Object value )
    {
        super(eventName, actionName, source, timestamp);
        this.value = value ;
    }

    public Object getValue()
    {
        return value;
    }

    Object value ;
}
