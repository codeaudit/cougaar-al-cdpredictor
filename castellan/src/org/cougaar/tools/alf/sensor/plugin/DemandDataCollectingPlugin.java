/*
  * <copyright>
  *  Copyright 2002 (Penn State University and Intelligent Automation, Inc.)
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

import org.w3c.dom.Element;
import org.w3c.dom.Document;
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

	UnaryPredicate taskPredicate = new UnaryPredicate()	{ 	
			public boolean execute(Object o) {  
				if (o instanceof Task)
				{
					Task tempTask = (Task) o;
					Verb verb = tempTask.getVerb();
					if (verb.equals("GetLogSupport"))
					{
						return true;
					}

					if (verb.equals("Supply")||verb.equals("ProjectSupply"))
					{
						PrepositionalPhrase pp = null;
						if ((pp = tempTask.getPrepositionalPhrase("OfType"))!=null)
						{
//							if (pp.getIndirectObject().getClass().getName().equalsIgnoreCase("java.lang.String"))
//							{
								String s = (String) pp.getIndirectObject();
//								System.out.print(s+" ");
	
//								if (pp.getIndirectObject().getClass().getName().equalsIgnoreCase("org.cougaar.glm.ldm.asset.BulkPOL"))
								if (s.equalsIgnoreCase("BulkPOL")||s.equalsIgnoreCase("Ammunition"))
								{
									return true;
								} 
//							}
						}
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
						PrepositionalPhrase pp = null;
						if ((pp = tempTask.getPrepositionalPhrase("OfType"))!=null)
						{
								String s = (String) pp.getIndirectObject();

								if (s.equalsIgnoreCase("BulkPOL")||s.equalsIgnoreCase("Ammunition"))
								{
									return true;
								} 
						}
					}
				}

				return false; 	
			} 
		};
/*
	UnaryPredicate oplanPredicate = new UnaryPredicate()	{ 	
			public boolean execute(Object o) {  
				if (o instanceof org.cougaar.glm.ldm.oplan.Oplan)
				{
					return true;
				}
				return false; 	
			} 
		};
*/
//	AlarmService as;
//  TriggerFlushAlarm alarm = null;

	BlackboardService bs;
   	UIDService uidservice;

   	String cluster;  // the current agent's name
	
	double [] a;

	int curr_state = -1;

	long nextTime = 0;
	long offsetTime = -1;  // Actual runtime
