package org.cougaar.tools.techspecs.config;

import java.io.Serializable;

/**
 * A simple model of a netork link between two nodes.
 */
public class NetworkLinkSpec implements Serializable
{
    private float txRate;

    /**
     *
     *
     * @param node1
     * @param node2
     * @param txRate
     */
    public NetworkLinkSpec(NodeSpec node1, NodeSpec node2, float txRate )
    {
        this.node1 = node1;
        this.node2 = node2;
        this.txRate = txRate ;
    }

    public float getTxRate()
    {
        return txRate;
    }

    NodeSpec node1, node2 ;
}
