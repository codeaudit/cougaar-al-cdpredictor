package org.cougaar.cpe.agents.plugin;

import org.cougaar.core.adaptivity.OMCRangeList;
import org.cougaar.core.adaptivity.OperatingModeCondition;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.plugin.ComponentPlugin;
import org.cougaar.core.service.LoggingService;
import org.cougaar.core.service.UIDService;
import org.cougaar.cpe.agents.Constants;
import org.cougaar.cpe.agents.messages.*;
import org.cougaar.cpe.model.*;
import org.cougaar.cpe.model.events.CPEEvent;
import org.cougaar.cpe.planning.zplan.*;
import org.cougaar.cpe.relay.GenericRelayMessageTransport;
import org.cougaar.cpe.relay.MessageSink;
import org.cougaar.cpe.relay.SourceBufferRelay;
import org.cougaar.cpe.relay.TimerMessage;
import org.cougaar.cpe.util.CPUConsumer;
import org.cougaar.cpe.util.ConfigParserUtils;
import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.planning.ldm.plan.Role;
import org.cougaar.tools.techspecs.events.MessageEvent;
import org.cougaar.tools.techspecs.events.TimerEvent;
import org.cougaar.util.ConfigFinder;
import org.cougaar.util.UnaryPredicate;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.cougaar.tools.techspecs.qos.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

public class BDEAgentPlugin extends ComponentPlugin implements MessageSink {
	private SourceBufferRelay worldStateRelay;
	private IncrementalSubscription findOrgsSubscription;
	private HashMap subordinateCombatOrganizations = new HashMap();

	private OMCRangeList replanPeriodList =
		new OMCRangeList(new int[] { 2, 3, 4 });

	/**
	 * The number of zone phases per replan.
	 */
	private OperatingModeCondition replanPeriodCondition;

	private OMCRangeList planningDepthList =
		new OMCRangeList(new int[] { 4, 5, 6, 7, 8, 9, 10, 11 });

	/**
	 * The depth of the replan in phases.
	 */
	private OperatingModeCondition planningDepthCondition;

	private OperatingModeCondition planningBreadthCondition;

	private OMCRangeList planningBreadthList =
		new OMCRangeList(new int[] { 30, 50, 70, 90, 100, 120 });

	private OperatingModeCondition planningDelayCondition;

	private OMCRangeList planningDelayList =
		new OMCRangeList(new int[] { 1, 2, 3 });
	/**
	 * The number of deltaT increments per planning phase. This is a multiple of the number of delta per task element.
	 */
	protected int deltaTPerPlanningPhase = 12;

	protected ZonePlanner zonePlanner;

	/**
	 * Configuration data saved at startup.
	 */
	private byte[] configBytes;
	private long updateStatusNIU = 0;
	private long baseTime;

	/**
	 * Measurement Points for the 2 classes of traffic dealt with in BDE
	 */
	private DelayMeasurementPoint BDEUpdateProcessMP;
	private DelayMeasurementPoint zonePlanMP;
    private HashMap bnAggUnitToMetricsMap = new HashMap();
    private IncrementalSubscription mpSubscrption;

    protected void setupSubscriptions() {
        mpSubscrption = (IncrementalSubscription) getBlackboardService().subscribe(
				new UnaryPredicate() {
                    public boolean execute(Object o)
                    {
                        return o instanceof MeasurementPoint ;
                    }
        } ) ;

		logger =
			(LoggingService) getServiceBroker().getService(
				this,
				LoggingService.class,
				null);

		logger.shout("BDEAgentPlugin:: STARTING agent " + getAgentIdentifier());
		gmrt =
			new GenericRelayMessageTransport(
				this,
				getServiceBroker(),
				getAgentIdentifier(),
				this,
				getBlackboardService());

		// Always sign in to the world state.
		worldStateRelay =
			gmrt.addRelay(
				MessageAddress.getMessageAddress(Constants.WORLD_STATE_AGENT));
		// Find the WorldStateAgent
		UIDService service =
			(UIDService) getServiceBroker().getService(
				this,
				UIDService.class,
				null);

		class FindOrgsPredicate implements UnaryPredicate {
			public boolean execute(Object o) {
				return o instanceof Organization;
			}
		}
		findOrgsSubscription =
			(IncrementalSubscription) getBlackboardService().subscribe(
				new FindOrgsPredicate());

		publishOperatingModes();

		makeMeasurementPoints();

		getConfigInfo();
	}

