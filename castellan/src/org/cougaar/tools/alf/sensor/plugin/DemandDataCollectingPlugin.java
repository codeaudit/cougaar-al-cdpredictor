package org.cougaar.tools.alf.sensor.plugin;

import org.cougaar.core.plugin.ComponentPlugin;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.util.UID;
import org.cougaar.core.agent.service.alarm.PeriodicAlarm;
import org.cougaar.core.service.community.CommunityService;
import org.cougaar.core.service.*;
import org.cougaar.util.*;
import org.cougaar.multicast.AttributeBasedAddress;
import org.cougaar.planning.ldm.plan.*;
import org.cougaar.glm.ldm.oplan.Oplan;
import org.cougaar.planning.ldm.measure.FlowRate;
import org.cougaar.planning.ldm.measure.CountRate;
import org.cougaar.glm.ldm.plan.AlpineAspectType;
import org.cougaar.glm.plugins.TimeUtils.*;
import org.cougaar.tools.alf.sensor.*;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.logistics.plugin.inventory.MaintainedItem;
import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.glm.ldm.plan.QuantityScheduleElement;
import org.cougaar.planning.ldm.plan.Schedule;
import org.cougaar.glm.ldm.asset.Inventory;
import org.cougaar.glm.ldm.asset.SupplyClassPG;

import org.cougaar.logistics.plugin.inventory.InventoryPolicy;
import org.cougaar.logistics.plugin.inventory.LogisticsInventoryPG;

import java.util.*;
import java.util.Iterator;
import java.util.Collection;
import java.util.Vector;
import java.text.DateFormat;
import java.text.ParseException;
import java.io.*;

/* 
 *	programed by Yunho Hong
 *	March 1, 2003
 *	Pennsylvania State University
 */

public class DemandDataCollectingPlugin extends ComponentPlugin
{

	IncrementalSubscription taskSubscription;
	IncrementalSubscription planelementSubscription;
	IncrementalSubscription oPlanSubscription;
	IncrementalSubscription inventorySubscription;
	IncrementalSubscription inventoryPolicySubscription;

	UnaryPredicate taskPredicate = new UnaryPredicate()	{ 	
			public boolean execute(Object o) {  
				if (o instanceof Task)
				{
					Task tempTask = (Task) o;
					Verb verb = tempTask.getVerb();

					if (verb.equals("Supply")||verb.equals("ProjectSupply"))
					{
//						PrepositionalPhrase pp = null;
//						if ((pp = tempTask.getPrepositionalPhrase("OfType"))!=null)
//						{
//							String s = (String) pp.getIndirectObject();
//							if (s.equalsIgnoreCase("BulkPOL")||s.equalsIgnoreCase("Ammunition"))
//							{
								return true;
//							} 
//						}
					}
				}
				return false; 	
			} 
		};

	UnaryPredicate pePredicate = new UnaryPredicate()	{ 	
			public boolean execute(Object o) {  
				if (o instanceof PlanElement)
				{
					PlanElement pe = (PlanElement) o;
					Task tempTask = (Task) pe.getTask();
					Verb verb = tempTask.getVerb();
					
					if (verb.equals("Supply")||verb.equals("ProjectSupply"))
					{
//						PrepositionalPhrase pp = null;
//						if ((pp = tempTask.getPrepositionalPhrase("OfType"))!=null)
//						{
//							String s = (String) pp.getIndirectObject();
//
//							if (s.equalsIgnoreCase("BulkPOL")||s.equalsIgnoreCase("Ammunition"))
//							{
								return true;
//							} 
//						}
					}
				}
				return false; 	
			} 
		};

    /** Selects the LogisticsOPlan objects   **/
/*
	private static class LogisticsOPlanPredicate implements UnaryPredicate {
      public boolean execute(Object o) {
        return o instanceof LogisticsOPlan;
      }
    }
*/
    /**  Passes Inventory assets that have a valid LogisticsInventoryPG   **/
    private static class InventoryPredicate implements UnaryPredicate {
      String supplyType;
    
      public InventoryPredicate(String type) {
        supplyType = type;
      }
    
