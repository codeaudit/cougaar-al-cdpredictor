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

import org.cougaar.core.plugin.*;
import org.cougaar.core.service.BlackboardService;
import org.cougaar.core.service.UIDService;
import org.cougaar.core.component.ServiceBroker;
//import org.cougaar.core.agent.ClusterIdentifier; //Himanshu
import org.cougaar.core.mts.*;
import org.cougaar.core.agent.service.alarm.PeriodicAlarm;
import org.cougaar.core.adaptivity.OMCRangeList;
import org.cougaar.core.adaptivity.SensorCondition;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.tools.castellan.plugin.SourceBufferRelay;
import org.cougaar.tools.castellan.plugin.TargetBufferRelay;
import org.cougaar.util.UnaryPredicate;
import org.cougaar.core.service.AgentIdentificationService;


import java.util.Vector;
import java.util.Iterator;

public class RelayTestPlugin2 extends ComponentPlugin {

    class FlushAlarm implements PeriodicAlarm {
        public FlushAlarm( long expTime )
        {
            this.expTime = expTime;
        }

        public void reset( long currentTime )
        {
            expTime = currentTime + delay ;
            expired = false ;
        }

        public long getExpirationTime()
        {
            return expTime ;
        }

        public void expire()
        {
            expired = true ;
            doProcess() ;
        }

        public boolean hasExpired()
        {
            return expired ;
        }

        public boolean cancel()
        {
            boolean was = expired;
            expired=true;
            return was;
        }

        boolean stop = false ;
        boolean expired = false ;
        int count = 1 ;
        long expTime ;
        long delay = 2000L ;
    }

    protected void doProcess() {
        BlackboardService bs = getBlackboardService() ;
        bs.openTransaction();
        String value = "Source" + ( count++ )  ;
        System.out.println("Sending object " + value + " from source..." );
        sourceBuffer.addOutgoing( value );
        if ( count % 5 == 0 ) {
            System.out.println("Flushing buffer");
            bs.publishChange( sourceBuffer ) ;
        }
        bs.closeTransaction();
    }

    protected void setupSubscriptions() {
        Vector v = new Vector( getParameters() ) ;
        String target = ( String ) v.get(0) ;

        BlackboardService bs = getBlackboardService() ;
        ServiceBroker sb = getServiceBroker() ;
        UIDService us = (UIDService ) sb.getService( this, UIDService.class, null ) ;
        ais = (AgentIdentificationService) getBindingSite().getServiceBroker().getService(this, AgentIdentificationService.class, null);

        targetBufferSubscription =  (IncrementalSubscription ) bs.subscribe( new UnaryPredicate() {
            public boolean execute(Object o) {
                if ( o instanceof TargetBufferRelay ) {
                   return true ;
                }
                return false ;
            }
        } ) ;
        sourceBufferSubscription = ( IncrementalSubscription ) bs.subscribe( new UnaryPredicate() {
            public boolean execute(Object o) {
                if ( o instanceof SourceBufferRelay ) {
                    return true ;
                }
                return false ;
            }
        }) ;

        sourceBuffer =
                new SourceBufferRelay( us.nextUID(), MessageAddress.getMessageAddress( target ), ais.getMessageAddress() ) ;

        bs.publishAdd( sourceBuffer ) ;

        getAlarmService().addRealTimeAlarm( new FlushAlarm( System.currentTimeMillis() + 5000 ) ) ;
    }

    int count = 0 ;
    int rcount = 0 ;

    protected void execute() {

        for ( Iterator iter = targetBufferSubscription.getAddedCollection().iterator(); iter.hasNext(); ) {
            System.out.println( ais.getName() + " FOUND NEW " + iter.next() );
        }

        for ( Iterator iter = targetBufferSubscription.getChangedCollection().iterator(); iter.hasNext(); ) {
            TargetBufferRelay relay = ( TargetBufferRelay ) iter.next() ;
            System.out.println("PROCESSING INCOMING AT TARGET " + relay );
            Object[] o = relay.clearIncoming() ;
            for (int i=0;i<o.length;i++) {
                System.out.println( o[i] );
                relay.addResponse( ( ( String ) o[i] ) + ":" + ( rcount++ )  );
                if ( rcount % 4 == 0 ) {
                    getBlackboardService().publishChange( relay ) ;
                }
            }
        }

        for ( Iterator iter = sourceBufferSubscription.getChangedCollection().iterator(); iter.hasNext(); ) {
            SourceBufferRelay relay = ( SourceBufferRelay ) iter.next() ;
            Object[] o = relay.clearReponses() ;
            System.out.println("PROCESSING RESPONSE AT SOURCE " + relay );
            for ( int i=0;i<o.length;i++) {
                System.out.println( o[i] );
            }
        }
    }

    IncrementalSubscription targetBufferSubscription, sourceBufferSubscription ;
    SourceBufferRelay sourceBuffer ;
    protected AgentIdentificationService ais;
}