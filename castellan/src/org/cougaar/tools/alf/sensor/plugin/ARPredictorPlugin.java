/*
  * Yunho Hong
  * email : yyh101@psu.edu
  * PSU, August , 2003
*/

package org.cougaar.tools.alf.sensor.plugin;

import org.cougaar.core.adaptivity.InterAgentCondition;
import org.cougaar.core.agent.service.alarm.PeriodicAlarm;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.plugin.ComponentPlugin;
import org.cougaar.core.service.*;
import org.cougaar.core.util.UID;
import org.cougaar.glm.ldm.Constants;
import org.cougaar.planning.ldm.PlanningFactory;
import org.cougaar.planning.plugin.util.PluginHelper;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.plan.*;
import org.cougaar.util.ConfigFinder;
import org.cougaar.util.UnaryPredicate;
import org.cougaar.glm.plugins.TaskUtils;
import org.cougaar.logistics.plugin.inventory.MaintainedItem;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;

public class ARPredictorPlugin extends ComponentPlugin {
/*
	UnaryPredicate commStatusPredicate = new UnaryPredicate() {
        public boolean execute(Object o) {
              return o instanceof CommStatus;
        }
    };
*/
	// this is for the tasks to be allocated by this predictor after communication is lost.
	UnaryPredicate supplyTaskPredicate = new UnaryPredicate() {
        public boolean execute(Object o) {
			if (o instanceof Task)
			{
				Task tempTask = (Task) o;;
				Verb verb = tempTask.getVerb();
				if (verb.equals("Supply"))  {		

					int end_time = (int) (tempTask.getPreferredValue(AspectType.END_TIME) / 86400000);
					if (Today < end_time)	{
						return true;								
					}

				}
			}
			return false;
        }
    };

	// Predicate for PlanElement of Supply Tasks.
	// It is enough to subscribe to the Allocation only. I think we don't nee to subscribe to Supply tasks. 
    UnaryPredicate allocPredicate = new UnaryPredicate()	{ 	
		public boolean execute(Object o) {  
			if (o instanceof Allocation)
			{
				Allocation alloc = (Allocation) o;
				Task tempTask = (Task) alloc.getTask();
				Verb verb = tempTask.getVerb();
				if (verb.equals("Supply"))  {		return true;		}
			}
			return false; 	
		} 
	};

    UnaryPredicate arPluginMessagePredicate = new UnaryPredicate()	{ 	
		public boolean execute(Object o) {  
			if (o instanceof ARPluginMessage)	{
				return true;
			}
			return false; 	
		} 
	};

	UnaryPredicate historyPredicate	= new UnaryPredicate()	{ public boolean execute(Object o) {  return o instanceof History;   }    };

	class History implements java.io.Serializable {

		HashMap listOfSubItem;
		String name = null;
		public History(String name) {	// this name is for identification
			super();
			this.name = name;
			listOfSubItem = new HashMap();
		}

		public boolean isThisName(String name) {
			return this.name.equalsIgnoreCase(name);
		}

		public TreeMap getHistory(String nomenclature) {

			TreeMap timeSeriesOfTheItem = (TreeMap) listOfSubItem.get(nomenclature);

			if (timeSeriesOfTheItem == null){
				timeSeriesOfTheItem = new TreeMap(new DateComparator());
				listOfSubItem.put(nomenclature,timeSeriesOfTheItem);
			}

			return timeSeriesOfTheItem;
		}
	};

	private class DateComparator implements Comparator, java.io.Serializable {
		
		public int compare(Object o1, Object o2) {
		    if (o1 == o2) return 0;
	    
		    Long x = (Long) o1;
		    Long y = (Long) o2;
		    long x_date = x.longValue();
		    long y_date = y.longValue();
				
	    
		    // Smaller dates are less preferable
		    if (x_date < y_date)
				return 1;
		    else if (x_date > y_date) 
				return -1;
		    else 
				return 0;
		}

    }

	History history = null;

	// I assume here that the record of allocation result will be kept in time sequence.
	class AllocResult implements java.io.Serializable 	{

		public int success = 0;		public int end_time = 0;

		public AllocResult(int end_time, int success) {
			this.success = success;
			this.end_time = end_time;
		}

	};

