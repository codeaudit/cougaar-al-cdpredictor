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
package org.cougaar.tools.castellan.plugin;

import org.cougaar.core.plugin.ComponentPlugin;
import org.cougaar.core.service.BlackboardTimestampService;
import org.cougaar.core.service.LoggingService;
import org.cougaar.core.service.BlackboardService;
import org.cougaar.core.service.UIDService;
import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.blackboard.*;
import org.cougaar.core.util.UniqueObject;
import org.cougaar.core.util.UID;
import org.cougaar.core.agent.ClusterIdentifier;
import org.cougaar.core.agent.service.alarm.PeriodicAlarm;
import org.cougaar.core.node.NodeIdentificationService;
import org.cougaar.util.UnaryPredicate;
import org.cougaar.util.ConfigFinder;
import org.cougaar.planning.ldm.plan.*;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.tools.castellan.ldm.PlanLogConfig;
import org.cougaar.tools.castellan.util.MultiTreeSet;
import org.cougaar.tools.castellan.util.Timestampable;
import org.cougaar.tools.castellan.pdu.*;
import org.cougaar.tools.castellan.planlog.PlanLogConstants;
import org.cougaar.tools.castellan.planlog.PDUBuffer;
import org.cougaar.glm.ldm.asset.Organization;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.*;
import java.io.File;

/**
 * This is a PlugIn based plan log which uses the BB timestamp service.
 */
public class PlanLogPlugin extends ComponentPlugin implements PDUSink {

    class FlushThread extends Thread {

        public long getInterval ()
        {
            return interval;
        }

        public void setInterval ( long interval )
        {
            this.interval = interval;
        }

        public boolean isStop ()
        {
            return stop;
        }

        public void setStop ( boolean stop )
        {
            this.stop = stop;
        }

        public void run ()
        {
            while ( !stop ) {
                try {
                   sleep( interval ) ;
                }
                catch ( InterruptedException e ) {
                }

                if ( stop ) {
                    break ;
                }

                BlackboardService bs = getBlackboardService() ;
                bs.openTransaction();
                execute();
                bs.closeTransaction();
            }
        }

        long interval = 4000 ;
        boolean stop = false ;
    }

    class FlushAlarm implements PeriodicAlarm {
        public FlushAlarm( long expTime )
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
            System.out.println("PlanLogPlugin:: Expiring");
            expired = true ;
            BlackboardService bs = getBlackboardService() ;
            bs.openTransaction();
            execute();
            bs.closeTransaction();
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

        boolean stop = false ;
        boolean expired = false ;
        long expTime ;
        long delay = 5000L ;
    }


    UnaryPredicate allPredicate = new UnaryPredicate()
    {
        public boolean execute( Object o )
        {
            return ( o instanceof PlanElement || o instanceof Task || o instanceof Asset );
        }
    };

    public void setupSubscriptions() {
        //DEBUG
        log = ( LoggingService ) getServiceBroker().getService( this, LoggingService.class, null ) ;
        log.info("PlanLogPlugin::Setting up subscriptions...");
        config= getConfigInfo() ;
        getBlackboardService().publishAdd( config ) ;  // Make this visible to the BB so that a servlet can see it.

        if ( config != null && config.getLogCluster() != null ) {

            ServiceBroker broker = getServiceBroker() ;
            bts = ( BlackboardTimestampService ) broker.getService( this, BlackboardTimestampService.class, null ) ;
            allElements = ( IncrementalSubscription ) getBlackboardService().subscribe( allPredicate ) ;

            // Start the statistics logging
            stats = new PlanLogStats() ;
            getBlackboardService().publishAdd( stats ) ;

            // Make a new client message transport
            UIDService service = ( UIDService ) broker.getService( this, UIDService.class, null ) ;
            mtImpl = new RelayClientMTImpl( config, getBlackboardService(), service.nextUID(), getBindingSite().getAgentIdentifier(), stats ) ;
            mtImpl.setPDUSink( this );

            // Check to see if any PDUBuffers already exist (based on rehydration?)
            Collection c = blackboard.query( new UnaryPredicate() {
                public boolean execute ( Object o )
                {
                    if ( o instanceof PDUBuffer ) {
                        return true ;
                    }
                    return false ;
                }
            } ) ;

            if ( c.size() == 0 ) {
                buffer = new PDUBuffer() ;
                getBlackboardService().publishAdd( buffer ) ;
            }
            else {
                Object[] buffers = c.toArray() ;
                buffer = ( PDUBuffer ) buffers[0] ;
                if ( buffers.length > 0 ) {
                    if ( log != null && log.isWarnEnabled() ) {
                        log.warn( "More than one PDU buffer created for agent \"" + getBindingSite().getAgentIdentifier() + "\". Using first." );
                    }
                }
            }

            // Declare myself immediately.
            DeclarePDU pdu =
               new DeclarePDU( config.getNodeIdentifier(),
                               getBindingSite().getAgentIdentifier().cleanToString(),
                               System.currentTimeMillis(), currentTimeMillis() ) ;
            sendMessage( pdu );

            //flushThread = new FlushThread() ;
            //flushThread.start();
            //Start the periodic alarm to flush the buffer.
            getAlarmService().addRealTimeAlarm( new FlushAlarm( System.currentTimeMillis() + 5000 ) ) ;
        }
    }

