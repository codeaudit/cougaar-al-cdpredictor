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
import org.cougaar.core.service.BlackboardService;
import org.cougaar.core.service.UIDService;
import org.cougaar.core.relay.Relay;
import org.cougaar.core.agent.ClusterIdentifier;
import org.cougaar.core.plugin.ComponentPlugin;
import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.util.UID;

import java.util.Map;
import java.util.ArrayList;
import java.io.*;

/**
 * Implements a client MT impl which uses a SourceBufferRelay to communicate with a server.
 */
public class RelayClientMTImpl implements ClientMessageTransport {

    public RelayClientMTImpl( PlanLogConfig config, BlackboardService bs, UID uid, ClusterIdentifier source, PlanLogStats stats ) {
        this.bs = bs;
        this.config = config ;
        this.source = source ;
        this.stats = stats ;

        buffer = new SourceBufferRelay( uid,
                        new ClusterIdentifier( config.getLogCluster() ), source ) ;
        bs.publishAdd( buffer ) ;

        try {
            oos = new ObjectOutputStream( bas ) ;
        }
        catch ( Exception e ) {
        }
    }

    public void setPreferences(Map prefs) {
    }

    public synchronized void execute() {
        // Take incoming messages from the buffer for each response.
        synchronized ( buffer ) {
            Object[] incoming = buffer.clearReponses() ;
            for ( int i=0;i<incoming.length;i++) {
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

            // Flush the buffer
            conditionalFlush();
        }
    }

    public boolean connect() {
        return true ;
    }

    public boolean isConnected() {
        return true ;
    }

    public void stop() {
    }

    public Object getServer() {
        return null;
    }

    public void setPDUSink(PDUSink sink) {
        this.sink = sink ;
    }

    public synchronized void sendMessage(PDU pdu) {
        if (pdu instanceof DeclarePDU) {
            flush();
            sendWrappedMessage(pdu);
        }
        else {
            try {
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

    private void sendOutgoing( LogMessage msg ) {
        msg.setSourceAgent( source.cleanToString() );
        msg.setDestination( new ClusterIdentifier( config.getLogCluster() ) );
        buffer.addOutgoing( msg ) ;
        if ( stats != null ) {
            stats.setNumMsgsSent( stats.getNumMsgsSent() + 1 ) ;
        }
        bs.publishChange( buffer ) ;
    }

    private void sendWrappedMessage(PDU pdu) {
        WrappedPDUMessage msg = new WrappedPDUMessage(pdu);
        // Send using the relay.
        sendOutgoing( msg );
    }

    public synchronized void conditionalFlush() {
        if (( System.currentTimeMillis() - lastAddTime ) > maximumDelay) {
            flush();
        }
    }

    public synchronized void flush() {
        System.out.print("F+");
        lastAddTime = System.currentTimeMillis();
        if (bas.size() <= 4) {
            return;
        }

        try {
            // Make a copy of the byte array.
            byte[] ba = bas.toByteArray();
            bas.reset();
            if ( stats != null ) {
                stats.setNumBytesSent( stats.getNumBytesSent() + ba.length );
            }

            oos.close() ;
            oos = new ObjectOutputStream(bas);
            BatchMessage bm = new BatchMessage(ba, BatchMessage.SERIALIZED, false);
            sendOutgoing( bm );
        }
        catch (Exception e) {
            e.printStackTrace();
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
    protected int maxBatchSize = 48000;

    protected PlanLogStats stats ;
    protected PlanLogConfig config ;
    protected SourceBufferRelay buffer ;

    protected ClusterIdentifier source ;

    protected ByteArrayOutputStream bas = new ByteArrayOutputStream( maxBatchSize ) ;
    protected ObjectOutputStream oos ;
    protected PDUSink sink ;
    protected BlackboardService bs ;
}
