package org.cougaar.tools.techspecs.qos;

public class TimePeriodMeasurement implements Record
{
    public TimePeriodMeasurement(long startTime, long endTime, Object value)
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
