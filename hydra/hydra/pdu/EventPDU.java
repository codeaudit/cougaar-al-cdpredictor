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
  *  5/14/01 Initial version  by IAI
  */

/**
 * Base class for all time-stamped event PDUs.
 *
 * Created on May 14, 2001, 7:21 PM
 */

package org.hydra.pdu ;

/**
 *  Represents an event visible on the plan.
 *
 * @author wpeng
 * @version 0.1
 */
public abstract class EventPDU extends PDU {

    /**  Signifies that the PDU corresponds to a logging directive from the
     *   server.
     */
    public static final int TYPE_DIRECTIVE = -1;
    public static final int TYPE_NONE = 0 ;
    public static final int TYPE_TASK = 1 ;
    public static final int TYPE_ALLOCATION = 2 ;
    public static final int TYPE_ASSET = 3 ;
    public static final int TYPE_EXPANSION = 4 ;
    public static final int TYPE_AGGREGATION = 5 ;
    public static final int TYPE_ALLOCATION_RESULT = 6 ;
    
    public static final String STRING_NONE = "NONE" ;
    public static final String STRING_TASK = "TASK" ;
    public static final String STRING_ALLOCATION = "ALLOCATION" ;
    public static final String STRING_ASSET = "ASSET" ;
    public static final String STRING_EXPANSION = "EXPANSION" ;
    public static final String STRING_AGGREGATION = "AGGREGATION" ;
    public static final String STRING_ALLOCATION_RESULT = "ALLOCATION_RESULT" ;    

    public static final int ACTION_ADD = 0 ;
    public static final int ACTION_REMOVE = 1 ;                  
    public static final int ACTION_CHANGE = 2 ;
    
    public static final String STRING_ADD = "ADD" ;
    public static final String STRING_REMOVE = "REMOVE" ;
    public static final String STRING_CHANGE = "CHANGE" ;
    
    /** The server always as a Symbol ID of 0.
     */
    public static final int SERVER_SID = 0 ;
    
    /** Creates new EventPDU.
     *  @param newType  One of TYPE_DIRECTIVE, TYPE_TASK, TYPE_ALLOCATION, etc.
     *  @param clusterSID The symbol id of the cluster.  (This is redundant?)
     */
    EventPDU( int newType, int newAction, long newExecutionTime, long newTime ) {
        this.type = ( byte ) newType ;
        if ( action < 0 || action > 2 ) {
           throw new IllegalArgumentException() ;   
        }
        this.action = ( byte ) newAction ;
        this.executionTime = newExecutionTime ;
        this.time = newTime ;
    }
    
    public void outputParamString( StringBuffer buf ) {
        super.outputParamString( buf ) ;
        buf.append( ",action=" ).append( actionToString( action ) ) ;
        buf.append( ",time=" ) ;
        formatTimeAndDate( buf, time ) ;
    }
    
    public static String typeToString( int type ) {
        switch ( type ) {
           case TYPE_TASK :
             return STRING_TASK ;
           case TYPE_ALLOCATION :
             return STRING_ALLOCATION ;
           case TYPE_ASSET :
             return STRING_ASSET ;
           case TYPE_EXPANSION :
             return STRING_EXPANSION ;
           case TYPE_AGGREGATION :
             return STRING_AGGREGATION ;
            case TYPE_NONE :
             return STRING_NONE ;
           default :
             return "UNKNOWN" ;
        }
    }

    public static String actionToString( int action ) {
        switch ( action ) {
           case ACTION_ADD :
             return STRING_ADD ;
           case ACTION_CHANGE :
             return STRING_CHANGE ;
           case ACTION_REMOVE :
             return STRING_REMOVE ;
           default :
             return "UNKNOWN" ;
        }
    }
    
    public int getAction() { return action ; }
    
    public long getTime() { return time ; }
    
    public void setTime( long newTime ) { this.time = newTime ; }
    
    public long getExecutionTime() { return executionTime ; }
    
    //public String getCluster() { return cluster ; }
    
    /** Source agent from which the event was logged.  Usually, this will be null
     *  for connection oriented sessions.  However, for store and forward type message transport
     *  layers, it may not be null and will establish the context for any subsequent communications.
     */
    //String cluster ;

    /** Cluster local execution time.*/    
    long executionTime ;
    long time ;
    byte type ;    
    byte action ;
    
    static final long serialVersionUID = 2089973597344075685L;    
}
