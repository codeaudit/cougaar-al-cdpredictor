package org.cougaar.cpe.model;

import org.w3c.dom.Document;

public interface TargetGenerator
{
    public void initialize( Document doc ) ;

    public void execute( WorldState ws ) ;

    /**
     * Reset the time base.  For example, if we generate a number of targets into the "future"
     * (e.g. in order to generate a number of initial targets) and then reset the time to zero
     * prior to the beginning of execution, we should reinitialize any internal time depedent
     * variables.
     *
     * @param time
     */
    public void resetTime( long time ) ;
}
