package org.cougaar.tools.techspecs;

import org.cougaar.core.adaptivity.OperatingMode;
import org.cougaar.tools.techspecs.events.ActionEvent;

import java.util.ArrayList;

/**
 *  A function which models/specifies state transitions.  It is used by both <code>ActionSpec</code> and
 *  <code>RoleImplSpec</code> instances.  In the latter case, each action is mapped to a ActionModelFunction through
 *  a lookup table.
 *
 */
public abstract class ActionModelFunction
{

    public ActionModelFunction(ActionSpec parentSpec)
    {
        this.parentSpec = parentSpec;
    }

    public ActionSpec getParentSpec()
    {
        return parentSpec;
    }

    /**
     * @param input
     * @return Whether is input is accepted by this action.
     */
    public abstract boolean accept( ActionEvent input ) ;

    /**
     * This method maps a role, a set of op modes and a triggering input to a set of results.
     *
     * @param s
     * @param opModes
     * @param input
     * @param result
     * @return Whether or not this input was accepted under the op modes and states.
     */
    public abstract boolean process( RoleStateSpec s, OperatingMode[] opModes, ActionEvent input, ArrayList result ) ;

    public int getNumOpModeDependencies( ) {
        return opModeDependencies.size() ;
    }

    public String getOpModeDependencyName( int i ) {
        return (String) opModeDependencies.get(i) ;
    }

    protected ArrayList opModeDependencies = new ArrayList();
    protected ActionSpec parentSpec ;

}
