package org.cougaar.tools.techspecs;

import org.cougaar.tools.techspecs.events.ActionEvent;

import java.util.ArrayList;

/**
 * A state in a particular role.  It encapsulates all actions emanating from this state.
 */
public class RoleStateSpec
{
    public RoleStateSpec(RoleSpec parentSpec, ArrayList actions)
    {
        this.parentSpec = parentSpec;
        this.actions = actions;
    }

    public int getNumActions() {
        return actions.size() ;
    }

    public ActionSpec getActionSpec( int i ) {
        return (ActionSpec) actions.get(i) ;
    }

    /**
     * Find the acceptable action spec for the current input.
     * @param input
     * @return
     */
    public ActionSpec getActionSpecForInput( ActionEvent input ) {
        for (int i = 0; i < actions.size(); i++) {
            ActionSpec actionSpec = (ActionSpec)actions.get(i);
            ActionModelFunction amf = actionSpec.getActionModelFunction() ;
            if ( amf != null && amf.accept( input ) ) {
                return actionSpec ;
            }
        }
        return null ;
    }

    protected RoleSpec parentSpec ;
    protected ArrayList actions = new ArrayList() ;
}
