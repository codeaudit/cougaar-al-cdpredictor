package org.cougaar.tools.techspecs;

import java.io.Serializable;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * This is the specification of a generic Role that is unattached to any Plugin implementation.  It is analogous to the
 * Role in org.cougaar.planning.ldm.plan.Role except that it specifies the functionality of the Role itself in terms
 * of a state machine formalization.
 *
 * <p> There is a differentiation between the role specification and the role implementation specification.  The RoleSpec
 * is implementation neutral.  It describes states, inputs and outputs and does not have any performance knowledge (though
 * it may have notation for describing invariants and performance requirements.
 */
public class RoleSpec implements Serializable
{

    public RoleSpec( String roleClassName, int layer )
    {
        this.roleClassName = roleClassName ;
        this.layer = layer ;
    }


    public String getProtocolName()
    {
        return protocolName;
    }

    public String getRoleClassName()
    {
        return roleClassName;
    }

    public int getLayer()
    {
        return layer;
    }

    int layer ;

    /**
     * This is the TechSpec specific role name.
     */
    String roleClassName ;

    String protocolName ;

    /**
     * The number of states in this role.
     */
    ArrayList roleStateSpecs = new ArrayList() ;

    ActionTimerSpec spec ;
}
