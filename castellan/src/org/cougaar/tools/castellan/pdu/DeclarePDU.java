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
  */

package org.cougaar.tools.castellan.pdu;

/**
 *  Used to begin a session (in connected oriented systems). Also used to indicate
 *  the beginning of logging for a particular agent.  This has been changed into a
 *  EventPDU.
 *
 * @author  wpeng
 */
public class DeclarePDU extends EventPDU {

    public DeclarePDU( String source ) {
        super( 0, 0, 0, 0 ) ;
        throw new UnsupportedOperationException( "Not supported." ) ;
    }

    /** Creates new DeclarePDU. */
    public DeclarePDU(String nodeIdentifer, String source, long executionTime, long time ) {
        super( EventPDU.TYPE_NONE, EventPDU.ACTION_ADD, executionTime, time ) ;
        this.source = source ;
        this.nodeIdentifier = nodeIdentifer ;
    }

    public String getName() { return source ; }

    public void outputParamString( StringBuffer buf ) {
        super.outputParamString( buf ) ;
        buf.append( ",source=" ).append( source ).append( ",nodeIdentifier=" ).append( nodeIdentifier ) ;
    }

    public String getSource() {
        return source;
    }

    public String getNodeIdentifier() {
        return nodeIdentifier;
    }

    /**
     * Name of the agent.
     */
    protected String source ;

    /**
     * String identifying node such as IpAddress.
     */
    protected String nodeIdentifier ;

    static final long serialVersionUID = -3536190749283243392L;
}
