package org.cougaar.tools.techspecs.qos;

import org.cougaar.core.adaptivity.OMCRangeList;

/**
 * Allow synthetic typed, timestamped measurements to be made.
 */
public class TimePeriodMeasurementPoint extends MeasurementPoint
{
    private Class type;
    private OMCRangeList omcrl ;

    public TimePeriodMeasurementPoint(String name, Class type )
    {
        super(name);
        if ( type == null ) {
            throw new IllegalArgumentException( "Type must be non-null." ) ;
        }
        this.type = type ;
    }

    public TimePeriodMeasurementPoint(String name, Class type, OMCRangeList omcrl)
    {
        super(name);
        this.type = type;
        this.omcrl = omcrl;
    }

    public boolean isValidMeasurement(Measurement m)
    {
        if ( !( m instanceof TimePeriodMeasurement ) ) {
            return false ;
        }
        TimePeriodMeasurement sm = (TimePeriodMeasurement) m;
        return sm.getValue().getClass().equals( type ) ;
    }
}
