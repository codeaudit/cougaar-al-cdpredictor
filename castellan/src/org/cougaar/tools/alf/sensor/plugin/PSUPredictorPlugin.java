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
	boolean OutputFileOn = true;

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


//// PSU

	UnaryPredicate demandHistoryManagerPredicate	= new UnaryPredicate()	{ 
		public boolean execute(Object o) {  return o instanceof DemandHistoryManager;   }    
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

		OutputFileOn = getParametersWhichTurnsOnOrOffOutputFile();
		myLoggingService.shout("OutputFileOn = " + OutputFileOn ); // for Debug

		if (selectedPredictor == KalmanFilter) {
/*
            taskSubscription = (IncrementalSubscription) myBS.subscribe(taskPredicate);
            arrayListSubscription = (IncrementalSubscription) myBS.subscribe(arrayListPredicate);
//            commstatusSubscription = (IncrementalSubscription) myBS.subscribe(commstatusPredicate);
            if (flagger == false) {
                if (!taskSubscription.isEmpty())	{            taskSubscription.clear();				}
                if (!servletSubscription.isEmpty()) {            servletSubscription.clear();			}
                flagger = true;
            }
            if (myBS.didRehydrate() == false)		{              getActualDemand();					}
*/
		} else {

			////// PSU start
            taskSubscription	= (IncrementalSubscription) myBS.subscribe(taskPredicate);		//	<- This is Common with Himanshu.
			demandHistoryManagerSubscription = (IncrementalSubscription) myBS.subscribe(demandHistoryManagerPredicate);	// for rehydration.

			// PredictorManager does not need to be backed up becasue it does not have changing data.
			predictorManager = new PredictorManager(cluster,myLoggingService,getConfigFinder(),this);

	        if (myBS.didRehydrate() == false) {
//				demandHistoryManager = new DemandHistoryManager(myLoggingService);
				demandHistoryManager = new DemandHistoryManager();
				predictorManager.setDemandHistoryManager(demandHistoryManager);
				myBS.publishAdd(demandHistoryManager);
	        }
			////// PSU end
        }

        myLoggingService.shout("PSUPredictorPlugin start at " + cluster);
        myBS.setShouldBePersisted(false);

//		alarm = new TriggerFlushAlarm(currentTimeMillis());
//       as.addAlarm(alarm);
    }

	public void callPredictor() {

		if (selectedPredictor == KalmanFilter) {
//	         whilePredictorON1();
		} else {
			////// PSU start			
			predictNextDemand();
			////// PSU end
		}
	}

    public void execute() {

/*		
		InterAgentCondition tr;

        for (Enumeration ent = servletSubscription.getAddedList(); ent.hasMoreElements();) {
            tr = (InterAgentCondition) ent.nextElement();
            String content = tr.getContent().toString();
//			setAlgorithmSelection(content);
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
*/
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
//                getActualDemand();
            } else {
                return;
            }
        } else {
			////// PSU start	
			read_DemandHistoryManager_class_from_BB_if_it_is_null();  // for rehydration of DemandHistoryManager.

			boolean updated = updateDemandHistory();

			// for rehydration
			if (updated)	{
				myBS.publishChange(demandHistoryManager);
			}

			// This will be used only after communication loss. However, we cannot emulate communication loss as of now. 
			// Each day, alarm call this execution so that predictor could predict the demand of next day after lead time.
			if (Rantime != (long) currentTimeMillis() / 86400000)		{

				alarm = new TriggerFlushAlarm(currentTimeMillis()+86400000);
				as.addAlarm(alarm);
				Rantime = (long) currentTimeMillis() / 86400000;
			}

			if (called)		{
				called = false;
				callPredictor();
			}						
			////// PSU end	
        }
    }

	/// PSU
	/// updateDemandHistory will be used 
    private boolean updateDemandHistory() {	

        boolean addedUpdated = false, removedUpdated = false;
        
		if (!taskSubscription.isEmpty()) {

			Collection c1 = taskSubscription.getAddedCollection();
			Collection c2 = taskSubscription.getRemovedCollection();
			long today = (long) currentTimeMillis()/86400000;
	
			addedUpdated	= updateDemandHistory(c1, true, today);		// true	 -> added
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

//			Himanshu		
//			Asset as = task.getDirectObject();
//			storeAsset(as);

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

	/// PSU
	private void predictNextDemand()
    {
		long today = (long) currentTimeMillis()/86400000;

		// Depending on item, forecased time point and commitment time is different.
		// All the details are processed in this class.
        myLoggingService.shout("PREDICTORMANAGER.FORCAST AT " + today);
		predictorManager.forecast(today,today+1);	// the first today is commLossDay.
    }

	/// PSU
	public void generateAndPublish(String customer, String ofType, MaintainedItem maintainedItem, long end_time, double quantity, long today, long commitmentTime) {

//		Vector v = vectorForPredictorTask(customer, ofType, maintainedItem.getNomenclature(),commitmentTime,end_time,quantity);
//		NewTask nt = getNewTask(v); 

		NewTask nt = getNewTask(customer, ofType, maintainedItem, end_time, quantity, today, commitmentTime);
		myBS.publishAdd(nt);
		myLoggingService.shout(cluster + ": Task added " + nt);
 		disposition(nt);

	}

	/// PSU
    public NewTask getNewTask(String customer, String ofType, MaintainedItem maintainedItem, long day, double qty, long Today, long commitmentTime) {

        PlanningFactory pf = (PlanningFactory) myDomainService.getFactory("planning");
        NewTask nt = pf.newTask();

        // Set verb
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

        Date commitmentDate = new Date((day-commitmentTime)* 86400000);		
		nt.setCommitmentDate(commitmentDate);
		
		return nt;

    }

	/// PSU
	private void read_DemandHistoryManager_class_from_BB_if_it_is_null() {

		if (demandHistoryManager == null )
		{
			for (Iterator iter = demandHistoryManagerSubscription.getAddedCollection().iterator() ; iter.hasNext() ;)
	        {
		        DemandHistoryManager demandHistoryManager = (DemandHistoryManager) iter.next();
				predictorManager.setDemandHistoryManager(demandHistoryManager);		// predictorManager is contructed in setupSubscription().
				break;
			}
		}
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

	/// PSU
	private	boolean getParametersWhichTurnsOnOrOffOutputFile() {

		Collection c = getParameters();

        Properties props = new Properties() ;
        // Iterate through the parameters
        int count = 0;
        for (Iterator iter = c.iterator() ; iter.hasNext() ;)
        {
            String s = (String) iter.next();
	        myLoggingService.shout("arguement = " + s);
			if (!s.equalsIgnoreCase("true"))
			{
				return false;
			}
//			break;
        }
		return true;
	}

	/// PSU
	public boolean isOutputFileOn() {

		return OutputFileOn;
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
    private IncrementalSubscription demandHistoryManagerSubscription;

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

