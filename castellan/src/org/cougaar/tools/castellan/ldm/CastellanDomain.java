package org.cougaar.tools.castellan.ldm;

import org.cougaar.core.domain.*;
import org.cougaar.core.blackboard.*;
import org.cougaar.core.agent.*;

import java.util.*;

public class CastellanDomain implements Domain
{
    public Factory getFactory(LDMServesPlugin ldm)
    {
        return new CastellanFactory( ldm ) ;
    }

    public void initialize()
    {
    }

    public Collection createLogicProviders(BlackboardServesLogicProvider logplan,
                                           ClusterServesLogicProvider cluster)
    {
        ArrayList list = new ArrayList( 10 ) ;
        //list.add( new LogMessageSenderLP( ( LogPlanServesLogicProvider ) logplan, cluster ) ) ;
        //list.add( new ReceiveLogMessageLP( ( LogPlanServesLogicProvider ) logplan, cluster ) ) ;
        list.add( new PlanEventLogLP( ( LogPlanServesLogicProvider ) logplan, cluster ) ) ;
        return list ;
    }

    /**
     * Just use another existing blackboard for now.
     */
    public XPlanServesBlackboard createXPlan(Collection existingXPlans)
    {
        for (Iterator plans = existingXPlans.iterator(); plans.hasNext(); ) {
          XPlanServesBlackboard xPlan = (XPlanServesBlackboard) plans.next();
          if (xPlan != null) return xPlan;
        }

        return new LogPlan();
    }
}
