package org.cougaar.tools.techspecs.measurement;

import org.cougaar.tools.techspecs.ActionSpec;

public class ChainDelaySpec extends MeasurementSpec {


    public static final int ACTION_START = 0 ;
    public static final int ACTION_STOP = 1 ;

    /**
     * Measures the time delay between a timestamped action (identified with the original
     * role, action, and agent.) and the current timestamped action.
     *
     * @param name
     * @param parent
     * @param layer
     * @param type One of ACTION_START or ACTION_STOP
     * @param roleName  The originating role
     * @param actionName The type of action
     * @param agentId  The originating agent id (matching the role)
     */
    public ChainDelaySpec( String name, ActionSpec parent, int layer, int type,
                           String roleName, String actionName, String agentId ) {
        super(name, parent, MeasurementSpec.TIME_MEASUREMENT_CHAIN_DELAY, layer);
        this.roleName = roleName ;
        this.type = type ;
        this.actionName = actionName ;
        this.agentId = agentId ;
    }

    String roleName ;
    String actionName ;
    String agentId ;
}
