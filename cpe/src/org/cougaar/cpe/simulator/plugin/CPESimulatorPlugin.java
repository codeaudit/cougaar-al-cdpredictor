package org.cougaar.cpe.simulator.plugin;

import org.cougaar.core.agent.service.alarm.Alarm;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.plugin.ComponentPlugin;
import org.cougaar.core.service.LoggingService;
import org.cougaar.util.ConfigFinder;
import org.cougaar.util.UnaryPredicate;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.cougaar.cpe.agents.messages.*;
import org.cougaar.cpe.agents.Constants;
import org.cougaar.cpe.agents.plugin.WorldStateReference;
import org.cougaar.cpe.model.*;
import org.cougaar.cpe.model.events.CPEEventListener;
import org.cougaar.cpe.model.events.CPEEvent;
import org.cougaar.cpe.relay.*;
import org.cougaar.cpe.planning.zplan.BNAggregate;
import org.cougaar.cpe.planning.zplan.ZoneWorld;
import org.cougaar.cpe.util.CloneUtils;
import org.cougaar.tools.techspecs.qos.*;
import org.cougaar.tools.techspecs.events.MessageEvent;
import org.cougaar.tools.techspecs.events.ActionEvent;
import org.cougaar.tools.techspecs.events.TimerEvent;

import java.io.*;
import java.util.*;
import java.lang.reflect.Method;
import java.text.DecimalFormat;

/**
 * Responsible for initializing the actual world state.
 */

public class CPESimulatorPlugin extends ComponentPlugin implements MessageSink {
    private IncrementalSubscription relaySubscription;
    private IncrementalSubscription controlRelaySubscription;
    private double advanceRate = 1.0;
    private long lastTime ;
    private long baseTime;
    private boolean isConfigured = false ;
    private String saveMPOutDir;
    private long simulationLength;
    private String TIME_ADVANCE_ALARM_ID = "TimeAdvanceAlarmId" ;
    private LoggingService log;
    private int numBNUnits;
    private int numCPYUnitsPerBN;
    private float zoneGridSize = 2;
    private ZoneWorld initialZoneWorld;
    private int numInitialTargetGenerationDeltas = 110 ;
    private TargetGenerator targetGenerator;
    private DecimalFormat format;
    private String targetGeneratorClassName;
    private String targetGeneratorConfigFile;
    private CPEEventListener worldEventListener;
    private ArrayList worldEvents = new ArrayList();
    private WorldStateReference refToWorldState;
    private String worldParamConfigFileName;
    private byte[] worldParamBytes;
    private boolean autoResupply;
    private int metricsIntegrationPeriod = 5000 ;

    protected void execute() {
        // First, check to see if there are any new client relays.
        Iterator iter = relaySubscription.getAddedCollection().iterator() ;
        while (iter.hasNext()) {
            TargetBufferRelay relay = (TargetBufferRelay) iter.next();
            log.shout("WorldState agent found new relay from " + relay.getSource() + " agent." );
            clientRelays.put( relay.getSource().getAddress(), relay ) ;
        }

        // Every transaction must call this!
        mt.execute( getBlackboardService() );
    }

    /**
     * Callback from the MessageTransport.
     * @param o
     */
    public void processMessage(Object o) {
        if ( o instanceof MessageEvent ) {
            MessageEvent msg = ( MessageEvent ) o ;
            MessageAddress address = msg.getSource() ;
            if ( clientRelays.get( address.getAddress() ) != null ) {
                processIncomingsMessagesFromClient( msg );
            }
        }
    }

    private void processIncomingsMessagesFromClient( MessageEvent msg ) {
        /**
         * Put the manuever plan into effect.
         */
        if ( msg instanceof ManueverPlanMessage ) {
            ManueverPlanMessage mpm = (ManueverPlanMessage) msg ;
            Plan p = mpm.getPlan() ;
            EntityInfo info = referenceWorldState.getEntityInfo( mpm.getSource().getAddress() ) ;
            if ( info == null ) {
                log.warn( getAgentIdentifier() + ":: WARNING No entity found for incoming message from " + mpm.getSource() );
            }
            else {
                UnitEntity ue = (UnitEntity) info.getEntity() ;
                ue.setManueverPlan( p );
                // System.out.println( getAgentIdentifier() + ":: SETTING PLAN " + p );
            }
        }
        else if ( msg instanceof SupplyPlanMessage ) {
            SupplyPlanMessage spm = (SupplyPlanMessage) msg ;
            HashMap splans = spm.getPlans() ;

            for (Iterator iterator = splans.entrySet().iterator(); iterator.hasNext();) {
                Map.Entry entry = (Map.Entry) iterator.next();
                SupplyVehicleEntity sentity = (SupplyVehicleEntity)
                        referenceWorldState.getEntity( ( String ) entry.getKey() ) ;
                log.debug( "UPDATING " + sentity.getId() );
                log.debug("OLD PLAN " + sentity.getSupplyPlan()  ) ;
                log.debug("NEW PLAN " + entry.getValue() );
                sentity.updateSupplyPlan( (Plan) entry.getValue() );
                log.debug("MERGED PLAN " + sentity.getSupplyPlan() );
            }
        }
        else if ( msg instanceof BundledMPMessage ) {
            dumpMeasurements( (BundledMPMessage) msg ) ;
        }
    }

