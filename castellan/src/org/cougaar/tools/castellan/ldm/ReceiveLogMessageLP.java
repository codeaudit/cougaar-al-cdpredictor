
package org.cougaar.tools.castellan.ldm;

import org.cougaar.core.blackboard.*;
import org.cougaar.core.agent.*;
import org.cougaar.core.domain.*;
import org.cougaar.core.relay.*;
import org.cougaar.planning.ldm.plan.*;
import org.cougaar.planning.ldm.lps.*;
import org.cougaar.planning.ldm.PlanningFactory;
import org.cougaar.core.domain.RootPlan;
import org.cougaar.planning.ldm.LogPlan;
import org.cougaar.core.mts.*;

import java.util.*;

/**
 * Receive log message from plug-in.
 */
public class ReceiveLogMessageLP extends RelayLP implements MessageLogicProvider
{
    public ReceiveLogMessageLP(RootPlan rootPlan, MessageAddress self)
    {
        super(rootPlan,self);
    }


    public void execute(Directive m, Collection changeReports)
    {
        if ( m instanceof LogMessage ) {
            // Do not check for duplicates right now, since we don't really care.
            //logplan.findUniqueObject( ) ;
            LogMessage lm = ( LogMessage ) m ;
            lm.setLocal( false );
            rootPlan.add( m );
        }
    }
    protected RootPlan rootPlan;
}
