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

public class DemandHistoryPerType implements java.io.Serializable 
{
	String type = null;		
	HashMap demandHistoryPerItem = null;
	LoggingService myLoggingService = null;
	long tempLeadTime = 0;
	long tempToday = 0;
	
	public DemandHistoryPerType(String type,LoggingService myLoggingService) {
		this.type = type;
		this.myLoggingService = myLoggingService;
		demandHistoryPerItem = new HashMap();
	}

	public void addDemandData(Task task) {  // public void addDemandData(Task task, long today) {

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
			myLoggingService.shout ("DemandHistoryPerType : null Prepositional Phrase Maintaining" );
			return;
		}

		DemandHistoryForAnItem demandHistoryForAnItem = (DemandHistoryForAnItem)demandHistoryPerItem.get(nomenclature);

		long end_time = (long) (task.getPreferredValue(AspectType.END_TIME) / 86400000);
		double qty = task.getPreferredValue(AspectType.QUANTITY);

		if (demandHistoryForAnItem == null)	{
			demandHistoryForAnItem = new DemandHistoryForAnItem(ofType,maintainedItem,myLoggingService);
			
			Date commitmentDate = task.getCommitmentDate();
			long commitment_time = 0;

			if (commitmentDate != null)		{
				commitment_time = commitmentDate.getTime()/86400000;					
			}
			
			demandHistoryForAnItem.setCommitmentTime((end_time-commitment_time));
		}

		demandHistoryForAnItem.addDemandData(end_time,qty);
		demandHistoryPerItem.put(nomenclature,demandHistoryForAnItem);
	}

	public boolean removeDemandData(Task task) {

		String nomenclature = null;
		PrepositionalPhrase pp = task.getPrepositionalPhrase("Maintaining");

		if (pp != null)
		{
			nomenclature = ((MaintainedItem) pp.getIndirectObject()).getNomenclature();
		} else {
			myLoggingService.shout ("DemandHistoryPerType : null Prepositional Phrase Maintaining" );
			return false;
		}

		DemandHistoryForAnItem demandHistoryForAnItem = (DemandHistoryForAnItem)demandHistoryPerItem.get(nomenclature);

		if (demandHistoryForAnItem == null)	{
			myLoggingService.shout ("DemandHistoryPerType : you tried to remove an unrecorded task "+nomenclature +" in "+type);
			return false; 
		}

		int end_time = (int) (task.getPreferredValue(AspectType.END_TIME) / 86400000);
		double qty = task.getPreferredValue(AspectType.QUANTITY);

		if (!demandHistoryForAnItem.removeDemandData(end_time,qty))	{
			myLoggingService.shout ("[HONG]DemandHistoryPerType : you tried to remove a task of "+nomenclature +" in "+type );
			return false; 
		}
		return true;
	}

	public double averagePast(int timeWindow, int targetDate, String nomenclature) {

		DemandHistoryForAnItem demandHistoryForAnItem = (DemandHistoryForAnItem)demandHistoryPerItem.get(nomenclature);

		if (demandHistoryForAnItem == null)	{
			myLoggingService.shout ("DemandHistoryPerType : you tried to get past history for unknown item." );
			return -1; 
		}

		return demandHistoryForAnItem.averagePast(timeWindow, targetDate);
	}

	public Collection getHistoryOfItems()	{
		return demandHistoryPerItem.values();
	}

	public long getMaxEndTime() {

		long max_end_time = 0;
		Collection historyOfItems = getHistoryOfItems();

		for (Iterator iter = historyOfItems.iterator();iter.hasNext() ; )	{
			
			DemandHistoryForAnItem demandHistoryForAnItem = (DemandHistoryForAnItem) iter.next();
			long temp_max_end_time = demandHistoryForAnItem.getMaxEndTimeInHistory();

			if (max_end_time < temp_max_end_time)	{
				max_end_time = temp_max_end_time;
			}

		}

		return max_end_time;
	}

	public long getLeadTime(long commLossDay) {

		long max_end_time = getMaxEndTime();

		if (max_end_time<=commLossDay)	{
			myLoggingService.shout ("DemandHistoryPerType : Something wrong on this type " + type +"because it has smaller value of max_end_time than commLossDay!!");
			return 0;
		}

		return (max_end_time-commLossDay) ;
	}

	public String getType() {
		return type;
	}
}	