package org.cougaar.tools.alf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.net.InetAddress;

/**
 * Describes nodes.
 */
public class NodeDesc {

    /**
     * Return the name for the node.
     */
    public NodeDesc(String nodeName) {
        this.nodeName = nodeName;
    }

    public NodeDesc(String nodeName, InetAddress address, double jps ) {
        this.nodeName = nodeName;
        this.address = address;
        this.normalizedJPS = jps ;
    }

    public String getNodeName() {
        return nodeName;
    }

    public double getNormalizedJPS() {
        return normalizedJPS;
    }

    /**
     * Get all agents on this node.
     * @param time The time of observation.
     * @return A set of observed agents.  Right now, we assume that agents are not mobile.
     */
    public Iterator getObservedAgents( long time ) {
        return agentMap.values().iterator() ;
    }

    /**
     * Get agents predicted on this node.
     * @param time The time at which the predictions will take place.
     * @return A set of observed agents.  Because agents are not mobile, observed agents and
     * predicted agents are the same.
     */
    public Iterator getPredictedAgents( long time ) {
        return agentMap.values().iterator() ;
    }

    /**
     * Agents are not assumed to be mobile.
     */
    public void addAgent( String agentName ) {
        if ( agentMap.get( agentName ) == null ) {
            agentMap.put( agentName, agentName ) ;
        }
    }

    /**
     * Normalized CPU speed.  Must use benchmark.
     */
    protected InetAddress address ;
    protected double normalizedJPS = 1.0 ;
    protected String nodeName ;
    protected HashMap agentMap = new HashMap() ;

}
