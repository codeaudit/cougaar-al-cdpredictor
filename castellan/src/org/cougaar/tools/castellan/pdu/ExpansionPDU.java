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
 * ExpansionPDU.java
 *
 * Created on August 15, 2001, 2:46 PM
 */

package org.cougaar.tools.castellan.pdu;
import java.util.* ;

/**
 *
 * @author  wpeng
 * @version 
 */
public class ExpansionPDU extends PlanElementPDU {

    public ExpansionPDU( UIDPDU parentTask, UIDPDU[] tasks,
       UIDPDU uid, int action, long executionTime, long time )
    {
       super( uid, EventPDU.TYPE_EXPANSION, action, executionTime, time ) ;
       this.parentTask = parentTask ;
       if ( tasks != null ) {
          this.tasks = ( UIDPDU[] ) tasks.clone() ;
       }
    }
    
    public UIDPDU[] getTasks() { return ( UIDPDU[] ) tasks.clone() ; }
    
    public UIDPDU getParentTask() { return parentTask ; }
    
    public int getNumTasks() {
        if ( tasks == null ) return 0 ;
        return tasks.length ; 
    }
    
    public UIDPDU getTask( int i ) {
        if ( tasks == null ) throw new NoSuchElementException() ;
        return tasks[i] ;
    }
    
    public void outputParamString( StringBuffer buf ) {
       super.outputParamString( buf ) ;
       buf.append( ",parent=" );
       format( buf, parentTask ) ;
       buf.append( ',' ) ;
       buf.append( "#children=" ) ;
       if ( tasks != null ) {
          buf.append( tasks.length ) ;
       }
       else {
          buf.append( '0' ) ;
       }
    }

    UIDPDU parentTask ;
    UIDPDU[] tasks ;
    
    static final long serialVersionUID = 8899840247479548367L;    
}
