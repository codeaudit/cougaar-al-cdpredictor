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

package org.cougaar.tools.castellan.pdu ;

import java.io.*;

/**
 *  Represents an event visible at an agent.
 *
 * @author wpeng
 * @version 0.1
 */
public abstract class EventPDU extends PDU implements EventPDUConstants {

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

    public long getTransactionId()
    {
        return transactionId;
    }

    public void setTransactionId(long transactionId)
    {
        this.transactionId = transactionId;
    }

    public void outputParamString( StringBuffer buf ) {
        super.outputParamString( buf ) ;
        buf.append( ",action=" ).append( actionToString( action ) ) ;
        buf.append( ",source=" ).append( source ) ;
        buf.append( ",time=" ) ;
        formatTimeAndDate( buf, time ) ;
        buf.append( ",transid=" ).append( transactionId );
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
           case TYPE_ALLOCATION_RESULT :
             return STRING_ALLOCATION_RESULT ;
           case TYPE_DECLARE_AGENT :
             return STRING_DECLARE_AGENT ;
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

    /**
     * Fill in which agent this event pdu was emitted from.
     */
    public void setSource(String source)
    {
        this.source = source;
    }

    /**
     * To be filled in by the infrastructure.
     */
    public String getSource()
    {
        return source;
    }

    public byte getType() {
        return type;
    }

    public int getAction() { return action ; }

    public long getTime() { return time ; }

    public void setTime( long newTime ) { this.time = newTime ; }

    public long getExecutionTime() { return executionTime ; }

    private void writeObject( ObjectOutputStream out ) throws IOException
    {
        out.defaultWriteObject() ;
        out.writeLong( executionTime );
        out.writeLong( time ) ;
        out.writeByte( type ) ;
        out.writeByte( action ) ;
        // Write the source
        if ( source != null ) {
            out.writeByte( (byte) 1 );
            out.writeUTF( source );
        }
        else {
            out.writeByte( (byte) 0 );
        }
    }

    private void readObject( ObjectInputStream in ) throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        executionTime = in.readLong() ;
        time = in.readLong() ;
        type = in.readByte() ;
        action = in.readByte() ;
        byte b = in.readByte() ;
        if ( b == 1 ) {
            source = in.readUTF() ;
        }
    }
    //public String getCluster() { return cluster ; }

    /** Source agent from which the event was logged.  Usually, this will be null
     *  for connection oriented sessions.  However, for store and forward type message transport
     *  layers, it may not be null and will establish the context for any subsequent communications.
     */
    //String cluster ;

    /** Cluster local execution time.*/
    transient long executionTime ;
    transient long time ;
    transient byte type ;
    transient byte action ;

    /** To be filled in by the infrastruture upon receiving the message. */
    transient String source ;

    /** The transaction in which this event was emitted, or -1. */
    long transactionId = -1;

    static final long serialVersionUID = 2089973597344075685L;
}
