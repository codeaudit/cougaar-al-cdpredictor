package org.cougaar.cpe.agents.messages;

import org.cougaar.cpe.model.Plan;
import org.cougaar.tools.techspecs.events.MessageEvent;

/**
 * Emitted by BDE units when a replan is performed.
 */
public class ZoneScheduleMessage extends MessageEvent {

    public ZoneScheduleMessage( Plan plan) {
        this.schedule = plan;
    }

    public Plan getSchedule() {
        return schedule;
    }

    Plan schedule ;
}
