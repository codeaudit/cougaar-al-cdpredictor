/*
 * InMemoryLog.java
 *
 * Created on August 28, 2001, 4:26 PM
 */

package org.hydra.planlog;
import org.hydra.pdu.* ;
import org.hydra.util.* ;
import java.util.* ;

/**
 *
 * @author  wpeng
 * @version 
 */
public class InMemoryEventLog  implements EventLog {

    /** Creates new InMemoryLog */
    public InMemoryEventLog() {
    }
    
    public synchronized void add( PDU p ) {
        Long l ;
        EventPDU pdu = ( EventPDU ) p ;
        allEvents.put( l = new Long( pdu.getTime() ), pdu ) ;
        if ( pdu instanceof AssetPDU ) {
           assetEvents.put( l, pdu ) ;    
        }
        if ( pdu instanceof UniqueObjectPDU ) {
            UniqueObjectPDU updu = ( UniqueObjectPDU ) pdu ;
            UIDPDU sp = updu.getUID() ;
            set.put( sp, pdu ) ;
        }
    }

    public int getNumEvents() { return allEvents.size() ; }

    public int getNumAssetEvents() { return assetEvents.size() ; }

    public void close() { }
    
    public void clear() {
        assetEvents.clear() ;
        allEvents.clear() ;
        set.clear() ;
    }

    /** Get all events between a start time and end time.
     */
    public synchronized Iterator getEventsBetween(long start, long end) {
        return allEvents.subMap( new Long( start ),  new Long( end ) )  ;
    }
    
    public synchronized long getFirstEventTime() {
        if ( allEvents.size() == 0 ) {
           return 0 ;   
        }
        Long l = (Long ) allEvents.firstKey() ;
        return l.longValue() ;
    }
    
    public synchronized long getLastEventTime() {
        if ( allEvents.size() == 0 ) {
            return 300000 ;   
        }
        Long l = ( Long ) allEvents.lastKey() ;
        return l.longValue() ;
    }
        
    public Iterator getEventsByUID(String uid) {
        throw new UnsupportedOperationException() ;
    }
    
    /** Returns all asset events between start and end.
     */
    public synchronized Iterator getAssetEvents(long start, long end) {
        return assetEvents.subMap( new Long( start ),  new Long( end ) ) ;
    }
    
    /** Returns all asset events.
     */
    public synchronized Iterator getAssetEvents() {
        return assetEvents.elements()  ;
    }
        
    public int getNumEventsBetween(long start, long end) {
        Iterator iter = assetEvents.subMap( new Long( start ),  new Long( end ) ) ;
        int count = 0 ;
        while ( iter.hasNext() ) {
            EventPDU pdu = ( EventPDU ) iter.next() ;
            count ++ ;
        }
        return count ;
    }
    
    public int getNumUniqueUIDs(long start, long end) {
        Iterator iter = assetEvents.subMap( new Long( start ),  new Long( end ) ) ;
        HashMap map = new HashMap() ;
        while ( iter.hasNext() ) {
            EventPDU pdu = ( EventPDU ) iter.next() ;
            if ( pdu instanceof UniqueObjectPDU ) {
                UniqueObjectPDU updu = ( UniqueObjectPDU ) pdu ;
                map.put( updu.getUID(), updu.getUID() ) ;
            }
        }
        return map.size() ;        
    }
    
    MultiTreeSet assetEvents = new MultiTreeSet() ;
    MultiTreeSet allEvents = new MultiTreeSet() ;
    org.hydra.util.MultiHashSet set = new org.hydra.util.MultiHashSet() ;
}
