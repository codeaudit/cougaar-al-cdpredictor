package org.cougaar.tools.techspecs;

import org.cougaar.tools.techspecs.measurement.MeasurementSpec;

import java.util.ArrayList;

/**
 *  An action is associated with a transition from a role state to another role state. It has a default action model function
 * which minimally indicates resource consumption as a function of operating mode.
 *
 * <p> For now, we assume that all actions emit the same type of message/timer events independent of operating mode and/or input.
 */
public class ActionSpec extends TechSpec
{
    /**
     *
     * @param name
     * @param parent
     * @param layer
     * @param start
     * @param next
     * @param emittedActions A set of emitted actions for this action which are independent of opModes.
     * @param amf
     */
    public ActionSpec( String name, RoleImplSpec parent, int layer, RoleStateSpec start,
                       RoleStateSpec next, ArrayList emittedActions, ActionModelFunction amf  )
    {
        super(name, parent, TechSpec.TYPE_ACTION, layer );
        this.startState = start ;
        this.nextState = next ;
        this.emittedActionEventSpecs = (ArrayList) emittedActions.clone() ;
        this.amf = amf ;
    }

    public ActionModelFunction getActionModelFunction() {
        return amf ;
    }

    protected RoleStateSpec startState ;
    protected RoleStateSpec nextState ;

    /**
     * A list of emitted actions.
     */
    protected ArrayList emittedActionEventSpecs ;

    /**
     * Measurements triggered by this action on entry.
     */
    protected MeasurementSpec[] measurementsOnEntry ;

    /**
     * Measurements triggered by the action on exit.
     */
    protected MeasurementSpec[] measurementsOnExit ;

    /**
     * These are primary synthetic measurements inserted by action-specific code.
     */
    protected MeasurementSpec[] measurementsDuringAction ;

    protected ActionModelFunction amf ;
}
