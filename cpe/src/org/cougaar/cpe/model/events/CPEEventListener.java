package org.cougaar.cpe.model.events;

import org.cougaar.cpe.model.events.CPEEvent;

/**
 * User: wpeng
 * Date: May 10, 2004
 * Time: 6:05:59 PM
 */
public interface CPEEventListener
{
    public void notify( CPEEvent e ) ;
}
