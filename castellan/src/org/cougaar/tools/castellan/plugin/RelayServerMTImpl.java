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

import org.cougaar.tools.castellan.pdu.PDUSink;
import org.cougaar.tools.castellan.pdu.PDU;
import org.cougaar.tools.castellan.pdu.DeclarePDU;
import org.cougaar.tools.castellan.ldm.BatchMessage;
import org.cougaar.tools.castellan.ldm.LogMessage;
import org.cougaar.tools.castellan.ldm.WrappedPDUMessage;
import org.cougaar.tools.castellan.ldm.PlanLogConfig;
import org.cougaar.tools.castellan.server.ServerMessageTransport;
import org.cougaar.core.service.BlackboardService;
import org.cougaar.core.service.UIDService;
import org.cougaar.core.relay.Relay;
import org.cougaar.core.agent.ClusterIdentifier;
import org.cougaar.core.plugin.ComponentPlugin;
import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.util.UID;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.util.UnaryPredicate;

import java.util.*;
import java.io.*;

/**
 * Implements a server MT impl.
 */
public class RelayServerMTImpl implements ServerMessageTransport {

    public RelayServerMTImpl( PlanLogConfig config, BlackboardService bs ) {
        this.bs = bs;
        this.config = config ;

        relaySubscription = ( IncrementalSubscription ) bs.subscribe( new UnaryPredicate() {
            public boolean execute(Object o) {
                if ( o instanceof TargetBufferRelay ) {
                    return true ;
                }
                return false ;
            }
        }) ;
    }

    public void start() {
    }

    /**
     * Return a list of clients which have connected with this server.
     */
    public synchronized Vector getClients() {
        return new Vector( buffers ) ;
    }

    public void setPreferences(Map prefs) {
    }

    private TargetBufferRelay getRelayForAgent( String name ) {
        return ( TargetBufferRelay ) nameToRelayMap.get( name ) ;
    }

    public synchronized void execute() {
        // Get the added list and add them to the list of buffers.
        for ( Iterator iter = relaySubscription.getAddedCollection().iterator(); iter.hasNext(); ) {
            TargetBufferRelay relay = ( TargetBufferRelay ) iter.next() ;
            buffers.add( relay  ) ;
            nameToRelayMap.put( relay.getSource().toString(), relay ) ;
        }

        // See what buffers have changed and flush their incoming messages.
        for ( Iterator iter = relaySubscription.getChangedCollection().iterator(); iter.hasNext(); ) {
        // Take incoming messages from the buffer for each response.
            TargetBufferRelay buffer  = ( TargetBufferRelay ) iter.next() ;
            flushIncomingBuffer( buffer );
        }
    }

    private void flushIncomingBuffer ( TargetBufferRelay buffer )
    {
        synchronized ( buffer ) {
            Object[] incoming = buffer.clearIncoming() ;
            for ( int i=0;i<incoming.length;i++) {
                System.out.print("L+" );
                LogMessage lm = ( LogMessage ) incoming[i] ;
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
                                sink.processPDU( p ) ;
                            }
                        }
                    }
                    catch ( Exception e ) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void stop() {
    }

    public void setPDUSink(PDUSink sink) {
        this.sink = sink ;
    }

    /**
     * Send a message to a client.
     * @pdu The pdu to be sent.  Should include a valid destination address.
     */
    public synchronized void sendMessage( PDU pdu ) {
        String dest = pdu.getDestination() ;
        if ( dest == null ) {
            throw new IllegalArgumentException( "No destination address for pdu " + pdu ) ;
        }
        sendWrappedMessage( pdu );
    }

    private void sendOutgoing( LogMessage msg ) {
        TargetBufferRelay relay = getRelayForAgent( msg.getDestination().toString() ) ;
        if ( relay == null ) {
            throw new RuntimeException( "Trying to send pdu " + msg + " to agent " + msg.getDestination()
                    + " with no existing relay." ) ;
        }

        // msg.setSourceAgent( sourceAgent );
        relay.addResponse( msg ) ;
        bs.publishChange( relay ) ;
    }

    private void sendWrappedMessage(PDU pdu) {
        WrappedPDUMessage msg = new WrappedPDUMessage(pdu);
        msg.setDestination( new ClusterIdentifier( pdu.getDestination() ) );
        // Send using the relay.
        sendOutgoing( msg );
    }

    public synchronized void conditionalFlush() {
        if (( System.currentTimeMillis() - lastAddTime ) > maximumDelay) {
            flush();
        }
    }

    public synchronized void flush() {
        for (int i=0;i<buffers.size();i++) {
            flushIncomingBuffer( ( TargetBufferRelay ) buffers.get(i) );
        }
    }

    protected long lastAddTime = 0L;

    /**
     * Maximum delay in seconds before messages are flushed. Default is four seconds.
     */
    protected long maximumDelay = 4000L;

    /**
     * Maximum batch size in bytes. Default value is 24 Kb of messages.
     */
    protected int maxBatchSize = 24000;

    protected PlanLogConfig config ;

    /**
     * Map cluster/agent names to relay.  Non-persistent.
     */
    protected HashMap nameToRelayMap = new HashMap() ;

    /**
     * Actual buffers.  Non-persistent.
     */
    protected ArrayList buffers = new ArrayList() ;

    /**
     * Initialized by parent. Non-persistent.
     */
    protected PDUSink sink ;

    protected BlackboardService bs ;

    protected IncrementalSubscription relaySubscription ;
}
