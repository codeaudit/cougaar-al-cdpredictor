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
import org.cougaar.core.util.UID;
import org.cougaar.glm.ldm.Constants;
import org.cougaar.logistics.plugin.inventory.TaskUtils;
import org.cougaar.logistics.servlet.CommStatus;
import org.cougaar.planning.ldm.PlanningFactory;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.plan.*;
import org.cougaar.util.UnaryPredicate;
import org.cougaar.util.Thunk;
import org.cougaar.util.Collectors;

import java.util.*;
import java.io.PrintWriter;
import java.io.BufferedWriter;


public class PredictorPlugin extends ComponentPlugin {

	private LoggingService myLoggingService;
	private DomainService myDomainService;
	private BlackboardService myBS;
	private AlarmService as;

	private IncrementalSubscription dataModelMapSubscription;
	private IncrementalSubscription taskSubscription;
	private IncrementalSubscription commstatusSubscription;
	private IncrementalSubscription salSubscription;

	private String cluster;
	private String customerAgentName;

	TriggerFlushAlarm alarm = null;
	ExecutionDemandUpdate edu = new ExecutionDemandUpdate();
	private KalmanFilter kf = null;
	PredictorSupplyArrayList local_alist = new PredictorSupplyArrayList();

	private final Verb forecastVerb = org.cougaar.logistics.ldm.Constants.Verb.Supply;

	private HashMap hashmap = null;
	private ArrayList asset_list = new ArrayList();

	private boolean toggle = false;
	private boolean flag = false;
	private boolean rehydrate_flag = false;
	public boolean status = true;
	public boolean comm_restore_flag = false;

	private long cutOffTime = 0;
	long commLossTime = -1;
	long commRestoreTime = -1;
	long pcd = -1;
	long clt = -1;
	long gap = -1;
	long ost = -1;
	long time_gap = -1;

	private final int MovingAverage = 1;
	private final int SupportVectorMachine = 2;
	private final int KalmanFilter = 3;
	private int selectedPredictor = KalmanFilter;
	private Collection taskColl = new ArrayList();
	private String[] sClassNames = {"Ammunition","Subsistence","BulkPOL","PackagedPOL","Consumable"};
	private HashMap maxEndTimePerSC = new HashMap();


	UnaryPredicate taskPredicate = new UnaryPredicate() {
		public boolean execute(Object o) {
			if (o instanceof Task) {
				Task tempTask = (Task)o;
				if (tempTask.getVerb().equals(Constants.Verb.SUPPLY)) {
					if (!tempTask.getPrepositionalPhrase("For").getIndirectObject().equals(cluster)) {
						if (!TaskUtils.isMyRefillTask(tempTask, cluster)) {
							if(tempTask.getDirectObject().getTypeIdentificationPG().getTypeIdentification().equalsIgnoreCase("DODIC/C625")){
								System.out.println("DODIC/C625FoundInPartOneAfterRefillTaskCheck"+" UID "+tempTask.getUID()+" ParentTaskUID "
								+tempTask.getParentTaskUID());
						}
							return true;
						}
					}
				} else {
					return false;
				}
			}
			return false;
		}
	};

	UnaryPredicate dataModelMapPredicate = new UnaryPredicate() {
		public boolean execute(Object o) {
			return o instanceof PredictorHashMap;
		}
	};

	UnaryPredicate supplyArrayListPredicate = new UnaryPredicate() {
		public boolean execute(Object o) {
			return o instanceof PredictorSupplyArrayList;
		}
	};

	UnaryPredicate commstatusPredicate = new UnaryPredicate() {
		public boolean execute(Object o) {
			return o instanceof CommStatus;
		}
	};

