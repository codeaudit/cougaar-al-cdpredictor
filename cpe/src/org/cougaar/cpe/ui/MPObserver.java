package org.cougaar.cpe.ui;

import org.cougaar.tools.techspecs.qos.MeasurementPoint;

public interface MPObserver
{

    public void updateData() ;

    public MeasurementPoint getMeasurementPoint() ;
}
