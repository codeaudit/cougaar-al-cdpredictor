package org.cougaar.tools.techspecs.qos;

import org.cougaar.tools.techspecs.measurement.MeasurementSpec;

/**
 * An instantiation of a measurement point associated with a MeasurementSpec.  This is obsolete
 * and simply a place holder for a revised MeasurementPoint base class that will incorporate a
 * <code>MeasurementSpec</code>.
 */
public class TypedMeasurementPoint extends MeasurementPoint
{
    private MeasurementSpec mspec;

    public TypedMeasurementPoint(String name, MeasurementSpec mspec )
    {
        super(name);
        this.mspec = mspec ;
    }

    public MeasurementSpec getMeasurementSpec()
    {
        return mspec;
    }

    public boolean isValidMeasurement(Measurement m)
    {
        return false ;
    }
}