    /**
     * Output a set of delay measurements as a CSV list of start times, a CSV
     * list of end times, and a CSV list of delay times ( delay = end- start )
     * @param dmp
     * @param stream
     */
    public static void dumpDelayMeasurements( DelayMeasurementPoint dmp, OutputStream stream ) {
        Iterator iter = dmp.getMeasurements() ;
        System.out.println("Dunping " + dmp + ", size=" + dmp.getHistorySize() );

        PrintWriter pw = new PrintWriter( stream ) ;
        while (iter.hasNext())
        {
            DelayMeasurement measurement = (DelayMeasurement) iter.next();
            // System.out.println("Dumping  " + measurement );
            pw.print( measurement.getTimestamp() );
            if ( iter.hasNext() ) {
                pw.print( ", " ) ;
            }
        }
        pw.println();

        iter = dmp.getMeasurements() ;
        while (iter.hasNext())
        {
            DelayMeasurement measurement = (DelayMeasurement) iter.next();
            pw.print( measurement.getLocalTime() );
            if ( iter.hasNext() ) {
                pw.print( ", " ) ;
            }
        }
        pw.println();

        iter = dmp.getMeasurements() ;
        while (iter.hasNext())
        {
            DelayMeasurement measurement = (DelayMeasurement) iter.next();
            pw.print( measurement.getDelay() );
            if ( iter.hasNext() ) {
                pw.print( ", " ) ;
            }
        }
        pw.flush();
    }

    public static void dumpTimestampMeasurements( TimestampMeasurementPoint dmp, OutputStream stream ) {
        Iterator iter = dmp.getMeasurements() ;
        PrintWriter pw = new PrintWriter( stream ) ;
        while (iter.hasNext())
        {
            TimestampMeasurement tm = (TimestampMeasurement) iter.next() ;
            pw.print( tm.getTimestamp() );
            if ( iter.hasNext() ) {
                pw.print( ", " ) ;
            }
        }
    }

    public static void dumpOMCMesurements( OMCMeasurementPoint mp, OutputStream stream ) {
        PrintWriter pw = new PrintWriter( stream ) ;

        Iterator iter = mp.getMeasurements() ;
        while (iter.hasNext())
        {
            OMCMeasurement m = (OMCMeasurement) iter.next() ;
            pw.print( m.getSimTime() );
            if ( iter.hasNext() ) {
                pw.print( ", " ) ;
            }
        }
        pw.println();

        iter = mp.getMeasurements() ;
        while (iter.hasNext())
        {
            OMCMeasurement m = (OMCMeasurement) iter.next() ;
            pw.print( m.getTime() );
            if ( iter.hasNext() ) {
                pw.print( ", " ) ;
            }
        }
        pw.println();

        iter = mp.getMeasurements() ;
        while (iter.hasNext())
        {
            OMCMeasurement m = (OMCMeasurement) iter.next() ;
            pw.print( m.getValue() );
            if ( iter.hasNext() ) {
                pw.print( ", " ) ;
            }
        }
        pw.println();
        pw.flush();
    }

    public String getSaveMPOutDir()
    {
        return saveMPOutDir;
    }

    public void setSaveMPOutDir(String saveMPOutDir)
    {
        this.saveMPOutDir = saveMPOutDir;
    }

