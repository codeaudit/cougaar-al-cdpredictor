/*
  * <copyright>
  *  Copyright 2001 (Intelligent Automation, Inc.)
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
  * CHANGE RECORD
  */

package org.cougaar.tools.castellan.tests;

import org.cougaar.core.plugin.*;
import org.cougaar.core.service.*;
import org.cougaar.core.blackboard.*;
import org.cougaar.core.util.*;
import org.cougaar.util.*;
import org.cougaar.planning.ldm.plan.*;
import org.cougaar.planning.ldm.asset.*;
import org.cougaar.tools.castellan.plugin.*;
import org.cougaar.tools.castellan.pdu.*;
import org.cougaar.tools.castellan.planlog.*;

import java.util.*;
import java.io.*;

public class SerializationPerformanceTest extends ComponentPlugin
{
    public void setupSubscriptions()
    {
        try {
        oos = new ObjectOutputStream( bos ) ;
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }
            BlackboardService bs = getBlackboardService() ;
        subscription = ( IncrementalSubscription ) bs.subscribe( new UnaryPredicate() {
            public boolean execute( Object o )
            {
                if ( o instanceof Task || o instanceof PlanElement ) {
                    return true ;
                }
                return false ;
            }
        }) ;
    }

    protected AllocationResult replicate( AllocationResult ar )
    {
        if ( ar == null ) return null;
        return ( AllocationResult ) ar.clone();
    }

    /**
     * Compares allocation results and returns new AR if the newResult has changed.
     */
    protected AllocationResult compareAndReplicate( UID planElement,
                                                    short arType, AllocationResult old, AllocationResult newResult,
                                                    ArrayList message )
    {
        if ( old == null && newResult == null )
        {
            return null;
        }
        if ( old == null && newResult != null )
        {
            message.add( PlanToPDUTranslator.makeAllocationResultMessage( getCurrentExecutionTime(), getCurrentTime(), planElement, newResult, arType, EventPDU.ACTION_ADD ) );
            return ( AllocationResult ) newResult.clone();

        }
        if ( old != null && newResult == null )
        {
            message.add( PlanToPDUTranslator.makeAllocationResultMessage( getCurrentExecutionTime(), getCurrentTime(), planElement, old, arType, EventPDU.ACTION_REMOVE ) );
            return null;  // Should publish an AllocationResultPDU with a REMOVE flag
        }
        if ( !old.isEqual( newResult ) )
        {
            AllocationResult result = ( AllocationResult ) newResult.clone();
            message.add( PlanToPDUTranslator.makeAllocationResultMessage( getCurrentExecutionTime(), getCurrentTime(), planElement, newResult, arType, EventPDU.ACTION_CHANGE ) );
            return result;
        }
        return null;
    }

    /**
     * Fast CRC32 comparison between two allocation results based only on confidence and success.
     */
    long compareAR( UID planElement, short arType, long value, AllocationResult newResult, ArrayList message )
    {
        long newValue = PlanToPDUTranslator.computeFastCRC32( newResult );
        if ( newValue == value )
        {
            return value;
        }
        if ( newValue != 0 && value == 0 )
        {
            message.add( PlanToPDUTranslator.makeAllocationResultMessage( getCurrentExecutionTime(), getCurrentTime(), planElement, newResult, arType, EventPDU.ACTION_ADD ) );
            return newValue;
        }
        if ( newValue == 0 && value != 0 )
        {
            // make a remove message
            //message.add( PlanToPDUTranslator.makeAllocationResultMessage( this, planElement, old, arType, EventPDU.ACTION_REMOVE ) ) ;
            return newValue;  // Should publish an AllocationResultPDU with a REMOVE flag
        }
        AllocationResult result = ( AllocationResult ) newResult.clone();
        message.add( PlanToPDUTranslator.makeAllocationResultMessage( getCurrentExecutionTime(), getCurrentTime(), planElement, newResult, arType, EventPDU.ACTION_CHANGE ) );

        return newValue;
    }

    protected int getActionForChange( int start, int end ) {
        if ( start == -1 ) {
            return EventPDU.ACTION_ADD ;
        }
        else if ( end == -1 ) {
            return EventPDU.ACTION_REMOVE ;
        }
        else
        {
            return EventPDU.ACTION_CHANGE ;
        }
    }

