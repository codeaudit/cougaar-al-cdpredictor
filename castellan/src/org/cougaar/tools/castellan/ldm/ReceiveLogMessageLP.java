
package org.cougaar.tools.castellan.ldm;

import org.cougaar.core.blackboard.*;
import org.cougaar.core.agent.*;
import org.cougaar.planning.ldm.plan.*;

import java.util.*;

/**
 * Receive log message from plug-in.
 */
public class ReceiveLogMessageLP extends LogPlanLogicProvider implements MessageLogicProvider
{
    public ReceiveLogMessageLP(LogPlanServesLogicProvider logplan, ClusterServesLogicProvider cluster)
    {
        super(logplan, cluster);
    }

    public void execute(Directive m, Collection changeReports)
    {
        if ( m instanceof LogMessage ) {
            // Do not check for duplicates right now, since we don't really care.
            //logplan.findUniqueObject( ) ;
            LogMessage lm = ( LogMessage ) m ;
            lm.setLocal( false );
            logplan.add( m );
        }
    }
}
