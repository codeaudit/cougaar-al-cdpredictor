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

public class DemandPerAgent implements java.io.Serializable 
{
	String agentName = null;		
	HashMap demandHistoryPerType = null;
//	LoggingService myLoggingService = null;
	long tempLeadTime = 0;
	long tempToday = 0;
	
//	public DemandPerAgent(String agentName,LoggingService myLoggingService) {
	public DemandPerAgent(String agentName) {
		this.agentName = agentName;
//		this.myLoggingService = myLoggingService;
		demandHistoryPerType = new HashMap();
	}

	public void addDemandData(Task task, long today) {

		String ofType = null;
		PrepositionalPhrase pp = task.getPrepositionalPhrase("OfType");
	
		if (pp != null)	{
			ofType	=	(String) pp.getIndirectObject();
		} else {
//			myLoggingService.shout ("DemandPerAgent : null Prepositional Phrase Maintaining" );
			return;
		}

		DemandHistoryPerType demandHistoryOfCertainType = (DemandHistoryPerType)demandHistoryPerType.get(ofType);

		if (demandHistoryOfCertainType == null)	{
//			demandHistoryOfCertainType = new DemandHistoryPerType(ofType, myLoggingService);
			demandHistoryOfCertainType = new DemandHistoryPerType(ofType);
		}

		demandHistoryOfCertainType.addDemandData(task);		//////////////////////
		demandHistoryPerType.put(ofType,demandHistoryOfCertainType);
	}

	public void removeDemandData(Task task) {

		String ofType = null;
		PrepositionalPhrase pp = task.getPrepositionalPhrase("OfType");

		if (pp != null)	{
			ofType	=	(String) pp.getIndirectObject();
		} else {
//			myLoggingService.shout ("DemandPerAgent : null Prepositional Phrase Maintaining" );
			return;
		}

		DemandHistoryPerType demandHistoryOfCertainType = (DemandHistoryPerType)demandHistoryPerType.get(ofType);

		if (demandHistoryOfCertainType == null)
		{
//			myLoggingService.shout ("DemandPerAgent : you tried to remove an unrecorded task "+ofType +" in "+agentName);
			return; 
		}

//		if (!demandHistoryOfCertainType.removeDemandData(task))	{
//			myLoggingService.shout ("[HONG]DemandPerAgent : you tried to remove a task of "+ofType +" in "+agentName );
//		}
	}

	public double averagePast(int timeWindow, int targetDate, String nomenclature, String type) {

		DemandHistoryPerType demandHistoryOfCertainType = (DemandHistoryPerType)demandHistoryPerType.get(type);

		if (demandHistoryOfCertainType == null)
		{
//			myLoggingService.shout ("DemandPerAgent : you tried to get past history for unknown type." );
			return -1; 
		}

		return demandHistoryOfCertainType.averagePast(timeWindow, targetDate,nomenclature);
	}

	public Collection getHistoryOfType()	{
		return demandHistoryPerType.values();
	}

	public String getName() {
		return agentName;
	}
}	