      public boolean execute(Object o) {
        if (o instanceof Inventory) {
          Inventory inv = (Inventory) o;
          LogisticsInventoryPG logInvpg = (LogisticsInventoryPG) inv.searchForPropertyGroup(LogisticsInventoryPG.class);
          if (logInvpg != null) {
            String type = getAssetType(logInvpg);

			// 
//			System.out.println("type = " + type);

			if (supplyType.equals(type)) {
              return true;
            }
          }
        }
        return false;
      }

	  private String getAssetType(LogisticsInventoryPG invpg) {
        Asset a = invpg.getResource();
        if (a == null) return null;
        SupplyClassPG pg = (SupplyClassPG)  a.searchForPropertyGroup(SupplyClassPG.class);
        return pg.getSupplyType();
      }

	}

    private String getAssetType(LogisticsInventoryPG invpg) {
      Asset a = invpg.getResource();
      if (a == null) return null;
      SupplyClassPG pg = (SupplyClassPG)  a.searchForPropertyGroup(SupplyClassPG.class);
      return pg.getSupplyType();
    }

	private class InventoryPolicyPredicate implements UnaryPredicate {
		String type;

	    public InventoryPolicyPredicate(String type) {
		  this.type = type;
	    }

        public boolean execute(Object o) {
          if (o instanceof org.cougaar.logistics.plugin.inventory.InventoryPolicy) {
            String type = ((InventoryPolicy) o).getResourceType();
//			System.out.println("type = " + type);
            if (type.equals(this.type)) {
//              if (logger.isInfoEnabled()) {
//                logger.info("Found an inventory policy for " + this.type + "agent is: " + getMyOrganization());
//              }
              return true;
            } else {
//              if (logger.isDebugEnabled()) {
//                logger.debug("Ignoring type of: " + type + " in " + getMyOrganization() + " this type is: " + this.type);
//              }
            }
          }
          return false;
        }
	}

	BlackboardService bs;
   	UIDService uidservice;

   	String cluster;  // the current agent's name
	
	double [] a;

	int curr_state = -1;

	boolean oplan_is_not_detected = true;
	
	boolean OutputFileOn = true;

	java.io.BufferedWriter rstTask = null, rstPE = null, rst = null, rstInv=null;
	LoggingService myLoggingService = null;

    public void setupSubscriptions()   {

		OutputFileOn = getParametersWhichTurnsOnOrOffOutputFile();
		if (!isOutputFileOn())	{		return;		}

        bs = getBlackboardService();
		cluster = agentId.toString(); // the cluster where this Plugin is running.
		
		myLoggingService = (LoggingService) getBindingSite().getServiceBroker().getService(this, LoggingService.class, null);

		taskSubscription		 	= (IncrementalSubscription) bs.subscribe(taskPredicate);
		planelementSubscription     = (IncrementalSubscription) bs.subscribe(pePredicate);

	    inventoryPolicySubscription = (IncrementalSubscription) bs.subscribe(new InventoryPolicyPredicate("Ammunition"));
//		logisticsOPlanSubscription	= (IncrementalSubscription) bs.subscribe(new LogisticsOPlanPredicate());
//	    inventorySubscription		= (IncrementalSubscription) bs.subscribe(new InventoryPredicate("Ammunition"));

		String dir = System.getProperty("org.cougaar.workspace");
		
		long forName = System.currentTimeMillis()/300000;

		try
		{
			rst = new java.io.BufferedWriter ( new java.io.FileWriter(dir+"/"+ cluster + forName +".dump.txt", true ));
			rstInv = new java.io.BufferedWriter ( new java.io.FileWriter(dir+"/"+ cluster + forName +"Inv.txt", true ));
//			rstTask = new java.io.BufferedWriter ( new java.io.FileWriter(dir+"/"+ cluster+System.currentTimeMillis()+".t", true ));
//			rstPE = new java.io.BufferedWriter ( new java.io.FileWriter(dir+"/"+ cluster+System.currentTimeMillis()+".p", true ));
		}
		catch (java.io.IOException ioexc)
	    {
		    System.err.println ("can't write data collecting file, io error" );
	    }						

		myLoggingService.shout("DemandDataCollectingPlugin start at " + cluster); 
		bs.setShouldBePersisted(false);

    }
	
