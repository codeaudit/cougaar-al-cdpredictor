/*
  * <copyright>
  *  Copyright 2002 (Penn State University and Intelligent Automation, Inc.)
  *  under sponsorship of the Defense Advanced Research Projects
  *  Agency (DARPA).
  *
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the Cougaar Open Source License as published by
  *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
  *
  *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
  *  PROVIDED "AS IS" WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
  *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
  *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
  *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
  *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
  *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
  *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
  *  PERFORMANCE OF THE COUGAAR SOFTWARE.
  *
  * </copyright>
  *
  */

package org.cougaar.tools.alf.sensor;

import org.cougaar.core.util.UID;
import org.cougaar.tools.castellan.pdu.* ;
import org.cougaar.tools.castellan.util.* ;
import org.cougaar.core.plugin.ComponentPlugin;
import org.cougaar.lib.aggagent.test.TestRelay;

import java.util.* ;
import java.lang.Float;

/**
 *
 * @author  wpeng
 * @version
 */
public class TheSensor {


	private long starttime = -1L; // The time when the first GetLogSupport arrives.
	private HashMap TallyTableMap, Fbthreshold;
	private java.io.BufferedWriter ForecastResult, FallingBehind ;

    //CONSTRUCTORS
    public TheSensor(String FileNameOfThreshold) {

	}

