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

package org.cougaar.tools.castellan.server.plugin;

import org.cougaar.core.plugin.*;
import org.cougaar.core.blackboard.*;
import org.cougaar.core.service.*;
import org.cougaar.core.agent.service.alarm.*;
import org.cougaar.util.*;
import org.cougaar.tools.castellan.planlog.*;
import org.cougaar.tools.castellan.pdu.*;
import com.axiom.lib.util.* ;
import com.axiom.lib.mat.* ;

import java.util.*;

public class PlanLogServerStatisticsPlugin extends ComponentPlugin
{
    /**
     * Hack to get decent stats out.
     */
    class DisplayThread extends Thread {
        public void run()
        {
            System.out.println("Opening MatEng...");
            eng = MatEng.getInstance() ;
            eng.open( null ) ;
            System.out.println("Done.") ;

            while ( true ) {
                try {
                Thread.sleep( Long.MAX_VALUE );
                }
                catch ( InterruptedException e ) {
                    updateAnalysis();
                }
            }
        }
    }

    class TriggerMeasurementAlarm implements PeriodicAlarm {
        public TriggerMeasurementAlarm( long expTime )
        {
            this.expTime = expTime;
        }

        public void reset( long currentTime )
        {
            expTime = currentTime + delay ;
            expired = false ;
        }

        public long getExpirationTime()
        {
            return expTime ;
        }

        public void expire()
        {
            expired = true ;

            // Print out statistics...
            if ( myEventLog != null ) {
                System.out.println( "PlanLogStatistics:: Start time " + EventPDU.formatTime( myEventLog.getFirstEventTime() ) + ",end time " +
                    EventPDU.formatTime( myEventLog.getLastEventTime() ) + ",# events = " + myEventLog.getNumEvents() );

                if ( displayThread != null ) {
                    displayThread.interrupt();
                }
            }

        }

        public boolean hasExpired()
        {
            return expired ;
        }

        public boolean cancel()
        {
            boolean was = expired;
            expired=true;
            return was;
        }

        boolean expired = false ;
        long expTime ;
    }

    MatEng eng ;
    protected void setupSubscriptions()
    {
        BlackboardService bs = getBlackboardService() ;
        subscription = (IncrementalSubscription ) bs.subscribe( new UnaryPredicate() {
            public boolean execute( Object o )
            {
                if ( o instanceof EventLog ) {
                    return true ;
                }
                return false ;
            }
        } ) ;
    }

    /**
     *
     */
    protected void findBatchSizes() {
        // For each execution trace,
        Iterator iter= myEventLog.getExecutionsActiveSometime( myEventLog.getFirstEventTime(), myEventLog.getLastEventTime() ) ;

        // Find batch sizes for specific agent?
        while ( iter.hasNext() ) {
            ExecutionPDU pdu = ( ExecutionPDU ) iter.next() ;

            // Are there any overlapping Executions on this same agent? If there are, aggregate them into one.
            Iterator oiter = myEventLog.getExecutionsActiveSometime( pdu.getStartTime(), pdu.getStopTime() ) ;
            ArrayList agentOverlaps = new ArrayList() ;
            while ( oiter.hasNext() ) {
                ExecutionPDU opdu = ( ExecutionPDU ) oiter.next() ;
                if ( opdu.getSource().equals( pdu.getSource() ) ) {
                    // Check for actual overlap.
                    if ( opdu.getStopTime() > pdu.getStartTime() && opdu.getStartTime() < pdu.getStopTime() )
                    {
                        agentOverlaps.add( opdu ) ;
                    }
                }
            }
            System.out.println("Warning: " + agentOverlaps.size() + " found!");

            // Now, check for the batch size.  This assumes that events are published within the transaction
            // and that they are tagged with an id.
            Iterator c2 = myEventLog.getEvents( pdu.getStartTime(), pdu.getStopTime() ) ;
            int count = 0  ;
            while ( c2.hasNext() ) {
                PDU updu = ( PDU ) c2.next() ;
                if ( updu instanceof UniqueObjectPDU ) {
                     // This is something from the same cluster. Process it.
                     if ( updu.getSource().equals( pdu.getSource() ) ) {
                          count++ ;
                     }
                }
            }
            if ( count > 0 ) {

            }
        }
    }

