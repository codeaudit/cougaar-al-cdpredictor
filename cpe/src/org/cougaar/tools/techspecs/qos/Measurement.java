package org.cougaar.tools.techspecs.qos;

import org.cougaar.core.util.UID;
import org.cougaar.core.mts.MessageAddress;

import java.io.Serializable;

/**
 * A measurement is a single observed event or QoS measurement.
 */
public interface Measurement extends Serializable {

    /**
     * The triggering input, timer event, or action.  Actions
     * may trigger other actions.
     *
     * @return
     */
    public abstract String getEvent() ;

    /**
     * The action type with which this measurement is associated,
     * e.g. the action taken as a result of the triggering event.
     *
     * @return
     */
    public abstract String getAction() ;

    /**
     * The UID of the measurement point associated with this id.
     * @return
     */
    public abstract UID getMeasurementPoint() ;

    /**
     * Identifier for where this measurement was attached, e.g.
     * an agent id address.
     * @return
     */
    public MessageAddress getSource() ;

    /**
     * A units expression associated with this QoS,
     * Recognized types are
     *
     * <pre>
     * <Expr>= <
     *   Time |
     *   TimeInterval |
     *   Average( <Expr> ) |
     *   Max( <Expr> ) |
     *   Min( <Expr> )
     * </pre>
     *
     * @return
     */
    public String getUnitType() ;
}
