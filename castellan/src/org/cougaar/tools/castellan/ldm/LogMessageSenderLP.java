package org.cougaar.tools.castellan.ldm;

import org.cougaar.core.agent.*;
import org.cougaar.core.blackboard.*;

import java.util.*;

/**
 * Looks for locally generated log messages and forwards them to the appropriate log cluster.
 */
public class LogMessageSenderLP extends LogPlanLogicProvider
        implements LogicProvider, EnvelopeLogicProvider
{
    public LogMessageSenderLP(LogPlanServesLogicProvider logplan, ClusterServesLogicProvider cluster)
    {
        super(logplan, cluster);
    }

    public void init()
    {
    }

    protected void examine( Object o, Collection changes ) {
        if (!(o instanceof LogMessage ) ) {
            return ;
        }

        LogMessage lm = ( LogMessage ) o ;
        // Non-local messages are not sent.
        if ( !lm.isLocal() ) {
            return ;
        }

        lm.setSent( true );
        // Note, the lm already has a destination set by the BlackboardClientMTImpl
        //
        logplan.sendDirective( lm, changes );
        // Remove the log message after it is sent?
        logplan.remove( lm );
    }

    /**
     * Propagate adds and changes of LogMessages.
     */
    public void execute(EnvelopeTuple o, Collection changes)
    {
        Object obj = o.getObject();
        if (o.isAdd() || o.isChange() ) {
          examine(obj, changes);
        } else if ( ( o.isAdd() || o.isChange() )  && o.isBulk()) {
          Collection c = (Collection) obj;
          for (Iterator e = c.iterator(); e.hasNext(); ) {
            examine(e.next(), changes);
          }
        }
    }
}
