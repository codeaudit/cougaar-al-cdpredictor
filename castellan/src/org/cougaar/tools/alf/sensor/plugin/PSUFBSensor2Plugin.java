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
import org.cougaar.core.util.XMLize;
import org.cougaar.core.util.XMLizable;
import org.cougaar.util.*;
import org.cougaar.multicast.AttributeBasedAddress;
import org.cougaar.planning.ldm.plan.*;
import org.cougaar.logistics.plugin.manager.LoadIndicator;

import org.cougaar.tools.alf.sensor.plugin.PSUSensorCondition;
import org.cougaar.tools.alf.sensor.*;
import org.cougaar.tools.alf.sensor.rbfnn.*;

import org.w3c.dom.Element;
import org.w3c.dom.Document;

import java.util.Iterator;
import java.util.Collection;
import java.util.Vector;

import java.io.*;

/* 
 *	programed by Yunho Hong
 *	August 21, 2002
 *	Pennsylvania State University
 */

public class PSUFBSensor2Plugin extends ComponentPlugin
{

	IncrementalSubscription taskSubscription;

	IncrementalSubscription loadIndicatiorSubscription;
	IncrementalSubscription internalStateSubscription;

	UnaryPredicate taskPredicate			= new UnaryPredicate()	{ 	public boolean execute(Object o) {  return (o instanceof Task); 	}   };
//	UnaryPredicate planelementPredicate		= new UnaryPredicate()	{ 	public boolean execute(Object o) {  return (o instanceof Task); 	}   };
	UnaryPredicate internalStatePredicate	= new UnaryPredicate()	{	public boolean execute(Object o) {  return (o instanceof InternalState);   }    };

	UnaryPredicate loadIndicatiorPredicate			
		= new UnaryPredicate()	{ 
			public boolean execute(Object o) {  
				if (o instanceof LoadIndicator)
				{
					LoadIndicator lindicator = (LoadIndicator) o;
					if ((lindicator.getAgentName()).equalsIgnoreCase(cluster))
					{
						return true;
					}
				}
				return false;   
			}    
		};

	AlarmService as;
    TriggerFlushAlarm alarm = null;

	BlackboardService bs;
   	UIDService uidservice;

   	String cluster;  // the current agent's name
	
	RbfRidgeRegression rbfnn = null;
	LoadIndicator loadIndicator = null;
	LFMessage lfMessage = null;

	double [] a;
	InternalState internalState = null;

	int curr_state = -1;

	long nextTime = 0;
	long unittime = 10000;
	long offsetTime = -1L;
	
	int previous_nTask = 0;
	int previous_nUnplannedTask = 0;

	java.io.BufferedWriter rst = null;
	boolean isTransporationAgent = false;
	
	boolean firstTime = true;

    public void setupSubscriptions()   {

        bs = getBlackboardService();
		cluster = getBindingSite().getAgentIdentifier().toString();

		internalStateSubscription 	= (IncrementalSubscription) bs.subscribe(internalStatePredicate);
		loadIndicatiorSubscription 	= (IncrementalSubscription) bs.subscribe(loadIndicatiorPredicate);
		taskSubscription		 	= (IncrementalSubscription) bs.subscribe(taskPredicate);

		uidservice =(UIDService) getServiceBroker().getService(this, UIDService.class, null);

		if (bs.didRehydrate() == false)
        {
			a = new double[6];
			rbfnn = new RbfRidgeRegression();			

			// num_hidden1, lamda1, increment1, count_limit1, dimension1
			rbfnn.setParameters(15, 10, 0.1, 20, 6);
			System.out.println("next check time " + nextTime);

			ConfigFinder finder = getConfigFinder();
			String inputName = "ulmodel.txt";

			try {

				if ( inputName != null && finder != null ) {

					File inputFile = finder.locateFile( inputName ) ;
					if ( inputFile != null && inputFile.exists() ) {
						System.out.println("Load Input model.");
						rbfnn.readModel(inputFile);
			        } else {
						System.out.println("Input model error.");
					}
				}

	        } catch ( Exception e ) {
		        e.printStackTrace() ;
			}

			internalState = new InternalState(1000, Integer.MAX_VALUE, uidservice.nextUID());

		    CommunityService communityService = (CommunityService) getBindingSite().getServiceBroker().getService(this, CommunityService.class, null);

			Collection alCommunities = communityService.listParentCommunities(getAgentIdentifier().toString(), "(CommunityType=AdaptiveLogistics)");				
			internalState.setalCommunities(alCommunities);
			bs.publishAdd(internalState);
			System.out.println("PSUFBSensor2Plugin start at " + cluster); 
		}

		bs.setShouldBePersisted(false);
		as = getAlarmService() ;
    }
	
