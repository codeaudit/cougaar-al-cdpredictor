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
  *  6/12/01 Initial version  by Penn State/IAI
  */

package org.hydra.pdu;
import java.util.* ;
import java.text.* ;

/**
 * PDU from the server to the client sending a request for local time base.
 *
 */
public class TimeRequestPDU extends PDU {

    /** Creates new TimeRequestPDU. */
    public TimeRequestPDU(int seqId, long time) {
        this.seqId = seqId ;
        this.time = time ;
    }
    
    public int hashCode() { return seqId; }
    
    public boolean equals( Object o ) {
       if ( o == this ) return true ;
       return ( ( ( TimeRequestPDU ) o ).getSeqId() == getSeqId() ) ;
    }
    
    
    public void outputParamString( StringBuffer buf ) {
        super.outputParamString( buf ) ;
        buf.append( ",seqId=" ).append( seqId ) ;
        buf.append( ",time=" ) ;
        formatTimeAndDate( buf, time ) ;
    }
    
    public int getSeqId() { return seqId ; }
    
    public long getTime() { return time ; }
 
    int seqId ;
    
    /** Server-side time. */
    long time ;
    
    static final long serialVersionUID = -3684858332870909532L;
}