	public void setupSubscriptions() {

		cluster = ((AgentIdentificationService) getBindingSite().getServiceBroker().getService(this, AgentIdentificationService.class, null)).getName();
		myBS = getBlackboardService();
		myDomainService = (DomainService) getBindingSite().getServiceBroker().getService(this, DomainService.class, null);
		myLoggingService = (LoggingService) getBindingSite().getServiceBroker().getService(this, LoggingService.class, null);
		as = (AlarmService) getBindingSite().getServiceBroker().getService(this, AlarmService.class, null);
		commstatusSubscription = (IncrementalSubscription) myBS.subscribe(commstatusPredicate);
		taskSubscription = (IncrementalSubscription) myBS.subscribe(taskPredicate);
		//if (!taskSubscription.isEmpty()) taskSubscription.clear();
		if (selectedPredictor == KalmanFilter) {
			salSubscription = (IncrementalSubscription) myBS.subscribe(supplyArrayListPredicate);
			dataModelMapSubscription = (IncrementalSubscription) myBS.subscribe(dataModelMapPredicate);
			if (myBS.didRehydrate() == false) myBS.setShouldBePersisted(false);
			else {
				rehydrate_flag = true;
				retrieveExecutionDataListFromBB();
				if (hashmap == null)	retrievePlanningDataListFromBB();
			}
		} else {
			//Put the subscriptions here for a different algorithm (Moving average, Support Vector) implementation
		}
	}

	//Kalman Filter specific data retrieval after rehydration
	public void retrieveExecutionDataListFromBB() {
		if (local_alist == null) {
			Collection c = myBS.query(supplyArrayListPredicate);
			for (Iterator iter = c.iterator(); iter.hasNext();) {
				local_alist = (PredictorSupplyArrayList) iter.next();
				System.out.println("local_alist rehydrated "+local_alist.size());
			}
		}
	}

	//Kalman Filter specific data retrieval after rehydration
	public void retrievePlanningDataListFromBB() {
		if (hashmap == null) {
			flag = false;
			Collection c = myBS.query(dataModelMapPredicate);
			for (Iterator iter = c.iterator(); iter.hasNext();) {
				PredictorHashMap pHashmap = (PredictorHashMap) iter.next();
				hashmap = pHashmap.getMap();
				if (hashmap.size() >= 1) {
					kf = new KalmanFilter(hashmap);
					myBS.publishAdd(kf);
					flag = true;
				}
			}
		}
	}

	public void callPredictor() {
		if (selectedPredictor == KalmanFilter) {
			publishPredictions();
		} else {
			//Call different algorithm implementation here
		}
	}

	//Kalman Filter specific implementation
	public void checkdataModelMapSubscription() {
		Collection e = taskSubscription.getAddedCollection();
		System.out.println("taskSubscription Size "+e.size()+" Current time "+new Date(currentTimeMillis()));
		for (Enumeration et = dataModelMapSubscription.getChangedList(); et.hasMoreElements();) {
			PredictorHashMap pHashmap = (PredictorHashMap)et.nextElement();
			hashmap = pHashmap.getMap();
			if (hashmap!= null) {
				myLoggingService.shout("DemandModel Received by agent " + cluster+" Modelsize: " + hashmap.size());
				printMapDetails(hashmap);
				kf = new KalmanFilter(hashmap);
				myBS.publishAdd(kf);
			  flag = true;
			}
		}
	}

	public void printMapDetails(HashMap hashmap){
		for (Iterator iterator = hashmap.values().iterator(); iterator.hasNext();) {
			HashMap hashMap = (HashMap)iterator.next();
			for (Iterator iterator1 = hashMap.keySet().iterator(); iterator1.hasNext();) {
				String itemname = (String) iterator1.next();
				if(itemname.equalsIgnoreCase("JP8")) {
					ArrayList list = (ArrayList)hashMap.get(itemname);
					for (Iterator iterator2 = list.iterator(); iterator2.hasNext();) {
						Values values = (Values) iterator2.next();
						System.out.println("HashMapDetailsJP8 "+new Date(values.getEndTime())+" "+values.getQuantity());
					}
				}
			}
		}
	}

	public void checkCommStatusSubscription() {
		if (!commstatusSubscription.isEmpty()) {
			Enumeration e1 = commstatusSubscription.getAddedList();
			Enumeration e2 = commstatusSubscription.getChangedList();
			if (e1.hasMoreElements() == true) {
				getCommStatusObject(e1);
			} else if (e2.hasMoreElements() == true) {
				getCommStatusObject(e2);
			}
		}
	}