    /**
     * Update current analysis of the event log and plot them. Note that this is observed primarily.
     */
    protected void updateAnalysis() {
        BlackboardService bs = getBlackboardService() ;

        // Iterate over the PlanExecutionLog and sort by execution length
        long maxElapsedTime = 0 ;
        int powersOfTen = 12 ;
        float[] logArray = new float[ powersOfTen ] ;
        float[] timeArray = new float[ powersOfTen ] ;
        float[] indexArray = new float[ powersOfTen ] ;

        // Fill in the index array
        for (int i=0;i<indexArray.length;i++) {
            indexArray[i] = ( float ) Math.pow( 10, (( double ) i ) / 2.0 ) ;
        }

        double totalElapsedTime = 0 ;
        double totalElapsedTime2 = 0 ;
        double count = 0;
        for ( Iterator iter = myEventLog.getExecutionsActiveSometime( myEventLog.getFirstEventTime(), myEventLog.getLastEventTime() ) ;
              iter.hasNext();)
        {
            ExecutionPDU pdu = ( ExecutionPDU ) iter.next() ;
            long elapsedTime = pdu.getElapsedTime() ;
            if ( elapsedTime > maxElapsedTime ) {
                maxElapsedTime = elapsedTime ;
            }
            totalElapsedTime += elapsedTime ;
            totalElapsedTime2 += ( ( double ) elapsedTime * ( double ) elapsedTime ) ;
            if ( elapsedTime != 0 ) {
                int index = ( int ) Math.round( Math.log( elapsedTime ) / ( Math.log( 10 ) / 2.0 ) ) ;
                logArray[index]++ ;
                timeArray[index] += elapsedTime ;
            }
            else {
                logArray[0]++ ;
                timeArray[0] += 1 ;
            }
            count++ ;
        }

        if ( count == 0 ) {
            return ;
        }

        FloatMatrix indexMatrix = new FloatMatrix( indexArray.length, 1 ) ;
        for (int i=0;i<indexArray.length;i++) {
           indexMatrix.set( i, 0, indexArray[i] );
        }
        eng.putArray( "indices", indexMatrix );

        System.out.println("PlanLogStatistics: Execution cycles = " + count +
                ", total elapsed time " + totalElapsedTime + " ms, max= " + maxElapsedTime  + " ms , avg=" +
                totalElapsedTime / count  );
        FloatMatrix fm = new FloatMatrix( logArray.length, 1 ) ;
        for (int i=0;i<logArray.length;i++) {
            if ( logArray[i] == 0 ) {
                fm.set( i, 0, 1 ) ;
            }
            else {
                fm.set( i, 0, logArray[i] );
            }
        }
        eng.putArray( "logExecutionTime", fm );

        //
        FloatMatrix fm2 = new FloatMatrix( timeArray.length, 1 ) ;
        for (int i=0;i<timeArray.length;i++) {
           fm2.set( i, 0, timeArray[i] );
        }
        eng.putArray( "accumulatedTime", fm2 );

        StringBuffer command = new StringBuffer() ;

        // Semilog plot of execution length vs. number of executions.
        command.append( "subplot(2,1,1); loglog ( indices, logExecutionTime ); title( 'Execution length vs. # executions' );" ) ;
        command.append( "xlabel( 'Execution length(ms)' ); ylabel( '# executions' );" ) ;

        // Loglog plot execution length against total distributed time.
        command.append( "subplot(2,1,2); semilogx( indices, accumulatedTime ); title( 'Execution length vs. Total time' ); " ) ;
        command.append( "xlabel( 'Execution length(ms)' ) ; ylabel( 'Accum. time' )" ) ;

        eng.evalString( command.toString() );
    }

    protected void execute()
    {
        if ( myEventLog == null ) {
            Enumeration e = subscription.getAddedList() ;
            while ( e.hasMoreElements() ) {
                myEventLog = ( InMemoryEventLog ) e.nextElement() ;
            }
            if ( myEventLog == null ) {
                return ;
            }

            System.out.println( "PlanLogServerStatisticsPlugin:: Found new event log. Starting monitoring service..."  );

            // Start the display thread. Note that the MatEng object instance must be created and opened in the
            // thread in which it will be operated on.
            displayThread = new DisplayThread() ;
            displayThread.start() ;

            // Start the alarm service
            AlarmService as = getAlarmService() ;
            as.addAlarm( new TriggerMeasurementAlarm( currentTimeMillis() + delay ) ) ;
        }

    }

    /**
     * Delay in ms.
     */
    long delay = 5000L ;
    DisplayThread displayThread ;
    IncrementalSubscription subscription ;
    transient InMemoryEventLog myEventLog ;
}
