/*
  * <copyright>
  *  Copyright 2003 (Intelligent Automation, Inc.)
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
  */

package org.cougaar.tools.alf.predictor.plugin;

import org.cougaar.core.agent.service.alarm.PeriodicAlarm;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.plugin.ComponentPlugin;
import org.cougaar.core.service.*;

import org.cougaar.glm.ldm.Constants;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.plan.*;

import org.cougaar.util.UnaryPredicate;
import org.cougaar.logistics.plugin.inventory.TaskUtils;

import java.util.*;


public class PredictorDataPlugin extends ComponentPlugin {

	private String cluster;

	private LoggingService myLoggingService;
	private UIDService myUIDService;
	private BlackboardService myBlackBoardService;
	private AlarmService as;
	private IncrementalSubscription relationSubscription;
	private IncrementalSubscription taskSubscription;
	private TriggerFlushAlarm alarm = null;

	private PredictorHashMap phm = new PredictorHashMap();
	private HashMap matchMap = new HashMap();
	private int count_alarm = 0;
	private boolean rehydrate_flag = false;
	private int tick = 0;
	private boolean modelsPublished = false;

	private ArrayList taskList = new ArrayList();

	UnaryPredicate relationPredicate = new UnaryPredicate() {
		public boolean execute(Object o) {
			if (o instanceof HasRelationships) return ((HasRelationships) o).isLocal();
			else return false;
		}
	};

	UnaryPredicate ALPredicate = new UnaryPredicate() {
		public boolean execute(Object o) {
			return o instanceof PredictorHashMap;
		}
	};

	UnaryPredicate taskPredicate = new UnaryPredicate() {
		public boolean execute(Object o) {
			if (o instanceof Task) {
				Task tempTask = (Task) o;
				if (tempTask.getVerb().equals(Constants.Verb.PROJECTSUPPLY)) {
					if (!tempTask.getPrepositionalPhrase("For").getIndirectObject().equals(cluster)) return true;
					else return false;
				}
				if (tempTask.getVerb().equals(Constants.Verb.SUPPLY)) {
					if (!tempTask.getPrepositionalPhrase("For").getIndirectObject().equals(cluster)) {
						if (!TaskUtils.isMyRefillTask(tempTask, cluster)) {
							return true;
						}
					}
				}
			}
			return false;
		}
	};


	public void setupSubscriptions() {
		myBlackBoardService = getBlackboardService();
		myLoggingService = (LoggingService) getBindingSite().getServiceBroker().getService(this,
						LoggingService.class, null);
		cluster = ((AgentIdentificationService) getBindingSite().getServiceBroker().getService(this,
						AgentIdentificationService.class, null)).getName();
		as = (AlarmService) getBindingSite().getServiceBroker().getService(this, AlarmService.class, null);
		relationSubscription = (IncrementalSubscription) myBlackBoardService.subscribe(relationPredicate);
		taskSubscription = (IncrementalSubscription) myBlackBoardService.subscribe(taskPredicate);
		myUIDService = (UIDService) getBindingSite().getServiceBroker().getService(this,
						UIDService.class, null);
		if (myBlackBoardService.didRehydrate() == false) myBlackBoardService.setShouldBePersisted(false);
		else {
			rehydrate_flag = true;
			retrieveALfromBB();
		}
	}


	public void execute() {
		if (alarm != null && alarm.hasExpired() == true) {
			count_alarm++;
			if (!rehydrate_flag) {
				if(!modelsPublished) {
					executeAlarm();
					alarm.cancel();
				}
			}
		}
		if (!rehydrate_flag) {
			if(!modelsPublished) {
				relationshipList();
				getPlannedDemand();
			}
		}
	}

	public void retrieveALfromBB() {
		if (phm == null) {
			Collection c = myBlackBoardService.query(ALPredicate);
			for (Iterator iter = c.iterator(); iter.hasNext();) {
				phm = (PredictorHashMap) iter.next();
				if (phm.size() == 0) {
					if (alarm != null) alarm.cancel();
					alarm = new TriggerFlushAlarm(currentTimeMillis() + 60000);
					as.addAlarm(alarm);
					count_alarm = 0;
				} else if (phm.size()!= 0) {
					count_alarm = 1;
					if (alarm!= null) alarm.cancel();
				}
			}
		}
	}