	public TheSensor(LogServerPlugin plugin) {

			String [] agent = { "1-35-ARBN",
								"1-6-INFBN",
								"1-AD",
								"102-POL-SUPPLYCO",
								"106-TCBN",
								"110-POL-SUPPLYCO",
								"123-MSB",
								"16-CSG",
								"18-MAINTBN",
								"191-ORDBN",
								"2-BDE-1-AD",
								"21-TSC-HQ",
                                "227-SUPPLYCO",
                                "28-TCBN",
                                "29-SPTGP",
                                "3-SUPCOM-HQ",
                                "343-SUPPLYCO",
                                "37-TRANSGP",
                                "47-FSB",
                                "485-CSB",
                                "5-CORPS",
                                "51-MAINTBN",
                                "565-RPRPTCO",
                                "592-ORDCO",
                                "6-TCBN",
                                "7-CSG",
                                "71-MAINTBN",
                                "AWR-2",
                                "CONUSGround",
                                "DISCOM-1-AD",
                                "DLAHQ",
                                "GlobalAir",
                                "GlobalSea",
                                "HNS",
                                "NCA",
                                "OSC",
                                "PlanePacker",
                                "ShipPacker",
                                "TheaterGround",
                                "TRANSCOM",
                                "USAEUR",
                                "USEUCOM" };
/*
			float [][] threshold = { {Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE}, // "1-35-ARBN",        
									 {Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE}, // "1-6-INFBN",        
									 {Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE}, // "1-AD",             
									 {Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE}, // "102-POL-SUPPLYCO", 
									 {Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE}, // "106-TCBN",         
									 {Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE}, // "110-POL-SUPPLYCO", 
									 {2000  ,4000, 4000,  20}, // "123-MSB",          
									 {Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE}, // "16-CSG",           
									 {Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE}, // "18-MAINTBN",       
									 {Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE}, // "191-ORDBN",        
									 {Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE}, // "2-BDE-1-AD",       
									 {Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE}, // "21-TSC-HQ",        
									 {Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE}, // "227-SUPPLYCO",     
									 {Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE}, // "28-TCBN",          
									 {Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE}, // "29-SPTGP",         
									 {Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE}, // "3-SUPCOM-HQ",      
									 {Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE}, // "343-SUPPLYCO",     
									 {Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE}, // "37-TRANSGP",       
									 {Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE}, // "47-FSB",           
									 {Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE}, // "485-CSB",          
									 {Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE}, // "5-CORPS",          
									 {Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE}, // "51-MAINTBN",       
									 {Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE}, // "565-RPRPTCO",      
									 {Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE}, // "592-ORDCO",        
									 {Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE}, // "6-TCBN",           
									 {Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE}, // "7-CSG",            
									 {Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE}, // "71-MAINTBN",       
									 {Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE}, // "AWR-2",            
									 {Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE}, // "CONUSGround",      
									 {Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE}, // "DISCOM-1-AD",      
									 {Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE}, // "DLAHQ",            
									 {Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE}, // "GlobalAir",        
									 {Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE}, // "GlobalSea",        
									 {Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE}, // "HNS",              
									 {Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE}, // "NCA",              
									 {Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE}, // "OSC",              
									 {Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE}, // "PlanePacker",      
									 {Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE}, // "ShipPacker",       
									 {Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE}, // "TheaterGround",    
									 {Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE}, // "TRANSCOM",         
									 {Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE}, // "USAEUR",           
									 {Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE}  // "USEUCOM" 
								 };        
*/

		try
		{
			ForecastResult = new java.io.BufferedWriter ( new java.io.FileWriter("forecating.txt", true ));
			FallingBehind = new java.io.BufferedWriter ( new java.io.FileWriter("fallingbheind.txt", true ));
		} 
		catch (java.io.IOException ioexc)
	    {
		    System.err.println ("can't write file, io error" );
	    }

		TallyTableMap = new HashMap();


		for (int i=0;i<agent.length;i++)
		{

			Vector LookupTable = null;
			long timelimit = Long.MAX_VALUE;
			// String src, int ut, float mildfb1, float obviousfb1, float checktime, float th
			if (agent[i].compareToIgnoreCase("47-FSB") == 0)
			{
				LookupTable	= new Vector();
				LookupTable.add(new criteria(    0,45000,3200,1)); 
				LookupTable.add(new criteria(45000,65000,3600,1)); 
				LookupTable.add(new criteria(65000,85000,3800,1)); 
				LookupTable.add(new criteria(85000,105000,4200,1)); 
				LookupTable.add(new criteria(105000,Long.MAX_VALUE,4400,1)); 

				LookupTable.add(new criteria(    0,30000,1800,0)); 
				LookupTable.add(new criteria(30000,45000,2000,0)); 
				LookupTable.add(new criteria(45000,60000,2200,0)); 
				LookupTable.add(new criteria(60000,75000,2400,0)); 
				LookupTable.add(new criteria(75000,95000,2600,0)); 
				LookupTable.add(new criteria(95000,110000,2800,0)); 
				LookupTable.add(new criteria(110000,Long.MAX_VALUE,3000,0)); 
				timelimit = 70000;
			}

			TallyTableMap.put(agent[i], new info(agent[i], 1000, timelimit, LookupTable)); // unit time = 1000 msec.
		}

		// The part for reading threshold data from a file.
//		java.io.BufferedReader fb = null;
/*		
		try
		{
			java.io.BufferedReader fb = new java.io.BufferedReader ( new java.io.FileReader(FileNameOfThreshold));
			// Detection phase 
			// Retrieve threshold data from a file. the data will be used for detecting falling behind.
			// File structure : source, checktime, threshold
			while (fb.ready())
			{
				String rt = fb.readLine(); 

				long [] table = new long[5];
				int ib = 0, ie = 0;

				ie = rt.indexOf(" ", ib);
				String src = rt.substring(ib,ie-1);

				ib = ie + 1;
	
				ie = rt.indexOf(" ", ib);
				table[0] = (Long.valueOf(rt.substring(ib,ie-1))).longValue();

				ib = ie + 1;

				table[1] = (Long.valueOf(rt.substring(ib))).longValue() ;
	
			    TallyTableMap.put(new String(src),table); 
			}
		
		}
		catch (java.io.IOException ioexc)
	    {
		    System.err.println ("can't read or write file, io error" );
			System.err.println ("RbfRidgeRegression constructor");
	    }
*/
    }

