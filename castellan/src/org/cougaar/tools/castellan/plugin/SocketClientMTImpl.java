package org.cougaar.tools.castellan.plugin;
import java.net.* ;
import java.io.* ;
import java.util.* ;
import java.lang.reflect.* ;
import org.cougaar.tools.castellan.pdu.* ;
import org.cougaar.tools.castellan.util.* ;
import org.cougaar.core.plugin.* ;
import org.cougaar.core.agent.*;
import org.cougaar.util.* ;
import org.cougaar.core.mts.*;

//import com.ibm.xml.parser.Parser;

import org.xml.sax.InputSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/** Threaded message transport implementation using sockets.
 */
public class SocketClientMTImpl implements ClientMessageTransport {
    /** Socket timeout.  Higher values decrease responsiveness in shutting down.  Lower values
     *  increase CPU load.
     */
    public static final int SO_TIMEOUT = 1500 ;

    /** Size of sending buffer.
     */
    public static final int SENDBUF_SIZE = 1300000 ;

    protected Field[] makeReplaceTable( Class c ) {
//        System.out.println( "Making replace table for " + c ) ;
        ArrayList result = new ArrayList() ;
        if ( !EventPDU.class.isAssignableFrom( c ) ) {
            System.out.println( "Warning " + c + " is not subclass of EventPDU" ) ;
            throw new RuntimeException( "Class " + c + " is not a subclass of EventPDU" ) ;
        }


        ArrayList fieldLists = new ArrayList() ;
        Class cc = c;
        while ( cc != Object.class ) {
            Field[] fields = cc.getDeclaredFields() ;
            fieldLists.add( fields ) ;
            cc = cc.getSuperclass();
        }

        for (int j=0;j<fieldLists.size();j++) {
            Field[] fields = ( Field[] ) fieldLists.get(j) ;
            for (int i=0;i<fields.length;i++) {
                if ( !Modifier.isStatic( fields[i].getModifiers() ) &&
                     ( UIDPDU.class == fields[i].getType() ||
                       SymbolPDU.class == fields[i].getType() ) )

                {
                    fields[i].setAccessible( true ) ;
                    result.add( fields[i] ) ;
                }
            }
        }

        Field[] res = new Field[ result.size() ] ;
        for (int i=0;i<result.size();i++) {
            res[i] = ( Field ) result.get(i) ;
//            System.out.println( "Adding field " + res[i] ) ;
        }
        return res ;
    }

    protected Field[] getReplaceTable( Class c ) {
        Field[] f = ( Field[] ) replaceMap.get( c.getName() );
        if ( f == null ) {
            f = makeReplaceTable( c ) ;
            replaceMap.put( c.getName(), f  ) ;
        }
        return f ;
    }

    protected void replaceSymbols( EventPDU pdu, ArrayList symbols ) {
        Field[] replace = getReplaceTable( pdu.getClass() ) ;

        for (int i=0;i<replace.length;i++) {
            try {
                Object f = replace[i].get( pdu ) ;
                if ( f instanceof UIDStringPDU ) {
                    UIDStringPDU u = ( UIDStringPDU ) f ;
                    if (  table.resolveSymbol( u.getOwner() ) == -1 ) {
                        int sid = table.addSymbol( u.getOwner() ) ;
                        SymbolAckPDU spdu = new SymbolAckPDU( u.getOwner(), sid ) ;
                        symbols.add( spdu ) ;
                        UIDSymIDPDU us = new UIDSymIDPDU( sid, u.getId() ) ;
                        replace[i].set( pdu, us ) ;
                    }
                    // u.getOwner()
                }
                else if ( f instanceof SymStringPDU ) {
                    SymStringPDU u = ( SymStringPDU ) f ;
                    if (  table.resolveSymbol( u.toString() ) == -1 ) {
                        int sid = table.addSymbol( u.toString() ) ;
                        SymbolAckPDU spdu = new SymbolAckPDU( u.toString(), sid ) ;
                        symbols.add( spdu ) ;
                        SymIDPDU us = new SymIDPDU( sid ) ;
                        replace[i].set( pdu, us ) ;
                    }
                }
            }
            catch ( Exception e ) {
                e.printStackTrace();
            }
        }
    }