	public void execute()
    {
		if (!isOutputFileOn())	{		return;		}

        Iterator iter;

//		long nowTime = (currentTimeMillis()/ 86400000) - baseTime; // long nowTime = System.currentTimeMillis()-offsetTime;
		long nowTime = currentTimeMillis();
//		long nowTime = currentTimeMillis()/ 86400000; // long nowTime = System.currentTimeMillis()-offsetTime;
		checkTaskSubscription(nowTime/86400000, taskSubscription);
		checkPeSubscription(nowTime/86400000, planelementSubscription);

		Collection c = bs.query(new InventoryPredicate("Ammunition"));
		if (c!=null)	{
			printInventory(c.size(), c.iterator(), "added",nowTime);
		}

		c = bs.query(new InventoryPredicate("BulkPOL"));
		if (c!=null)	{
			printInventory(c.size(), c.iterator(), "added",nowTime);
		}

//		checkInventorySubscription(nowTime, inventorySubscription);
    }

	private void checkInventorySubscription(long nowTime, IncrementalSubscription inventorySubscription)
	{
		if (!inventorySubscription.isEmpty()) {
			Collection c1 = inventorySubscription.getAddedCollection();
			if (c1!=null)	{	printInventory(c1.size(), c1.iterator(), "added",nowTime);		}

			Collection c2 = inventorySubscription.getRemovedCollection();
			if (c2!=null)	{	printInventory(c2.size(), c2.iterator(), "removed",nowTime);	} 

			Collection c3 = inventorySubscription.getChangedCollection();
			if (c3!=null)	{	printInventory(c3.size(), c3.iterator(), "changed",nowTime);	} 
		}
	}

	private void printInventory(int nInventories, Iterator inventortyIter, String modifier, long nowTime)
	{
			long checkingTime = System.currentTimeMillis();
			for (int i = 0; i < nInventories; i++) {
				Inventory inv = (Inventory)inventortyIter.next();
				LogisticsInventoryPG logInvpg = (LogisticsInventoryPG) inv.searchForPropertyGroup(LogisticsInventoryPG.class);
				
				String type = "";
				String outputStr ="";
//				String outputStr2 ="";
				String nomenclature ="";
				String typeId ="";
				Collection invLevelCollection = null;
				Schedule invLevels = null;

				if (logInvpg != null) {

					Asset a = logInvpg.getResource();
				    if (a != null) {
						SupplyClassPG pg = (SupplyClassPG)  a.searchForPropertyGroup(SupplyClassPG.class);
						type = pg.getSupplyType();
//						nomenclature = a.getItemIdentificationPG().getNomenclature();
						typeId = a.getTypeIdentificationPG().getTypeIdentification();
						nomenclature = a.getTypeIdentificationPG().getNomenclature(); 

					}

					invLevels = logInvpg.getBufferedInvLevels(); 
//					invLevelCollection = invLevels.getScheduleElementsWithTime(nowTime);
					
//					for (Enumeration en = invLevels.getAllScheduleElements() ; en.hasMoreElements() ;) {
//				      QuantityScheduleElement invLevel = (QuantityScheduleElement) en.nextElement();
//                    outputStr = outputStr + invLevel.getStartTime()+"\t"+invLevel.getEndTime()+"\t"+invLevel.getQuantity() + "\t";
//					}
/*
					Iterator it = invLevelCollection.iterator();
//
                    while (it.hasNext()) {
                      QuantityScheduleElement invLevel = (QuantityScheduleElement) it.next();
                      outputStr = outputStr + invLevel.getQuantity() + "\t";
                    }
*/
//					System.out.println("test 2 " + outputStr);
/*
					outputStr2 ="";
					while (it2.hasNext()) {
                      QuantityScheduleElement invLevel = (QuantityScheduleElement) it2.next();
                      outputStr2 = outputStr2 + invLevel.getQuantity() + "\t";
                    }
					System.out.println("test 3" + outputStr2);
*/
				}
		
				try
				{

					for (Enumeration en = invLevels.getAllScheduleElements() ; en.hasMoreElements() ;) {
				      QuantityScheduleElement invLevel = (QuantityScheduleElement) en.nextElement();
//                      outputStr = outputStr + invLevel.getStartTime()+"\t"+invLevel.getEndTime()+"\t"+invLevel.getQuantity() + "\t";
//					  rst.write("Inv"+nowTime/86400000 +"\t"+ checkingTime + "\t" + modifier + "\t" + type + "\t"+ nomenclature+ "\t" + typeId + "\t" 
//									+invLevel.getStartTime()/86400000+"\t"+invLevel.getEndTime()/86400000+"\t"+invLevel.getQuantity() +"\n");

					  rstInv.write(nowTime/86400000 +"\t"+ checkingTime + "\t" + modifier + "\t" + type + "\t"+ nomenclature+ "\t" + typeId + "\t" 
									+invLevel.getStartTime()/86400000+"\t"+invLevel.getEndTime()/86400000+"\t"+invLevel.getQuantity() +"\n");
					}

//					rstInv.write(nowTime/86400000 +"\t"+ checkingTime+ "\t" + modifier + "\t" + type + "\t"+ nomenclature+ "\t" + typeId + "\t" +outputStr +"\n");
//					rstInv.write("task\t"+refill+"\t"+oftype+"\t"+modifier+"\t"+nowTime+"\t"+ checkingTime+"\t"+ uid.toString()+"\t"+v.toString()
//								 +"\t"+nomenclature+"\t"+qty+"\t"+rate+"\t"+start_time+"\t"+ end_time+"\t\t"+rarsuccess +"\t"+ rarConfidence
//								 +"\t"+earsuccess+"\t"+ earConfidence+"\t"+ti.getUID().getOwner() +"\t"+ cluster+"\n");
					rstInv.flush();
//					rst.flush();
				}
				catch (java.io.IOException ioexc)
				{
					System.err.println ("can't write file, io error" );
			    }					
			} // for
	}

