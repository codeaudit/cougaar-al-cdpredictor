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

public class DemandHistoryForAnItem implements java.io.Serializable {

	public long leadTime = 0; 
	public long commitmentTime = 0;
	public MaintainedItem maintainedItem = null; 
	public String itemName = null;
	public String ofType = null;
	private boolean leadTimeSet = false;
	private boolean commitmentDateSet = false;
//	private LoggingService myLoggingService;

	public TreeMap demandHistory = null;	

//	public DemandHistoryForAnItem(String ofType, MaintainedItem maintainedItem, LoggingService myLoggingService) { 
	public DemandHistoryForAnItem(String ofType, MaintainedItem maintainedItem) { 
		this.ofType = ofType;
		this.maintainedItem = maintainedItem; 
//		this.myLoggingService = myLoggingService;
		this.itemName = maintainedItem.getNomenclature();
		demandHistory = new TreeMap(new DateComparator());
//		dateHistory = new TreeSet(new DateComparator());
	}	

	public boolean didSetLeadTime()			{     return leadTimeSet;			}
	public boolean didSetCommitmentDate()	{     return commitmentDateSet;		}

	public long getLeadTime(long today)		{     

		if (demandHistory==null)	{
//			myLoggingService.shout("[PREDICTOR]DemandHistoryForAnItem : " + itemName + " has null demandHistory");		
			return 1;
		}

		if (demandHistory.size()==0)	{
//			myLoggingService.shout("[PREDICTOR]DemandHistoryForAnItem : " + itemName + " has zero number of demandHistory");		
			return 1;
		}

		Long tempEndDate = (Long) demandHistory.firstKey();

		return (tempEndDate.longValue()-today);			
	}

//	public void setLeadTime(long leadTime)	{	this.leadTime = leadTime;	setLeadTime = true; }
	public void setLeadTime(long leadTime)	{	this.leadTime = leadTime;	}

	// CommitmentDate has a day difference from end date.
	public void setCommitmentTime(long commitmentTime)	{	this.commitmentTime = commitmentTime;	commitmentDateSet = true; }
	public long getCommitmentTime()						{		return commitmentTime;		}
	
//	public void addDemandData(long date, double quantity, long currentTime) {
	public void addDemandData(long date, double quantity) {	

		Long dateInLong = new Long(date);
//		Long currentTimeInLong = new Long(currentTime);
//		dateHistory.add(currentTimeInLong);

		UnitDemand unitDemand = (UnitDemand) demandHistory.get(dateInLong);

		if (unitDemand == null)	{
			unitDemand = new UnitDemand(date,quantity);
		} else {
			unitDemand.quantity += quantity;
		}

		demandHistory.put(dateInLong,unitDemand);

		// DEBUG
/*
		myLoggingService.shout("[PREDICTOR]DemandHistoryForAnItem : after adding. "+ quantity+ " "+itemName+" for "+date+" at " + System.currentTimeMillis());

		Collection dump = (Collection) demandHistory.values();

		for (Iterator iter = dump.iterator();iter.hasNext() ; ){
				UnitDemand unitDemand2 = (UnitDemand) iter.next();
				myLoggingService.shout("[PREDICTOR]DemandHistoryForAnItem : " + unitDemand2.date + "," + unitDemand2.quantity);
		}
*/
	}

//	public boolean removeDemandData(long date, double quantity, long currentTime) {
	public boolean removeDemandData(long date, double quantity) {	

		Long dateInLong = new Long(date);

//		Long currentTimeInLong = new Long(currentTime);
//		dateHistory.add(currentTimeInLong);

		UnitDemand unitDemand = (UnitDemand) demandHistory.get(dateInLong);

		if (unitDemand == null)		{
/*
			myLoggingService.shout("[PREDICTOR]DemandHistoryForAnItem : Unknown Demand was tried to be removed. "+ quantity+ " "+itemName+" for "+date+" at " + System.currentTimeMillis());
			
			myLoggingService.shout("[PREDICTOR]DemandHistoryForAnItem : Does it exist ? " +  demandHistory.containsKey(dateInLong));

			myLoggingService.shout("[PREDICTOR]DemandHistoryForAnItem : Keys");

			Collection dump1 = (Collection) demandHistory.keySet() ;

			for (Iterator iter = dump1.iterator();iter.hasNext() ; ){
				Long key = (Long) iter.next();
				myLoggingService.shout("[PREDICTOR]DemandHistoryForAnItem : key = " + key);
			}

			Collection dump = (Collection) demandHistory.values();

			for (Iterator iter = dump.iterator();iter.hasNext() ; ){
				UnitDemand unitDemand2 = (UnitDemand) iter.next();
				myLoggingService.shout("[PREDICTOR]DemandHistoryForAnItem : " + unitDemand2.date + "," + unitDemand2.quantity);
			}
*/
			return false;

		} else {
			unitDemand.quantity -= quantity;
		}

		if (unitDemand.quantity > 0)	{
			demandHistory.put(dateInLong,unitDemand);
		} else {
			demandHistory.remove(dateInLong);
		}
		
		// Reset Lead time
//		Long tempEndDate = (Long) demandHistory.lastKey();
//		setLeadTime(tempEndDate.longValue()- currentTime);

		// DEBUG
/*
		myLoggingService.shout("[PREDICTOR]DemandHistoryForAnItem : after removing. "+ quantity+ " "+itemName+" for "+date+" at " + System.currentTimeMillis());

		Collection dump = (Collection) demandHistory.values();

		for (Iterator iter = dump.iterator();iter.hasNext() ; ){
				UnitDemand unitDemand2 = (UnitDemand) iter.next();
				myLoggingService.shout("[PREDICTOR]DemandHistoryForAnItem : " + unitDemand2.date + "," + unitDemand2.quantity);
		}
*/
		return true;
	}
	