	public void execute()
    {
        Iterator iter;

        if (alarm != null) alarm.cancel();

        if (loadIndicator == null)
		{
	        for (iter = loadIndicatiorSubscription.getAddedCollection().iterator() ; iter.hasNext() ;)
	        {
		        loadIndicator = (LoadIndicator) iter.next();
				break;
			}
		}

		if (internalState == null)
		{
	        for (iter = internalStateSubscription.getAddedCollection().iterator() ; iter.hasNext() ;)
	        {
		        internalState = (InternalState) iter.next();
				// debug
				internalState.show();
				break;
			}
		} 

		if (offsetTime == -1L)
		{
			for (iter = taskSubscription.getAddedCollection().iterator() ; iter.hasNext() ;)
	        {
		        Task t = (Task) iter.next();
				Verb v = t.getVerb();
				if (cluster.equalsIgnoreCase("CONUSGround") || cluster.equalsIgnoreCase("GlobalAir")     ||
					cluster.equalsIgnoreCase("GlobalSea")   || cluster.equalsIgnoreCase("PlanePacker")   ||
					cluster.equalsIgnoreCase("ShipPacker")  || cluster.equalsIgnoreCase("TheaterGround") )
				{
					if (!v.equals("ReportForDuty") && !v.equals("ReportForService") )
					{
						offsetTime = System.currentTimeMillis();
						nextTime = offsetTime + unittime;
//						nextTime = unittime;
//						sendLoadIndicator(0, LoadIndicator.NORMAL_LOAD);
//						sendLoadIndicator(0, LoadIndicator.NORMAL_LOAD);
						reportBlackboardState(0, 0, 0);
						curr_state = -1;

//						checkTaskSubscription(0,taskSubscription);
						checkTaskSubscription(offsetTime,taskSubscription);
						isTransporationAgent = true;
						System.out.println("Start to checking blackboard at " + cluster); 
						break;
					}

				} else {
					if (v.equals("GetLogSupport"))
					{
						offsetTime = System.currentTimeMillis();
						nextTime = offsetTime + unittime;
//						nextTime = unittime;
//						checkTaskSubscription(0,taskSubscription);
//						sendLoadIndicator(0, LoadIndicator.NORMAL_LOAD);
						reportBlackboardState(0, 0, 0);
						checkTaskSubscription(offsetTime,taskSubscription);
//						sendLoadIndicator(0, LoadIndicator.NORMAL_LOAD);
						curr_state = -1;
						System.out.println("Start to checking blackboard at " + cluster); 
						break;
					}
				}
			}
		} 
		else 
		{
//		    long nowTime = System.currentTimeMillis()-offsetTime; // this is for the falling behind sensor
		    long nowTime = System.currentTimeMillis(); // in order to get proper timing.

//			if (nowTime >= nextTime)	  
//			{		  
//				nextTime =  (nowTime - nowTime%unittime) + unittime;	

				checkTaskSubscription(nowTime, taskSubscription);
//			}
		}

		alarm = new TriggerFlushAlarm(  currentTimeMillis() + 60000 );
        as.addAlarm(alarm) ;
    }

