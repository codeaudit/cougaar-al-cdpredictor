/*
 * EventThread.java
 *
 * Created on June 18, 2001, 4:49 PM
 */

package org.hydra.server;
import java.util.* ;
import org.hydra.util.MultiTreeSet ;

class ScheduledEventThread extends Thread {
    
    public static class RunnableEvent {
        RunnableEvent( long time, Runnable r ) { this.time = time ; this.r = r ; }
        
        public boolean equals( Object o ) {
            if ( o instanceof RunnableEvent ) {
               return ( ( RunnableEvent ) o ).r == r ; 
            }
            return false ;
        }
        
        long time ;
        Runnable r ;
        public String toString() { return "[ " + time + "," + r + "]" ; }
    }
    
    public String toString() {
        StringBuffer result = new StringBuffer() ;
        result.append( "[#events=" + map.size() + "," ) ;
        return result.toString() ;
    }
    
    public synchronized long getNextEventTime() {
        if ( map.size() == 0 ) {
           return Long.MAX_VALUE ;  
        }
        
        Long result = ( Long ) map.firstKey() ;
        return result.longValue() ;
    }
        
    public synchronized Object[] getNextEvents() {
        if ( map.size() == 0 ) {
            return null ;
        }
        
        Object key =  map.firstKey() ;
        Object[] o = map.getObjects( key ) ;
        map.removeObjects( key ) ;
        return o ;
    }

    /** Schedule an event to occur at time t in the future.
     *  @param r A runnable event to be scheduled.
     *  @param time 
     */
    public synchronized void scheduleEvent( long time, Runnable r ) {
        //System.out.println( "Scheduling event at " + time + ", current Time is " + System.currentTimeMillis() ) ;

        //if ( time < System.currentTimeMillis() ) {
        //    return ;
        //}
                        
        RunnableEvent re = new RunnableEvent( time, r ) ;
        
        map.put( new Long( time ) , re ) ;

        if ( time < nextTime ) {
            // nextTime = time ;
            // System.out.println( "Interrupt." ) ;
            this.interrupt() ;   
        }        
    }
    
    //public long getTimeTillNextEvent() ;
    
    public void run() {    
       while ( !stopThread ) {
         try {
            while ( true ) {
              nextTime = getNextEventTime() ;
              if ( nextTime - System.currentTimeMillis() < 20 ) {
                 Object[] events = getNextEvents() ;
                 for (int i=0;i<events.length;i++) {
                    Thread t = new Thread( (( RunnableEvent ) events[i] ).r ) ;
                    t.start() ;       
                 }
              }
              else {
                 break ;
              }
            }
            
            //System.out.println( "Sleeping for " + ( ( nextTime - System.currentTimeMillis() ) / 1000f )+ " sec"  ) ;
            sleep( nextTime - System.currentTimeMillis() ) ;
            //System.out.println( "Waking up..." ) ;
         }
         catch ( InterruptedException e ) {
         }
       }
    }
    
    MultiTreeSet map = new MultiTreeSet() ;
    boolean stopThread = false ;
    long nextTime ;
    
    public static final void main(String[] args) {
        ScheduledEventThread set = new ScheduledEventThread() ;
        set.start() ;
        
        long time ;
        set.scheduleEvent( time = System.currentTimeMillis() + 10000, new Runnable() {
            public void run() { 
                System.out.println( "Hello World: " + System.currentTimeMillis() ) ;   
            }
        } ) ;
        
        set.scheduleEvent( time , new Runnable() {
            public void run() { 
                System.out.println( "Hello World Again! " + System.currentTimeMillis() ) ;   
            }
        } ) ;
       

        set.scheduleEvent( System.currentTimeMillis() + 5000, new Runnable() {
            public void run() { 
                System.out.println( "Hello World: " + System.currentTimeMillis() ) ;   
            }
        } ) ;

        set.scheduleEvent( System.currentTimeMillis() + 20000, new Runnable() {
            public void run() { 
                System.out.println( "Hello World: " + System.currentTimeMillis() ) ;   
            }
        } ) ;
        
        //for ( Iterator iter = set.map.values().iterator(); iter.hasNext(); ) {
        //   RunnableEvent revent = ( RunnableEvent ) iter.next() ;
        //   System.out.println( "Event " + revent.time ) ;
        //}
        
        try {
        Thread.sleep( 40000 ) ;
        }
        catch ( Exception e ) {
        }
    }
}

