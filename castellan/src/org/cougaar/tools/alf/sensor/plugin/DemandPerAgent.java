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
	//		ArrayList demandHistoryFromACustomer = null; // this ArrayList has DemandHistoryForAnItem. In the future, I will change this into HashMap.
	HashMap demandHistoryFromACustomer = null;
	LoggingService myLoggingService = null;
	long tempLeadTime = 0;
	long tempToday = 0;
	
	public DemandPerAgent(String agentName,LoggingService myLoggingService) {
		this.agentName = agentName;
		this.myLoggingService = myLoggingService;
		demandHistoryFromACustomer = new HashMap();
	}

	public void addDemandData(Task task, long today) {

		MaintainedItem maintainedItem = null;
		String nomenclature = null;
		String ofType = null;
		PrepositionalPhrase pp0 = task.getPrepositionalPhrase("OfType");
		PrepositionalPhrase pp = task.getPrepositionalPhrase("Maintaining");
	
		if (pp != null && pp0 != null)	{
			ofType	=	(String) pp0.getIndirectObject();
			maintainedItem = (MaintainedItem) pp.getIndirectObject();
			nomenclature = maintainedItem.getNomenclature();
		} else {
			myLoggingService.shout ("DemandPerAgent : null Prepositional Phrase Maintaining" );
			return;
		}

		DemandHistoryForAnItem demandHistoryForAnItem = (DemandHistoryForAnItem)demandHistoryFromACustomer.get(nomenclature);

		if (demandHistoryForAnItem == null)	{
			demandHistoryForAnItem = new DemandHistoryForAnItem(ofType,maintainedItem,myLoggingService);
		}

		long end_time = (long) (task.getPreferredValue(AspectType.END_TIME) / 86400000);
		double qty = task.getPreferredValue(AspectType.QUANTITY);

//		if (!demandHistoryForAnItem.didSetLeadTime())	{
//		if (!demandHistoryForAnItem.didSetLeadTime())	{
//			demandHistoryForAnItem.setLeadTime(end_time-today);
//		}
		
		myLoggingService.shout ("[PSU]DemandPerAgent : ADD\t"+task.getUID().toString());

//		demandHistoryForAnItem.addDemandData(end_time,qty,today);
		demandHistoryForAnItem.addDemandData(end_time,qty);
		demandHistoryFromACustomer.put(nomenclature,demandHistoryForAnItem);
	}

	public void removeDemandData(Task task) {

		String nomenclature = null;
		PrepositionalPhrase pp = task.getPrepositionalPhrase("Maintaining");

		if (pp != null)
		{
			nomenclature = ((MaintainedItem) pp.getIndirectObject()).getNomenclature();
		} else {
			myLoggingService.shout ("DemandPerAgent : null Prepositional Phrase Maintaining" );
			return;
		}

		DemandHistoryForAnItem demandHistoryForAnItem = (DemandHistoryForAnItem)demandHistoryFromACustomer.get(nomenclature);

		if (demandHistoryForAnItem == null)
		{
			myLoggingService.shout ("DemandPerAgent : you tried to remove an unrecorded task "+nomenclature +" in "+agentName);
			return; 
		}

		int end_time = (int) (task.getPreferredValue(AspectType.END_TIME) / 86400000);
		double qty = task.getPreferredValue(AspectType.QUANTITY);

		myLoggingService.shout ("[PSU]DemandPerAgent : REMOVE\t"+task.getUID().toString());
//		demandHistoryForAnItem.removeDemandData(end_time,qty,today);
		if (!demandHistoryForAnItem.removeDemandData(end_time,qty))	{
			myLoggingService.shout ("[HONG]DemandPerAgent : you tried to remove a task of "+nomenclature +" in "+agentName );
		}
	}

	public double averagePast(int timeWindow, int targetDate, String nomenclature) {

		DemandHistoryForAnItem demandHistoryForAnItem = (DemandHistoryForAnItem)demandHistoryFromACustomer.get(nomenclature);

		if (demandHistoryForAnItem == null)
		{
			myLoggingService.shout ("DemandPerAgent : you tried to get past history for unknown item." );
			return -1; 
		}

		return demandHistoryForAnItem.averagePast(timeWindow, targetDate);
	}

	public Collection getHistoryOfItems()	{
		return demandHistoryFromACustomer.values();
	}

	public String getName() {
		return agentName;
	}
}	