    /**
     * Compare current set of allocation results against previous versions based only on
     * success/failure/null values.
     */
    protected void checkARSuccess( Allocation a ) {
        int[] tuple = ( int[] ) allocationToARMap.get( a.getUID() );

        // Compute
        if ( tuple == null ) {
            if ( a.getEstimatedResult() == null && a.getReceivedResult() == null &&
                    a.getReportedResult() == null && a.getObservedResult() == null )
            {
                return;
            }
            tuple = new int[4];
            AllocationResult estimated = a.getEstimatedResult(), received = a.getReceivedResult(),
                    reported = a.getReportedResult(), observed = a.getObservedResult();
            tuple[0] = ( estimated == null ) ? -1 : ( estimated.isSuccess() ? 1 : 0 ) ;
            tuple[1] = ( received == null ) ? -1 : ( received.isSuccess() ? 1 : 0 ) ;
            tuple[2] = ( reported == null ) ? -1 : ( reported.isSuccess() ? 1 : 0 ) ;
            tuple[3] = ( observed == null ) ? -1 : ( observed.isSuccess() ? 1 : 0 ) ;
            allocationToARMap.put( a.getUID(), tuple );
            if ( tuple[0] != -1 )
            {
                AllocationResultPDU pdu = PlanToPDUTranslator.makeAllocationResultMessage( getCurrentExecutionTime(), getCurrentTime(), a.getUID(), estimated,
                        AllocationResultPDU.ESTIMATED, EventPDU.ACTION_ADD );
                sendMessage( pdu );
            }
            if ( tuple[1] != -1 )
            {
                AllocationResultPDU pdu = PlanToPDUTranslator.makeAllocationResultMessage( getCurrentExecutionTime(), getCurrentTime(), a.getUID(), received,
                        AllocationResultPDU.RECEIVED, EventPDU.ACTION_ADD );
                sendMessage( pdu );
            }
            if ( tuple[2] != -1 )
            {
                AllocationResultPDU pdu = PlanToPDUTranslator.makeAllocationResultMessage( getCurrentExecutionTime(), getCurrentTime(), a.getUID(), reported,
                        AllocationResultPDU.REPORTED, EventPDU.ACTION_ADD );
                sendMessage( pdu );
            }
            if ( tuple[3] != -1 )
            {
                AllocationResultPDU pdu = PlanToPDUTranslator.makeAllocationResultMessage( getCurrentExecutionTime(), getCurrentTime(), a.getUID(), observed,
                        AllocationResultPDU.OBSERVED, EventPDU.ACTION_ADD );
                sendMessage( pdu );
            }
        }
        else {
            // Compare success/failure/non-existence of AR
            AllocationResult estimated = a.getEstimatedResult(), received = a.getReceivedResult(),
                    reported = a.getReportedResult(), observed = a.getObservedResult();
            int t1 = ( estimated == null ) ? -1 : ( estimated.isSuccess() ? 1 : 0 ) ;
            int t2 = ( received == null ) ? -1 : ( received.isSuccess() ? 1 : 0 ) ;
            int t3 = ( reported == null ) ? -1 : ( reported.isSuccess() ? 1 : 0 ) ;
            int t4 = ( observed == null ) ? -1 : ( observed.isSuccess() ? 1 : 0 ) ;
            if ( t1 != tuple[0] ) {
                AllocationResultPDU pdu = PlanToPDUTranslator.makeAllocationResultMessage( getCurrentExecutionTime(), getCurrentTime(), a.getUID(), estimated,
                        AllocationResultPDU.ESTIMATED, getActionForChange(tuple[0], t1) );
                sendMessage( pdu );
            }
            if ( t2 != tuple[1] ) {
                AllocationResultPDU pdu = PlanToPDUTranslator.makeAllocationResultMessage( getCurrentExecutionTime(), getCurrentTime(), a.getUID(), received,
                        AllocationResultPDU.RECEIVED, getActionForChange(tuple[1], t2) );
                sendMessage( pdu );
            }
            if ( t3 != tuple[2] ) {
                AllocationResultPDU pdu = PlanToPDUTranslator.makeAllocationResultMessage( getCurrentExecutionTime(), getCurrentTime(), a.getUID(), reported,
                        AllocationResultPDU.REPORTED, getActionForChange(tuple[2], t3) );
                sendMessage( pdu );
            }
            if ( t4 != tuple[3] ) {
                AllocationResultPDU pdu = PlanToPDUTranslator.makeAllocationResultMessage( getCurrentExecutionTime(), getCurrentTime(), a.getUID(), observed,
                        AllocationResultPDU.OBSERVED, getActionForChange(tuple[3], t4) );
                sendMessage( pdu );
            }
        }
    }

