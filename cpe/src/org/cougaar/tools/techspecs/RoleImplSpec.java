package org.cougaar.tools.techspecs;

import java.util.HashMap;

/**
 * This is the role implementation specification and ties a generic RoleSpec with a specific programmatic implementation
 * within a Cougaar plugin. RoleSpec instances describe generic interactions, whereas the RoleImplSpec
 * describe the performance and resource consumption of specific software implementations.
 *
 * <p. In the future, it is possible that the role implementation has different performance characteristics (and
 * hence a different ActionModelFunction for each ActionSpec.)
 */
public class RoleImplSpec extends TechSpec
{
    public RoleImplSpec(String name, PluginSpec parent, RoleSpec spec )
    {
        super(name, parent, TechSpec.TYPE_ROLE, spec.getLayer());
    }

    public RoleSpec getSpec()
    {
        return spec;
    }

    /**
     * Allows "overriding" the action model function for different action names.
     * @param actionName
     * @return An overriding action model function that describes this particular implementation of the role.
     */
    public ActionModelFunction getModelForAction( String actionName ) {
        return (ActionModelFunction) actionToModelFunctionMap.get( actionName ) ;
    }

    protected RoleSpec spec ;

    /**
     * A map for ActionSpecs names to role implementation specific actionModelFunction specs.
     */
    protected HashMap actionToModelFunctionMap ;
}