	public void checkTaskSubscription() {
		if (!taskSubscription.isEmpty()) {
			Enumeration e1 = taskSubscription.getAddedList();
			Enumeration e2 = taskSubscription.getChangedList();
			if (e1.hasMoreElements() == true) {
				//task = (Task) e1.nextElement();
				//getActualDemand(task);
			} else if (e2.hasMoreElements() == true) {
				//task = (Task) e2.nextElement();
				//getActualDemand(task);
			}
		} else {
			return;
		}
	}

	private void setDemandDataList(PredictorSupplyArrayList demandDataList) {
		if(local_alist.isEmpty())	{
			this.local_alist.addAll(demandDataList);
			myBS.publishAdd(local_alist);
		}
		else {
			//local_alist.clear();
			local_alist.addAll(demandDataList);
			myBS.publishChange(local_alist);

		}
	}

	private ArrayList getDemandDataList() {
		return local_alist;
	}

	public void getCommStatusObject(Enumeration e) {
		CommStatus cs = (CommStatus) e.nextElement();
		customerAgentName = cs.getConnectedAgentName();
		status = cs.isCommUp();
		if (!status) {
			commLossTime = cs.getCommLossTime();
			PredictorSupplyArrayList executionDataList = edu.getExecutionDemandPerDay();
			System.out.println("executionDataList size "+executionDataList.size());
			kf.measurementUpdate(executionDataList);
			setDemandDataList(executionDataList);
			alarm = new TriggerFlushAlarm(currentTimeMillis());
			as.addAlarm(alarm);
			System.out.println("CurrentTimeMillisForPP "+new Date(currentTimeMillis()));
			//findLastSupplyTaskTime(taskColl, customerAgentName);
			for(int i = 0; i < sClassNames.length; i++) {
				long endTime = findLastSupplyTaskTimePerSC(taskColl, sClassNames[i], customerAgentName);
				maxEndTimePerSC.put(sClassNames[i],new Long(endTime));
			}
		} else {
			commRestoreTime = cs.getCommRestoreTime();
			time_gap = (int) (((commRestoreTime - commLossTime) / 86400000) * 4);
			cutOffTime = commRestoreTime / 86400000;
			if (alarm.hasExpired() == false) {
				alarm.cancel();
			}
		}
	}

	public void execute() {
		if (selectedPredictor == KalmanFilter) {
			if (!flag) checkdataModelMapSubscription(); //This executes once to activate this plugin
			else {
				if (alarm!= null && alarm.hasExpired() == true) callPredictor();
				checkCommStatusSubscription();
				actualInputDemand();
			}
		} else {
			//Other Algorithm Implementation here
		}
	}

