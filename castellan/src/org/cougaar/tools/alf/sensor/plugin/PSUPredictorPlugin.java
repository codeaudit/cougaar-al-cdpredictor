package org.cougaar.tools.alf.sensor.plugin;

import org.cougaar.core.adaptivity.InterAgentCondition;
import org.cougaar.core.agent.service.alarm.PeriodicAlarm;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.plugin.ComponentPlugin;
import org.cougaar.core.service.*;
import org.cougaar.core.util.UID;
import org.cougaar.glm.ldm.Constants;
//import org.cougaar.logistics.servlet.CommStatus;
import org.cougaar.logistics.plugin.inventory.TaskUtils;
import org.cougaar.logistics.plugin.inventory.MaintainedItem;
import org.cougaar.planning.ldm.PlanningFactory;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.plan.*;
import org.cougaar.planning.service.LDMService;
import org.cougaar.util.ConfigFinder;
import org.cougaar.util.UnaryPredicate;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;

/** 
 *	programed by Yunho Hong
 *	June 10, 2003
 *	PSU-IAI
**/

public class PSUPredictorPlugin extends ComponentPlugin {

	boolean called = false;
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
/*
        public void expire() {
//            expired = true;
			reset(currentTimeMillis());
			myBS.signalClientActivity(); 
			myLoggingService.shout("CALL PREDICTOR IN PERIODICALARM AT "+currentTimeMillis()/86400000);	
			called = true;
//			myBS.openTransaction();
//			callPredictor();
//				myBS.closeTransaction();

//			cancel();
        }
*/
        public void expire() {
            expired = true;
			myBS.signalClientActivity(); 
			myLoggingService.shout("CALL PREDICTOR IN PERIODICALARM AT "+currentTimeMillis()/86400000);	
			called = true;
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
/*
     UnaryPredicate commstatusPredicate = new UnaryPredicate() {
        public boolean execute(Object o) {
            //myLoggingService.shout("Communication Object Detected");
            return o instanceof CommStatus;
        }
    };
*/
    UnaryPredicate relationPredicate = new UnaryPredicate() {
        public boolean execute(Object o) {
            if (o instanceof HasRelationships) {
                return ((HasRelationships) o).isLocal();
            } else {
                return false;
            }
        }
    };


////////

	UnaryPredicate historyPredicate	= new UnaryPredicate()	{ 
		public boolean execute(Object o) {  return o instanceof DemandHistoryManager;   }    
	};

	// I will delete this class   
	class ForecastRecord
	{
		public int ForecastedDay = -1, ForecastedAtDay=-1;
		public double ForecastedValue = 0;

		public ForecastRecord () { }
	};

////////


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
//            commstatusSubscription = (IncrementalSubscription) myBS.subscribe(commstatusPredicate);
            if (flagger == false) {
                if (!taskSubscription.isEmpty())	{            taskSubscription.clear();				}
                if (!servletSubscription.isEmpty()) {            servletSubscription.clear();			}
                flagger = true;
            }
            if (myBS.didRehydrate() == false)		{              getActualDemand();					}
        } else {

			////// PSU start
            taskSubscription	= (IncrementalSubscription) myBS.subscribe(taskPredicate);		//	<- This is Common with Himanshu.
			historySubscription = (IncrementalSubscription) myBS.subscribe(historyPredicate);	// 
			
//			openLoggingFile();	// private member function

	        if (myBS.didRehydrate() == false) {
				demandHistoryManager = new DemandHistoryManager(myLoggingService);
				predictorManager = new PredictorManager(cluster,myLoggingService,getConfigFinder(),this);
				predictorManager.setDemandHistoryManager(demandHistoryManager);
	        }
			////// PSU end
        }

        myLoggingService.shout("PSUPredictorPlugin start at " + cluster);
        myBS.setShouldBePersisted(false);