    // METHODS
    public synchronized void add( PDU p ) {

			if (starttime == -1L)
			{
				if ( p instanceof TaskPDU ) {

					TaskPDU pdu = ( TaskPDU ) p ;

					if (pdu.getAction() != 0) // if this pdu is not an add event.
					{
						return;
					}

					// check the start time of planning
					if (pdu.getTaskVerb().toString().compareToIgnoreCase("GetLogSupport") == 0)
					{
						starttime = pdu.getTime();
						printout("\n GetLogSupport time = " + starttime,ForecastResult,true);
						printout("The task is " + pdu.toString(),ForecastResult,true);
					}
				}

			} else {

				if ( p instanceof TaskPDU ) {

					TaskPDU tpdu = ( TaskPDU ) p ;

					String s = tpdu.getTaskVerb().toString();

					if (tpdu.getAction() != 0 || s.compareToIgnoreCase("Ready")== 0|| s.compareToIgnoreCase("Finish")== 0||  // if this pdu is not an add event.
						s.compareToIgnoreCase("Sample")== 0|| s.compareToIgnoreCase("GetLogSupport")== 0|| s.compareToIgnoreCase("ReportForDuty")== 0||
						s.compareToIgnoreCase("ReportForService")== 0|| s.compareToIgnoreCase("Start")== 0)
					{
						return;
					}

					// for load forecasting
					info tinfo = (info) TallyTableMap.get(tpdu.getSource());

					if (tinfo.StartTime == 0 )
					{
						if (tpdu.getAction() == 0 && tpdu.getDirectObject() != null)
						{
							if ((tpdu.getDirectObject()).equalsIgnoreCase("1") == true)
							{
//								tinfo.StartTime = tpdu.getTime() - starttime;
								tinfo.StartTime = tpdu.getTime();
								printout("\nA: "+ tpdu.getSource() + "'s start time = " + (tinfo.StartTime - starttime), ForecastResult,true);

								forecast(tpdu.getSource(),(tinfo.StartTime- starttime));
							}
						}
					} else {

						// check waiting time
						if (tpdu.getAction() == 0)
						{
							tinfo.add(tpdu);
						} else if (tpdu.getAction() == 1) {
							tinfo.getAverageWaitingTime((UIDStringPDU) tpdu.getUID(), tpdu.getTime(), tpdu.getSource());
	          			}
					}
					
				} else if (p instanceof ExpansionPDU)	{
					ExpansionPDU ppdu = (ExpansionPDU) p;
					if (ppdu.getAction() != 0 )	{
						return;
					}
					info tinfo = (info) TallyTableMap.get(ppdu.getSource());
					tinfo.getAverageWaitingTime((UIDStringPDU) ppdu.getParentTask(), ppdu.getTime(), ppdu.getSource() );
				} else if (p instanceof AggregationPDU)	{
					AggregationPDU ppdu = (AggregationPDU) p;
					if (ppdu.getAction() != 0 )	{
						return;
					}
					info tinfo = (info) TallyTableMap.get(ppdu.getSource());
					tinfo.getAverageWaitingTime((UIDStringPDU) ppdu.getTask(), ppdu.getTime(), ppdu.getSource() );
				} else if (p instanceof AllocationPDU)	{
					AllocationPDU ppdu = (AllocationPDU) p;
					if (ppdu.getAction() != 0 )	{
						return;
					}
					info tinfo = (info) TallyTableMap.get(ppdu.getSource());
					tinfo.getAverageWaitingTime((UIDStringPDU) ppdu.getTask(), ppdu.getTime(), ppdu.getSource() );
				}
		 	}
    }