	public void actualInputDemand() {
		Task task;
		long currentTime = currentTimeMillis()/86400000; //Time in terms of days
		//System.out.println("PPlugincurrentTime "+currentTime);
		//if(currentTime > 1) System.out.println("InsideActualInputDemand Time "+new Date(currentTimeMillis()));
		if(status) {
			Collection e = taskSubscription.getCollection();
			for (Iterator iter = e.iterator(); iter.hasNext();) {
			//for(Enumeration e = taskSubscription.getAddedList(); e.hasMoreElements();) {
				task = (Task)iter.next();
			  //task = (Task)e.nextElement();
				taskColl.add(task);
				String customer = (String) task.getPrepositionalPhrase("For").getIndirectObject();
				String supplyclass = (String) task.getPrepositionalPhrase("OfType").getIndirectObject();
				Asset as = task.getDirectObject();
				//System.out.println("LISTTASKS Customer "+customer+" supplyClass "+supplyclass+" item "+as.getTypeIdentificationPG().getTypeIdentification());
				//String item_name = as.getTypeIdentificationPG().getNomenclature();

				if(as!= null) {
					String item_name = as.getTypeIdentificationPG().getTypeIdentification();
					System.out.println("PPlugin asset: UID"+" "+task.getUID()+" "+customer+" "
					+supplyclass+" "+item_name+" ParentTaskUID "+task.getParentTaskUID()+" time "+
					(long) (task.getPreferredValue(AspectType.END_TIME)) +" quantity "+
					task.getPreferredValue(AspectType.QUANTITY)+ " verb "+task.getVerb());

				//else System.out.println("PPlugin asset null:  TaskUID"+task.getUID()+" "+customer+" "+supplyclass
					//			+" "+item_name+" ParentTaskUID "+task.getParentTaskUID());
				addAsset(as);
				if(task.getUID().getOwner().equalsIgnoreCase("Prediction")) System.out.println("Is a Prediction Task: "+task.getUID());
				//ArrayList assetList = getAssetList();
				//if (assetList.size()== 1) myBS.publishAdd(assetList); else myBS.publishChange(assetList);

				if (selectedPredictor == KalmanFilter) {
					if (currentTime >= 0) {
						if (!toggle) {  //To initially set positive non-zero cutofftime
							cutOffTime = currentTime;
							toggle = true; //Always remains true thereafter, never reset back to false
						}
						long endTime = (long) (task.getPreferredValue(AspectType.END_TIME));
						double quantity = task.getPreferredValue(AspectType.QUANTITY);
						long commitmentTime = task.getCommitmentDate().getTime();
						Values elementValues = new Values(currentTime*86400000, commitmentTime, endTime, quantity);
						if (currentTime <= cutOffTime) {
							edu.addDemandData(customer, supplyclass, item_name, elementValues);
						} else {
							PredictorSupplyArrayList executionDataList = edu.getExecutionDemandPerDay();
							//long lastDate = edu.getLastDemandDate(customer, supplyclass);
							//System.out.println("LastDatePP "+" Customer "+customer+" Supply Class "+supplyclass+
										//	" lastDate "+lastDate);
							kf.measurementUpdate(executionDataList);
							setDemandDataList(executionDataList);
							//edu.clearDemandDataMap();
							edu.addDemandData(customer, supplyclass, item_name, elementValues);
							cutOffTime = currentTime;
						}
					}
				}
			}
			}
		} else return;
	}

	public void addAsset(Asset as) {
		//String itemname = as.getTypeIdentificationPG().getNomenclature();
		String itemname = as.getTypeIdentificationPG().getTypeIdentification();
		boolean isPresent = false;
		if (asset_list.isEmpty())	asset_list.add(as);
		else {
			for (int i = 0; i < asset_list.size(); i++) {
				//String test = ((Asset) asset_list.get(i)).getTypeIdentificationPG().getNomenclature();
				String test = ((Asset) asset_list.get(i)).getTypeIdentificationPG().getTypeIdentification();
				if (test.equalsIgnoreCase(itemname) == true) {
					isPresent = true;
					break;
				}
			}
			if (!isPresent) asset_list.add(as);
		}
	}

	public ArrayList getAssetList() {
		return asset_list;
	}

	public Asset getAsset(String itemName) {
		for (int i = 0; i < asset_list.size(); i++) {
			Asset asset = (Asset) asset_list.get(i);
			//String item_name = asset.getTypeIdentificationPG().getNomenclature();
			String item_name = asset.getTypeIdentificationPG().getTypeIdentification();
			if (item_name.equalsIgnoreCase(itemName)) return asset;
		}
		return null;
	}

	public long getCustomerLeadTime(long lastSupplyDay, long commLossDay) {
		clt = lastSupplyDay  - commLossDay;
		return clt;
	}

	public long getPredictorGap(long lastSupplyDay, long clt) {
		gap = currentTimeMillis() + clt - lastSupplyDay;
		gap = (int) (gap / 86400000);
		gap = gap * 86400000;
		return gap;
	}

	public long getOrderShipTime(long lastSupplyDay, long commitmentDay) {
		ost = lastSupplyDay - commitmentDay;
		return ost;
	}

	public void setPredictionCommitmentDate(long lastSupplyDay, long ordershiptime) {
		pcd = lastSupplyDay + gap - ordershiptime;
	}

	public long getPredictionCommitmentDate() {
		return pcd;
	}

