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
  */

package org.cougaar.tools.castellan.ldm;

public class PlanLogConfig implements java.io.Serializable
{
    public String getLogCluster()
    {
        return logCluster;
    }

    public void setLogCluster( String logCluster )
    {
        this.logCluster = logCluster;
    }

    public int getARLogLevel()
    {
        return arLogLevel;
    }

    public void setARLogLevel( int arLogLevel )
    {
        this.arLogLevel = arLogLevel;
    }

    public boolean isLogEpochs()
    {
        return logEpochs;
    }

    public void setLogEpochs( boolean logEpochs )
    {
        this.logEpochs = logEpochs;
    }

    public boolean isLogPluginTriggers()
    {
        return logPluginTriggers;
    }

    public void setLogPluginTriggers( boolean logPluginTriggers )
    {
        this.logPluginTriggers = logPluginTriggers;
    }

    public boolean isServer()
    {
        return isServer;
    }

    public void setServer( boolean server )
    {
        isServer = server;
    }

    public String getNodeIdentifier() {
        return nodeIdentifier;
    }

    public void setNodeIdentifier(String nodeIdentifier) {
        this.nodeIdentifier = nodeIdentifier;
    }

    String logCluster ;
    String nodeIdentifier ;
    int arLogLevel ;
    boolean logEpochs ;
    boolean logPluginTriggers ;
    boolean isServer = false ;
}
