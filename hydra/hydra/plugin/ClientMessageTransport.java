/*
 * MessageTransport.java
 *
 * Created on June 12, 2001, 1:42 PM
 */

package org.hydra.plugin;
import org.hydra.pdu.* ;
import java.util.* ;

/**
 * The interface for client-side message transport.  The Client plug-in
 * will run in the agent's main thread, but the message handling may have its own
 * higher priority thread to avoid overrunning input/output buffers.
 * 
 * @author  wpeng
 * @version 
 */
public interface ClientMessageTransport {
    
   public void setPreferences( Map prefs ) ;
   
   /** Initial the connection between the server and the client.
    */
   public boolean connect() ;
   
   public boolean isConnected() ;
   
   /** Stop the session.
    */
   public void stop() ;
   
   /** Returns a description of the server.
    */
   public Object getServer() ;

   public void setPDUSink( PDUSink sink ) ;
   
   public void sendMessage( PDU pdu ) ;
}