    protected AllocationResult replicate(AllocationResult ar) {
        if (ar == null) return null;
        return (AllocationResult) ar.clone();
    }

    protected PlanLogConfig getConfigInfo() {
        Collection params = getParameters() ;
        Vector paramVector = new Vector( params ) ;

        String fileName = null ;
        if ( paramVector.size() > 0 ) {
            fileName = ( String ) paramVector.elementAt(0) ;
        }

        // DEBUG
        // System.out.printlbn( "Configuring PlanEventLogPlugIn from " + fileName ) ;

        ConfigFinder finder = getConfigFinder() ;
        ClusterIdentifier clusterId = null ;

        // Start with the default settings
        PlanLogConfig config = new PlanLogConfig() ;

        ServiceBroker sb = getServiceBroker() ;
        NodeIdentificationService nis = ( NodeIdentificationService )
                sb.getService( this, NodeIdentificationService.class, null ) ;
        config.setNodeIdentifier( nis.getNodeIdentifier().cleanToString() ) ;

        try {
            String clusterName = null ;
            if ( fileName != null && finder != null ) {

                File f = finder.locateFile( fileName ) ;

                // DEBUG -- Replace by call to log4j
                if ( log != null && log.isInfoEnabled() ) {
                    log.info( "PlanLogConfigPlugin:: Configuring PlanLogConfig from " + f );
                }
                System.out.println( "PlanLogConfigPlugin:: Configuring PlanLogConfig from " + f ) ;

                if ( f != null && f.exists() ) {
                    //
                    // Now, parse the config file
                    //

                    Document doc = null ;
                    try {
                        doc = finder.parseXMLConfigFile(fileName);
                    }
                    catch ( Exception e ) {
                        System.out.println( e ) ;
                    }

                    if ( doc != null ) {
                        try {
                            Node root = doc.getDocumentElement() ;
                            if( root.getNodeName().equals( "plpconfig" ) ) {
                                clusterName = getNodeValueForTag(doc, "PlanLogAgent", "identifier" );

                                if ( clusterName != null ) {
                                    config.setLogCluster( clusterName );
                                }

                                String value = getNodeValueForTag( doc, "LogAllocationResults", "value" ) ;
                                if ( value != null ) {
                                    boolean logar = Boolean.valueOf( value ).booleanValue() ;
                                    config.setLogAllocationResults( logar );
                                }

                                value = getNodeValueForTag( doc, "LogTaskRemoves", "value" ) ;
                                if ( value != null ) {
                                    boolean logtr = Boolean.valueOf( value ).booleanValue() ;
                                    config.setLogTaskRemoves( logtr );
                                }

                                // Get logging level
                                //nodes = doc.getElementsByTagName( PlanLogConstants.AR_LOG_DETAIL ) ;
                                // Get whether to log epochs
                                //nodes = doc.getElementsByTagName( PlanLogConstants.LOG_EPOCHS ) ;

                                // Get whether to log trigger executes on plug-ins.
                            }
                            else {
                                // DEBUG -- replace with log4j
                                if ( log.isWarnEnabled() ) {
                                    log.warn( "Warning:: Plan log config file is invalid, no root node \"plpconfig\"" );
                                }
                                System.out.println( "Warning:: Plan log config file is invalid, no root node \"plpconfig\"" ) ;
                            }
                        }
                        catch ( Exception e ) {
                            if ( log.isErrorEnabled() ) {
                                log.error( "Exception thrown parsing configuration file " + f );
                            }
                            System.out.println( e ) ;
                        }
                    }

                }

            }

        } catch ( Exception e ) {
            e.printStackTrace() ;
        }

        if ( config.getLogCluster() == null ) {
            //System.out.println( "Warning:: No configuration information found for agent " + getBindingSite().getAgentIdentifier() ) ;
            log.error( "Warning:: No configuration information found for agent " + getBindingSite().getAgentIdentifier() );
        }
        else {
            if ( config.getLogCluster().equals( getBindingSite().getAgentIdentifier().cleanToString() ) ) {
            // DEBUG
                System.out.println( "Setting " + getBindingSite().getAgentIdentifier().cleanToString() + " as server." );
                config.setServer( true );
            }
        }

        System.out.println("PlanLogPlugin:: Configuration data " + config );

        return config ;

    }

