package org.cougaar.tools.techspecs.qos;

import java.io.Serializable;

public class TimePeriodMeasurement implements Record
{
    public TimePeriodMeasurement(long startTime, long endTime, Serializable value)
    {
        this.startTime = startTime;
        this.endTime = endTime;
        this.value = value;
    }

    public long getEndTime()
    {
        return endTime;
    }

    public long getStartTime()
    {
        return startTime;
    }

    public Object getValue()
    {
        return value;
    }

    long startTime, endTime ;
    Object value ;
}
