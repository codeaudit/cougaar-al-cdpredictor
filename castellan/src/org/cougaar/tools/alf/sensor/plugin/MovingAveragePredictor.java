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
	public void forecast(long commlossday, long today) {	

		// for continuous forecasting, I assume the current day when Plugin actually is as commloss day and forecast for the next day.
		// here, today is actually next day only in the context of testing.
		// In actual run, today will be the actual current day.
		
		Collection customers = demandHistoryManager.getCustomerCollection(); // key list of agent demand 		

		for (Iterator iter = customers.iterator();iter.hasNext() ; )
		{
			DemandPerAgent customer = (DemandPerAgent) iter.next();

			Collection historyOfType = customer.getHistoryOfType();

			for (Iterator iterType = historyOfType.iterator();iterType.hasNext(); )
			{
				DemandHistoryPerType types = (DemandHistoryPerType) iterType.next();

				long leadTime = types.getLeadTime(commlossday);

				Collection historyOfItems = types.getHistoryOfItems();
			  
				for (Iterator iterItem = historyOfItems.iterator();iterItem.hasNext() ; )
				{
					DemandHistoryForAnItem demandHistoryForAnItem = (DemandHistoryForAnItem) iterItem.next();

					myLoggingService.shout("[PREDICTOR:MovingAveragePredictor]"+demandHistoryForAnItem.getName());

					// in actual commloss, this will be replaced with today + leadtime.
//					long nextEstimeatedEndTime = demandHistoryForAnItem.nextEstimatedEndtime();
					long maxEndTimeInHistory = demandHistoryForAnItem.getMaxEndTimeInHistory();	 // if -1, then there's no history.
			        long averageInterval = demandHistoryForAnItem.getAverageInterval();	// it sometimes return 1. 
																						// it means that it is actually 1 or there is not sufficient data to determine interval.

					if (maxEndTimeInHistory == -1) 	{	// if -1, then there's no history. Then, we cannot forecast for this item.
						myLoggingService.shout("[PREDICTOR:MovingAveragePredictor] currently no histroy!!");
						continue;
					}

					// assuming comm loss day is yester day.
					long timeWidnow = 3;
					double expectedDemand = demandHistoryForAnItem.averagePast(timeWidnow);
					
					long expectedNextEndTime = maxEndTimeInHistory+averageInterval;
					long minimumAllowableEndtime = commlossday + leadTime - averageInterval;
					if ( expectedNextEndTime < minimumAllowableEndtime)
					{
						long diff = minimumAllowableEndtime-expectedNextEndTime;
						myLoggingService.shout("[PREDICTOR:MovingAveragePredictor] There is a difference between expected next demand time and allowable period.");
						myLoggingService.shout("[PREDICTOR:MovingAveragePredictor] expected end time, required minimum end time, difference "
													+expectedNextEndTime+","+minimumAllowableEndtime+","+diff);
						// find the time point
						int t = (int) Math.ceil(diff/averageInterval);
						long newExpectedNextEndTime = expectedNextEndTime+t*averageInterval;
						
						myLoggingService.shout("[PREDICTOR:MovingAveragePredictor] expected end time which is greater than required minimum end time = "+newExpectedNextEndTime);
						myLoggingService.shout("[PREDICTOR:MovingAveragePredictor] expected demand = " + expectedDemand);

					} else {

						write(customer.getName()+"\t"+demandHistoryForAnItem.getOfType()+"\t"+demandHistoryForAnItem.getName()+"\t"+today+"\t"+expectedNextEndTime+"\t"+expectedDemand+"\t"+commlossday+"\t"+leadTime+"\n");
						myLoggingService.shout("[PREDICTOR:MovingAveragePredictor]"+expectedNextEndTime+","+expectedDemand+","+demandHistoryForAnItem.getName()+","+today+","+commlossday+","+leadTime);	

					}

/*					this is for the case in which actual comm loss happens and time continous progress
					
					if (((today + leadTime - maxEndTimeInHistory)%averageInterval)== 0)	{
						write(customer.getName()+"\t"+demandHistoryForAnItem.getOfType()+"\t"+demandHistoryForAnItem.getName()+"\t"+today+"\t"+expectedNextEndTime+"\t"+expectedDemand+"\t"+demandHistoryForAnItem.getCommitmentTime()+"\n");						
						predictorPlugin.generateAndPublish(String customer, String ofType, MaintainedItem maintainedItem, long end_time, double quantity, long today, long commitmentTime);
					} else {
						// print out zero.
						write(customer.getName()+"\t"+demandHistoryForAnItem.getOfType()+"\t"+demandHistoryForAnItem.getName()+"\t"+today+"\t"+expectedNextEndTime+"\t"+expectedDemand+"\t"+demandHistoryForAnItem.getCommitmentTime()+"\n");						
					}
*/
				}
			}
		}
		flush();
	}	
}

	