	private void checkTaskSubscription(long nowTime, IncrementalSubscription taskSubscription)
	{
		if (!taskSubscription.isEmpty()) {
				
			Collection c1 = taskSubscription.getAddedCollection();
			if (c1!=null)
			{
				int nAddedTasks = c1.size();
			    Iterator addedTaskIterator = c1.iterator();   
				printOut(nAddedTasks, addedTaskIterator, "added",nowTime);
			}

			Collection c2 = taskSubscription.getRemovedCollection();
			if (c2!=null)
			{
  				int nRemovedTasks = c2.size();
			    Iterator removedTaskIterator = c2.iterator();   
				printOut(nRemovedTasks, removedTaskIterator, "removed",nowTime);
			} 

			Collection c3 = taskSubscription.getChangedCollection();
			if (c3!=null)
			{
	  			int nChangedTasks = c3.size();
			    Iterator changedTaskIterator = c3.iterator();   
				printOut(nChangedTasks, changedTaskIterator, "changed",nowTime);
			} 
		}
	}

	private void checkPeSubscription(long nowTime, IncrementalSubscription planelementSubscription)
	{
		if (!planelementSubscription.isEmpty())
		{
//			myLoggingService.shout("planelementSubscription is not Empty");
			Collection c1 = planelementSubscription.getAddedCollection();
			if (c1!=null)
			{
				int nAddedPes = c1.size();
			    Iterator addedPeIterator = c1.iterator();   
				printOutFromPlanElement(nAddedPes, addedPeIterator, "added-pe",nowTime);
			}

			Collection c2 = planelementSubscription.getRemovedCollection();
			if (c2!=null)
			{
  				int nRemovedPes = c2.size();
			    Iterator removedPeIterator = c2.iterator();   
				printOutFromPlanElement(nRemovedPes, removedPeIterator, "removed-pe",nowTime);
			} 

			Collection c3 = planelementSubscription.getChangedCollection();
			if (c3!=null)
			{
	  			int nChangedPes = c3.size();
			    Iterator changedPeIterator = c3.iterator();   
				printOutFromPlanElement(nChangedPes, changedPeIterator, "changed-pe",nowTime);
			} 
		}

	}