//	long baseTime = 13005; // August 10th 2005 
	long baseTime = 12974; // July 10th 2005 
	boolean oplan_is_not_detected = true;

	java.io.BufferedWriter rst = null;

    public void setupSubscriptions()   {

        bs = getBlackboardService();
		cluster = agentId.toString(); // the cluster where this Plugin is running.

		taskSubscription		 	= (IncrementalSubscription) bs.subscribe(taskPredicate);
//		planelementSubscription     = (IncrementalSubscription) bs.subscribe(pePredicate);
//		oPlanSubscription		 	= (IncrementalSubscription) bs.subscribe(oplanPredicate);

		try
		{
			rst = new java.io.BufferedWriter ( new java.io.FileWriter(cluster+System.currentTimeMillis()+".txt", true ));
		}
		catch (java.io.IOException ioexc)
	    {
		    System.err.println ("can't write file, io error" );
	    }						

		System.out.println("DemandDataCollectingPlugin start at " + cluster); 
		bs.setShouldBePersisted(false);
/*
		try
		{
			DateFormat df =  DateFormat.getInstance();
			Date date1 = df.parse("July 11, 2005,  00:00:00 GMT"); // "07/11/2005 19:01:15"
			System.out.println(date1);
			Calendar cDay = Calendar.getInstance();
			cDay.setTime(date1);		
			System.out.println("first cDay = " + cDay.getTimeInMillis());		
		}
		catch (ParseException per)
		{
			System.out.println("parsing errors");
		}
*/		
//		as = getAlarmService() ;
    }
	
	public void execute()
    {
        Iterator iter;
/*
		if (oplan_is_not_detected)
		{
			if (!oPlanSubscription.isEmpty()) {
				for (iter = oPlanSubscription.getAddedCollection().iterator() ; iter.hasNext() ;)
				{
					Oplan oplan = (Oplan) iter.next();					
					Date date = oplan.getCday();
					System.out.println("cDay = " + date);
					Calendar cDay = Calendar.getInstance();
					cDay.setTime(date);
					baseTime =cDay.getTimeInMillis(); 
				}
			}

			oplan_is_not_detected = false;
		}
*/
//        if (alarm != null) alarm.cancel();

/*		if (offsetTime == -1)
		{
			for (iter = taskSubscription.getAddedCollection().iterator() ; iter.hasNext() ;)	// add 
	        {
		        Task t = (Task) iter.next();
				Verb v = t.getVerb();
				if (cluster.equalsIgnoreCase("CONUSGround") || cluster.equalsIgnoreCase("GlobalAir")     ||
					cluster.equalsIgnoreCase("GlobalSea")   || cluster.equalsIgnoreCase("PlanePacker")   ||
					cluster.equalsIgnoreCase("ShipPacker")  || cluster.equalsIgnoreCase("TheaterGround") )
				{
					if (!v.equals("ReadyForDuty") && !v.equals("ReadyForService") )
					{
						offsetTime = System.currentTimeMillis();
	//					nextTime = 1000;
	//					sendLoadIndicator(0, LoadIndicator.NORMAL_LOAD);
						curr_state = -1;

						checkTaskSubscription(0,taskSubscription);

						break;
					}

				} else {
					if (v.equals("GetLogSupport"))
					{
						offsetTime = System.currentTimeMillis();
//						nextTime = 1000;
//						sendLoadIndicator(0, LoadIndicator.NORMAL_LOAD);
//						curr_state = -1;
						break;
					}
				}
			}
		} 
		else 
		{*/
		    long nowTime = (currentTimeMillis()/ 86400000) - baseTime; // long nowTime = System.currentTimeMillis()-offsetTime;
			checkTaskSubscription(nowTime, taskSubscription);
//			checkPeSubscription(nowTime, planelementSubscription);

//		}

//		alarm = new TriggerFlushAlarm( currentTimeMillis() + 60000 );
//        as.addAlarm(alarm) ;
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
//			System.out.println("planelementSubscription is not Empty");
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
		
			for (int i = 0; i < nPes; i++) {
				
				PlanElement pi = (PlanElement)peIter.next();

				Task ti = (Task) pi.getTask();

				PrepositionalPhrase pp = ti.getPrepositionalPhrase("OfType");
				String oftype = null;

				if (pp != null)
				{
					oftype = (String) pp.getIndirectObject();
				} else {
					System.out.println ("null Prepositional Phrase" );
					continue;
				}

				PrepositionalPhrase pp2 = ti.getPrepositionalPhrase("Refill");
				String refill = "Refill";

				if (pp2 == null)
				{
					refill = "null";
				} 

				Verb v = ti.getVerb();

				double start_time=0, start_time2=0;
				double end_time=0, end_time2=0;
				double qty = 0, rate = 0, rarConfidence=-1,earConfidence=-1;
				String rarsuccess=" ",earsuccess=" ";
				AllocationResult rar=null, ear=null;

				if (v.equals("GetLogSupport")==false)
				{
//					System.out.println(v.toString());

//					start_time = ti.getPreferredValue(AspectType.START_TIME);
//   					start_time2 = ti.getPreference(AspectType.START_TIME).getScoringFunction().getBest().getValue();
	
//					end_time = ti.getPreferredValue(AspectType.END_TIME);
//					end_time2 = ti.getPreference(AspectType.END_TIME).getScoringFunction().getBest().getValue();
						
/*					if (v.equals("Supply"))
/					{
						qty = ti.getPreferredValue(AspectType.QUANTITY);
					} 
					else if (v.equals("ProjectSupply"))
					{
						AspectRate aspectrate = (AspectRate) ti.getPreference(AlpineAspectType.DEMANDRATE).getScoringFunction().getBest().getAspectValue(); 

						FlowRate flowrate = (FlowRate) aspectrate.rateValue();

						rate = flowrate.getGallonsPerDay();  // from LogSupplyProjector.java
						qty = rate*(end_time-start_time)/(double) org.cougaar.glm.plugins.TimeUtils.MSEC_PER_DAY;
					}
*/					
//					if (ti.getPlanElement()!=null)
//					{
						if ((rar = pi.getReportedResult())!=null)
						{
							if (rar.isSuccess())	{ rarsuccess = "success"; } else { rarsuccess = "fail";}
							rarConfidence = rar.getConfidenceRating();
						}
						if ((ear = pi.getEstimatedResult())!=null)
						{
							if (ear.isSuccess())	{ earsuccess = "success"; } else { earsuccess = "fail";}
							earConfidence = ear.getConfidenceRating();
						}
//					}
				}
		
				UID uid = ti.getUID();
			
				try
				{
					rst.write(refill+","+oftype+","+modifier+","+nowTime+","+ uid.toString() + ","+rarsuccess +","+ rarConfidence+","+ earsuccess +","+ earConfidence+"\n");
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
			for (int i = 0; i < nTasks; i++) {
				Task ti = (Task)taskIter.next();

				PrepositionalPhrase pp = ti.getPrepositionalPhrase("OfType");

				String oftype = null;

				if (pp != null)
				{
					oftype = (String) pp.getIndirectObject();
				} else {
					System.out.println ("null Prepositional Phrase" );
					continue;
				}

				PrepositionalPhrase pp2 = ti.getPrepositionalPhrase("Refill");
				String refill = "Refill";

				if (pp2 == null)
				{
					refill = "null";
				} 

				Verb v = ti.getVerb();

				long start_time=0, start_time2=0;
				long end_time=0, end_time2=0;
				double qty = 0, rate = 0, rarConfidence=-1, earConfidence=-1;
				String rarsuccess=" ",earsuccess=" ";
				AllocationResult rar=null, ear=null;

				if (v.equals("GetLogSupport")==false)
				{
//					System.out.println(v.toString());

					start_time = (long) (ti.getPreferredValue(AspectType.START_TIME) / 86400000) - baseTime;
//   					start_time2 = ti.getPreference(AspectType.START_TIME).getScoringFunction().getBest().getValue();
	
					end_time = (long) (ti.getPreferredValue(AspectType.END_TIME) / 86400000) - baseTime;
//					end_time2 = ti.getPreference(AspectType.END_TIME).getScoringFunction().getBest().getValue();
						
					if (v.equals("Supply"))
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
					
					if (ti.getPlanElement()!=null)
					{
						if ((rar = ti.getPlanElement().getReportedResult())!=null)
						{
							if (rar.isSuccess())	{ rarsuccess = "success"; } else { rarsuccess = "fail";}
							rarConfidence = rar.getConfidenceRating();
						}
						if ((ear = ti.getPlanElement().getEstimatedResult())!=null)
						{
							if (ear.isSuccess())	{ earsuccess = "success"; } else { earsuccess = "fail";}
							earConfidence = ear.getConfidenceRating();
						}
					}
				}
		
				UID uid = ti.getUID();
			
				try
				{
					rst.write(refill+","+oftype+","+modifier+","+nowTime+","+ uid.toString()+","+v.toString()+","+qty+","+rate+","+start_time+","+ end_time+","+rarsuccess +","+ rarConfidence+","+ earsuccess +","+ earConfidence+","+ti.getUID().getOwner() +","+ cluster+"\n");
					rst.flush();
				}
				catch (java.io.IOException ioexc)
				{
					System.err.println ("can't write file, io error" );
			    }					
			} // for
	}


//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
//	public void checkFallingBehindness(long nowTime, int nTasks, int nUnconfidentTasks, int nFailedTasks, int nUnestimatedTasks, int nUnplannedTasks) 
	public void checkFallingBehindness(long nowTime, int nTasks, int nUnconfidentTasks, int nUnestimatedTasks, int nUnplannedTasks, int nPSI, int nPSO, int nSI, int nSO, int nPW, int nW)
	{

//		int newlydetectedstate = internalState.currentstate;

			try
			{
//				System.out.println("\n"+nowTime+","+ nTasks+","+ nUnconfidentTasks+","+ nFailedTasks+","+ nUnestimatedTasks+","+ nUnplannedTasks);
//				rst.write(nowTime+","+ nTasks+","+ nUnconfidentTasks+","+ nFailedTasks+","+ nUnestimatedTasks+","+ nUnplannedTasks+"\n");
				rst.write(nowTime+","+ nTasks+","+ nUnconfidentTasks+","+ nUnestimatedTasks+","+ nUnplannedTasks+","+nPSI+","+ nPSO+","+ nSI+","+ nSO+","+ nPW+","+ nW+"\n");
				rst.flush();
/*
				a[0] = (double) nowTime;
				a[1] = (double) nTasks;
				a[2] = (double) nUnconfidentTasks;
				a[3] = (double) nFailedTasks;
				a[4] = (double) nUnestimatedTasks;
				a[5] = (double) nUnplannedTasks;

				double y = rbfnn.f(a);

				if (y > 0)
				{
					if (curr_state == -1)
					{
						System.out.println("SEVERE_LOAD");
						sendLoadIndicator(1, LoadIndicator.SEVERE_LOAD); 	
						curr_state = 1;
					}

				} else {
					if (curr_state == 1)
					{
						System.out.println("NORMAL_LOAD");
						sendLoadIndicator(1, LoadIndicator.NORMAL_LOAD); 				
						curr_state = -1;
					}
				}
*/
			}
			catch (java.io.IOException ioexc)
			{
				System.err.println ("can't write file, io error" );
		    }					
	}

/*
	public void sendLoadIndicator(int mode, String loadlevel) 
	{

		if (mode == 0)
		{
			loadIndicator = new LoadIndicator(this.getClass(), cluster, uidservice.nextUID(), loadlevel);
			for (Iterator iterator = internalState.alCommunities.iterator(); iterator.hasNext();) {
				String community = (String) iterator.next();
				loadIndicator.addTarget(new AttributeBasedAddress(community,"Role","AdaptiveLogisticsManager"));
				bs.publishAdd(loadIndicator);
			}
		} else {
	        loadIndicator.setLoadStatus(loadlevel);
			bs.publishChange(loadIndicator);
		}	

		System.out.println(cluster + ": adding loadIndicator to be sent to " + loadIndicator.getTargets());
	}
*/
/*
	class TriggerFlushAlarm implements PeriodicAlarm
    {
        public TriggerFlushAlarm(long expTime)
        {
            this.expTime = expTime;
        }

        public void reset(long currentTime)
        {
            expTime = currentTime + delay;
            expired = false;
        }

        public long getExpirationTime()
        {
            return expTime;
        }

        public void expire()
        {
            expired = true;
			getBlackboardService().openTransaction();
//            sendLoadIndicator(1, LoadIndicator.NORMAL_LOAD);
			getBlackboardService().closeTransaction();

        }

        public boolean hasExpired()
        {
            return expired;
        }

        public boolean cancel()
        {
            boolean was = expired;
            expired = true;
            return was;
        }

        boolean expired = false;
        long expTime;
        long delay = 60000;
    }
*/
}