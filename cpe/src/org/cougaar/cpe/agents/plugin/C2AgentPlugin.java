package org.cougaar.cpe.agents.plugin;

import org.cougaar.core.adaptivity.OMCPoint;
import org.cougaar.core.adaptivity.OMCRangeList;
import org.cougaar.core.adaptivity.OperatingModeCondition;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.plugin.ComponentPlugin;
import org.cougaar.core.relay.Relay;
import org.cougaar.core.service.LoggingService;
import org.cougaar.core.service.UIDService;
import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.planning.ldm.plan.Role;
import org.cougaar.util.ConfigFinder;
import org.cougaar.util.UnaryPredicate;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.cougaar.cpe.agents.messages.*;
import org.cougaar.cpe.agents.qos.QoSConstants;
import org.cougaar.cpe.agents.Constants;
import org.cougaar.cpe.model.*;
import org.cougaar.cpe.mplan.ManueverPlanner;
import org.cougaar.tools.techspecs.qos.*;
import org.cougaar.cpe.relay.GenericRelayMessageTransport;
import org.cougaar.cpe.relay.MessageSink;
import org.cougaar.cpe.relay.SourceBufferRelay;
import org.cougaar.cpe.relay.TargetBufferRelay;
import org.cougaar.cpe.util.StandardAlarm;
import org.cougaar.cpe.util.CPUConsumer;
import org.cougaar.cpe.util.ConfigParserUtils;
import org.cougaar.cpe.planning.zplan.ZoneTask;
import org.cougaar.tools.techspecs.events.MessageEvent;
import org.cougaar.tools.techspecs.events.TimerEvent;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

public class C2AgentPlugin extends ComponentPlugin implements MessageSink {
    private IncrementalSubscription operatingModeSubscription;

    private OMCRangeList updateStateConditionList = new OMCRangeList( new int[] { 10000, 15000, 20000, 25000, 30000, 35000, 40000 } ) ;

    private OMCRangeList planningDepthList = new OMCRangeList( new int[] { 3, 4, 5, 6, 7 } ) ;

    private OMCRangeList planningBreadthList = new OMCRangeList( new int[] { 10, 20, 30, 40, 50, 60, 70, 80 } ) ;

    /**
     * Replanning periods, given in ms. Generally, the replanning time must be greater than the planning horizon.
     */
    private OMCRangeList replanPeriodList = new OMCRangeList( new int[] { 20000, 30000, 40000, 50000, 60000, 70000, 80000 } );

    /**
     * These are integrative values of deltaT.
     */
    private OMCRangeList planningDelayList = new OMCRangeList( new int[] { 0, 5000, 10000, 15000, 20000, 25000, 30000 } ) ;

    private long targetWakeupTime;
    private DelayMeasurementPoint replanTimerDelayMP;
    /**
     * Measurement point for replanning times.
     */
    private DelayMeasurementPoint replanTimeMP;

    /**
     * Number of instructions to process update status.
     */
    private int updateStatusNIU = 0 ;
    private boolean started = false ;
    private EventDurationMeasurementPoint updateUnitStatusDelayMP;
    private byte[] configBytes;
    private Plan zoneSchedule;
    private String UPDATE_WORLD_STATE_TIMER = "UpdateWorldStateTimer";
    private LoggingService log;

    public long getReplanPeriodInMillis() {
        if ( replanPeriodCondition != null ) {
            Comparable value = replanPeriodCondition.getValue() ;
            if ( value instanceof Integer ) {
                return ( ( Integer ) value ).intValue() ;
            }
        }
        throw new RuntimeException( "Could not calculate replan period.") ;
    }

    public int getNumDeltasPerTask() {
        return numDeltasPerTask;
    }

    public int getNumIncrementsPerPlan() {
        return numIncrementsPerPlan;
    }

    public int getUpdateWorldStatePeriod() {
        if ( updateStatePeriodCondition != null ) {
            Comparable value = updateStatePeriodCondition.getValue() ;
            if ( value instanceof Integer ) {
                return ( ( Integer ) value ).intValue() ;
            }
        }
        throw new RuntimeException( "Unknown update state period condition." ) ;
    }

    public int getPlanningDelay() {
        if ( planningDelayCondition != null ) {
            Comparable value = planningDelayCondition.getValue() ;
            if ( value instanceof Integer ) {
             return ( ( Integer ) value ).intValue() ;
            }
        }
        throw new RuntimeException( "Unknown planning delay." ) ;
    }

    public int getSearchDepth() {
        if ( planningDepthCondition != null ) {
            Comparable value =  planningDepthCondition.getValue() ;
            if ( value instanceof Integer ) {
                return ( ( Integer ) value ).intValue() ;
            }
        }
        throw new RuntimeException( "Unknown search depth." ) ;
    }

    public int getMaxBreadth() {
        if ( planningBreadthCondition != null ) {
            Comparable value =  planningBreadthCondition.getValue() ;
            if ( value instanceof Integer ) {
                return ( ( Integer ) value ).intValue() ;
            }
        }
        throw new RuntimeException( "Unknown planning breadth level." ) ;
    }

