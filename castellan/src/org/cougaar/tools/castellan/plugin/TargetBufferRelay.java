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

import org.cougaar.core.relay.Relay;
import org.cougaar.core.util.UID;
import org.cougaar.core.mts.MessageAddress;

import java.util.Set;
import java.util.ArrayList;
import java.io.Serializable;

/**
 *  This is created on the PlanLogAgent side by the relay declared by the client.
 */
public class TargetBufferRelay implements Relay.Target, java.io.Serializable {

    public TargetBufferRelay(UID uid, MessageAddress source, Object c ) {
        this.uid = uid;
        this.source = source;
        addIncoming( ( Object[] ) c ) ;
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

    protected void addIncoming( Object[] ics ) {
        for ( int i=0;i<ics.length;i++) {
            content.add( ics[i] ) ;
        }
    }

    /**
     * Called by plug-ins to return one or more responses to the buffer.
     */
    public void addResponse( Serializable o ) {
        responses.add( o ) ;
    }

    /**
     * Get the incoming objects on the buffer.
     */
    public Object[] clearIncoming() {
        Object[] incoming = new Object[ content.size() ] ;
        for (int i=0;i<content.size();i++) {
            incoming[i] = content.get(i) ;
        }
        content.clear();
        return incoming ;
    }

    /**
     * Returns an array of objects which are responses.
     */
    public Object getResponse() {
        Object[] resp = new Object[ responses.size() ] ;
        for (int i=0;i<responses.size();i++) {
            resp[i] = responses.get(i) ;
        }
        responses.clear();
        return resp ;
    }

    public int updateContent(Object o, Relay.Token token) {
        addIncoming( ( Object[] ) o );
        return Relay.CONTENT_CHANGE ;
    }

    protected UID uid ;
    protected MessageAddress source ;
    protected ArrayList content = new ArrayList( 500 ) ;
    protected ArrayList responses = new ArrayList( 50 ) ;
}
