/*
 * DataLogServer.java
 *
 * Created on June 12, 2001, 3:46 PM
 */

package org.cougaar.tools.castellan.planlog;

import java.util.* ;
import org.cougaar.tools.castellan.pdu.* ;
import org.cougaar.core.util.UID;
/**
 * Abstracts the database for the use of analytical and metric plugIns.
 * Provides a generic set of query services to the outside world.
 *
 * @author  wpeng
 * @version
 */
public interface EventLog {

   public  void add( PDU p ) ;

    public void clear() ;

    public void close() ;

    /** Returns all asset events.
     */
    public  Iterator getAssetEvents() ;

    /** Returns all asset events between start and end.
     */
    public  Iterator getAssetEvents( long start, long end ) ;

    /** Get all events.
     */
    public  Iterator getEvents() ;

    /** Get all events between a start time and end time.
     */
    public  Iterator getEvents( long start, long end ) ;

    public  Iterator getEvents( UID uid ) ;

    public  Iterator getEvents( UID uid, long start, long end ) ;

    /**
    * Returns the intersection of the set of all plugin executions that started and
    * the set of all plugins that completed execution in the given time interval.
    * Find number of events from HashSet.size(). Get iterator from HashSet.iterator().
    **/
    public Iterator getExecutionsActiveOnly( long start, long end );

    /**
    * Returns the union of the set of all plugin executions that started and
    * the set of all plugins that completed execution in the given time interval.
    * Find number of events from HashSet.size(). Get iterator from HashSet.iterator().
    **/
    public Iterator getExecutionsActiveSometime( long start, long end );

    /**
    * Returns all execution events that started in the given time interval.
    **/
    public  Iterator getExecutionsStarted( long start, long end );

    /**
    * Returns all execution events that Stopped in the given time interval.
    **/
    public  Iterator getExecutionsStopped( long start, long end );

    public  long getFirstEventTime() ;

    public  long getFirstExecutionTime() ;

    public  long getLastEventTime() ;

    public  long getLastExecutionTime() ;

    public int getNumAssetEvents();

    public int getNumAssetEvents( long start, long end );

    public int getNumEvents();

    public int getNumEvents( long start, long end ) ;

    /**
    * Returns the number of execution events that started in the given time interval.
    **/
    public  int getNumExecutionsStarted( long start, long end );

    /**
    * Returns the number of execution events that stopped in the given time interval.
    **/
    public int getNumExecutionsStopped( long start, long end );

    public int getNumUIDs() ;

    public int getNumUIDs( long start, long end ) ;

}
