/*
  * Yunho Hong
  * email : yyh101@psu.edu
  * PSU, August , 2003
*/

package org.cougaar.tools.alf.predictor.plugin;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.plugin.ComponentPlugin;
import org.cougaar.core.service.*;
import org.cougaar.core.util.UID;
import org.cougaar.glm.ldm.Constants;
import org.cougaar.planning.ldm.PlanningFactory;
import org.cougaar.planning.ldm.plan.*;
import org.cougaar.util.UnaryPredicate;
import org.cougaar.logistics.plugin.inventory.MaintainedItem;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.*;

public class ARPredictorPlugin extends ComponentPlugin {

	// It is enough to subscribe to the Allocation only. I think we don't nee to subscribe to Supply tasks. 
    UnaryPredicate allocPredicate = new UnaryPredicate()	{ 	
		public boolean execute(Object o) {  
			if (o instanceof Allocation)
			{
				Allocation alloc = (Allocation) o;
				Task tempTask = (Task) alloc.getTask();
				Verb verb = tempTask.getVerb();
				if (verb.equals("Supply"))  {		
					return true;		
				}
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

    public void setupSubscriptions() {

		cluster = ((AgentIdentificationService) getBindingSite().getServiceBroker().getService(this, AgentIdentificationService.class, null)).getName();

        myBS = getBlackboardService();
        myDomainService = (DomainService) getBindingSite().getServiceBroker().getService(this, DomainService.class, null);
        myLoggingService = (LoggingService) getBindingSite().getServiceBroker().getService(this, LoggingService.class, null);

		allocSubscription			= (IncrementalSubscription) myBS.subscribe(allocPredicate);		//	subscribe to allocation 
        arPluginMessageSubscription	= (IncrementalSubscription) myBS.subscribe(arPluginMessagePredicate);
		historySubscription			= (IncrementalSubscription) myBS.subscribe(historyPredicate);	// for rehydration

		if((OutputFileOn=getParametersWhichTurnsOnOrOffOutputFile())) {

			String dir = System.getProperty("org.cougaar.workspace");
            // Result file
            try {
               rst = new java.io.BufferedWriter(new java.io.FileWriter(dir+"/"+ cluster + System.currentTimeMillis()/300000 + ".ar.txt", true));
            } catch (java.io.IOException ioexc) {
               myLoggingService.shout("can't write file, io error");
            }

		}

	    if (myBS.didRehydrate() == false) {
			history = new History("HistoryForCustomerSidePredictor");			
			myBS.publishAdd(history);
		} else {
			// this is for rehydration
			if_history_is_null_retrieve_from_bb();
		}
    }

    public void execute() {
	
		checkAllocSubscription();
		checkARPluginMessage();
		System.out.println("ARPredictorPlugin executing");
		if (c1!=null )	{
			predictAllocationResults(c1);
			System.out.println("ARPredictorPlugin publishing predictions");
		}

    }

	private void if_history_is_null_retrieve_from_bb() {
		
		if (history==null)	{
			Collection c = myBS.query(historyPredicate);			
			for (Iterator iter = c.iterator();iter.hasNext() ; )	{
				history = (History) iter.next();
				break;
			}
		}

	}

	Collection c1=null, c3=null;
		
	private void checkAllocSubscription() {

       	if (!allocSubscription.isEmpty()) {

			Today = (int) (currentTimeMillis()/ 86400000);

			c1 = allocSubscription.getAddedCollection();    
			c3 = allocSubscription.getChangedCollection();  

			updateHistory(c3);	
		}

	}
	
	private void checkARPluginMessage() {

		Collection addedMessages	= arPluginMessageSubscription.getAddedCollection();
		Collection changedMessages  = arPluginMessageSubscription.getChangedCollection();

		for (Iterator iter = addedMessages.iterator(); iter.hasNext(); )	{

			ARPluginMessage arPluginMessage = (ARPluginMessage)iter.next();
			if (arPluginMessage.getAgentName().equalsIgnoreCase(cluster))	{
				continue;					
			}

			iInfo =	arPluginMessage.getInventoryInfo();

			if (iInfo != null && isOutputFileOn())	{
				myLoggingService.shout("[ARPredictorPlugin] added ARPluginMessage");
			}

			if (iInfo != null && isOutputFileOn())	{
				if (!iInfo.getAgentName().equalsIgnoreCase(cluster))
				{
					for (int i=0;i<iInfo.getNumberOfItems(); i++)	{
						myLoggingService.shout("["+ cluster+":"+iInfo.getTime()/86400000+"] " + iInfo.getAgentName() + ", Inventory level "+i+" , " +  iInfo.getInventoryLevel(i));
					}
				}				
			}
		}

		for (Iterator iter2 = changedMessages.iterator(); iter2.hasNext(); )	{

			ARPluginMessage arPluginMessage = (ARPluginMessage)iter2.next();

			iInfo =	arPluginMessage.getInventoryInfo();

			if (iInfo != null && isOutputFileOn())	{
				myLoggingService.shout("[ARPredictorPlugin] added ARPluginMessage");
			}

			if (isOutputFileOn())	{
				if (iInfo != null)		{
					if (!iInfo.getAgentName().equalsIgnoreCase(cluster))	{
						for (int i=0;i<iInfo.getNumberOfItems(); i++)	{
							myLoggingService.shout("["+ cluster+":"+iInfo.getTime()/86400000+"] " + iInfo.getAgentName() + ", Inventory level "+i+" , " +  iInfo.getInventoryLevel(i));
						}
					}				
				}
			}
		}
	}

	private void updateHistory(Collection changedAllocation)	{

		boolean updated = false;

		if (changedAllocation	!=	null)	{		updated = reflectAllocationIntoHistory(changedAllocation);		}

		if (updated) {
			myBS.publishChange(history);	// for rehydration.
		}

	}

	private boolean bulkOrAmmo(Task ti) {
		PrepositionalPhrase pp = ti.getPrepositionalPhrase("OfType");
		if (pp != null)
		{
			String oftype = (String) pp.getIndirectObject();
			if (!oftype.equalsIgnoreCase("BulkPOL")&&!oftype.equalsIgnoreCase("Ammunition"))	{	return false;	}
		} else {
//			myLoggingService.shout ("This task "+ti.getUID()+ " has null Prepositional Phrase OfType" );
			return false;
		}
		return true;
	}

	private String getNomenclature(Task ti) {

		PrepositionalPhrase pp3 = ti.getPrepositionalPhrase("Maintaining");

		if (pp3 != null)
		{
			return (String)((MaintainedItem) pp3.getIndirectObject()).getNomenclature();
		} else {
//			myLoggingService.shout ("null Prepositional Phrase Maintaining" );
			return null;
		}
	}

	private String getClass(Task ti) {
		
		PrepositionalPhrase pp = ti.getPrepositionalPhrase("OfType");
		String oftype = null;

		if (pp != null)	{
			oftype = (String) pp.getIndirectObject();
		} else {
//			myLoggingService.shout ("null Prepositional Phrase OfType" );
			return null;
		}
		return oftype;
	}

	private boolean reflectAllocationIntoHistory(Collection addedAllocation)  // addedAllocation could be adde or changed Allocation.
	{

		boolean updated = false;

		if (addedAllocation!=null)	{

			for (Iterator allocIterator = addedAllocation.iterator(); allocIterator.hasNext(); ) {

				Allocation ai = (Allocation)allocIterator.next();	
				
				int rarsuccess = -1;  // -1 -> nothing, 0 -> failure, 1 -> success.
				double rarConfidence = 0;
				AllocationResult rar = null;

				if ((rar = ai.getReportedResult())!=null)
				{
					Task ti = (Task) ai.getTask();
					if (!ti.getUID().getOwner().equalsIgnoreCase(cluster))	{		continue;		}

					// nomenclature
					String nomenclature = null;
					if ((nomenclature = getNomenclature(ti))==null)	{		continue;		}
	
					// Search 
					HistoryElement historyElement = history.getHistory(nomenclature);

					if (historyElement == null && isOutputFileOn()) {		
						myLoggingService.shout("HistoryElement for history of "+ nomenclature + " could not be created !!");
						continue;		
					}

					if (rar.isSuccess())	{ rarsuccess = 1; } else { rarsuccess = 0;}
					rarConfidence = rar.getConfidenceRating();

					historyElement.addResult(rarsuccess);
					history.putHistory(nomenclature,historyElement);
					if (!updated) 	{ updated = true; }
				}

			} 

		}

		return updated;

	}

	private void predictAllocationResults(Collection c)    {

		for (Iterator iter = c.iterator();iter.hasNext(); )
		{

			Allocation allocation = (Allocation) iter.next();
			Task t = (Task) allocation.getTask();

			if (!t.getUID().getOwner().equalsIgnoreCase(cluster))	{		continue;		}

				AllocationResult receivedResult = allocation.getReceivedResult();
				AllocationResult reportedResult = allocation.getReportedResult();

				/// 
				AllocationResult estimatedResult = allocation.getEstimatedResult();
				double earConfidence = estimatedResult.getConfidenceRating();

				///
				if (reportedResult ==null && receivedResult==null && earConfidence < 0.4 )
				{
					double success = 0;
					boolean successBoolean = false;
					String successOrNot = "fail";

					// nomenclature
					String nomenclature = null;

					if ((nomenclature = getNomenclature(t))==null)	{		
//						myLoggingService.shout("This task " + t.getUID() + " has null nomenclatured !!"); 
						continue;		
					}

					//////////// Search history 
					HistoryElement historyElement = history.getHistory(nomenclature);

					if (historyElement == null) {		

//						myLoggingService.shout("TreeSet for history of "+ nomenclature + " does not exist.");
						success = 0.4;
						successOrNot = "success";
						successBoolean = true;

					} else {
					
						success = 0.6*historyElement.getConfidence();
						if (success > 0.4)	{	// decision on the success or failure.
							successOrNot = "success";
							successBoolean = true;
						} 

					}

					PlanningFactory rootFactory=null;
					if(myDomainService != null) {
						rootFactory = (PlanningFactory) myDomainService.getFactory("planning");
					}

					AspectValue [] avs = estimatedResult.getAspectValueResults(); 

					AllocationResult newReceivedResult = rootFactory.newAllocationResult(success, successBoolean, avs);
					AllocationImpl allocImpl = (AllocationImpl) allocation;

					allocImpl.setReceivedResult(newReceivedResult);

					myBS.publishChange(allocImpl);

					if (isOutputFileOn())	{
						long end_time = (long) (t.getPreferredValue(AspectType.END_TIME) / 86400000);
						try {
							rst.write(Today+"\t"+t.getUID()+"\t"+nomenclature+"\t"+successOrNot+"\t"+success+"\t"+end_time+"\n");
							rst.flush();
			            } catch (java.io.IOException ioexc) {
							myLoggingService.shout("can't write file, io error");
						}
					}
				}
		}
    }

	private	boolean getParametersWhichTurnsOnOrOffOutputFile() {

		Collection c = getParameters();

        for (Iterator iter = c.iterator() ; iter.hasNext() ;)
        {
            String s = (String) iter.next();
			if (!s.equalsIgnoreCase("true"))	{
				return false;
			}
        }
		return true;
	}

	public boolean isOutputFileOn() {

		return OutputFileOn;
	}

	private int Today = 0;
	private String cluster;
    private LoggingService myLoggingService;
    private DomainService myDomainService;
    private BlackboardService myBS;

    private IncrementalSubscription allocSubscription, historySubscription, arPluginMessageSubscription;

	private InventoryInfo iInfo = null;
	private History history = null;
	private boolean OutputFileOn = true;
    java.io.BufferedWriter rst = null;
   
}
