/*
 * PlanMonitorServer.java
 *
 * Created on June 14, 2001, 3:52 PM
 */

package org.cougaar.tools.castellan.server;
import java.io.* ;
import java.net.* ;
import org.cougaar.tools.castellan.pdu.* ;
import java.util.* ;
import org.cougaar.tools.castellan.util.* ;

/**
 * Socket based ServerMessageTransport implementation.
 * By default, it listens on port 9003 for new clients.
 *
 * @author  wpeng
 * @version
 */
public class SocketServerMTImpl implements ServerMessageTransport
//implements ServerMessageTransport
{
    class SocketAcceptorThread extends Thread {
        protected ServerSocket ss ;
        
        public SocketAcceptorThread() {
            try {
                ss = new ServerSocket( 9003, 10 ) ;
                ss.setSoTimeout( 1500 ) ;
                ServerApp.instance().println("Creating server socket " + ss ) ;
            }
            catch ( Exception e ) {
                e.printStackTrace();
            }
        }
        
        public void setHalt( boolean halt ) {
            this.halt = halt ;
        }
        
        public void run() {
            ServerApp.instance().println( "Running SocketServerMT..." ) ;
            while ( !halt ) {
                Socket s = null ;
                try {
                    s = ss.accept() ;
                    s.setReceiveBufferSize(3000000); // Make a big buffer.
                    ServerApp.instance().println( "Accepting client socket " + s + " from " + s.getInetAddress() ) ;
                    System.out.flush();
                }
                catch ( InterruptedIOException e ) {
                }
                catch ( Exception e ) {
                    e.printStackTrace();
                }
                if ( s != null ) {
                    addClient( s ) ;
                }
            }
            
            try {
            ss.close() ;
            }
            catch ( IOException e ) {
                System.out.println( "Error closing " + ss ) ;
            }
        }
        
        boolean halt = false ;
    }

    /** Creates new PlanMonitorServer */
    public SocketServerMTImpl() {
        // Get defaults and set them up
    }
    
    /** Start accepting clients.
     */
    public void start() {
        sa = new SocketAcceptorThread() ;
        sa.start() ;
    }
    
    /** Send a message to a client.  Have to decide how to handle this.
     */
    public synchronized void sendMessage( PDU pdu ) {
        if ( pdu.getDestination() == null ) {
            throw new RuntimeException( "PDU has no destination." ) ;
        }
        ClientThread ct = ( ClientThread ) clientTable.get( pdu.getDestination() ) ;
        
        if ( ct == null ) {
            throw new RuntimeException( "SocketServerMTImpl: PDU destination " + pdu.getDestination() + " cannot be found." ) ;
        }
        
        try {
            System.out.print( "-" ) ;
            // System.out.println( "Sending " + pdu + " to " + ct.getClusterName() ) ;
            ct.getObjectOutputStream().writeObject( pdu ) ;
            ct.getObjectOutputStream().flush() ;
            ct.getObjectOutputStream().reset() ;
        }
        catch ( IOException e ) {
            e.printStackTrace() ;
        }
    }
    
    /** Terminate all connections.
     */
    public void stop() {
        for (Enumeration e = clientThreads.elements();e.hasMoreElements(); ) {
            ClientThread ct = ( ClientThread ) e.nextElement() ;
            try {
                ct.halt() ;
                ct.wait() ;
            }
            catch  ( Exception exception ) {
            }
        }
        // clientThreads.clear() ;
        
        // Stop the socket acceptor thread.
        if ( sa != null ) {
            sa.setHalt( true ) ;
            sa = null ;
        }
        // sa.wait() ;
    }
    
    /** Returns a list of clients currently connected.
     */
    public Vector getClients() {
        return ( Vector ) clientThreads.clone()  ;
    }
    
    /** If a PDUSink is set, the transport will insert messages
     * into the sink as fast as it can process them to avoid possibly
     * overloading the input buffer.
     */
    public void setPDUSink( PDUSink sink ) {
        pduSink = sink ;
    }
    
    protected void removeClient( ClientThread thread ) {
        clientThreads.remove( thread ) ;
        if ( clientTable.get( thread.getClusterName() ) == thread ) {
            clientTable.remove( thread.getClusterName() ) ;
        }
    }
    
    protected void addClient( Socket s ) {
        // Create a new thread for each client socket.  Without non-blocking I/O,
        // we need a thread for each agent client.
        // ServerApp.instance().println( "Adding client for " + s ) ;
        ClientThread st = new ClientThread( this, s ) ;
        st.setPriority( Thread.NORM_PRIORITY + 2 ) ;
        clientThreads.addElement( st );
        
        // Now, start it.
        st.start();
    }
    
    protected synchronized void putMessage( ClientThread ct, PDU pdu ) {
        // Process meta pdus first.
        
        System.out.print( '+' ) ;
        if (!processMetaPDU(ct,pdu) && pduSink != null ) {
            pduSink.processPDU( pdu ) ;
        }
    }
    
    private void processDeclarePDU( ClientThread thread, PDU pdu ) {
        DeclarePDU dpdu = ( DeclarePDU ) pdu ;
        if ( dpdu.getName() != null ) {
            if ( clientTable.get( dpdu.getName() ) == null ) {
                ServerApp.instance().println( "Agent " + dpdu.getName() + " declared." ) ;
                thread.clusterName = dpdu.getName() ;
                clientTable.put( thread.clusterName, thread ) ;
                //SymbolAckPDU spdu = new SymbolAckPDU( dpdu.getName(), nameTable.resolveSymbol( dpdu.getName() ) ) ;
                //sendMessage( thread, spdu ) ;
                
                // Schedule a bunch of TimeRequestPDU events to run at one second intervals
                // intervals
                if ( !eventThread.isAlive() ){
                    eventThread.start() ;
                }
                
                //for (int i=0;i<4;i++) {
                    TimeRequestRunnable trr = new TimeRequestRunnable( this, thread.clusterName ) ;
                    eventThread.scheduleEvent( System.currentTimeMillis() + 1000, trr ) ;
                //}
            }
            else {
                ServerApp.instance.println( "Warning: Declared client " + dpdu.getName() + " already exists." ) ;
            }
        }
        
    }
    
    /** Intercept DeclarePDU and TimeReqPDUs and any other non-plan related PDUs.
     */
    private boolean processMetaPDU( ClientThread thread, PDU pdu ) {
        boolean result = false ;
        
        // The client is declaring its cluster name.
        //  In response, the server initializes the sychronization procedure.
        if ( pdu instanceof DeclarePDU ) {
            processDeclarePDU( thread, pdu ) ;
            result = true ;
        }
        else if ( pdu instanceof TimeAckPDU ) {  // Client responds with time
            // app.println( "Processing " + pdu ) ;
            TimeAckPDU tap = ( TimeAckPDU ) pdu ;
            TimeRequestPDU trp = TimeRequestRunnable.getRequest( tap.getSeqId() ) ;
            TimeRequestRunnable.removeRequest( tap.getSeqId() ) ;
            long delay = System.currentTimeMillis()- trp.getTime() ;
            long skew = trp.getTime() - ( tap.getTime() - delay / 2 ) ;
            thread.updateClockSkew( skew, delay ) ;

            // Send the next ping message?
            if ( thread.pingCount < 5 ) {
                TimeRequestRunnable trr = new TimeRequestRunnable( this, thread.clusterName ) ;
                eventThread.scheduleEvent( System.currentTimeMillis() + delay + 500, trr ) ;
            }
            
            result = true ;
        }
        
        return result ;
    }

    public void execute()
    {
        // Do thing, since I am not bound to a plug-in or component.
    }

    /** Blocking call to send message.
     */
    private synchronized void sendMessage( ClientThread thread, PDU pdu ) {
        try {
            //if ( pdu instanceof TimeRequestPDU ) {
            //}
            thread.getObjectOutputStream().writeObject( pdu ) ;
        }
        catch ( IOException e ) {
            e.printStackTrace() ;
        }
    }
    
    private ScheduledEventThread eventThread = new ScheduledEventThread() ;
    private PDUSink pduSink ;
    private SocketAcceptorThread sa ;
    private HashMap clientTable = new HashMap() ;
    private Vector clientThreads = new Vector() ;
    private SymbolTable nameTable = new SymbolTable() ;
   
}
