/*
  * <copyright>
  *  Copyright 2002 (Intelligent Automation, Inc.)
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
package org.cougaar.tools.castellan.ldm;

import org.cougaar.core.agent.*;
import org.cougaar.core.blackboard.*;
import org.cougaar.core.util.*;
import org.cougaar.core.service.LoggingService;
import org.cougaar.core.domain.*;
import org.cougaar.core.relay.*;
import org.cougaar.core.domain.RootPlan;
//import org.cougaar.planning.ldm.LogPlan;
//import org.cougaar.planning.ldm.PlanningFactory;
import org.cougaar.planning.ldm.plan.*;
import org.cougaar.planning.ldm.asset.*;
//import org.cougaar.planning.ldm.lps.*;
import org.cougaar.tools.castellan.plugin.*;
import org.cougaar.tools.castellan.pdu.*;
import org.cougaar.tools.castellan.util.*;
import org.cougaar.tools.castellan.planlog.*;
import org.cougaar.glm.ldm.asset.Organization;
//import org.cougaar.glm.execution.eg.ClusterInfo;
import org.cougaar.core.mts.*;

import java.util.*;

/**
 * Monitors all EnvelopeTuples.  For client agents, they are sent to the designed plan log server.  For
 * server agents.
 *
 * <p>  This plugin is configured using a XML configuration file whose name is passed to the PlanLogConfigPlugin.
 * If this configuration file is not present, no EventPDUs will be emitted.
 */

/*public class PlanEventLogLP extends LogPlanLogicProvider implements LogicProvider, EnvelopeLogicProvider, MessageLogicProvider,
        PDUSink {

    public PlanEventLogLP(LogPlanServesLogicProvider logplan, ClusterServesLogicProvider cluster) {
    public PlanEventLogLP(RootPlan logplan, ClusterContext cluster) {
        super(logplan, cluster);
        Send message declaring existence of self.
    }
 */

