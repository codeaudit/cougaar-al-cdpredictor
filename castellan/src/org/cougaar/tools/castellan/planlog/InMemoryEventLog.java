/*
  * <copyright>
  *  Copyright 2002 (Penn State University and Intelligent Automation, Inc.)
  *  under sponsorship of the Defense Advanced Research Projects
  *  Agency (DARPA).
  *
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the Cougaar Open Source License as published by
  *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
  *
  *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
  *  PROVIDED "AS IS" WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
  *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
  *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
  *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
  *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
  *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
  *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
  *  PERFORMANCE OF THE COUGAAR SOFTWARE.
  *
  * </copyright>
  *
  */

package org.cougaar.tools.castellan.planlog;

import org.cougaar.core.util.UID;
import org.cougaar.tools.castellan.pdu.* ;
import org.cougaar.tools.castellan.util.* ;
import java.util.* ;
import java.lang.* ;

/**
 *
 * @author  wpeng
 * @version
 */
public class InMemoryEventLog  implements EventLog {

    // ATTRIBUTES
    int countPlugInEvents = 0;
    MultiTreeSet assetEvents = new MultiTreeSet() ;
    MultiTreeSet allEvents = new MultiTreeSet() ;
    MultiHashSet eventsByUID = new MultiHashSet() ;
    MultiTreeSet startPlugInEvents = new MultiTreeSet() ;
    MultiTreeSet stopPlugInEvents = new MultiTreeSet() ;
    MultiTreeSet eventsByPlugIn = new MultiTreeSet( new Comparator(){
       public int compare(Object o1, Object o2){
          Pair p1 = ( Pair ) o1, p2 = ( Pair ) o2 ;
          int value = ( ( String ) p1.first() ).compareTo( ( String ) p2.first() ) ;
          if ( value == 0 ) {
             return ( ( String ) p1.second() ).compareTo( ( String ) p2.second() ) ;
          }
          else{
            return value;
          }
       }
    });

    //CONSTRUCTORS
    /** Creates new InMemoryLog */
    public InMemoryEventLog() {
    }

    // METHODS
    public synchronized void add( PDU p ) {
        Long l ;
        if ( p instanceof EventPDU ) {
            EventPDU pdu = ( EventPDU ) p ;
            allEvents.put( l = new Long( pdu.getTime() ), pdu ) ;
            // Handle events
            if ( pdu instanceof AssetPDU ) {
               assetEvents.put( l, pdu ) ;
            }
            // Handle unique objects
            if ( pdu instanceof UniqueObjectPDU ) {
                UniqueObjectPDU updu = ( UniqueObjectPDU ) pdu ;
                UIDPDU sp = updu.getUID() ;
                eventsByUID.put( sp, pdu ) ;
            }
        }
        else if ( p instanceof ExecutionPDU ) {
            addPlugInEvent( (ExecutionPDU) p  ) ;
        }
    }

    protected synchronized void addPlugInEvent( ExecutionPDU pdu ) {

        // Increment the counter for total # of PlugIn events
        countPlugInEvents ++;
        // Store pdu by start time
        startPlugInEvents.put( new Long( pdu.getStartTime() ), pdu ) ;
        // Store pdu by stop time
        stopPlugInEvents.put( new Long( pdu.getStopTime() ), pdu ) ;
        // Store pdu by plugin unique identifier
        eventsByPlugIn.put( new Pair( pdu.getClusterIdentifier(), pdu.getPlugInName()+"@" +pdu.getPlugInHash() ), pdu ) ;
    }

    public void clear() {
        assetEvents.clear() ;
        allEvents.clear() ;
        eventsByUID.clear() ;

        startPlugInEvents.clear();
        stopPlugInEvents.clear();
        eventsByPlugIn.clear();
    }

    public void close() { }

    /** Returns all asset events.
     */
    public synchronized Iterator getAssetEvents() {
        return assetEvents.elements();
    }

     /** Returns all asset events between start and end.
     */
    public synchronized Iterator getAssetEvents(long start, long end) {
        return assetEvents.subMap( new Long( start ),  new Long( end ) ) ;
    }

   /** Get all events.
     */
    public synchronized Iterator getEvents() {
       return allEvents.subMap( allEvents.firstKey(), allEvents.lastKey() );
    }

