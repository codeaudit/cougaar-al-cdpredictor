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
package org.cougaar.tools.castellan.plugin;

import org.cougaar.core.plugin.*;

import java.util.*;

// IAIMOD
// Updated after every execution cycle by ComponentPlugIn
public class ExecutionTrace {
    public ExecutionTrace( String clusterName, PluginBase plugIn )
    {
        this.clusterName = clusterName ;
        this.pluginClass = plugIn.getClass().getName() ;
        this.pluginHashCode = plugIn.hashCode() ;
    }

    public long getCount()
    {
        return count;
    }

    public String getClusterName() {
        return clusterName ;
    }

    public String getPluginClass() {
        return pluginClass ;
    }

    public int getPluginHashCode() {
        return pluginHashCode ;
    }

    public synchronized void addRecord( long startTime, long endTime, long transactionId ) {
        list.add( new ExecutionRecord( count++,startTime, endTime, transactionId ) ) ;
    }

    public synchronized void clearRecords() {
        list.clear();
    }

    public int getNumRecords() { return list.size() ; }

    public ExecutionRecord getRecord( int i ) {
        return (ExecutionRecord) list.get(i) ;
    }

    private long loadTime ;
    private long count = 0 ;
    private String clusterName ;
    private String pluginClass ;
    private int pluginHashCode ;

    ArrayList list = new ArrayList( 10 ) ;
}