	private void checkTaskSubscription(long nowTime, IncrementalSubscription taskSubscription)
	{
		if (!taskSubscription.isEmpty()) {
	
			// examine tasks
			int nUnplannedTasks = 0;
		    int nUnestimatedTasks = 0;
//		    int nFailedTasks = 0;
			int nUnconfidentTasks = 0;

//////////////
			int nPSI=0;
			int nPSO=0;
			int nPW=0;
			int nSI=0;
			int nSO=0;
			int nW=0;
			int nOthers=0;
//////////////

			int nTasks = taskSubscription.size();
		    Iterator taskIter = taskSubscription.iterator();
			
			for (int i = 0; i < nTasks; i++) {
				Task ti = (Task)taskIter.next();
///////////
/*
				if (!isTransporationAgent)
				{
					Verb v = ti.getVerb();
	
					if (v.equals("ProjectSupply"))			{	
						if (ti.getUID().getOwner().equalsIgnoreCase(cluster))	{		nPSI++;		} 
						else													{		nPSO++;		}
					}
					else if (v.equals("Supply"))			{	
						if (ti.getUID().getOwner().equalsIgnoreCase(cluster))	{		nSI++;		} 
						else													{		nSO++;		}
					}	
					else if (v.equals("ProjectWithdraw"))	{	nPW++;	}
					else if (v.equals("Withdraw"))			{	nW++;	}
				}
*/
////////////////////

				PlanElement pe = ti.getPlanElement();
				if (pe != null) {
/*
						AllocationResult peEstResult = pe.getEstimatedResult();
						if (peEstResult != null) {
							 double estConf = peEstResult.getConfidenceRating();
//				             if (peEstResult.isSuccess()) {
									if (estConf > 0.99) {
					                  // 100% success
									} else {					  	
								      nUnconfidentTasks++;
									}
//							 } else {
//				                nFailedTasks++;   // Most of the case is success. No meaning
//			                 }
						} else {
				          nUnestimatedTasks++;
					    }
*/
				} else {
				    nUnplannedTasks++;
				}
			} // for

//			checkFallingBehindness(nowTime, nTasks, nUnconfidentTasks, nFailedTasks, nUnestimatedTasks, nUnplannedTasks);
			checkFallingBehindness(nowTime, nTasks, nUnconfidentTasks, nUnestimatedTasks, nUnplannedTasks, nPSI, nPSO, nSI, nSO, nPW, nW);
		}
	}


//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
//	public void checkFallingBehindness(long nowTime, int nTasks, int nUnconfidentTasks, int nFailedTasks, int nUnestimatedTasks, int nUnplannedTasks) 
	public void checkFallingBehindness(long nowTime, int nTasks, int nUnconfidentTasks, int nUnestimatedTasks, int nUnplannedTasks, int nPSI, int nPSO, int nSI, int nSO, int nPW, int nW)
	{

		int newlydetectedstate = internalState.currentstate;

//			try
//			{
//				System.out.println("\n"+nowTime+","+ nTasks+","+ nUnconfidentTasks+","+ nFailedTasks+","+ nUnestimatedTasks+","+ nUnplannedTasks);
//				rst.write(nowTime+","+ nTasks+","+ nUnconfidentTasks+","+ nFailedTasks+","+ nUnestimatedTasks+","+ nUnplannedTasks+"\n");
//				rst.write(nowTime+","+ nTasks+","+ nUnconfidentTasks+","+ nUnestimatedTasks+","+ nUnplannedTasks+","+nPSI+","+ nPSO+","+ nSI+","+ nSO+","+ nPW+","+ nW+"\n");
//				rst.flush();

	
//				if (firstTime == true)	{	reportBlackboardState(0, nTasks, nowTime);		firstTime = false;	} 
//				else					{	reportBlackboardState(1, nTasks, nowTime);							}

				if (previous_nTask != nTasks || previous_nUnplannedTask != nUnplannedTasks )
				{
					reportBlackboardState(1, nTasks, nowTime);
					previous_nTask = nTasks;
					previous_nUnplannedTask = nUnplannedTasks;
					System.out.println("\n[PSUFB2] "+cluster+ " : " +nowTime+","+ nTasks+","+ nUnconfidentTasks+","+ nUnestimatedTasks+","+ nUnplannedTasks+","+nPSI+","+ nPSO+","+ nSI+","+ nSO+","+ nPW+","+ nW+"\n");
				}

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
//			}
//			catch (java.io.IOException ioexc)
//			{
//				System.err.println ("can't write file, io error" );
//		    }					
	}

	// send load indicator to the transcom
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

	// send current blackboard state to the transcom
	public void reportBlackboardState(int mode, int num_of_tasks, long time) 
	{

		if (mode == 0)
		{
			lfMessage = new LFMessage(this.getClass(), cluster, uidservice.nextUID(), num_of_tasks, time);
			for (Iterator iterator = internalState.alCommunities.iterator(); iterator.hasNext();) {
				String community = (String) iterator.next();
				lfMessage.addTarget(new AttributeBasedAddress(community,"Role","AdaptiveLogisticsManager"));
				bs.publishAdd(lfMessage);
			}
		} else {
	        lfMessage.setNum_of_tasks(num_of_tasks);
			lfMessage.setTime(time);
			bs.publishChange(lfMessage);
		}	

//		System.out.println(cluster + ": sending current state to " + loadIndicator.getTargets()+ ", "+ lfMessage.getNum_of_tasks()+","+lfMessage.getTime()+","+offsetTime);
	}

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
			System.out.println("expire()");
			execute();
			if (curr_state == 1)
			{
//	            sendLoadIndicator(1, LoadIndicator.NORMAL_LOAD);
			}

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
}