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

package org.cougaar.tools.castellan.ldm;

import org.cougaar.tools.castellan.plugin.*;
import org.cougaar.tools.castellan.pdu.*;
import org.cougaar.core.blackboard.*;
import org.cougaar.core.agent.*;
import org.cougaar.core.service.*;
import org.cougaar.util.*;
import org.cougaar.planning.ldm.plan.*;

import java.util.*;
import java.io.*;

/**
 * Implements the ClientMessageTransport for the PlanEventLogLP.
 *
 */

public class BlackboardMTForPlanEventLogLP implements ClientMessageTransport {
    public BlackboardMTForPlanEventLogLP(LogPlanServesLogicProvider logPlan, ClusterServesLogicProvider cluster) {
        this.logPlan = logPlan;
        this.cluster = cluster;

        // Configure batch size and object output stream
        try {
            bas = new ByteArrayOutputStream(maxBatchSize + 3000);
            oos = new ObjectOutputStream(bas);
        }
        catch (Exception e) {

        }

    }

    public void setPreferences(Map prefs) {
    }

    /**
     *  This will be called by the PlanEventLog logic provider for
     *  every execute. As a result, it may be slightly inefficient.
     */
    public void execute() {
        if (( cluster.currentTimeMillis() - lastAddTime ) > maximumDelay) {
            flush();
        }
    }

    public boolean connect() {
        return true;
    }

    public boolean isConnected() {
        return true;
    }

    public void stop() {
    }

    public Object getServer() {
        return null;
    }

    public void handleIncomingMessages(LogMessage lm) {
//        System.out.println("RECEIVING " + lm );
        if (lm instanceof WrappedPDUMessage) {
            WrappedPDUMessage wpm = (WrappedPDUMessage) lm;
            PDU pdu = wpm.getPDU();
            String sa = lm.getSourceAgent();
            if (sa != null) {
                sa = sa.intern();
            }
            pdu.setSource(sa);
            sink.processPDU(pdu);
        }
        else if (lm instanceof BatchMessage) {
            // Decompress each batch message.
            BatchMessage bm = ( BatchMessage ) lm ;

            try {
                ByteArrayInputStream bis = new ByteArrayInputStream( bm.getByteArray() ) ;
                ObjectInputStream ois = new ObjectInputStream( bis ) ;
                while ( bis.available() > 0 ) {
                    Object o = ois.readObject() ;

                    if ( o instanceof PDU ) {
                        PDU p = ( PDU ) o ;
                        String sa = lm.getSourceAgent() ;
                        if ( sa != null ) {
                            sa.intern() ;
                        }
                        p.setSource( sa ) ;
//                        if ( p instanceof TaskPDU ) {
//                            System.out.println("Receiving " + p );
//                        }
                        sink.processPDU( p ) ;
                    }
                }
            }
            catch ( Exception e ) {
                e.printStackTrace();
            }

            /*
            for (Iterator iter1 = ( (BatchMessage) lm ).getIterator() ; iter1.hasNext() ;) {
                PDU pdu = (PDU) iter1.next();
                String sa = lm.getSourceAgent();
                if (sa != null) {
                    sa = sa.intern();
                }
                pdu.setSource(sa);
                if (pdu != null) {
                    sink.processPDU(pdu);
                }
            }
            */
        }
    }

    public void setLoggingDestination(String destination) {
        targetCluster = new ClusterIdentifier(destination);
        flush();
    }

    public void setPDUSink(PDUSink sink) {
        this.sink = sink;
    }

    public void sendMessage(PDU pdu) {

        if (pdu instanceof DeclarePDU) {
            flush();
            sendWrappedMessage(pdu);
        }
        else {

            try {
//                if ( pdu instanceof TaskPDU ) {
//                    System.out.println("Writing " + pdu );
//                }
                oos.writeObject(pdu);
                oos.flush();
                if (bas.size() > maxBatchSize) {
                    flush();
                }
                else {
                    conditionalFlush();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendWrappedMessage(PDU pdu) {
        WrappedPDUMessage msg = new WrappedPDUMessage(pdu);
        if (logPlan != null && targetCluster != null) {
            flush();
            msg.setDestination(targetCluster);
            msg.setSourceAgent(cluster.getClusterIdentifier().toString());
            logPlan.sendDirective(msg);
        }
        else {
            outList.add(msg);
        }

    }

    public void conditionalFlush() {
        if (( System.currentTimeMillis() - lastAddTime ) > maximumDelay) {
            flush();
        }
    }

    public void flush() {
        lastAddTime = System.currentTimeMillis();
        if (bas.size() <= 4) {
            return;
        }

        try {
            // Make a copy of the byte array.
            byte[] ba = bas.toByteArray();
            bas.reset();
            //oos.close() ;
            oos = new ObjectOutputStream(bas);

            BatchMessage bm = new BatchMessage(ba, BatchMessage.SERIALIZED, false);
            if (logPlan != null && targetCluster != null) {
                // Clear the outlist.
                if (outList.size() > 0) {
                    for (int i = 0 ; i < outList.size() ; i++) {
                        LogMessage d = (LogMessage) outList.get(i);
                        d.setDestination(targetCluster);
                        d.setSourceAgent(cluster.getClusterIdentifier().cleanToString());
                        logPlan.sendDirective(d);
                    }
                    outList.clear();
                }

                // Add the batch message directly to the plan.
                bm.setSourceAgent(cluster.getClusterIdentifier().cleanToString());
                bm.setDestination(targetCluster);
                logPlan.sendDirective(bm);
            }
            else {
                outList.add(bm);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    LogPlanServesLogicProvider logPlan;
    ClusterServesLogicProvider cluster;
    PDUSink sink;
    ClusterIdentifier targetCluster;

    long lastAddTime = 0L;

    /**
     * Maximum delay in seconds before messages are flushed. Default is 10 seconds.
     */
    long maximumDelay = 10000L;

    /**
     * Maximum batch size in bytes. Default is 16kb.
     */
    int maxBatchSize = 24000;
    ObjectOutputStream oos;
    ByteArrayOutputStream bas = new ByteArrayOutputStream();

    /**
     * Temporary buffer for outgoing LogMessages. Buffer messages until the
     * destination cluster is available.
     */
    ArrayList outList = new ArrayList(32);

}
