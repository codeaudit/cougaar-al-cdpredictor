package org.cougaar.tools.castellan.server ;
import java.io.* ;
import org.cougaar.tools.castellan.pdu.* ;
import java.net.* ;
import java.util.* ;
import org.cougaar.tools.castellan.util.* ;
import java.lang.reflect.* ;

/**
 * Currently, each client (corresponding to a cluster/agent is associated with
 * a thread/socket pair.  This may be not be the most scalable approach to
 * implementation, however.
 *
 * @author  wpeng
 * @version
 */
public class ClientThread extends Thread {
    /** Average clock skew computed during the synchronization phase.
     */
    protected long pingCount ;
    protected long numEvents ;
    protected long clockSkew ;
    protected double clockSkew2 ;
    protected double clockSkewMean ;
    protected long sumDelay ;
    protected boolean isDeclared = false ;
    protected boolean suspend = false ;
    protected Socket socket ;
    protected ObjectInputStream ois ;
    protected ObjectOutputStream oos ;
    protected boolean disconnect = false ;
    protected String clusterName ;
    protected SocketServerMTImpl mt ;
    protected HashMap replaceMap = new HashMap() ;
    protected HashMap idToStringMap = new HashMap() ;

    public String getClusterName() { return clusterName ; }

    public Socket getSocket() { return socket ; }

    public ObjectOutputStream getObjectOutputStream() { return oos ; }

    protected Field[] makeReplaceTable( Class c ) {
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

    protected void replaceSymbols( EventPDU pdu ) {
        Field[] replace = getReplaceTable( pdu.getClass() ) ;

        for (int i=0;i<replace.length;i++) {
            try {
                Object f = replace[i].get( pdu ) ;
                if ( f instanceof UIDSymIDPDU ) {
                    UIDSymIDPDU u = ( UIDSymIDPDU ) f ;
                    Integer sid = new Integer( u.getOwnerSID() ) ;
                    String symbol = ( String ) idToStringMap.get( sid ) ;
                    if ( symbol != null ) {
                        UIDStringPDU us = new UIDStringPDU( symbol, u.getID() ) ;
                        replace[i].set( pdu, us ) ;
                    }
                    else {
                        System.out.println( "Warning: SID " + sid + " cannot be resolved in " + pdu ) ;
                    }
                    // u.getOwner()
                }
                else if ( f instanceof SymIDPDU ) {
                    SymIDPDU u = ( SymIDPDU ) f ;
                    Integer sid = new Integer( u.getID() ) ;
                    String symbol = ( String ) idToStringMap.get( sid ) ;
                    if ( symbol != null ) {
                        SymStringPDU us = new SymStringPDU( symbol ) ;
                        replace[i].set( pdu, us ) ;
                    }
                    else {
                        System.out.println( "Warning: SID " + sid + " cannot be resolved in " + pdu ) ;
                    }
                }
            }
            catch ( Exception e ) {
                e.printStackTrace();
            }
        }
    }

    public ClientThread( SocketServerMTImpl mt, Socket s ) {
        //ServerApp.instance().println( "Initializing client thread." ) ;
        this.mt = mt ;
        this.socket = s ;
        try {
            socket.setSoTimeout( 1500 ) ;
            socket.setReceiveBufferSize(600000);
            oos = new ObjectOutputStream( new BufferedOutputStream( socket.getOutputStream() ) ) ;
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }
        //ServerApp.instance().println( "Done." ) ;
        setPriority( Thread.NORM_PRIORITY + 2 ) ;
    }

    public long getPingCount() { return pingCount ; }

    public long getClockSkewMean() {
        if ( pingCount == 0 ) {
            return 0 ;
        }
        return ( long ) clockSkewMean ;
    }
    
    public long getDelay() {
        return ( long ) ( ( double ) sumDelay / ( double ) pingCount ) ;
    }
    
    /** Returns sqrt( E( X^2 ) - E(X) ^ 2 )
     */
    public double getClockSkewVariance() {
        double value = ( ( double ) clockSkew / ( double ) pingCount ) ;
        return Math.sqrt( ( clockSkew2 / pingCount - value * value ) );
    }
    
    public void resetClockSkew() {
        pingCount = 0 ;
        clockSkew = 0 ;
        sumDelay = 0 ;
        clockSkew2 = 0 ;
    }
    
    public long getNumEvents() { return numEvents ; }
    
    /** Maintains a running average of the estimated clock skew between the server and client.
     */
    public void updateClockSkew( long skewValue, long delay ) {
        clockSkew += skewValue ;
        clockSkew2 += skewValue * skewValue ;
        sumDelay += delay ;
        pingCount ++ ;
        if ( pingCount > 0 ) {
            clockSkewMean = clockSkew / pingCount ;
        }
    }
    
    private void processPDU( PDU pdu ) {
        if ( pdu instanceof SymbolAckPDU ) {
            SymbolAckPDU sack = ( SymbolAckPDU ) pdu ;
//            System.out.println( getClusterName() +"::Processing symbol" + pdu ) ;
            idToStringMap.put( new Integer( sack.getSymId() ),
                ( ( String ) sack.getSymbol() ).intern() )  ;
            return ;
        }

        if ( clusterName != null ) {
            pdu.setSource( clusterName ) ;
        }
        else {
            pdu.setSource( PDU.UNKNOWN ) ;
        }
        pdu.setDestination( PDU.SERVER ) ;

        // Correct for clock skew.
        if ( pdu instanceof EventPDU ) {
            if ( pingCount > 3 ) {
                numEvents++ ;
                EventPDU epdu = ( EventPDU ) pdu ;
                epdu.setTime( epdu.getTime() + getClockSkewMean() ) ;
            }
            else {
                buf.add( pdu ) ;
                return ;  // Wait to process
            }
        }

        // Resolve any symbols which are not resolved.
        if ( pdu instanceof EventPDU ) {
            replaceSymbols( ( EventPDU) pdu );
        }

        mt.putMessage( this, pdu )  ;
    }

    ArrayList buf = new ArrayList() ;
    
    public void run() {
        //if ( isAlive() ) {
        //    return ; // Not reentrant!   
        //}
        
        if ( socket != null ) {
            while ( !disconnect ) {
                try {
                    if ( ois == null ) {
                        ois = new ObjectInputStream( new BufferedInputStream( socket.getInputStream() ) ) ;
                    }

                    // Clear the delayed event buffer
                    if ( buf.size() > 0 && pingCount > 3 ) {
                        for (int i =0;i<buf.size();i++) {
                           PDU p = ( PDU ) buf.get(i) ;
                           processPDU( p ) ;
                        }
                        buf.clear() ;
                    }                    
                    
                    // System.out.println( "Waiting for pdu " + this ) ;
                    Object o = ois.readObject() ;
                    
                    // Fill in PDU stuff.
                    if ( o instanceof PDU ) {
                        processPDU( ( PDU ) o ) ;
                    }
                }
                catch ( InterruptedIOException e ) {
                    continue ;
                }
                catch ( SocketException e ) {
                    break ;                    
                }
                catch ( Exception e ) {
                    System.out.println( "Unexpected exception receiving message." ) ;
                    e.printStackTrace() ;
                    break ;
                    //System.out.println( e ) ;
                    // e.printStackTrace();
                }
            }
            ServerApp.instance().println( "Client " + clusterName + " disconnected by server."  ) ;
            mt.removeClient( this ) ;
        }
    }
    
    public void setSuspend( boolean suspend ) {
       this.suspend = suspend ;   
    }
    
    // Stop the client thread from running and accepting messages.
    public void halt() {
        disconnect = true ;
        // interrupt() ;  // Does nothing
    }
    
}