	private void publishOperatingModes() {

		replanPeriodCondition =
			new OperatingModeCondition("ReplanPeriod", replanPeriodList);
		getBlackboardService().publishAdd(replanPeriodCondition);

		planningDepthCondition =
			new OperatingModeCondition("PlanningDepth", planningDepthList);
		getBlackboardService().publishAdd(planningDepthCondition);

		planningBreadthCondition =
			new OperatingModeCondition("PlanningBreadth", planningBreadthList);
		getBlackboardService().publishAdd(planningBreadthCondition);

		planningDelayCondition =
			new OperatingModeCondition("PlanningDelay", planningDelayList);
		getBlackboardService().publishAdd(planningDelayCondition);
	}

	protected void processOrganizations() {
		//  Find subordinates signing in.
		Collection addedCollection = findOrgsSubscription.getAddedCollection();
		ArrayList self = new ArrayList(),
			newSubordinates = new ArrayList(),
			newSuperiors = new ArrayList();

		OrganizationHelper.processSuperiorAndSubordinateOrganizations(
			getAgentIdentifier(),
			addedCollection,
			newSuperiors,
			newSubordinates,
			self);

		// Make relays to all new subordinates organizations.

		boolean subordinatesAdded = false;
		for (int i = 0; i < newSubordinates.size(); i++) {
			Organization subOrg = (Organization) newSubordinates.get(i);
			if (gmrt.getRelay(subOrg.getMessageAddress()) == null) {
				gmrt.addRelay(subOrg.getMessageAddress());
				logger.shout(
					getAgentIdentifier()
						+ " adding relay to subordinate "
						+ subOrg.getMessageAddress());
			}

			if (subOrg.getOrganizationPG().inRoles(Role.getRole("Combat"))) {
				logger.info(
					getAgentIdentifier()
						+ " ADDING subordinate combat organization "
						+ subOrg);
				subordinateCombatOrganizations.put(
					subOrg.getMessageAddress().getAddress(),
					subOrg);
				subordinatesAdded = true;
			}
		}

	}