	//Get the current day value and the previous 4 values from the stored list
	//Apply weighted average over the 5 values to generate prediction for next day
	//or gap period
	public void publishPredictions1() {
		if (status)return;
		else {
			if(!comm_restore_flag) {
				if (alarm != null) alarm.cancel();
				alarm = new TriggerFlushAlarm(currentTimeMillis() + 86400000);
				as.addAlarm(alarm);
			}
			if (!getDemandDataList().isEmpty()) {
				HashMap executionDataList = (HashMap)local_alist.get(0);
				for (Iterator iterator = executionDataList.keySet().iterator(); iterator.hasNext();) {
					CustomerRoleKey crk = (CustomerRoleKey) iterator.next();
					if (crk.getCustomerName().equalsIgnoreCase(customerAgentName)) {
						HashMap itemMap = (HashMap) executionDataList.get(crk);
						for (Iterator iterator1 = itemMap.keySet().iterator(); iterator1.hasNext();) {
							String itemName = (String) iterator1.next();
							ArrayList valueList = (ArrayList) itemMap.get(itemName);
							for (Iterator iterator2 = valueList.iterator(); iterator2.hasNext();) {
								Values values = (Values) iterator2.next();
								//long endTime = values.getEndTime();
								long endTime = findLastSupplyTaskTimePerSC(taskColl, crk.getRoleName(),customerAgentName );
								//System.out.println("crk.getRoleName() "+crk.getRoleName());
								long commitmentTime = values.getCommitmentTime();
								double quantity = values.getQuantity();
								long customer_lead_time = getCustomerLeadTime(endTime, commLossTime);
								long pred_gap = getPredictorGap(endTime, customer_lead_time);
								long order_ship_time = getOrderShipTime(endTime, commitmentTime);
								setPredictionCommitmentDate(endTime, order_ship_time);
								if (time_gap == -1 || time_gap <= ((int) pred_gap/86400000)) {
									if ((int) pred_gap / 86400000 >= 1) {
										if ((((int) pred_gap / 86400000) % 4) == 0 || ((int) pred_gap / 86400000) == 1) {
											HashMap planItemMap = (HashMap) hashmap.get(crk);
											ArrayList planValueList = (ArrayList) planItemMap.get(itemName);
											double firstDataPoint = 0.0;
											double secondDataPoint = 0.0;
											double thirdDataPoint = 0.0;
											for (Iterator iter = planValueList.iterator(); iter.hasNext();) {
												Values planValues = (Values) iter.next();
												if (planValues.getEndTime() == (endTime - (4 * 1 * 86400000))) {
													firstDataPoint = planValues.getQuantity();
												}
												if (planValues.getEndTime() == (endTime - (4 * 2 * 86400000))) {
													secondDataPoint = planValues.getQuantity();
												}
												if (planValues.getEndTime() == (endTime - (4 * 3 * 86400000))) {
													thirdDataPoint = planValues.getQuantity();
												}
											}
											double finalPredictionQuantity = 0.7 * quantity +
															0.2 * firstDataPoint + 0.1 * secondDataPoint + 0.05 * thirdDataPoint;
											if (((int) pred_gap / 86400000) == 1) {
												commitmentTime = commitmentTime + 3 * 86400000;
												endTime = endTime + 3 * 86400000;
												Values v = new Values(commitmentTime, endTime, finalPredictionQuantity);
												NewTask new_task = getNewTask(crk.getCustomerName(), crk.getRoleName(), itemName, v);
												myBS.publishAdd(new_task);
												if(itemName.equalsIgnoreCase("GLOWPLUG")||itemName.equalsIgnoreCase("JP8"))
												myLoggingService.shout(cluster + ": NEW TASK ADDED PPOUTPUT1" + new_task);
											} else {
												commitmentTime = commitmentTime + 4 * 86400000;
												endTime = endTime + 3 * 86400000;
												Values v = new Values(commitmentTime, endTime, finalPredictionQuantity);
												NewTask new_task = getNewTask(crk.getCustomerName(), crk.getRoleName(), itemName, v);
												myBS.publishAdd(new_task);
												if(itemName.equalsIgnoreCase("GLOWPLUG")||itemName.equalsIgnoreCase("JP8"))
												myLoggingService.shout(cluster + ": NEW TASK ADDED PPOUTPUT" + new_task);
											}
										} else
											break;
									} else
										break;
								}
							}
						}
					}
				}
			}
			comm_restore_flag = false;
		}
	}

