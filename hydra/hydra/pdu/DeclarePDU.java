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
  *  8/15/01 Initial version  by IAI
  */


/**
 * Intializes the logging session between a client plugIn and the server.
 *
 * Created on June 15, 2001, 5:07 PM
 */

package org.hydra.pdu;

/**
 *  Used to begin a session (in connected oriented systems), or
 *  to identify a bundle of PDUs in a message oriented logging system.
 *
 * @author  wpeng
 * @version 
 */
public class DeclarePDU extends PDU {

    /** Creates new DeclarePDU. */
    public DeclarePDU(String source) {
        this.source = source ;
    }
    
    public String getName() { return source ; }
    
    public void outputParamString( StringBuffer buf ) {
        super.outputParamString( buf ) ;
        buf.append( ',' ).append( "source=" ).append( source ) ;
    }

    String source ;
    
    static final long serialVersionUID = -3536190749283243392L;    
}