    private String getNodeValueForTag(Document doc, String tagName, String namedItem ) {
        NodeList nodes = doc.getElementsByTagName( tagName );

        String value = null ;
        // Get target plan log
        for (int i=0;i<nodes.getLength();i++) {
            Node n = nodes.item(i) ;
            value = n.getAttributes().getNamedItem( namedItem ).getNodeValue() ;
            //System.out.println( "Found identifier=" + value );
        }
        return value;
    }


    /**
     * Compares allocation results and returns new AR if the newResult has changed.
     */
    protected AllocationResult compareAndReplicate(UID planElement,
                                                   short arType, AllocationResult old, AllocationResult newResult,
                                                   ArrayList message) {
        if (old == null && newResult == null) {
            return null;
        }
        if (old == null && newResult != null) {
            message.add(PlanToPDUTranslator.makeAllocationResultMessage(getCurrentExecutionTime(), getCurrentTime(), planElement, newResult, arType, EventPDU.ACTION_ADD));
            return (AllocationResult) newResult.clone();

        }
        if (old != null && newResult == null) {
            message.add(PlanToPDUTranslator.makeAllocationResultMessage(getCurrentExecutionTime(), getCurrentTime(), planElement, old, arType, EventPDU.ACTION_REMOVE));
            return null;  // Should publish an AllocationResultPDU with a REMOVE flag
        }
        if (!old.isEqual(newResult)) {
            AllocationResult result = (AllocationResult) newResult.clone();
            message.add(PlanToPDUTranslator.makeAllocationResultMessage(getCurrentExecutionTime(), getCurrentTime(), planElement, newResult, arType, EventPDU.ACTION_CHANGE));
            return result;
        }
        return null;
    }

    /**
     * Fast CRC32 comparison between two allocation results based only on confidence and success.
     */
    long compareAR(UID planElement, short arType, long value, AllocationResult newResult, ArrayList message) {
        long newValue = PlanToPDUTranslator.computeFastCRC32(newResult);
        if (newValue == value) {
            return value;
        }
        if (newValue != 0 && value == 0) {
            message.add(PlanToPDUTranslator.makeAllocationResultMessage(getCurrentExecutionTime(), getCurrentTime(), planElement, newResult, arType, EventPDU.ACTION_ADD));
            return newValue;
        }
        if (newValue == 0 && value != 0) {
            // make a remove message
            //message.add( PlanToPDUTranslator.makeAllocationResultMessage( this, planElement, old, arType, EventPDU.ACTION_REMOVE ) ) ;
            return newValue;  // Should publish an AllocationResultPDU with a REMOVE flag
        }
        AllocationResult result = (AllocationResult) newResult.clone();
        message.add(PlanToPDUTranslator.makeAllocationResultMessage(getCurrentExecutionTime(), getCurrentTime(), planElement, newResult, arType, EventPDU.ACTION_CHANGE));

        return newValue;
    }

    protected int getActionForChange(int start, int end) {
        if (start == -1) {
            return EventPDU.ACTION_ADD;
        }
        else if (end == -1) {
            return EventPDU.ACTION_REMOVE;
        }
        else {
            return EventPDU.ACTION_CHANGE;
        }
    }

