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

public class PredictorManager implements java.io.Serializable {

//	HashMap DemandHistory; 
	MovingAveragePredictor movingAverage;
//	ARMAPredictor		   aRMA;
	SVMPredictor		   sVM;
	DemandHistoryManager demandHistoryManager;

	String cluster = null;
	LoggingService myLoggingService = null;

	// All the demand will be managed by this manager
	public PredictorManager(String cluster, LoggingService myLoggingService,ConfigFinder configfinder, PSUPredictorPlugin predictorPlugin) {
		this.cluster = cluster;
		this.myLoggingService = myLoggingService;

		movingAverage = new MovingAveragePredictor(cluster,myLoggingService,configfinder,predictorPlugin);
//		aRMA = new ARMAPredictor();
		sVM = new SVMPredictor(cluster,myLoggingService,configfinder,predictorPlugin);
	}

	public void setDemandHistoryManager(DemandHistoryManager demandHistoryManager) {
		this.demandHistoryManager = demandHistoryManager;

		movingAverage.setDemandHistoryManager(demandHistoryManager);
//		aRMA.setDemandHistoryManager(demandHistoryManager);
		sVM.setDemandHistoryManager(demandHistoryManager);
	}

	// in this function, most appropriate one might be chosen. this will be called once just after communication loss. 
	// This is regularly called by plugin for the purpose of test. 
	// this is a future work.
	public void updateEsimationOfPredictorParameters() {

	}

	/////////////	 
	public void forecast(long today) {

		movingAverage.forecast(today);
		if (cluster.equalsIgnoreCase("123-MSB"))	{
			sVM.forecast(today);
		}

	}
}

	