/*
 * <copyright>
 *  Copyright 2002 Intelligent Automation, Inc.
 *  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the Cougaar Open Source License as published by
 *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
 *
 *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 *  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *  PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */

package org.cougaar.tools.castellan.ldm;

import org.cougaar.core.agent.*;
import org.cougaar.core.service.*;
import org.cougaar.core.mts.*;
import org.cougaar.core.component.*;
import org.cougaar.core.domain.*;
import org.cougaar.util.*;

/**
 * Implement a Plan logging service as an agent.
 */

public class PlanLogAgent extends Agent
        implements MessageTransportClient, MessageStatistics
{
    public ClusterIdentifier getAgentIdentifier()
    {
        return new ClusterIdentifier( "PlanLogAgent" ) ;
    }

    public ConfigFinder getConfigFinder()
    {
        return ConfigFinder.getInstance();
    }

    public String toString()
    {
        return "PlanLogAgent " + name ;
    }

    public final void setBindingSite(BindingSite bs) {
      super.setBindingSite(bs);
      if (bs instanceof AgentBindingSite) {
        bindingSite = (AgentBindingSite) bs;
      } else {
        throw new RuntimeException("Tried to load "+this+" into "+bs);
      }
    }

    protected final AgentBindingSite getBindingSite() {
      return bindingSite;
    }

    public void receiveMessage(Message message)
    {
       System.out.println("Message " + message + " received." );
    }

    public MessageStatistics.Statistics getMessageStatistics(boolean reset)
    {
        return statisticsService.getMessageStatistics(reset);
    }

    public MessageAddress getMessageAddress()
    {
        return getAgentIdentifier() ;
    }

    public synchronized void load() throws StateModelException
    {
        System.out.println("Loading PlanLogAgent...");
        // Do the loading actions first,
        if (!( getBindingSite() instanceof AgentBindingSite ) ) {
          throw new StateModelException(
              "Container ("+getBindingSite()+") does not implement AgentBindingSite");
        }
        ServiceBroker sb = getServiceBroker();

        // get the Messenger instance from the service broker
        messenger = (MessageTransportService)
          sb.getService(this, MessageTransportService.class, null);
        messenger.registerClient(this);

        statisticsService = (MessageStatisticsService)
          sb.getService(
              this, MessageStatisticsService.class, null);

        watcherService = (MessageWatcherService)
          sb.getService(
              this, MessageWatcherService.class, null);

        // Transit the state
        super.load();
        System.out.println("Done!");
    }

    private MessageAddress address ;
    private AgentBindingSite bindingSite ;
    private String name ;
    private MessageTransportService messenger;
    private MessageStatisticsService statisticsService;
    private MessageWatcherService watcherService;

}