    public void setupSubscriptions() {

		cluster = ((AgentIdentificationService) getBindingSite().getServiceBroker().getService(this, AgentIdentificationService.class, null)).getName();

        myBS = getBlackboardService();
        myDomainService = (DomainService) getBindingSite().getServiceBroker().getService(this, DomainService.class, null);
        myLoggingService = (LoggingService) getBindingSite().getServiceBroker().getService(this, LoggingService.class, null);

//		commStatusSubscription	= (IncrementalSubscription) myBS.subscribe(commStatusPredicate);
		allocSubscription				= (IncrementalSubscription) myBS.subscribe(allocPredicate);		//	subscribe to allocation 
        arPluginMessageSubscription		= (IncrementalSubscription) myBS.subscribe(arPluginMessagePredicate);
		historySubscription				= (IncrementalSubscription) myBS.subscribe(historyPredicate);	// for rehydration

		if((OutputFileOn=getParametersWhichTurnsOnOrOffOutputFile())) {

			String dir = System.getProperty("org.cougaar.workspace");
            // Result file
            try {
               rst = new java.io.BufferedWriter(new java.io.FileWriter(dir+"/"+ cluster + System.currentTimeMillis()/300000 + ".ar.txt", true));
            } catch (java.io.IOException ioexc) {
               System.err.println("can't write file, io error");
            }

		}

	    if (myBS.didRehydrate() == false) {
			history = new History("HistoryForCustomerSidePredictor");			
			myBS.publishAdd(history);
		}

        myLoggingService.shout("ARPredictorPlugin start at " + cluster);
        myBS.setShouldBePersisted(false);
    }

    public void execute() {
	
		// this is for rehydration
		if_history_is_null_retrieve_from_bb();

		boolean changed = checkAllocSubscription();
		checkARPluginMessage();
//		boolean commLost = checkComm();

		predictAllocationResults();
//		if (commLost)	{			
//			callPredictor();			
//			alarm = new TriggerFlushAlarm(currentTimeMillis());
//          as.addAlarm(alarm);
//		}  
    }
/*
	private boolean checkComm()	{

		boolean status = false;

		for (Enumeration e = commStatusSubscription.getAddedList(); e.hasMoreElements();) {
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
						
		return status;
	}
*/

	private void if_history_is_null_retrieve_from_bb() {
		
		if (history==null)	{
			
			Collection c = historySubscription.getAddedCollection();
			for (Iterator iter = c.iterator();iter.hasNext() ; )	{
				history = (History) iter.next();
			}
		}

	}

    private boolean checkAllocSubscription() {

		boolean updated = false;
       	if (!allocSubscription.isEmpty()) {

			Collection c1 = allocSubscription.getAddedCollection();
			Collection c2 = allocSubscription.getRemovedCollection();
			Collection c3 = allocSubscription.getChangedCollection();

			// I think in updateHistory we can print out the difference between actual confidence and estimated confidence.
			updated = updateHistory(c1, c2, c3, currentTimeMillis());	
		}

        return updated;
	}
	
	private void checkARPluginMessage() {

		Collection c1 = arPluginMessageSubscription.getAddedCollection();
		Collection c2 = arPluginMessageSubscription.getChangedCollection();

		for (Iterator iter = c1.iterator(); iter.hasNext(); )
		{
//			try
//			{
				ARPluginMessage arPluginMessage = (ARPluginMessage)iter.next();
				if (arPluginMessage.getAgentName().equalsIgnoreCase(cluster))	{
					continue;					
				}
/*
				ByteArrayOutputStream byteArrayInfo = arPluginMessage.getInventoryInfo();

				if (byteArrayInfo != null)	{
	
					ByteArrayInputStream istream = new ByteArrayInputStream(byteArrayInfo.toByteArray());
					ObjectInputStream p = new ObjectInputStream(istream);
	
					iInfo = (InventoryInfo)p.readObject();
		
					istream.close();
				
					if (iInfo != null && isOutputFileOn())	{
						myLoggingService.shout("[ARPredictorPlugin] added ARPluginMessage");
					}

				}
*/
				iInfo =	arPluginMessage.getInventoryInfo();

				if (iInfo != null && isOutputFileOn())	{
					myLoggingService.shout("[ARPredictorPlugin] added ARPluginMessage");
				}

//			}
//			catch (java.io.IOException e)
//			{
//				myLoggingService.shout("[ARPredictorPlugin]Error 1");	
//			}
//			catch (java.lang.ClassNotFoundException e2)
//			{
//				myLoggingService.shout("[ARPredictorPlugin]Error 3");	
//			}

			if (iInfo != null && isOutputFileOn())	{
				if (!iInfo.getAgentName().equalsIgnoreCase(cluster))
				{
					for (int i=0;i<iInfo.getNumberOfItems(); i++)	{
						myLoggingService.shout("["+ cluster+":"+iInfo.getTime()/86400000+"] " + iInfo.getAgentName() + ", Inventory level "+i+" , " +  iInfo.getInventoryLevel(i));
					}
				}				
			}
		}


		for (Iterator iter2 = c2.iterator(); iter2.hasNext(); )
		{
//			try
//			{
				ARPluginMessage arPluginMessage = (ARPluginMessage)iter2.next();
/*
				ByteArrayOutputStream byteArrayInfo = arPluginMessage.getInventoryInfo();

				if (byteArrayInfo != null)	{
	
					ByteArrayInputStream istream = new ByteArrayInputStream(byteArrayInfo.toByteArray());
					ObjectInputStream p = new ObjectInputStream(istream);
	
					iInfo = (InventoryInfo)p.readObject();
		
					istream.close();
				}
*/
				iInfo =	arPluginMessage.getInventoryInfo();

				if (iInfo != null && isOutputFileOn())	{
					myLoggingService.shout("[ARPredictorPlugin] added ARPluginMessage");
				}

//			}
//			catch (java.io.IOException e)
//			{
//				myLoggingService.shout("[ARPredictorPlugin]Error 2");	
//			}
//			catch (java.lang.ClassNotFoundException e2)
//			{
//				myLoggingService.shout("[ARPredictorPlugin]Error 4");	
//			}

			if (iInfo != null)	{
				if (!iInfo.getAgentName().equalsIgnoreCase(cluster))
				{
					for (int i=0;i<iInfo.getNumberOfItems(); i++)	{
						myLoggingService.shout("["+ cluster+":"+iInfo.getTime()/86400000+"] " + iInfo.getAgentName() + ", Inventory level "+i+" , " +  iInfo.getInventoryLevel(i));
					}
				}				
			}
		}
	}

