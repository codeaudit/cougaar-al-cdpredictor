/*
 * <copyright>
 *  Copyright 2003-2004 Intelligent Automation, Inc.
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

package org.cougaar.cpe.agents.plugin;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.plugin.ComponentPlugin;
import org.cougaar.core.relay.Relay;
import org.cougaar.core.service.UIDService;
import org.cougaar.core.service.LoggingService;
import org.cougaar.core.agent.service.alarm.Alarm;
import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.util.UnaryPredicate;
import org.cougaar.util.ConfigFinder;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.cougaar.cpe.agents.messages.*;
import org.cougaar.cpe.agents.qos.QoSConstants;
import org.cougaar.cpe.relay.GenericRelayMessageTransport;
import org.cougaar.cpe.relay.MessageSink;
import org.cougaar.cpe.relay.SourceBufferRelay;
import org.cougaar.cpe.model.*;
import org.cougaar.tools.techspecs.qos.TimestampMeasurement;
import org.cougaar.tools.techspecs.qos.TimestampMeasurementImpl;
import org.cougaar.tools.techspecs.qos.MeasurementChain;
import org.cougaar.tools.techspecs.qos.VectorMeasurement;
import org.cougaar.cpe.splan.SPlanner;
import org.cougaar.tools.techspecs.events.MessageEvent;

import java.util.*;
import java.io.File;

public class SupplierPlugin extends ComponentPlugin implements MessageSink {
    private HashMap supplyPlans;
    private boolean started = false ;
    private LoggingService logger;

    protected void execute() {
        Collection newOrgs = findOrgsSubscription.getAddedCollection() ;

        processNewOrganizations( newOrgs );

        // Process all messages from subordinates.  This is be done on every
        // transaction cycle.
        gmrt.execute( getBlackboardService() );
    }

    private void processNewOrganizations( Collection newOrgs) {
        if ( !newOrgs.isEmpty() ) {
            ArrayList selfList = new ArrayList(), newSuperiors = new ArrayList(), subordinates = new ArrayList() ;

            OrganizationHelper.processSuperiorAndSubordinateOrganizations( getAgentIdentifier(),
                    newOrgs, newSuperiors, subordinates, selfList );

            for (int i = 0; i < newSuperiors.size(); i++) {
                Organization organization = (Organization) newSuperiors.get(i);
                if ( superior == null ) {
                    superior = organization ;
                }
                else if ( !superior.getMessageAddress().getAddress().equals( organization.getMessageAddress().getAddress() )) {
                    System.err.println("Warning: More than one superior " +
                            superior + " and " + organization + " found for " + getAgentIdentifier() );
                }
            }

            ArrayList newCustomers = new ArrayList(), newProviders = new ArrayList() ;
            OrganizationHelper.processCustomerAndProviderOrganizations( getAgentIdentifier(),
                    newOrgs, newCustomers, newProviders );

            for (int i = 0; i < newCustomers.size(); i++) {
                Organization organization = (Organization) newCustomers.get(i) ;
                customers.add( organization ) ;
            }
        }
    }

    public void processMessage(Object o) {
        if ( o instanceof MessageEvent ) {
            MessageEvent cm = (MessageEvent) o ;

            if ( cm.getSource().getAddress().equals( "WorldState")) {
                processMessageFromWorldState( cm );
            }
            else {
                if ( customers.size() == 0 ) {
                    System.out.println( getAgentIdentifier() + " processMessage():: WARNING: Message from unknown customer "
                            + cm.getSource() + ", no customers available.");
                }
                else {
                    boolean found = false ;
                    for (int i = 0; i < customers.size(); i++) {
                        Organization customer = (Organization) customers.get(i);
                        if ( customer.getMessageAddress().getAddress().equals( cm.getSource().getAddress() ) ) {
                            processMessagesFromCustomer( cm );
                            found = true ;
                        }
                    }
                    if (!found) {
                        System.out.println( getAgentIdentifier() + " processMessage():: WARNING: Message from unknown customer "
                            + cm.getSource() );
                    }
                }
            }
        }
    }

    /**
     * A signal from the WorldState to kick everything off.
     * @param message
     */
    private void processStartTimeMessage( StartMessage message ) {
        baseTime = message.getBaseTime() ;
        started = true ;

        // Start in five second intervals.  Scan the manueverPlan list to see
        // if any replan is really neccessary.
        // The first time, replan quickly to generate an initial plan.
        getAlarmService().addRealTimeAlarm( new ReplanAlarm( baseTime + 1000L ) ) ;
    }

    private void processMessageFromWorldState(MessageEvent message ) {
        if ( message instanceof StartMessage ) {
            System.out.println("SIMULATION START.");
            processStartTimeMessage( ( StartMessage ) message ) ;
        }
        else if ( message instanceof UnitStatusUpdateMessage) {
            // System.out.println( getAgentIdentifier() + " processMessageFromWorldState::  PROCESSING MESSAGE " + message  );
            UnitStatusUpdateMessage wsum = ( UnitStatusUpdateMessage ) message ;
            WorldStateModel ws = wsum.getWorldState() ;

            // Copy over current set of manuever plans into the world state,
            // just to be safe.
            for (Iterator iterator = manueverPlans.entrySet().iterator(); iterator.hasNext();) {
                Map.Entry entry = (Map.Entry) iterator.next() ;
                UnitEntity entity = (UnitEntity) ws.getEntity( ( String ) entry.getKey() ) ;
                if ( entity != null ) {
                    ManueverPlanMessage mpm = (ManueverPlanMessage) entry.getValue() ;
                    entity.setManueverPlan( mpm.getPlan() );
                }
            }
            perceivedWorldState = ws ;
        }
        else if ( message instanceof StopMessage ) {
            started = false ;
        }
    }

    private void processMessagesFromCustomer(MessageEvent cm) {
        System.out.println( getAgentIdentifier() + " SUPPLIER RECEIVED MESSAGE "
                + cm + " from " + cm.getSource() );
        if ( cm instanceof ManueverPlanMessage ) {
            manueverPlans.put( cm.getSource().getAddress(), cm ) ;
            UnitEntity ue = (UnitEntity)
                    perceivedWorldState.getEntity( cm.getSource().getAddress() ) ;

            isManueverPlanDirty.put( cm.getSource().getAddress(),
                    cm.getSource().getAddress() ) ;
            // If there is no sustainment plan in effect, make one iff
            // we have all the messages from the customers.
            if ( manueverPlans.size() == customers.size() && supplyPlans == null  ) {
                planAndDistribute() ;
            }
        }
    }

    private void planAndDistribute() {
        // Check for manuever plans from each client.
        // If there are any new plans, replan to take into account the new plans.
        // If there aren't any fresh ones, don't replan

//        VectorMeasurement vm = new VectorMeasurement( QoSConstants.TIMER_ACTION,
//                QoSConstants.PLAN_SUSTAINMENT_ACTION, getAgentIdentifier() ) ;
//        TimestampMeasurement[] tm =
//                new TimestampMeasurement[ customers.size() ] ;
//
//        for (int i = 0; i < customers.size(); i++) {
//            String s = (String) customers.get(i);
//            ManueverPlanMessage mp =
//                    (ManueverPlanMessage) manueverPlans.get( s ) ;
//            MeasurementChain mc = mp.getMeasurements() ;
//
//            // Fill out the FreshnessMeasurement for all mps, e.g. how fresh is the
//            // WorldState data associated with this manuever plan.
//            if ( mc != null && mc.getNumMeasurements() > 0 ) {
//                TimestampMeasurement tm2 = (TimestampMeasurement) mc.getMeasurement(0) ;
//                tm[i] = new TimestampMeasurementImpl( tm2.getAction(), tm2.getEvent(),
//                        tm2.getSource(), tm2.getTimestamp() ) ;
//            }
//        }
//        vm.setMeasurements( tm );

        // Don't try to replan.
        if ( manueverPlans.size() == 0 || isManueverPlanDirty.size() == 0 ) {
            System.out.println( getAgentIdentifier() + ":: planAndDistribute NOTHING TO PLAN.");
            return ;
        }

        System.out.println( getAgentIdentifier() + " CREATING SUPPLY PLAN AT CURRENT TIME "
                + ( ( System.currentTimeMillis() - baseTime ) /1000 )  +
                " s. and WORLD TIME " + perceivedWorldState.getTimeInSeconds() + " s.") ;
        SPlanner splanner = new SPlanner() ;
        // WorldStateModel wsm =
        WorldStateModel wsm = new WorldStateModel( perceivedWorldState, true, true ) ;

        for (int i = 0; i < customers.size(); i++) {
            Organization s = ( Organization ) customers.get(i);
            String customerName = s.getMessageAddress().getAddress() ;
            ManueverPlanMessage mp =
                    (ManueverPlanMessage) manueverPlans.get( customerName ) ;

            // Use the new manuever plan to replan the sustainment plan.
            if ( mp == null ) {
                UnitEntity ue = (UnitEntity) wsm.getEntity( customerName ) ;
                if ( ue == null ) {
                    logger.error("No unit entity for customer " + customerName + " found.");
                }
                else if ( ue.getManueverPlan() == null ) {
                    logger.warn("Warning: No existing maneuver plan for customer " + ue.getId() );
                }
                else {
                    Plan p = ue.getManueverPlan() ;
                    if ( p.getNumTasks() > 0 &&
                            p.getTask( p.getNumTasks() - 1).getEndTime() <
                            wsm.getTime() + scheduleDelay ) {
                        System.out.println("Warning: Existing plan " + p + " for " + ue.getId() +
                                " has end time > current planning start time " + wsm.getTime() + scheduleDelay );
                    }
                }
            }
            else if ( isManueverPlanDirty.get( mp.getSource().getAddress() ) != null ) {
                EntityInfo info = wsm.getEntityInfo( mp.getSource().getAddress() ) ;
                if ( info != null ) {
                    isManueverPlanDirty.remove( mp.getSource().getAddress() ) ;
                    UnitEntity entity = ( UnitEntity ) info.getEntity() ;
                    entity.setManueverPlan( mp.getPlan() );
                }
                else {
                    System.err.println("Received manuever plan for unknown entity " + mp.getSource() );
                }
            }
        }

        ArrayList customerNames = new ArrayList( customers.size() ) ;
        for (int i = 0; i < customers.size(); i++) {
            Organization organization = (Organization)customers.get(i);
            customerNames.add( organization.getMessageAddress().getAddress() ) ;
        }
        splanner.plan( wsm, scheduleDelay, customerNames, supplyVehicles );

        supplyPlans = splanner.getPlans() ;

        if ( supplyPlans == null ) {
            logger.warn( "Could not create supply plan.");
            return ;
        }

        //System.out.println( getAgentIdentifier() + "::INFO:: CREATED SUSTAINMENT PLANS " );
        for (Iterator iterator = supplyPlans.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            //System.out.println( entry.getKey() + ", plan=" + entry.getValue()  );
        }

//        for (Iterator iterator = supplyPlans.entrySet().iterator(); iterator.hasNext();) {
//            Map.Entry entry = (Map.Entry) iterator.next();
//            SupplyVehicleEntity sentity = (SupplyVehicleEntity) wsm.getEntity( ( String ) entry.getKey() ) ;
//            System.out.println("UPDATING " + sentity.getId() );
//        }

        // Now, send this directly to the WorldState since we are not modeling any vehicles yet.
        gmrt.sendMessage( MessageAddress.getMessageAddress("WorldState"), new SupplyPlanMessage(supplyPlans) );
    }

    protected HashMap isManueverPlanDirty = new HashMap() ;

    private void processReplanTimer( ) {

        long startTime = System.currentTimeMillis() ;

        try {
            planAndDistribute();
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }

        // getAlarmService().addRealTimeAlarm( new ReplanAlarm( ) ) ;

        if ( started ) {
            getAlarmService().addRealTimeAlarm( new ReplanAlarm( startTime + replanUpdatePeriod  ) ) ;
        }
        else {
            System.out.println("STOPPED.");
        }
    }

    protected void setupSubscriptions() {
        logger = ( LoggingService ) getServiceBroker().getService( this, LoggingService.class, null ) ;


        System.out.println("\nSTARTING agent " + getAgentIdentifier() );
        gmrt = new GenericRelayMessageTransport( this, getServiceBroker(), getAgentIdentifier(),
                this, getBlackboardService() ) ;

        getConfigInfo();

        // Make a list of my supply vehicles.
        System.out.println( getAgentIdentifier() + ": ADDING " + numberOfSupplyVehicles + " VEHICLES.");
        for (int i=0;i<numberOfSupplyVehicles;i++) {
            supplyVehicles.add( getAgentIdentifier() + "." + (i+1) );
        }

        gmrt.addRelay( MessageAddress.getMessageAddress("WorldState") ) ;

        /**
         * A predicate that matches all organizations that
         */
        class FindOrgsPredicate implements UnaryPredicate {
            public boolean execute(Object o) {
                return o instanceof Organization ;
            }
        }
        findOrgsSubscription = (IncrementalSubscription)
                getBlackboardService().subscribe(new FindOrgsPredicate());
    }

    protected void getConfigInfo() {
        Collection params = getParameters() ;
        Vector paramVector = new Vector( params ) ;
        LoggingService log = (LoggingService)
                getServiceBroker().getService( this, LoggingService.class, null ) ;

        String fileName = null ;
        if ( paramVector.size() > 0 ) {
            fileName = ( String ) paramVector.elementAt(0) ;
        }

        if ( fileName == null ) {
            System.err.println("No configuration file parameter found for " + getAgentIdentifier() );
            return ;
        }

        ConfigFinder finder = getConfigFinder() ;
        // ServiceBroker sb = getServiceBroker() ;

        try {
            if ( fileName != null && finder != null ) {

                File f = finder.locateFile( fileName ) ;

                // DEBUG -- Replace by call to log4j
                System.out.println( "CPESociety WorldState:: Configuring from " + f ) ;

                if ( f != null && f.exists() ) {
                    //
                    // Now, parse the config file
                    //

                    Document doc = null ;
                    try {
                        doc = finder.parseXMLConfigFile(fileName);
                    }
                    catch ( Exception e ) {
                        System.out.println( e ) ;
                    }

                    if ( doc != null ) {
                        try {
                            Node root = doc.getDocumentElement() ;
                            if( root.getNodeName().equals( "WorldStateConfig" ) ) {
                                String numberOfSupplyVehiclesValue = getNodeValueForTag( doc, "NumberOfSupplyVehicles", "value") ;
                                if ( numberOfSupplyVehiclesValue != null ) {
                                    numberOfSupplyVehicles = Integer.parseInt( numberOfSupplyVehiclesValue ) ;
                                }
                            }
                            else {
                                // DEBUG -- replace with log4j
                                if ( log.isWarnEnabled() ) {
                                    log.warn( "Warning:: WorldStateConfig file " + f + " is invalid, no root node \"WorldStateConfig\"" );
                                }
                                System.out.println( "Warning:: WorldStatConfig file " + f + " is invalid, no root node \"WorldStateConfig\"" ) ;
                            }
                        }
                        catch ( Exception e ) {
                            if ( log.isErrorEnabled() ) {
                                log.error( "Exception thrown parsing configuration file " + f );
                            }
                            System.out.println( e ) ;
                        }
                    }

                }

            }

        }
        catch ( Exception e ) {
            e.printStackTrace() ;
        }
    }

    private String getNodeValueForTag(Document doc, String tagName, String namedItem ) {
        NodeList nodes = doc.getElementsByTagName( tagName );

        String value = null ;
        // Get target plan log
        for (int i=0;i<nodes.getLength();i++) {
            Node n = nodes.item(i) ;
            value = n.getAttributes().getNamedItem( namedItem ).getNodeValue() ;
            //System.out.println( "Found identifier=" + value );
        }
        return value;
    }

    public Plan getSustainmentPlanForEntity( String id ) {
        return (Plan) sustainmentPlans.get(id) ;
    }

    public Plan getManueverPlanForEntity( String id ) {
        return (Plan) manueverPlans.get(id) ;
    }

    public class ReplanAlarm implements Alarm {
        private boolean expired;

        public ReplanAlarm( long expirationTime ) {
            this.expirationTime = expirationTime ;
        }

        public boolean cancel() {
            return false;
        }

        public void reset(long currentTime) {
            expired = false ;
            expirationTime = currentTime + period ;
        }

        public void expire() {
            expired = true ;
            processReplanTimer() ;
        }

        public long getExpirationTime() {
            return expirationTime ;
        }

        public boolean hasExpired() {
            return expired;
        }

        public long getPeriod() {
            return period;
        }

        public void setPeriod(long period) {
            this.period = period;
        }

        protected long expirationTime ;
        protected long period ;
    }

    private ArrayList supplyVehicles = new ArrayList() ;
    private int numberOfSupplyVehicles;
    private Organization self, superior ;
    private ArrayList customers = new ArrayList(), providers = new ArrayList() ;

    private GenericRelayMessageTransport gmrt;
    private SourceBufferRelay relayToWorldAgent;
    private IncrementalSubscription findOrgsSubscription;

    private HashMap manueverPlans = new HashMap();
    private HashMap sustainmentPlans = new HashMap() ;

    private long baseTime;

    private WorldStateModel perceivedWorldState ;

    /**
     * The schedule is delayed this much from the start planning horizon.
     */
    private long scheduleDelay = 5000 ;

    private long replanUpdatePeriod = 20000 ;
}
