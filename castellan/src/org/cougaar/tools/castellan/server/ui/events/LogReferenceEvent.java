package org.cougaar.tools.castellan.server.ui.events;

import org.cougaar.tools.castellan.analysis.Loggable;
import org.cougaar.tools.castellan.analysis.UniqueObjectLog;

import java.awt.*;
import java.util.EventObject;

/**
 *  Event representing following a link/reference to a particular type of log.
 */
public class LogReferenceEvent extends EventObject
{
    public LogReferenceEvent ( Object source, UniqueObjectLog l )
    {
        super( source );
        this.l = l;
    }

    public UniqueObjectLog getLog ()
    {
        return l;
    }

    UniqueObjectLog l ;
}
