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
  */

package org.cougaar.tools.castellan.pdu;

public class ExecutionPDU extends PDU
{
    public ExecutionPDU( String clusterIdentifier, String plugInName, int plugInHash,
                         long seqNumber, long transactionId, long startTime, long stopTime )
    {
        this.clusterIdentifier = clusterIdentifier;
        this.plugInName = plugInName;
        this.plugInHash = plugInHash;
        this.seqId = seqNumber ;
        this.transactionId = transactionId ;
        this.startTime = startTime;
        this.stopTime = stopTime;
    }

    public long getElapsedTime() {
        return stopTime - startTime ;
    }

    public long getStartTime()
    {
        return startTime;
    }

    public long getStopTime()
    {
        return stopTime;
    }

    public String getClusterIdentifier()
    {
        return clusterIdentifier;
    }

    public String getPlugInName()
    {
        return plugInName;
    }

    public int getPlugInHash()
    {
        return plugInHash;
    }

    public long getSeqId()
    {
        return seqId;
    }

    public long getTransactionId()
    {
        return transactionId;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer() ;
        buf.append( "[Execution " ).append( clusterIdentifier ).append( '/' ).append( plugInName ) ;
        buf.append( ",transid=" ).append( transactionId ) ;
        buf.append( ",start=") ;
        EventPDU.formatTime( buf, startTime );
        buf.append( ",end=" ) ;
        EventPDU.formatTime( buf, stopTime );
        buf.append( ",elapsed=" );
        EventPDU.formatElapsedTime( buf, stopTime - startTime ) ;
        buf.append( ']' ) ;
        return buf.toString() ;
    }

    protected String clusterIdentifier ;
    protected String plugInName ;
    protected int plugInHash ;
    protected long seqId ;
    protected long transactionId ;
    protected long startTime, stopTime ;
    protected static final long serialVersionUID = 3897690940363528222L;
}