public class PlanEventLogLP extends RelayLP implements LogicProvider, EnvelopeLogicProvider, MessageLogicProvider,
        PDUSink {

    public PlanEventLogLP(RootPlan rootPlan, MessageAddress self) {
                super(rootPlan, self);
        // Send message declaring existence of self.
    }

    public void init() {
        clientMessageTransport = new BlackboardMTForPlanEventLogLP(rootPlan, self);
        clientMessageTransport.setPDUSink(this);
        //sendMessage(new DeclarePDU(cluster.getClusterIdentifier().cleanToString()));
    }

    protected AllocationResult replicate(AllocationResult ar) {
        if (ar == null) return null;
        return (AllocationResult) ar.clone();
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

    protected void processChangedObject(EnvelopeTuple env, Object o) {
        long cet, ct;
        int tid ;
        if (env instanceof Timestampable) {
            Timestampable t = (Timestampable) env;
            cet = t.getExecutionTime();
            ct = t.getTime();
            tid = t.getTransactionId() ;
        }
        else {
            cet = getCurrentExecutionTime();
            ct = getCurrentTime();
            tid = -1 ;
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
                if (logAllocationResultsLevel == PlanLogConstants.AR_LOG_LEVEL_NONE) {
                    // Do nothing by default
                }
                else if (logAllocationResultsLevel == PlanLogConstants.AR_LOG_LEVEL_FULL) {
                    checkAllocationResultChanged(al);
                }
                else if (logAllocationResultsLevel == PlanLogConstants.AR_LOG_LEVEL_SUCCESS) {
                    checkARSuccess(ct, cet, al, pdus);
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

    protected void processObject(EnvelopeTuple env, Object o, int action) {
        long cet, ct;
        int tid ;
        if (env instanceof Timestampable) {
            Timestampable t = (Timestampable) env;
            cet = t.getExecutionTime();
            ct = t.getTime();
            tid = t.getTransactionId();
        }
        else {
            cet = getCurrentExecutionTime();
            ct = getCurrentTime();
            tid = -1 ;
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

    protected void setLogConfig(PlanLogConfig config) {
        this.config = config;

        // Tell the CMT to route all outbound messages to the server.
        if (config.getLogCluster() != null) {
            clientMessageTransport.setLoggingDestination(config.getLogCluster());
        }
        flushInboundPDUs();
    }

    private static int count = 0 ;
    protected void sendMessage(PDU pdu) {
        if (clientMessageTransport != null && pdu != null ) {
            clientMessageTransport.sendMessage(pdu);
        }
    }

    public long getCurrentTime() {
        return currentTime;
    }

    public long getCurrentExecutionTime() {
        return currentExecutionTime;
    }

    public void execute(Directive m, Collection changeReports) {
        // Handle incoming directives.
        if (m instanceof LogMessage) {
            System.out.print("L+");
            clientMessageTransport.handleIncomingMessages((LogMessage) m);
        }
    }

    private void flushInboundPDUs() {
        if (inboundPDUs.size() > 0 && config != null) {
            ArrayList inbound = inboundPDUs;
            inboundPDUs = new ArrayList();
            for (int i = 0 ; i < inbound.size() ; i++) {
                PDU p = (PDU) inbound.get(i);
                processPDU(p);
            }
        }
    }

    /**
     * Process inbound PDUs. This is only called by the BlackboardMTForLP!
     */
    public void processPDU(PDU pdu) {

        if (config == null) {
            inboundPDUs.add(pdu);
            return;
        }

        // If I am a server, update the buffer.
        if (config.isServer()) {
            serverProcessPDU(pdu);
        }
    }

    private void serverProcessPDU(PDU pdu) {
        // Respond to declare pdus.
        if (pdu instanceof DeclarePDU) {
            DeclarePDU dpdu = (DeclarePDU) pdu;
            // System.out.println( "PlanEventLogLP:: New cluster " + dpdu.getName() + " registered."  );
            // If time synch was enabled, it would happen here.
        }
        // System.out.println("Received " + pdu);

        // Record all unique object pdus to the blackboard.
        if (pdu instanceof EventPDU || pdu instanceof ExecutionPDU) {
            //System.out.print( "M" )

            if (buffer == null) {
                buffer = new PDUBuffer();
                rootPlan.add(buffer);
            }

            // Add the pdu to the buffer, synchronizing along the way.
            synchronized ( buffer ) {
                // Check to see if nobody is at home.
                if ( buffer.getIncomingSize() > 350000 ) {

                    buffer.clearIncoming();
                }
                buffer.addIncoming(pdu);
            }
        }
    }

    private ArrayList traces = new ArrayList();

    /**
     * Periodically, remove any execution traces.
     */
    private void flushTraces() {
        for (int j = 0 ; j < traces.size() ; j++) {
            ExecutionTrace trace = (ExecutionTrace) traces.get(j);
            if (trace.getNumRecords() > 0) {
                for (int i = 0 ; i < trace.getNumRecords() ; i++) {
                    ExecutionRecord record = trace.getRecord(i);
                    // This is somewhat stupid.
                    ExecutionPDU pdu = new ExecutionPDU(trace.getClusterName(), trace.getPluginClass(),
                            trace.getPluginHashCode(), record.getSeqId(), record.getTransactionId(),
                            record.getStartTime(), record.getEndTime());
                    sendMessage(pdu);
                }
                trace.clearRecords();
            }
        }
    }

    public void execute(EnvelopeTuple env, Collection changeReports) {
        Object o = env.getObject();

        if ( config != null && !config.isActive() ) {
            return ;
        }

        //Object ob = (Object) self;

        //ClusterInfo ci = (ClusterInfo)ob;
        currentExecutionTime = getCurrentExecutionTime();   
        currentTime = System.currentTimeMillis();

        // Handle all execution traces.
        if (env.isAdd() && o instanceof ExecutionTrace) {
            ExecutionTrace trace = (ExecutionTrace) o;
            traces.add(trace);
            return;
        }

        flushTraces();

        // Handle any configuration objects and send a DeclarePDU if we receive them.
        if (o instanceof PlanLogConfig) {
            PlanLogConfig config = ( PlanLogConfig ) o ;
            System.out.println("PlanEventLogLP::Received configuration settings...");
            setLogConfig( config );
            if ( config.isActive() ) {
                DeclarePDU pdu =
                   new DeclarePDU( config.getNodeIdentifier(),
                           self.toString(), System.currentTimeMillis(), -1 ) ;
                sendMessage( pdu );
            }
            return;
        }

        // Ignore all bulk changes
        if (env.isBulk()) {
            // DEBUG  -- REMOVE
            System.out.println("PlanEventLogLP::Ignoring all bulk changes.");
            if ( log.isInfoEnabled() ) {
                log.info( "PlanEventLogLP::Ignoring all bulk changes." ) ;
            }
            return;
        }

        if (env.isChange() && ( o instanceof FlushObject )) {
            clientMessageTransport.conditionalFlush();
            return;
        }

        // Ignore all other non-plan related tasks for now
        if (!( o instanceof PlanElement || o instanceof Task || o instanceof Asset )) {
            return;
        }

        //if ( o instanceof Task ) {
        //    System.out.print("Processing " + ( ( UniqueObject ) o ).getUID() );
        //}

        if (env.isAdd()) {
            processObject(env, o, UniqueObjectPDU.ACTION_ADD);
        }
        else if (env.isRemove()) {
            processObject(env, o, UniqueObjectPDU.ACTION_REMOVE);
        }
        else if (env.isChange()) {
            processChangedObject(env, o);
        }
        else {
            System.out.println("Warning: Unknown change type.");
        }
    }

    protected boolean declared = false;
    protected PlanLogConfig config;

    protected LoggingService log ;
    protected long currentTime, currentExecutionTime;
    ArrayList messages = new ArrayList(4);

    protected int logAllocationResultsLevel = PlanLogConstants.AR_LOG_LEVEL_SUCCESS;
    // Parse parameters.  Where do I send my results?
    protected long lastExecuted;
    protected long totalTime;
    protected BlackboardMTForPlanEventLogLP clientMessageTransport;

    /**
     * Mapping from allocations to succes/failure mappings.
     */
    protected HashMap allocationToBooleanMap = new HashMap();
    protected HashMap allocationToARMap = new HashMap();

    protected PDUBuffer buffer;
    protected ArrayList inboundPDUs = new ArrayList();
    protected RootPlan rootPlan;
    protected MessageAddress self;
}
