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
  *  7/25/01 Initial version  by Penn State/IAI
  */

/*
 * AllocationPDU.java
 *
 * Created on July 25, 2001, 10:42 AM
 */

package org.hydra.pdu;

/**
 *
 * @author  wpeng
 * @version 
 */
public class AllocationPDU extends PlanElementPDU {

    /** Creates new AllocationPDU */
    public AllocationPDU( UIDPDU task, UIDPDU asset, UIDPDU allocTask, UIDPDU uid, int newAction, long executionTime, long time ) 
    {
        super( uid, EventPDU.TYPE_ALLOCATION, newAction, executionTime, time ) ;
        this.task = task ;
        this.asset = asset ;
        this.allocTask = allocTask ;
    }
    
    public void outputParamString( StringBuffer buf ) {
        super.outputParamString( buf ) ;
        buf.append( ",task=" ) ;
        format( buf, task ) ;
        buf.append( ",asset=" ) ;
        format( buf, asset ) ;
    }
    
    public UIDPDU getTask() { return task ; }
    
    public UIDPDU getAsset() { return asset ; }
    
    public UIDPDU getAllocTask() { return allocTask ; }

    protected UIDPDU task ;
    
    /** Asset to which task is allocated. */
    protected UIDPDU asset ;
    
    /** Non-null if the asset is organizational. */
    protected UIDPDU allocTask ;  // This is used for tasks which are allocated to clusters.
    
    static final long serialVersionUID = -6437064842461008264L;    
}