    /** Changed to use CRC32 to compare allocation results.
     *  This is most likely too slow and will require significant optimization.
     */
    protected void checkARChanged( Allocation a )
    {
        long[] tuple = ( long[] ) allocationToARMap.get( a.getUID() );
        if ( tuple == null )
        {
            if ( a.getEstimatedResult() == null && a.getReceivedResult() == null &&
                    a.getReportedResult() == null && a.getObservedResult() == null )
            {
                return;
            }
            tuple = new long[4];
            AllocationResult estimated = a.getEstimatedResult(), received = a.getReceivedResult(),
                    reported = a.getReportedResult(), observed = a.getObservedResult();
            tuple[0] = PlanToPDUTranslator.computeFastCRC32( estimated );
            tuple[1] = PlanToPDUTranslator.computeFastCRC32( received );
            tuple[2] = PlanToPDUTranslator.computeFastCRC32( reported );
            tuple[3] = PlanToPDUTranslator.computeFastCRC32( observed );
            allocationToARMap.put( a.getUID(), tuple );
            if ( tuple[0] != 0 )
            {
                AllocationResultPDU pdu = PlanToPDUTranslator.makeAllocationResultMessage( getCurrentExecutionTime(), getCurrentTime(), a.getUID(), estimated,
                        AllocationResultPDU.ESTIMATED, EventPDU.ACTION_ADD );
                sendMessage( pdu );
            }
            if ( tuple[1] != 0 )
            {
                AllocationResultPDU pdu = PlanToPDUTranslator.makeAllocationResultMessage( getCurrentExecutionTime(), getCurrentTime(), a.getUID(), received,
                        AllocationResultPDU.RECEIVED, EventPDU.ACTION_ADD );
                sendMessage( pdu );
            }
            if ( tuple[2] != 0 )
            {
                AllocationResultPDU pdu = PlanToPDUTranslator.makeAllocationResultMessage( getCurrentExecutionTime(), getCurrentTime(), a.getUID(), reported,
                        AllocationResultPDU.REPORTED, EventPDU.ACTION_ADD );
                sendMessage( pdu );
            }
            if ( tuple[3] != 0 )
            {
                AllocationResultPDU pdu = PlanToPDUTranslator.makeAllocationResultMessage( getCurrentExecutionTime(), getCurrentTime(), a.getUID(), observed,
                        AllocationResultPDU.OBSERVED, EventPDU.ACTION_ADD );
                sendMessage( pdu );
            }
        } else
        {
            messages.clear();
            tuple[0] = compareAR( a.getUID(), AllocationResultPDU.ESTIMATED, tuple[0], a.getEstimatedResult(), messages );
            tuple[1] = compareAR( a.getUID(), AllocationResultPDU.RECEIVED, tuple[1], a.getReceivedResult(), messages );
            tuple[2] = compareAR( a.getUID(), AllocationResultPDU.REPORTED, tuple[2], a.getReportedResult(), messages );
            tuple[3] = compareAR( a.getUID(), AllocationResultPDU.OBSERVED, tuple[3], a.getObservedResult(), messages );
            for ( int i = 0; i < messages.size(); i++ )
            {
                PDU pdu = ( PDU ) messages.get( i );
                sendMessage( pdu );
            }
        }
    }