	private void printOutFromPlanElement(int nPes, Iterator peIter, String modifier, long nowTime)
	{
			long checkingTime = System.currentTimeMillis();

			for (int i = 0; i < nPes; i++) {

				String agentName = "";			// the name of a agent to which this task is allocated.
				String typeOfPlanElement = "";

				PlanElement pi = (PlanElement) peIter.next();

				if (pi instanceof Allocation)
				{
					Allocation alloc = (Allocation) pi;
					Asset asset = alloc.getAsset() ;
					if (asset instanceof Organization)
					{
						agentName = ((Organization) asset).getMessageAddress().getAddress();
					}else {
						agentName = asset.getName();
					}

					typeOfPlanElement = "Allocation";
				} 
				else if (pi instanceof Expansion)	{ typeOfPlanElement = "Expansion"; }
				else if (pi instanceof Aggregation) { typeOfPlanElement = "Aggregation"; }
				else if (pi instanceof Disposition) { typeOfPlanElement = "Disposition"; }
				else { typeOfPlanElement = "theOthers"; }

				Task ti = (Task) pi.getTask();
				
				PrepositionalPhrase pp = ti.getPrepositionalPhrase("OfType");
				String oftype = null;

				if (pp != null)
				{
					oftype = (String) pp.getIndirectObject();
				} else {
					myLoggingService.shout ("null Prepositional Phrase OfType" );
					continue;
				}

				// refill 
				PrepositionalPhrase pp2 = ti.getPrepositionalPhrase("Refill");
				String refill = "Refill";
				if (pp2 == null)  {		refill = "null";		} 

				// maintaining 
				PrepositionalPhrase pp3 = ti.getPrepositionalPhrase("Maintaining");
				String nomenclature = null;
				if (pp3 != null)
				{
					nomenclature = ((MaintainedItem) pp3.getIndirectObject()).getNomenclature();
				} else {
					myLoggingService.shout ("null Prepositional Phrase Maintaining" );
					continue;
				}

				Verb v = ti.getVerb();

				double start_time=0, start_time2=0;
				double end_time=0, end_time2=0;
				double qty = 0, rate = 0, rarConfidence=-1,earConfidence=-1;
				String rarsuccess=" ",earsuccess=" ";
				double rarQuantity = 0;	long rarEndTime = 0;
				double earQuantity = 0;	long earEndTime = 0;
				

				AllocationResult rar=null, ear=null;

					start_time = (long) (ti.getPreferredValue(AspectType.START_TIME) / 86400000);
//   					start_time2 = ti.getPreference(AspectType.START_TIME).getScoringFunction().getBest().getValue();
	
//					end_time = (long) (ti.getPreferredValue(AspectType.END_TIME) / 86400000) - baseTime;
					end_time = (long) (ti.getPreferredValue(AspectType.END_TIME) / 86400000);
//					end_time2 = ti.getPreference(AspectType.END_TIME).getScoringFunction().getBest().getValue();
						
					if (v.equals("Supply")||v.equals("ForecastDemand"))
					{
						qty = ti.getPreferredValue(AspectType.QUANTITY);
					} 
					else if (v.equals("ProjectSupply"))
					{
						AspectRate aspectrate = (AspectRate) ti.getPreference(AlpineAspectType.DEMANDRATE).getScoringFunction().getBest().getAspectValue(); 

						if (oftype.equals("BulkPOL"))
						{
							FlowRate flowrate = (FlowRate) aspectrate.rateValue();
							rate = flowrate.getGallonsPerDay();  // from LogSupplyProjector.java
						} else {
							CountRate countrate = (CountRate) aspectrate.rateValue();
							rate = countrate.getEachesPerDay();  // from LogSupplyProjector.java
						}

						qty = rate*(end_time-start_time)/(double) org.cougaar.glm.plugins.TimeUtils.MSEC_PER_DAY;
					}


						if ((rar = pi.getReportedResult())!=null)
						{
							if (rar.isSuccess())	{ rarsuccess = "success"; } else { rarsuccess = "fail";}
							rarConfidence = rar.getConfidenceRating();
/*							
							AspectValue [] av = rar.getAspectValueResults();
							int [] at = rar.getAspectTypes();

							int s = av.length;
							for (int k=0; k<s ; k++ )
							{
								System.out.println(av[k]+","+at[k]+","+rar.getAspectTypeFromArray(at[k])+","+rar.getAspectTypeFromArray(k));
							}		
*/
							if ((pi instanceof Allocation) && v.equals("Supply"))
							{
								AspectValue avQ = rar.getAspectValue(AspectType.QUANTITY); 
								AspectValue avE = rar.getAspectValue(AspectType.END_TIME);
								rarQuantity = avQ.doubleValue();
								rarEndTime = avE.longValue()/86400000;
							}
						}

						if ((ear = pi.getEstimatedResult())!=null)
						{
							if (ear.isSuccess())	{ earsuccess = "success"; } else { earsuccess = "fail";}
							earConfidence = ear.getConfidenceRating();
							if ((pi instanceof Allocation) && v.equals("Supply"))
							{
								AspectValue avQ = ear.getAspectValue(AspectType.QUANTITY); 
								AspectValue avE = ear.getAspectValue(AspectType.END_TIME);
								earQuantity = avQ.doubleValue();
								earEndTime = avE.longValue()/86400000;
							}						
						}
	
				UID uid = ti.getUID();

				long commitmentTime = 0;

				Date commitmentDate = ti.getCommitmentDate();

				if (commitmentDate != null)		{
					commitmentTime = commitmentDate.getTime()/86400000;					
				} 

				try
				{
//					rst.write(typeOfPlanElement+","+refill+","+oftype+","+modifier+","+nowTime+","+ checkingTime+","+uid.toString() + ","+v.toString()+","+agentName+","+rarsuccess +","+ rarConfidence+","+ earsuccess +","+ earConfidence+"\n");
					rst.write(typeOfPlanElement+"\t"+refill+"\t"+oftype+"\t"+modifier+"\t"+nowTime+"\t"+ checkingTime+"\t"+uid.toString() + "\t"
								+ v.toString()+"\t"+nomenclature+"\t"+qty+"\t"+rate+"\t"+start_time+"\t"+end_time+"\t"+agentName+"\t"+rarsuccess +"\t"
								+ rarConfidence +"\t"+ rarQuantity+"\t"+rarEndTime+"\t"+earsuccess +"\t"+ earConfidence+"\t"
								+ earQuantity +"\t"+earEndTime+"\t"+ti.getUID().getOwner() +"\t"+ cluster+"\t"+ commitmentTime+"\n");
					rst.flush();
				}
				catch (java.io.IOException ioexc)
				{
					System.err.println ("can't write file, io error" );
			    }					
			} // for
	}