    private void dumpMeasurements(BundledMPMessage msg)
    {
        if ( saveMPOutDir != null ) {
            System.out.println("Saving BundledMPMessage from " + msg.getSource() );
            ArrayList data = msg.getMeasurementPointData() ;
            for (int i = 0; i < data.size(); i++) {
                Object o = data.get(i) ;
                if ( o instanceof MeasurementPoint ) {
                    MeasurementPoint measurementPoint = (MeasurementPoint) data.get(i);
                    File f = new File( saveMPOutDir, msg.getSource() + "." + measurementPoint.getName() + ".dat" ) ;
                    try
                    {
                        FileOutputStream fos = new FileOutputStream( f ) ;
                        if ( measurementPoint instanceof DelayMeasurementPoint ) {
                            dumpDelayMeasurements( ( DelayMeasurementPoint ) measurementPoint, fos );
                        }
                        else if ( measurementPoint instanceof TimestampMeasurementPoint ) {
                            dumpTimestampMeasurements( ( TimestampMeasurementPoint ) measurementPoint, fos );
                        }
                        else if ( measurementPoint instanceof TimePeriodMeasurementPoint ) {
                            dumpTimePeriodMeasurements( ( TimePeriodMeasurementPoint ) measurementPoint, fos ) ;
                        }
                        else if ( measurementPoint instanceof OMCMeasurementPoint ) {
                            dumpOMCMesurements( (OMCMeasurementPoint) measurementPoint, fos );
                        }
                        fos.flush();
                        fos.close();
                    }
                    catch (FileNotFoundException e)
                    {
                        e.printStackTrace();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
                else if ( o instanceof Object[] ) {
                    Object[] configData = (Object[]) o ;
                    File f = new File( saveMPOutDir, msg.getSource() + "." + configData[0] ) ;
                    try
                    {
                        FileOutputStream fos = new FileOutputStream( f ) ;
                        try
                        {
                            fos.write( ( byte[]) configData[1]);
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                    catch (FileNotFoundException e)
                    {
                        e.printStackTrace();
                    }

                }
            }

        }
    }

    private void dumpTimePeriodMeasurements(TimePeriodMeasurementPoint mp, FileOutputStream fos)
    {
        Iterator iter = mp.getMeasurements() ;
        PrintWriter pw = new PrintWriter( fos ) ;
        while (iter.hasNext())
        {
            TimePeriodMeasurement measurement = (TimePeriodMeasurement) iter.next();
            pw.print( measurement.getStartTime() );
            if ( iter.hasNext() ) {
                pw.print( ",") ;
            }
        }
        pw.println();
        iter = mp.getMeasurements() ;
        while (iter.hasNext())
        {
            TimePeriodMeasurement measurement = (TimePeriodMeasurement) iter.next();
            pw.print( measurement.getEndTime() );
            if ( iter.hasNext() ) {
                pw.print( ",") ;
            }
        }
        pw.println();
        iter = mp.getMeasurements() ;
        while (iter.hasNext())
        {
            TimePeriodMeasurement measurement = (TimePeriodMeasurement) iter.next();
            pw.print( measurement.getValue() );
            if ( iter.hasNext() ) {
                pw.print( ",") ;
            }
        }
        pw.close();
    }

    /**
     * The callback that handles time advances.
     *
     * @param m
     */
    public void DoTimeAdvance( TimerEvent m ) {
        double rate ;

        long newTime = System.currentTimeMillis() ;
        rate =  ( referenceWorldState.getDeltaT() * VGWorldConstants.MILLISECONDS_PER_SECOND ) / ( newTime - lastTime ) ;

        lastTime = newTime ;
        long elapsedTime = lastTime - baseTime ;

        log.shout( getAgentIdentifier() +
                " ADVANCING time at simTime=" + referenceWorldState.getTime() +
                ", elapsed=" + elapsedTime * VGWorldConstants.SECONDS_PER_MILLISECOND +
                ", advanceRate =" + format.format(rate) + "x Real Time" ) ;

        // Start transaction
        boolean wasClosed = false ;
        try {
            /**
             * Process any new messages.
             */
//            mt.execute( getBlackboardService() );

            // Generate targets
            generateNewTargets();

            /*
             * Update the world state for a single time advance.
             */
            referenceWorldState.updateWorldState();

            // Forward all of these directly to the relevent clients.
            sendWorldStateToClients();

        }
        finally {
//            if ( wasClosed ) {
//                getBlackboardService().closeTransaction() ;
//            }
        }

        // Reschedule the alarm to fire one DeltaT into the future.
        if ( lastTime > baseTime + simulationLength ) {
            System.out.println("\nWorldStateExecutor: STOPPING SIMULATION");
            mt.clearAlarm( TIME_ADVANCE_ALARM_ID );
            stopTimeAdvance();
        }

        if ( autoResupply ) {
            Iterator iter = referenceWorldState.getUnits() ;
            log.shout( "AUTO RESUPPLY");
            while (iter.hasNext()) {
                UnitEntity entity = (UnitEntity) iter.next();
                entity.setAmmoQuantity( VGWorldConstants.getMaxUnitAmmoLoad() );
                entity.setFuelQuantity( VGWorldConstants.getMaxUnitFuelLoad() );
            }
        }

//        if ( lastTime < baseTime + simulationLength ) {
//            getAlarmService().addRealTimeAlarm( new WorldStateUpdateAlarm( lastTime
//                + ( long ) ( referenceWorldState.getDeltaT() * org.cougaar.cpe.model.VGWorldConstants.MILLISECONDS_PER_SECOND) ) ) ;
//        }
//        else {
//            // Send a stop message
//            System.out.println("\nWorldStateExecutor: STOPPING SIMULATION");
//            stopTimeAdvance();
//        }
    }

    /**
     * The WorldStateModel is sent to clients is centered on the agent's identifier.  If
     * it is a UnitAgent, it knows its location and the picture of the board.  Otherwise,
     * only the target locations are sent in the WorldState.
     */

    private void sendWorldStateToClients() {
        // System.out.println("\nWorldState:: SENDING " + model );
        for (Iterator iterator = clientRelays.values().iterator(); iterator.hasNext();) {
            TargetBufferRelay o = (TargetBufferRelay) iterator.next();
            String agentId = o.getSource().getAddress() ;
            EntityInfo info = referenceWorldState.getEntityInfo( agentId ) ;
            UnitEntity entity = null ;

            WorldStateModel model ;
            // Fill in the entity information iff the client is a UnitAgent (ought to start with CPY entity in this case.)
            if ( info != null && info.getEntity() instanceof UnitEntity) {
                model = referenceWorldState.filter( WorldStateModel.SENSOR_SHORT, false, false, null ) ;
                // Add only the entity corresponding to this entity.
                entity = (UnitEntity) info.getEntity().clone() ;
                model.addEntity( entity );
            }
            // For now, the suppliers are  special and have a picture of where
            // all the units are _except_ for their manuever plans.
            else if ( info != null && info.getEntity() instanceof SupplyUnit )
            {
                model = new WorldStateModel( referenceWorldState, true, true ) ;
                Iterator unitsIterator = model.getUnits() ;
                while (unitsIterator.hasNext()) {
                    UnitEntity unitEntity = (UnitEntity) unitsIterator.next();
                    unitEntity.setManueverPlan( null );
                }
            }
            else if ( agentId.startsWith( "BN" ) ) {
                model = referenceWorldState.filter( WorldStateModel.SENSOR_MEDIUM, false, false, null ) ;
            }
            else if ( agentId.startsWith( "BDE" ) ) {
                model = referenceWorldState.filter( WorldStateModel.SENSOR_LONG, false, false, null ) ;
            }
            else { // No one else has the current status.
//                System.err.println("WorldState::  Client " + agentId + " unknown.");
                model = new WorldStateModel( referenceWorldState ) ;
            }

            // Send all the events to the target.
            ArrayList events = (ArrayList) CloneUtils.deepClone( worldEvents ) ;
            UnitStatusUpdateMessage wsum = new UnitStatusUpdateMessage( entity, model, events ) ;
            wsum.setPriority( ActionEvent.PRIORITY_HIGH );
            mt.sendMessage( o.getSource(), wsum );
        }

        // Now, send the messages to the control relay. Always use the long range sensor filter.
        for (Iterator iterator = controlRelaySubscription.iterator(); iterator.hasNext();) {
            //
            ControlTargetBufferRelay relay = (ControlTargetBufferRelay) iterator.next();
            String agentId = relay.getSource().getAddress() ;
            WorldStateModel model = referenceWorldState.filter( WorldStateModel.SENSOR_LONG, false, false, null ) ;
            ArrayList events = (ArrayList) CloneUtils.deepClone( worldEvents ) ;
            log.debug( "Sending state to ControlTargetBufferRelay with " +
                    agentId + " and " + events.size() + " messages.");
            UnitStatusUpdateMessage wsum = new UnitStatusUpdateMessage( null, model, events ) ;
            wsum.setPriority( ActionEvent.PRIORITY_HIGH );
            relay.addResponse( wsum );
            if ( relay.isResponseChanged() ) {
                getBlackboardService().publishChange( relay );
            }
        }

        // Clear the world events
        worldEvents.clear();
    }

    protected void generateNewTargets() {
        // Generate any new targets
        targetGenerator.execute(referenceWorldState);
    }

    public boolean isConfigured() {
        return isConfigured;
    }

    protected void doDumpMeasurementPoints() {
        for (Iterator iterator = clientRelays.values().iterator(); iterator.hasNext();) {
            TargetBufferRelay o = (TargetBufferRelay) iterator.next();
            String agentId = o.getSource().getAddress() ;
            mt.sendMessage( o.getSource(), new PublishMPMessage() );
        }
    }

    protected void doConfigureWorld() {

        try {
            getBlackboardService().openTransaction();

            if ( worldParamConfigFileName != null ) {
                log.shout( "-------------------------------");
                log.shout( "CONFIGURING WORLD STATE from " + worldParamConfigFileName);
                Document doc = getConfigFinder().parseXMLConfigFile( worldParamConfigFileName ) ;
                VGWorldConstants.setParameterValues( doc );
                VGWorldConstants.printParameters( new PrintWriter( System.out ));
            }

            System.out.println( getAgentIdentifier() + ":: CONFIGURING AND CREATING WORLD STATE...");

            // Now, actually make the reference world state.
            WorldStateInfo info = new WorldStateInfo( boardWidth, boardHeight, penaltyHeight, recoveryHeight, deltaT) ;
            info.setAmmoConsumptionFactor( ammoConsumptionFactor );
            info.setFuelConsumptionFactor( fuelConsumptionFactor );
            info.setKillScore( killFactor );
            info.setPenaltyFactor( penaltyFactor );
            info.setViolationFactor( violationFactor );
            info.setAttritionFactor( attritionFactor );

            referenceWorldState = new ReferenceWorldState( info ) ;
            referenceWorldState.setLogEvents( true );
            MeasuredWorldMetrics metrics =
                    new MeasuredWorldMetrics( "Metrics", referenceWorldState, metricsIntegrationPeriod ) ;
            referenceWorldState.setDefaultMetric( metrics );

            System.out.println("\n---------------------------------\nWORLD CONFIGURATION:");
            System.out.println("BoardWidth=" + referenceWorldState.getBoardWidth() );
            System.out.println("BoardHeight=" + referenceWorldState.getBoardHeight() );
            System.out.println("RecoveryHeight=" + referenceWorldState.getRecoveryLine() );
            System.out.println("DeltaT=" + referenceWorldState.getDeltaT() + " secs." );
            System.out.println("PenaltyHeight=" + referenceWorldState.getPenaltyHeight() );
            System.out.println("\n\tSCORING");
            System.out.println("AttritionFactor=\t" + attritionFactor );
            System.out.println("KillFactor=\t\t" + killFactor );
            System.out.println("PenaltyFactor=\t" + penaltyFactor);
            System.out.println("ViolationFactor=\t" + violationFactor );
            System.out.println("FuelConsumptionFactor=\t" + fuelConsumptionFactor );
            System.out.println("AmmoConsumptionFactor=\t" + ammoConsumptionFactor );

            System.out.println("\n\tSIMULATION CONFIGURATION:");
            System.out.println("Number of BN units=" + numBNUnits + ", each with " + numCPYUnitsPerBN + " subordinates.");
            System.out.println("Number of CPY units (total)=" + numCPYAgents );
            System.out.println("Number of supply vehicles per agent=" + numberOfSupplyVehicles );
            System.out.println("Number of supply units=" + numberOfSupplyUnits );
            System.out.println("Simulation length=" + simulationLength );

            System.out.println("AutoResupply=" + autoResupply );
            System.out.println("MetricsIntegrationPeriod=" + metricsIntegrationPeriod );
            System.out.println("------------------------------------");

            // Make new UnitEntities locally and broadcast their values to the target.
            int cpyCount= 0 ;
            double currentZoneIndex = 0 ;
            double averageZoneSize = ( ( referenceWorldState.getUpperX() - referenceWorldState.getLowerX() ) / ( numBNUnits * zoneGridSize ) ) ;

            initialZoneWorld = new ZoneWorld( referenceWorldState, zoneGridSize ) ;

            for (int i=0;i<numBNUnits;i++) {
                // Now, break the BNs into zones.
                String id = "BN" + (i+1) ;
                String[] subentities = new String[numCPYUnitsPerBN] ;

                int lowerZoneIndex = (int ) Math.floor( currentZoneIndex ) ;
                currentZoneIndex += averageZoneSize ;
                int upperZoneIndex = (int ) Math.floor( currentZoneIndex ) - 1 ;

                // Make sure the upper zone takes up all the slack.
                if ( i == numBNUnits- 1 ) {
                   upperZoneIndex = initialZoneWorld.getNumZones() - 1 ;
                }

                IndexedZone zone = new IndexedZone( lowerZoneIndex, upperZoneIndex ) ;
                float lower = initialZoneWorld.getZoneLower( zone ) ;
                float upper = initialZoneWorld.getZoneUpper( zone ) ;

                ArrayList subordinateEntities = new ArrayList() ;

                /*
                 * Space the CPY entities evenly within the zone.
                 */
                for (int j=0;j<numCPYUnitsPerBN;j++) {
                    UnitEntity entity = referenceWorldState.addUnit( (j + 0.5 ) * ( upper - lower ) / numCPYUnitsPerBN + lower , 0, "CPY" + ( cpyCount + 1 ) ) ;
                    cpyCount ++ ;
                    subordinateEntities.add( entity.getId() ) ;
                    subentities[j] = entity.getId() ;
                }

                // Create the aggregated BN units.
                BNAggregate bnAggregate = new BNAggregate( id, subentities) ;
                bnAggregate.setCurrentZone( zone );
                initialZoneWorld.addAggUnitEntity( bnAggregate );
                // aggBNUnits.add( id ) ;
            }

            for (int i=0;i<numberOfSupplyUnits;i++) {
                String unitName =  "Supply" + (i+1)  ;

                SupplyUnit su = referenceWorldState.addSupplyUnit( unitName,
                        (i + 0.5) * referenceWorldState.getBoardWidth() /  numberOfSupplyUnits ,
                        referenceWorldState.getRecoveryLine() * 2, null, null ) ;

                double min = i * referenceWorldState.getBoardWidth() / numberOfSupplyUnits ;
                double max = ( i + 1 ) * referenceWorldState.getBoardWidth() / numberOfSupplyUnits ;
                for (int j=0;j<numberOfSupplyVehicles;j++) {
                    String vehicleName = unitName + "." + (j+1) ;
                    referenceWorldState.addSupplyVehicle(
                            min + ( j + 0.5 ) * ( max - min ) / numberOfSupplyVehicles,
                            referenceWorldState.getRecoveryLine(),
                            vehicleName ) ;
                }
            }


            if ( targetGeneratorClassName != null ) {
                Class c = Class.forName( targetGeneratorClassName ) ;
                if ( TargetGenerator.class.isAssignableFrom( c ) ) {
                    targetGenerator = (TargetGenerator) c.newInstance() ;
                    File f = getConfigFinder().locateFile( targetGeneratorConfigFile ) ;

                    // DEBUG -- Replace by call to log4j
                    System.out.println("\n---------------------------------" ) ;
                    System.out.println( "LOADING TARGET GENERATOR \nGenerator Class \t\t" + c.getName() +
                            "\nConfiguration File \t\t " + targetGeneratorConfigFile );

                    if ( f != null && f.exists() ) {
                        Document doc = getConfigFinder().parseXMLConfigFile( targetGeneratorConfigFile ) ;
                        targetGenerator.initialize( doc );
                        System.out.println("Target generator \t\t" + targetGenerator);
                    }
                    else {
                        log.error( "Could not load " + targetGeneratorConfigFile + " target generation config file.");
                    }
                    System.out.println("-----------------------------------\n");
                }
            }
            else {
                targetGenerator = new TargetGeneratorModel(referenceWorldState, 0xcafebabe, 4, 0.5f) ;
                System.out.println("\n---------------------------------" ) ;
                System.out.println("LOADING DEFAULT TARGET GENERATOR\n" + targetGenerator);
            }

            // Add an event listener so that I can broadcast interesting events to the world
            referenceWorldState.addEventListener( worldEventListener = new CPEEventListener() {
                public void notify(CPEEvent e)
                {
                   worldEvents.add( e ) ;
                }
            });

            // Now, add some measurements.

            // Publish this to the BB.
            refToWorldState = new WorldStateReference( "WorldState", referenceWorldState ) ;
            getBlackboardService().publishAdd( refToWorldState );
            uiFrame.setWorldState( referenceWorldState );

            // Add one to measure and track the data for display purposes.

            // Populate board with initial setup.
            doPopulateTargets();

            log.info( "CPESimulatorPlugin:: Initialized world state.");
//            System.out.println( getAgentIdentifier() +
//                    ":: INITIAL WORLD STATE " + referenceWorldState );
//            System.out.println( getAgentIdentifier() + ":: ZONE WORLD STATE " + initialZoneWorld );

            sendConfigureMessages();
            // sendWorldStateToClients();

            isConfigured = true ;
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }
        finally {
            // CLOSING TRANSACTION
            if ( getBlackboardService().isTransactionOpen() ) {
                getBlackboardService().closeTransaction();
            }
        }
    }

    protected void doPopulateTargets() {
        // Populate the board with targets
        if ( targetGenerator == null ) {
            return ;
        }

        WorldStateModel temp = new WorldStateModel( referenceWorldState, false, false, false ) ;
        for (int i=0;i<numInitialTargetGenerationDeltas;i++) {
            targetGenerator.execute( temp );
            temp.updateWorldState();
        }

        // Now, copy back into the ws.

        Iterator iter = temp.getTargets() ;
        while (iter.hasNext())
        {
            TargetEntity entity = (TargetEntity) iter.next();
            referenceWorldState.addEntity( ( Entity ) entity.clone() );
        }
        // Now, reset back to the initial time.
        targetGenerator.resetTime( referenceWorldState.getTime() );

        // Update the sensors since we added the targets outside of execution.
        referenceWorldState.updateSensors();
    }


    protected void stopTimeAdvance() {

        // Send start message to all interested parties.
        for (Iterator iter = clientRelays.values().iterator(); iter.hasNext();) {
            TargetBufferRelay relay = (TargetBufferRelay) iter.next();
            mt.sendMessage( relay.getSource(), new StopMessage() );
        }

        mt.clearAlarm( TIME_ADVANCE_ALARM_ID );
    }

    /**
     * Send initial configuration messages to all clients.  This version also sends the world parameter
     * document.
     */
    protected void sendConfigureMessages() {
        for (Iterator iterator = clientRelays.values().iterator(); iterator.hasNext();) {
            TargetBufferRelay o = (TargetBufferRelay) iterator.next();
            String agentId = o.getSource().getAddress() ;
            if ( agentId.startsWith( "BDE") ) {
                // Send this data.
                mt.sendMessage( o.getSource(), new ConfigureMessage( initialZoneWorld, worldParamBytes ) );
            }
            else if ( agentId.startsWith( "CPY") ) {
                WorldStateModel model = referenceWorldState.filter( WorldStateModel.SENSOR_SHORT, false, false, null ) ;
                // Add only the entity corresponding to this entity.
                EntityInfo info = referenceWorldState.getEntityInfo( agentId ) ;
                if ( info != null ) {
                    UnitEntity entity = (UnitEntity) info.getEntity().clone() ;
                    model.addEntity( entity );
                    mt.sendMessage( o.getSource(), new ConfigureMessage( model, worldParamBytes ) );
                }
            }
        }
    }

    protected void startTimeAdvance() {
        if ( !isConfigured ) {
            throw new RuntimeException( getAgentIdentifier() + " is not yet configured!" ) ;
        }

        try {
            System.out.println( getAgentIdentifier()
                    + ":: STARTING TIME ADVANCE with delta=" +
                    referenceWorldState.getDeltaT() + " secs.");

            // OPENING TRANSACTION TO SEND EVENTS.
            getBlackboardService().openTransaction();

            baseTime = System.currentTimeMillis() ;
            referenceWorldState.setBaseTime( baseTime );

            // Send start message to all interested parties.
            for (Iterator iter = clientRelays.values().iterator(); iter.hasNext();) {
                TargetBufferRelay relay = (TargetBufferRelay) iter.next();
                mt.sendMessage( relay.getSource(), new StartMessage(baseTime) );
            }

        }
        finally {
            if ( getBlackboardService().isTransactionOpen() ) {
                getBlackboardService().closeTransaction();
            }
        }

        // Reschedule for next advance.
        mt.setAlarm( "DoTimeAdvance", TIME_ADVANCE_ALARM_ID, ( long ) ( ( referenceWorldState.getDeltaT() *
                VGWorldConstants.MILLISECONDS_PER_SECOND ) /
                advanceRate ), true );
//        getAlarmService().addRealTimeAlarm( new WorldStateUpdateAlarm( System.currentTimeMillis()
//                + ( long ) ( ( referenceWorldState.getDeltaT() *
//                org.cougaar.cpe.model.VGWorldConstants.MILLISECONDS_PER_SECOND ) /
//                advanceRate ) ) ) ;
    }

    protected void setupSubscriptions() {
        // Load the configuration info.
        getConfigInfo();
        log = (LoggingService) getServiceBroker().getService( this, LoggingService.class, null ) ;
        if ( !getAgentIdentifier().getAddress().equals( Constants.WORLD_STATE_AGENT ) ) {
            if ( log != null ) {
                log.warn( "Unexpected agent name " + getAgentIdentifier() + ", expected " + Constants.WORLD_STATE_AGENT );
            }
        }

        // Subscribe to relays from UnitAgent.  Rely on the unit agents to sign in.
        relaySubscription = ( IncrementalSubscription )
                getBlackboardService().subscribe( new UnaryPredicate() {
            public boolean execute(Object o) {
                if ( o instanceof TargetBufferRelay ) {
                    return true ;
                }
                return false ;
            }
        }) ;

        // Subscribe to any control oriented target buffer relays.
        controlRelaySubscription = ( IncrementalSubscription )
                getBlackboardService().subscribe( new UnaryPredicate() {
            public boolean execute(Object o) {
                return ( o instanceof ControlTargetBufferRelay ) ;
            }
        }) ;

        mt = new GenericRelayMessageTransport( this, getServiceBroker(), getAgentIdentifier(),
                this, getBlackboardService() ) ;

       uiFrame = new CPESimulatorUIFrame( getAgentIdentifier().getAddress(), this ) ;
       uiFrame.setVisible( true );

        format = new DecimalFormat( "#0.0") ;
    }

    protected void getConfigInfo() {
        Collection params = getParameters() ;
        Vector paramVector = new Vector( params ) ;
        LoggingService log = (LoggingService)
                getServiceBroker().getService( this, LoggingService.class, null ) ;

        String fileName = null ;

        // Process the world configuration.
        if ( paramVector.size() >= 1 ) {
            fileName = ( String ) paramVector.elementAt(0) ;
            loadWorldConfiguration(fileName, log);
        }

        // Load the target generator.
        if ( paramVector.size() >= 3 ) {
            targetGeneratorClassName = (String) paramVector.elementAt(1) ;
            if ( targetGeneratorClassName != null && targetGeneratorClassName.equals( "null") ) {
                targetGeneratorClassName = null ;
            }
            targetGeneratorConfigFile = (String) paramVector.elementAt(2) ;
        }

        // Load the world configuration static parameters.
        if ( paramVector.size() >= 4 ) {
            worldParamConfigFileName = (String) paramVector.elementAt( 3 ) ;
            if ( worldParamConfigFileName != null && worldParamConfigFileName.equals("null") ) {
               worldParamConfigFileName = null ;
            }

            // Try and load the world configuration file.
            if ( worldParamConfigFileName != null ) {
                File f = getConfigFinder().locateFile( worldParamConfigFileName ) ;
                if ( f.exists() && f.canRead() ) {
                    try
                    {
                        FileInputStream fis = new FileInputStream( f ) ;
                        byte[] buf = new byte[ fis.available() ] ;
                        int len = fis.read( buf ) ;
                        worldParamBytes = buf ;
                    }
                    catch (FileNotFoundException e)
                    {
                        e.printStackTrace();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void loadWorldConfiguration(String fileName, LoggingService log)
    {
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
                                String boardWidthValue = getNodeValueForTag(doc, "BoardWidth", "value" ) ;
                                if ( boardWidthValue != null ) {
                                    boardWidth = Double.parseDouble( boardWidthValue ) ;
                                }

                                String boardHeightValue = getNodeValueForTag(doc, "BoardHeight", "value") ;
                                if ( boardHeightValue != null ) {
                                    boardHeight = Double.parseDouble( boardHeightValue ) ;
                                }

                                String penaltyHeightValue = getNodeValueForTag(doc, "PenaltyHeight", "value" ) ;
                                if ( penaltyHeightValue != null ) {
                                    penaltyHeight = Double.parseDouble( penaltyHeightValue ) ;
                                }

                                String recoveryHeightValue = getNodeValueForTag(doc, "RecoveryHeight", "value" ) ;
                                if ( recoveryHeightValue != null ) {
                                    recoveryHeight = Double.parseDouble( recoveryHeightValue ) ;
                                    if ( recoveryHeight > 0  ) {
                                        System.err.println("Warning: Recovery height must be < 0, found " + recoveryHeight );
                                    }
                                }
                                else {
                                    System.err.println( getAgentIdentifier() +
                                            "::No RecoveryHeight parameter, using default " + recoveryHeight );
                                }
                                String deltaTValue = getNodeValueForTag( doc, "DeltaT", "value" ) ;
                                if ( deltaTValue != null ) {
                                    deltaT = Double.parseDouble( deltaTValue ) ;
                                }

                                String numberOfBNAgents = getNodeValueForTag( doc, "NumberOfBNUnits", "value" ) ;
                                if ( numberOfBNAgents != null ) {
                                    numBNUnits = Integer.parseInt( numberOfBNAgents ) ;
                                }

                                String numberOfUnitAgents = getNodeValueForTag(doc, "NumberOfCPYUnitsPerBN", "value" ) ;
                                if ( numberOfUnitAgents != null ) {
                                    numCPYUnitsPerBN = Integer.parseInt( numberOfUnitAgents ) ;
                                }

                                numCPYAgents = numBNUnits * numCPYUnitsPerBN ;

                                String zoneGridSizeString = getNodeValueForTag( doc, "ZoneGridSize", "value" ) ;
                                if ( zoneGridSizeString != null ) {
                                    zoneGridSize = Float.parseFloat( zoneGridSizeString ) ;
                                }

                                String numberOfTargetsValue =getNodeValueForTag(doc, "InitialNumberOfTargets", "value" ) ;
                                if ( numberOfTargetsValue != null ) {
                                    initialNumberOfTargets = Integer.parseInt( numberOfTargetsValue ) ;
                                }
                                String targetPopupProbValue = getNodeValueForTag(doc, "TargetPopupProbability", "value" ) ;
                                if ( targetPopupProbValue != null ) {
                                     targetPopupProb = Double.parseDouble( targetPopupProbValue ) ;
                                }

                                String numberOfSupplyVehiclesValue = getNodeValueForTag( doc, "NumberOfSupplyVehicles", "value") ;
                                if ( numberOfSupplyVehiclesValue != null ) {
                                    numberOfSupplyVehicles = Integer.parseInt( numberOfSupplyVehiclesValue ) ;
                                }

                                String numberOfSupplyUnitsValue = getNodeValueForTag( doc, "NumberOfSupplyUnits", "value" )  ;
                                if ( numberOfSupplyUnitsValue != null ) {
                                    numberOfSupplyUnits = Integer.parseInt( numberOfSupplyUnitsValue ) ;
                                }

                                String simulationLengthValue = getNodeValueForTag( doc, "SimulationDuration", "value" ) ;
                                if ( simulationLengthValue != null ) {
                                    simulationLength = Long.parseLong( simulationLengthValue ) ;
                                }

                                String autoResupplyValue = getNodeValueForTag( doc, "AutoResupply", "value") ;
                                if ( autoResupplyValue != null ) {
                                    try {
                                        autoResupply = Boolean.valueOf( autoResupplyValue ).booleanValue()  ;
                                    }
                                    catch ( Exception e ) {
                                        e.printStackTrace();
                                    }
                                }
                                String attritionFactorValue = getNodeValueForTag( doc, "AttritionFactor", "value" ) ;
                                if ( attritionFactorValue != null ) {
                                    try {
                                        attritionFactor = Float.valueOf( attritionFactorValue ).floatValue() ;
                                    }
                                    catch ( Exception e ) {
                                        e.printStackTrace();
                                    }
                                }
                                String killFactorValue = getNodeValueForTag( doc, "KillFactor", "value" ) ;
                                if ( killFactorValue != null ) {
                                    try {
                                        killFactor = Float.valueOf( killFactorValue).floatValue() ;
                                    }
                                    catch ( Exception e ) {
                                        e.printStackTrace();
                                    }
                                }
                                String penaltyFactorValue = getNodeValueForTag( doc, "PenaltyFactor", "value" ) ;
                                if ( penaltyFactorValue != null ) {
                                    try {
                                        penaltyFactor = Float.valueOf( penaltyFactorValue).floatValue() ;
                                    }
                                    catch ( Exception e ) {
                                        e.printStackTrace();
                                    }
                                }
                                String violationFactorValue = getNodeValueForTag( doc, "ViolationFactor", "value" ) ;
                                if ( violationFactorValue != null ) {
                                    try {
                                        violationFactor = Float.valueOf( violationFactorValue).floatValue() ;
                                    }
                                    catch ( Exception e ) {
                                        e.printStackTrace();
                                    }
                                }
                                String fuelConsumptionFactorValue = getNodeValueForTag( doc, "FuelConsumptionFactor", "value" ) ;
                                if ( fuelConsumptionFactorValue != null ) {
                                    try {
                                        fuelConsumptionFactor = Float.valueOf( fuelConsumptionFactorValue).floatValue() ;
                                    }
                                    catch ( Exception e ) {
                                        e.printStackTrace();
                                    }
                                }
                                String ammoConsumptionFactorValue = getNodeValueForTag( doc, "AmmoConsumptionFactor", "value" ) ;
                                if ( ammoConsumptionFactorValue != null ) {
                                    try {
                                        ammoConsumptionFactor = Float.valueOf( ammoConsumptionFactorValue).floatValue() ;
                                    }
                                    catch ( Exception e ) {
                                        e.printStackTrace();
                                    }
                                }

                                String metricsIntegrationPeriodValue = getNodeValueForTag( doc, "MetricsIntegrationPeriod", "value" ) ;
                                if ( metricsIntegrationPeriodValue != null ) {
                                    try {
                                        metricsIntegrationPeriod = Integer.parseInt( metricsIntegrationPeriodValue ) ;
                                    }
                                    catch (Exception e ) {
                                        e.printStackTrace();
                                    }
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

        } catch ( Exception e ) {
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



    // Create the update alarm.
    protected class WorldStateUpdateAlarm implements Alarm {

        public WorldStateUpdateAlarm(long expirationTime) {
            this.expirationTime = expirationTime;
        }

        public boolean cancel() {
            return false;
        }

        public void expire() {
            expired = true ;
            DoTimeAdvance( null ) ;
        }

        public long getExpirationTime() {
            return expirationTime;
        }

        public boolean hasExpired() {
            return expired;
        }

        long expirationTime ;
        boolean expired ;
    };

    private CPESimulatorUIFrame uiFrame;
    GenericRelayMessageTransport mt ;

    /**
     * Map parameters.
     */
    double boardWidth, boardHeight, penaltyHeight, recoveryHeight ;

    /**
     * Initialization parameters.
     */
    int numCPYAgents, initialNumberOfTargets ;

    int numberOfSupplyVehicles, numberOfSupplyUnits ;

    double deltaT = 5 ;

    /*
     * Scoring parameters.
     */
    float attritionFactor = 1, killFactor = 15, penaltyFactor = 5, violationFactor = 50,
        fuelConsumptionFactor=0.2f, ammoConsumptionFactor = 0.1f ;

    /**
     * Arrival rate.
     */
    double targetPopupProb = 0.2 ;

    ReferenceWorldState referenceWorldState ;
    HashMap clientRelays = new HashMap() ;
    boolean isTimeAdvancing = false ;
}
