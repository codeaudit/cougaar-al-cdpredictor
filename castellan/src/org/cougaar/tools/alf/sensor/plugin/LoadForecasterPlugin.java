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
import org.cougaar.core.adaptivity.OMCRangeList;
import org.cougaar.core.plugin.*;

import org.cougaar.planning.ldm.plan.*;
import org.cougaar.util.*;

import org.cougaar.logistics.plugin.manager.LoadIndicator;
import org.cougaar.tools.alf.sensor.*;
import org.cougaar.tools.alf.sensor.plugin.*;
import org.cougaar.tools.alf.sensor.rbfnn.*;
import org.cougaar.tools.castellan.pdu.* ;
import org.cougaar.tools.castellan.util.* ;

import java.util.* ;
import java.lang.Double;

import java.io.*;

/**
 *
 * @author  Yunho internalState.Hong
 * @version
 */

public class LoadForecasterPlugin extends ComponentPlugin {
	
	// 
	IncrementalSubscription loadIndicatiorSubscription;
	IncrementalSubscription lfMessageSubscription;
	IncrementalSubscription rbfnnSubscription;
	IncrementalSubscription internalStateSubscription;
	IncrementalSubscription taskSubscription;

	//
	UnaryPredicate lfMessagePredicate = new UnaryPredicate() { 
		public boolean execute(Object o) 
		{  
			if (o instanceof LFMessage)	{	return true;	}
			return false;
		}    
	};

    UnaryPredicate internalStatePredicate	= new UnaryPredicate()	{	public boolean execute(Object o) {  return o instanceof InternalStateLF;		}    };
    UnaryPredicate rbfnnPredicate			= new UnaryPredicate()	{	public boolean execute(Object o) {  return o instanceof RbfRidgeRegression;		}    };
	UnaryPredicate taskPredicate			= new UnaryPredicate()	{ 	public boolean execute(Object o) {  return (o instanceof Task); 				}   };

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

	//
	UIDService uidservice;
    BlackboardService bs;

	// 
	LoadIndicator loadIndicator = null;
	RbfRidgeRegression rbfnn = null;
	InternalStateLF internalState = null;

	long offsetTime = -1;
	int curr_state = -1;

	double [] a_row; // CONUSGround, GlobalAir, GlobalSea, PlanePacker, ShipPacker, TheaterGround, TRANSCOM

	double cumEstimatedValue=0;
	int count=0;

	String cluster = null;
	int canForecast =0;

    public void setupSubscriptions()   {
		
		a_row = new double[14];

		for (int i=0;i<14 ; i++)	{		a_row[i] = 0; 		} 

		UIDService us=(UIDService) getServiceBroker().getService(this, UIDService.class, null);

		ServiceBroker broker = getServiceBroker();
        bs = getBlackboardService();

		internalStateSubscription	= (IncrementalSubscription) bs.subscribe(internalStatePredicate);
        lfMessageSubscription		= (IncrementalSubscription) bs.subscribe(lfMessagePredicate);
        rbfnnSubscription			= (IncrementalSubscription) bs.subscribe(rbfnnPredicate);
		taskSubscription			= (IncrementalSubscription) bs.subscribe(taskPredicate);

		uidservice =(UIDService) getServiceBroker().getService(this, UIDService.class, null);

		if (bs.didRehydrate() == false)
        {
			rbfnn = new RbfRidgeRegression();

			// num_hidden1, lamda1, increment1, count_limit1, dimension1
//			rbfnn.setParameters(500, 0.00000001, 0.01, 20, 14);

			ConfigFinder finder = getConfigFinder();
			String inputName = "model.txt";

			try {

				File paramFile = finder.locateFile( "param.txt" ) ;
				if ( paramFile != null && paramFile.exists() ) {

					rbfnn.readParam(paramFile);

			    } else {
					System.out.println("Param model error.");
				}

				if ( inputName != null && finder != null ) {

					File inputFile = finder.locateFile( inputName ) ;
					if ( inputFile != null && inputFile.exists() ) {
						rbfnn.readModel(inputFile);
			        } else {
						System.out.println("Input model error.");
					}
				}

	        } catch ( Exception e ) {
		        e.printStackTrace() ;
			}

			internalState = new InternalStateLF(uidservice.nextUID());

			if (internalState != null)	{	bs.publishAdd(internalState);	}

			if (rbfnn != null)			{	bs.publishAdd(rbfnn);			}

			loadIndicator = new LoadIndicator(this.getClass(), cluster, uidservice.nextUID(), LoadIndicator.NORMAL_LOAD);
			bs.publishAdd(loadIndicator);
			System.out.println("NORMAL_LOAD");
		}

		bs.setShouldBePersisted(false);

		String cluster = ((AgentIdentificationService) getBindingSite().getServiceBroker().getService(this, AgentIdentificationService.class, null)).getName();
		System.out.println("LoadForecasterPlugin start at " + cluster);
    }