    /** Get all events between a start time and end time.
     */
    public synchronized Iterator getEvents(long start, long end) {
        return allEvents.subMap( new Long( start ),  new Long( end ) )  ;
    }

    public synchronized Iterator getEvents(UID uid) {
        // Get objects by uid.
        Object[] objects = eventsByUID.getObjects( uid ) ;

        class ArrayIterator implements Iterator {
            public ArrayIterator(Object[] objects) {
                this.objects = objects;
            }

            public boolean hasNext() {
                if ( objects == null || objects.length == 0 )  {
                    return false ;
                }
                return count < objects.length ;
            }

            public Object next() {
                if ( objects == null || count >= objects.length ) {
                    throw new NoSuchElementException() ;
                }
                return objects[count++] ;
            }

            public void remove() {
                throw new UnsupportedOperationException( "Remove not supported." ) ;
            }

            int count = 0 ;
            Object[] objects ;
        }

        return new ArrayIterator( objects ) ;
    }

    public  Iterator getEvents( UID uid, long start, long end ) {
       System.out.println( "not implemented yet!" );
//       Iterator eventsInIntervalIter = getEvents( start, end );
//       EventPDU event = null;
//       Vector events = new Vector();
//       while( eventsInIntervalIter.hasNext() ){
//          event = (EventPDU)eventsInIntervalIter.next();
//          if ( event instanceof UniqueObjectPDU ) {
//             UniqueObjectPDU updu = (UniqueObjectPDU)event;
//             if( updu.getUID() == uid ){
//                events.add( event );
//             }
//          }
//       }
//       return events.iterator();
       return null;
    }
    /**
    * Returns the union of the set of all plugin executions that started and
    * the set of all plugins that completed execution in the given time interval.
    * Find number of events from HashSet.size(). Get iterator from HashSet.iterator().
    **/
    public synchronized Iterator getExecutionsActiveOnly( long start, long end ){
       // Get the set of all plugins that started and add them to the return list.
       HashSet plugins = new HashSet(1000) ;
       for ( Iterator i1 = getExecutionsStarted( start, end );i1.hasNext();) {
            plugins.add( i1.next() ) ;
       }
       // Now get the set of all plugins that stopped and add any unique plugins
       // from that set to the return set.  Note that Set properties ensure only
       // unique objects in the list, no duplicates!
       Iterator iter = stopPlugInEvents.subMap( new Long( start ),  new Long( end ) ) ;
       while ( iter.hasNext() ) {
          plugins.add(iter.next());
       }
       return plugins.iterator();
    }

    /**
    * Returns the intersection of the set of all plugin executions that started and
    * the set of all plugins that completed execution in the given time interval.
    * Find number of events from HashSet.size(). Get iterator from HashSet.iterator().
    **/
    public synchronized Iterator getExecutionsActiveSometime( long start, long end ){
        Iterator iter = startPlugInEvents.subMap( new Long( start ),  new Long( end ) ) ;
        HashSet plugins = new HashSet(1000);
        ExecutionPDU expdu = null;
        while ( iter.hasNext() ) {
           // Determine which plugins that started execution in this time frame
           // also ended execution in this time frame.  Add them to the return list.
           expdu = ( ExecutionPDU ) iter.next() ;
           if( expdu.getStopTime() <= end ){
              plugins.add(expdu);
           }
        }
        return plugins.iterator();
    }

    /**
    * Returns all plugin executions that started in the given time interval.
    **/
    public synchronized Iterator getExecutionsStarted( long start, long end ){
       return startPlugInEvents.subMap( new Long( start ),  new Long( end ) );
    }

    /**
    * Returns all plugin executions that Stopped in the given time interval.
    **/
    public synchronized Iterator getExecutionsStopped( long start, long end ){
       return stopPlugInEvents.subMap( new Long( start ),  new Long( end ) );
    }

    public synchronized long getFirstEventTime() {
        if ( allEvents.size() == 0 ) {
           return 0 ;
        }
        Long l = (Long ) allEvents.firstKey() ;
        return l.longValue() ;
    }

    public synchronized long getFirstExecutionTime() {
        if ( startPlugInEvents.size() == 0 ) {
           return 0 ;
        }
        Long l = (Long ) startPlugInEvents.firstKey() ;
        return l.longValue() ;
    }

