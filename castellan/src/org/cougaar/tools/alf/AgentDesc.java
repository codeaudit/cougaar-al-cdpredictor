package org.cougaar.tools.alf;

import org.cougaar.tools.castellan.pdu.UIDPDU;

import java.util.ArrayList;

public class AgentDesc {

    public AgentDesc(String agentName) {
        this.agentName = agentName;
    }

    public String getAgentName() {
        return agentName;
    }
    /**
     * Returns the last element observed.
     */
    //public long getLastObservedTime() ;

    //public long getMaxProjectedTime() ;

    public void addObservedNodeStart( String nodeName, long startTime ) {
        observed.addNodeRecord( nodeName, startTime );
    }


    /**
     * Find first observed task parent.
     */
    // public UIDPDU getObservedParent( UIDPDU task ) ;

    /**
     * Just return the last observed node.
     */
    public String getCurrentNode( long time ) {
        return null ;
    }

    /**
     * Maps agent to observed nodes.
     */
    AgentNodeSchedule observed = new AgentNodeSchedule();

    /**
     * Predicts agents on nodes.
     */
    AgentNodeSchedule predicted = new AgentNodeSchedule() ;

    protected long id = -1 ;

//  protected AgentExecutionSchedule observed;
//  predicted AgentSlotSchedule predicted;

    protected String agentName ;

    /**
     * A list of suppliers.
     */
    protected ArrayList suppliers = new ArrayList() ;

    /**
     * A list of subordinaates.
     */
}