	private void forecast(String agentname, long time) {
		
		if (agentname.equalsIgnoreCase("123-MSB") == true)
		{

			printout("F, DLAHQ, start time =, " + (1.2431*time + 30.57),ForecastResult,true);
			printout("F, 227-SupplyCo, start time =, " + (1.1597*time + 19.378),ForecastResult,true);
			printout("F, 343-SupplyCo, start time =, " + (1.2205*time + 23.153),ForecastResult,true);

		} 
		else if (agentname.equalsIgnoreCase("47-FSB") == true)
		{

			printout("F, 227-SupplyCo, start time =, " + (26.676*Math.pow((double) time, 0.6217)),ForecastResult,true);
			printout("F, 343-SupplyCo, start time =, " + (27.895*Math.pow((double) time, 0.6237)),ForecastResult,true);
			printout("F, DLAHQ, start time =, " + (29.447*Math.pow((double) time, 0.6188)),ForecastResult,true);
			printout("F, 123-MSB, start time =, " + (26.65*Math.pow((double) time, 0.5852)),ForecastResult,true);

		} 
		else if (agentname.equalsIgnoreCase("1-35-ARBN") == true || agentname.equalsIgnoreCase("1-6-INFBN") == true )
		{
			printout("F, 47-FSB, start time =, " + (4.6449*time + 17.341),ForecastResult,true);
		} 
	}
	
	private void printout(String s, java.io.BufferedWriter bw, boolean flag ){

		// Create recipient addresses
		MessageAddress source = plugin.getBindingSite().getAgentIdentifier();
		MessageAddress target = new AttributeBasedAddress("", "Role", "Manager");

		// Create Relay object
		TestRelay tr = new TestRelay( getUIDService().nextUID(), source, target, s, null);

		// publish Relay object
		plugin.getBlackboardService().openTransaction();
		plugin.getBlackboardService().publishAdd(tr);
		plugin.getBlackboardService().closeTransaction();

		try
		{
			bw.write(s+"\n");			
			bw.flush();
		}
		catch (java.io.IOException ioexc)
	    {
		    System.err.println ("can't write file, io error" + s);
	    }
		
		if (flag)
		{
			System.out.println(s);
		}
	}

	class info 
	{
		public info(String src, long ut, long timelimit1, Vector lookuptable) {

			StartTime = 0;
			CurrentTime = 0;
			unittime = ut;  // 1 sec
			TaskList = new Hashtable();
			NoFinish = 0;
			CTFinish = 0;
			NoTasks = 0;
			source=src;
//			byno=true;
//		    bywt=true;
			currentstate = 0;
			timelimit = timelimit1;
			lookup = lookuptable;
			if (lookuptable == null)
			{
				over=true;
			}

		}

		public void add(TaskPDU tpdu) {

			if (over)
			{
				return;
			}

			TaskList.put(((UIDStringPDU)tpdu.getUID()).toString(),new taskinfo(tpdu.getTime()-StartTime));
			NoTasks++;

/*
			if (byno)
			{
				if (tpdu.getTime() > CheckTime && NoTasks < threshold )
				{
					printout(source+ "," + tpdu.getTime()+","+ (tpdu.getTime()-StartTime)+"," +NoTasks+", F, by num of tasks",FallingBehind, true);				
					byno = false;
				}
			}
*/
		}

		public void getAverageWaitingTime(UIDStringPDU uid, long time1, String source) {

			if (over) {	return;	}

			long time = time1 - StartTime;

			// add cumulative finishtime
			taskinfo tpdu = null;

			if ((tpdu = (taskinfo) TaskList.get(uid.toString()))== null)
			{
				return;
			}

			long wt = 0; 
			float awt = 0;
			long chktime = CurrentTime + unittime;
			String ss="N";

			if (chktime > time)
			{
				((taskinfo) TaskList.get(uid.toString())).finishtime = time;
				if (timelimit < time) // if the time is over a certain number them it will not calculate the 
				{
					over = true;				
				}
				return;
			}

			while (chktime < time)
			{
				for (Enumeration e = TaskList.keys() ; e.hasMoreElements() ;) {
					taskinfo t = (taskinfo) TaskList.get(e.nextElement()); 
					if (t.finishtime < chktime)
					{
						CTFinish = CTFinish + t.finishtime - t.eventtime;
						TaskList.remove(uid.toString());
						NoFinish++;
					} else {
						wt = chktime - t.eventtime;
					}
				}

				int s = TaskList.size();

				awt = (float) (wt+CTFinish)/(NoFinish+s);

				checkfallingbehindness(chktime,awt);
				CurrentTime = CurrentTime + unittime;
				chktime = CurrentTime + unittime;
			} 
		}

