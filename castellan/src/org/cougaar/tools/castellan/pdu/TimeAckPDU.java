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

/*
 * TimeAckPDU.java
 *
 * Created on June 12, 2001, 3:05 PM
 */

package org.cougaar.tools.castellan.pdu;
import java.util.* ;
import java.text.* ;

/**
 * Message from client to server with the agent local time. This is sent in
 * response to receiving <code>TimeRequestPDU</code>.
 *
 * @author  wpeng
 * @version 
 */
public class TimeAckPDU extends PDU {

    /** Creates new TimeAckPDU */
    public TimeAckPDU( int seqId, long time, int estProcTime) {
        // this.srcSID = srcSID ;
        this.seqId = seqId ;
        this.time = time ;
        this.estProcTime = estProcTime ;
    }
    
    protected void outputParamString( StringBuffer buf ) {
        super.outputParamString( buf ) ;
        buf.append( ",seqId=").append(seqId).append(",time=" ) ;
        formatTimeAndDate( buf, time ) ;
    }
    
    public long getTime() { return time ; }

    public int getSeqId() { return seqId ; }
    
    /** How much time (estimated) it took to service this request. Affects
     * the skew of the clock.  May be -1 to indicate no knowledge of this.
     */
    int estProcTime = -1;
    
    /** Agent-local time.
     */
    long time ;
    int seqId ;
    // int srcSID ;

    static final long serialVersionUID = 2937381327468446792L;    
}