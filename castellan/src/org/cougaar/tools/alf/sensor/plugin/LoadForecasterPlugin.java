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
import org.cougaar.tools.castellan.pdu.* ;
import org.cougaar.tools.castellan.util.* ;

import java.util.* ;
import java.lang.Double;


/**
 *
 * @author  Yunho internalState.Hong
 * @version
 */

public class LoadForecasterPlugin extends ComponentPlugin {
	
	IncrementalSubscription timeIndicatiorSubscription;
	IncrementalSubscription planCompletion1Subscription;
	IncrementalSubscription planCompletion2Subscription;
	IncrementalSubscription conusgroundStartSubscription;
	IncrementalSubscription transcomStartSubscription;
	IncrementalSubscription rbfNNSubscription;
	IncrementalSubscription internalStateSubscription;

	UnaryPredicate planCompletion1Predicate = new UnaryPredicate() { 
		public boolean execute(Object o) 
		{  
			if (o instanceof PSUSensorCondition)
			{
				PSUSensorCondition sensorcon = (PSUSensorCondition) o;
				if (sensorcon.getName().compareToIgnoreCase("PlanCompletionTime-Transcom")==0)
				{
					return true;
				}
			}
			return false;
		}    
	};

	UnaryPredicate planCompletion2Predicate = new UnaryPredicate() { 
		public boolean execute(Object o) 
		{  
			if (o instanceof PSUSensorCondition)
			{
				PSUSensorCondition sensorcon = (PSUSensorCondition) o;
				if (sensorcon.getName().compareToIgnoreCase("PlanCompletionTime-GlobalSeaAir")==0)
				{
					return true;
				}
			}
			return false;
		}    
	};

    UnaryPredicate timeIndicatiorPredicate = new UnaryPredicate()	{ public boolean execute(Object o) {  return o instanceof StartIndicator;   }    };

    UnaryPredicate conusgroundStartPredicate = new UnaryPredicate()	{ 

		public boolean execute(Object o) 
		{  
			if (o instanceof PSUSensorCondition)
			{
				PSUSensorCondition sensorcon = (PSUSensorCondition) o;
				if (sensorcon.getName().compareToIgnoreCase("conusgroundStart")==0)
				{
					return true;
				}
			}
			return false;
		}    
	};

    UnaryPredicate transcomStartPredicate = new UnaryPredicate()	{ 

		public boolean execute(Object o) 
		{  
			if (o instanceof PSUSensorCondition)
			{
				PSUSensorCondition sensorcon = (PSUSensorCondition) o;
				if (sensorcon.getName().compareToIgnoreCase("transcomStart")==0)
				{
					return true;
				}
			}
			return false;
		}    
	};

    UnaryPredicate internalStatePredicate			= new UnaryPredicate()	{ public boolean execute(Object o) {  return o instanceof InternalStateLF;   }    };
//    UnaryPredicate rbfNNPredicate = new UnaryPredicate()	{ public boolean execute(Object o) {  return o instanceof RBFNN;   }    };
    UIDService uidservice;
    BlackboardService bs;

	PSUSensorCondition completionTime1Condition = null;
	PSUSensorCondition completionTime2Condition = null;
	PSUSensorCondition conusgroundStartCondition = null;
	PSUSensorCondition transcomStartCondition = null;
//	RBFNN rbfNN = null;
	InternalStateLF internalState = null;