		public void checkfallingbehindness(long chktime, float awt) {

			int []c = { -1, -1 };
			
			for (Enumeration e = lookup.elements() ; e.hasMoreElements() ;) {
				 criteria k = (criteria) e.nextElement();
				 if (k.check(chktime))
				 {
					 if (k.threshold < awt )
					 {
						c[k.type] = 1;
					 } 
				 }
			}

			if (c[1] > 0 && currentstate < 2)
			{
				printout(source+","+chktime+","+awt+", S, by waiting time",FallingBehind, true);
				currentstate = 2;
			} else 	if (c[0] > 0  && c[1] < 0 && currentstate < 1) {
				printout(source+","+chktime+","+awt+", M, by waiting time",FallingBehind, true);
				currentstate = 1;
			} 

		}

		
/*		
		Old code june 9, 2002
		public void getAverageWaitingTime(UIDStringPDU uid, long time, String source) {

			// add cumulative finishtime
			TaskPDU tpdu = null;

			if ((tpdu = (TaskPDU)TaskList.get(uid.toString()))== null)
			{
				return;
			}

			long wt = 0; 
			float awt = 0;
			String ss="N";

			CTFinish = CTFinish + time - tpdu.getTime();
			TaskList.remove(uid.toString());
			NoFinish++;

			if (CurrentTime + unittime < time)
			{

				CurrentTime = time;	
				for (Enumeration e = TaskList.keys() ; e.hasMoreElements() ;) {
				     wt = wt + time - ((TaskPDU)TaskList.get(e.nextElement())).getTime();
				}

				int s = TaskList.size();

				awt = (float) (wt+CTFinish)/(NoFinish+s);

				if ( mildfb > awt)
				{
//					ss="N";
					printout(source+","+time+","+(time-StartTime)+","+awt+", N, by waiting time",FallingBehind, false);
				} else if ( mildfb < awt && obviousfb > awt)	{
//					ss="M";
					printout(source+","+time+","+(time-StartTime)+","+awt+", M, by waiting time",FallingBehind, true);
				} else if ( obviousfb < awt)	{
//					ss="F";
					printout(source+","+time+","+(time-StartTime)+","+awt+", F, by waiting time",FallingBehind, true);
				}
		
			} 
		}
*/


		public long StartTime;
		public long CurrentTime;
		public long unittime;
		public long timelimit;
		private Hashtable TaskList;	
		private Vector lookup;	
		private int NoFinish;
		private long CTFinish;  // Cumulative waiting time;
		private float mildfb;
		private float obviousfb;
		private int NoTasks;
		private String source;
		private int currentstate;
//		private boolean byno;
//		private boolean bywt;
		private boolean over;
	};
}

class criteria
{
	public criteria(long from1, long to1, float threshold1, int type1) {
		from = from1;
		to = to1;
		threshold = threshold1;
		type = type1;
	}

	public boolean check(long time) {

		if (from < time && time <= to)
		{
			return true;
		} 
		return false;
	}

	long from;
	long to;
	public float threshold;
	public int type;

};

class taskinfo
{
	public taskinfo(long e) { 
		eventtime = e;
		finishtime = Long.MAX_VALUE;
	}

	public long eventtime;
	public long finishtime;

};


/*
class threshold
{
	public threshold(float mildfb1, float obviousfb1) {
		mildfb = mildfb1;
		obviousfb = obviousfb1;
	}
	public float mildfb;
	public float obviousfb;
};
*/