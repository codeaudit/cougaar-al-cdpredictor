package org.cougaar.tools.techspecs.results;

/**
 * User: wpeng
 * Date: Apr 1, 2004
 * Time: 12:38:42 PM
 */
public class CPUTimeActionResult extends ActionResult
{
    public CPUTimeActionResult( int observedTime )
    {
        super( ActionResult.RAW_CPU_TIME_ELAPSED );
        this.observedTime = observedTime ;
    }

    public int getObservedTime()
    {
        return observedTime;
    }

    int observedTime ;
}
