package org.cougaar.tools.techspecs.config;

import org.cougaar.tools.techspecs.TechSpec;

import java.util.ArrayList;

/**
 *   This is a configuration specific node spec.
 */
public class NodeSpec extends TechSpec
{
    public NodeSpec(String name, String hostName, String ipAddress, TechSpec parent )
    {
        super(name, parent, TechSpec.TYPE_HOST, TechSpec.LAYER_EXECUTION );
    }

    /**
     * A broad rating of the speed of the actual node.  Finer ratigns could be developed later.
     */
    protected float instructionUnitsPerSecond ;

    /**
     * The network host name of the node.
     */
    protected String hostName ;

    protected String ipAddress ;

    protected ArrayList agentInstanceSpecs = new ArrayList() ;
}
