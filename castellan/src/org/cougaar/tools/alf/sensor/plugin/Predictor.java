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

public class Predictor implements java.io.Serializable {

    private java.io.BufferedWriter rst = null;
	public DemandHistoryManager demandHistoryManager;
	private String cluster = null;
	public LoggingService myLoggingService = null;
	public PSUPredictorPlugin predictorPlugin = null;

	// All the demand will be managed by this manager
	public Predictor(String cluster,LoggingService myLoggingService,ConfigFinder configfinder,PSUPredictorPlugin predictorPlugin) {	
		this.cluster = cluster;
		this.myLoggingService = myLoggingService;
		this.predictorPlugin = predictorPlugin;
	}

	public void setDemandHistoryManager(DemandHistoryManager demandHistoryManager) {
		this.demandHistoryManager = demandHistoryManager;
	}

	public void openLoggingFile(String predictorName) {
		
		if (!predictorPlugin.isOutputFileOn())	{		return;		}

		String dir = System.getProperty("org.cougaar.workspace");
        // the Result file in workspace.
        try {
           rst = new java.io.BufferedWriter(new java.io.FileWriter(dir+"/"+ cluster + System.currentTimeMillis() + "."+predictorName+".txt", true));
        } catch (java.io.IOException ioexc) {
           myLoggingService.error("can't write file, io error");
        }
	}

	public void write(String output) {

		if (!predictorPlugin.isOutputFileOn())	{		return;		}

        try {
           rst.write(output);
        } catch (java.io.IOException ioexc) {
           myLoggingService.error("can't write file, io error");
        }
	}

	public void flush() {
		
		if (!predictorPlugin.isOutputFileOn())	{		return;		}
        
		try {
           rst.flush();
        } catch (java.io.IOException ioexc) {
           myLoggingService.error("can't write file, io error");
        }

	}
}

	