    public synchronized long getLastEventTime() {
        if ( allEvents.size() == 0 ) {
            return 300000 ;
        }
        Long l = ( Long ) allEvents.lastKey() ;
        return l.longValue() ;
    }

    public synchronized long getLastExecutionTime() {
        if ( stopPlugInEvents.size() == 0 ) {
            return 300000 ;
        }
        Long l = ( Long ) stopPlugInEvents.lastKey() ;
        return l.longValue() ;
    }

    public synchronized int getNumAssetEvents() { return assetEvents.size(); }

    public synchronized int getNumAssetEvents(long start, long end) {
       Iterator iter = assetEvents.subMap( new Long( start ),  new Long( end ) );
        int count = 0 ;
        while ( iter.hasNext() ) {
            iter.next() ;
            count ++ ;
        }
        return count ;
    }

    public synchronized int getNumEvents() { return allEvents.size(); }

    public synchronized int getNumEvents(long start, long end) {
        Iterator iter = allEvents.subMap( new Long( start ),  new Long( end ) ) ;
        int count = 0 ;
        while ( iter.hasNext() ) {
            iter.next() ;
            count ++ ;
        }
        return count ;
    }

    /**
    * Returns the number of plugin executions that started in the given time interval.
    **/
    public synchronized int getNumExecutionsStarted( long start, long end ){
       Iterator iter = startPlugInEvents.subMap( new Long( start ),  new Long( end ) ) ;
       int count = 0 ;
       while ( iter.hasNext() ) {
          iter.next() ;
          count ++ ;
       }
       return count ;
    }

    public synchronized int getNumUIDs() {
       // bbowles - Not sure if this is just the size of eventsByUID???
        Iterator iter = allEvents.subMap( allEvents.firstKey(), allEvents.lastKey() );
        int cntUIDs = 0;
        while ( iter.hasNext() ) {
            iter.next() ;
            cntUIDs++;
        }
        return cntUIDs;
    }

    public synchronized int getNumUIDs(long start, long end) {
       // bbowles - AND WHAT ABOUT THIS ONE? 
       // Not sure if this is just the size of eventsByUID??? for a given time interval
        Iterator iter = allEvents.subMap( new Long( start ), new Long( end ) ) ;
        int cntUIDs = 0;
        while ( iter.hasNext() ) {
            iter.next() ;
            cntUIDs++;
        }
        return cntUIDs;
    }

    /**
    * Returns the number of plugin executions that stopped in the given time interval.
    **/
    public synchronized int getNumExecutionsStopped( long start, long end ){
       Iterator iter = stopPlugInEvents.subMap( new Long( start ),  new Long( end ) ) ;
       int count = 0 ;
       while ( iter.hasNext() ) {
          iter.next() ;
          count ++ ;
       }
       return count ;
    }
    
    /**
     * Returns all execution events for a given AgentName.
     */
    public synchronized Iterator getExecutionsByAgent(String theAgentName) {
       System.out.println( "not implemented yet!" );
//       Vector executions = new Vector();
//       Iterator iter = eventsByPlugIn.iterator();
//       ExecutionPDU epdu = null;
//       while( iter.hasNext() ){
//          epdu = (ExecutionPDU)iter.next();
//          if( epdu.getClusterIdentifier() == theAgentName ){
//             executions.add( epdu );
//          }
//       }
//       return executions.iterator();
       return null;
    }
    
    /** Returns all agents found in ExecutionPDUs, i.e. agents that had
     * plugin(s) that executed.
     */
    public synchronized Collection getAgents(){
       System.out.println( "not implemented yet!" );
//       Vector executions = new Vector();
//       Iterator iter = eventsByPlugIn.iterator();
//       ExecutionPDU epdu = null;
//       String agentName = null;
//       while( iter.hasNext() ){
//          epdu = (ExecutionPDU)iter.next();
//          agentName = epdu.getClusterIdentifier();
//          boolean nameIsUnique = true;
//          // test if name already exists in executions Vector
//          for( int cnt = 0; cnt < executions.size(); cnt++ ){
//             if( agentName == (String) executions(cnt).get() ){
//                nameIsUnique = false;
//             }
//          }
//          if( nameIsUnique ){
//             executions.add( agentName );
//         }
//       }
//       return executions.iterator();
       return null;
    }
}