	public void relationshipList() {
		HasRelationships org;
		Collection customers = new HashSet();
		RelationshipSchedule schedule = null;

		for (Enumeration et = relationSubscription.getChangedList(); et.hasMoreElements();) {
			org = (HasRelationships) et.nextElement();
			schedule = org.getRelationshipSchedule();
			Collection ammo_customer = schedule.getMatchingRelationships(Constants.Role.AMMUNITIONCUSTOMER); //Get a collection of ammunition customers
			customers.addAll(ammo_customer);
			Collection fuel_customer = schedule.getMatchingRelationships(Constants.Role.FUELSUPPLYCUSTOMER);
			customers.addAll(fuel_customer);
			Collection packpol_customer = schedule.getMatchingRelationships(Constants.Role.PACKAGEDPOLSUPPLYCUSTOMER);
			customers.addAll(packpol_customer);
			Collection subsistence_customer = schedule.getMatchingRelationships(Constants.Role.SUBSISTENCESUPPLYCUSTOMER);
			customers.addAll(subsistence_customer);
			Collection consumable_customer = schedule.getMatchingRelationships(Constants.Role.SPAREPARTSCUSTOMER);
			customers.addAll(consumable_customer);
			/* Iterate through the collection to get the customers and their supply class
			 * Create HashMap for each Unique relationship
			 * Put the HashMap in an ArrayList
			 */

			for (Iterator iter = customers.iterator(); iter.hasNext();) {
				Relationship orgname = (Relationship) iter.next();
				Asset subOrg = (Asset) schedule.getOther(orgname);
				String role = schedule.getOtherRole(orgname).getName();
				String org_name = subOrg.getClusterPG().getMessageAddress().toString();
				boolean flag = uniqueMatch(cluster, org_name, role);
				if (flag) {
					if (org_name!= null && role!= null) {
						tick++;
						role = stringRevManipulation(role);
						myLoggingService.debug("Supplier : " + cluster + "| Customer: " + org_name + "| SupplyClass "+role);
						phm.addHashMap(org_name, role);
						if(tick == 1) phm.setUID(myUIDService.nextUID());
					}
				}
			}
		}
	}

	public void getPlannedDemand() {
		Task task;
		//if(phm.getMap()!= null || !phm.getMap().isEmpty()) HashMap hashmap = phm.getMap();	else return;
		for (Enumeration e = taskSubscription.getAddedList(); e.hasMoreElements();) {
			task = (Task) e.nextElement();
			String owner = task.getPrepositionalPhrase("For").getIndirectObject().toString();
			String comp = (String) task.getPrepositionalPhrase("OfType").getIndirectObject();
			if (comp!= null && owner!= null) {
				if (alarm != null) alarm.cancel();
          alarm = new TriggerFlushAlarm(currentTimeMillis() + 60000);
          as.addAlarm(alarm);
					taskList.add(task);
				}
			}
	}

	public void executeAlarm() {
		HashMap processedMap = null;
		myBlackBoardService.publishAdd(phm);
		HashMap hashmap = phm.getMap();
		ProcessHashData phd = new ProcessHashData(cluster, hashmap, taskList);
		if (phd!= null) processedMap = phd.iterateList();
			if (processedMap!= null) {
					myBlackBoardService.publishChange(phm);
					modelsPublished = true;
					if(alarm!= null) alarm.cancel();
			}
	}


	/* This method makes sure each hashtable for a given supplier, customer
	 * and supply class is unique
	 */
	public boolean uniqueMatch(String a, String b, String c) {
		boolean flag = false;
		StringBuffer sb = new StringBuffer().append(a).append(b).append(c);
		if (matchMap.isEmpty()) {
			matchMap.put(sb.toString(), null);
			flag = true;
			return flag;
		} else if (!matchMap.containsKey(sb.toString())) {
			matchMap.put(sb.toString(), null);
			flag = true;
			return flag;
		} else return flag;
	}

	/* Converts the role string into Supply Class string */
	public String stringRevManipulation(String s_class) {

		final String ammo = "Ammunition";
		final String packpol = "PackagedPOL";
		final String subs = "Subsistence";
		final String bulkpol = "BulkPOL";
		final String consumable = "Consumable";

		if (s_class.equalsIgnoreCase("AmmunitionCustomer")) return ammo;
		if (s_class.equalsIgnoreCase("SubsistenceSupplyCustomer")) return subs;
		if (s_class.equalsIgnoreCase("PackagedPolSupplyCustomer")) return packpol;
		if (s_class.equalsIgnoreCase("FuelSupplyCustomer")) return bulkpol;
		if (s_class.equalsIgnoreCase("SparePartsCustomer")) return consumable;

		return null;
	}

	/* Alarm class to trigger the processing of projection data obtained during planning
	 * The trigger goes off when no more projection data is obtained for a period of 1 minute
	 * where the assumption is 1 minute is a sufficient long time for no task occurences
	 */
	class TriggerFlushAlarm implements PeriodicAlarm {
		public TriggerFlushAlarm(long expTime) {
			this.expTime = expTime;
		}

		public void reset(long currentTime) {
			expTime = currentTime + delay;
			expired = false;
		}

		public long getExpirationTime() {
			return expTime;
		}

		public void expire() {
			expired = true;
			myBlackBoardService.signalClientActivity();
			cancel();
		}

		public boolean hasExpired() {
			return expired;
		}

		public boolean cancel() {
			boolean was = expired;
			expired = true;
			return was;
		}

		boolean expired = false;
		long expTime;
		long delay = 60000;
	}

}