    public void setupSubscriptions()   {

		UIDService us=(UIDService) getServiceBroker().getService(this, UIDService.class, null);

		ServiceBroker broker = getServiceBroker();
        bs = getBlackboardService();

		internalStateSubscription = (IncrementalSubscription) bs.subscribe(internalStatePredicate);
        planCompletion1Subscription = (IncrementalSubscription) bs.subscribe(planCompletion1Predicate);
        planCompletion2Subscription = (IncrementalSubscription) bs.subscribe(planCompletion2Predicate);
        conusgroundStartSubscription = (IncrementalSubscription) bs.subscribe(conusgroundStartPredicate);
        transcomStartSubscription = (IncrementalSubscription) bs.subscribe(transcomStartPredicate);

        timeIndicatiorSubscription = (IncrementalSubscription) bs.subscribe(timeIndicatiorPredicate);
  //      rbfNNSubscription = (IncrementalSubscription) bs.subscribe(rbfNNPredicate);
		
		uidservice =(UIDService) getServiceBroker().getService(this, UIDService.class, null);

		if (bs.didRehydrate() == false)
        {
			completionTime1Condition = new PSUSensorCondition("PlanCompletionTime-Transcom", new OMCRangeList(new Double(0),new Double(Double.MAX_VALUE)),new Double(0), us.nextUID());
			completionTime2Condition = new PSUSensorCondition("PlanCompletionTime-GlobalSeaAir", new OMCRangeList(new Double(0),new Double(Double.MAX_VALUE)),new Double(0), us.nextUID());
			conusgroundStartCondition = new PSUSensorCondition("conusgroundStart", new OMCRangeList(new Double(0),new Double(Double.MAX_VALUE)),new Double(0), us.nextUID());
			transcomStartCondition = new PSUSensorCondition("transcomStart", new OMCRangeList(new Double(0),new Double(Double.MAX_VALUE)),new Double(0), us.nextUID());
			
	//		rbfNN = new RBFNN();

			if (completionTime1Condition != null)
			{
				bs.publishAdd(completionTime1Condition);
			} 

			if (completionTime2Condition != null)
			{
				bs.publishAdd(completionTime2Condition);
			} 

			if (conusgroundStartCondition != null)
			{
				bs.publishAdd(conusgroundStartCondition);
			} 

			if (transcomStartCondition != null)
			{
				bs.publishAdd(transcomStartCondition);
			}

			internalState = new InternalStateLF(uidservice.nextUID());

			if (internalState != null)
			{
				bs.publishAdd(internalState);
			}

/*			

			if (rbfNN != null)
			{
				bs.publishAdd(rbfNN);
			}
*/
		}

		bs.setShouldBePersisted(false);

		String cluster = getBindingSite().getAgentIdentifier().toString();
		System.out.println("LoadForecasterPlugin start at " + cluster);

    }

	public void execute()
    {
        Iterator iter;
        

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

        // Retrieve completionTimeCondition which contains the estimated plan completion time 
		if ( completionTime1Condition == null)
		{
	        for (iter = planCompletion1Subscription.getAddedCollection().iterator() ; iter.hasNext() ;)
	        {
		        completionTime1Condition = (PSUSensorCondition) iter.next();
			}
		}

		if ( completionTime2Condition == null)
		{
	        for (iter = planCompletion2Subscription.getAddedCollection().iterator() ; iter.hasNext() ;)
	        {
		        completionTime2Condition = (PSUSensorCondition) iter.next();
			}
		}

		if ( conusgroundStartCondition == null)
		{
	        for (iter = conusgroundStartSubscription.getAddedCollection().iterator() ; iter.hasNext() ;)
	        {
		        conusgroundStartCondition = (PSUSensorCondition) iter.next();
			}
		}

		if ( transcomStartCondition == null)
		{
	        for (iter = transcomStartSubscription.getAddedCollection().iterator() ; iter.hasNext() ;)
	        {
		        transcomStartCondition = (PSUSensorCondition) iter.next();
			}
		}

		// collect timeindicators which are supposed to be sent by other agent.
		for (iter = timeIndicatiorSubscription.getAddedCollection().iterator() ; iter.hasNext() ;)
        {
            StartIndicator timeindicator = (StartIndicator)iter.next(); 
			forecast(timeindicator);
        }
    }	