    /**
     * Compare current set of allocation results against previous versions based only on
     * success/failure/null values.
     */
    protected void checkARSuccess(long currentTime, long currentExecutionTime, PlanElement a, ArrayList pdus) {
        int[] tuple = (int[]) allocationToARMap.get(a.getUID());

        // Compute
        if (tuple == null) {
            if (a.getEstimatedResult() == null && a.getReceivedResult() == null &&
                    a.getReportedResult() == null && a.getObservedResult() == null) {
                return;
            }
            tuple = new int[4];
            AllocationResult estimated = a.getEstimatedResult(), received = a.getReceivedResult(),
                    reported = a.getReportedResult(), observed = a.getObservedResult();
            tuple[0] = ( estimated == null ) ? -1 : ( estimated.isSuccess() ? 1 : 0 );
            tuple[1] = ( received == null ) ? -1 : ( received.isSuccess() ? 1 : 0 );
            tuple[2] = ( reported == null ) ? -1 : ( reported.isSuccess() ? 1 : 0 );
            tuple[3] = ( observed == null ) ? -1 : ( observed.isSuccess() ? 1 : 0 );
            allocationToARMap.put(a.getUID(), tuple);
            if (tuple[0] != -1) {
                AllocationResultPDU pdu = PlanToPDUTranslator.makeAllocationResultMessage(currentExecutionTime, currentTime, a.getUID(), estimated,
                        AllocationResultPDU.ESTIMATED, EventPDU.ACTION_ADD);
                pdus.add(pdu);
            }
            if (tuple[1] != -1) {
                AllocationResultPDU pdu = PlanToPDUTranslator.makeAllocationResultMessage(currentExecutionTime, currentTime, a.getUID(), received,
                        AllocationResultPDU.RECEIVED, EventPDU.ACTION_ADD);
                pdus.add(pdu);
            }
            if (tuple[2] != -1) {
                AllocationResultPDU pdu = PlanToPDUTranslator.makeAllocationResultMessage(currentExecutionTime, currentTime, a.getUID(), reported,
                        AllocationResultPDU.REPORTED, EventPDU.ACTION_ADD);
                pdus.add(pdu);
            }
            if (tuple[3] != -1) {
                AllocationResultPDU pdu = PlanToPDUTranslator.makeAllocationResultMessage(currentExecutionTime, currentTime, a.getUID(), observed,
                        AllocationResultPDU.OBSERVED, EventPDU.ACTION_ADD);
                pdus.add(pdu);
            }
        }
        else {
            // Compare success/failure/non-existence of AR
            AllocationResult estimated = a.getEstimatedResult(), received = a.getReceivedResult(),
                    reported = a.getReportedResult(), observed = a.getObservedResult();
            int t1 = ( estimated == null ) ? -1 : ( estimated.isSuccess() ? 1 : 0 );
            int t2 = ( received == null ) ? -1 : ( received.isSuccess() ? 1 : 0 );
            int t3 = ( reported == null ) ? -1 : ( reported.isSuccess() ? 1 : 0 );
            int t4 = ( observed == null ) ? -1 : ( observed.isSuccess() ? 1 : 0 );
            if (t1 != tuple[0]) {
                AllocationResultPDU pdu = PlanToPDUTranslator.makeAllocationResultMessage(currentExecutionTime, currentTime, a.getUID(), estimated,
                        AllocationResultPDU.ESTIMATED, getActionForChange(tuple[0], t1));
                pdus.add(pdu);
            }
            if (t2 != tuple[1]) {
                AllocationResultPDU pdu = PlanToPDUTranslator.makeAllocationResultMessage(currentExecutionTime, currentTime, a.getUID(), received,
                        AllocationResultPDU.RECEIVED, getActionForChange(tuple[1], t2));
                pdus.add(pdu);
            }
            if (t3 != tuple[2]) {
                AllocationResultPDU pdu = PlanToPDUTranslator.makeAllocationResultMessage(currentExecutionTime, currentTime, a.getUID(), reported,
                        AllocationResultPDU.REPORTED, getActionForChange(tuple[2], t3));
                pdus.add(pdu);
            }
            if (t4 != tuple[3]) {
                AllocationResultPDU pdu = PlanToPDUTranslator.makeAllocationResultMessage(currentExecutionTime, currentTime, a.getUID(), observed,
                        AllocationResultPDU.OBSERVED, getActionForChange(tuple[3], t4));
                pdus.add(pdu);
            }
        }
    }

