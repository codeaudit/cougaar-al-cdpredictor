package org.cougaar.tools.castellan.plugin;

import java.util.*;
import java.net.*;

import org.cougaar.tools.castellan.pdu.*;

import org.cougaar.util.*;
import org.cougaar.core.agent.*;
import org.cougaar.planning.ldm.plan.*;
import org.cougaar.planning.ldm.asset.*;
import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.core.blackboard.*;
import org.cougaar.core.util.*;
import org.cougaar.core.plugin.*;
import org.cougaar.core.component.*;
import org.cougaar.glm.ldm.asset.OrganizationPG;
import org.cougaar.planning.ldm.plan.*;
import org.cougaar.core.service.*;

import java.io.*;

import org.cougaar.tools.castellan.util.*;

/**
 *  PlanMonitorPlugIn sends "out-of-band" messages to a central data collector.
 * <p> Only one of these plugins should be created for each cluster.
 */
public class PlanMonitorPlugIn extends org.cougaar.core.plugin.ComponentPlugin
{

    /**
     * Disable logging for allocation results.
     */
    public static final int AR_LOG_LEVEL_NONE = 0;

    public static final int AR_LOG_LEVEL_SUCCESS = 1;

    public static final int AR_LOG_LEVEL_FULL = 2;

    public static final int AR_LOG_LEVEL_FAST = 3;

    /** Subscribe to all tasks, assets, and allocations. */
    UnaryPredicate allPredicate = new UnaryPredicate()
    {
        public boolean execute( Object o )
        {
            return ( o instanceof PlanElement || o instanceof Task || o instanceof Asset );
        }
    };

    Collection queryLocal( UnaryPredicate up )
    {
        return blackboardService.query( up );
    }

    IncrementalSubscription allElements;

    /** The level of detail to which allocation results will be logged.
     */
    public int logAllocationResultsLevel()
    {
        return logAllocationResultsLevel;
    }

    public ClusterIdentifier getIdentifier()
    {
        return getClusterIdentifier();
    }

    public PlanMonitorPlugIn()
    {
    }

    public LoggingService getLoggingService()
    {
        return log;
    }

    public void setupSubscriptions()
    {
        try {
            ServiceBroker broker = getBindingSite().getServiceBroker();

            // Set up the logging service
            if ( broker != null )
            {
                log = ( LoggingService )
                        broker.getService( this, LoggingService.class, null );
            }

            if ( log != null && log.isDebugEnabled() )
            {
                log.debug( "Setting up subscriptions for PlanMonitorPlugIn" );
            }

            // Use agent based message transport for now
            ConfigFinder finder = getConfigFinder();
            clientMessageTransport = new BlackboardClientMTImpl( this, finder );
            //clientMessageTransport = new SocketClientMTImpl( getConfigFinder(),getAgentIdentifier(),this) ;
            clientMessageTransport.connect();

            System.out.println( "PlanMonitorPlugIn:: Logging for " + getIdentifier() );
            if ( clientMessageTransport.isConnected() )
            {
                System.out.println( "Subscribing to blackboard for cluster " + getIdentifier() );
                subscribe();
            }


            if ( log != null && log.isDebugEnabled() )
            {
                log.debug( "Done setting up subscriptions." );
            }
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }
        finally {
        }
        System.out.println( "Done setting up subscriptions." );

        //uiFrame = new PlanMonitorPlugInUIFrame(this) ;
        //uiFrame.setVisible( true ) ;
    }

    public BlackboardService getBlackboardService() {
        return super.getBlackboardService() ;
    }

    private void subscribe()
    {
        allElements = ( IncrementalSubscription ) getBlackboardService().subscribe( allPredicate, true );
        // allElements = ( IncrementalSubscription ) subscribe( allPredicate ) ;
    }

    synchronized void unsubscribe()
    {
        if ( allElements != null )
        {
            blackboardService.unsubscribe( allElements );
            allElements = null;
        }
    }

