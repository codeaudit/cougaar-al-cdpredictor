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
import org.cougaar.core.service.*;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.util.UID;
import org.cougaar.core.agent.ClusterIdentifier;
import org.cougaar.core.adaptivity.OMCRangeList;
import org.cougaar.core.service.community.CommunityService;
import org.cougaar.core.util.UniqueObject;
import org.cougaar.core.util.XMLize;
import org.cougaar.core.util.XMLizable;

import org.w3c.dom.Element;
import org.w3c.dom.Document;

import org.cougaar.multicast.AttributeBasedAddress;

import org.cougaar.planning.ldm.plan.*;
import org.cougaar.util.*;
import org.cougaar.logistics.plugin.manager.LoadIndicator;

import java.util.Iterator;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Arrays;

import org.cougaar.tools.alf.sensor.plugin.PSUSensorCondition;
import org.cougaar.tools.alf.sensor.*;


import java.io.*;
import java.util.Vector;
import java.util.Enumeration;

/* 
	programed by Yunho Hong
	July 23, 2002
	Pennsylvania State University
*/

public class PSUFBSensor2Plugin extends ComponentPlugin
{

	IncrementalSubscription taskSubscription;
	IncrementalSubscription timeIndicatiorSubscription;
	IncrementalSubscription loadIndicatiorSubscription;
	IncrementalSubscription internalStateSubscription;

	UnaryPredicate taskPredicate					= new UnaryPredicate()	{ public boolean execute(Object o) {  return o instanceof Task;  }   };
    UnaryPredicate timeIndicatiorPredicate			= new UnaryPredicate()	{ public boolean execute(Object o) {  return o instanceof StartIndicator;   }    };
    
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

    UnaryPredicate internalStatePredicate			= new UnaryPredicate()	{ public boolean execute(Object o) {  return o instanceof InternalState;   }    };

	BlackboardService bs;
    BlackboardTimestampService bts; 
    UIDService uidservice;

    String cluster;  // the current agent's name

	LoadIndicator loadIndicator = null;
	Hashtable lookuptable = null;

	long starttime = -1L;
	private int Hongfbtype = 2;
	private String [] fbresult = null;

	InternalState internalState = null;

    public void setupSubscriptions()   {

		bts = ( BlackboardTimestampService ) getServiceBroker().getService( this, BlackboardTimestampService.class, null ) ;
        bs = getBlackboardService();
		cluster = getBindingSite().getAgentIdentifier().toString();

	    fbresult = new String[3];
	    fbresult[0] = LoadIndicator.NORMAL_LOAD; // normal
	    fbresult[1] = LoadIndicator.MODERATE_LOAD; // mild falling behind
		fbresult[2] = LoadIndicator.SEVERE_LOAD; // severe falling behind

		internalStateSubscription = (IncrementalSubscription) bs.subscribe(internalStatePredicate);
		loadIndicatiorSubscription = (IncrementalSubscription) bs.subscribe(loadIndicatiorPredicate);
		taskSubscription = (IncrementalSubscription) bs.subscribe(taskPredicate);
		timeIndicatiorSubscription = (IncrementalSubscription) bs.subscribe(timeIndicatiorPredicate);

		// Read threshold values and publishAdd the lookuptable for future hydration.
		getThresholdforLookupTable(); // in this function fbsensor will be constructed.

		uidservice =(UIDService) getServiceBroker().getService(this, UIDService.class, null);

		if (bs.didRehydrate() == false)
        {
			internalState = new InternalState(1000, Integer.MAX_VALUE, uidservice.nextUID());
			if (lookuptable == null)
			{
				internalState.over = true;
			}
			bs.publishAdd(internalState);
			if(!internalState.over) { System.out.println("PSUFBSensor2Plugin start at " + cluster); }
		}

		bs.setShouldBePersisted(false);
    }

	// read user defined threshold values from a file
	private void getThresholdforLookupTable() {

		ConfigFinder finder = getConfigFinder();
		String inputName = "lookup.txt";
       
		try {

			if ( inputName != null && finder != null ) {

				File inputFile = finder.locateFile( inputName ) ;

                if ( inputFile != null && inputFile.exists() ) {
                    
					java.io.BufferedReader input_file = new java.io.BufferedReader ( new java.io.FileReader(inputFile));					

					read_lookupTable(input_file);

					input_file.close();
                }

            }
        } catch ( Exception e ) {
            e.printStackTrace() ;
        }
	}

