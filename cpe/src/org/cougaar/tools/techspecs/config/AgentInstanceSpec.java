package org.cougaar.tools.techspecs.config;

import org.cougaar.tools.techspecs.PluginSpec;
import org.cougaar.tools.techspecs.TechSpec;

/**
 * A specific agent implementation obtained by creating a role implementation (i.e. a plugin.).  Currently,
 * we are assuming one plug in per agent.  This may change in the future.
 */
public class AgentInstanceSpec extends TechSpec
{
    /**
     * The plugin running in this agent.
     */
    private PluginSpec plugInSpec;

    public AgentInstanceSpec(String name, NodeSpec parent, PluginSpec rs )
    {
        super(name, parent, TechSpec.TYPE_AGENT, TechSpec.LAYER_APPLICATION ) ;
        plugInSpec = rs ;
    }

    public AgentInstanceSpec(String name, PluginSpec plugInSpec)
    {
        this( name, null, plugInSpec );
    }

    public String getAgentName() { return getName() ; }

    public void setParent( NodeSpec parent ) {
        this.parent = parent ;
    }

    public NodeSpec getNode() {
        return (NodeSpec) parent ;
    }

    public PluginSpec getPlugInSpec()
    {
        return plugInSpec;
    }
}
