package org.cougaar.cpe.model;

import org.w3c.dom.Document;

public interface TargetGenerator
{
    public void initialize( Document doc ) ;

    public void execute( WorldState ws ) ;
}