	public void getConfigInfo() {
		Collection params = getParameters();
		Vector paramVector = new Vector(params);
		LoggingService log =
			(LoggingService) getServiceBroker().getService(
				this,
				LoggingService.class,
				null);

		String fileName = null;
		if (paramVector.size() > 0) {
			fileName = (String) paramVector.elementAt(0);
		}

		try {
			ConfigFinder finder = getConfigFinder();
			if (fileName != null && finder != null) {
				File f = finder.locateFile(fileName);

				// Save the configruation data
				try {
					FileInputStream fis = new FileInputStream(f);
					configBytes = new byte[fis.available()];
					fis.read(configBytes);
					fis.close();
				} catch (Exception e) {
					e.printStackTrace();
				}

				// DEBUG -- Replace by call to log4j
				log.info("BDEAgentPlugin configuring from " + f);

				if (f != null && f.exists()) {
					Document doc = null;
					try {
						doc = finder.parseXMLConfigFile(fileName);
					} catch (Exception e) {
						System.out.println(e);
					}

					if (doc != null) {
						try {
							Node root = doc.getDocumentElement();
							if (root.getNodeName().equals("CPConfig")) {
								Integer val =
									ConfigParserUtils.parseIntegerValue(
										doc,
										"BDEReplanPeriod",
										"value");
								if (val != null) {
									replanPeriodCondition.setValue(val);
								}

								val =
									ConfigParserUtils.parseIntegerValue(
										doc,
										"BDEPlanningDepth",
										"value");
								if (val != null) {
									planningDepthCondition.setValue(val);
								}

								val =
									ConfigParserUtils.parseIntegerValue(
										doc,
										"BDEPlanningBreadth",
										"value");
								if (val != null) {
									planningBreadthCondition.setValue(val);
								}
								val =
									ConfigParserUtils.parseIntegerValue(
										doc,
										"BDEPlanningDelay",
										"value");
								if (val != null) {
									planningDelayCondition.setValue(val);
								}

								val =
									ConfigParserUtils.parseIntegerValue(
										doc,
										"BDEUpdateStatusNIU",
										"value");
								if (val != null) {
									updateStatusNIU = val.intValue();
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void processMessage(Object o) {
		if (o instanceof MessageEvent) {
			MessageEvent event = (MessageEvent) o;
			if (event
				.getSource()
				.getAddress()
				.equals(Constants.WORLD_STATE_AGENT)) {
				processMessageFromWorldState(event);
			} else {
				Organization org =
					(Organization) subordinateCombatOrganizations.get(
						event.getSource().getAddress());
				if (org != null) {
					//NG: NEED RT (update message)
					processMessageFromSubordinate(event);
					//System.out.println("PMFS called");
				}
			}
		}
	}

	protected void processMessageFromSubordinate(MessageEvent message) {
		if (message instanceof BNStatusUpdateMessage) {
			//			TODO NG: NEED PBT (update message)
			long startTime = System.currentTimeMillis();
			boolean wasOpen = true;
			if (!getBlackboardService().isTransactionOpen()) {
				wasOpen = false;
				getBlackboardService().openTransaction();
			}
			BNStatusUpdateMessage wsum = (BNStatusUpdateMessage) message;

			WorldStateModel statusReport = wsum.getWorldStateModel();
			// QoS measurement.
			//measureUpdateUnitStatusDelays(wsum);
			//
			BNAggregate agg =
				(BNAggregate) referenceZoneWorld.getAggUnitEntity(
					wsum.getBnUnitId());
			for (int i = 0; i < agg.getNumSubEntities(); i++) {
				UnitEntity newEntity =
					(UnitEntity) statusReport.getEntity(
						agg.getSubEntityName(i));
				if (newEntity == null) {
					continue;
				} else {
					org.cougaar.cpe.model.EntityInfo info =
						referenceZoneWorld.getEntityInfo(newEntity.getId());
					org.cougaar.cpe.model.UnitEntity pue;

					// Fill in the perceived message state.
					if (info == null) {
						referenceZoneWorld.addEntity(
							(UnitEntity) newEntity.clone(),
							new BinaryEngageByFireModel(0));
						info =
							referenceZoneWorld.getEntityInfo(newEntity.getId());
					} else {
						pue =
							(org.cougaar.cpe.model.UnitEntity) info.getEntity();
						pue.setX(newEntity.getX());
						pue.setY(newEntity.getY());
						pue.setAmmoQuantity(newEntity.getAmmoQuantity());
						pue.setFuelQuantity(newEntity.getFuelQuantity());
					}
				}
			}

			// TODO merge the target locations based on fidelity/error.
            mergeSensorValues( statusReport ) ;

			if (updateStatusNIU > 0) {
				CPUConsumer.consumeCPUMemoryIntensive(updateStatusNIU);
			}

			//			TODO NG: NEED ET (update message)
			long endTime = System.currentTimeMillis();
			// updateUnitStatusDelayMP.add(new Delay(null, null, null, startTime, endTime));
			BDEUpdateProcessMP.addMeasurement(
				new DelayMeasurement(
					"ProcessUpdateBDE",
					"ProcessUpdateBDE",
					getAgentIdentifier(),
					startTime,
					endTime));

			if (getBlackboardService().isTransactionOpen() && !wasOpen) {
				getBlackboardService().closeTransaction();
			}

			// Add a measurement point here.
			// updateUnitStatusDelayMP.addMeasurement(new DelayMeasurement(null, null, null, startTime, endTime));
		}
	}

	/**
	 * Merge two sensor perceived world states together.
	 *
	 * @param statusReport
	 */
	private void mergeSensorValues(WorldStateModel statusReport) {
		WorldStateUtils.mergeSensorValues(referenceZoneWorld, statusReport);
		//        Iterator newTargets = statusReport.getTargets() ;
		//        while (newTargets.hasNext())
		//        {
		//            TargetEntity newSensedTarget = (TargetEntity) newTargets.next();
		//            TargetContact currentContact = (TargetContact) referenceZoneWorld.getEntity( newSensedTarget.getId() );
		//            if ( currentContact != null ) {
		//                if ( newSensedTarget instanceof TargetContact ) {
		//                    TargetContact newContact = (TargetContact) newSensedTarget ;
		//
		//                    if ( newContact.getXError() < currentContact.getXError() && newContact.getTimeStamp() >= ( currentContact.getTimeStamp() - 10000) )
		//                    {
		//                        currentContact.setPosition( newContact.getX(), newContact.getY() );
		//                        currentContact.setError( newContact.getXError(), newContact.getYError() );
		//                    }
		//                }
		//                else {
		//                    currentContact.setPosition( newSensedTarget.getX(), newSensedTarget.getY() );
		//                    currentContact.setError( 0, 0 );
		//                }
		//            }
		//            else {
		//                currentContact = (TargetContact) newSensedTarget.clone() ;
		//                referenceZoneWorld.addEntity( currentContact );
		//            }
		//        }
	}

	public int getPlanningDelay() {
		if (planningDelayCondition != null) {
			Integer intValue = (Integer) planningDelayCondition.getValue();
			return intValue.intValue();
		}
		throw new RuntimeException("Planning delay operating mode is not initialized.");

	}

	public int getReplanPeriod() {
		if (replanPeriodCondition != null) {
			Integer intValue = (Integer) replanPeriodCondition.getValue();
			return intValue.intValue();
		}
		throw new RuntimeException("ReplanPeriod operating mode is not initialized.");
	}

	public int getPlanningDepth() {
		if (planningDepthCondition != null) {
			Integer intValue = (Integer) planningDepthCondition.getValue();
			return intValue.intValue();
		}
		throw new RuntimeException(
			planningDepthCondition.getName()
				+ " operating mode is not initialized.");
	}

	protected void processStartMessage(StartMessage msg) {
		this.baseTime = msg.getBaseTime();
		long replanTime =
			getReplanPeriod()
				* deltaTPerPlanningPhase
				* referenceZoneWorld.getDeltaTInMS();
		logger.shout(
			"Starting time for "
				+ getAgentIdentifier()
				+ " with replan period "
				+ replanTime / 1000
				+ " secs.");

		ProcessReplanTimer(null);

		gmrt.setAlarm("ProcessReplanTimer", "ReplanTimer", replanTime, true);

		// Send initial zone assigments to all subordinates.
	}

	protected void doConfigure(ConfigureMessage cm) {
		logger.shout(
			" CONFIGURING "
				+ getAgentIdentifier()
				+ " with subordinates "
				+ subordinateCombatOrganizations);
		referenceZoneWorld = (ZoneWorld) cm.getWorldStateModel();

		worldStateRef =
			new WorldStateReference("ZoneWorld", referenceZoneWorld);
		getBlackboardService().publishAdd(worldStateRef);

		ArrayList aggEntities = new ArrayList();

		for (int i = 0; i < referenceZoneWorld.getNumAggUnitEntities(); i++) {
			BNAggregate agg =
				(BNAggregate) referenceZoneWorld.getAggUnitEntity(i);

			// System.out.println("Initialize aggregrate with " + agg.getCurrentZone() + " zone.");
			aggEntities.add(agg.getId());
			IndexedZone currentZone = (IndexedZone) agg.getCurrentZone();
			ZoneTask t =
				new ZoneTask(
					referenceZoneWorld.getTime(),
					deltaTPerPlanningPhase * referenceZoneWorld.getDeltaTInMS(),
					currentZone,
					currentZone);
			Plan p = new Plan(t);
			agg.setZonePlan(p);

            MeasuredWorldMetrics mwm;
            referenceZoneWorld.addEventListener( mwm = new MeasuredWorldMetrics( agg.getId(),
                    referenceZoneWorld, 40000 ) );
            bnAggUnitToMetricsMap.put( agg.getId(), mwm ) ;
            getBlackboardService().publishAdd( mwm.getAttrition() ) ;
            getBlackboardService().publishAdd( mwm.getEntryRate() ) ;
            getBlackboardService().publishAdd( mwm.getKills() ) ;
            getBlackboardService().publishAdd( mwm.getPenalties() ) ;

			// Make configuration message and send it to the subordinate.
			if (subordinateCombatOrganizations.get(agg.getId()) == null) {
				logger.warn(
					"Subordinate unit with id="
						+ agg.getId()
						+ " not found as subordinate.");
			} else {
				// Send a configuration message but translate the current zone information.
				ZoneWorld zw =
					new ZoneWorld(
						referenceZoneWorld,
						referenceZoneWorld.getZoneGridSize());
				zw.addAggUnitEntity(agg = (BNAggregate) agg.clone());
				t =
					new ZoneTask(
						referenceZoneWorld.getTime(),
						deltaTPerPlanningPhase
							* referenceZoneWorld.getDeltaTInMS(),
						referenceZoneWorld.getIntervalForZone(currentZone),
						referenceZoneWorld.getIntervalForZone(currentZone));
				p = new Plan(t);
				agg.setZonePlan(p);
				agg.setCurrentZone(
					referenceZoneWorld.getIntervalForZone(currentZone));
                mwm.setZoneSchedule( p );

				gmrt.sendMessage(
					MessageAddress.getMessageAddress(agg.getId()),
					new ConfigureMessage(zw));
			}
		}

		zonePlanner =
			new ZonePlanner(
				aggEntities,
				referenceZoneWorld,
				deltaTPerPlanningPhase);
		zonePlanner.setMaxBranchFactor(getPlanningBreadth());
		zonePlanner.setMaxDepth(getPlanningDepth());

		System.out.println(
			"\n-------------------------------------------------------");
		System.out.println(
			"Agent \"" + getAgentIdentifier() + "\" INITIAL CONFIGURATION");
		System.out.println();
		System.out.println(
			"Deltas Per Phase="
				+ deltaTPerPlanningPhase
				+ ",time="
				+ referenceZoneWorld.getDeltaT() * deltaTPerPlanningPhase
				+ " .sec");
		System.out.println(
			"PlanningDepth="
				+ getPlanningDepth()
				+ " phases, horizon="
				+ referenceZoneWorld.getDeltaT() * getPlanningDepth()
				+ " .sec");
		System.out.println(
			"PlanningBreadth=" + getPlanningBreadth() + " branches per ply.");
		System.out.println(
			"ReplanPeriod="
				+ getReplanPeriod()
				+ " planning phases ("
				+ referenceZoneWorld.getDeltaT()
					* deltaTPerPlanningPhase
					* getReplanPeriod()
				+ " sec.)");
		System.out.println(
			"Planning Delay="
				+ getPlanningDelay()
				+ " planning phases ("
				+ referenceZoneWorld.getDeltaT()
					* deltaTPerPlanningPhase
					* getPlanningDelay()
				+ " sec.)");
		//System.out.println("UpdateStatusNIU=" + updateStatusNIU );
		System.out.println(
			"-------------------------------------------------------\n");

		// Send a dummy zone schedule to all subordinates with the

	}

	public Zone getCurrentZone(Plan zoneSchedule, long time) {
		Zone zone = null;

		if (zoneSchedule != null) {
			for (int i = 0; i < zoneSchedule.getNumTasks(); i++) {
				ZoneTask t = (ZoneTask) zoneSchedule.getTask(i);
				if (t.getStartTime() <= time && t.getEndTime() >= time) {
					zone = t.getEndZone();
				}
			}
		}

		// Always return the last task if we are after the time, always return the first task
		// if we are before the time.
		if (zoneSchedule.getNumTasks() > 0) {
			if (time
				> zoneSchedule
					.getTask(zoneSchedule.getNumTasks() - 1)
					.getStartTime()) {
				zone =
					((ZoneTask) zoneSchedule
						.getTask(zoneSchedule.getNumTasks() - 1))
						.getStartZone();
			} else if (time < zoneSchedule.getTask(0).getStartTime()) {
				zone = ((ZoneTask) zoneSchedule.getTask(0)).getEndZone();
			}
		}

		return zone;
	}

	/**
	 * Does zone based planning and distributes the results to any subordinate organizations.
	 *
	 * @param event
	 */
	public void ProcessReplanTimer(TimerEvent event) {
		// Now, find out what the current plans are and what the scheduled zones ought to be based on the current plans.
		// Run the reference zone forward until matches the current base time.
		// Update the location of the aggregate units.

		//TODO NG: NEED RT (zone plan)
		long startTime = System.currentTimeMillis();
		boolean wasOpen = true;
		if (!getBlackboardService().isTransactionOpen()) {
			wasOpen = false;
			getBlackboardService().openTransaction();
		}

		ArrayList units = zonePlanner.getSubordinateUnits();
		for (int i = 0; i < units.size(); i++) {
			String id = (String) units.get(i);
			BNAggregate agg =
				(BNAggregate) referenceZoneWorld.getAggUnitEntity(id);
			Plan p = agg.getZonePlan();
			if (p != null) {
				Zone z = getCurrentZone(p, referenceZoneWorld.getTime());
				if (z != null) {
					agg.setCurrentZone(z);
				}
			}

		}

		//TODO NG: NEED PBT (zone plan)
		zonePlanner.plan(
			referenceZoneWorld,
			getPlanningDelay()
				* deltaTPerPlanningPhase
				* referenceZoneWorld.getDeltaTInMS());
		Object[][] plans = zonePlanner.getPlans(false);

		// Just update all the zone plans without merging?
		for (int i = 0; i < plans.length; i++) {
			Object[] plan = plans[i];
			String unitId = (String) plan[0];
			Plan zonePlan = (Plan) plan[1];
			BNAggregate agg =
				(BNAggregate) referenceZoneWorld.getAggUnitEntity(unitId);
			agg.setZonePlan(zonePlan);
		}

		plans = zonePlanner.getPlans(true);

		for (int i = 0; i < plans.length; i++) {
			Object[] plan = plans[i];
			String unitId = (String) plan[0];
			Plan zonePlan = (Plan) plan[1];

            // Update the measurements according to the zone schedule.
            MeasuredWorldMetrics mw = (MeasuredWorldMetrics) bnAggUnitToMetricsMap.get( unitId );
            if ( mw != null ) {
               mw.setZoneSchedule( zonePlan );
            }

			Organization org =
				(Organization) subordinateCombatOrganizations.get(unitId);
			if (org != null) {
				ZoneScheduleMessage zsm = new ZoneScheduleMessage(zonePlan);
				gmrt.sendMessage(org.getMessageAddress(), zsm);
			}
		}
		//TODO NG: NEED ET (zone plan)
		long endTime = System.currentTimeMillis();
		// updateUnitStatusDelayMP.add(new Delay(null, null, null, startTime, endTime));
		zonePlanMP.addMeasurement(
			new DelayMeasurement(
				"ZonePlan",
				"ZonePlan",
				getAgentIdentifier(),
				startTime,
				endTime));

		if (getBlackboardService().isTransactionOpen() && !wasOpen) {
			getBlackboardService().closeTransaction();
		}
	}

	protected int getPlanningBreadth() {
		if (planningBreadthCondition != null) {
			Integer intValue = (Integer) planningBreadthCondition.getValue();
			return intValue.intValue();
		}
		throw new RuntimeException("ReplanPeriod operating mode is not initialized.");
	}

	protected void processMessageFromWorldState(MessageEvent event) {
		if (event instanceof StartMessage) {
			processStartMessage((StartMessage) event);
		} else if (event instanceof ConfigureMessage) {
			doConfigure((ConfigureMessage) event);
		} else if (event instanceof UnitStatusUpdateMessage) {
			processWorldStateUpdateMessage((UnitStatusUpdateMessage) event);
		} if ( event instanceof PublishMPMessage ) {
             BundledMPMessage bmm = new BundledMPMessage() ;
             // Just subscribe to all measurement points!
             for (Iterator iterator = mpSubscrption.iterator(); iterator.hasNext();)
             {
                 MeasurementPoint mp = (MeasurementPoint) iterator.next();
                 bmm.addData( mp ) ;
             }

             // Send the configuration information.
             Collection params = getParameters() ;
             Vector paramVector = new Vector( params ) ;
             String fileName = null ;
             if ( paramVector.size() > 0 && configBytes != null  ) {
                 fileName = ( String ) paramVector.elementAt(0) ;
                 bmm.addData( fileName, configBytes);
             }

             gmrt.sendMessage( event.getSource(), bmm );
         }
	}

	/**
	 * Process change to the world state as reported by the simulator.  This removes contacts from the list
	 * as well as adding them.
	 *
	 * @param event
	 */
	protected void processWorldStateUpdateMessage(UnitStatusUpdateMessage event) {
		WorldStateModel sensedWorldState = event.getWorldState();

		// DEBUG
		if (event
			.getSource()
			.getAddress()
			.equals(Constants.WORLD_STATE_AGENT)) {
			// Update the official time associated with the zone world.
			referenceZoneWorld.setTime(sensedWorldState);
			logger.shout(
				"Agent "
					+ getAgentIdentifier()
					+ ":: Updating world state to time "
					+ referenceZoneWorld.getTimeInSeconds()
					+ ",measured elapsed time="
					+ (System.currentTimeMillis() - baseTime));

		}

		WorldStateUtils.mergeWorldStateWithReference(
			referenceZoneWorld,
			sensedWorldState);
		//        System.out.println("Modified zone world=" + referenceZoneWorld );
		referenceZoneWorld.setTime(sensedWorldState);

        processEvents( event.getEvents() ) ;

		getBlackboardService().publishChange(worldStateRef);

	}

    private void processEvents(ArrayList events)
    {
        for (int i = 0; i < events.size(); i++) {
            CPEEvent event = (CPEEvent)events.get(i);
            referenceZoneWorld.fireEvent( event );
        }
    }

	public void execute() {
		processOrganizations();

		gmrt.execute(getBlackboardService());
	}

	private void makeMeasurementPoints() {
		// Make some measurement points.

		BDEUpdateProcessMP =
			new EventDurationMeasurementPoint("ProcessUpdateBDE");
		getBlackboardService().publishAdd(BDEUpdateProcessMP);

		zonePlanMP = new EventDurationMeasurementPoint("ZonePlan");
		getBlackboardService().publishAdd(zonePlanMP);

	}

	LoggingService logger;
	WorldStateReference worldStateRef;
	ZoneWorld referenceZoneWorld;
	boolean isConfigured = false;
	GenericRelayMessageTransport gmrt;
}