	public void execute()
    {
        Iterator iter;
        
		if (rbfnn == null)
		{
	        for (iter = rbfnnSubscription.getAddedCollection().iterator() ; iter.hasNext() ; )
	        {
		        rbfnn = (RbfRidgeRegression) iter.next();
				break;
			}
		} 

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
		        internalState = (InternalStateLF) iter.next();
				// debug
				internalState.show();
				break;
			}
		} 

		if (offsetTime == -1)
		{
			for (iter = taskSubscription.getAddedCollection().iterator() ; iter.hasNext() ;)
	        {
			        Task t = (Task) iter.next();
					Verb v = t.getVerb();
					if (v.equals("GetLogSupport"))
					{
						offsetTime = System.currentTimeMillis();
						System.out.println("PSULF_STARTTIME="+offsetTime);
						a_row[12] = 0;
						a_row[13] = (float) taskSubscription.size();  

						break;
					}
			}

		} else {

			if (taskSubscription.hasChanged())	{	a_row[12] = (float) (System.currentTimeMillis() - offsetTime);	a_row[13] = (float) taskSubscription.size();  	
//				printA_row();
			}
		}

//		if (lfMessageSubscription.hasChanged())
//		{
			// collect timeindicators which are supposed to be sent by other agent.
			for (iter = lfMessageSubscription.getChangedCollection().iterator() ; iter.hasNext() ;)
			{
				LFMessage lfmessage = (LFMessage) iter.next();
				String agentname = lfmessage.getAgentName();

				double time = (double) (lfmessage.getTime() - offsetTime);
				if (time < 0 || offsetTime == -1)	{ time = 0;	}
	
				if ((a_row[12] > 25000 || time > 25000) && canForecast == 0 )	 {	canForecast = 1;		 }
				// CONUSGround, GlobalAir, GlobalSea, PlanePacker, ShipPacker, TheaterGround, TRANSCOM
				if		(agentname.equalsIgnoreCase("CONUSGround"))		{	a_row[0] = time ;		a_row[1] = (double) lfmessage.getNum_of_tasks();  	}
				else if (agentname.equalsIgnoreCase("GlobalAir"))		{	a_row[2] = time ;		a_row[3] = (double) lfmessage.getNum_of_tasks();  	}
				else if (agentname.equalsIgnoreCase("GlobalSea"))		{	a_row[4] = time ;		a_row[5] = (double) lfmessage.getNum_of_tasks();  	}
				else if (agentname.equalsIgnoreCase("PlanePacker"))		{	a_row[6] = time ;		a_row[7] = (double) lfmessage.getNum_of_tasks();  	}
				else if (agentname.equalsIgnoreCase("ShipPacker"))		{	a_row[8] = time ;		a_row[9] = (double) lfmessage.getNum_of_tasks();  	}
				else if (agentname.equalsIgnoreCase("TheaterGround"))	{	a_row[10] = time ;		a_row[11] = (double) lfmessage.getNum_of_tasks();  	}
			}
//			printA_row();
//		}

		if ((lfMessageSubscription.hasChanged() || taskSubscription.hasChanged()) && canForecast > 0 )
		{
			forecast(a_row);
		}
    }	

	public void forecast(double [] a_row) {

		double y = rbfnn.f(a_row);

		count++;

		cumEstimatedValue = cumEstimatedValue+y;
		
		double avg = (double)cumEstimatedValue/count;

		if (avg > 630000)
		{
			if (curr_state == -1)
			{
				System.out.println("SEVERE_LOAD");
//				loadIndicator.setLoadStatus(LoadIndicator.SEVERE_LOAD);
//				bs.publishChange(loadIndicator);
				curr_state = 1;
			}

		} else {
			if (curr_state == 1)
			{
				System.out.println("NORMAL_LOAD");
//				loadIndicator.setLoadStatus(LoadIndicator.NORMAL_LOAD);
//				bs.publishChange(loadIndicator);
				curr_state = -1;
			}
		}

//		if (count > 30)	{    canForecast = -1; 	}

		printA_row(y,avg);
	}

	private void printA_row(double y, double avg) 
	{
		// printing
		String s = "[PSULF-y] ";
		s = s+a_row[0];

		for (int i=1;i<14;i++)	{	s = s + ","+ a_row[i];	}
						
		s = s + " ["+ y +","+avg+"]";
		System.out.println(s);
	}

	private void printA_row() 
	{
		// printing
		String s = "[PSULF] ";
		s = s+a_row[0];

		for (int i=1;i<14;i++)	{	s = s + ","+ a_row[i];	}

		System.out.println(s);
	}
}