    public void processMessage(Object o) {
        //System.out.println("C2Agent " + getAgentIdentifier() + " RECEIVED MESSAGE " + o );

        if ( o instanceof MessageEvent ) {
            MessageEvent cm = (MessageEvent) o ;
            Relay r = (Relay) subordinateRelays.get( cm.getSource().getAddress() ) ;
            if ( r != null ) {
                processMessagesFromSubordinates( cm );
            }
            else if ( cm.getSource().getAddress().equals( "WorldState") ) {
                processMessageFromWorldState( cm );
            }
        }
    }

    /**
     * Note that this is called by both timers and by Cougaar itself.   This isn't perfect, since it
     * would be better if it was only called by Cougaar.
     */
    protected void execute() {
        // System.out.println("\n\nC2AgentPlugin:: " + getAgentIdentifier() + " EXECUTING...");
        processOrganizations();

        gmrt.execute( getBlackboardService() );
        // display.execute();
    }

    private void processOrganizations() {
        //  Find subordinates signing in.
        Collection addedCollection = findOrgsSubscription.getAddedCollection() ;
        ArrayList self = new ArrayList(),
                newSubordinates = new ArrayList(),
                newSuperiors = new ArrayList() ;

        OrganizationHelper.processSuperiorAndSubordinateOrganizations( getAgentIdentifier(),
                addedCollection, newSuperiors, newSubordinates, self );

        // Make relays to all new subordinates organizations.
        boolean subordinatesAdded = false ;
        for (int i = 0; i < newSubordinates.size(); i++) {
            Organization subOrg = (Organization) newSubordinates.get(i) ;
            if ( subordinateRelays.get( subOrg.getMessageAddress().getAddress() ) == null ) {
                SourceBufferRelay relay = gmrt.addRelay( subOrg.getMessageAddress() ) ;
                subordinateRelays.put( subOrg.getMessageAddress().getAddress(), relay ) ;
                System.out.println(getAgentIdentifier() + " adding relay to subordinate "
                        + subOrg.getMessageAddress() );
            }

            if ( subOrg.getOrganizationPG().inRoles( Role.getRole("Combat") ) ) {
                System.out.println( getAgentIdentifier() + ":: ADDING subordinate combat organization " + subOrg);
                subordinateCombatOrganizations.put( subOrg.getMessageAddress().getAddress(), subOrg ) ;
                subordinatesAdded = true ;
            }
        }

        // Find superior orgs.
        for (int i = 0; i < newSuperiors.size(); i++) {
            Organization organization = (Organization) newSuperiors.get(i);
            if ( superior == null ) {
                superior = organization ;
            }
            else if ( !superior.getMessageAddress().getAddress().equals( organization.getMessageAddress().getAddress() )) {
                System.err.println("WARNING: More than one superior " +
                        superior + " and " + organization + " found for " + getAgentIdentifier() );
            }
        }

        // Find any new relays from one's superior.
        Collection relayCollection = targetRelaySubscription.getAddedCollection() ;
        Iterator iter = relayCollection.iterator() ;
        while (iter.hasNext()) {
            TargetBufferRelay relay = (TargetBufferRelay) iter.next();
            if ( relayFromSuperior == null ) {
                System.out.println( getAgentIdentifier() + " found relay from " + relay.getSource() );
                relayFromSuperior = relay ;
            }
        }
    }