	private boolean updateHistory(Collection addedAllocation, Collection removedAllocation, Collection changedAllocation, long nowTime)	{
		int nAllocations=0;
		Iterator allocIterator=null;
		boolean updated = true;

		today = (int) (nowTime/ 86400000);

		if (addedAllocation!=null)		{		reflectAllocationIntoHistory(addedAllocation,	today);		}
		if (changedAllocation!=null)	{		reflectAllocationIntoHistory(changedAllocation,	today);		}

		myBS.publishChange(history);	// for rehydration.
		return updated;
	}

	private boolean bulkOrAmmo(Task ti) {
		PrepositionalPhrase pp = ti.getPrepositionalPhrase("OfType");
		if (pp != null)
		{
			String oftype = (String) pp.getIndirectObject();
			if (!oftype.equalsIgnoreCase("BulkPOL")&&!oftype.equalsIgnoreCase("Ammunition"))	{	return false;	}
		} else {
			myLoggingService.shout ("null Prepositional Phrase OfType" );
			return false;
		}
		return true;
	}

	private String getNomenclature(Task ti) {
		// maintaining 
		PrepositionalPhrase pp3 = ti.getPrepositionalPhrase("Maintaining");
		String nomenclature = null;
		if (pp3 != null)
		{
			return (String)((MaintainedItem) pp3.getIndirectObject()).getNomenclature();
		} else {
			myLoggingService.shout ("null Prepositional Phrase Maintaining" );
			return null;
		}
	}

	private boolean reflectAllocationIntoHistory(Collection addedAllocation, int nowTime)
	{
		if (addedAllocation!=null)
		{
			int nAllocations = addedAllocation.size();
		    Iterator allocIterator = addedAllocation.iterator();   

			for (int i = 0; i < nAllocations; i++) {

				Allocation ai = (Allocation)allocIterator.next();	Task ti = (Task) ai.getTask();

/////////////// Examine whethe it is a task of BulkPOL or Ammunition.
				if (!bulkOrAmmo(ti))	{		continue;	}
				// nomenclature
				String nomenclature = null;
				if ((nomenclature = getNomenclature(ti))==null)	{		continue;		}
	
				// Search 
				TreeMap subitem = history.getHistory(nomenclature);

				if (subitem == null && isOutputFileOn()) {		
					myLoggingService.shout("TreeSet for history of "+ nomenclature + " could not be created !!");
					continue;		
				}		

				int rarsuccess = -1;  // -1 -> nothing, 0 -> failure, 1 -> success.
				double rarConfidence = 0;
				AllocationResult rar = null;
				if ((rar = ai.getReportedResult())!=null)
				{
					if (rar.isSuccess())	{ rarsuccess = 1; } else { rarsuccess = 0;}
					rarConfidence = rar.getConfidenceRating();

					// in the future					
//					if ((pi instanceof Allocation) && v.equals("Supply"))
//					{
//						AspectValue avQ = rar.getAspectValue(AspectType.QUANTITY); 
//						AspectValue avE = rar.getAspectValue(AspectType.END_TIME);
//						rarQuantity = avQ.doubleValue();
//						rarEndTime = avE.longValue()/86400000;
//					}
				}
		    
				long end_time = (long) (ti.getPreferredValue(AspectType.END_TIME) / 86400000) ;

				if (rarsuccess >=0)		{
					subitem.put(new Long(end_time), new Integer(rarsuccess));
				}
			} // for
		}
		return true;
	}

