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
  *  8/14/01 Initial version  by Penn State/IAI
  */

/*
 * AggregationPDU.java
 *
 * Created on August 14, 2001, 1:20 PM
 */

package org.hydra.pdu;

/**
 *
 * @author  wpeng
 * @version
 */
public class AggregationPDU extends PlanElementPDU {
    
    /** Creates new AggregationPDU */
    public AggregationPDU(UIDPDU mpTask, UIDPDU task, UIDPDU uid,
        int action, long executionTime, long time ) 
    {
        super( uid, EventPDU.TYPE_AGGREGATION, action, executionTime, time ) ;
        this.mpTask = mpTask ;
        this.task = task ;
    }
    
    public void outputParamString( StringBuffer buf ) {
        super.outputParamString( buf ) ;
        buf.append( ",MPTask=" ).append( mpTask ).append( ',' ) ;
        buf.append( "task=" ).append( task ) ;
    }
    
    /** The task which the parent task is combined into. */
    public UIDPDU getCombinedTask() { return mpTask ; }
    
    public UIDPDU getTask() { return task ; }
    
    UIDPDU task ;
    UIDPDU mpTask ;
    
    static final long serialVersionUID = -7446233136790940410L;    
}