	// 	Read threshold values specified by user and put them into lookup tables
	private void read_lookupTable(java.io.BufferedReader input_stream) {

		String current_agent = null;
		Hashtable LookupTable = null;
		String s = null;
		Long From; 
		int FbLevel = 0;
		float LLThreshold = 0, ULThreshold = 0;
		int lt = 0; 
		int st = 0;

		try
		{
			while ((s = input_stream.readLine()) != null)
			{
				int is = 0;
				int ix = s.indexOf(" ");

				String temp_string = s.substring(is,ix);
				
				if(temp_string.charAt(0) == '#'){   // # represents Comment

					continue;

				} else if (temp_string.equalsIgnoreCase("@agent"))	{

					if (current_agent != null)
					{
						lookuptable = LookupTable;
						break;  
					}
					
					st = ix+1;
					if (cluster.equalsIgnoreCase(s.substring(st).trim()))  // 'cluster' is the name of the agent in which this plugin is running.
					{
						current_agent = cluster;
						LookupTable	= new Hashtable();
						// debug
//						System.out.println("agent : " + current_agent);
					}
					continue;
				} 
				else if (current_agent == null)
				{
					continue;
				}

				// from	 (specific time)
				From = new Long(temp_string.trim());

				// Level
				is = ix + 1;
				ix = s.indexOf(" ",is);	
				FbLevel = (Integer.valueOf(s.substring(is,ix).trim())).intValue();

				// Lower limit threshold value
				is = ix + 1;
				ix = s.indexOf(" ",is);	
				LLThreshold = (Float.valueOf(s.substring(is,ix).trim())).floatValue();

				// Upper limit threshold value
				st = ix+1;
				ULThreshold = (Float.valueOf(s.substring(st).trim())).floatValue();
				
				// debug
//				System.out.println("lookup: " + From + ", " + FbLevel + ", " + LLThreshold + ", " + ULThreshold);
				Vector ThresholdList = null;
				if ((ThresholdList = (Vector) LookupTable.get(From)) == null)
				{
					ThresholdList = new Vector();
				}
				ThresholdList.add(new ConditionByNo( FbLevel, LLThreshold, ULThreshold, 0));
				LookupTable.put(From,ThresholdList);  // Last part represents level of falling behindness. 
													  // Here, I just set one level of falling behindness. 
													  // In the future, if we specify more levels, then it will have meaning. 

			}

			if (lookuptable == null)
			{
				lookuptable = LookupTable;
			}

		} 
		catch (java.io.IOException ioexc)
	    {
		    System.err.println ("can't read the input file, io error" );
	    }
	}

