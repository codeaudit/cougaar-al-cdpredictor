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
 *	August, 2003
 *	PSU
**/

public class MovingAveragePredictor extends Predictor {

	// All the demand will be managed by this manager
	public MovingAveragePredictor(String cluster,LoggingService myLoggingService,ConfigFinder configfinder, PSUPredictorPlugin predictorPlugin) {	
		super(cluster,myLoggingService,configfinder,predictorPlugin);
		openLoggingFile("ma");
	}

	// in this function, most appropriate one might be chosen. this will be called once just after communication loss. 
	// This is regularly called by plugin for the purpose of test. 
	// this is a future work.
	public void updateEsimationOfPredictorParameters() {

	}

	// forecast for all customer agent and publish forecasted data.
	public void forecast(long today) {
		
		Collection customers = demandHistoryManager.getCustomerCollection(); // key list of agent demand 		

		for (Iterator iter = customers.iterator();iter.hasNext() ; )
		{
			DemandPerAgent customer = (DemandPerAgent) iter.next();

			Collection historyOfItems = customer.getHistoryOfItems();

			for (Iterator iterItem = historyOfItems.iterator();iterItem.hasNext() ; )
			{
				DemandHistoryForAnItem demandHistoryForAnItem = (DemandHistoryForAnItem) iterItem.next();
				long leadTime = demandHistoryForAnItem.getLeadTime(today);	
				// assuming comm loss day is yester day.
				long timeWidnow = 3;
				double expectedDemand = demandHistoryForAnItem.averagePast(timeWidnow,today+leadTime);
				if (expectedDemand > 0 )
				{
					// Here the forecasted demands are publishes as Tasks.
					// customer, item, end_date, qty
//					predictorPlugin.generateAndPublish(customer.getName(), demandHistoryForAnItem.getOfType(), demandHistoryForAnItem.getMaintainedItem(),today+leadTime, expectedDemand); 
					
//			        myLoggingService.shout("WRITE " + customer.getName()+", "+demandHistoryForAnItem.getOfType()+", "+demandHistoryForAnItem.getName()+", "+today+"\t"+(today+leadTime)+", "+expectedDemand);
					write(customer.getName()+"\t"+demandHistoryForAnItem.getOfType()+"\t"+demandHistoryForAnItem.getName()+"\t"+today+"\t"+(today+leadTime)+"\t"+expectedDemand+"\n");
				}
			}
		}
		flush();
	}	
	

}

	