    /** Changed to use CRC32 to compare allocation results.
     *  This is most likely too slow and will require significant optimization.
     * @deprecated
     */
    protected void checkARChanged(Allocation a) {
        long[] tuple = (long[]) allocationToARMap.get(a.getUID());
        if (tuple == null) {
            if (a.getEstimatedResult() == null && a.getReceivedResult() == null &&
                    a.getReportedResult() == null && a.getObservedResult() == null) {
                return;
            }
            tuple = new long[4];
            AllocationResult estimated = a.getEstimatedResult(), received = a.getReceivedResult(),
                    reported = a.getReportedResult(), observed = a.getObservedResult();
            tuple[0] = PlanToPDUTranslator.computeFastCRC32(estimated);
            tuple[1] = PlanToPDUTranslator.computeFastCRC32(received);
            tuple[2] = PlanToPDUTranslator.computeFastCRC32(reported);
            tuple[3] = PlanToPDUTranslator.computeFastCRC32(observed);
            allocationToARMap.put(a.getUID(), tuple);
            if (tuple[0] != 0) {
                AllocationResultPDU pdu = PlanToPDUTranslator.makeAllocationResultMessage(getCurrentExecutionTime(), getCurrentTime(), a.getUID(), estimated,
                        AllocationResultPDU.ESTIMATED, EventPDU.ACTION_ADD);
                sendMessage(pdu);
            }
            if (tuple[1] != 0) {
                AllocationResultPDU pdu = PlanToPDUTranslator.makeAllocationResultMessage(getCurrentExecutionTime(), getCurrentTime(), a.getUID(), received,
                        AllocationResultPDU.RECEIVED, EventPDU.ACTION_ADD);
                sendMessage(pdu);
            }
            if (tuple[2] != 0) {
                AllocationResultPDU pdu = PlanToPDUTranslator.makeAllocationResultMessage(getCurrentExecutionTime(), getCurrentTime(), a.getUID(), reported,
                        AllocationResultPDU.REPORTED, EventPDU.ACTION_ADD);
                sendMessage(pdu);
            }
            if (tuple[3] != 0) {
                AllocationResultPDU pdu = PlanToPDUTranslator.makeAllocationResultMessage(getCurrentExecutionTime(), getCurrentTime(), a.getUID(), observed,
                        AllocationResultPDU.OBSERVED, EventPDU.ACTION_ADD);
                sendMessage(pdu);
            }
        }
        else {
            messages.clear();
            tuple[0] = compareAR(a.getUID(), AllocationResultPDU.ESTIMATED, tuple[0], a.getEstimatedResult(), messages);
            tuple[1] = compareAR(a.getUID(), AllocationResultPDU.RECEIVED, tuple[1], a.getReceivedResult(), messages);
            tuple[2] = compareAR(a.getUID(), AllocationResultPDU.REPORTED, tuple[2], a.getReportedResult(), messages);
            tuple[3] = compareAR(a.getUID(), AllocationResultPDU.OBSERVED, tuple[3], a.getObservedResult(), messages);
            for (int i = 0 ; i < messages.size() ; i++) {
                PDU pdu = (PDU) messages.get(i);
                sendMessage(pdu);
            }
        }

    }


    /**
     * This is a (too) slow but comprehensive measure of allocation results.
     */
    protected void checkAllocationResultChanged(Allocation a) {
        ARTuple tuple = (ARTuple) allocationToARMap.get(a.getUID());
        if (tuple == null) {
            if (a.getEstimatedResult() == null && a.getReceivedResult() == null &&
                    a.getReportedResult() == null && a.getObservedResult() == null) {
                return;
            }

            tuple = new ARTuple();
            tuple.estimated = replicate(a.getEstimatedResult());
            tuple.received = replicate(a.getReceivedResult());
            tuple.reported = replicate(a.getReportedResult());
            tuple.observed = replicate(a.getObservedResult());
            allocationToARMap.put(a.getUID(), tuple);
            if (tuple.estimated != null) {
                AllocationResultPDU pdu = PlanToPDUTranslator.makeAllocationResultMessage(getCurrentExecutionTime(), getCurrentTime(), a.getUID(), tuple.estimated,
                        AllocationResultPDU.ESTIMATED, EventPDU.ACTION_ADD);
                sendMessage(pdu);
            }
            if (tuple.received != null) {
                AllocationResultPDU pdu = PlanToPDUTranslator.makeAllocationResultMessage(getCurrentExecutionTime(), getCurrentTime(), a.getUID(), tuple.received,
                        AllocationResultPDU.RECEIVED, EventPDU.ACTION_ADD);
                sendMessage(pdu);
            }
            if (tuple.reported != null) {
                AllocationResultPDU pdu = PlanToPDUTranslator.makeAllocationResultMessage(getCurrentExecutionTime(), getCurrentTime(), a.getUID(), tuple.reported,
                        AllocationResultPDU.REPORTED, EventPDU.ACTION_ADD);
                sendMessage(pdu);
            }
            if (tuple.observed != null) {
                AllocationResultPDU pdu = PlanToPDUTranslator.makeAllocationResultMessage(getCurrentExecutionTime(), getCurrentTime(), a.getUID(), tuple.observed,
                        AllocationResultPDU.OBSERVED, EventPDU.ACTION_ADD);
                sendMessage(pdu);
            }
        }
        else { // Compare the existing against the old
            messages.clear();
            tuple.estimated = compareAndReplicate(a.getUID(), AllocationResultPDU.ESTIMATED, tuple.estimated, a.getEstimatedResult(), messages);
            tuple.received = compareAndReplicate(a.getUID(), AllocationResultPDU.RECEIVED, tuple.received, a.getReceivedResult(), messages);
            tuple.reported = compareAndReplicate(a.getUID(), AllocationResultPDU.REPORTED, tuple.estimated, a.getReportedResult(), messages);
            tuple.observed = compareAndReplicate(a.getUID(), AllocationResultPDU.OBSERVED, tuple.estimated, a.getObservedResult(), messages);
            for (int i = 0 ; i < messages.size() ; i++) {
                PDU pdu = (PDU) messages.get(i);
                sendMessage(pdu);
            }
        }
    }

