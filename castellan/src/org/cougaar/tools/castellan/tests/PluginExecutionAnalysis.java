/*
* $Header: /opt/rep/cougaar.cvs/al-cdpredictor/castellan/src/org/cougaar/tools/castellan/tests/PluginExecutionAnalysis.java,v 1.1 2002-06-11 12:22:06 cvspsu Exp $
*
* $Copyright$
*
* This file contains proprietary information of Intelligent Automation, Inc.
* You shall use it only in accordance with the terms of the license you
* entered into with Intelligent Automation, Inc.
*/

/*
* $Log: PluginExecutionAnalysis.java,v $
* Revision 1.1  2002-06-11 12:22:06  cvspsu
* *** empty log message ***
*
*
*/
package org.cougaar.tools.castellan.tests;

import org.cougaar.core.plugin.ComponentPlugin;
import org.cougaar.core.service.BlackboardService;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.util.UnaryPredicate;
import org.cougaar.tools.castellan.planlog.InMemoryEventLog;

import java.util.Enumeration;

public class PluginExecutionAnalysis extends ComponentPlugin
{
    public void execute() {
        Enumeration c = subscription.getAddedList() ;
        if ( log == null ) {
            while ( c.hasMoreElements() ) {
                Object o = c.nextElement() ;
                if ( o instanceof InMemoryEventLog ) {
                    log = ( InMemoryEventLog ) o ;
                }
            }
        }
    }

    protected void setupSubscriptions()
    {
        BlackboardService bs = getBlackboardService() ;
        subscription = ( IncrementalSubscription) bs.subscribe( new UnaryPredicate() {
            public boolean execute(Object o)
            {
                if ( o instanceof InMemoryEventLog ) {
                    return true ;
                }
                return false ;
            }
        } ) ;
    }

    IncrementalSubscription subscription ;
    InMemoryEventLog log ;
}
