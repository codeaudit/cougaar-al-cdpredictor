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

package org.cougaar.tools.castellan.plugin;

import org.cougaar.tools.castellan.pdu.PDU;
import org.cougaar.tools.castellan.ldm.LogMessage;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * A Cougaar blackboard object representing both incoming and outgoing LogMessages.  The
 * LogMessageMTImpl writes to and reads from this
 */
public class LogMessageBuffer implements java.io.Serializable
{
    public int getSize() {
        return incoming.size() ;
    }

    /**
     * Incoming messages are characterized by log messages.  The LogServerPlugin will
     * decompress these into PDUs and deliver them appropriately.
     */
    public synchronized void addIncoming( LogMessage msg ) {
        incoming.add( msg ) ;
    }

    /**
     * PDUs are generated by LPs. The LogClientPlugin will translate these into LogMessages
     * for subsequent delivery using a Relay approach.
     */
    public synchronized void addOutgoing( LogMessage msg ) {
        outgoing.add( msg ) ;
    }

    public ArrayList getOutgoing() {
        return outgoing ;
    }

    public ArrayList getIncoming() {
        return incoming ;
    }

    public synchronized void clearIncoming() {
        incoming.clear();
    }

    public synchronized void clearOutgoing() {
        outgoing.clear() ;
    }

    ArrayList outgoing = new ArrayList( 50 ) ;
    ArrayList incoming = new ArrayList( 80 ) ;
}
