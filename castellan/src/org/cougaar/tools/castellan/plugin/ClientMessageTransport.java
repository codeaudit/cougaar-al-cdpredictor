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
  *  6/12/01 Initial version by IAI
  */

package org.cougaar.tools.castellan.plugin;
import org.cougaar.tools.castellan.pdu.* ;
import java.util.* ;

/**
 * The interface for client-side message transport.  The Client plug-in
 * will run in the agent's main thread, but the message handling may have its own
 * higher priority thread to avoid overrunning input/output buffers.
 * 
 * @author  wpeng
 * @version 
 */
public interface ClientMessageTransport {
    
    public void setPreferences( Map prefs ) ;

    /**
     * Called periodically if this ClientMessageTransport is embedded within
     * a plug-in.
     */
    public void execute() ;

    /** Initial the connection between the server and the client.
    */
    public boolean connect() ;

    public boolean isConnected() ;

    /** Stop the session.
    */
    public void stop() ;

    /** Returns a description of the server.
    */
    public Object getServer() ;

    public void setPDUSink( PDUSink sink ) ;

    public void sendMessage( PDU pdu ) ;

    public void flush() ;
}
