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
import org.cougaar.core.service.BlackboardService;
import org.cougaar.core.relay.Relay;
import org.cougaar.core.agent.ClusterIdentifier;

import java.util.Map;
import java.util.ArrayList;
import java.io.*;

/**
 * Implements a client MT impl which uses a LogMessage buffer on the blackboard.
 */
public class BlackboardBufferClientMTImpl implements ClientMessageTransport {

    public BlackboardBufferClientMTImpl( PlanLogConfig config, BlackboardService bs ) {
        this.bs = bs;
        this.config = config ;
        buffer = new LogMessageBuffer() ;
        bs.publishAdd( buffer ) ;

        try {
            oos = new ObjectOutputStream( bas ) ;
        }
        catch ( Exception e ) {
        }
    }

    public void setPreferences(Map prefs) {
    }

    public void execute() {
        // Take incoming messages from the buffer for each incoming message.
        synchronized ( buffer ) {
            ArrayList incoming = buffer.getIncoming() ;
            for ( int i=0;i<incoming.size();i++) {
                LogMessage lm = ( LogMessage ) incoming.get(i) ;
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

            // Always clear this.
            buffer.clearIncoming();
        }
    }

    public boolean connect() {

        return true ;
    }

    public boolean isConnected() {
        return false;
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
        msg.setDestination( new ClusterIdentifier( config.getLogCluster() ) );
        buffer.addOutgoing( msg ) ;
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
            sendOutgoing( bm );
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    protected long lastAddTime = 0L;

    /**
     * Maximum delay in seconds before messages are flushed. Default is 10 seconds.
     */
    protected long maximumDelay = 8000L;

    /**
     * Maximum batch size in bytes. Default is 16kb.
     */
    protected int maxBatchSize = 24000;

    protected PlanLogConfig config ;
    protected LogMessageBuffer buffer ;
    /**
     * The current target cluster.
     */
    protected String targetCluster ;
    protected ByteArrayOutputStream bas = new ByteArrayOutputStream( maxBatchSize ) ;
    protected ObjectOutputStream oos ;
    protected PDUSink sink ;
    protected BlackboardService bs ;
}
