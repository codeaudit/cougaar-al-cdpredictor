/*
 * ServerMessageTransport.java
 *
 * Created on June 14, 2001, 4:32 PM
 */

package org.cougaar.tools.castellan.server;
import org.cougaar.tools.castellan.pdu.* ;
import java.util.* ;

/**
 *
 * @author  wpeng
 * @version 
 */
public interface ServerMessageTransport {
    
    /** Start accepting clients.
     */
    public void start() ;
    
    // public long getEstimatedSkew( SymbolPDU symPDU ) ;
    
    /** End the current logging session and disconnect from all clients.
     */
    public void stop() ;

    /** Returns a Vector of cluster names.
     */
    public Vector getClients() ;
        
    /** If a PDUSink is set, the MT will automatically insert messages
     * into the sink as fast as it can process them. Hence, the PDUSink
     * should enqueue messages somewhere as quickly as possible.
     */
    public void setPDUSink( PDUSink sink ) ;
    
    public void sendMessage( PDU pdu ) ;

    /**
     * If this MT serves a plug-in, the plug-in should call this once per
     * execute cycle.
     */
    public void execute() ;
}