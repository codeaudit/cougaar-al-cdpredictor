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

import java.util.Set;
import java.util.ArrayList;
import java.io.Serializable;

/**
 *  This is created on the sending side by the relay declared by the client.
 */
public class TargetBufferRelay implements Relay.Target, Serializable {
    private GenericRelayMessageTransport gmrt;

    public TargetBufferRelay(UID uid, MessageAddress source, Object c ) {
        this.uid = uid;
        this.source = source;
        addIncoming( ( Object[] ) c ) ;
    }

    public GenericRelayMessageTransport getGmrt()
    {
        return gmrt;
    }

    public void setRelayManager(GenericRelayMessageTransport gmrt)
    {
        this.gmrt = gmrt;
    }

    public UID getUID() {
        return uid ;
    }

    public MessageAddress getSource() {
        return source ;
    }

    public String toString() {
        return "<Target Buffer, source=" + getSource() + ">" ;
    }

    public void setUID(UID uid) {
        throw new RuntimeException( "Cannot set UID at runtime." ) ;
    }

    /**
     * Handles groups of incoming data.
     * @param ics
     */
    protected void addIncoming( Object[] ics ) {
        if ( gmrt != null ) {
            for ( int i=0;i<ics.length;i++) {
                gmrt.addMessage( ics[i], getSource() );
            }
        }
        else {
            for ( int i=0;i<ics.length;i++) {
                content.add( ics[i] ) ;
            }
        }
    }

    /**
     * Whether anyone has added responses since the logic provider processed
     * all the responses and forwarded them off to the recipient.
     * @return
     */
    public boolean isResponseChanged() {
        return responseChanged;
    }

    /**
     * Called by plug-ins to return one or more responses to the buffer.
     */
    public synchronized void addResponse( Serializable o ) {
        responses.add( o ) ;
        responseChanged = true ;
    }

    /**
     * Get the incoming objects on the buffer.
     */
    public synchronized Object[] clearIncoming() {
        Object[] incoming = new Object[ content.size() ] ;
        for (int i=0;i<content.size();i++) {
            incoming[i] = content.get(i) ;
        }
        content.clear();
        return incoming ;
    }

    /**
     * Returns an array of objects which are responses. This is called by the
     * logic provider only!
     */
    public synchronized Object getResponse() {
        Object[] resp = new Object[ responses.size() ] ;
        for (int i=0;i<responses.size();i++) {
            resp[i] = responses.get(i) ;
        }
        responses.clear();
        responseChanged = false ;
        return resp ;
    }

    public int updateContent(Object o, Relay.Token token) {
        // System.out.println("updateContent() called on " + this);
        addIncoming( ( Object[] ) o );
        return Relay.CONTENT_CHANGE ;
    }

    protected boolean responseChanged = false ;
    protected UID uid ;
    protected MessageAddress source ;
    protected ArrayList content = new ArrayList( 500 ) ;
    protected ArrayList responses = new ArrayList( 500 ) ;
}
