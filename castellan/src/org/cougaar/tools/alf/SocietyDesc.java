package org.cougaar.tools.alf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * A description of a society.  Encapsulates all predicted and observed results,
 * as well as the baseline.
 */
public class SocietyDesc {

    public SocietyDesc() {
    }

    public SocietyDesc(String societyName) {
        this.societyName = societyName;
    }

    public String getSocietyName() {
        return societyName;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer() ;

        buf.append( "< Society " ) ;
        buf.append( societyName ).append( " #nodes=" ).append( getNumNodes() ).append( " [" ) ;
        for ( Iterator iter= getNodes();iter.hasNext(); ) {
            NodeDesc node = ( NodeDesc ) iter.next() ;
            buf.append( node.getNodeName() ) ;
            if ( iter.hasNext() ) {
                buf.append( ',') ;
            }
        }

        buf.append( "], #Agents= " ).append( getNumAgents() ).append( " [" ) ;
        for ( Iterator iter= getAgents();iter.hasNext(); ) {
            AgentDesc agent = ( AgentDesc ) iter.next() ;
            buf.append( agent.getAgentName() ) ;
            if ( iter.hasNext() ) {
                buf.append( ',') ;
            }
        }
        buf.append( "] >" ) ;

        return buf.toString() ;
    }

    /**
     * Add a new node.
     * @throws RuntimeException if the node already exists.
     */
    public NodeDesc addNode( String name ) {
        if ( nodeMap.get( name ) != null ) {
            throw new IllegalArgumentException( "Node " + name + " already exists." ) ;
        }

        NodeDesc nd = new NodeDesc( name ) ;
        nodeMap.put( name, nd ) ;
        return nd ;
    }

    public Iterator getAgents() {
        return agentMap.values().iterator() ;
    }

    public int getNumAgents() {
        return agentMap.size() ;
    }

    public AgentDesc addAgent( String agentName ) {
        if ( agentMap.get( agentName ) != null ) {
            throw new IllegalArgumentException( "Agent " + agentName + " already exists." ) ;
        }

        AgentDesc ad = new AgentDesc( agentName ) ;
        agentMap.put( agentName, ad ) ;
        return ad ;
    }

    public AgentDesc getAgent( String agentName ) {
        return ( AgentDesc ) agentMap.get( agentName ) ;
    }

    // public long getSocietyTime() ;

    /**
     * Get a Node by name.
     */
    public NodeDesc getNode( String name ) {
        return ( NodeDesc ) nodeMap.get( name ) ;
    }

    public int getNumNodes() {
        return nodeMap.size() ;
    }

    public Iterator getNodes() {
        return nodeMap.values().iterator() ;
    }

    protected String societyName ;

    protected HashMap agentMap = new HashMap() ;

    protected HashMap nodeMap  = new HashMap() ;
}