	public void forecast(StartIndicator timeindicator) {
		
		long x;
		double y;

		// I assume the NCA's GetLogSupport arrives first. In Tiny-1AD, this is always correct. 
		// But, in distributed environment, it might be wrong. Still, the error is not much large.
		if (timeindicator.getAgentName().equalsIgnoreCase("NCA"))
		{
			internalState.PlanStartTime = timeindicator.getStartTime();
			System.out.println("The society plan start time = " + internalState.PlanStartTime);
		} else if (timeindicator.getAgentName().equalsIgnoreCase("TRANSCOM")) {
			
			x = timeindicator.getStartTime() - internalState.PlanStartTime;
			y = 7.5691*x + 178905;

			completionTime1Condition.setValue(new Double(y));
			bs.publishChange(completionTime1Condition);
			System.out.println("The estimated plan completion time based on TRANSCOM time = " + y);

			y = 3.4561*x + 123563;	// The estimated start time of CONUSGROUND based on TRANSCOM time

			System.out.println("The estimated start time of CONUSGROUND based on TRANSCOM time = " + y);
			conusgroundStartCondition.setValue(new Double(y));
			bs.publishChange(conusgroundStartCondition);

		} else if (timeindicator.getAgentName().equalsIgnoreCase("Globalsea"))	{

			internalState.globalsea_time = timeindicator.getStartTime() - internalState.PlanStartTime;
			internalState.hari++;

		} else if (timeindicator.getAgentName().equalsIgnoreCase("Globalair"))	{

			internalState.globalair_time = timeindicator.getStartTime() - internalState.PlanStartTime;
			internalState.hari++;

		} else if (timeindicator.getAgentName().equalsIgnoreCase("1-AD"))	{

			internalState.onead_time = timeindicator.getStartTime() - internalState.PlanStartTime;

		} else if (timeindicator.getAgentName().equalsIgnoreCase("21-TSC-HQ"))	{

			internalState.Hong[0] = timeindicator.getStartTime() - internalState.PlanStartTime;
			internalState.Thadakamala++;

		} else if (timeindicator.getAgentName().equalsIgnoreCase("3-SUPCOM-HQ"))	{

			internalState.Hong[1] = timeindicator.getStartTime() - internalState.PlanStartTime;
			internalState.Thadakamala++;

		} else if (timeindicator.getAgentName().equalsIgnoreCase("5-CORPS-ARTY"))	{

			internalState.Hong[2] = timeindicator.getStartTime() - internalState.PlanStartTime;
			internalState.Thadakamala++;
		} else if (timeindicator.getAgentName().equalsIgnoreCase("5-CORPS-REAR"))	{

			internalState.Hong[3] = timeindicator.getStartTime() - internalState.PlanStartTime;
			internalState.Thadakamala++;
		}

		if (internalState.hari == 2)
		{
			double xx = (internalState.globalsea_time + internalState.globalair_time)/2;
			y = 3.711*xx + 153705;

			completionTime2Condition.setValue(new Double(y));
			bs.publishChange(completionTime2Condition);
			
			System.out.println("The estimated plan completion time based on GlobalSea and GlobalAir time = " + y);
			internalState.hari = -1;
		}

		if (internalState.onead_time > 0 && internalState.Thadakamala == 2)
		{

			double xx = rbf(internalState.onead_time, internalState.Hong);

			y = xx + 5000;

			transcomStartCondition.setValue(new Double(y));
			bs.publishChange(transcomStartCondition);
			
			System.out.println("The estimated starting time of Transcom = " + y);
			internalState.Thadakamala = -2;
		}

		System.out.println("Actual time" + timeindicator.getAgentName() + " starts at " + (timeindicator.getStartTime() - internalState.PlanStartTime));

	}

	// This part will be implemented before August 26.
	private double rbf(long onead_time, long[] Hong) {
		
		// rbfNN
		return	(double) (onead_time + Hong[0] + Hong[1] + Hong[2] + Hong[3])/3;
	}
}