	public void publishPredictions() {
	if (status) return;
	else {
		if(!comm_restore_flag) {
			if (alarm != null) alarm.cancel();
				alarm = new TriggerFlushAlarm(currentTimeMillis() + 86400000);
				as.addAlarm(alarm);
			}
			int number_of_prediction_items = 0;
			for (Iterator iterator = hashmap.keySet().iterator(); iterator.hasNext();) {
				CustomerRoleKey customerRoleKey = (CustomerRoleKey) iterator.next();
				if(customerRoleKey.getCustomerName().equalsIgnoreCase(customerAgentName)) {
					long endTime = 0;
					for(Iterator iter = maxEndTimePerSC.keySet().iterator();iter.hasNext();){
						String key = (String)iter.next();
						if(customerRoleKey.getRoleName().equalsIgnoreCase(key)){
							endTime = ((Long)maxEndTimePerSC.get(key)).longValue();
							break;
						}
					}
					//long endTime = findLastSupplyTaskTimePerSC(taskColl, customerRoleKey.getRoleName(), customerAgentName);
					long commitmentTime = endTime - 86400000; //hardcode as of now
					long customer_lead_time = getCustomerLeadTime(endTime, commLossTime);
					long pred_gap = getPredictorGap(endTime, customer_lead_time);
					long order_ship_time = getOrderShipTime(endTime, commitmentTime);
					setPredictionCommitmentDate(endTime, order_ship_time);

					HashMap item_map = (HashMap)hashmap.get(customerRoleKey);
					for (Iterator iterator1 = item_map.keySet().iterator(); iterator1.hasNext();) {
						String item_name = (String) iterator1.next();
						ArrayList valueList = (ArrayList)item_map.get(item_name);
						for (Iterator iterator2 = valueList.iterator(); iterator2.hasNext();) {
							Values values = (Values)iterator2.next();
							double quantity = values.getQuantity();
							long execEndTime = -1;
							double execQuantity = -1;
							for(Iterator it = local_alist.iterator();it.hasNext();) {
								HashMap executionDataList = (HashMap)it.next();
								//for (Iterator it1 = executionDataList.keySet().iterator(); it1.hasNext();) {
								//	CustomerRoleKey crk = (CustomerRoleKey) it1.next();
								//HashMap executionDataList = (HashMap)local_alist.get(0);
									HashMap itemMap = (HashMap)executionDataList.get(customerRoleKey);
									if(itemMap!= null) {
										ArrayList valueList1 = (ArrayList)itemMap.get(item_name);
										//System.out.println("itemMAPexistsForItem "+item_name);
										if(valueList1!= null) {
											for (Iterator iter2 = valueList1.iterator(); iter2.hasNext();) {
												Values values1 = (Values) iter2.next();
												execEndTime = values1.getEndTime();
												execQuantity = values1.getQuantity();
												if(item_name.equalsIgnoreCase("JP8")) System.out.println("execEndTimeJP8 "+new Date(execEndTime)+" execQuantity "+execQuantity);
											}
											quantity = execQuantity;
											System.out.println("execQuantityPrediction "+quantity+" itemname "+item_name);
											if(item_name.equalsIgnoreCase("JP8")) System.out.println("QuantitySetJP8 "+quantity);
										}
									}
								//}
							}
							if (time_gap == -1 || time_gap <= ((int) pred_gap/86400000)) {
								if ((int) pred_gap / 86400000 >= 1) {
									if ((((int) pred_gap / 86400000) % 4) == 0 || ((int) pred_gap / 86400000) == 1) {
								/*		double firstDataPoint = 0.0;
										double secondDataPoint = 0.0;
										double thirdDataPoint = 0.0;
										if (values.getEndTime() == (endTime - (4 * 1 * 86400000))) {
											firstDataPoint = values.getQuantity();
										}
										if (values.getEndTime() == (endTime - (4 * 2 * 86400000))) {
											secondDataPoint = values.getQuantity();
										}
										if (values.getEndTime() == (endTime - (4 * 3 * 86400000))) {
											thirdDataPoint = values.getQuantity();
										} */
										//double finalPredictionQuantity = 0.7 * quantity + 0.2 * firstDataPoint + 0.1 * secondDataPoint + 0.05 * thirdDataPoint;
										double finalPredictionQuantity = quantity;
										if(finalPredictionQuantity!= -1) {
											if (((int) pred_gap / 86400000) == 1) {
												commitmentTime = getPredictionCommitmentDate() + 3 * 86400000;
												endTime = commitmentTime + 86400000;
												Values v = new Values(commitmentTime, endTime, finalPredictionQuantity);
												NewTask new_task = getNewTask(customerRoleKey.getCustomerName(), customerRoleKey.getRoleName(),
																					 item_name, v);
												myBS.publishAdd(new_task);
												myLoggingService.shout(cluster + ": NEW TASK ADDED PPOUTPUT1" + new_task);
												number_of_prediction_items++;
												break;
											} else {
												commitmentTime = getPredictionCommitmentDate() + 4 * 86400000;
												endTime = commitmentTime + 86400000;
												Values v = new Values(commitmentTime, endTime, finalPredictionQuantity);
												NewTask new_task = getNewTask(customerRoleKey.getCustomerName(), customerRoleKey.getRoleName(),
																					 item_name, v);
												myBS.publishAdd(new_task);
												myLoggingService.shout(cluster + ": NEW TASK ADDED PPOUTPUT" + new_task);
												number_of_prediction_items++;
												break;
											}
										}
									} else break;
								} else break;
							} else break;
						}
					}
				}
			}
			comm_restore_flag = false;
			System.out.println("Number of Predictiontasks added "+number_of_prediction_items+" cluster "+cluster);
		}
	}

