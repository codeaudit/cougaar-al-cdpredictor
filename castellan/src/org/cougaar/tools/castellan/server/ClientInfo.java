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
  * </copyright>
  *
  * CHANGE RECORD
  *  3/27/02 Initial version by IAI
  */

package org.cougaar.tools.castellan.server;

/**
 * Represents a bundle of client info and statistics.
 */
public class ClientInfo
{

    /**
     * Name of the cluster.
     */
    String clusterName ;

    boolean isClient ;

    /**
     * Whether clock skew is being estimated relative to the server.
     */
    boolean isTrackingSkew ;

    /**
     * Estimated clock skew in ms if skew is being tracked.
     */
    long skew ;

    /**
     * Number of messages
     */
    long numMessagesSent ;

    long numPDUSent ;

    long numMessagesReceived ;

    long numPDUReceived ;

    long numBytesReceived ;

    long numBytesSent ;

    /**
     * Time spent executing by the client or server plugin. If there are
     * multiple agents in a single node, this does not measure actual CPU
     * time since multiple threads may be running.
     */
    long cpuTimeBySystemClock ;

    /**
     * Whether allocation results are logged.
     */
    boolean logAllocationResults ;

    /**
     * Whether time based preferences are logged.
     */
    boolean logTimePreferences ;

}
