/*
 * ServerPDUSink.java
 *
 * Created on June 18, 2001, 11:20 AM
 */

package org.cougaar.tools.castellan.server;
import org.cougaar.tools.castellan.pdu.* ;
import org.cougaar.tools.castellan.planlog.* ;
import java.util.* ;

/**
 * The server PDU sink is responsible for queueing the incoming PDUs for
 * use in another thread.  Currently, it just maintains a circular queue of PDUs to be processed.
 * Any time base correction is done by querying the SocketServerMTImpl.
 *
 * @author  wpeng
 * @version 
 */
public class ServerPDUSink implements org.cougaar.tools.castellan.pdu.PDUSink {

    /** Creates new ServerPDUSink. */
    public ServerPDUSink( EventLog el ) {
        this.eventLog = el ;
    }
    
    public void addPDUSink( PDUSink sink ) {
        sinks.add( sink ) ;   
    }
    
    public void removePDUSink( PDUSink sink ) {
        sinks.remove( sink ) ;   
    }

    /** Process all logplan related PDUs.
     */
    public void processPDU(PDU pdu) {
        if ( pdu == null ) {
            throw new IllegalArgumentException("Null cannot be added." ) ;   
        }
        
        if ( !logEvents ) {
            return ;   
        }
        
        synchronized ( eventLog ) {
            eventLog.add( pdu ) ;
            //cq.add( pdu ) ;
        }
        
        synchronized ( sinks ) {
            for (int i=0;i<sinks.size(); i++) {
                PDUSink sink = ( PDUSink ) sinks.get(i) ;
                sink.processPDU( pdu ) ;
            }
        }
    }
    
    //public boolean hasNext() {
    //    synchronized ( cq ) {
    //       return cq.isEmpty() ;
    //    }
    //}
    
    //public PDU getNext() {
    //    synchronized ( cq ) {
    //       return ( PDU ) cq.next() ;   
    //    }
    //}
    
    protected ArrayList sinks = new ArrayList() ;
    protected EventLog eventLog ;
    protected boolean logEvents = true ;
    //org.cougaar.util.CircularQueue cq = new org.cougaar.util.CircularQueue() ;
}
