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
import org.cougaar.cpe.model.*;
import org.cougaar.cpe.relay.GenericRelayMessageTransport;
import org.cougaar.cpe.relay.MessageSink;
import org.cougaar.cpe.relay.TargetBufferRelay;
import org.cougaar.cpe.relay.TimerMessage;
import org.cougaar.cpe.planning.zplan.BNAggregate;
import org.cougaar.cpe.planning.zplan.ZoneWorld;
import org.cougaar.tools.techspecs.qos.*;
import org.cougaar.tools.techspecs.events.MessageEvent;
import org.cougaar.tools.techspecs.events.ActionEvent;
import org.cougaar.tools.techspecs.events.TimerEvent;

import java.io.*;
import java.util.*;
import java.lang.reflect.Method;

/**
 * Responsible for initializing the actual world state.
 */

public class CPESimulatorPlugin extends ComponentPlugin implements MessageSink {
    private IncrementalSubscription relaySubscription;
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
    private TargetGeneratorModel targetGeneratorModel;

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
                System.err.println( getAgentIdentifier() + ":: WARNING No entity found for incoming message from " + mpm.getSource() );
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
                System.out.println("UPDATING " + sentity.getId() );
                System.out.println("OLD PLAN " + sentity.getSupplyPlan()  ) ;
                System.out.println("NEW PLAN " + entry.getValue() );
                sentity.updateSupplyPlan( (Plan) entry.getValue() );
                System.out.println("MERGED PLAN " + sentity.getSupplyPlan() );
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

    /**
     * The callback that handles time advances.
     *
     * @param m
     */
    public void DoTimeAdvance( TimerEvent m ) {
        double rate ;

        rate =  ( referenceWorldState.getDeltaT() * VGWorldConstants.MILLISECONDS_PER_SECOND ) / ( System.currentTimeMillis() - lastTime ) ;

        lastTime = System.currentTimeMillis() ;
        long elapsedTime = lastTime - baseTime ;

        System.out.println( getAgentIdentifier() +
                "::WorldStateExecutorPlugin:: ADVANCING time at simTime=" + referenceWorldState.getTime() +
                ", elapsed=" + elapsedTime * VGWorldConstants.SECONDS_PER_MILLISECOND +
                ", advanceRate =" + rate + "x Real Time" ) ;

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

            UnitStatusUpdateMessage wsum = new UnitStatusUpdateMessage( entity, model ) ;
            wsum.setPriority( ActionEvent.PRIORITY_HIGH );
            mt.sendMessage( o.getSource(), wsum );
        }
    }

    protected void generateNewTargets() {
        // Generate any new targets
        targetGeneratorModel.execute();
    }

    private void placeTarget() {
        double targetLocation = targetGenerator.nextDouble() *
                referenceWorldState.getBoardWidth() ;
        TargetEntity t = referenceWorldState.addTarget( targetLocation, referenceWorldState.getBoardHeight(),
                0, -VGWorldConstants.getTargetMoveRate() ) ;
        System.out.println("WorldState:: GENERATED TARGET AT " + t );
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

            System.out.println( getAgentIdentifier() + ":: CONFIGURING AND CREATING WORLD STATE...");

            // Now, actually make the reference world state.
            referenceWorldState = new ReferenceWorldState( boardWidth, boardHeight, penaltyHeight, recoveryHeight, deltaT ) ;
            referenceWorldState.setLogEvents( true );
            uiFrame.setWorldState( referenceWorldState );

            System.out.println("\n---------------------------------\nWORLD CONFIGURATION:");
            System.out.println("BoardWidth=" + referenceWorldState.getBoardWidth() );
            System.out.println("BoardHeight=" + referenceWorldState.getBoardHeight() );
            System.out.println("RecoveryHeight=" + referenceWorldState.getRecoveryLine() );
            System.out.println("DeltaT=" + referenceWorldState.getDeltaT() + " secs." );
            System.out.println("PenaltyHeight=" + referenceWorldState.getPenaltyHeight() );

            System.out.println("\nSIMULATION CONFIGURATION:");
            System.out.println("Number of BN units=" + numBNUnits + ", each with " + numCPYUnitsPerBN + " subordinates.");
            System.out.println("Number of CPY units (total)=" + numCPYAgents );
            System.out.println("Number of supply vehicles per agent=" + numberOfSupplyVehicles );
            System.out.println("Number of supply units=" + numberOfSupplyUnits );
            System.out.println("Simulation length=" + simulationLength );
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

            targetGeneratorModel = new TargetGeneratorModel(referenceWorldState, 0xcafebabe, 4, 0.5f) ;

            // Populate board with initial setup.
            doPopulateTargets();

            log.info( "CPESimulatorPlugin:: Initialized world state.");
//            System.out.println( getAgentIdentifier() +
//                    ":: INITIAL WORLD STATE " + referenceWorldState );
//            System.out.println( getAgentIdentifier() + ":: ZONE WORLD STATE " + initialZoneWorld );

            sendConfigureMessages();
            sendWorldStateToClients();

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
        WorldStateModel temp = new WorldStateModel( referenceWorldState, false, false, false ) ;
        for (int i=0;i<numInitialTargetGenerationDeltas;i++) {
            targetGeneratorModel.execute( temp );
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
        targetGeneratorModel.resetTime( referenceWorldState.getTime() );

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
     * Send initial configuration messages to all clients.
     */
    protected void sendConfigureMessages() {
        for (Iterator iterator = clientRelays.values().iterator(); iterator.hasNext();) {
            TargetBufferRelay o = (TargetBufferRelay) iterator.next();
            String agentId = o.getSource().getAddress() ;
            if ( agentId.startsWith( "BDE") ) {
                ConfigureMessage msg = new ConfigureMessage( initialZoneWorld ) ;
                // Send this data.
                ByteArrayOutputStream bos = new ByteArrayOutputStream() ;
                try
                {
                    ObjectOutputStream oos = new ObjectOutputStream( bos ) ;
                    oos.writeObject( msg );
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                mt.sendMessage( o.getSource(), msg  );
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

        mt = new GenericRelayMessageTransport( this, getServiceBroker(), getAgentIdentifier(),
                this, getBlackboardService() ) ;

       uiFrame = new CPESimulatorUIFrame( getAgentIdentifier().getAddress(), this ) ;
       uiFrame.setVisible( true );
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

    /**
     * Arrival rate.
     */
    double targetPopupProb = 0.2 ;
    Random targetGenerator = new Random(0) ;

    ReferenceWorldState referenceWorldState ;
    HashMap clientRelays = new HashMap() ;
    boolean isTimeAdvancing = false ;
}