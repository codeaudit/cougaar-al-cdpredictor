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
package org.cougaar.tools.castellan.tests;

import org.cougaar.core.plugin.ComponentPlugin;
import org.cougaar.core.service.BlackboardService;
import org.cougaar.core.service.UIDService;
import org.cougaar.core.service.UIDServer;
import org.cougaar.core.adaptivity.InterAgentCondition;
import org.cougaar.core.adaptivity.SensorCondition;
import org.cougaar.core.adaptivity.OMCRangeList;
import org.cougaar.core.relay.Relay;
import org.cougaar.core.util.UID;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.agent.ClusterIdentifier;
import org.cougaar.util.UnaryPredicate;
import org.cougaar.tools.alf.sensor.SensorConditionRelay;

import java.util.Set;
import java.util.Collections;
import java.util.Vector;
import java.util.Iterator;

public class RelayTestPlugin extends ComponentPlugin {

    protected void setupSubscriptions() {
        BlackboardService bs = getBlackboardService() ;
        ServiceBroker sb = getServiceBroker() ;
        UIDService us = (UIDService ) sb.getService( this, UIDService.class, null ) ;

        // This is stupid.
        OMCRangeList range = new OMCRangeList( new Integer( 0 ), new Integer( 1 ) ) ;

        SensorCondition condition = new SensorCondition( "FallingBehind", range, new Integer( 0 ) ) ;

        Vector v = new Vector( getParameters() )  ;
        mt = new SensorConditionRelay(us.nextUID(), new ClusterIdentifier( ( String ) v.get(0) ), getClusterIdentifier(),
                condition, null ) ;
        bs.publishAdd( mt ) ;

        relays = ( IncrementalSubscription ) bs.subscribe( new UnaryPredicate() {
            public boolean execute(Object o) {
                if ( o instanceof SensorConditionRelay ) {
                    return true ;
                }
                return false ;
            }
        }) ;

    }

    protected void execute() {

        for ( Iterator iter = relays.getAddedCollection().iterator() ;  iter.hasNext() ; ) {
            System.out.println(getBindingSite().getAgentIdentifier() + "::Found relay in " + getClusterIdentifier() + "= "  + iter.next() );
        }

    }

    SensorConditionRelay mt ;
    IncrementalSubscription relays ;
}