	public double averagePast(long timeWindow, long targetDate) {

		boolean notFound = true;
		UnitDemand tempUnitDemand = null;
		double sumOfQuantity = 0; 
		int tw = 0;
		long interval = 0, prev_date = 0, most_recent_date = 0; 
			
		Collection recordCollection = demandHistory.values();

		for (Iterator iter = recordCollection.iterator();iter.hasNext() && tw < timeWindow; )
		{
			tw++;
			UnitDemand unitDemand = (UnitDemand)iter.next();
			if (prev_date > 0 )	{
				interval +=  (prev_date - unitDemand.date);
			}

			if (most_recent_date ==0)	{
				most_recent_date = unitDemand.date;
			}

			prev_date = unitDemand.date;
			sumOfQuantity += unitDemand.quantity;
//			myLoggingService.shout("[PREDICTOR]averagePast : "+unitDemand.date+","+unitDemand.quantity+","+targetDate+","+itemName);	// for Debug
		}
		
		if (tw >1)	{
			if ((int)Math.floor(interval/(tw-1)) + most_recent_date > targetDate)	{
				return 0;
			}
		} 

		return sumOfQuantity/tw;
	}


	public double averagePast(long timeWindow) {

		boolean notFound = true;
		UnitDemand tempUnitDemand = null;
		double sumOfQuantity = 0; 
		int tw = 0;
			
		Collection recordCollection = demandHistory.values();

		for (Iterator iter = recordCollection.iterator();iter.hasNext() && tw < timeWindow; )
		{
			tw++;
			UnitDemand unitDemand = (UnitDemand)iter.next();

			sumOfQuantity += unitDemand.quantity;
//			myLoggingService.shout("[PREDICTOR]averagePast : "+unitDemand.date+","+unitDemand.quantity+","+itemName);	// for Debug
		}
		
		return sumOfQuantity/tw;
	}

