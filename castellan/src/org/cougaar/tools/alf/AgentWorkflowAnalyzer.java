package org.cougaar.tools.alf;

import java.util.HashMap;
import java.util.ArrayList;

/**
 * Takes a stream of events and find agent relationships.  Attempt to find the number of
 * tasks that propagate based on historical data and the type of relationships.
 */
public class AgentWorkflowAnalyzer
{

    public static class AgentNode {

        // Agent name

        // Observed ExecutionCycle/Plan schedule objects for the meta plan.  These
        // Have already been observed.

        // Projected ExecutionCycle objects for the meta plan.

        // Relationships with other agents.

        // Am I a source?
    }

    /**
     * One or more execution cycles.  (Can be aggregated togather.)
     */
    public static class ExecutionCycle {
        // What tasks are we characterizing? (A list if necessary.)
        // What tasks will be processed?
        // Output to be generated.

        // ``Rate" at which processing takes place.

        // Estimated batch size (if batched)

        // Output for this execution cycle.

        // Start time (estimated/measured)
        // Stop time  (estimated/measured)

        // Bins measuring jips consumption for this execution cycle.
    }

    public static class Bin {

        /**
         * Execution cycles calculated to be available for this bin.
         */
        double jips ;
    }

    /**
     * Maps agents to AgentNode objects.
     */
    protected HashMap agentMap = new HashMap() ;

    /**
     * Last updated projection time.
     */
    protected long lastUpdate ;

    /**
     * Bin granularity in milliseconds of planning time. Default is
     * five seconds and leads to 72000 bins for a horizon of one hour.
     */
    protected long binSize = 5000L ;
}
