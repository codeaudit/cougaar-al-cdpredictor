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


package org.cougaar.tools.alf.sensor.plugin;

import org.cougaar.core.adaptivity.InterAgentCondition;
import org.cougaar.core.agent.service.alarm.PeriodicAlarm;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.plugin.ComponentPlugin;
import org.cougaar.core.service.*;
import org.cougaar.core.util.UID;
import org.cougaar.glm.ldm.Constants;
import org.cougaar.glm.ldm.asset.NewSupplyClassPG;
import org.cougaar.glm.ldm.asset.SupplyClassPG;
import org.cougaar.glm.ldm.asset.PackagedPOL;
import org.cougaar.planning.ldm.PlanningFactory;
import org.cougaar.planning.ldm.asset.*;
import org.cougaar.planning.ldm.plan.*;
import org.cougaar.planning.ldm.LDMServesPlugin;
import org.cougaar.util.ConfigFinder;
import org.cougaar.util.UnaryPredicate;
import org.cougaar.glm.plugins.TaskUtils;
import org.cougaar.planning.service.LDMService;
import org.cougaar.core.component.ServiceRevokedListener;
import org.cougaar.core.component.ServiceRevokedEvent;
import org.cougaar.planning.service.PrototypeRegistryService;
import org.cougaar.core.mts.MessageAddress;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;

/** 
 *	programed by Yunho Hong and Himanshu Gupta
 *	June 10, 2003
 *	PSU-IAI
**/

public class PredictorPlugin extends ComponentPlugin {

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
            myBS.openTransaction();
			callPredictor();
            myBS.closeTransaction();
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

    UnaryPredicate taskPredicate = new UnaryPredicate() {
        public boolean execute(Object o) {
            return o instanceof Task;
        }
    };

    UnaryPredicate servletPredicate = new UnaryPredicate() {
        public boolean execute(Object o) {
            return o instanceof InterAgentCondition;
        }

    };

    UnaryPredicate arrayListPredicate = new UnaryPredicate() {
        public boolean execute(Object o) {
            return o instanceof PredictorArrayList;
        }
    };