	public NewTask getNewTask(String customer, String supplyClass, String itemName, Values v) {
		PlanningFactory pf = (PlanningFactory) myDomainService.getFactory("planning");
		NewTask nt = pf.newTask();
		nt.setVerb(forecastVerb);

		NewPrepositionalPhrase npp = pf.newPrepositionalPhrase();
		npp.setPreposition(Constants.Preposition.FOR);
		npp.setIndirectObject(customer);
		nt.addPrepositionalPhrase(npp);

		NewPrepositionalPhrase npp1 = pf.newPrepositionalPhrase();
		npp1.setPreposition(Constants.Preposition.OFTYPE);
		npp1.setIndirectObject(supplyClass);
		nt.addPrepositionalPhrase(npp1);

		nt.setCommitmentDate(new Date(v.getCommitmentTime()));

		AspectValue av = AspectValue.newAspectValue(AspectType.END_TIME, new Long(v.getEndTime()));
		Preference np = pf.newPreference(av.getAspectType(), ScoringFunction.createStrictlyAtValue(av));
		nt.addPreference(np);

		AspectValue av1 = AspectValue.newAspectValue(AspectType.QUANTITY, new Double(v.getQuantity()));
		Preference np1 = pf.newPreference(av1.getAspectType(), ScoringFunction.createStrictlyAtValue(av1));
		nt.addPreference(np1);

		Asset asset = getAsset(itemName);
		if (asset!= null) {
			UID uid = asset.getUID();
			long id = uid.getId();
			UID newuid = new UID("Prediction", id);
			asset.setUID(newuid);
			nt.setDirectObject(asset);
		} else {
			for (Iterator iterator = hashmap.values().iterator(); iterator.hasNext();) {
				HashMap map = (HashMap) iterator.next();
				boolean foundAsset = false;
				for (Iterator iterator1 = map.keySet().iterator(); iterator1.hasNext();) {
					String itemname = (String) iterator1.next();
					if(itemname.equalsIgnoreCase(itemName)) {
						ArrayList valuesList = (ArrayList)map.get(itemName);
						Values value = (Values)valuesList.get(0);
						asset = value.getAsset();
						UID uid = asset.getUID();
						long id = uid.getId();
						UID newuid = new UID("Prediction", id);
						asset.setUID(newuid);
						nt.setDirectObject(asset);
						System.out.println("FoundAssetFromPlanningData "+asset.getUID());
						foundAsset = true;
						break;
					}
				} if(foundAsset)	break;
			}
		}
		return nt;
	}