    public class PlugInSocketThread extends Thread {
        public void run() {

            while ( true ) {
                try {
                    if ( sin == null ) {
                        sin = new ObjectInputStream( new BufferedInputStream( socket.getInputStream() ) ) ;
                    }
                    Object o = sin.readObject() ;
                    //System.out.println( "PlugInSocketThread:: received " + o ) ;

                    // Fill in PDU stuff.
                    if ( o instanceof PDU ) {
                        PDU pdu = ( PDU ) o ;
                        pdu.setSource( PDU.SERVER ) ;
                        if ( pdu instanceof TimeRequestPDU ) {
                            TimeRequestPDU trpdu = ( TimeRequestPDU ) o ;
                            TimeAckPDU tapdu = new TimeAckPDU( trpdu.getSeqId(), System.currentTimeMillis(), 0 ) ;
                            sendMessage( tapdu ) ;
                        }
                        else {
                            if ( sink != null ) {
                                sink.processPDU( ( PDU ) o ) ;
                            }
                        }
                    }

                }
                catch ( InterruptedIOException e ) { // Handle timeouts quietly.
                    continue ;
                }
                catch ( java.net.SocketException e ) {
                    //System.out.println( e ) ;
                    System.out.println( clusterIdentifier.toString() + ":: Connection to Castellan server closed by server." ) ;
                    disconnect() ;
                    break ;
                }
                catch ( Exception e ) {
                    System.out.println( clusterIdentifier + ":: PlanMonitorPlugIn unexpected exception: "  ) ;
                    System.out.println( e ) ;
                    disconnect() ;
                    break ;
                    // e.printStackTrace();
                }
            }
        }
    }

    public SocketClientMTImpl( ConfigFinder cf, MessageAddress clusterIdentifier, PlanMonitorPlugIn newPlugIn) {
        this.plugIn = newPlugIn ;
        //this.cluster = newCluster ;
        this.configFinder = cf ;
        this.clusterIdentifier = clusterIdentifier ;
    }

    private InetAddress getAddressFromConfig() {
        Collection params = plugIn.getParameters() ;
        Vector paramVector = new Vector( params ) ;

        String fileName = null ;
        if ( paramVector.size() > 0 ) {
            fileName = ( String ) paramVector.elementAt(0) ;
        }

        // DEBUG
        // System.out.println( "Configuring PlanEventLogPlugIn from " + fileName ) ;

        InetAddress iaddr = null ;

        try {
            if ( fileName != null ) {
                ConfigFinder finder = configFinder ;
                String address = null ;

                File f = finder.locateFile( fileName ) ;
	            System.out.println( "Configuring PlanEventLogPlugIn from " + f ) ;

                if ( f != null && f.exists() ) {
                    //
                    // Now, parse the config file
                    //
                    
                    Document doc = null ;
                    try {
                        doc = configFinder.parseXMLConfigFile(fileName);
                    }
                    catch ( Exception e ) {
                        System.out.println( e ) ;
                    }
                    
                    if ( doc != null ) {
                        try {
                            Node root = doc.getDocumentElement() ;
                            if( root.getNodeName().equals( "plpconfig" ) ) {
                                NodeList nodes = doc.getElementsByTagName( "PlanLogServer" );
                                for (int i=0;i<nodes.getLength();i++) {
                                    Node n = nodes.item(i) ;
                                    address = n.getAttributes().getNamedItem( "address" ).getNodeValue() ;
                                }
                            }
                            else {
                                System.out.println( "plpconfig file is invalid!." ) ;
                            }
                        }
                        catch ( Exception e ) {
                            System.out.println( e ) ;
                        }
                    }
                    
                }
                
                if ( address == null ) {
                    System.out.println( "PlanMonitorPlugIn:: Warning: Falling back to localhost..." ) ;
                    address = "localhost" ;
                }
                
                iaddr =  InetAddress.getByName(address) ;
            }
        } catch ( Exception e ) {
            e.printStackTrace() ;
        }
        
        if ( iaddr == null ) {
            try {
                System.out.println( "PlanMonitorPlugIn:: Warning: Falling back to localhost." ) ;
                iaddr = InetAddress.getLocalHost() ;
            }
            catch ( Exception e ) {
            }
        }
        
	  System.out.println( "Returning " + iaddr ) ;
        return iaddr ;
    }