    public void callPredictor() {
		predictAllocationResults();
    }

	private int Today = 0;

	private void predictAllocationResults() 
    {
		Today = (int) (currentTimeMillis()/ 86400000);
		Collection c = myBS.query(supplyTaskPredicate);  // These tasks should be after 

		for (Iterator iter = c.iterator();iter.hasNext(); )
		{
			// How can I know which task is for other agents ?
			Task t = (Task) iter.next();

			if (!t.getUID().getOwner().equalsIgnoreCase(cluster))	{		continue;		}

			// if Task t is not BulkPOL and not Ammunition, then skip.
			if (!bulkOrAmmo(t))	{		continue;		}
			// nomenclature
			String nomenclature = null;
			if ((nomenclature = getNomenclature(t))==null)	{		continue;		}

			// if this task does not have the report allocationResult, then print out this. 
			// in actual comm loss, create allocationResult.
			PlanElement allocation = (PlanElement) t.getPlanElement();
			if (allocation != null)
			{
				if (!(allocation instanceof Allocation))	{
					continue;
				}

				AllocationResult reportedResult = allocation.getReportedResult(); 
			
				if (reportedResult==null)
				{
					double success = 0;
					boolean successBoolean = false;
					String successOrNot = "fail";

					//////////// Search history 
					TreeMap subitem = history.getHistory(nomenclature);
					if (subitem == null) {		
						myLoggingService.shout("TreeSet for history of "+ nomenclature + " does not exist.");

					}		

					long end_time = (long) (t.getPreferredValue(AspectType.END_TIME) / 86400000);

					int tw = 0;
					
					Collection subitemCollection = (Collection) subitem.tailMap(new Long(end_time)).values();
					
					for (Iterator iter2 = subitemCollection.iterator();iter2.hasNext()&& tw < 5 ; )
					{
						tw++;
						Integer successValue = (Integer) iter2.next();
						success += successValue.intValue();

					}

					if (tw!=0)	{		
						success = 0.6*success/tw;		
						if (success > 0.4)	{	// decision on the success or failure.
							successOrNot = "success";
							successBoolean = true;
						} 
					} else {
						success = 0.4;
						successOrNot = "success";
						successBoolean = true;
					}

					PlanningFactory rootFactory=null;
					if(myDomainService != null) {
						rootFactory = (PlanningFactory) myDomainService.getFactory("planning");
					}

			        AspectValue [] avs = new AspectValue[2];
			        avs[0] = AspectValue.newAspectValue(AspectType.END_TIME, end_time);
			        avs[1] = AspectValue.newAspectValue(AspectType.QUANTITY, PluginHelper.getPreferenceBestValue(t, AspectType.QUANTITY));

					AllocationResult expectedResult = rootFactory.newAllocationResult(success, successBoolean, avs);
					allocation.setEstimatedResult(expectedResult);
					myBS.publishChange(allocation);
					if (isOutputFileOn())	{
						try {
							rst.write(Today+"\t"+t.getUID()+"\t"+nomenclature+"\t"+successOrNot+"\t"+success+"\t"+end_time+"\n");
							rst.flush();
			            } catch (java.io.IOException ioexc) {
							System.err.println("can't write file, io error");
						}
					}
				}
			}
		}
    }

	private	boolean getParametersWhichTurnsOnOrOffOutputFile() {

		Collection c = getParameters();

        Properties props = new Properties() ;
        // Iterate through the parameters
        int count = 0;
        for (Iterator iter = c.iterator() ; iter.hasNext() ;)
        {
            String s = (String) iter.next();
			if (!s.equalsIgnoreCase("true"))
			{
				return false;
			}
//			break;
        }
		return true;
	}

	public boolean isOutputFileOn() {

		return OutputFileOn;
	}

	private String cluster;
    private LoggingService myLoggingService;
    private DomainService myDomainService;
    private BlackboardService myBS;

    private IncrementalSubscription taskSubscription;  
    private IncrementalSubscription allocSubscription, supplyTaskSubscription, historySubscription, arPluginMessageSubscription;
    private IncrementalSubscription commStatusSubscription;

	private InventoryInfo iInfo = null;
    boolean changed = false;
    int today = -1;

	private boolean OutputFileOn = true;
    long nextTime = 0;
    java.io.BufferedWriter rst = null;
   
    long commLossTime = -1;
    long commRestoreTime = -1;
    int comm_count = 0;

}