    /**
     * This is a (too) slow but comprehensive measure of allocation results.
     */
    protected void checkAllocationResultChanged( Allocation a )
    {
        ARTuple tuple = ( ARTuple ) allocationToARMap.get( a.getUID() );
        if ( tuple == null )
        {
            if ( a.getEstimatedResult() == null && a.getReceivedResult() == null &&
                    a.getReportedResult() == null && a.getObservedResult() == null )
            {
                return;
            }

            tuple = new ARTuple();
            tuple.estimated = replicate( a.getEstimatedResult() );
            tuple.received = replicate( a.getReceivedResult() );
            tuple.reported = replicate( a.getReportedResult() );
            tuple.observed = replicate( a.getObservedResult() );
            allocationToARMap.put( a.getUID(), tuple );
            if ( tuple.estimated != null )
            {
                AllocationResultPDU pdu = PlanToPDUTranslator.makeAllocationResultMessage( getCurrentExecutionTime(), getCurrentTime(), a.getUID(), tuple.estimated,
                        AllocationResultPDU.ESTIMATED, EventPDU.ACTION_ADD );
                sendMessage( pdu );
            }
            if ( tuple.received != null )
            {
                AllocationResultPDU pdu = PlanToPDUTranslator.makeAllocationResultMessage( getCurrentExecutionTime(), getCurrentTime(), a.getUID(), tuple.received,
                        AllocationResultPDU.RECEIVED, EventPDU.ACTION_ADD );
                sendMessage( pdu );
            }
            if ( tuple.reported != null )
            {
                AllocationResultPDU pdu = PlanToPDUTranslator.makeAllocationResultMessage( getCurrentExecutionTime(), getCurrentTime(), a.getUID(), tuple.reported,
                        AllocationResultPDU.REPORTED, EventPDU.ACTION_ADD );
                sendMessage( pdu );
            }
            if ( tuple.observed != null )
            {
                AllocationResultPDU pdu = PlanToPDUTranslator.makeAllocationResultMessage( getCurrentExecutionTime(), getCurrentTime(), a.getUID(), tuple.observed,
                        AllocationResultPDU.OBSERVED, EventPDU.ACTION_ADD );
                sendMessage( pdu );
            }
        } else
        { // Compare the existing against the old
            messages.clear();
            tuple.estimated = compareAndReplicate( a.getUID(), AllocationResultPDU.ESTIMATED, tuple.estimated, a.getEstimatedResult(), messages );
            tuple.received = compareAndReplicate( a.getUID(), AllocationResultPDU.RECEIVED, tuple.received, a.getReceivedResult(), messages );
            tuple.reported = compareAndReplicate( a.getUID(), AllocationResultPDU.REPORTED, tuple.estimated, a.getReportedResult(), messages );
            tuple.observed = compareAndReplicate( a.getUID(), AllocationResultPDU.OBSERVED, tuple.estimated, a.getObservedResult(), messages );
            for ( int i = 0; i < messages.size(); i++ )
            {
                PDU pdu = ( PDU ) messages.get( i );
                sendMessage( pdu );
            }
        }
    }

    protected void processChangedObject( Object o )
    {

        int action = EventPDU.ACTION_CHANGE;
        // System.out.println( "Processing " + o ) ;
        PDU pdu = null;
        if ( o instanceof MPTask )
        {
            pdu = PlanToPDUTranslator.makeMPTaskPDU( getCurrentExecutionTime(), getCurrentTime(), ( MPTask ) o, action );
        } else if ( o instanceof Task )
        {
            pdu = PlanToPDUTranslator.makeTaskMessage( getCurrentExecutionTime(), getCurrentTime(), ( Task ) o, action );
        } else if ( o instanceof Asset )
        {
            pdu = PlanToPDUTranslator.makeAssetMessage( getCurrentExecutionTime(), getCurrentTime(), ( Asset ) o, action );
        } else if ( o instanceof Allocation )
        {
            // Check to (specifically) see if the AR has changed.  If so, make
            // an AllocationResultPDU
            Allocation al = ( Allocation ) o;
            PDU temp = PlanToPDUTranslator.makeAllocationMessage( getCurrentExecutionTime(), getCurrentTime(), al, action );
            sendMessage( temp );
            if ( logAllocationResultsLevel == PlanLogConstants.AR_LOG_LEVEL_NONE ) {
                // Do nothing by default
            }
            else if ( logAllocationResultsLevel == PlanLogConstants.AR_LOG_LEVEL_FULL )
            {
                checkAllocationResultChanged( al );
            }
            else if ( logAllocationResultsLevel == PlanLogConstants.AR_LOG_LEVEL_SUCCESS )
            {
                checkARSuccess( al );
            }
            //checkAllocationResultChanged( al ) ;
        } else if ( o instanceof Aggregation )
        {
            pdu = PlanToPDUTranslator.makeAggregationPDU( getCurrentExecutionTime(), getCurrentTime(), ( AggregationImpl ) o, action );
        } else if ( o instanceof Expansion )
        {
            pdu = PlanToPDUTranslator.makeExpansionPDU( getCurrentExecutionTime(), getCurrentTime(), ( Expansion ) o, action );
        }
        if ( pdu == null )
        {
            return;
        }
        sendMessage( pdu );
    }

    private long getCurrentExecutionTime() {
        return 0 ;
    }

    private long getCurrentTime() {
        return 0 ;
    }

