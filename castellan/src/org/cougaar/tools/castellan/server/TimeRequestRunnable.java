/*
 * TimeRequestThread.java
 *
 * Created on June 19, 2001, 10:53 AM
 */

package org.cougaar.tools.castellan.server;
import org.cougaar.tools.castellan.pdu.* ;
import java.util.* ;

/**
 *
 * @author  wpeng
 * @version
 */
public class TimeRequestRunnable implements Runnable {
    
    /** Creates new TimeRequestThread */
    public TimeRequestRunnable(ServerMessageTransport smt, String clusterName) {
        this.smt = smt ;
        this.clusterName = clusterName ;
    }
    
    public void run() {
        int id = 0 ;
        TimeRequestPDU srpdu = new TimeRequestPDU(id = getNextId(), System.currentTimeMillis() ) ;
        srpdu.setDestination( clusterName ) ;
        srpdu.setSource( PDU.SERVER ) ;
        
//        synchronized ( idToClusterMap ) {
            idToClusterMap.put( new Integer( id ), srpdu ) ; // Cache this here.
//        }
        smt.sendMessage( srpdu ) ;
        
    }
    
    public synchronized static int getNextId() {
        return seqId++ ;
    }
    
    public static TimeRequestPDU getRequest( int seqId ) {
//        synchronized ( idToClusterMap ) {
            return ( TimeRequestPDU ) idToClusterMap.get( new Integer( seqId ) ) ;
//        }
    }
    
    public static void removeRequest( int seqId ) {
//        synchronized ( idToClusterMap ) {
            idToClusterMap.remove( new Integer( seqId ) ) ;
//       }
    }
    
    protected ServerMessageTransport smt ;
    protected String clusterName ;
    
    private static Map idToClusterMap = Collections.synchronizedMap( new HashMap() ) ;
    private static int seqId = 1 ;
}
