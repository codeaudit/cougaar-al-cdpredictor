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
import org.cougaar.logistics.plugin.inventory.MaintainedItem;

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

public class DemandHistoryManager implements java.io.Serializable {

	HashMap DemandHistory; 

	// All the demand will be managed by this manager
	public DemandHistoryManager() {
		DemandHistory = new HashMap();
	}

	public void addDemandData(Task task, long today) {

		DemandPerAgent demandPerAgent = (DemandPerAgent) DemandHistory.get((String) task.getUID().getOwner());

		if (demandPerAgent == null)	{	
			demandPerAgent = new DemandPerAgent((String) task.getUID().getOwner());		
		}

		demandPerAgent.addDemandData(task, today);
		DemandHistory.put((String) task.getUID().getOwner(), demandPerAgent);
	}

	public void removeDemandData(Task task) {

		DemandPerAgent demandPerAgent = (DemandPerAgent) DemandHistory.get((String) task.getUID().getOwner());

		demandPerAgent.removeDemandData(task);
		DemandHistory.put((String) task.getUID().getOwner(), demandPerAgent);
	}

	public Collection getCustomerCollection() {

		return (Collection) DemandHistory.values(); 
	}

}

	