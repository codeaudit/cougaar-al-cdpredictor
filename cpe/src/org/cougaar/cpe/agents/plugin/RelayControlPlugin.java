package org.cougaar.cpe.agents.plugin;

import org.cougaar.core.plugin.ComponentPlugin;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.adaptivity.InterAgentOperatingMode;
import org.cougaar.core.adaptivity.InterAgentCondition;
import org.cougaar.core.service.LoggingService;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.util.UnaryPredicate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.HashMap;

import org.cougaar.cpe.relay.SourceBufferRelay;
import org.cougaar.cpe.relay.TargetBufferRelay;

public class RelayControlPlugin extends ComponentPlugin {

    IncrementalSubscription operatingModes ;
    HashMap outboundRelays = new HashMap() ;
    HashMap inboundRelays = new HashMap() ;
    HashMap outboundOperatingConditions = new HashMap(), inboundOperatingConditions = new HashMap() ;
    private IncrementalSubscription relays;


    /**
     * Look for inter agent condition relays with the name
     * convention LinkRelay:FromAgent:ToAgent.
     */
    protected void execute() {

        // Process all the inbound/ outbound relays.
        for ( Iterator iter = relays.getAddedCollection().iterator(); iter.hasNext(); ) {
            Object o = iter.next() ;
            if ( o instanceof TargetBufferRelay ) {
                TargetBufferRelay t = (TargetBufferRelay) o ;
                inboundRelays.put( t.getSource().getAddress(), t ) ;
            }
            else if ( o instanceof SourceBufferRelay ) {
                SourceBufferRelay s = (SourceBufferRelay) o ;
                if ( !s.getTargets().isEmpty() ) {
                   MessageAddress addr =  (MessageAddress) s.getTargets().iterator().next() ;
                    outboundRelays.put( addr.getAddress(), s ) ;
                }
            }
        }

        Collection c = operatingModes.getAddedCollection() ;

        // Process all the interagent operating conditions.
        Iterator iter = c.iterator() ;
        while (iter.hasNext()) {
            InterAgentCondition interAgentCondition = (InterAgentCondition) iter.next();
            String name = (String) interAgentCondition.getValue() ;

            if ( name.startsWith( "LinkRelay.") ) {
                String[] substrings = name.split( ":" ) ;
                if ( substrings.length < 3 ) {
                    //getServiceBroker().getService( this, LoggingService.class, null ) ;
                    System.err.println( "Unrecognized link relay name " + name );
                }
                if ( substrings[1].equals(getAgentIdentifier().getAddress())) {
                    //SourceBufferRelay relay = (SourceBufferRelay) outboundRelays.get( substrings[1] ) ;
                    outboundOperatingConditions.put( getAgentIdentifier().getAddress(), interAgentCondition ) ;
                }
                else if ( substrings[2].equals( getAgentIdentifier().getAddress() ) ) {
                    inboundOperatingConditions.put( getAgentIdentifier().getAddress(), interAgentCondition ) ;
                }
            }
            else if ( name.startsWith( "SymmetricLinkRelay.") ) {
            }
        }

        // Process any changed relays.
    }

    protected void setupSubscriptions() {
        operatingModes = (IncrementalSubscription) blackboard.subscribe( new UnaryPredicate() {
            public boolean execute(Object o) {
                return ( o instanceof InterAgentCondition ) ;
            }
        })  ;

        relays = (IncrementalSubscription) blackboard.subscribe( new UnaryPredicate() {
            public boolean execute(Object o) {
                return ( o instanceof TargetBufferRelay || o instanceof SourceBufferRelay ) ;
            }
        }) ;
    }

    public static final void main( String[] args ) {
        String testString = "LinKRelay:Agent1:Agent2" ;
        String[] results = testString.split( ":" ) ;
        for (int i = 0; i < results.length; i++) {
            String result = results[i];
            System.out.println( result ) ;
        }
    }
}
