package org.cougaar.cpe.agents.messages;

import org.cougaar.cpe.model.Plan;
import org.cougaar.tools.techspecs.events.MessageEvent;

import java.util.HashMap;

/**
 * User: wpeng
 * Date: May 21, 2003
 * Time: 10:39:52 AM
 */
public class SupplyPlanMessage extends MessageEvent {
    /**
     * A map of vehicle id to supply plans.
     * @param plans
     */
    public SupplyPlanMessage(HashMap plans) {
        this.plans = plans;
    }

    public HashMap getPlans() {
        return plans;
    }

    private HashMap plans ;
}