    public synchronized void add(org.cougaar.core.util.UniqueObject p, int action ) {	

			if (internalState.StartTime == -1L)
			{
				if ( p instanceof Task) {

					Task pdu = ( Task ) p ; 

					// if this pdu is not an add event.
					if (action != 0) {	return;	}
					
					// check the start time of planning
					String v = pdu.getVerb().toString();

					if (v.compareToIgnoreCase("ReportForDuty") != 0 && v.compareToIgnoreCase("ReportForService") != 0)
					{

						starttime = bts.getCreationTime(pdu.getUID()); 

						if (starttime == -1) { return; }

						internalState.StartTime = starttime;
						System.out.println("\n" + cluster + "'s first task : time = " + internalState.StartTime + " with verb = " + v +", " + pdu.getUID().toString());
						bs.publishChange(internalState);

					    CommunityService communityService = (CommunityService) getBindingSite().getServiceBroker().getService(this, CommunityService.class, null);

						Collection alCommunities = communityService.listParentCommunities(getAgentIdentifier().toString(), "(CommunityType=AdaptiveLogistics)");				
						internalState.setalCommunities(alCommunities);

						Collection alCommunities2 = communityService.search("(CommunityType=AdaptiveLogistics)");				

						if (internalState.alCommunities != null)
						{
							for (Iterator iterator = alCommunities2.iterator(); iterator.hasNext();) {
						        String community = (String) iterator.next();
								StartIndicator tindicator = new StartIndicator(cluster, uidservice.nextUID(), starttime, 10);
								tindicator.addTarget(new AttributeBasedAddress(community,"Role","AdaptiveLogisticsManager"));
								System.out.println(getAgentIdentifier().toString() + ": adding StartIndicator to be sent to " + tindicator.getTargets());
								bs.publishAdd(tindicator);
							}

							if(!internalState.over) { sendLoadIndicator(0, LoadIndicator.NORMAL_LOAD); }
						} else {
							System.out.println(getAgentIdentifier().toString() + " Destination address is null");
						}
					}
				}

			} else if (!internalState.over) {

				if ( p instanceof Task ) {

					Task tpdu = ( Task ) p ;

					String s = tpdu.getVerb().toString();

					if (internalState.StartTime > 0 )
					{
						org.cougaar.core.util.UID uid = (org.cougaar.core.util.UID) tpdu.getUID();
						// check waiting time
						if (action == 0)	{
							addTaskForCheckingFallingBehind(tpdu);
							bs.publishChange(internalState);
						} else if (action == 1 && Hongfbtype == 1) { // for type 1, considering the case in which a task is cancelled before allocation
							getAverageWaitingTime(uid, bts.getCreationTime(uid));
							bs.publishChange(internalState);
          				}
					}
					
				}
  			    else if (Hongfbtype == 1 && action != 0 && internalState != null)
  			    {
					if (p instanceof PlanElement)	{
						PlanElement ppdu = (PlanElement) p;
						getAverageWaitingTime((org.cougaar.core.util.UID) ppdu.getTask().getUID(), bts.getCreationTime(ppdu.getUID()));
						bs.publishChange(internalState);
					} 
  			    } 
		 	}
    }
	