    protected void processChangedObject( UniqueObject o ) {
        long cet, ct;
        int tid = -1;

        cet = getCurrentExecutionTime();
        if ( bts != null ) {
            ct = bts.getModificationTime( o.getUID() ) ;
        }
        else {
            ct = getCurrentTime() ;
        }

        int action = EventPDU.ACTION_CHANGE;
        // System.out.println( "Processing " + o ) ;
        EventPDU pdu = null;
        synchronized (pdus) {
            if (o instanceof MPTask) {
                pdu = PlanToPDUTranslator.makeMPTaskPDU(cet, ct, (MPTask) o, action);
            }
            else if (o instanceof Task) {
                pdu = PlanToPDUTranslator.makeTaskMessage(cet, ct, (Task) o, action);
            }
            else if (o instanceof Asset) {
                pdu = PlanToPDUTranslator.makeAssetMessage(cet, ct, (Asset) o, action);
            }
            else if (o instanceof Allocation) {
                // Check to (specifically) see if the AR has changed.  If so, make
                // an AllocationResultPDU
                Allocation al = (Allocation) o;
                pdu = PlanToPDUTranslator.makeAllocationMessage(cet, ct, al, action);
                if ( config.isLogAllocationResults() ) {
                    if (logAllocationResultsLevel == PlanLogConstants.AR_LOG_LEVEL_NONE) {
                        // Do nothing by default
                    }
                    else if (logAllocationResultsLevel == PlanLogConstants.AR_LOG_LEVEL_FULL) {
                        checkAllocationResultChanged(al);
                    }
                    else if (logAllocationResultsLevel == PlanLogConstants.AR_LOG_LEVEL_SUCCESS) {
                        checkARSuccess(ct, cet, al, pdus);
                    }
                }
                //checkAllocationResultChanged( al ) ;
            }
            else if (o instanceof Aggregation) {
                pdu = PlanToPDUTranslator.makeAggregationPDU(cet, ct, (AggregationImpl) o, action);
            }
            else if (o instanceof Expansion) {
                pdu = PlanToPDUTranslator.makeExpansionPDU(cet, ct, (Expansion) o, action);
            }
            if (pdu == null) {
                return;
            }
            // Send all generated pdus.

            for (int i = 0 ; i < pdus.size() ; i++) {
                EventPDU epdu = (EventPDU) pdus.get(i);
                epdu.setTransactionId(tid);
                sendMessage(epdu);
            }
            pdus.clear();
        }
        pdu.setTransactionId(tid);
        sendMessage(pdu);
    }

    static ArrayList pdus = new ArrayList(4);