    private void createWorldStateFreshnessMP() {
        ArrayList measurementPointNames = new ArrayList() ;
        for (Iterator iterator = subordinateCombatOrganizations.values().iterator(); iterator.hasNext();) {
            Organization organization = (Organization) iterator.next();
            measurementPointNames.add( organization.getMessageAddress().getAddress() + ".WorldStateTimestamp" ) ;
        }
        Collections.sort( measurementPointNames, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ( ( String ) o1).compareTo( ( String ) o2 ) ;
            }
        });
        MeasurementPoint[] mp = new MeasurementPoint[ measurementPointNames.size() + 2 ] ;
        int i=0;
        for (i = 0; i < measurementPointNames.size(); i++) {
            String s = (String)measurementPointNames.get(i);
            mp[i]  = new TimestampMeasurementPoint( (String) measurementPointNames.get(i) ) ;
            //getBlackboardService().publishAdd( mp[i] );
            System.out.println( getAgentIdentifier() + ":: DEBUG:: Created " + mp[i] );
        }

        // Make the measurement points based on the operating mode conditions.
        mp[i++] = new OMCMeasurementPoint( "PlanningDepth",
                planningDepthCondition.getAllowedValues() ) ;
        mp[i++] = new OMCMeasurementPoint( "PlanningBreadth",
                planningBreadthCondition.getAllowedValues() ) ;

        // Make the vector measurement point for the entire world state.
        worldStateFreshnessMeasurementPoint =
                new VectorMeasurementPoint( getAgentIdentifier(), "WorldStateFreshness",
                                            mp ) ;

        getBlackboardService().publishAdd( worldStateFreshnessMeasurementPoint );
        System.out.println( getAgentIdentifier() + ":: DEBUG:: Created " + worldStateFreshnessMeasurementPoint );
    }

    protected void processMessageFromWorldState( MessageEvent message ) {
         if ( message instanceof StartMessage ) {
             processStartTimeMessage( ( StartMessage ) message ) ;
         }
         else if ( message instanceof StopMessage ) {
             started = false;
         }
         else if ( message instanceof UnitStatusUpdateMessage ) {
             UnitStatusUpdateMessage wsum = (UnitStatusUpdateMessage) message ;
             // WorldStateModel newModel = wsum.getWorldState() ;
             // System.out.println( getAgentIdentifier() + ":: RECEIVED NEW WORLD STATE " + newModel );

             // Now, copy the current perceptions of entity position into the model if the
             // model already exists.

             // NOTE The unit entities have to be cloned because they retain the old manuever plan information
             // and the old state.  The WorldState UnitStatusUpdateMessage does not provide them.
             if ( perceivedWorldState == null ) {
                 setPerceivedWorldState( wsum.getWorldState() ) ;
             }
             else {
                 processUpdateFromWorldStateAgent( wsum);
             }
         }
         else if ( message instanceof PublishMPMessage ) {
             BundledMPMessage bmm = new BundledMPMessage() ;
             bmm.addData( replanTimeMP ) ;
             bmm.addData( replanTimerDelayMP ) ;
             bmm.addData( updateUnitStatusDelayMP );
             Collection c = updateStatusDelayMeasPoints.values() ;
             for (Iterator iterator = c.iterator(); iterator.hasNext();)
             {
                 MeasurementPoint point = (MeasurementPoint) iterator.next();
                 bmm.addData( point );
             }

             // Send the configuration information.
             Collection params = getParameters() ;
             Vector paramVector = new Vector( params ) ;
             String fileName = null ;
             if ( paramVector.size() > 0 && configBytes != null  ) {
                 fileName = ( String ) paramVector.elementAt(0) ;
                 bmm.addData( fileName, configBytes);
             }

             gmrt.sendMessage( message.getSource(), bmm );
         }
    }

    protected void processUpdateFromWorldStateAgent( UnitStatusUpdateMessage event ) {
        WorldStateModel newSensedWorldState = event.getWorldState() ;
        WorldStateUtils.mergeWorldStateWithReference( perceivedWorldState, newSensedWorldState );
        perceivedWorldState.setTime( newSensedWorldState );

        getBlackboardService().publishChange( perceivedWorldStateRef );
    }


    protected void setPerceivedWorldState( WorldStateModel model ) {
        perceivedWorldState = model ;
        perceivedWorldStateRef.setState( model );
        getBlackboardService().publishChange( perceivedWorldStateRef );
    }

    private void processStartTimeMessage( StartMessage message ) {
        baseTime = message.getBaseTime() ;
        started = true ;

        ArrayList subordinateEntities = new ArrayList() ;

        // Compile a list of subordinate entities and their delay measurement points for
        // the world state update message.
        for (Iterator iterator = subordinateCombatOrganizations.values().iterator();
             iterator.hasNext();)
        {
            Organization organization = (Organization) iterator.next();
            subordinateEntities.add( organization.getMessageAddress().getAddress() ) ;
            DelayMeasurementPoint newDelayMP =
                    new DelayMeasurementPoint(
                            organization.getMessageAddress().getAddress()
                            + "." + "UnitUpdateStatusDelay" ) ;
            updateStatusDelayMeasPoints.put(
                    organization.getMessageAddress().getAddress(),
                    newDelayMP ) ;
            // Publish this to the blackboard.
            getBlackboardService().publishAdd( newDelayMP );
        }

        // Create the MP for the vector measurement of world state freshness.
        createWorldStateFreshnessMP();

        // Create the manuever planner.
        mp = new ManueverPlanner( getAgentIdentifier().getAddress(), subordinateEntities ) ;
        // Number of deltas per planning cycle.
        mp.setDeltaValues( getNumDeltasPerTask(), getNumIncrementsPerPlan() );

        // Calculate the current planning horizon.
        long planningHorizon = (getSearchDepth() * getNumDeltasPerTask() * perceivedWorldState.getDeltaTInMS() ) ;
        if ( getReplanPeriodInMillis() > planningHorizon ) {
            System.err.println("WARNING:: Planning horizon " + planningHorizon * VGWorldConstants.SECONDS_PER_MILLISECOND +
                    " secs is less than replan time ");
        }

//        replanDeltas = (int) Math.ceil( ( 4 * getNumDeltasPerTask() * 3f ) / 4f ) ;
//        replanUpdatePeriod = ( long ) ( replanDeltas * perceivedWorldState.getDeltaT() * 1000 ) ;

//        if ( subordinateCombatOrganizations.size() > 0 ) {
//            System.out.println("C2Agent " + getAgentIdentifier() +
//                    ":: PLANNING FOR SUBORDINATES " + subordinateCombatOrganizations
//                    + " with WorldState=" + perceivedWorldState);
//            planAndDistribute();
//        }

        // Start using the base time.
        System.out.println( getAgentIdentifier() + "::STARTING TIME ADVANCE with task granularity= " +
                getNumDeltasPerTask() * perceivedWorldState.getDeltaT() + " secs." +
                " and replanUpdatePeriod= " + ( getReplanPeriodInMillis() * VGWorldConstants.SECONDS_PER_MILLISECOND ) );

        // The first time replan immediately to generate an initial plan.
        System.out.println("Initial replanning at time " );
        getAlarmService().addRealTimeAlarm( new ReplanAlarm( baseTime + 1000 ) ) ;

        // Set the alarm for updating the world state.
        gmrt.setAlarm( "DoUpdateWorldStateTimer", UPDATE_WORLD_STATE_TIMER, getUpdateWorldStatePeriod(), true );

        targetWakeupTime = baseTime + 1000 ;
    }

    public void DoUpdateWorldStateTimer( TimerEvent te ) {
        log.shout( getAgentIdentifier() + " updating world state to superior.");
        gmrt.sendMessage( superior.getMessageAddress(),
                new UnitStatusUpdateMessage( null, ( WorldStateModel ) perceivedWorldState.clone()) ) ;
    }

    public void planAndDistribute() {
        long startTime = System.currentTimeMillis() ;

        boolean wasOpen = false ;
        try {
            if ( !lockBlackboardWhilePlanning && getBlackboardService().isTransactionOpen() ) {
                getBlackboardService().closeTransaction();
                wasOpen = true ;
            }

            // Set the new search parameters.
            int searchDepth = getSearchDepth(), searchBreadth = getMaxBreadth(), planDelay = getPlanningDelay() ;
            System.out.println( getAgentIdentifier() + ":: PLANNING WITH SearchDepth="
                    + searchDepth + ", SearchBreadth=" + searchBreadth );
            mp.setMaxDepth( searchDepth );
            mp.setMaxBranchFactor( searchBreadth );
            mp.plan( perceivedWorldState, planDelay, zoneSchedule );
            Object[][] plans = mp.getPlans() ;
            mp.release() ;

            if ( plans != null ) {
                System.out.println("Plans at time " + perceivedWorldState.getTime() );
                for (int i = 0; i < plans.length; i++) {
                    Object[] plan = plans[i];
                    System.out.println( plan[0] + "=" );
                    System.out.println( plan[1] );
                }
                long endTime = System.currentTimeMillis() ;
                System.out.println( getAgentIdentifier() +
                        "::PLANNING ELAPSED TIME="
                        + (( endTime - startTime ) * VGWorldConstants.SECONDS_PER_MILLISECOND )
                        + " secs...");

                manueverPlan = plans ;

                // Open the bb for writing.
                if ( !getBlackboardService().isTransactionOpen() ) {
                    getBlackboardService().openTransaction();
                }
                for (int i=0;i<plans.length;i++) {
                    EntityInfo info = perceivedWorldState.getEntityInfo( ( String ) plans[i][0]) ;
                    Plan p = (Plan) plans[i][1] ;
                    UnitEntity entity = (UnitEntity) info.getEntity() ;
                    entity.setManueverPlan( p ) ;

                    // Now, send this to the subordinate if it exists.
                    ManueverPlanMessage mpm = new ManueverPlanMessage( entity.getId(), p ) ;

                    // Add the start timer timestamp.
                    TimestampMeasurement tm = new TimestampMeasurementImpl( null,
                            QoSConstants.TIMER_ACTION, getAgentIdentifier(),
                            startTime ) ;
                    mpm.getMeasurements().addMeasurement( tm );

                    // Measure the additional planning QoS.
                    measureSendManueverPlan( mpm );

                    if ( subordinateCombatOrganizations.get( entity.getId()) != null ) {
                        log.shout( "Sending " + mpm.getPlan() + " to " + entity.getId() );
                        gmrt.sendMessage( MessageAddress.getMessageAddress( entity.getId() ), mpm ) ;
                    }
                }
            }
            else {
                System.out.println( getAgentIdentifier() + "NO PLANS available.");
            }
        }
        finally {
            if ( wasOpen && !getBlackboardService().isTransactionOpen() ) {
                getBlackboardService().openTransaction();
            }
        }
    }

    private void measureSendManueverPlan( ManueverPlanMessage mpm ) {
//        VectorMeasurement vm = new VectorMeasurement( QoSConstants.TIMER_ACTION, QoSConstants.PLAN_MANUEVER_ACTION,
//                getAgentIdentifier() ) ;


        // Create a vector of freshness measures by scanning all of the StatusDelay
        // measurement points.
        String[] names = new String[ updateStatusDelayMeasPoints.size() ] ;
        Measurement[] measurements = new Measurement[updateStatusDelayMeasPoints.size() ] ;
        Collection c = updateStatusDelayMeasPoints.entrySet() ;
        int i= 0 ;
        for (Iterator iterator = c.iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            names[i] = (String) entry.getKey() + ".WorldStateTimestamp";
            MeasurementPoint mp = (MeasurementPoint) entry.getValue() ;
            measurements[i] = mp.getLastMeasurement();
            i++ ;
        }

        // Add a vector measurement consisting of individual world state freshness
        worldStateFreshnessMeasurementPoint.addMeasurement(
                QoSConstants.TIMER_ACTION,
                QoSConstants.PLAN_MANUEVER_ACTION,
                names, measurements );
        Measurement freshnessMeasurement =
                worldStateFreshnessMeasurementPoint.getLastMeasurement() ;

        // This is the vector freshness associated with the planning results.
        mpm.getMeasurements().addMeasurement( freshnessMeasurement );

        // This is the ending timestamp.
        TimestampMeasurementImpl tm =
                new TimestampMeasurementImpl( QoSConstants.PLAN_MANUEVER_ACTION,
                QoSConstants.SEND_ACTION,
                        getAgentIdentifier(), System.currentTimeMillis() ) ;
        mpm.getMeasurements().addMeasurement( tm );
        //System.out.println("DEBUG:: MANUEVER PLAN MEASUREMENTS " + mpm.getMeasurements() );
    }

    private void processReplanTimer() {
        // Replan.
        //getBlackboardService().openTransaction();

        // Lock the black board in this version to prevent messages from being fired!
        boolean wasOpen = true ;
        if ( !getBlackboardService().isTransactionOpen() ) {
            wasOpen = false ;
            getBlackboardService().openTransaction();
        }

        long lastWakeupTime = targetWakeupTime ;
        long startTime = System.currentTimeMillis() ;
        replanTimerDelayMP.addMeasurement( new DelayMeasurement( "ReplanTimerFired", "ProcessReplan",
                getAgentIdentifier(), lastWakeupTime, startTime ) ) ;

        // Calculate the current planning horizon and make sure it is okay.
        long planningHorizon = (long) (getSearchDepth() * getNumDeltasPerTask() * perceivedWorldState.getDeltaTInMS() ) ;
        if ( getReplanPeriodInMillis() > planningHorizon ) {
            System.err.println("WARNING:: Planning horizon " + planningHorizon * VGWorldConstants.SECONDS_PER_MILLISECOND +
                    " secs is less than replan time.");
        }

        if ( subordinateCombatOrganizations.size() > 0 ) {
            // DEBUG
            log.shout( getAgentIdentifier() + " PLANNING AT TIME " + ( startTime - baseTime ) /1000 + " secs." ) ;

            // measureProcessReplanTimer() ;
            planAndDistribute();
        }

        replanTimeMP.addMeasurement( new DelayMeasurement( "ProcessReplan", null, getAgentIdentifier(), startTime, System.currentTimeMillis() ));

        // Schedule replanning alarm.
        targetWakeupTime = startTime + getReplanPeriodInMillis();
        if ( started ) {
            getAlarmService().addRealTimeAlarm( new ReplanAlarm( targetWakeupTime  ) ) ;
        }

        // Execute for good measure.
        // TODO Check to see if an execute is really called for here.
        // execute() ;
        if ( getBlackboardService().isTransactionOpen() && !wasOpen ) {
            getBlackboardService().closeTransaction();
        }
    }

    /**
     * Whether everyone has checked in or not.
     * @return
     */
    public boolean isPerceivedWorldStateComplete() {
        for (Iterator iterator = subordinateCombatOrganizations.values().iterator(); iterator.hasNext();) {
            Organization organization = (Organization) iterator.next();
            EntityInfo info = perceivedWorldState.getEntityInfo( organization.getMessageAddress().getAddress() ) ;
            if ( info == null ) {
                return false ;
            }
        }
        return true ;
    }

    protected void processMessageFromSuperior( MessageEvent message ) {
        if ( message instanceof ZoneScheduleMessage ) {
            ZoneScheduleMessage zoneScheduleMessage = (ZoneScheduleMessage) message ;
            mergeZoneSchedule( zoneScheduleMessage.getSchedule() ) ;
        }
    }

    private void mergeZoneSchedule( Plan p ) {

        if ( zoneSchedule == null ) {
            log.shout( getAgentIdentifier() + " Initializing " + getAgentIdentifier() + " with schedule " + p );
            zoneSchedule = p ;
        }
        else {
            // Just use the current time.
            long currentTime = perceivedWorldState.getTime() ;
            int firstNewTaskIndex = -1 ;
            int firstOldTaskIndex = -1;
            int lastOldTaskIndex = -1 ;
            Task firstNewTask = null ;

            ArrayList newTasks = new ArrayList();
            for (int i=0;i<p.getNumTasks();i++) {
                ZoneTask t = (ZoneTask) p.getTask(i) ;
                if ( t.getStartTime() >= currentTime ) {
                    firstNewTaskIndex = i ;
                    firstNewTask = t ;
                }
            }

            if ( firstNewTaskIndex == -1 ) {
                // Whoa, we don't know what our task is anyways. Just keep the old schedule if it exists.
                return ;
            }

            for (int i=0;i<zoneSchedule.getNumTasks();i++) {
                ZoneTask t = (ZoneTask) zoneSchedule.getTask(i) ;
                if ( t.getEndTime() > currentTime ) {
                    if ( firstOldTaskIndex == -1 ) {
                        firstOldTaskIndex = i ;
                    }

                    lastOldTaskIndex = i ;
                    if ( t.getEndTime() >= firstNewTaskIndex ) {
                        break ;
                    }
                }
            }

            // Now, copy all the old indices.
            if (firstOldTaskIndex != -1 && lastOldTaskIndex != -1)
            {
                for (int i = firstOldTaskIndex; i <= lastOldTaskIndex; i++)
                {
                    Task t = zoneSchedule.getTask(i) ;

                    // Fix up the last old index and make it compliant with the first new task so that
                    // there are no gaps.
                    if ( i == lastOldTaskIndex && firstNewTask != null ) {
                       t.setEndTime( firstNewTask.getStartTime() );
                    }
                    newTasks.add( t ) ;
                }
            }

            if ( firstNewTaskIndex != -1 ) {
                for (int i=firstNewTaskIndex;i<p.getNumTasks();i++) {
                    newTasks.add( p.getTask(i) ) ;
                }
            }

            zoneSchedule = new Plan( newTasks ) ;
        }
    }

    protected void processMessagesFromSubordinates( MessageEvent message ) {
        // System.out.println( "\n" + getAgentIdentifier() + ":: PROCESSING MESSAGE from subordinate " + message );
        if ( message instanceof UnitStatusUpdateMessage ) {
            UnitStatusUpdateMessage wsum = (UnitStatusUpdateMessage) message ;

            // QoS measurement.
            measureUpdateUnitStatusDelays(wsum);
            //

            org.cougaar.cpe.model.UnitEntity entity = wsum.getEntity() ;
            org.cougaar.cpe.model.EntityInfo info = perceivedWorldState.getEntityInfo( entity.getId() ) ;
            org.cougaar.cpe.model.UnitEntity pue ;

            // Fill in the perceived message state.
            if ( info == null ) {
                perceivedWorldState.addEntity( ( UnitEntity ) entity.clone(), new BinaryEngageByFireModel(0) ) ;
                info = perceivedWorldState.getEntityInfo( entity.getId() ) ;
            }
            else {
                pue = (org.cougaar.cpe.model.UnitEntity) info.getEntity() ;
                pue.setX( entity.getX() ) ; pue.setY( entity.getY() ) ;
                pue.setAmmoQuantity( entity.getAmmoQuantity() ) ;
                pue.setFuelQuantity( entity.getFuelQuantity() ) ;
            }

            // Now, merge the sensor values.
            WorldStateUtils.mergeSensorValues( perceivedWorldState, wsum.getWorldState() );

            long startTime = System.currentTimeMillis() ;

            if ( updateStatusNIU > 0 ) {
                CPUConsumer.consumeCPUMemoryIntensive( updateStatusNIU );
            }

            long endTime = System.currentTimeMillis() ;

            updateUnitStatusDelayMP.addMeasurement( new DelayMeasurement( null, null, null, startTime, endTime ));
            // Now, measure the time to process a message from a subordinate.

            // Consider doing an initial replan.
            if ( manueverPlan == null && isPerceivedWorldStateComplete() ) {
                System.out.println("\n" + getAgentIdentifier() + ":: CREATING INITIAL PLAN...");
                planAndDistribute();
            }

        }
    }

    /**
     * For each subordinate unit sending a UnitStatusUpdateMessage, record
     * the delay between sending and processing.
     *
     * @param wsum The message from the subordinate unit to be processed.
     */
    private void measureUpdateUnitStatusDelays(UnitStatusUpdateMessage wsum) {
        MeasurementChain m = wsum.getMeasurements() ;

        // Make a delay measurement by extracting the first element of the chain and marking it down.
        if ( m.getNumMeasurements() > 0 ) {
            TimestampMeasurement t = ( TimestampMeasurement ) m.getMeasurement(0) ;
            if ( t.getAction() == null || !t.getAction().equals( QoSConstants.TIMER_ACTION ) ) {
                System.err.println("Warning:: Unexpected action " + t.getAction() + " in UnitStatusUpdateMessage measurement chain " + m +
                        ", expected " + QoSConstants.TIMER_ACTION );
            }

            // Also, extract the send time for reference.
            TimestampMeasurement sendTime = null ;
            if ( m.getNumMeasurements() > 1 ) {
                sendTime =  ( TimestampMeasurement ) m.getMeasurement(1) ;
                if ( sendTime.getAction() == null || !sendTime.getAction().equals( QoSConstants.SEND_ACTION ) ) {
                    System.err.println("Warning:: Unexpected action " + t.getAction() + " in UnitStatusUpdateMessage measurement chain " + m +
                            ", expected " + QoSConstants.SEND_ACTION );
                }
            }

            Organization org = ( Organization ) subordinateCombatOrganizations.get( t.getSource().getAddress() ) ;
            if ( org == null ) {
                System.err.println("No subordinate " + t.getSource() + " found" );
                System.err.println("Subordinates=" + subordinateCombatOrganizations );
            }
            DelayMeasurementPoint tp = ( DelayMeasurementPoint )
                    updateStatusDelayMeasPoints.get(
                            t.getSource().getAddress() ) ;
            if ( tp == null ) {
                System.err.println("No measurement point found for " + wsum.getSource() );
            }
            else {
                long currentTime = System.currentTimeMillis() ;
                if ( currentTime - t.getTimestamp() < 0 ) {
                    System.err.println("Warning: Negative delay in measurement, setting to zero." );
                    currentTime = t.getTimestamp() ;
                }
                tp.addMeasurement(
                        new DelayMeasurement(
                        t.getAction(), t.getEvent(), t.getSource(),
                        t.getTimestamp(), currentTime ));
            }
        }
    }

    protected void setupSubscriptions() {
        log = (LoggingService) getServiceBroker().getService( this, LoggingService.class, null ) ;

        System.out.println("\nSTARTING agent " + getAgentIdentifier() );
        gmrt = new GenericRelayMessageTransport( this, getServiceBroker(), getAgentIdentifier(),
                this, getBlackboardService() ) ;

        // Find the WorldStateAgent
        UIDService service = (UIDService) getServiceBroker().getService( this, UIDService.class, null ) ;
        relayToWorldAgent = new SourceBufferRelay(service.nextUID(),
                MessageAddress.getMessageAddress( "WorldState"), getAgentIdentifier() ) ;
        getBlackboardService().publishAdd( relayToWorldAgent );

        /**
         * A predicate that matches all organizations that
         */
        class FindOrgsPredicate implements UnaryPredicate {
            public boolean execute(Object o) {
                return o instanceof Organization ;
            }
        }
        findOrgsSubscription = (IncrementalSubscription) getBlackboardService().subscribe(new FindOrgsPredicate());

        class MyMessageRelays implements UnaryPredicate {
            public boolean execute(Object o) {
                return false;
            }
        }

        targetRelaySubscription = ( IncrementalSubscription ) getBlackboardService().subscribe( new UnaryPredicate() {
            public boolean execute(Object o) {
                if ( o instanceof TargetBufferRelay ) {
                    return true ;
                }
                return false ;
            }
        }) ;

        makeOperatingModeConditions();

        makeMeasurementPoints();

        // Now, load all the configuration information.
        getConfigInfo();

        System.out.println("\n-------------------------------------------------------");
        System.out.println("Agent \"" + getAgentIdentifier() + "\" INITIAL CONFIGURATION" );
        System.out.println(" ");
        System.out.println("PlanningDepth=" + planningDepthCondition.getValue() );
        System.out.println("PlanningBreadth=" + planningBreadthCondition.getValue() );
        System.out.println("ReplanPeriod(sec)=" + getReplanPeriodInMillis() / 1000.0 );
        System.out.println("Planning Delay(sec)=" + getPlanningDelay() / 1000.0 );
        System.out.println("UpdateStatusNIU=" + updateStatusNIU );
        System.out.println("-------------------------------------------------------\n");
    }

    private void makeOperatingModeConditions()
    {
        // Expose the adaptive OperatingModeConditions

        planningDepthCondition = new OperatingModeCondition( "PlanningDepth", planningDepthList ) ;
        planningDepthCondition.setValue( new Integer(5) ) ;

        planningBreadthCondition = new OperatingModeCondition( "PlanningBreadth", planningBreadthList ) ;
        planningBreadthCondition.setValue( new Integer(50) );

        // The ReplanPeriod condition values are given in task deltas and
        // must be < the depth.
        replanPeriodCondition = new OperatingModeCondition( "ReplanPeriod", replanPeriodList ) ;
        replanPeriodCondition.setValue( new Integer(60000) );

        planningDelayCondition = new OperatingModeCondition( "PlanStartDelay", planningDelayList ) ;
        planningDelayCondition.setValue( planningDelayList.getAllowedValues()[0].getMin() );

        getBlackboardService().publishAdd( planningDepthCondition ) ;
        getBlackboardService().publishAdd( planningBreadthCondition ) ;
        getBlackboardService().publishAdd( replanPeriodCondition );

        perceivedWorldStateRef = new WorldStateReference( "PerceivedWorldState", null) ;
        getBlackboardService().publishAdd( perceivedWorldStateRef );

        updateStatePeriodCondition = new OperatingModeCondition( "UpdateStatePeriod", updateStateConditionList  ) ;
        updateStatePeriodCondition.setValue( updateStateConditionList.getAllowedValues()[0].getMin() );
        getBlackboardService().publishAdd( updateStatePeriodCondition );

        // Look for changed operating modes.
        operatingModeSubscription = (IncrementalSubscription) getBlackboardService().subscribe( new UnaryPredicate() {
            public boolean execute(Object o) {
                return ( o instanceof OperatingModeCondition ) ;
            }
        }) ;
    }

    private void makeMeasurementPoints()
    {
        // Make some measurement points.
        replanTimerDelayMP = new EventDurationMeasurementPoint( "ReplanTimerDelay") ;
        getBlackboardService().publishAdd( replanTimerDelayMP );
        replanTimeMP = new EventDurationMeasurementPoint( "ReplanTime" ) ;
        getBlackboardService().publishAdd( replanTimeMP );
        updateUnitStatusDelayMP = new EventDurationMeasurementPoint( "UpdateUnitStateDelay" ) ;
        getBlackboardService().publishAdd( updateUnitStatusDelayMP );
    }

    private String getNodeValueForTag(Document doc, String tagName, String namedItem ) {
        NodeList nodes = doc.getElementsByTagName( tagName );

        String value = null ;
        // Get target plan log
        for (int i=0;i<nodes.getLength();i++) {
            Node n = nodes.item(i) ;
            value = n.getAttributes().getNamedItem( namedItem ).getNodeValue() ;
        }
        return value;
    }

    protected void getConfigInfo() {
        Collection params = getParameters() ;
        Vector paramVector = new Vector( params ) ;

        String fileName = null ;
        if ( paramVector.size() > 0 ) {
            fileName = ( String ) paramVector.elementAt(0) ;
        }

        try {
            ConfigFinder finder = getConfigFinder() ;

            if ( fileName != null && finder != null ) {
                File f = finder.locateFile( fileName ) ;

                try {
                FileInputStream fis = new FileInputStream( f ) ;
                configBytes = new byte[ fis.available() ] ;
                fis.read( configBytes ) ;
                fis.close();
                }
                catch ( Exception e ) {
                    e.printStackTrace();
                }

                // DEBUG -- Replace by call to log4j
                log.info( "C2AgentPlugin:: Configuring from " + f ) ;

                if ( f != null && f.exists() ) {
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
                            if( root.getNodeName().equals( "CPConfig" ) ) {

                                // Get the default update time, measured in ms.
                                String replanTimerPeriodString = getNodeValueForTag(doc, "BNReplanTimerPeriod", "value" ) ;
                                if ( replanTimerPeriodString != null ) {
                                    int newReplanUpdatePeriod = Integer.parseInt( replanTimerPeriodString ) ;
                                    if ( !replanPeriodCondition.getAllowedValues().isAllowed( new Integer( newReplanUpdatePeriod ) ) ) {
                                        System.err.println("Warning: Replan Update Period " + newReplanUpdatePeriod + " is out of range " + replanPeriodList );
                                    }
                                    else {
                                        replanPeriodCondition.setValue( new Integer( newReplanUpdatePeriod ) ) ;
                                    }
                                }

                                Integer period = ConfigParserUtils.parseIntegerValue( doc, "BNUpdateStatePeriod", "value" ) ;
                                if ( period != null ) {
                                    updateStatePeriodCondition.setValue( period );
                                }

                                // Find the default search depth.
                                String searchDepthString = getNodeValueForTag( doc, "BNPlanningDepth", "value" ) ;
                                if ( searchDepthString != null ) {
                                    int newSearchDepth = Integer.parseInt( searchDepthString ) ;
                                    if ( !planningDepthList.isAllowed( new Integer( newSearchDepth )) ) {
                                        System.err.println("Warning: Default search depth " + newSearchDepth + " is out of range " + planningDepthList );
                                    }
                                    else {
                                        planningDepthCondition.setValue( new Integer( newSearchDepth ) ) ;
                                    }
                                }

                                // Find the default search breadth.
                                String searchBreadthString = getNodeValueForTag( doc, "BNPlanningBreadth", "value" ) ;
                                if ( searchBreadthString != null ) {
                                    int newSearchBreadth = Integer.parseInt( searchBreadthString ) ;
                                    if ( !planningBreadthList.isAllowed( new Integer( newSearchBreadth ) ) ) {
                                        System.err.println("Warning: Default search breadth " + newSearchBreadth + " is out of range." );
                                    }
                                    else {
                                        planningBreadthCondition.setValue( new Integer( newSearchBreadth ) );
                                    }
                                }

                                //
                                String planningDelayString = getNodeValueForTag( doc, "BNPlanningDelay", "value" ) ;
                                if ( planningDelayString != null ) {

                                    int newPlanningDelay = Integer.parseInt(planningDelayString ) ;
                                    Integer planningDelay = new Integer( newPlanningDelay ) ;
                                    if ( planningDelayList.isAllowed( planningDelay ) ) {
                                       planningDelayCondition.setValue( planningDelay ) ;
                                    }
                                    else {
                                        System.err.println("Warning: Planning Delay " + planningDelay + " is not allowed in " + planningDelayList );
                                    }
                                }

                                String processUpdateStatusNIUString = getNodeValueForTag( doc, "BNUpdateStatusNIU", "value" ) ;
                                if ( processUpdateStatusNIUString != null ) {
                                     updateStatusNIU = Integer.parseInt( processUpdateStatusNIUString ) ;
                                }
                            }
                            else {
                                // DEBUG -- replace with log4j
                                if ( log.isWarnEnabled() ) {
                                    log.warn( "Warning::CP Config file " + f + " is invalid, no root node \"CPConfig\"" );
                                }
                                System.out.println( "Warning:: CP agent Config file " + f + " is invalid, no root node \"CPConfig\"" ) ;
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


    public class ReplanAlarm extends StandardAlarm {
        public ReplanAlarm(long expirationTime) {
            super(expirationTime);
        }

        protected void processExpire() {
            processReplanTimer();
        }
    }

    /**
     * A list of TargetBufferRelay objects from superiors.
     */
    private TargetBufferRelay relayFromSuperior ;
    private SourceBufferRelay relayToWorldAgent ;

    Object[][] manueverPlan ;

    private Organization superior ;
    private HashMap subordinateCombatOrganizations = new HashMap() ;

    private IncrementalSubscription findOrgsSubscription;

    private IncrementalSubscription targetRelaySubscription;
    private HashMap subordinateRelays = new HashMap();

    private GenericRelayMessageTransport gmrt;
    private long baseTime;
    private ManueverPlanner mp;

    private VectorMeasurementPoint worldStateFreshnessMeasurementPoint;

    private OperatingModeCondition planningDepthCondition, planningBreadthCondition,
         replanPeriodCondition, updateStatePeriodCondition ;

    /**
     * The delay before the start of the new plan.
     */
    private OperatingModeCondition planningDelayCondition ;

    /**
     * Measurement points for StatusUpdates from subordinates.
     */
    private HashMap updateStatusDelayMeasPoints = new HashMap() ;

    /**
     * Number of delta units per task. Currently, this is fixed with value 4.
     */
    private int numDeltasPerTask = 4 ;

    private int numIncrementsPerPlan = 2 ;

    private boolean lockBlackboardWhilePlanning = true;

    private org.cougaar.cpe.model.WorldStateModel perceivedWorldState ;
    private WorldStateReference perceivedWorldStateRef ;

    public MessageAddress getAgentIdentifier() {
        return super.getAgentIdentifier();
    }
}