	public void execute()
    {
        Iterator iter;
        
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
				if(!internalState.over) { System.out.println("PSUFBSensor2Plugin start at " + cluster); }
				break;
			}
		} 

		if (internalState != null)
		{
			// collect the information about the time and number of tasks
		    for (iter = taskSubscription.getAddedCollection().iterator() ; iter.hasNext() ;)
			{
				Task task = (Task)iter.next();
				add(task, 0);
		    }
		}
    }

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		public void addTaskForCheckingFallingBehind(Task tpdu) {

			if (internalState.over) {	return;	}

			internalState.NoTasks++;
			long t = bts.getCreationTime(tpdu.getUID())-internalState.StartTime;

			if (t >= internalState.nextcheckpoint)
			{
				System.out.println(cluster + " at time " + t + ", # of tasks = " + internalState.NoTasks);
				internalState.nextcheckpoint = t - t%1000;
				checkfallingbehindness2(internalState.nextcheckpoint, internalState.NoTasks);	// Type 2
				internalState.nextcheckpoint = internalState.nextcheckpoint + 1000;
			}
		}

		public void checkfallingbehindness2(long curr_time, int num_task) { // for the number of tasks

			boolean FbSV = false;
			int newlydetectedstate = internalState.currentstate;

			if (FbSV == true) // check whether SV check falling behindness.
			{

			
			} else {

				// Consult Lookup table
				Vector thresholdlist = (Vector) lookuptable.get(new Long(curr_time));

				if (thresholdlist != null)
				{
					for (Enumeration e = thresholdlist.elements() ; e.hasMoreElements() ;) {
						 ConditionByNo k = (ConditionByNo) e.nextElement();
						 if (k.check(num_task, curr_time))
						 {
							newlydetectedstate = k.getLevel();
						 } 
					}

					if (newlydetectedstate != internalState.currentstate)
					{
						internalState.currentstate = newlydetectedstate;
						System.out.println(cluster+","+num_task+","+curr_time+", Falling behind: level "+ fbresult[internalState.currentstate]);
						sendLoadIndicator(1, fbresult[internalState.currentstate]);
					}
				}
			}
		}

		public void getAverageWaitingTime(org.cougaar.core.util.UID uid, long time1) {
/*
			if (internalState.over) {	return;	}

			long time = time1 - internalState.StartTime;

			// add cumulative finishtime
			taskinfo tpdu = null;

			if ((tpdu = (taskinfo) internalState.TaskList.get(uid))== null)
			{
				return;
			}

			long wt = 0; 
			float awt = 0;
			long chktime = internalState.CurrentTime + internalState.unittime;
			String ss="N";

			if (chktime > time)
			{
				((taskinfo) internalState.TaskList.get(uid.toString())).finishtime = time;
				if (internalState.timelimit < time) // if the time is over a certain number them it will not calculate the 
				{
					internalState.over = true;				
				}
				return;
			}

			while (chktime < time)
			{
				for (Enumeration e = internalState.TaskList.keys() ; e.hasMoreElements() ;) {
					
					taskinfo t = (taskinfo) internalState.TaskList.get(e.nextElement()); 

					if ( t!=null) {
						if (t.finishtime < chktime)
						{
							internalState.CTFinish = internalState.CTFinish + t.finishtime - t.eventtime;
							internalState.TaskList.remove(uid.toString());
							internalState.NoFinish++;
						} else {
							wt = chktime - t.eventtime;
						}
					}
				}

				int s = internalState.TaskList.size();

				awt = (float) (wt+internalState.CTFinish)/(internalState.NoFinish+s);

				checkfallingbehindness(chktime,awt);
				internalState.CurrentTime = internalState.CurrentTime + internalState.unittime;
				chktime = internalState.CurrentTime + internalState.unittime;
			} 
*/
		}

		public void checkfallingbehindness(long chktime, float awt) {   // for the average waiting time

			int []c = { -1, -1 };
			
			for (Enumeration e = lookuptable.elements() ; e.hasMoreElements() ;) {
				 criteria k = (criteria) e.nextElement();
				 if (k.check(chktime))
				 {
					 if (k.threshold < awt )
					 {
						c[k.type] = 1;
					 } 
				 }
			}

			if (c[1] > 0 && internalState.currentstate < 2)  // Severe falling behind
			{
				System.out.println(cluster+","+chktime+","+awt+", S, by waiting time");
				internalState.currentstate = 2;
				sendLoadIndicator(1, fbresult[internalState.currentstate]);
			} else 	if (c[0] > 0  && c[1] < 0 && internalState.currentstate < 1) {  // Mild falling behind
				System.out.println(cluster+","+chktime+","+awt+", M, by waiting time");
				internalState.currentstate = 1;
				sendLoadIndicator(1, fbresult[internalState.currentstate]);
			} 
		}

		public void sendLoadIndicator(int mode, String loadlevel) 
		{

			if (mode == 0)
			{
				loadIndicator = new LoadIndicator(this.getClass(), cluster, uidservice.nextUID(), loadlevel);
				for (Iterator iterator = internalState.alCommunities.iterator(); iterator.hasNext();) {
					String community = (String) iterator.next();
					loadIndicator.addTarget(new AttributeBasedAddress(community,"Role","AdaptiveLogisticsManager"));
				}
				bs.publishAdd(loadIndicator);

			} else {
			
		        loadIndicator.setLoadStatus(loadlevel);
				bs.publishChange(loadIndicator);
			}	

			System.out.println(cluster + ": adding loadIndicator to be sent to " + loadIndicator.getTargets());
		}

	class ConditionByNo implements java.io.Serializable
	{
		public ConditionByNo(int FbLevel, float LLThreshold, float ULThreshold, int type1) {
			level = FbLevel;
			LowerLimit = LLThreshold;
			Upperlimit = ULThreshold;  // time;
			type = type1;
		}

		public boolean check(int num_task, long t) {

			if (LowerLimit < num_task && num_task <= Upperlimit) {

				return true;
			}

			return false;
		}

		public int getLevel() {
			return level;
		}

		int level;
		float LowerLimit;
		float Upperlimit;  // time;
		public int type;

	}

	class criteria implements java.io.Serializable
	{
		public criteria(long from1, long to1, float threshold1, int type1) {
			from = from1;
			to = to1;
			threshold = threshold1;
			type = type1;
		}

		public boolean check(long time) {

			if (from < time && time <= to)
			{
				return true;
			} 
			return false;
		}

		long from;
		long to;
		public float threshold;
		public int type;

	};

	// it is used for average waiting time
	class taskinfo implements java.io.Serializable
	{
		public taskinfo(long e) { 
			eventtime = e;
			finishtime = Long.MAX_VALUE;
		}

		public long eventtime;
		public long finishtime;

	};

}