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
import org.cougaar.core.relay.Relay;
import org.cougaar.core.util.UID;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.agent.ClusterIdentifier;
import org.cougaar.util.UnaryPredicate;

import java.util.Set;
import java.util.Collections;
import java.util.Vector;
import java.util.Iterator;

public class RelayTestPlugin extends ComponentPlugin {
    public static class MyTestRelay implements Relay.Source, Relay.Target {
        public MyTestRelay(UID uid, MessageAddress target, MessageAddress source, Object content, Object response ) {
            this.uid = uid;
            this.source= source ;
            this.value = content ;
            this.response = response ;
            targets = ((target != null) ?
             Collections.singleton(target) :
             Collections.EMPTY_SET);
        }

        public String toString() {
            return "<MyTestRelay: Uid= " + uid + ", value=" + getValue() + ",source=" + getSource() + ",target=" + targets + ">" ;
        }

        public Set getTargets() {
            return targets ;
        }

        public UID getUID() {
            return uid ;
        }

        public MessageAddress getSource() {
            return source ;
        }

        public void setValue( Object o ) {
            value = o ;
        }

        public Object getValue() {
            return value ;
        }

        public Object getContent() {
            return value;
        }

        public void setUID(UID uid) {
            throw new RuntimeException( "Tried to set UID" ) ;
        }

        public Object getResponse() {
            return null;
        }

        public Relay.TargetFactory getTargetFactory() {
            return SimpleRelayFactory.INSTANCE ;
        }

        public int updateContent(Object o, Relay.Token token) {
            value = o ;
            return Relay.CONTENT_CHANGE ;
        }

        public int updateResponse(MessageAddress address, Object o) {
            return 0;
        }

        private static final class SimpleRelayFactory
        implements TargetFactory, java.io.Serializable {

          public static final SimpleRelayFactory INSTANCE =
            new SimpleRelayFactory();

          private SimpleRelayFactory() {}

          public Relay.Target create(
              UID uid,
              MessageAddress source,
              Object content,
              Token token) {
            return new MyTestRelay( uid, null, source, content, null ) ;
          }

          private Object readResolve() {
            return INSTANCE;
          }
        };


        protected UID uid ;
        protected Set targets ;
        protected MessageAddress source ;
        protected Object value ;
        protected Object response ;

    }

    protected void setupSubscriptions() {
        BlackboardService bs = getBlackboardService() ;
        ServiceBroker sb = getServiceBroker() ;
        UIDService us = (UIDService ) sb.getService( this, UIDService.class, null ) ;


        Vector v = new Vector( getParameters() )  ;
        mt = new MyTestRelay(us.nextUID(), new ClusterIdentifier( ( String ) v.get(0) ), getClusterIdentifier(),
                getClusterIdentifier().cleanToString(), null ) ;
        bs.publishAdd( mt ) ;

        relays = ( IncrementalSubscription ) bs.subscribe( new UnaryPredicate() {
            public boolean execute(Object o) {
                if ( o instanceof MyTestRelay ) {
                    return true ;
                }
                return false ;
            }
        }) ;

    }

    protected void execute() {

        for ( Iterator iter = relays.getAddedCollection().iterator() ;  iter.hasNext() ; ) {
            System.out.println("Found relay in " + getClusterIdentifier() + "= "  + iter.next() );
        }

        mt.setValue( "Moose" );
        getBlackboardService().publishChange( mt ) ;
    }

    MyTestRelay mt ;
    IncrementalSubscription relays ;
}