	public long nextEstimatedEndtime() {

		long interval = 0, averageInteval = 0, prev_date =0, most_recent_date = 0;
		int tw = 0;

		Collection recordCollection = demandHistory.values();

		if (recordCollection.size() <=1 )	{
			return 1;
		}

		for (Iterator iter = recordCollection.iterator();iter.hasNext(); )
		{
			UnitDemand unitDemand = (UnitDemand)iter.next();
			if (prev_date> 0 )	{
				interval +=  (prev_date - unitDemand.date);
			}

			if (most_recent_date ==0)	{
				most_recent_date = unitDemand.date;
			}

			prev_date = unitDemand.date;
			tw++;
		}

		return (long) (most_recent_date + (int)Math.floor(interval/(tw-1)));
	}


	public long nextEstimatedCommitmentTime() {

		long interval = 0, averageInteval = 0, prev_date =0, most_recent_date = 0;
		int tw = 0;

		Collection recordCollection = demandHistory.values();

		if (recordCollection.size() <=1 )	{
			return 0;
		}

		for (Iterator iter = recordCollection.iterator();iter.hasNext(); )
		{
			UnitDemand unitDemand = (UnitDemand)iter.next();
			if (prev_date> 0 )	{
				interval +=  prev_date - unitDemand.date;
			}

			if (most_recent_date ==0)	{
				most_recent_date = unitDemand.date;
			}

			prev_date = unitDemand.date;
			tw++;
		}

		return (long) (most_recent_date + (int)Math.floor(interval/(tw-1)));
	}


	public double[] getHistoryOf(int days) {

		double [] history = new double[days];

		boolean notFound = true;
		UnitDemand tempUnitDemand = null;
		double sumOfQuantity = 0; 
		int tw = 0;
		long interval = 0, prev_date = 0, most_recent_date = 0; 
			
		Collection recordCollection = demandHistory.values();

		for (Iterator iter = recordCollection.iterator();iter.hasNext() && tw < days; )
		{
			UnitDemand unitDemand = (UnitDemand)iter.next();

//			if (prev_date > 0 )	{
//				interval +=  prev_date - unitDemand.date;
//			}

//			if (most_recent_date ==0)	{
//				most_recent_date = unitDemand.date;
//			}

//			prev_date = unitDemand.date;
//			sumOfQuantity += unitDemand.quantity;

			history[tw] = unitDemand.quantity;
			tw++;
		}
		
		if (tw < days)	{
			return null;
		}

		return history;
	}

	public String getName() {
		return itemName;
	}

	public String getOfType() {
		return ofType;
	}

	public MaintainedItem getMaintainedItem()	{
		return maintainedItem;
	}

	public long getMaxEndTimeInHistory() {
	
		if (demandHistory.size()==0)	{
			return -1;
		}
		Long tempEndDate = (Long) demandHistory.firstKey();
//		if (tempEndDate == null)	{
//			return -1;
//		}	
		return tempEndDate.longValue();
	}

	public long getAverageInterval() {

		long interval = 0, prev_date =0;
		int tw = 0;

		Collection recordCollection = demandHistory.values();

		if (recordCollection.size() <=1 )	{
			return 1;
		}

		for (Iterator iter = recordCollection.iterator();iter.hasNext(); )
		{
			UnitDemand unitDemand = (UnitDemand)iter.next();
			if (prev_date> 0 )	{
				interval +=  prev_date - unitDemand.date;
			}

			prev_date = unitDemand.date;
			tw++;
		}

		return (long) Math.floor(interval/(tw-1));
	}
/*
	private class UnitDemand implements java.io.Serializable {
		public long date = 0;			// end date
		public long commitmentdate = 0;	// commitment date
		public double quantity = 0;
		
		public UnitDemand(long date, double quantity) {
			this.date = date; this.quantity = quantity; 
		}
	};

	private class DateComparator implements Comparator {
		
		public int compare(Object o1, Object o2) {
		    if (o1 == o2) return 0;
	    
		    Long x = (Long) o1;
		    Long y = (Long) o2;
		    long x_date = x.longValue();
		    long y_date = y.longValue();
				
		    // Smaller dates are less preferable
		    if (x_date < y_date)
				return 1;
		    else if (x_date > y_date) 
				return -1;
		    else 
				return 0;
		}
    }
*/
}