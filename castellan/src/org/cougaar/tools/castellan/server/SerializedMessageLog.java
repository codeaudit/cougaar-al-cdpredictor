/*
 * MessageLog.java
 *
 * Created on July 1, 2001, 5:13 PM
 */

package org.cougaar.tools.castellan.server;
import java.io.* ;
import org.cougaar.tools.castellan.pdu.* ;

/**
 * A very simple message log which simply writes everything to a file.
 *
 * @author  wpeng
 * @version
 */
public class SerializedMessageLog implements java.io.Serializable, PDUSink {
    
    ObjectOutputStream oos ;
    FileOutputStream fos ;
    int messageCount = 0 ;
    
    /** Creates new MessageLog */
    public SerializedMessageLog() {
    }
    
    public synchronized void setFile( File file ) {
        try {
            if ( oos != null ) {
                oos.close() ;
            }
            
            // Create new streams
            fos = new FileOutputStream( file ) ;
            oos = new ObjectOutputStream( new BufferedOutputStream( fos ) ) ;
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }
    }
        
    public synchronized void close() {
        try {
            if ( oos != null ) {
                oos.close() ;
            }
            if ( fos != null ) {
                fos.close() ;
            }
            oos = null; fos = null ;
        }
        catch ( Exception e ) {
            e.printStackTrace() ;
        }
    }
    
    public void processPDU(PDU pdu) {
        if ( oos != null ) {
            try {
                oos.writeObject( pdu ) ;
                System.out.println( "+" ) ;
                messageCount++ ;
            }
            catch ( Exception e ) {
                e.printStackTrace() ;
            }
        }
    }
    
}