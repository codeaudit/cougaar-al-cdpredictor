package org.cougaar.tools.alf.sensor.plugin;

import org.cougaar.core.adaptivity.InterAgentCondition;
import org.cougaar.core.agent.service.alarm.PeriodicAlarm;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.plugin.ComponentPlugin;
import org.cougaar.core.service.*;
import org.cougaar.core.util.UID;
import org.cougaar.glm.ldm.Constants;
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

public class SVMPredictor extends Predictor {

    SvmResult svmResult = null;

	// All the demand will be managed by this manager
//	public SVMPredictor(String cluster,LoggingService myLoggingService,ConfigFinder configfinder,PredictorPlugin predictorPlugin) {	
	public SVMPredictor(String cluster,LoggingService myLoggingService,ConfigFinder configfinder,PSUPredictorPlugin predictorPlugin) {	
		super(cluster,myLoggingService,configfinder,predictorPlugin);
		svmResult = read_SVM_Model(configfinder);
		openLoggingFile("svm");
	}

	// in this function, most appropriate one might be chosen. this will be called once just after communication loss. 
	// This is regularly called by plugin for the purpose of test. 
	// this is a future work.
	public void updateEsimationOfPredictorParameters() {

	}

	// general explanation is in the MovingAveragePredictor.
	public void forecast(long commlossday, long today) {

		Collection customers = demandHistoryManager.getCustomerCollection(); // key list of agent demand 		

		for (Iterator iter = customers.iterator();iter.hasNext() ; )	{

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

					if (!demandHistoryForAnItem.getName().equalsIgnoreCase("120MM APFSDS-T M829A1"))	{
						continue;
					}

					// in actual commloss, this will be replaced with today + leadtime.
					long maxEndTimeInHistory = demandHistoryForAnItem.getMaxEndTimeInHistory();	 // if -1, then there's no history.
			        long averageInterval = demandHistoryForAnItem.getAverageInterval();

					if (maxEndTimeInHistory == -1) 	{	// if -1, then there's no history. Then, we cannot forecast for this item.
						continue;
					}

					// assuming comm loss day is yester day.
					long timeWidnow = 3;
					myLoggingService.shout("SVM : "+demandHistoryForAnItem.getName());

					int timeLag = 3;
			    
					double pastRecord[] = demandHistoryForAnItem.getHistoryOf(timeLag);
			    
					// for test purpose
					for (int k=0;k<timeLag;k++)	{
						myLoggingService.shout("SVM : pastRecord["+k+"]="+pastRecord[k]);
					}
					//
			    
					double expectedDemand = 0;
			    
					if (pastRecord !=null )	{
						expectedDemand = svmResult.f(pastRecord);
					}
				
					long expectedNextEndTime = maxEndTimeInHistory+averageInterval;
					long minimumAllowableEndtime = commlossday + leadTime - averageInterval;

					if ( expectedNextEndTime < minimumAllowableEndtime )
					{
						long diff = minimumAllowableEndtime-expectedNextEndTime;
						myLoggingService.shout("[SVM] There is a difference between expected next demand time and allowable period.");
						myLoggingService.shout("[SVM] expected end time, required minimum end time, difference "
													+expectedNextEndTime+","+minimumAllowableEndtime+","+diff);
						// find the time point
						int t = (int) Math.ceil(diff/averageInterval);
						long newExpectedNextEndTime = expectedNextEndTime+t*averageInterval;
						
						myLoggingService.shout("[SVM] expected end time which is greater than required minimum end time = "+newExpectedNextEndTime);
						myLoggingService.shout("[SVM] expected demand = " + expectedDemand);

					} else {

						write(customer.getName()+"\t"+demandHistoryForAnItem.getOfType()+"\t"+demandHistoryForAnItem.getName()+"\t"+today+"\t"+expectedNextEndTime+"\t"+expectedDemand+"\n");
						myLoggingService.shout("[HONG]averagePast : "+expectedDemand+","+expectedNextEndTime+","+demandHistoryForAnItem.getName());	

					}

/*					this is for the case in which actual comm loss happens and time continous progress
					
					if (((today + leadTime - maxEndTimeInHistory)%averageInterval)== 0)	{
						write(customer.getName()+"\t"+demandHistoryForAnItem.getOfType()+"\t"+demandHistoryForAnItem.getName()+"\t"+today+"\t"+expectedNextEndTime+"\t"+expectedDemand+"\n");						
					} 
					//else {
					//	// print out zero.
					//	write(customer.getName()+"\t"+demandHistoryForAnItem.getOfType()+"\t"+demandHistoryForAnItem.getName()+"\t"+today+"\t"+expectedNextEndTime+"\t"+expectedDemand+"\n");						
					//}
*/


				}
			}
		}

		flush();

	}

	private SvmResult read_SVM_Model(ConfigFinder configfinder) {

       SvmResult svmresult = new SvmResult();
       String inputName = "123-MSB-120MM-APFSDS-T-M829A1.txt.svm";
	   
	   try {
	   
           File paramFile = configfinder.locateFile("param.dat");
           if (paramFile != null && paramFile.exists())		{	svmresult.readParam(paramFile);					} 
		   else												{	myLoggingService.shout("SVM Param model error.");   }
	   
           if (inputName != null && configfinder != null)	{
               File inputFile = configfinder.locateFile(inputName);
               if (inputFile != null && inputFile.exists()) {	svmresult.readModel(inputFile);					} 
			   else {											myLoggingService.shout("SVM Input model error.");	}
           }
	   
       } catch (Exception e) {
           e.printStackTrace();
       }

	   return svmresult;
	}

}

	