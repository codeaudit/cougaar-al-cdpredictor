package org.cougaar.tools.techspecs.results;



/**
 * A measurement point that takes ActionResult entities of a certain type.
 */
public class ActionMeasurementPoint
{
    public ActionMeasurementPoint(String name)
    {
        this.name = name ;
    }

    // Observed elapsed time measurement statistics
    float averageElapsedTime ;

    protected String name ;
}
