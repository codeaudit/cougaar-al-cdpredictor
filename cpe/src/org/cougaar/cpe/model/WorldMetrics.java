package org.cougaar.cpe.model;

import org.cougaar.tools.techspecs.qos.MeasurementPoint;
import org.cougaar.tools.techspecs.qos.TypedMeasurementPoint;

/**
 * Measure scoring rate
 */
public class WorldMetrics
{
    MeasurementPoint fuelShortFalls = new TypedMeasurementPoint( "Fuel Shortfalls", null  ) ;
    MeasurementPoint ammoShortFalls = new TypedMeasurementPoint( "Ammo Shortfalls", null ) ;
}
