package org.cougaar.tools.techspecs.qos;

/**
 * Similar to a delay measurement point, but useful for "tagging" individual elements.
 * This is the same as the DelayMeasurementPoint, just plotted differently (e.g. a single bar
 * per event.)
 */
public class EventDurationMeasurementPoint extends DelayMeasurementPoint
{
    public EventDurationMeasurementPoint(String name)
    {
        super(name);
    }
}