		alarm = new TriggerFlushAlarm(currentTimeMillis());
        as.addAlarm(alarm);
    }

	public void callPredictor() {

		if (selectedPredictor == KalmanFilter) {
	         whilePredictorON1();
		} else {
			////// PSU start			
			predictNextDemand();
			////// PSU end
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

//		alarm = new TriggerFlushAlarm(currentTimeMillis());
//        as.addAlarm(alarm);

//		alarm.reset(currentTimeMillis());

/***    Temporarilly closed for the purpose of testing
        for (Enumeration e = commstatusSubscription.getAddedList(); e.hasMoreElements();) {
            CommStatus cs = (CommStatus) e.nextElement();
            String customerAgentName = cs.getConnectedAgentName();
            status = cs.isCommUp();
            myLoggingService.shout("Communication status is: "+status);
            if(status == false){
                commLossTime = cs.getCommLossTime();
                myLoggingService.shout("Communication Lost with Customer: "+ customerAgentName);
                alarm = new TriggerFlushAlarm(currentTimeMillis());
                as.addAlarm(alarm);
                myLoggingService.shout("Comm. Loss Alarm Added");
                comm_count++;
            } else {
                if(comm_count==1){
                    commRestoreTime = cs.getCommRestoreTime();
                    myLoggingService.shout("Communication Re-Established with Customer: "+ customerAgentName);
                    comm_count = 0;
                }
            }
        }
****/
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
			////// PSU start	
			read_DemandHistoryManager_class_from_BB_if_it_is_null();  // for rehydration.
			boolean updated = updateDemandHistory();
//			callPredictor();

			if (Rantime != (long) currentTimeMillis() / 86400000)		{

				alarm = new TriggerFlushAlarm(currentTimeMillis()+86400000);
				as.addAlarm(alarm);

				Rantime = (long) currentTimeMillis() / 86400000;

			}

			if (called)
			{
				called = false;
				callPredictor();
			}						
			////// PSU end	
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
                                    	Hashtable asset_hashtable = storeAsset(as);
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
//                                                sd.getSupplyQuantity(cluster, owner, comp, ti, sTime, qty, item_name);
                                                sd.getSupplyQuantity(cluster, owner, comp, ti, sTime, qty);
                                            } else if (ti > x) {
//                                                ArrayList total_qty_alist = sd.returnDemandQuantity(cluster, owner, comp, ti, sTime, qty, item_name);
                                                ArrayList total_qty_alist = sd.returnDemandQuantity(cluster, owner, comp, ti, sTime, qty);
                                                local_alist = total_qty_alist;
                                                if(status == true)
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
                                                                myLoggingService.shout(cluster + ": Task added " + nt1);
 																disposition(nt1);
// 																//System.out.println("a"+flag1);
// 																myLoggingService.shout("Task disposed "+cluster);
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
                                                                myLoggingService.shout(cluster + ": Task1 added " + nt1);
 																disposition(nt1);
															
// 																myLoggingService.shout("Task1 disposed "+cluster);
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

	/// PSU
    private boolean updateDemandHistory() {

        boolean addedUpdated = false, removedUpdated = false;
        
		if (!taskSubscription.isEmpty()) {

			Collection c1 = taskSubscription.getAddedCollection();
			Collection c2 = taskSubscription.getRemovedCollection();
			long today = (long) currentTimeMillis()/86400000;
	
			addedUpdated	= updateDemandHistory(c1, true, today);	// true	 -> added
			removedUpdated	= updateDemandHistory(c2, false, today);	// false -> removed
        }

        return ( addedUpdated || removedUpdated );

    }

	/// PSU
	private boolean updateDemandHistory(Collection tasks, boolean action, long today)
	{
		Iterator taskIterator=null;
		boolean updated = false;

		if (tasks ==null) {  return false;	}

		for (Iterator iter = tasks.iterator(); iter.hasNext(); ) {
		
			Task ti = (Task)iter.next();
		
			if (!ti.getVerb().equals("Supply"))						{		continue;		}	// if it is not a supply task, skip.
			if (ti.getUID().getOwner().equalsIgnoreCase(cluster))	{		continue;		}	// if it is not a task from customer, skip.
		
			if(!updated) {		updated = true;		}

			if (action)	// true	 -> added
			{
				demandHistoryManager.addDemandData(ti,today);
			} else {
				demandHistoryManager.removeDemandData(ti);
			}
		}
	
		return updated;
	}

    public Hashtable storeAsset(Asset as){
       Asset as1 =as;
       String itemname = as1.getTypeIdentificationPG().getNomenclature();
       if(asset_list.isEmpty()){
           asset_list.put(new Integer(1),as1);
           return asset_list;
       } else {
       for(int i = 1; i<=asset_list.size(); i++){
           String test = ((Asset)asset_list.get(new Integer(i))).getTypeIdentificationPG().getNomenclature();
       if(test.equalsIgnoreCase(itemname)==false){
           asset_list.put(new Integer(asset_list.size()+1),as1);
           break;
       } else
           continue;
       }
           return asset_list;
       }
    }

    public Asset getAsset(Vector v){

       for(int i = 1; i<=asset_list.size();i++){
           Asset as2 = (Asset)asset_list.get(new Integer(i));
           String item = as2.getTypeIdentificationPG().getNomenclature();
           String sclas1 = stringReverseManipulation(v.elementAt(2).toString());
           String sclas = stringPlay(sclas1);
           //StringBuffer sbf = new StringBuffer().append("org.cougaar.glm.ldm.asset.").append(sclas);
           if(item.equalsIgnoreCase(v.elementAt(5).toString())==true) {
               return as2;
           } else
               continue;
       }
        return null;
    }

    public Asset getAsset1(Vector v){

       for(int i = 1; i<=asset_list.size();i++){
           Asset as2 = (Asset)asset_list.get(new Integer(i));
           String item = as2.getTypeIdentificationPG().getNomenclature();
           String sclas1 = stringReverseManipulation(v.elementAt(2).toString());
           String sclas = stringPlay(sclas1);
           //StringBuffer sbf = new StringBuffer().append("org.cougaar.glm.ldm.asset.").append(sclas);
           if(item.equalsIgnoreCase(v.elementAt(6).toString())==true) {
               return as2;
           } else
               continue;
       }
        return null;
    }

	/// PSU
	private void predictNextDemand()
    {
		long today = (long) currentTimeMillis()/86400000;

		// Depending on item, forecased time point and commitment time is different.
		// All the details are processed in this class.
        myLoggingService.shout("PREDICTORMANAGER.FORCAST AT " + today);
		predictorManager.forecast(today);
    }

/*
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
					//myLoggingService.shout("ActualDemand\t" + customer +"\t" + ofType +"\t"+d+"\t"+(today+4) + "\t" + today +
					//		"\t" + ForecastedValue +"\t" + ForecastedDay +"\t"+ForecastedAtDay+"\t"+(ForecastedValue-d) + "\n");
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
					
                    //myLoggingService.shout("forecast\t"+ofType+"\t" + day + "\t" + avg + "\t" + customer + "\t"+cluster+ "\t"+today +"\n");
                    rst.write("forecast\t"+ofType+"\t" + day + "\t" + avg + "\t" + customer + "\t"+cluster+ "\t"+today +"\n");
					rst.flush();
                    qty = avg;

                } else {
                    qty = historyOfOnDemandDay.averagePast(3, day);
                    //myLoggingService.shout("forecast\t"+ofType+"\t" + day + "\t" + qty + "\t" + customer + "\t"+cluster+ "\t"+today +"\n");
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
					
					//myLoggingService.shout(cluster + ": publish + "+ ofType +" [" + day + "," + qty + "] as " + new_task);
                    

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
*/

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

    public long getCustomerLeadTime(long lastSupplyDay, long commLossDay){
       clt = lastSupplyDay - commLossDay;
       return clt;
    }

    public long getPredictorGap(long lastSupplyDay, long clt){
        gap = currentTimeMillis() + clt - lastSupplyDay;
        return gap;
    }

    public long getOrderShipTime(long lastSupplyDay, long commitmentDay){
        ost = lastSupplyDay - commitmentDay;
        return ost;
    }

    public void setPredictionCommitmentDate(long lastSupplyDay, long ordershiptime){
        pcd = lastSupplyDay + gap - ordershiptime;
    }

     public long getPredictionCommitmentDate(){
        return pcd;
    }

    public void whilePredictorON() {
        if (relay_added == true) {  //here put the comm status instead of relay
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
                        if (vt1 != null) {
                        vt1.removeElementAt(4);
                        vt1.insertElementAt(new Double(avg_qty), 4);                    
                            NewTask new_task = getNewTask(vt1);
                            if (new_task != null) {
                                myBS.publishAdd(new_task);
//                                 disposition(new_task);
                                myLoggingService.shout(cluster + ": New Task ADDED "+ new_task);
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
        }
        else
            return;
    }


    public void whilePredictorON1() {
        if (status == false) {
            if(relay_added == false){
            ArrayList final_al = new ArrayList();
            if(!local_alist.isEmpty()) {
                myLoggingService.shout("loacl_alist size: "+local_alist.size());
                for(int al_iter = 0; al_iter < local_alist.size(); al_iter++){
                    Hashtable local_hash = (Hashtable) local_alist.get(al_iter);
                    String customer_copy = ((Vector)local_hash.get(new Integer(local_hash.size()))).elementAt(1).toString();
                    String supplyclass_copy = ((Vector)local_hash.get(new Integer(local_hash.size()))).elementAt(2).toString();
                    String itemname_copy = ((Vector)local_hash.get(new Integer(local_hash.size()))).elementAt(7).toString();
                    long last_day = new Long(((Vector)local_hash.get(new Integer(local_hash.size()))).elementAt(8).toString()).longValue();
                    long commit_day = new Long(((Vector)local_hash.get(new Integer(local_hash.size()))).elementAt(3).toString()).longValue();
                    long customer_lead_time = getCustomerLeadTime(last_day, commLossTime);
                    long pred_gap = getPredictorGap(last_day, customer_lead_time);
                    long order_ship_time = getOrderShipTime(last_day, commit_day);
                    setPredictionCommitmentDate(last_day, order_ship_time);
                    Vector commloss_vec = new Vector();
                    commloss_vec.insertElementAt(customer_copy, 0);
                    commloss_vec.insertElementAt(supplyclass_copy, 1);
                    commloss_vec.insertElementAt(itemname_copy, 2);
                    commloss_vec.insertElementAt(new Long(last_day), 3);
                    commloss_vec.insertElementAt(new Long(customer_lead_time), 4);
                    commloss_vec.insertElementAt(new Long(pred_gap), 5);
                    commloss_vec.insertElementAt(new Long(order_ship_time), 6);
                    commloss_vec.insertElementAt(new Long(getPredictionCommitmentDate()), 7);
                    final_al.add(al_iter,commloss_vec);
                    myLoggingService.shout("Comm. loss Predictor List Data Created");
                }
                long current_day = (currentTimeMillis() / 86400000);
                for(int al_iter1 = 0; al_iter1 < final_al.size(); al_iter1++) {
                    long commitmentday = new Long(((Vector)final_al.get(al_iter1)).elementAt(7).toString()).longValue();
                    if(current_day < (commitmentday/86400000)){
                        retainAL((Vector)final_al.get(al_iter1));
                    }   else if(current_day == (commitmentday/86400000)){
                          for (int i = 0; i < arraylist.size(); i++) {
                            Hashtable new_hash = (Hashtable) arraylist.get(i);
                            for (int j = 1; j <= new_hash.size(); j++) {
                                Vector vt = (Vector) new_hash.get(new Integer(j));
                                String customer = vt.elementAt(1).toString();
                                String supply_class = vt.elementAt(2).toString();
                                String item_class = vt.elementAt(5).toString();
                                if(customer.equalsIgnoreCase(((Vector)retain_alist.get(al_iter1)).elementAt(0).toString())==true &&
                                   supply_class.equalsIgnoreCase(((Vector)retain_alist.get(al_iter1)).elementAt(1).toString())==true &&
                                   item_class.equalsIgnoreCase(((Vector)retain_alist.get(al_iter1)).elementAt(2).toString())==true){
                                   long order_ship_time = new Long(((Vector)retain_alist.get(al_iter1)).elementAt(6).toString()).longValue();
                                   long supply_date = new Long(vt.elementAt(3).toString()).longValue();
                                   if(supply_date == (order_ship_time + commitmentday)) {
                                      NewTask new_task = getNewTask(vt);
                                      if (new_task != null) {
                                        myBS.publishAdd(new_task);
//                                 disposition(new_task);
                                        myLoggingService.shout(cluster + ": New Task ADDED "+ new_task);
                                        retain_alist.remove(al_iter1);
                                     }
                                       break;
                                   }   else     {
                                       continue;
                                   }
                                }
                                else
                                    break;
                            }
                             break;
                        }
                    }   else {
                        return;
                    }
                }
                if(retain_alist!= null){
                for(int al_iter2 = 0; al_iter2 < retain_alist.size(); al_iter2++){
                     long commitmentday = new Long(((Vector)retain_alist.get(al_iter2)).elementAt(7).toString()).longValue();
                     if(current_day < (commitmentday/86400000)){
                             continue;
                     }   else if(current_day == (commitmentday/86400000)) {
                             //execute predictor task
                         for (int i = 0; i < arraylist.size(); i++) {
                            Hashtable new_hash = (Hashtable) arraylist.get(i);
                            for (int j = 1; j <= new_hash.size(); j++) {
                                Vector vt = (Vector) new_hash.get(new Integer(j));
                                String customer = vt.elementAt(1).toString();
                                String supply_class = vt.elementAt(2).toString();
                                String item_class = vt.elementAt(5).toString();
                                if(customer.equalsIgnoreCase(((Vector)retain_alist.get(al_iter2)).elementAt(0).toString())==true &&
                                   supply_class.equalsIgnoreCase(((Vector)retain_alist.get(al_iter2)).elementAt(1).toString())==true &&
                                   item_class.equalsIgnoreCase(((Vector)retain_alist.get(al_iter2)).elementAt(2).toString())==true){
                                   long order_ship_time = new Long(((Vector)retain_alist.get(al_iter2)).elementAt(6).toString()).longValue();
                                   long supply_date = new Long(vt.elementAt(3).toString()).longValue();
                                   if(supply_date == (order_ship_time + commitmentday)) {
                                      NewTask new_task = getNewTask(vt);
                                      if (new_task != null) {
                                        myBS.publishAdd(new_task);
//                                 disposition(new_task);
                                        myLoggingService.shout(cluster + ": New Task ADDED "+ new_task);
                                        retain_alist.remove(al_iter2);
                                     }
                                       break;
                                   }   else     {
                                       continue;
                                   }
                                }
                                else
                                    break;
                            }
                             break;
                        }
                     }
                     else {
                         //delete the row
                         return;
                     }
                }
                    }

            alarm = new TriggerFlushAlarm(currentTimeMillis() + 86400000);
            as.addAlarm(alarm);
            //myLoggingService.shout("Alarm");
        }

        }
        } else
            return;
    }


    public void retainAL(Vector v_list)	{
        retain_alist.add(v_list);
    }

    public NewTask getNewTask(Vector v) {
        //PlanningFactory pf = ((PredictorFactory) myDomainService.getFactory(new PredictorDomain().getDomainName())).getPlanningFactory();
        PlanningFactory pf = (PlanningFactory) myDomainService.getFactory("planning");
        NewTask nt = pf.newTask();

        nt.setVerb(forecastVerb);
        //Verb verb = new Verb("Supply");
        //nt.setVerb(verb);

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
        
        String sclass = stringPlay(supplyclass);
         Asset asset = getAsset(v);
        /*Asset as = pf.createAsset(sclass);
        NewSupplyClassPG pg = (NewSupplyClassPG) pf.createPropertyGroup(SupplyClassPG.class);
        pg.setSupplyClass(sclass);
        pg.setSupplyType(supplyclass);
        as.setPropertyGroup(pg); */

        /*ldms = (LDMService) getBindingSite().getServiceBroker().getService(this, LDMService.class,
                new ServiceRevokedListener() {
                    public void serviceRevoked(ServiceRevokedEvent re) {
                        if(LDMService.class.equals(re.getService()))
                            ldms = null;
                    }
                    });

        as.registerWithLDM(ldms.getLDM());
        getBindingSite().getServiceBroker().releaseService(this, LDMService.class, ldms); */
        if(asset!= null){
        UID uid = asset.getUID();
            //System.out.println("uid is "+uid.toString()+ " cluster "+cluster);
        long id = uid.getId();
        UID newuid = new UID(v.elementAt(1).toString(),id);
            //System.out.println("Newuid is "+newuid.toString()+ " cluster "+cluster);
        asset.setUID(newuid);
             //System.out.println("Setuid is "+asset.getUID().toString()+ " cluster "+cluster);
  		//NewTypeIdentificationPG ntipg = (NewTypeIdentificationPG) as.getTypeIdentificationPG();
        //ntipg.setNomenclature(v.elementAt(6).toString());

        nt.setDirectObject(asset);
        }

		return nt;

    }


    public NewTask getNewTask1(Vector v) {
        //PlanningFactory pf = ((PredictorFactory) myDomainService.getFactory(new PredictorDomain().getDomainName())).getPlanningFactory();
        PlanningFactory pf = (PlanningFactory) myDomainService.getFactory("planning");
        NewTask nt = pf.newTask();

        nt.setVerb(forecastVerb);
        //Verb verb = new Verb("Supply");
        //nt.setVerb(verb);

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
        
        String sclass = stringPlay(supplyclass);
        Asset asset = getAsset1(v);
        /*Asset as = pf.createAsset(sclass);
        NewSupplyClassPG pg = (NewSupplyClassPG) pf.createPropertyGroup(SupplyClassPG.class);
        pg.setSupplyClass(sclass);
        pg.setSupplyType(supplyclass);
        as.setPropertyGroup(pg); */

        /*ldms = (LDMService) getBindingSite().getServiceBroker().getService(this, LDMService.class,
                new ServiceRevokedListener() {
                    public void serviceRevoked(ServiceRevokedEvent re) {
                        if(LDMService.class.equals(re.getService()))
                            ldms = null;
                    }
                    });

        as.registerWithLDM(ldms.getLDM());
        getBindingSite().getServiceBroker().releaseService(this, LDMService.class, ldms); */
        if(asset!= null){
        UID uid = asset.getUID();
            //System.out.println("uid is "+uid.toString()+ " cluster "+cluster);
        long id = uid.getId();
        UID newuid = new UID(v.elementAt(1).toString(),id);
            //System.out.println("Newuid is "+newuid.toString()+ " cluster "+cluster);
        asset.setUID(newuid);
             //System.out.println("Setuid is "+asset.getUID().toString()+ " cluster "+cluster);
  		//NewTypeIdentificationPG ntipg = (NewTypeIdentificationPG) as.getTypeIdentificationPG();
        //ntipg.setNomenclature(v.elementAt(6).toString());

        nt.setDirectObject(asset);
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
    
    public String stringPlay(String a){
        if(a.equalsIgnoreCase("PackagedPol")==true){
            a = "PackagedPOL";
            return a;
        }
        if(a.equalsIgnoreCase("Subsistence")==true){
            a = "ClassISubsistence";
            return a;
        }
        else
            return a;
    }

	/// PSU
	public void generateAndPublish(String customer, String ofType, MaintainedItem maintainedItem, long end_time, double quantity, long today) {

		NewTask nt = getNewTask(customer, ofType, maintainedItem, end_time, quantity, today);
		myBS.publishAdd(nt);
		myLoggingService.shout(cluster + ": Task added " + nt);
 		disposition(nt);

	}

    public NewTask getNewTask(String customer, String ofType, MaintainedItem maintainedItem, long day, double qty, long Today) {

        PlanningFactory pf = (PlanningFactory) myDomainService.getFactory("planning");
        NewTask nt = pf.newTask();

        // Set verb
        //nt.setVerb(forecastVerb);
        Verb verb = new Verb("Supply");
        nt.setVerb(verb);

        NewPrepositionalPhrase npp = pf.newPrepositionalPhrase();
        npp.setPreposition(Constants.Preposition.FOR);
        npp.setIndirectObject(customer);
        nt.addPrepositionalPhrase(npp);

        // BulkPol or ammo
        NewPrepositionalPhrase npp1 = pf.newPrepositionalPhrase();
        npp1.setPreposition(Constants.Preposition.OFTYPE);
        npp1.setIndirectObject(ofType);
        nt.addPrepositionalPhrase(npp1);

/// I have to put item here for nomenclature.
		NewPrepositionalPhrase npp3 = pf.newPrepositionalPhrase();
        npp3.setPreposition(Constants.Preposition.MAINTAINING);
        npp3.setIndirectObject(maintainedItem);		//	 this is questionable.
        nt.addPrepositionalPhrase(npp3);

///

		// Designate date in which these forecast tasks are generated.
        NewPrepositionalPhrase npp2 = pf.newPrepositionalPhrase();
        npp2.setPreposition("TODAY");
        Date date = new Date(Today * 86400000);
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

	/// PSU
/*
	private	void openLoggingFile() {
		String dir = System.getProperty("org.cougaar.workspace");
        // the Result file in workspace.
        try {
           rst = new java.io.BufferedWriter(new java.io.FileWriter(dir+"/"+ cluster + System.currentTimeMillis() + ".pred.txt", true));
        } catch (java.io.IOException ioexc) {
           myLoggingService.error("can't write file, io error");
        }
	}
*/
	private void read_DemandHistoryManager_class_from_BB_if_it_is_null() {

		if (demandHistoryManager == null )
		{
			for (Iterator iter = historySubscription.getAddedCollection().iterator() ; iter.hasNext() ;)
	        {
		        DemandHistoryManager demandHistoryManager = (DemandHistoryManager) iter.next();
				break;
			}
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
    private IncrementalSubscription commstatusSubscription;
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

//     private final Verb forecastVerb = Verb.getVerb("ForecastDemand");
    private final Verb forecastVerb = org.cougaar.logistics.ldm.Constants.Verb.Supply;

    private final int MovingAverage = 1;
    private final int SupportVectorMachine = 2;
    private final int KalmanFilter = 3;
    private int selectedPredictor = MovingAverage;

	private NewTask publishedTasksOfAmmo = null;
	private NewTask publishedTasksOfBulk = null;
	private Hashtable asset_list = new Hashtable();

	long Rantime = 0; 

    long commLossTime = -1;
    long commRestoreTime = -1;
    boolean status = true;
    ArrayList local_alist = new ArrayList();
    long pcd = -1;
    long clt = -1;
    long gap =-1;
    long ost = -1;
    int comm_count = 0;
    ArrayList retain_alist = new ArrayList();

	// PSU
	private PredictorManager predictorManager=null;
	private DemandHistoryManager demandHistoryManager=null;
    private java.io.BufferedWriter rst = null;
}