    public void log( String s )
    {
        // Use log4j here to do any message logging.
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
     * Compares CRC32 signatures of the current allocation result and the new allocation result.  If they don't
     * match, return the new CRC32 result or zero otherwise.
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
            // make a remove message.  The problem is we don't have the AR slot.
            message.add( PlanToPDUTranslator.makeAllocationResultMessage( getCurrentExecutionTime(), getCurrentTime(),
                    planElement, null, arType, EventPDU.ACTION_REMOVE ) ) ;
            return newValue;  // Should publish an AllocationResultPDU with a REMOVE flag also
        }
        AllocationResult result = ( AllocationResult ) newResult.clone();
        message.add( PlanToPDUTranslator.makeAllocationResultMessage( getCurrentExecutionTime(), getCurrentTime(), planElement, newResult, arType, EventPDU.ACTION_CHANGE ) );

        return newValue;
    }

    /** Changed to use CRC32 to compare different allocation results.
     *  This is most likely too slow and will require significant optimization.
     */
    protected void checkARChanged( Allocation a )
    {
        long[] tuple = ( long[] ) allocationToARMap.get( a.getUID() );

        // Case zero.  No existing allocation result has been seen before
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
        } else  // Compare allocation results
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

    /** Notifies the plugin that its message transport is no longer delivering messages.
     */
    void notifyDisconnected( ClientMessageTransport cmt )
    {
        unsubscribe();
    }

    ArrayList messages = new ArrayList( 4 );

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
            checkARChanged( al );
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

    protected void processObject( Object o, int action )
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
                if ( logAllocationResultsLevel == AR_LOG_LEVEL_FULL )
                {
                    checkAllocationResultChanged( al );
                } else if ( logAllocationResultsLevel == AR_LOG_LEVEL_FAST )
                {
                    checkARChanged( al );
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

    void sendMessage( PDU pdu )
    {
        // System.out.println( "Sending : " + pdu ) ;
        if ( pdu != null && clientMessageTransport != null )
        {
            clientMessageTransport.sendMessage( pdu );
        }
    }

    public long getCurrentTime()
    {
        return currentTime;
    }

    public long getCurrentExecutionTime()
    {
        return currentExecutionTime;
    }

    public synchronized void execute()
    {

        try {
            if ( allElements == null )
            {
                return;
            }

            if ( clientMessageTransport != null )
            {
                clientMessageTransport.execute();
            }

            long startTime = System.currentTimeMillis();
            currentTime = System.currentTimeMillis();
            currentExecutionTime = currentTimeMillis();

            String cluster = getClusterIdentifier().toString();
            // Process added/removed/changed
            //blackboardService.openTransaction();
            for ( Enumeration e = allElements.getAddedList(); e.hasMoreElements(); )
            {
                Object o = e.nextElement();
                processObject( o, EventPDU.ACTION_ADD );
            }

            for ( Enumeration e = allElements.getRemovedList(); e.hasMoreElements(); )
            {
                Object o = e.nextElement();
                processObject( o, EventPDU.ACTION_REMOVE );
            }

            for ( Enumeration e = allElements.getChangedList(); e.hasMoreElements(); )
            {
                Object o = e.nextElement();
                processChangedObject( o );
            }
            //blackboardService.closeTransaction();

            totalTime += System.currentTimeMillis() - startTime;
            lastExecuted = System.currentTimeMillis();
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }
        finally {
        }
    }

    protected BlackboardService blackboardService;
    protected LoggingService log;
    protected long currentTime, currentExecutionTime;
    protected int logAllocationResultsLevel = AR_LOG_LEVEL_FAST;
    // Parse parameters.  Where do I send my results?
    protected long lastExecuted;
    protected long totalTime;
    protected InetAddress target;
    protected ObjectOutputStream sout;
    protected ClientMessageTransport clientMessageTransport;
    // protected PlanMonitorPlugInUIFrame uiFrame ;
    protected SymbolTable symTable = new SymbolTable();
    protected HashMap allocationToARMap = new HashMap();
}