    UnaryPredicate supplyTaskPredicate = new UnaryPredicate() {
        public boolean execute(Object o) {
            if (o instanceof Task) {
                Task tempTask = (Task) o;
                Verb verb = tempTask.getVerb();

                if (verb.equals("Supply")) {
                    PrepositionalPhrase pp = null;
                    if ((pp = tempTask.getPrepositionalPhrase("OfType")) != null) {
                        String s = (String) pp.getIndirectObject();

                        if (s.equalsIgnoreCase("BulkPOL") || s.equalsIgnoreCase("Ammunition")) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }
    };

    UnaryPredicate relationPredicate = new UnaryPredicate() {
        public boolean execute(Object o) {
            if (o instanceof HasRelationships) {
                return ((HasRelationships) o).isLocal();
            } else {
                return false;
            }
        }
    };

	UnaryPredicate historyPredicate	= new UnaryPredicate()	{ public boolean execute(Object o) {  return o instanceof HashMap;   }    };

	class History
	{
		public int lastDay = -1, startDay = 1000000, demandDay = 0;  // demandDay is the day on which supply tasks are generated.
		HashMap history=null;

		public History(int demandDay) {
			this.demandDay = demandDay;
			history = new HashMap();
		} 
		
		public void add(int date, double demand) {

			if (lastDay < date) 	{ 	lastDay = date;		}
			if (startDay > date)    { 	startDay = date;	}

			Double Demand = (Double) history.get(new Integer(date));

			if (Demand != null)
			{
				double d = Demand.doubleValue();
				demand = demand + d;
			}
			history.put(new Integer(date), new Double(demand));
		}

		public void minus(int date, double demand) {

			Double Demand = (Double) history.get(new Integer(date));

			if (Demand != null)
			{
				double d = Demand.doubleValue();
				demand = d - demand;
			} else {
				demand = -1*demand;
			}
		
			history.put(new Integer(date), new Double(demand));
		}

		public double averagePast(int timeWindow, int today) {

			double utdDemand = getUptoDateDemand(today);
			double avg =0;
			int currentDay = today;
			int index = 0;

			double [] t = new double[timeWindow];
			
			for (int i=0;i<timeWindow;i++) {	t[i]=0;	  }

			t[index] = utdDemand;

			while (startDay <= currentDay-index && timeWindow > index)
			{
			 	double td = t[index] - getDemandOnDay(currentDay-index);
				index++;
				if (timeWindow > index)
				{
					t[index] = td;
				}
			}
			
			for (int i=0;i<index;i++) {	avg = avg + t[i];	  }
			if (index == 0)
			{
				return 0;
			}
			return avg/index;
		}

		public HashMap getHistory()		{	return history; }
		public int getDemandDay()		{	return demandDay; }
		
		public double getUptoDateDemand(int today)	{	
			
			double qty=0;
			int toDay = 0;

			if (today > demandDay)
			{
				toDay = demandDay;
			} else {
				toDay = today;
			}

			for (int i=startDay;i<=toDay;i++ )
			{
				Double demand = (Double) history.get(new Integer(i));
				if (demand !=null)	{				
					qty = qty + demand.doubleValue();
				} 
			}

			return qty; 
		}

		public double getRecentOne()	{	return ((Double)history.get(new Integer(lastDay))).doubleValue(); }

		public double getDemandOnDay(int Day) {	
			
			Double d = (Double)history.get(new Integer(Day));
			if (d==null)	{			return -1;			}
			return d.doubleValue(); 
		}
	};

	class HistoryList
	{
		int currentDate = -1;
		
		HashMap historyList = null; // This ArrayList will contain a list of supply demand history of a specific future day.
		HashMap addedUidList = null;
		HashMap removedUidList = null;
		LoggingService myLoggingService = null;

		public HistoryList(LoggingService myLoggingService) { 
			historyList = new HashMap(); 
			this.myLoggingService = myLoggingService;
		} 
		
		// Set current date and delete history data of the dates which are past.
		public void setCurrentDate(int currentDate) {  
			if (this.currentDate == currentDate)	{		return; 		}
			addedUidList = new HashMap();
			removedUidList = new HashMap();
			this.currentDate = currentDate;
		}
		
		// I assume that currentDate is already set. All the data which is put here are currentDate's data.
		public boolean add(UID uid, int futureDate, double demand) {
			
			if (addedUidList.get(uid)!= null)	{ return false; }
			addedUidList.put(uid, new String("exist"));

			History h = (History) historyList.get(new Integer(futureDate));

			if (h==null)
			{
				h = new History(futureDate);  // history for this futureDate.
				myLoggingService.shout("add new History object for " + futureDate +","+demand); 
			}	

			h.add(currentDate,demand);			
			historyList.put(new Integer(futureDate), h);
//			myLoggingService.shout("historyList size = " + historyList.size()); 
			return true;
		}

		public boolean remove(UID uid, int futureDate, double demand) {
			
			if (removedUidList.get(uid)!= null)	{ return false; }
			removedUidList.put(uid, new String("exist"));

			History h = (History) historyList.get(new Integer(futureDate));

			if (h==null)
			{
				myLoggingService.shout("This task can not be removedadd " +uid + "," + futureDate +","+demand); 
				return false;
			} else {
				h.minus(currentDate,demand);			
			}
			historyList.put(new Integer(futureDate), h);
//			myLoggingService.shout("historyList size = " + historyList.size()); 
			return true;
		}

		public History get(int date) {
			return ((History) historyList.get(new Integer(date)));
		}
	};

	   
	class ForecastRecord
	{
		public int ForecastedDay = -1, ForecastedAtDay=-1;
		public double ForecastedValue = 0;

		public ForecastRecord () { }
	};

    public void setupSubscriptions() {

		cluster = ((AgentIdentificationService) getBindingSite().getServiceBroker().getService(this, AgentIdentificationService.class, null)).getName();

        myBS = getBlackboardService();
        myDomainService = (DomainService) getBindingSite().getServiceBroker().getService(this, DomainService.class, null);
        myLoggingService = (LoggingService) getBindingSite().getServiceBroker().getService(this, LoggingService.class, null);
        myUIDService = (UIDService) getBindingSite().getServiceBroker().getService(this, UIDService.class, null);
        servletSubscription = (IncrementalSubscription) myBS.subscribe(servletPredicate);
        as = (AlarmService) getBindingSite().getServiceBroker().getService(this, AlarmService.class, null);

		if (selectedPredictor == KalmanFilter) {
            taskSubscription = (IncrementalSubscription) myBS.subscribe(taskPredicate);
            arrayListSubscription = (IncrementalSubscription) myBS.subscribe(arrayListPredicate);
            if (flagger == false) {
                if (!taskSubscription.isEmpty())	{            taskSubscription.clear();				}
                if (!servletSubscription.isEmpty()) {            servletSubscription.clear();			}
                flagger = true;
            }
            if (myBS.didRehydrate() == false)		{              getActualDemand();					}
        } else {
			
			if (!(cluster.equalsIgnoreCase("123-MSB") ||  cluster.equalsIgnoreCase("47-FSB")))	{	return; 	}
			relationSubscription = (IncrementalSubscription) myBS.subscribe(relationPredicate);
            taskSubscription = (IncrementalSubscription) myBS.subscribe(supplyTaskPredicate);
			historySubscription = (IncrementalSubscription) myBS.subscribe(historyPredicate);

			String dir = System.getProperty("org.cougaar.workspace");
            // Result file
            try {
                rst = new java.io.BufferedWriter(new java.io.FileWriter(dir+"/"+ cluster + System.currentTimeMillis() + ".pred.txt", true));
            } catch (java.io.IOException ioexc) {
                //System.err.println("can't write file, io error");
                myLoggingService.error("can't write file, io error");
            }
			
			if (selectedPredictor == SupportVectorMachine)
			{
               svmResult = new SvmResult();
               ConfigFinder finder = getConfigFinder();
               String inputName = "Training.svm.data.txt.svm";
			   
	           try {
			   
                   File paramFile = finder.locateFile("param.dat");
                   if (paramFile != null && paramFile.exists()) {  svmResult.readParam(paramFile);					} 
					else										 {  myLoggingService.shout("Param model error.");   }
			   
                   if (inputName != null && finder != null) {
                       File inputFile = finder.locateFile(inputName);
                       if (inputFile != null && inputFile.exists()) {  svmResult.readModel(inputFile);					} 
						else {											myLoggingService.shout("Input model error.");	}
                   }
			   
               } catch (Exception e) {
                   e.printStackTrace();
               }
			}

	        if (myBS.didRehydrate() == false) {
	            ammoHistory = new HashMap();		ammoHistory.put("class","Ammo");
	            bulkPOLHistory = new HashMap();		bulkPOLHistory.put("class","Bulk");
	        }
        }

        myLoggingService.shout("PredictorPlugin start at " + cluster);
        myBS.setShouldBePersisted(false);
    }

	public void callPredictor() {

		if (selectedPredictor == KalmanFilter) {
	         whilePredictorON();
		} else {
			if (!(cluster.equalsIgnoreCase("123-MSB") ||  cluster.equalsIgnoreCase("47-FSB")))	{	return; 	}
			predictNextDemand10_4();
		}
	}

    public void execute() {
		
		InterAgentCondition tr;

        for (Enumeration ent = servletSubscription.getAddedList(); ent.hasMoreElements();) {
            tr = (InterAgentCondition) ent.nextElement();
            String content = tr.getContent().toString();
			setAlgorithmSelection(content);
            String f = null;
            if (content.equalsIgnoreCase("Servlet_Relay = 0") == true) {
                f = "OFF";
            } else if (content.equalsIgnoreCase("Servlet_Relay = 1") == true) {
                f = "SLEEP";
            } else if (content.equalsIgnoreCase("Servlet_Relay = 2") == true) {
                f = "ON";
            }
            if (f != null && f == "OFF") {
                relay_added = true;
                myBS.publishChange(tr);
                break;
            } else if (f != null && f == "SLEEP") {
                relay_added = false;
                myBS.publishChange(tr);
                break;
            } else if (f != null && f == "ON") {

                count++;
                if ((Math.IEEEremainder(count, 2.0) != 0)) {
                    relay_added = true;
                    alarm = new TriggerFlushAlarm(currentTimeMillis());
                    as.addAlarm(alarm);
                    myBS.publishChange(tr);
                } else {
                    relay_added = false;
                    myBS.publishChange(tr);
                }

                break;
            } else
                return;
        }

        if (selectedPredictor == KalmanFilter) {

            for (Enumeration et = arrayListSubscription.getAddedList(); et.hasMoreElements();) {
                arraylist = (PredictorArrayList) et.nextElement();
                if (arraylist != null) {
                    myLoggingService.shout("Demand Model Received by agent " + cluster);
                    kf = new KalmanFilter(arraylist);
                    myBS.publishAdd(kf);
                    flag = true;
                }
            }
            if (flag == true && !relay_added == true) {
                getActualDemand();
            } else {
                return;
            }
        } else {

			if (!(cluster.equalsIgnoreCase("123-MSB") ||  cluster.equalsIgnoreCase("47-FSB")))	{	return; 	}

			if (ammoHistory == null && bulkPOLHistory == null )
			{
				for (Iterator iter = historySubscription.getAddedCollection().iterator() ; iter.hasNext() ;)
	            {
			        HashMap hashMap = (HashMap) iter.next();
					if (((String) hashMap.get("class")).equalsIgnoreCase("Ammo"))
					{
						ammoHistory = new HashMap(hashMap);
					} else if (((String) hashMap.get("class")).equalsIgnoreCase("Bulk")) {
						bulkPOLHistory = new HashMap(hashMap);
					}
					break;
				}
			}
			
			for (Enumeration et = relationSubscription.getChangedList(); et.hasMoreElements();) {
                HasRelationships org = (HasRelationships) et.nextElement();
                RelationshipSchedule schedule = org.getRelationshipSchedule();

                Collection ammo_customer = schedule.getMatchingRelationships(Constants.Role.AMMUNITIONCUSTOMER); //Get a collection of ammunition customers
                //Collection food_customer = schedule.getMatchingRelationships(Constants.Role.FOODCUSTOMER);
                Collection fuel_customer = schedule.getMatchingRelationships(Constants.Role.FUELSUPPLYCUSTOMER);
                //Collection packpol_customer = schedule.getMatchingRelationships(Constants.Role.PACKAGEDPOLSUPPLYCUSTOMER);
                //Collection spareparts_customer = schedule.getMatchingRelationships(Constants.Role.SPAREPARTSCUSTOMER);
                //Collection subsistence_customer = schedule.getMatchingRelationships(Constants.Role.SUBSISTENCESUPPLYCUSTOMER);

                for (Iterator iter = ammo_customer.iterator(); iter.hasNext();) {
                    Relationship orgname = (Relationship) iter.next();
                    Asset subOrg = (Asset) schedule.getOther(orgname);
                    String role = schedule.getOtherRole(orgname).getName();
                    String org_name = subOrg.getClusterPG().getMessageAddress().toString();
                    if (ammoHistory.get(org_name) == null) {
                        myLoggingService.shout("@@@@@@@ predictorPlugin Supplier : " + cluster + ", Customer: " + org_name + ", Role " + role);
                        ammoHistory.put(org_name, new HistoryList(myLoggingService));
						if (!ammoCustomers.contains(org_name)) {	ammoCustomers.add(org_name); 	}
                    }
                }

                for (Iterator iter = fuel_customer.iterator(); iter.hasNext();) {
                    Relationship orgname = (Relationship) iter.next();
                    Asset subOrg = (Asset) schedule.getOther(orgname);
                    String role = schedule.getOtherRole(orgname).getName();
                    String org_name = subOrg.getClusterPG().getMessageAddress().toString();
                    if (bulkPOLHistory.get(org_name) == null) {
                        myLoggingService.shout("@@@@@@@ predictorPlugin Supplier : " + cluster + ", Customer: " + org_name + ", Role " + role);
                        bulkPOLHistory.put(org_name, new HistoryList(myLoggingService));
						if (!bulkPolCustomers.contains(org_name)) {	bulkPolCustomers.add(org_name); 	}
                    }
                }
            }
//////	Temporarily Closed
//            if (!relay_added == true) {       checkTaskSubscription();           }
//////  Temproraily Used
			boolean checkTask = checkTaskSubscription();
//			myLoggingService.shout("checkTaskSubscription()="+checkTask);
			if (today > 0 && checkTask)
			{
				callPredictor();
			}
//////
        }
    }

    public void getActualDemand() {
        Task task;
        for (Enumeration e = taskSubscription.getAddedList(); e.hasMoreElements();) {
            task = (Task) e.nextElement();
            if (task != null) {
                // String owner = task.getUID().getOwner();
                String verb = task.getVerb().toString();
                if (verb != null) {
                    if (verb.equalsIgnoreCase("Supply") == true) {
                        String owner = (String) task.getPrepositionalPhrase("For").getIndirectObject();
                        if (owner != null) {
                            String pol = (String) task.getPrepositionalPhrase("OfType").getIndirectObject();
                            if (owner.equalsIgnoreCase(cluster) == false) {
                                String comp = stringManipulation(pol);
                                if (comp != null) {
                                    Asset as = task.getDirectObject();
                                    if(as!= null) {
                                        String item_name = as.getTypeIdentificationPG().getNomenclature();
                                    //myLoggingService.shout("D");
                                    //long ti = (currentTimeMillis() / 86400000) - 13005;
                                    long ti = currentTimeMillis() / 86400000;
                                    if (ti >= 0) {
                                        if (toggle == false) {
                                            x = ti;
                                            toggle = true;
                                        }
                                        //long sTime = (long) ((task.getPreferredValue(AspectType.END_TIME)) / 86400000) - 13005;
                                        long sTime = (long) (task.getPreferredValue(AspectType.END_TIME)) / 86400000;
                                        double qty = task.getPreferredValue(AspectType.QUANTITY);
                                        if (ti != -1 && qty != -1) {
                                            if (ti == x) {
                                                sd.getSupplyQuantity(cluster, owner, comp, ti, sTime, qty, item_name);
                                            } else if (ti > x) {
                                                ArrayList total_qty_alist = sd.returnDemandQuantity(cluster, owner, comp, ti, sTime, qty, item_name);
                                                if (total_qty_alist != null) {
                                                    counter++;
                                                    if (counter > 1) {
                                                        kf.measurementUpdate(total_qty_alist);
                                                        ArrayList prediction_arraylist = kf.timeUpdate(total_qty_alist);
                                                        myBS.publishChange(kf);
                                                        myBS.publishChange(prediction_arraylist);
                                                        for (int m = 0; m < prediction_arraylist.size(); m++) {
                                                            Vector v = ((Vector) prediction_arraylist.get(m));
                                                            if (v != null) {
                                                                NewTask nt1 = getNewTask1(v);
																
                                                                myBS.publishAdd(nt1);
																//System.out.println("b"+flag);
																myLoggingService.shout("Task added "+cluster);
																disposition(nt1);
																//System.out.println("a"+flag1);
																myLoggingService.shout("Task disposed "+cluster);
                                                            }
                                                        }
                                                        x = ti;
                                                    } else {
                                                        ArrayList prediction_arraylist = kf.timeUpdate(total_qty_alist);
                                                        myBS.publishChange(kf);
                                                        myBS.publishAdd(prediction_arraylist);
                                                        for (int m = 0; m < prediction_arraylist.size(); m++) {
                                                            Vector v = ((Vector) prediction_arraylist.get(m));
                                                            if (v != null) {
                                                                NewTask nt1 = getNewTask1(v);
                                                                myBS.publishAdd(nt1);
																myLoggingService.shout("Task1 added "+cluster);
																disposition(nt1);
															
																myLoggingService.shout("Task1 disposed "+cluster);
                                                            }
                                                        }
                                                        x = ti;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean checkTaskSubscription() {
        boolean updated = false;
        if (!taskSubscription.isEmpty()) {

			Collection c1 = taskSubscription.getAddedCollection();
			Collection c2 = taskSubscription.getRemovedCollection();
			updated = updateHistory(c1, c2, currentTimeMillis());
			/// find actual demand on the day which is today + 4 and calculate difference between actual value and forecasted value
			if (updated)
			{
				getActualDemand(today+4);
			}
			///
        }
        return updated;
    }

	private void getActualDemand(int day) {

		for (Enumeration e = ammoCustomers.elements() ; e.hasMoreElements() ;) {
			String AnAmmoCustomer = (String) e.nextElement();
            HistoryList historyList = (HistoryList) ammoHistory.get(AnAmmoCustomer);
			printActualDemandOf(historyList, AnAmmoCustomer, "Ammunition", day);
            myLoggingService.shout("\n");
        }

		for (Enumeration e = bulkPolCustomers.elements() ; e.hasMoreElements() ;) {
			String ABulkPolCustomer = (String) e.nextElement();
            HistoryList historyList = (HistoryList) bulkPOLHistory.get(ABulkPolCustomer);
 			printActualDemandOf(historyList, ABulkPolCustomer, "BulkPOL", day);
            myLoggingService.shout("\n");
        }
	}

    private void predictNextDemand10_4() // Cougaar 10.4
    {
        double[] aRow;
        int pF = 1; // period of future days to be forecasted.

		myLoggingService.shout("today = " + today + ", starting day = " + startingday);
		// set fore
//        if (cluster.equalsIgnoreCase("47-FSB"))			{           pF = 10;        } 
//		else if (cluster.equalsIgnoreCase("123-MSB"))	{           pF = 20;        }

//        try {
			for (Enumeration e = ammoCustomers.elements() ; e.hasMoreElements() ;) {
				String AnAmmoCustomer = (String) e.nextElement();
//				rst.write("ammo " + AnAmmoCustomer + "\n");
//				rst.flush();
                myLoggingService.shout("predict ammo " + AnAmmoCustomer + " @ " + today + " ");
                HistoryList historyList = (HistoryList) ammoHistory.get(AnAmmoCustomer);
                forecastDemand(pF, historyList, AnAmmoCustomer, "Ammunition");
//				dumpHistoryList(historyList, AnAmmoCustomer,"Ammo");
                myLoggingService.shout("\n");
            }

			for (Enumeration e = bulkPolCustomers.elements() ; e.hasMoreElements() ;) {
				String ABulkPolCustomer = (String) e.nextElement();
//                rst.write("bulk " + ABulkPolCustomer + "\n");
//				rst.flush();
                myLoggingService.shout("predict bulk " + ABulkPolCustomer + " @ " + today + " ");
                HistoryList historyList = (HistoryList) bulkPOLHistory.get(ABulkPolCustomer);
                forecastDemand(pF, historyList, ABulkPolCustomer, "BulkPOL");
//				dumpHistoryList(historyList, ABulkPolCustomer,"Bulk");
                myLoggingService.shout("\n");
            }
//        } catch (java.io.IOException ioexc) {
//            System.err.println("can't write prediction results, io error");
//        }
    }

	private void dumpHistoryList(HistoryList historyList, String customer, String ofType) {

		myLoggingService.shout("History of " + customer);

		for (int day = startingday; day <= today; day++)
		{
			History historyOfOnDemandDay = historyList.get(day);

            if (historyOfOnDemandDay != null) {

				int sDay = historyOfOnDemandDay.startDay;
				for (int j=sDay;j<=day;j++ )
				{
					double d =historyOfOnDemandDay.getDemandOnDay(j);
					if (d !=-1)
					{
						myLoggingService.shout("History : at day "+ j+" with "+ofType+ " demand = "+d+" for "+day +", today is "+ today);
					}
				}
            }
		}
	}
/*
	private void printActualDemand() {

		for (Enumeration e = ammoCustomers.elements() ; e.hasMoreElements() ;) {
			String AnAmmoCustomer = (String) e.nextElement();
            HistoryList historyList = (HistoryList) ammoHistory.get(AnAmmoCustomer);
			printActualDemandOf(historyList, AnAmmoCustomer, "Ammo", );
            myLoggingService.shout("\n");
        }

		for (Enumeration e = bulkPolCustomers.elements() ; e.hasMoreElements() ;) {
			String ABulkPolCustomer = (String) e.nextElement();
            HistoryList historyList = (HistoryList) bulkPOLHistory.get(ABulkPolCustomer);
 			printActualDemandOf(historyList, ABulkPolCustomer, "Bulk");
            myLoggingService.shout("\n");
        }
	}		
*/
	private void printActualDemandOf(HistoryList historyList, String customer, String ofType, int day) {

//		myLoggingService.shout("Actual Demand of " + customer);

		History historyOfOnDemandDay = historyList.get(day);  // 'day' is the target day.
		try
		{
            if (historyOfOnDemandDay != null) {

				double d =historyOfOnDemandDay.getUptoDateDemand(today);
				if (d !=-1)
				{
//					myLoggingService.shout("ActualDemand of " + customer +"'s " + ofType +"is "+d+" for "+(today+4) + " in " + today);
					
					ForecastRecord fr = null;
					if (ofType.equalsIgnoreCase("Ammunition"))
					{
						fr = (ForecastRecord) ammoForecastedData.get(new Integer(day-5));
					} else { 
						fr = (ForecastRecord) bulkForecastedData.get(new Integer(day-5));
					}

					int ForecastedDay = 0, ForecastedAtDay = 0;
					double ForecastedValue = 0; 

					if (fr != null)
					{
						ForecastedDay = fr.ForecastedDay;	ForecastedValue = fr.ForecastedValue;	ForecastedAtDay = fr.ForecastedAtDay; 
					} 

					rst.write("ActualDemand\t" + customer +"\t" + ofType +"\t"+d+"\t"+(today+4) + "\t" + today + 
							"\t" + ForecastedValue +"\t" + ForecastedDay +"\t"+ForecastedAtDay+"\t"+(ForecastedValue-d) + "\n");
					rst.flush();
					myLoggingService.shout("ActualDemand\t" + customer +"\t" + ofType +"\t"+d+"\t"+(today+4) + "\t" + today + 
							"\t" + ForecastedValue +"\t" + ForecastedDay +"\t"+ForecastedAtDay+"\t"+(ForecastedValue-d) + "\n");
				}
            }
		} catch (java.io.IOException ioexc) {
            //System.err.println("can't write prediction results, io error");
            myLoggingService.error("can't write prediction results, io error");
        }
	}

    private void forecastDemand(int timeHorizon, HistoryList historyList, String customer, String ofType) {

        double qty = 0;
        try {
            for (int day = today + 5; day < today + 5 + timeHorizon; day++) {
                History historyOfOnDemandDay = historyList.get(day);

                if (historyOfOnDemandDay == null) {
					double avg = 0;
					if (cluster.equalsIgnoreCase("47-FSB"))
					{
                        if (startingday < today) {
                            int fromTime = startingday;
                            // search at least past 10 days's demand.
                            if (today - startingday > 10) {
                                fromTime = today - 9;
                            }
							
                            for (int j = fromTime; j <= today; j++) {
                                History hT = historyList.get(j+4);
                                if (hT != null) {    avg = avg + hT.getUptoDateDemand(today);   }
                            }
                            avg = avg / (today - fromTime+1);
                        }
					}
					
                    myLoggingService.shout("forecast\t"+ofType+"\t" + day + "\t" + avg + "\t" + customer + "\t"+cluster+ "\t"+today +"\n");
                    rst.write("forecast\t"+ofType+"\t" + day + "\t" + avg + "\t" + customer + "\t"+cluster+ "\t"+today +"\n");
					rst.flush();
                    qty = avg;

                } else {
                    qty = historyOfOnDemandDay.averagePast(3, day);
                    myLoggingService.shout("forecast\t"+ofType+"\t" + day + "\t" + qty + "\t" + customer + "\t"+cluster+ "\t"+today +"\n");
                    rst.write("forecast\t"+ofType+"\t" + day + "\t" + qty + "\t" + customer + "\t"+cluster+ "\t"+today +"\n");
					rst.flush();
                }
		
				ForecastRecord fr = fr = new ForecastRecord();
				fr.ForecastedDay = day;		fr.ForecastedValue = qty;	fr.ForecastedAtDay = today; 

				HashMap tempHashMap = null;
				if (ofType.equalsIgnoreCase("Ammunition"))
				{   
					ammoForecastedData.put(new Integer(today), fr);
					tempHashMap = ammoForecastedData;
				} else {
					bulkForecastedData.put(new Integer(today), fr);
					tempHashMap = bulkForecastedData;
				}

				if (tempHashMap.containsKey(new Integer(today-2)))
				{
					tempHashMap.remove(new Integer(today-2));
				}

                NewTask new_task = getNewTask(ofType, customer, (long) day, qty, today);
                if (new_task != null) {
					
					myLoggingService.shout("publish + "+ ofType +" [" + day + "," + qty + "] in "+cluster);
                    

					if (ofType.equalsIgnoreCase("Ammunition"))		{		
						if (publishedTasksOfAmmo!=null)			{	// this should be a kind of vector if we publish more than one day forecast tasks
							myBS.publishRemove(publishedTasksOfAmmo);
						}
						publishedTasksOfAmmo = new_task;
					}	else {													
						if (publishedTasksOfBulk!=null)			{
							myBS.publishRemove(publishedTasksOfBulk);
						}
						publishedTasksOfBulk = new_task;
					}
					myBS.publishAdd(new_task);
                }

            }
        } catch (java.io.IOException ioexc) {
            //System.err.println("can't write prediction results, io error");
            myLoggingService.error("can't write prediction results, io error");
        }
    }

    public NewTask getNewTask(String ofType, String customer, long day, double qty, int Today) {

        PlanningFactory pf = (PlanningFactory) myDomainService.getFactory("planning");
        NewTask nt = pf.newTask();

        // Set verb
        Verb new_verb = new Verb("ForecastDemand");
        nt.setVerb(new_verb);

        NewPrepositionalPhrase npp = pf.newPrepositionalPhrase();
        npp.setPreposition(Constants.Preposition.FOR);
        npp.setIndirectObject(customer);
        nt.addPrepositionalPhrase(npp);

        // BulkPol or ammo
        NewPrepositionalPhrase npp1 = pf.newPrepositionalPhrase();
        npp1.setPreposition(Constants.Preposition.OFTYPE);
        npp1.setIndirectObject(ofType);
        nt.addPrepositionalPhrase(npp1);

        // Designate date in which these forecast tasks are generated.
        NewPrepositionalPhrase npp2 = pf.newPrepositionalPhrase();
        npp2.setPreposition("TODAY");
        Date date = new Date((long) Today * 86400000);
        npp2.setIndirectObject(date.toString());
        nt.addPrepositionalPhrase(npp2);

        AspectValue av = AspectValue.newAspectValue(AspectType.END_TIME, new Long(day * 86400000));
        Preference np = pf.newPreference(av.getAspectType(), ScoringFunction.createStrictlyAtValue(av));
        nt.addPreference(np);

        AspectValue av1 = AspectValue.newAspectValue(AspectType.QUANTITY, new Double(qty));
        Preference np1 = pf.newPreference(av1.getAspectType(), ScoringFunction.createStrictlyAtValue(av1));
        nt.addPreference(np1);

		return nt;

    }

	private boolean updateHistory(Collection addedSupplyTasks, Collection removedSupplyTasks, long nowTime)
	{
		int nTasks=0;
		Iterator taskIterator=null;
		boolean updated = false;

		today = (int) (nowTime/ 86400000);

		if (addedSupplyTasks !=null)
		{
			nTasks = addedSupplyTasks.size();
		    taskIterator = addedSupplyTasks.iterator();   

			for (int i = 0; i < nTasks; i++) {
		    
				Task ti = (Task)taskIterator.next();
			
				UID uid = ti.getUID();
				if (uid.getOwner().equalsIgnoreCase(cluster))	{ continue;		}
		    
				PrepositionalPhrase pp = ti.getPrepositionalPhrase("OfType");
				String oftype = null;
		    
				if (pp != null) {	oftype = (String) pp.getIndirectObject();
				} else { 
					myLoggingService.shout ("null Prepositional Phrase" );
					continue;
				}
		    
				PrepositionalPhrase pp2 = ti.getPrepositionalPhrase("Refill");
				if (pp2 == null) {	continue; } 
		    
				Verb v = ti.getVerb();
				
//				try
//				{
				String adj = "";
				HistoryList historyList=null;
		    
					if (oftype.equalsIgnoreCase("Ammunition") && v.equals("Supply"))
					{
						historyList = (HistoryList) ammoHistory.get(uid.getOwner());
						if (historyList == null) { 
							myLoggingService.shout("@@@@@@@ "+ uid.getOwner()+"is not an Ammunition customer");
							continue;
						} 
						adj = "ammo";
					} 
					else if (oftype.equalsIgnoreCase("BulkPOL") && v.equals("Supply"))
					{
						historyList = (HistoryList) bulkPOLHistory.get(uid.getOwner());
						if (historyList == null) { 
							myLoggingService.shout("@@@@@@@ "+ uid.getOwner()+"is not an BulkPOL customer");
							continue;
						} 
						adj = "bulk";
					}
		    
					int end_time = (int) (ti.getPreferredValue(AspectType.END_TIME) / 86400000) ;
					
					if (startingday > end_time ) 	{	
						startingday = end_time;	
						myLoggingService.shout("@@@@@@@@@ the first day of supply task is "+startingday);
					}
		    
					double qty = ti.getPreferredValue(AspectType.QUANTITY);
					historyList.setCurrentDate(today);
		    
					if (historyList.add(uid,end_time,qty))
					{
//						myLoggingService.shout("@@@@@@@ "+adj+ " add "+ uid.getOwner()+"["+ today+","+end_time+","+qty +"] in "+cluster);
						updated = true;
					} 
//				}
//				catch (java.io.IOException ioexc)
//				{
//					System.err.println ("can't write file, io error" );
//			    }		
			} // for
		}

		if (removedSupplyTasks !=null)
		{
			nTasks = removedSupplyTasks.size();
		    taskIterator = removedSupplyTasks.iterator();   

			for (int i = 0; i < nTasks; i++) {
		    
				Task ti = (Task)taskIterator.next();
			
				UID uid = ti.getUID();
				if (uid.getOwner().equalsIgnoreCase(cluster))	{ continue;		}
		    
				PrepositionalPhrase pp = ti.getPrepositionalPhrase("OfType");
				String oftype = null;
		    
				if (pp != null) {	oftype = (String) pp.getIndirectObject();
				} else { 
					myLoggingService.shout ("null Prepositional Phrase" );
					continue;
				}
		    
				PrepositionalPhrase pp2 = ti.getPrepositionalPhrase("Refill");
				if (pp2 == null) {	continue; } 
		    
				Verb v = ti.getVerb();
				
//				try
//				{
				String adj = "";
				HistoryList historyList=null;
		    
					if (oftype.equalsIgnoreCase("Ammunition") && v.equals("Supply"))
					{
						historyList = (HistoryList) ammoHistory.get(uid.getOwner());
						if (historyList == null) { 
							myLoggingService.shout("@@@@@@@ "+ uid.getOwner()+"is not an Ammunition customer");
							continue;
						} 
						adj = "ammo";
					} 
					else if (oftype.equalsIgnoreCase("BulkPOL") && v.equals("Supply"))
					{
						historyList = (HistoryList) bulkPOLHistory.get(uid.getOwner());
						if (historyList == null) { 
							myLoggingService.shout("@@@@@@@ "+ uid.getOwner()+"is not an BulkPOL customer");
							continue;
						} 
						adj = "bulk";
					}
		    
					int end_time = (int) (ti.getPreferredValue(AspectType.END_TIME) / 86400000);
					
//					if (startingday > end_time ) 	{	
//						startingday = end_time;	
//						myLoggingService.shout("@@@@@@@@@ the first day of supply task is "+startingday);
//					}
		    
					double qty = ti.getPreferredValue(AspectType.QUANTITY);
					historyList.setCurrentDate(today);
		    
					if (historyList.remove(uid,end_time,qty))
					{
//						myLoggingService.shout("@@@@@@@ "+adj+ " remove "+ uid.getOwner()+"["+ today+","+end_time+","+qty +"] in "+cluster);
						updated = true;
					} 
//				}
//				catch (java.io.IOException ioexc)
//				{
//					System.err.println ("can't write file, io error" );
//			    }		
			} // for
		}
/* DEBUG */
		// Dump History
/*
			for (Enumeration e = ammoCustomers.elements() ; e.hasMoreElements() ;) {
				String AnAmmoCustomer = (String) e.nextElement();
                HistoryList historyList = (HistoryList) ammoHistory.get(AnAmmoCustomer);
				dumpHistoryList(historyList, AnAmmoCustomer);
                myLoggingService.shout("\n");
            }

			for (Enumeration e = bulkPolCustomers.elements() ; e.hasMoreElements() ;) {
				String ABulkPolCustomer = (String) e.nextElement();
                HistoryList historyList = (HistoryList) bulkPOLHistory.get(ABulkPolCustomer);
 				dumpHistoryList(historyList, ABulkPolCustomer);
                myLoggingService.shout("\n");
            }
*/
/* DEBUG */
		return updated;
	}


    public String stringManipulation(String a) {

        String s_class = a;
        if (s_class.compareToIgnoreCase("Ammunition") == 0 || s_class.compareToIgnoreCase("Food") == 0) {
            String s_class1 = s_class.concat("Customer");
            return s_class1;
        }
        if (s_class.compareToIgnoreCase("PackagedPol") == 0 || s_class.compareToIgnoreCase("Subsistence") == 0) {
            String s_class1 = s_class.concat("SupplyCustomer");
            return s_class1;
        }
        if (s_class.compareToIgnoreCase("BulkPol") == 0) {
            String s_class1 = "FuelSupplyCustomer";
            return s_class1;
        }
        /*	if(s_class.compareToIgnoreCase("Consumable")==0)
            {
                    String s_class1 = "SparePartsCustomer";
                    return s_class1;
            }*/

        return null;
    }

    public String stringReverseManipulation(String a) {

        String s_class = a;
        if (s_class.compareToIgnoreCase("AmmunitionCustomer") == 0) {
            String s_class1 = "Ammunition";
            return s_class1;
        }
        if (s_class.compareToIgnoreCase("PackagedPolSupplyCustomer") == 0) {
            String s_class1 = "PackagedPol";
            return s_class1;
        }
        if (s_class.compareToIgnoreCase("SubsistenceSupplyCustomer") == 0) {
            String s_class1 = "Subsistence";
            return s_class1;
        }
        if (s_class.compareToIgnoreCase("FuelSupplyCustomer") == 0) {
            String s_class1 = "BulkPOL";
            return s_class1;
        }
        /*	if(s_class.compareToIgnoreCase("SparePartsCustomer")==0)
            {
                    String s_class1 = "Consumable";
                    return s_class1;
            }*/

        return null;
    }

    public void whilePredictorON() {
        if (relay_added == true) {
            myLoggingService.shout("Size " + arraylist.size() + " cluster " + cluster);
            for (int i = 0; i < arraylist.size(); i++) {
                Hashtable new_hash = (Hashtable) arraylist.get(i);
                //long current_day = (currentTimeMillis() / 86400000) - 13005;
                long current_day = (currentTimeMillis() / 86400000) + 4;
                //myLoggingService.shout(" Current Day " + current_day + " cluster " + cluster);
                for (int j = 1; j <= new_hash.size(); j++) {
                    Vector vt = (Vector) new_hash.get(new Integer(j));
                    String customer = vt.elementAt(1).toString();
                    String supply_class = vt.elementAt(2).toString();
                    String item_class = vt.elementAt(5).toString();
                    long hash_day = new Long(vt.elementAt(3).toString()).longValue();
                    //myLoggingService.shout(" Supplier " + cluster + " Customer " + customer + " Supply Class " + supply_class + "Item "+item_class+ " Hash_day " + hash_day);
                    if (current_day == hash_day) {
                         double pre_qty = 0;
                         if (j == 1) {
                             pre_qty = 0;
                         } else {
                             Vector vtb = (Vector) new_hash.get(new Integer(j - 1));
                             pre_qty = new Double(vtb.elementAt(4).toString()).doubleValue();
                         }
                        //Vector vt1 = (Vector) new_hash.get(new Integer(j - 1));
                        Vector vt1 = (Vector) new_hash.get(new Integer(j + 1));
                        //double pred_qty = new Double(vt1.elementAt(4).toString()).doubleValue();
                        double prev_qty = new Double(vt.elementAt(4).toString()).doubleValue();
                        double avg_qty = (0.8 * prev_qty) + (0.2 * pre_qty);
                        myLoggingService.shout("Supplier: " + cluster + " Customer: " + customer +
                                " Supply Class " + supply_class + " Prediction for Day " + (current_day + 1) + " Quantity is " + avg_qty);
                        try {
                            pr = new PrintWriter(new BufferedWriter(new FileWriter(cluster + "predict.txt", true)));
                            pr.print(cluster);
                            pr.print(",");
                            pr.print(customer);
                            pr.print(",");
                            pr.print(supply_class);
                            pr.print(",");
                            pr.print(current_day + 1);
                            //pr.print(current_day + 86400000);
                            pr.print(",");
                            pr.print(avg_qty);
                            pr.println();
                            pr.close();
                        } catch (Exception e) {
                            System.err.println(e);
                            //myLoggingService.error(e);
                        }
                        vt1.removeElementAt(4);
                        vt1.insertElementAt(new Double(avg_qty), 4);
                        if (vt1 != null) {
                            NewTask new_task = getNewTask(vt1);
                            if (new_task != null) {
                                myBS.publishAdd(new_task);
                                disposition(new_task);
                                //myLoggingService.shout("New Task ADDED");
                            }
                        }
                        break;
                    } else {
                        continue;
                    }
                }
            }

            alarm = new TriggerFlushAlarm(currentTimeMillis() + 86400000);
            as.addAlarm(alarm);
            //myLoggingService.shout("Alarm");
        } else
            return;
    }

    public NewTask getNewTask(Vector v) {
        //PlanningFactory pf = ((PredictorFactory) myDomainService.getFactory(new PredictorDomain().getDomainName())).getPlanningFactory();
        PlanningFactory pf = (PlanningFactory) myDomainService.getFactory("planning");
        NewTask nt = pf.newTask();

        //Verb new_verb = new Verb("ForecastDemand");
        Verb new_verb = new Verb("ForecastDemand");
        nt.setVerb(new_verb);

        NewPrepositionalPhrase npp = pf.newPrepositionalPhrase();
        npp.setPreposition(Constants.Preposition.FOR);
        npp.setIndirectObject(v.elementAt(1));
        nt.addPrepositionalPhrase(npp);

        NewPrepositionalPhrase npp1 = pf.newPrepositionalPhrase();
        npp1.setPreposition(Constants.Preposition.OFTYPE);
        String supplyclass = stringReverseManipulation(v.elementAt(2).toString());
        npp1.setIndirectObject(supplyclass);
        nt.addPrepositionalPhrase(npp1);

        AspectValue av = AspectValue.newAspectValue(AspectType.END_TIME, new Long(v.elementAt(3).toString()).longValue() * 86400000);
        Preference np = pf.newPreference(av.getAspectType(), ScoringFunction.createStrictlyAtValue(av));
        nt.addPreference(np);

        AspectValue av1 = AspectValue.newAspectValue(AspectType.QUANTITY, new Double(v.elementAt(4).toString()).doubleValue());
        Preference np1 = pf.newPreference(av1.getAspectType(), ScoringFunction.createStrictlyAtValue(av1));
        nt.addPreference(np1);
        
        Asset as = new Asset();
        NewSupplyClassPG pg = (NewSupplyClassPG) pf.createPropertyGroup(SupplyClassPG.class);
        pg.setSupplyClass(supplyclass);
        pg.setSupplyType(supplyclass);
        as.setPropertyGroup(pg);

        ldms = (LDMService) getBindingSite().getServiceBroker().getService(this, LDMService.class,
                new ServiceRevokedListener() {
                    public void serviceRevoked(ServiceRevokedEvent re) {
                        if(LDMService.class.equals(re.getService()))
                            ldms = null;
                    }
                    });

        as.registerWithLDM(ldms.getLDM());
        getBindingSite().getServiceBroker().releaseService(this, LDMService.class, ldms);

        UID uid = as.getUID();
        long id = uid.getId();
        UID newuid = new UID(v.elementAt(1).toString(),id);
        as.setUID(newuid);

  		NewTypeIdentificationPG ntipg = (NewTypeIdentificationPG) as.getTypeIdentificationPG();
        ntipg.setNomenclature(v.elementAt(5).toString());

        nt.setDirectObject(as);

		return nt;

    }


    public NewTask getNewTask1(Vector v) {
        //PlanningFactory pf = ((PredictorFactory) myDomainService.getFactory(new PredictorDomain().getDomainName())).getPlanningFactory();
        PlanningFactory pf = (PlanningFactory) myDomainService.getFactory("planning");
        NewTask nt = pf.newTask();

        Verb new_verb = new Verb("ForecastDemand");
        nt.setVerb(new_verb);

        NewPrepositionalPhrase npp = pf.newPrepositionalPhrase();
        npp.setPreposition(Constants.Preposition.FOR);
        npp.setIndirectObject(v.elementAt(1));
        nt.addPrepositionalPhrase(npp);

        NewPrepositionalPhrase npp1 = pf.newPrepositionalPhrase();
        npp1.setPreposition(Constants.Preposition.OFTYPE);
        String supplyclass = stringReverseManipulation(v.elementAt(2).toString());
        npp1.setIndirectObject(supplyclass);
        nt.addPrepositionalPhrase(npp1);

        AspectValue av = AspectValue.newAspectValue(AspectType.END_TIME, new Long(v.elementAt(3).toString()).longValue() * 86400000);
        Preference np = pf.newPreference(av.getAspectType(), ScoringFunction.createStrictlyAtValue(av));
        nt.addPreference(np);

        AspectValue av1 = AspectValue.newAspectValue(AspectType.QUANTITY, new Double(v.elementAt(5).toString()).doubleValue());
        Preference np1 = pf.newPreference(av1.getAspectType(), ScoringFunction.createStrictlyAtValue(av1));
        nt.addPreference(np1);

        Asset as = new Asset();
        NewSupplyClassPG pg = (NewSupplyClassPG) pf.createPropertyGroup(SupplyClassPG.class);
        pg.setSupplyClass(supplyclass);
        pg.setSupplyType(supplyclass);
        as.setPropertyGroup(pg);

        ldms = (LDMService) getBindingSite().getServiceBroker().getService(this, LDMService.class,
                new ServiceRevokedListener() {
                    public void serviceRevoked(ServiceRevokedEvent re) {
                        if(LDMService.class.equals(re.getService()))
                            ldms = null;
                    }
                    });
                    
        as.registerWithLDM(ldms.getLDM());
        getBindingSite().getServiceBroker().releaseService(this, LDMService.class, ldms);

        UID uid = as.getUID();
        long id = uid.getId();
        UID newuid = new UID(v.elementAt(1).toString(),id);
        as.setUID(newuid);

  		NewTypeIdentificationPG ntipg = (NewTypeIdentificationPG) as.getTypeIdentificationPG();
        ntipg.setNomenclature(v.elementAt(6).toString());

        nt.setDirectObject(as);

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

    public void setAlgorithmSelection(String radio_selection) {
    String content1 = radio_selection;
      if(content1.equalsIgnoreCase("Algorithm_Relay = 1")==true){
         selectedPredictor = MovingAverage;
         myLoggingService.shout("Predictor with Moving Average");
      }
      if(content1.equalsIgnoreCase("Algorithm_Relay = 2")==true){
             selectedPredictor = SupportVectorMachine;
             myLoggingService.shout("Predictor with Support Vector Machine");
      }
      if(content1.equalsIgnoreCase("Algorithm_Relay = 3")==true){
           selectedPredictor = KalmanFilter;
           myLoggingService.shout("Predictor with Kalman Filter");
      }
    }

    private String cluster;
    private LoggingService myLoggingService;
    private DomainService myDomainService;
    LDMService ldms;
    UIDService myUIDService;
    private BlackboardService myBS;
    private IncrementalSubscription arrayListSubscription;
    private IncrementalSubscription taskSubscription;
    private IncrementalSubscription servletSubscription;
    private IncrementalSubscription relationSubscription;
    private IncrementalSubscription historySubscription;

    AlarmService as;
    TriggerFlushAlarm alarm = null;
    private ArrayList arraylist = new PredictorArrayList();
    SupplyDataUpdate sd = new SupplyDataUpdate();
    private KalmanFilter kf = null;
    private boolean relay_added = false;

    private boolean toggle = false;
    private long x = 0;
    private int counter = 0;
    private boolean flag = false;
    private boolean flagger = false;
    double count = 0;
    private PrintWriter pr;

    private final int MovingAverage = 1;
    private final int SupportVectorMachine = 2;
    private final int KalmanFilter = 3;
    private int selectedPredictor = KalmanFilter;
    private HashMap ammoHistory = null;
    private HashMap bulkPOLHistory = null;
    private Vector ammoCustomers = new Vector();
    private Vector bulkPolCustomers = new Vector();
//	private int ammoForecastedDay = -1, ammoForecastedAtDay=-1, bulkForecastedDay=-1, bulkForecastedAtDay=-1;
//	private double ammoForecastedValue = 0, bulkForecastedValue = 0;

	private HashMap ammoForecastedData = new HashMap();
	private HashMap bulkForecastedData = new HashMap();

	private NewTask publishedTasksOfAmmo = null;
	private NewTask publishedTasksOfBulk = null;

    boolean changed = false;
    int today = -1, startingday = 100000;

    long nextTime = 0;
//	long baseTime = 13005; // August 10th 2005, long baseTime = 12974; // July 10th 2005
    java.io.BufferedWriter rst = null;
    SvmResult svmResult = null;
}