    private void sendMessage( PDU pdu ) {
        try {
            messageCount ++ ;
            oos.writeObject( pdu );

        if ( bos.size() > 11000 ) {
            oos.close() ;
            byteCount += bos.size() ;
            bos.reset();
            oos = new ObjectOutputStream( bos ) ;
        }
        }catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    public void processObject( Object o, int action )
    {
        // System.out.println( "Processing " + o ) ;
        try
        {
            PDU pdu = null;
            if ( o instanceof MPTask )
            {
                pdu = PlanToPDUTranslator.makeMPTaskPDU( getCurrentExecutionTime(), getCurrentTime(), ( MPTask ) o, action );
            } else if ( o instanceof Task )
            {
                pdu = PlanToPDUTranslator.makeTaskMessage( getCurrentExecutionTime(), getCurrentTime(), ( Task ) o, action );
            } else if ( o instanceof Asset )
            {
                pdu = PlanToPDUTranslator.makeAssetMessage( getCurrentExecutionTime(), getCurrentTime(), ( Asset ) o, action );
            } else if ( o instanceof Allocation )
            {
                // Check to (specifically) see if the AR has changed.  If so, make
                // an AllocationResultPDU
                Allocation al = ( Allocation ) o;
                PDU temp = PlanToPDUTranslator.makeAllocationMessage( getCurrentExecutionTime(), getCurrentTime(), al, action );
                sendMessage( temp );
                if ( logAllocationResultsLevel == PlanLogConstants.AR_LOG_LEVEL_NONE ) {
                    // Do nothing by default
                }
                else if ( logAllocationResultsLevel == PlanLogConstants.AR_LOG_LEVEL_FULL )
                {
                    checkAllocationResultChanged( al );
                }
                else if ( logAllocationResultsLevel == PlanLogConstants.AR_LOG_LEVEL_SUCCESS )
                {
                    checkARSuccess( al );
                }
            } else if ( o instanceof Aggregation )
            {
                pdu = PlanToPDUTranslator.makeAggregationPDU( getCurrentExecutionTime(), getCurrentTime(), ( AggregationImpl ) o, action );
            } else if ( o instanceof Expansion )
            {
                pdu = PlanToPDUTranslator.makeExpansionPDU( getCurrentExecutionTime(), getCurrentTime(), ( Expansion ) o, action );
            }
            if ( pdu == null )
            {
                return;
            }
            sendMessage( pdu );
        } catch ( Exception e )
        {
            e.printStackTrace();
        }
    }


    public void execute()
    {
        // Just loop an zillion times, serializing messages into a byte buffer.
        System.out.println( "Starting test..." );
        long startTime = System.currentTimeMillis() ;
        for (int i=0;i<10000;i++) {
            int tc = 0 ;
            Enumeration e = subscription.getAddedList() ;
            while ( e.hasMoreElements() ) {
                Object o = e.nextElement() ;
                processObject( o, UniqueObjectPDU.ACTION_ADD );
                tc ++ ;
            }
            e = subscription.getRemovedList() ;
            while ( e.hasMoreElements() ) {
                Object o = e.nextElement() ;
                processObject( o, UniqueObjectPDU.ACTION_REMOVE ) ;
                tc ++ ;
            }
            e = subscription.getChangedList() ;
            while ( e.hasMoreElements() ) {
                Object o = e.nextElement() ;
                processChangedObject( o );
                tc ++ ;
            }
            if ( tc == 0 ) {
                break ;
            }
        }
        long endTime = System.currentTimeMillis() ;
        totalTime += ( endTime - startTime ) ;
        System.out.println( "Total elapsed time(sec)=  " + Math.round( ( ( double ) totalTime ) / 1000d ) ) ;
        System.out.println( "Bytes= " + byteCount );
        System.out.println( "# Messages= " + messageCount );
        System.out.println( "Average size/ msg = " + Math.round( ( float ) byteCount / ( float )messageCount ) );
    }

    long totalTime = 0 ;
    long messageCount = 0 ;
    long byteCount = 0 ;
    ByteArrayOutputStream bos = new ByteArrayOutputStream( 11000 ) ;
    ObjectOutputStream oos ;
    long logAllocationResultsLevel = PlanLogConstants.AR_LOG_LEVEL_SUCCESS ;
    ArrayList messages = new ArrayList() ;
    HashMap allocationToARMap = new HashMap() ;
    IncrementalSubscription subscription ;
}
