/*
  * <copyright>
  *  Copyright 2002 (Intelligent Automation, Inc.)
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
package org.cougaar.cpe.ui;

import org.cougaar.core.plugin.ComponentPlugin;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.blackboard.Subscription;
import org.cougaar.core.adaptivity.OperatingModeCondition;
import org.cougaar.core.service.LoggingService;
import org.cougaar.util.UnaryPredicate;
import org.cougaar.tools.techspecs.qos.MeasurementPoint;
import org.cougaar.cpe.agents.plugin.WorldStateReference;
import org.cougaar.cpe.relay.SourceBufferRelay;
import org.cougaar.cpe.relay.TargetBufferRelay;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collections;
import java.util.Comparator;


public class AgentDisplayPlugin extends ComponentPlugin {


    private static final String WINDOWS_LF =
            "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";


//    static {
//        try {
//            UIManager.setLookAndFeel( WINDOWS_LF ) ;
//            Thread.sleep( 5000 );
//        } catch ( Exception e ) {
//        }
//    }

    protected void setupSubscriptions() {
        logger = (LoggingService) getServiceBroker().getService( this, LoggingService.class, null ) ;

        baseTime = System.currentTimeMillis() ;
        panel = new AgentDisplayPanel( getAgentIdentifier().getAddress(), this ) ;
        panel.setSize( 800, 600 ) ;
        panel.setVisible( true );

        worldStateSubscription = (IncrementalSubscription) getBlackboardService().subscribe( new UnaryPredicate() {
            public boolean execute(Object o) {
                return o instanceof WorldStateReference ;
            }
        }) ;

        opModeCondSubscription = (IncrementalSubscription) getBlackboardService().subscribe( new UnaryPredicate() {
            public boolean execute(Object o) {
                return o instanceof OperatingModeCondition ;
            }
        }) ;

        measurementPointSubscription = ( IncrementalSubscription ) getBlackboardService().subscribe( new UnaryPredicate() {
            public boolean execute(Object o) {
                return o instanceof MeasurementPoint ;
            }
        } ) ;

        relaySubscription = (IncrementalSubscription) getBlackboardService().subscribe( new UnaryPredicate() {
            public boolean execute(Object o) {
                return o instanceof SourceBufferRelay || o instanceof TargetBufferRelay ;
            }
        } ) ;
    }

    public long getBaseTime() {
        return baseTime;
    }

    protected void execute() {
        if ( !worldStateSubscription.getAddedCollection().isEmpty() || !worldStateSubscription.getRemovedCollection().isEmpty() ) {
            worldStates.clear();
            worldStates.addAll( worldStateSubscription.getCollection() ) ;
            Collections.sort( worldStates, new Comparator() {
                public int compare(Object o1, Object o2) {
                   WorldStateReference w1 = (WorldStateReference) o1, w2 = (WorldStateReference) o2 ;
                    return w1.getName().compareTo( w2.getName() ) ;
                }
            } ) ;
            panel.updateWorldStates( worldStates );
            if ( worldStates.size() > 0 && baseTime == 0  ) {
                WorldStateReference ref = (WorldStateReference) worldStates.get(0) ;
                if ( ref.getState() != null ) {
                    baseTime = ref.getState().getBaseTime() ;
                }
            }
        }

        if ( !opModeCondSubscription.getAddedCollection().isEmpty() ||
                !opModeCondSubscription.getRemovedCollection().isEmpty() ) {
            opModes.clear();
            opModes.addAll( opModeCondSubscription.getCollection() ) ;
            logger.info( " AgentDisplayPlugin:: Found updated opModes" + opModes );
            panel.updateControls( opModes );
        }

        if ( !measurementPointSubscription.getAddedCollection().isEmpty() || !measurementPointSubscription.getRemovedCollection().isEmpty() ) {
            measurementPoints.clear();
            measurementPoints.addAll( measurementPointSubscription.getCollection() ) ;
            Collections.sort( worldStates, new Comparator() {
                public int compare(Object o1, Object o2) {
                   MeasurementPoint w1 = (MeasurementPoint) o1, w2 = (MeasurementPoint) o2 ;
                    return w1.getName().compareTo( w2.getName() ) ;
                }
            } ) ;
            panel.updateMeasurements( measurementPoints );
        }

        if ( !relaySubscription.getCollection().isEmpty() ) {
            panel.updateRelays( relaySubscription.getCollection() );
        }
    }

    LoggingService logger ;
    AgentDisplayPanel panel ;
    ArrayList worldStates = new ArrayList();
    ArrayList opModes = new ArrayList() ;
    ArrayList measurementPoints = new ArrayList() ;
    long baseTime = 0 ;

    IncrementalSubscription worldStateSubscription, opModeCondSubscription, measurementPointSubscription ;
    private IncrementalSubscription relaySubscription;

}
