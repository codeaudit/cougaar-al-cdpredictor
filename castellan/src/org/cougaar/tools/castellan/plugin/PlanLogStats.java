package org.cougaar.tools.castellan.plugin;

/**
 * Collect some statistics on plan logging for this agent.
 */
public class PlanLogStats implements java.io.Serializable {

    public long getFirstEventTime () {
        return firstEventTime;
    }

    public void setFirstEventTime ( long firstEventTime ) {
        this.firstEventTime = firstEventTime;
    }

    public long getLastEventTime () {
        return lastEventTime;
    }

    public void setLastEventTime ( long lastEventTime ) {
        this.lastEventTime = lastEventTime;
    }

    public int getNumBadTimestamps () {
        return numBadTimestamps;
    }

    public void setNumBadTimestamps ( int numBadTimestamps ) {
        this.numBadTimestamps = numBadTimestamps;
    }

    public long getNumBytesSent () {
        return numBytesSent;
    }

    public void setNumBytesSent ( long numBytesSent ) {
        this.numBytesSent = numBytesSent;
    }

    public long getNumBytesReceived () {
        return numBytesReceived;
    }

    public void setNumBytesReceived ( long numBytesReceived ) {
        this.numBytesReceived = numBytesReceived;
    }

    public int getNumPdusSent () {
        return pdusSent;
    }

    public void setNumPdusSent ( int msgSent ) {
        this.pdusSent = msgSent;
    }

    public int getNumPdusReceived () {
        return pdusReceived;
    }

    public void setNumPdusReceived ( int msgReceived ) {
        this.pdusReceived = msgReceived;
    }

    public int getNumUniqueTaskUIDs () {
        return numUniqueTaskUIDs;
    }

    public void setNumUniqueTaskUIDs ( int numUniqueIds ) {
        this.numUniqueTaskUIDs = numUniqueIds;
    }

    public int getNumTaskChanges () {
        return numTaskChanges;
    }

    public void setNumTaskChanges ( int numChanges ) {
        this.numTaskChanges = numChanges;
    }

    public int getNumTaskAdds () {
        return numTaskAdds;
    }

    public void setNumTaskAdds ( int numAdds ) {
        this.numTaskAdds = numAdds;
    }

    public int getNumTaskRemoves () {
        return numTaskRemoves;
    }

    public void setNumTaskRemoves ( int numRemoves ) {
        this.numTaskRemoves = numRemoves;
    }

    public int getNumMsgsSent () {
        return numRelayMsgsSent;
    }

    public void setNumMsgsSent ( int numRelayMsgsSent ) {
        this.numRelayMsgsSent = numRelayMsgsSent;
    }

    public int getNumTasksSeenDebug ()
    {
        return numTasksSeenDebug;
    }

    public void setNumTasksSeenDebug ( int numTasksSeenDebug )
    {
        this.numTasksSeenDebug = numTasksSeenDebug;
    }

    public int getNumAddsTotal() {
        return numAddsTotal;
    }

    public void setNumAddsTotal(int numAddsTotal) {
        this.numAddsTotal = numAddsTotal;
    }

    /**
     * Batched and wrapped messages sent.
     */
    protected int numRelayMsgsSent ;
    protected long firstEventTime ;
    protected long lastEventTime ;

    /**
     * The number of bad (-1) timestamps handed out by the service.
     */
    protected int numBadTimestamps ;
    protected long numBytesSent = 0  ;
    protected long numBytesReceived = 0 ;
    protected int pdusSent = 0 ;
    protected int pdusReceived = 0 ;
    protected int numUniqueTaskUIDs = 0 ;
    protected int numTaskChanges = 0 ;
    protected int numTaskAdds = 0 ;
    protected int numTaskRemoves = 0 ;

    // DEBUG
    protected int numTasksSeenDebug = 0 ;
    protected int numSubscriptionTotal = 0 ;
    protected int numAddsTotal = 0 ;
    protected int numRemovesTotal = 0 ;
    protected int numChangesTotal = 0 ;

    protected static final long serialVersionUID = 0L ;
}