    public void execute()
    {
    }

    void disconnect() {
        plugIn.notifyDisconnected( this ) ;
        stop() ;
    }
        
    public boolean connect() {
        // Open a socket and send a DeclarePDU.
        
        InetAddress inetAddr = getAddressFromConfig() ;
        if ( inetAddr == null ) {
            return false ;
        }
                
        try {
            socket = new Socket( inetAddr, 9003 ) ;
        }
        catch ( Exception e ) {
            System.out.println( "\nPlanMonitorPlugIn:: Could not open PlanMonitor's server socket on " + inetAddr + " at port 9003" ) ;
            socket = null ;
            return false ;
            // e.printStackTrace();
        }

        try {
            sout = new ObjectOutputStream( new BufferedOutputStream( socket.getOutputStream() ) ) ;
            socket.setSoTimeout( SO_TIMEOUT ) ;
            socket.setSendBufferSize( SENDBUF_SIZE ) ;
        }
        catch ( Exception e ) {
            e.printStackTrace() ;   
        }
        
        if ( sout== null ) {
            System.out.println( "Could not establish connection with server." ) ;   
        }
        
        // Spawn a new high-priority thread to respond to TimeRequestPDUs
        thread = new PlugInSocketThread() ;
        thread.start() ;
        
        // Declare myself
        DeclarePDU pdu = new DeclarePDU( clusterIdentifier.toString()  ) ;
        // System.out.println( "Sending " + pdu ) ;
        sendMessage( pdu ) ;
        
        return true ;
    }
    
    //public int enqueueForTransmission( EventPDU pdu ) {
    //
    //}
    
    // Resolve a symbol.
    //public int addResolveSymbolMessage( String s, SymbolEventListener rsl ) {
    //    return -1 ;
    //}
    
    public void setPreferences(Map prefs) {
    }
    
    /** Returns a description of the server.
     */
    public Object getServer() {
        return null ;
    }
    
    /** Stop the session.
     */
    public void stop() {
        try {
            socket.close() ;
        }
        catch ( IOException e ) {
        //    e.printStackTrace() ;
        }
        socket = null ;
        sout = null ;
        sin = null ;
    }
    
    public boolean isConnected() {
        return socket != null ;
    }
    
    public void setPDUSink(PDUSink sink) {
        this.sink = sink ;
    }

    public synchronized void sendMessage(PDU pdu) {
        if ( sout != null ) {
            try {
                // System.out.println( "Processing " + pdu ) ;
                // compress the PDU first replacing all Strings with SymIds
                if ( compress && pdu instanceof EventPDU ) {
                    slist.clear();
                    replaceSymbols( ( EventPDU ) pdu, slist );
                    if ( slist.size() > 0 ) {
                        for (int i=0;i<slist.size();i++) {
                            sout.writeObject( slist.get(i));
                        }
                    }
                }
                
                sout.writeObject( pdu ) ;
                sout.flush() ;
                sout.reset() ;
            }
            catch ( Exception e ) {
            }
        }
    }

    /**
     * Flush any state associated with this MT.
     */
    public void flush()
    {
    }

    protected ArrayList slist = new ArrayList() ;
    protected boolean compress = true ;

    protected HashMap replaceMap = new HashMap() ;
    protected SymbolTable table = new SymbolTable() ;
    protected PlugInSocketThread thread ;
    protected PDUSink sink ;
    protected PlanMonitorPlugIn plugIn ;
    protected Socket socket ;
    protected ObjectOutputStream sout ;
    protected ObjectInputStream sin ;
    protected ConfigFinder configFinder;
    //protected ClusterIdentifier clusterIdentifier ;
    protected MessageAddress clusterIdentifier ;
}