	private void printOut(int nTasks, Iterator taskIter, String modifier, long nowTime)
	{
			long checkingTime = System.currentTimeMillis();
			for (int i = 0; i < nTasks; i++) {
				Task ti = (Task)taskIter.next();

				// if the task does not have ofType, then just skip.
				PrepositionalPhrase pp = ti.getPrepositionalPhrase("OfType");
				String oftype = null;
				if (pp != null)	{		
					oftype = (String) pp.getIndirectObject();		
				} else {
					myLoggingService.shout ("null Prepositional Phrase" );
					continue;
				}

				PrepositionalPhrase pp2 = ti.getPrepositionalPhrase("Refill");
				String refill = "Refill";

				if (pp2 == null)	{			refill = "null";			} 

				PrepositionalPhrase pp3 = ti.getPrepositionalPhrase("Maintaining");
				String nomenclature = null;

				if (pp3 != null)
				{
					nomenclature = ((MaintainedItem) pp3.getIndirectObject()).getNomenclature();
				} else {
					myLoggingService.shout ("null Prepositional Phrase Maintaining" );
					continue;
				}

				Verb v = ti.getVerb();

				long start_time=0, start_time2=0;
				long end_time=0, end_time2=0;
				double qty = 0, rate = 0, rarConfidence=-1, earConfidence=-1;
				String rarsuccess=" ",earsuccess=" ";
				AllocationResult rar=null, ear=null;
				double rarQuantity = 0;	long rarEndTime = 0;
				double earQuantity = 0;	long earEndTime = 0; long commitmentTime = 0;
				String agentName = "";

//				start_time = (long) (ti.getPreferredValue(AspectType.START_TIME) / 86400000) - baseTime;
				start_time = (long) (ti.getPreferredValue(AspectType.START_TIME) / 86400000);
//   			start_time2 = ti.getPreference(AspectType.START_TIME).getScoringFunction().getBest().getValue();
	
//				end_time = (long) (ti.getPreferredValue(AspectType.END_TIME) / 86400000) - baseTime;
				end_time = (long) (ti.getPreferredValue(AspectType.END_TIME) / 86400000);
//				end_time2 = ti.getPreference(AspectType.END_TIME).getScoringFunction().getBest().getValue();
				
				Date commitmentDate = ti.getCommitmentDate();

				if (commitmentDate != null)		{
					commitmentTime = commitmentDate.getTime()/86400000;					
				} 

				if (v.equals("Supply")||v.equals("ForecastDemand"))		{
					qty = ti.getPreferredValue(AspectType.QUANTITY);
				} 
				else if (v.equals("ProjectSupply"))		{
					AspectRate aspectrate = (AspectRate) ti.getPreference(AlpineAspectType.DEMANDRATE).getScoringFunction().getBest().getAspectValue(); 

					if (oftype.equals("BulkPOL"))
					{
						FlowRate flowrate = (FlowRate) aspectrate.rateValue();
						rate = flowrate.getGallonsPerDay();  // from LogSupplyProjector.java
					} else {
						CountRate countrate = (CountRate) aspectrate.rateValue();
						rate = countrate.getEachesPerDay();  // from LogSupplyProjector.java
					}

					qty = rate*(end_time-start_time)/(double) org.cougaar.glm.plugins.TimeUtils.MSEC_PER_DAY;
				}
				
				PlanElement planElement = null;
				if ((planElement = (PlanElement) ti.getPlanElement())!=null)
				{
					if (planElement instanceof Allocation)
					{
						Allocation alloc = (Allocation) planElement;
						Asset asset = alloc.getAsset() ;
						if (asset instanceof Organization)	{	agentName = ((Organization) asset).getMessageAddress().getAddress();
						}else {									agentName = asset.getName();											}
					} 

					if ((rar = ti.getPlanElement().getReportedResult())!=null)
					{
						if (rar.isSuccess())	{ rarsuccess = "success"; } else { rarsuccess = "fail";}
						rarConfidence = rar.getConfidenceRating();

							if ((planElement instanceof Allocation) && v.equals("Supply"))
							{
								AspectValue avQ = rar.getAspectValue(AspectType.QUANTITY); 
								AspectValue avE = rar.getAspectValue(AspectType.END_TIME);
								rarQuantity = avQ.doubleValue();
								rarEndTime = avE.longValue();
							}
					}
					if ((ear = ti.getPlanElement().getEstimatedResult())!=null)
					{
						if (ear.isSuccess())	{ earsuccess = "success"; } else { earsuccess = "fail";}
						earConfidence = ear.getConfidenceRating();
				
							if ((planElement instanceof Allocation) && v.equals("Supply"))
//							if (v.equals("Supply"))
							{
								AspectValue avQ = ear.getAspectValue(AspectType.QUANTITY); 
								AspectValue avE = ear.getAspectValue(AspectType.END_TIME);
								earQuantity = avQ.doubleValue();
								earEndTime = avE.longValue();
							}						
					}
				}
		
				UID uid = ti.getUID();
			
				try
				{
					rst.write("task\t"+refill+"\t"+oftype+"\t"+modifier+"\t"+nowTime+"\t"+ checkingTime+"\t"+ uid.toString()+"\t"+v.toString()
								 +"\t"+nomenclature+"\t"+qty+"\t"+rate+"\t"+start_time+"\t"+ end_time+"\t"+agentName+"\t"+rarsuccess +"\t"+ rarConfidence
								 +"\t"+ rarQuantity+"\t"+rarEndTime+"\t"+earsuccess+"\t"+ earConfidence+"\t"
								 +earQuantity+"\t"+earEndTime+"\t"+ti.getUID().getOwner() +"\t"+ cluster+"\t"+ commitmentTime+"\n");

					rst.flush();
				}
				catch (java.io.IOException ioexc)
				{
					System.err.println ("can't write file, io error" );
			    }					
			} // for
	}

	private	boolean getParametersWhichTurnsOnOrOffOutputFile() {

		Collection c = getParameters();

        Properties props = new Properties() ;
        // Iterate through the parameters
        int count = 0;
        for (Iterator iter = c.iterator() ; iter.hasNext() ;)
        {
            String s = (String) iter.next();
			if (!s.equalsIgnoreCase("true"))	{
				return false;
			}
//			break;
        }
		return true;
	}

	public boolean isOutputFileOn() {
		return OutputFileOn;
	}
}