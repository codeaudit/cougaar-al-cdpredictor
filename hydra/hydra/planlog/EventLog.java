/*
 * DataLogServer.java
 *
 * Created on June 12, 2001, 3:46 PM
 */

package org.hydra.planlog;
import java.util.* ;
import org.hydra.pdu.* ;

/**
 * Abstracts the database for the use of analytical and metric plugIns.
 * Provides a generic set of query services to the outside world.
 *
 * @author  wpeng
 * @version 
 */
public interface EventLog {
    public void clear() ;

    public void close() ;
    
    public void add( PDU p ) ;
    
    public long getFirstEventTime() ;
    
    public long getLastEventTime() ;
    
    /** Returns all asset events.
     */
    public Iterator getAssetEvents() ;
    
    /** Returns all asset events between start and end. 
     */
    public Iterator getAssetEvents( long start, long end ) ;
    
    /** Get all events between a start time and end time.
     */
    public Iterator getEventsBetween( long start, long end ) ;
    
    public Iterator getEventsByUID( String uid ) ;
    
    public int getNumUniqueUIDs( long start, long end ) ;
    
    public int getNumEventsBetween( long start, long end ) ;
}