	public void disposition(NewTask newtask) {
		PlanningFactory pf = (PlanningFactory) myDomainService.getFactory("planning");
		AspectValue av_array[] = new AspectValue[2];
		av_array[0] = AspectValue.newAspectValue(AspectType.END_TIME,
						TaskUtils.getPreference(newtask, AspectType.END_TIME));
		av_array[1] = AspectValue.newAspectValue(AspectType.QUANTITY,
						TaskUtils.getPreference(newtask, AspectType.QUANTITY));
		AllocationResult dispAR =
						pf.newAllocationResult(1.0, false, av_array);
		Disposition disp = pf.createDisposition(newtask.getPlan(), newtask, dispAR);
		myBS.publishAdd(disp);

	}

	 private long findLastSupplyTaskTime(Collection tasks, String customerName) {
		 MaxEndThunk thunk = new MaxEndThunk(customerName);
		 Collectors.apply(thunk, tasks);
		 return thunk.getMaxEndTime();
	 }

	private long findLastSupplyTaskTimePerSC(Collection Alltasks, String supplyClass, String customerName) {
		 MaxEndThunk thunk = new MaxEndThunk(customerName, supplyClass);
		 Collection tasks = filterTasks(Alltasks, customerName);
		 Collectors.apply(thunk, tasks);
		 return thunk.getMaxEndTime(supplyClass);
	 }

	private Collection filterTasks(Collection tasks, String customerAgentName){
		Collection c = new ArrayList();
		for (Iterator iterator = tasks.iterator(); iterator.hasNext();) {
			Task task = (Task) iterator.next();
			String customer = (String) task.getPrepositionalPhrase("For").getIndirectObject();
			if(customer.equalsIgnoreCase(customerAgentName)){
				c.add(task);
			}
		}
		return c;
	}

	public class TriggerFlushAlarm implements PeriodicAlarm {

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
			myBS.signalClientActivity();
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
		long delay = 86400000;
	};



	private class MaxEndThunk implements Thunk {
		 long maxEnd = Long.MIN_VALUE;
		 String customerName;
		 String supplyClass;
		 Task lastTask;
		 HashMap endTimes = new HashMap();

		 public MaxEndThunk (String customerName) {
			 this.customerName = customerName;
		 }

		public MaxEndThunk (String customerName, String supplyClass) {
			 this.customerName = customerName;
			 this.supplyClass = supplyClass;
		 }

		  public void apply(Object o) {
			 if (o instanceof Task) {
				 Task task = (Task) o;
				 String supplyclass = (String) task.getPrepositionalPhrase("OfType").getIndirectObject();
				 if(endTimes.containsKey(supplyclass)) {
					 Long endtime = (Long)endTimes.get(supplyclass);
					 long endTime = TaskUtils.getEndTime(task);
					 if (endTime > endtime.longValue()) {
						 endTimes.put(supplyclass, new Long(endTime));
						 maxEnd = endTime;
						 lastTask = task;
					 }
				 } else {
					 long endTime = TaskUtils.getEndTime(task);
					 endTimes.put(supplyclass, new Long(endTime));
					 if(endTime > maxEnd){
						 maxEnd = endTime;
						 lastTask = task;
					 }
				 }
			 }
		 }

		 public long getMaxEndTime(){
			 for (Iterator iterator = endTimes.keySet().iterator(); iterator.hasNext();) {
				 String supplyClass = (String) iterator.next();
				 Long endtime = (Long)endTimes.get(supplyClass);
				 System.out.println("PredictorPluginLastTask  MaxEndTime "+(new Date(endtime.longValue())).toString()+" Customer "+customerName+" Supplier "+cluster+
							 " SupplyClass "+supplyClass);
			 }
			 return maxEnd;
		 }

		 public long getMaxEndTime(String supplyClass){
			 if(endTimes.containsKey(supplyClass)){
				 Long endtime = (Long)endTimes.get(supplyClass);
				 System.out.println("PredictorPluginLastTask MaxEndTimeForSC "+(new Date(endtime.longValue())).toString()+" Customer "+customerName+" Supplier "+cluster
				 +" SupplyClass "+supplyClass);
				 return endtime.longValue();
			 }
			 else return -1;
		 }
	 }

}