    protected void processObject( UniqueObject o, int action ) {
        long cet, ct;
        int tid = -1;

        cet = getCurrentExecutionTime();
        if ( bts == null ) {
            ct = getCurrentTime() ;
        }
        else if ( action == EventPDU.ACTION_ADD ) {
            ct = bts.getCreationTime( o.getUID() ) ;
        }
        else { // This must be a changed condition, so just get the current system time.
            ct = getCurrentTime() ;
        }

        // System.out.println( "Processing " + o ) ;
        try {
            EventPDU pdu = null;

            synchronized (pdus) {
                if (o instanceof MPTask) {
                    pdu = PlanToPDUTranslator.makeMPTaskPDU(cet, ct, (MPTask) o, action);
                }
                else if (o instanceof Task) {
                    pdu = PlanToPDUTranslator.makeTaskMessage(cet, ct, (Task) o, action);
                }
                else if (o instanceof Asset) {
                    AssetPDU apdu = PlanToPDUTranslator.makeAssetMessage(cet, ct, (Asset) o, action);
                    // Send relationship information for all organization assets.
                    if (action == EventPDU.ACTION_ADD && o instanceof org.cougaar.glm.ldm.asset.Organization) {
                        Organization org = (Organization) o;
                        RelationshipPGPDU pgpdu = PlanToPDUTranslator.makeRelationshipPDU(cet, ct, org.getRelationshipPG());
                        ClusterPGPDU cpgpdu = PlanToPDUTranslator.makeClusterPDU( org.getClusterPG() ) ;
                        apdu.setPropertyGroups(new PropertyGroupPDU[]{pgpdu, cpgpdu});
                    }
                    pdu = apdu;
                }
                else if (o instanceof Allocation) {
                    // Check to (specifically) see if the AR has changed.  If so, make
                    // an AllocationResultPDU
                    Allocation al = (Allocation) o;
                    pdu = PlanToPDUTranslator.makeAllocationMessage(cet, ct, al, action);
                    if ( config.isLogAllocationResults() ) {
                        if (logAllocationResultsLevel == PlanLogConstants.AR_LOG_LEVEL_NONE) {
                            // Do nothing by default
                        }
                        else if (logAllocationResultsLevel == PlanLogConstants.AR_LOG_LEVEL_FULL) {
                            checkAllocationResultChanged(al);
                        }
                        else if (logAllocationResultsLevel == PlanLogConstants.AR_LOG_LEVEL_SUCCESS) {
                            checkARSuccess(ct, cet, al, pdus);
                        }
                    }
                }
                else if (o instanceof Aggregation) {
                    pdu = PlanToPDUTranslator.makeAggregationPDU(cet, ct, (AggregationImpl) o, action);
                }
                else if (o instanceof Expansion) {
                    pdu = PlanToPDUTranslator.makeExpansionPDU(cet, ct, (Expansion) o, action);
                }
                if (pdu == null) {
                    return;
                }

                for (int i = 0 ; i < pdus.size() ; i++) {
                    EventPDU epdu = (EventPDU) pdus.get(i);
                    epdu.setTransactionId(tid);
                    sendMessage(epdu);
                }
                pdus.clear();
            }
            pdu.setTransactionId(tid);
            sendMessage(pdu);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Send outgoing PDU.
     */
    protected void sendMessage( PDU pdu ) {
        // Publish this to the LogMessageBuffer
        // System.out.print("S+");
        // increment count
        if ( stats != null &&  pdu instanceof EventPDU ) {
            EventPDU epdu = ( EventPDU ) pdu ;
            if ( stats.getFirstEventTime() == 0 && epdu.getTime() != -1 ) {
                stats.setFirstEventTime( epdu.getTime() );
            }
            else {
                stats.setNumBadTimestamps( stats.getNumBadTimestamps() + 1 );
            }
            if ( epdu.getTime() != -1 ) {
                stats.setLastEventTime( epdu.getTime() );
            }

            // Increment the number of messages sent by one.
            stats.setNumPdusSent( stats.getNumPdusSent() + 1 );

            // Track stats for task PDUs specifically.
            if ( epdu instanceof TaskPDU ) {
                TaskPDU tpdu = ( TaskPDU ) epdu ;
                switch ( epdu.getAction() ) {
                    case EventPDU.ACTION_ADD :
                        stats.setNumTaskAdds( stats.getNumTaskAdds() + 1 );
                        break ;
                    case EventPDU.ACTION_CHANGE :
                        stats.setNumTaskChanges( stats.getNumTaskChanges() + 1 );
                        break ;
                    case EventPDU.ACTION_REMOVE :
                        stats.setNumTaskRemoves( stats.getNumTaskRemoves() + 1 );
                        break ;
                    default :
                        if ( log.isErrorEnabled() ) {
                            log.error( "EventPDU with unknown action." );
                        }
                }

                taskUIDMap.put( tpdu.getUID(), tpdu.getUID() ) ;
                stats.setNumUniqueTaskUIDs( taskUIDMap.size() ) ;
            }

        }
        mtImpl.sendMessage( pdu );
    }

    /**
     * Process incoming pdu.  If I am a server, update the PDUBuffer on the
     * blackboard.
     */
    public void processPDU(PDU pdu) {
        // If I am a server, just update the PDUBuffer on the blackboard.
        if ( config.isServer() ) {
            // System.out.println("RECEIVED " + pdu );
            synchronized ( buffer ) {
                buffer.addIncoming( pdu );
            }
        }
    }

    public void processChangedList( Enumeration iter ) {
        // Remove duplicates.
        map.clear();
        while ( iter.hasMoreElements() ) {
            processChangedObject( ( UniqueObject ) iter.nextElement() );
            //UniqueObject uo = ( UniqueObject ) iter.nextElement() ;
            //map.put( uo.getUID(), uo ) ;
        }

//        for ( Iterator iter2 =map.values().iterator();iter2.hasNext();) {
//            UniqueObject uo = ( UniqueObject ) iter2.next() ;
//            long l = bts.getModificationTime( uo.getUID() ) ;
//            if ( l != TimestampEntry.UNKNOWN_TIME ) {
//                 sortedMap.put( new Long(l), uo ) ;
//            }
//        }

        // Now, process the changed list.
        //for ( Iterator iter2=map.values().iterator();iter2.hasNext();) {
        //    processChangedObject( ( UniqueObject ) iter2.next() );
        //}
    }

    public long getCurrentTime() {
        return currentTime ;
    }

    public long getCurrentExecutionTime() {
        return currentExecutionTime;
    }

    public void suspend ()
    {
        super.suspend();
    }

    public void unload ()
    {
        super.unload();

        // Stop the thread
        flushThread.setStop( true );
        flushThread.interrupt();
        flushThread = null ;

        // Release the logging service.
        getServiceBroker().releaseService( this,  LoggingService.class, log );
    }

    public void execute() {
        if ( allElements == null ) {
            if ( log.isWarnEnabled() ) {
                log.warn("PlanLogPlugin:: Null subscription?");
            }
            return ;
        }

        currentExecutionTime = currentTimeMillis() ;
        currentTime = System.currentTimeMillis() ;

        //stats.setNumAddsTotal( allElements.getAddedCollection().size());

        for ( Iterator e = allElements.getAddedCollection().iterator(); e.hasNext(); )
        {
            Object o = e.next();
            if ( o instanceof Task ) {
                //Task temp = ( Task ) o ;
                // Just count these UIDs up since we should only ever see one add.
                //debugTaskUIDMap.put( temp.getUID(), temp.getUID() ) ;
                stats.setNumTasksSeenDebug( stats.getNumTasksSeenDebug() + 1 );
            }
            processObject( ( UniqueObject ) o, EventPDU.ACTION_ADD );
        }

        processChangedList( allElements.getChangedList() );

        if ( config.isLogTaskRemoves() ) {
            for ( Enumeration e = allElements.getRemovedList(); e.hasMoreElements(); ) {
                processObject( ( UniqueObject ) e.nextElement(), EventPDU.ACTION_REMOVE );
            }
        }

        // Execute
        mtImpl.execute();
    }

    protected HashMap debugTaskUIDMap = new HashMap() ;
    protected HashMap taskUIDMap = new HashMap() ;
    protected PlanLogStats stats ;
    protected FlushThread flushThread ;
    protected PlanLogConfig config ;
    protected PDUBuffer buffer ;
    protected ClientMessageTransport mtImpl ;
    /**
     * Persistent observation of AllocationResult true/false state.
     */
    protected HashMap allocationToBooleanMap = new HashMap();

    /**
     * Persistent observation of AR state.
     */
    protected HashMap allocationToARMap = new HashMap();

    /**
     * Temp buffer. Non-persistent.
     */
    protected ArrayList messages = new ArrayList(4);
    protected long currentExecutionTime, currentTime ;
    protected int logAllocationResultsLevel = PlanLogConstants.AR_LOG_LEVEL_SUCCESS;
    protected IncrementalSubscription allElements ;

    /** Used to remove duplicates. Does not need to be persisted.*/
    protected HashMap map = new HashMap() ;
    protected LoggingService log;
    protected BlackboardTimestampService bts = null ;
}
