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
package org.cougaar.cpe.relay;

import org.cougaar.core.relay.Relay;
import org.cougaar.core.util.UID;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.tools.techspecs.events.MessageEvent;

import java.io.Serializable;
import java.util.Set;
import java.util.ArrayList;
import java.util.Collections;

public class SourceBufferRelay implements Relay.Source, Serializable {
    private MessageAddress target;
    private String tag;
    private int numResponses = 0 ;
    private int numSent;
    private int numReceived;
    private int maxSequenceNumber ;

    /**
     * Signifies queries from the LP to empty relay content.
     */
    private int numQueries ;

    public SourceBufferRelay(UID uid, MessageAddress target, MessageAddress source) {
        targets = Collections.singleton(target);
        this.source = source;
        this.target = target ;
        this.uid = uid;
    }

    public SourceBufferRelay(String tag, UID uid, MessageAddress target, MessageAddress source) {
        targets = Collections.singleton(target);
        this.source = source;
        this.target = target ;
        this.uid = uid;
        this.tag = tag ;
    }

    public String getTag() {
        return tag;
    }

    /**
     * The maximum sequence number received.
     * @return
     */
    public int getMaxSequenceNumber()
    {
        return maxSequenceNumber;
    }

    public int getBufferSize() {
        return outgoing.size() ;
    }

    public int getNumResponses() {
        return numResponses;
    }

    public int getNumSent() {
        return numSent;
    }

    public int getNumReceived() {
        return numReceived;
    }

    public int getNumQueries() {
        return numQueries;
    }

    public GenericRelayMessageTransport getGmrt()
    {
        return gmrt;
    }

    public void setRelayManager(GenericRelayMessageTransport gmrt)
    {
        this.gmrt = gmrt;
    }

    public String toString() {
        return ("< Source Buffer, target=" + getTargets() + ", source= " + source + ", #outgoing=" + outgoing.size() + "> " );
    }

    public Set getTargets() {
        return targets;
    }

    public UID getUID() {
        return uid;
    }

    public MessageAddress getSource() {
        return source;
    }

    public MessageAddress getTarget() {
        return target ;
    }

    public boolean isOutgoingChanged() {
        return isOutgoingChanged;
    }

    public Object getContent() {
        // System.out.println("SourceBufferRelay::getContent() called on " + this );
        Object[] c = outgoing.toArray() ;
        outgoing.clear();
        numQueries++ ;
        isOutgoingChanged = false ;
        return c ;
    }

    public void setUID(UID uid) {
        throw new RuntimeException("Trying to set uid on existing relay.");
    }

    public Relay.TargetFactory getTargetFactory() {
        return SimpleRelayFactory.INSTANCE;
    }

    static final Object[] nullArray = new Object[0] ;

    /**
     * Get responses.  This is called by an application or MessageTransport to receive events from
     * this relay.
     */
    public synchronized Object[] clearReponses() {
        if ( responses.size() == 0 ) {
            return nullArray ;
        }

        Object[] result = new Object[responses.size()];
        for (int i = 0 ; i < responses.size() ; i++) {
            result[i] = responses.get(i);
        }
        responses.clear();
        return result;
    }

    public void addOutgoing( Serializable o ) {
        outgoing.add( o ) ;
        if (o instanceof MessageEvent) {
            MessageEvent cmsg = (MessageEvent) o;
            cmsg.setSeqId( numSent ) ;
        }
        numSent ++ ;

        isOutgoingChanged = true ;
    }

    protected void processReponse(Object[] resp) {
        if ( gmrt != null ) {
            for (int i = 0 ; i < resp.length ; i++) {
                gmrt.addMessage( resp[i], getTarget() ) ;
                numReceived ++ ;
                if ( resp[i] instanceof MessageEvent ) {
                    MessageEvent event = (MessageEvent) resp[i] ;
                    if ( event.getSeqId() > maxSequenceNumber ) {
                        maxSequenceNumber = event.getSeqId();
                    }
                }
            }
        }
        else {
            for (int i = 0 ; i < resp.length ; i++) {
                responses.add(resp[i]);
                numReceived ++ ;
                if ( resp[i] instanceof MessageEvent ) {
                    MessageEvent event = (MessageEvent) resp[i] ;
                    if ( event.getSeqId() > maxSequenceNumber ) {
                        maxSequenceNumber = event.getSeqId();
                    }
                }
            }
        }
    }

    public int updateResponse(MessageAddress address, Object o) {
        numResponses ++ ;
        processReponse((Object[]) o);
        return Relay.RESPONSE_CHANGE ;
    }

    private static final class SimpleRelayFactory implements TargetFactory, Serializable {
        public static final SimpleRelayFactory INSTANCE =
                new SimpleRelayFactory();

        private SimpleRelayFactory() {
        }

        public Relay.Target create(
                UID uid,
                MessageAddress source,
                Object content,
                Token token) {
            return new TargetBufferRelay( uid, source, content);
        }

        private Object readResolve() {
            return INSTANCE;
        }
    };

    protected GenericRelayMessageTransport gmrt ;
    protected boolean isOutgoingChanged = false ;
    protected ArrayList outgoing = new ArrayList( 50 );
    protected ArrayList responses = new ArrayList( 50 );
    protected UID uid;
    protected MessageAddress source;
    protected Set targets;
}
