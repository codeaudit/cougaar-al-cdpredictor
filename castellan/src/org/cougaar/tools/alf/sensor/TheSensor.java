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

import org.cougaar.tools.alf.sensor.plugin.*;
// import org.cougaar.tools.alf.sensor.TheLoadForecaster;

import org.cougaar.core.adaptivity.InterAgentOperatingMode;
//import org.cougaar.core.agent.ClusterIdentifier; Changed by Himanshu
import org.cougaar.core.agent.*; //Added by Himanshu
import org.cougaar.core.mts.MessageAddress;//Added by Himanshu
import org.cougaar.core.plugin.*;
import org.cougaar.core.service.*;
import org.cougaar.core.adaptivity.OMCRangeList;
import org.cougaar.util.ConfigFinder;

import org.w3c.dom.*;

import java.util.* ;
import java.lang.Float;
import java.lang.Double;
import java.lang.Integer;
import java.lang.String;
import java.lang.Math;
import java.io.File;

/**
 *
 * @author  Yunho Hong
 * @version
 */

public class TheSensor implements java.io.Serializable {


	private long starttime = -1L;	// The time when the first GetLogSupport arrives.
	private HashMap TallyTableMap;  // Maintain the information of counting tasks
	private java.io.BufferedWriter FallingBehind ; // Log file
	private TheLoadForecaster loadforecaster;
	private int Hongfbtype;

	SensorPlugin sensorplugin;
	InterAgentOperatingMode[] psu_lf3;

   	// Costructor
	public TheSensor(SensorPlugin plugin, String fbtype) {
	  
	  sensorplugin = plugin;
	  Hongfbtype = 2;
	  loadforecaster = new TheLoadForecaster(plugin);

	  try
	  {
      	 FallingBehind = new java.io.BufferedWriter ( new java.io.FileWriter("fallingbheind.txt", true ));
	  } 
	  catch (java.io.IOException ioexc)
	  {
		 System.err.println ("can't write file, io error" );
	  }

	  // Table for checking number of tasks by agents
	  TallyTableMap = new HashMap();

	  // July 1, 2002
	  // get Threshold from a file
	  // Two types of files : SV or user defined threshold 
	  if (fbtype.compareToIgnoreCase("LookupTable") == 0)
	  {
		  getThresholdforLookupTable(); // read user defined threshold values from a file
	  } else {
		  getSV();						// read support vector in order to build classifier from files which correspond to each agent one by one.
	  }
    }

	// read user defined threshold values from a file
	private void getThresholdforLookupTable() {

		ConfigFinder finder = sensorplugin.obtainConfigFinder() ;
		String inputName = "lookup.txt";
       
		try {

			if ( inputName != null && finder != null ) {

				File inputFile = finder.locateFile( inputName ) ;

                if ( inputFile != null && inputFile.exists() ) {
                    
					java.io.BufferedReader input_file = new java.io.BufferedReader ( new java.io.FileReader(inputFile));					

					read_lookupTable(input_file);

					input_file.close();
                }

            }
        } catch ( Exception e ) {
            e.printStackTrace() ;
        }

	}

	// 	Read threshold values specified by user and put them into lookup tables
	private void read_lookupTable(java.io.BufferedReader input_stream) {

		String current_agent = null;
		Hashtable LookupTable = null;
		String s = null;
		Long From; 
		int FbLevel = 0;
		float LLThreshold = 0, ULThreshold = 0;
		int lt = 0; 
		int st = 0;

		try
		{
			while ((s = input_stream.readLine()) != null)
			{
				int is = 0;
				int ix = s.indexOf(" ");

				String temp_string = s.substring(is,ix);
				
				if(temp_string.charAt(0) == '#'){   // # represents Comment

					continue;

				} else if (temp_string.compareToIgnoreCase("@agent") == 0)	{

					if (current_agent != null)
					{
						TallyTableMap.put(current_agent, new TheFallingBehindSensor2(current_agent, 1000, Integer.MAX_VALUE , LookupTable, sensorplugin));
					}
					
					st = ix+1;
					current_agent = s.substring(st);
					current_agent = current_agent.trim();
					LookupTable	= new Hashtable();

					// debug
					System.out.println("agent : " + current_agent);

					continue;
				}

				// from	 (specific time)
				From = new Long(temp_string.trim());

				// Level
				is = ix + 1;
				ix = s.indexOf(" ",is);	
				FbLevel = (Integer.valueOf(s.substring(is,ix).trim())).intValue();

				// Lower limit threshold value
				is = ix + 1;
				ix = s.indexOf(" ",is);	
				LLThreshold = (Float.valueOf(s.substring(is,ix).trim())).floatValue();

				// Upper limit threshold value
				st = ix+1;
				ULThreshold = (Float.valueOf(s.substring(st).trim())).floatValue();
				
				// debug
				System.out.println("lookup: " + From + ", " + FbLevel + ", " + LLThreshold + ", " + ULThreshold);
				Vector ThresholdList = null;
				if ((ThresholdList = (Vector) LookupTable.get(From)) == null)
				{
					ThresholdList = new Vector();
				}
				ThresholdList.add(new ConditionByNo( FbLevel, LLThreshold, ULThreshold, 0));
				LookupTable.put(From,ThresholdList);  // Last part represents level of falling behindness. 
													  // Here, I just set one level of falling behindness. 
													  // In the future, if we specify more levels, then it will have meaning. 

			}

			TallyTableMap.put(current_agent, new TheFallingBehindSensor2(current_agent, 1000, Integer.MAX_VALUE , LookupTable, sensorplugin));

		} 
		catch (java.io.IOException ioexc)
	    {
		    System.err.println ("can't read the input file, io error" );
	    }
	}

	// retrieve support vector to build threshold function
	private void getSV() {


		ConfigFinder finder = sensorplugin.obtainConfigFinder() ;
		String inputName = "param.dat";
		String paramName = "txt.svm";
       
		try {

			if ( paramName != null && finder != null ) {
                File inputFile = finder.locateFile( inputName ) ;
                File paramFile = finder.locateFile( paramName ) ;

                if ( paramFile != null && paramFile.exists() ) {

					java.io.BufferedReader param_File = new java.io.BufferedReader ( new java.io.FileReader(paramFile));					

					read_input(param_File, "47-FSB");

					param_File.close();

				}

                if ( inputFile != null && inputFile.exists() ) {
                    
					java.io.BufferedReader input_file = new java.io.BufferedReader ( new java.io.FileReader(inputFile));					

					read_input(input_file, "47-FSB");

					input_file.close();
                }

            }
        } catch ( Exception e ) {
            e.printStackTrace() ;
        }
	}

	private void read_param(java.io.BufferedReader param_stream){ // read parameters

		String s = null;
		String type = null;
		float gamma, degree, a, b, v, C, epsilon;
		int lt = 0, st = 0;

		try
		{
			while ((s = param_stream.readLine()) != null)
			{
				int is = 0;
				int ix = s.indexOf(" ");

				String temp_string = s.substring(is,ix);
				
				if(temp_string.charAt(0) == '#'){   // # represents Comment
					continue;
				}

				if(temp_string.compareToIgnoreCase("type") == 0) {	// kernel type
				
					lt = s.length(); st = ix+1;
					type =	(s.substring(st, lt)).trim();

				} else if (temp_string.compareToIgnoreCase("gamma") == 0) {

					lt = s.length(); st = ix+1;
					gamma =	(Float.valueOf((String) s.substring(st, lt))).floatValue();

				} else if (temp_string.compareToIgnoreCase("degree") == 0) {
	
					lt = s.length(); st = ix+1;
					degree = (Float.valueOf((String) s.substring(st, lt))).floatValue();

				} else if (temp_string.compareToIgnoreCase("a") == 0) {
	
					lt = s.length(); st = ix+1;
					a =	(Float.valueOf((String) s.substring(st, lt))).floatValue();

				} else if (temp_string.compareToIgnoreCase("b") == 0) {

					lt = s.length(); st = ix+1;
					b =	(Float.valueOf((String) s.substring(st, lt))).floatValue();

				} else if (temp_string.compareToIgnoreCase("C") == 0) {

					lt = s.length(); st = ix+1;
					C =	(Float.valueOf((String) s.substring(st, lt))).floatValue();

				} else if (temp_string.compareToIgnoreCase("epsilon") == 0) {

					lt = s.length(); st = ix+1;
					epsilon = (Float.valueOf((String) s.substring(st, lt))).floatValue();

				} else if (temp_string.compareToIgnoreCase("") == 0) {

					lt = s.length(); st = ix+1;
					epsilon = (Float.valueOf((String) s.substring(st, lt))).floatValue();
				}  
			}
		} 
		catch (java.io.IOException ioexc)
	    {
		    System.err.println ("can't read the input file, io error" );
	    }
	}

	// read SV from files.
	private void read_input(java.io.BufferedReader input_stream, String agent){ 

		String s = null;
		int is_linear=1; // linear kernel?
		int i=0;
		int dimension = 0;
		int number = 0;
		float b = 0;

		// SV 
		double [][] X = null;
		double [] y = null;
		double [] lamda = null;
		boolean param = true;

		try
		{
			while ((s = input_stream.readLine()) != null)
			{
				int is = 0;
				int ix = s.indexOf(" ");

				String temp_string = s.substring(is,ix);
				
				if(temp_string.charAt(0) == '#'){   // # represents Comment
					continue;
				}

				if (param == true)
				{
					if(temp_string.compareToIgnoreCase("dimension") == 0) {	// read parameters

						dimension =	(Integer.valueOf((String) s.substring(ix+1, s.length()))).intValue();
	
					} else if(temp_string.compareToIgnoreCase("number") == 0) {	// read parameters
						
						number =	(Integer.valueOf((String) s.substring(ix+1, s.length()))).intValue();
	
					} else if(temp_string.compareToIgnoreCase("b") == 0) {	// read parameters

						b =	(Float.valueOf((String) s.substring(ix+1, s.length()))).floatValue();
					} else if(temp_string.compareToIgnoreCase("format") == 0) {	// read parameters

						param = false;
						X = new double[number][dimension];
						y = new double[number];
						lamda = new double[number];
					}

				} else {
					
					X[i][0] = (Double.valueOf((String) s.substring(is,ix))).doubleValue();
					is = ix+1;

					for (int j=1;j<dimension;j++)
					{
							ix = s.indexOf(" ",is);	
							X[i][j] = (Double.valueOf((String) s.substring(is,ix))).doubleValue();
							is = ix+1;
					}

					ix = s.indexOf(" ",is);	
					y[i] = (Double.valueOf((String) s.substring(is,ix))).doubleValue();

					lamda[i] = (Double.valueOf((String) s.substring(ix+1, s.length()))).doubleValue();
				}
			}
		} 
		catch (java.io.IOException ioexc)
	    {
		    System.err.println ("can't read the input file, io error" );
	    }


		
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
						loadforecaster.printout("\n GetLogSupport time = " + starttime,true);
						loadforecaster.printout("The task is " + pdu.toString(),true);
					}
				}

			} else {

				if ( p instanceof TaskPDU ) {

					TaskPDU tpdu = ( TaskPDU ) p ;

					String s = tpdu.getTaskVerb().toString();

					if (s.compareToIgnoreCase("Ready")== 0|| s.compareToIgnoreCase("Finish")== 0||  // if this pdu is not an add event.
						s.compareToIgnoreCase("Sample")== 0|| s.compareToIgnoreCase("GetLogSupport")== 0|| s.compareToIgnoreCase("ReportForDuty")== 0||
						s.compareToIgnoreCase("ReportForService")== 0|| s.compareToIgnoreCase("Start")== 0)
					{
						return;
					}

//					System.out.println(tpdu.getSource()+","+s);
					TheFallingBehindSensor2 tinfo = (TheFallingBehindSensor2) TallyTableMap.get(tpdu.getSource());
					if (tinfo==null) return;

//					System.out.println(tpdu.getSource()+","+s);

					String t = tpdu.getDirectObject();

					if (tinfo.StartTime == 0 )
					{
						if (tpdu.getAction() == 0 && tpdu.getDirectObject() != null)
						{
							tinfo.StartTime = tpdu.getTime();
						//	printout("\nA: " + tpdu.getSource() + "'s start time = " + ((float)(tinfo.StartTime - starttime)/1000), ForecastResult,true);
						}
					}

					// Load forecasting
					if (t!=null) {
						if (t.equalsIgnoreCase("5") == true) {

//							System.out.println(tpdu.getSource()+","+s+","+t+"second");

							if (((Long)(tinfo.stime).get(t)).intValue() == 0) {
//								System.out.println(tpdu.getSource()+","+s+","+t+"second");
								tinfo.stime.put(t, new Long(tpdu.getTime()));
								loadforecaster.printout("\nA: class "+ t+ ", " + tpdu.getSource() + "'s start time = " + ((float)(tpdu.getTime() - starttime)/1000),true);
								loadforecaster.forecast(tpdu.getSource(),(tpdu.getTime() - starttime)/1000,t);
							}
						}
					}

					if (tinfo.StartTime > 0 )
					{
						// check waiting time
						if (tpdu.getAction() == 0)
						{
							tinfo.add(tpdu,starttime);
						} else if (tpdu.getAction() == 1 && Hongfbtype == 1) { // for type 1, considering the case in which a task is cancelled before allocation
							tinfo.getAverageWaitingTime((UIDStringPDU) tpdu.getUID(), tpdu.getTime());
          				}
					}
				}
  			    else if (p instanceof ExpansionPDU && Hongfbtype == 1)	{
					ExpansionPDU ppdu = (ExpansionPDU) p;
					if (ppdu.getAction() != 0 )	{
						return;
					}
					TheFallingBehindSensor2 tinfo = (TheFallingBehindSensor2) TallyTableMap.get(ppdu.getSource());
					if (tinfo==null) return;
					tinfo.getAverageWaitingTime((UIDStringPDU) ppdu.getParentTask(), ppdu.getTime());
				} else if (p instanceof AggregationPDU && Hongfbtype == 1)	{
					AggregationPDU ppdu = (AggregationPDU) p;
					if (ppdu.getAction() != 0 )	{
						return;
					}
					TheFallingBehindSensor2 tinfo = (TheFallingBehindSensor2) TallyTableMap.get(ppdu.getSource());
					if (tinfo==null) return;
					tinfo.getAverageWaitingTime((UIDStringPDU) ppdu.getTask(), ppdu.getTime());
				} else if (p instanceof AllocationPDU && Hongfbtype == 1)	{
					AllocationPDU ppdu = (AllocationPDU) p;
					if (ppdu.getAction() != 0 )	{
						return;
					}
					TheFallingBehindSensor2 tinfo = (TheFallingBehindSensor2) TallyTableMap.get(ppdu.getSource());
					if (tinfo==null) return;
					tinfo.getAverageWaitingTime((UIDStringPDU) ppdu.getTask(), ppdu.getTime());
				} 
		 	}
    }

	private void printout(String s, java.io.BufferedWriter bw, boolean flag ){

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


	//////////////////////////////////////////////////////////////////////////////////////////////

	class TheFallingBehindSensor2 
	{
		/*
		 *	src : Agent name
		 *	ut :  Unit time
		 *	timelimit1 : timelimit
		 *	lookuptable : lookuptable
		 */

		public TheFallingBehindSensor2(String src, long ut, long timelimit1, Hashtable lookuptable, SensorPlugin sensorplugin1) { 

			nextcheckpoint = 0;
			previoustime = 0;
			StartTime = 0;
			CurrentTime = 0;
			unittime = ut;  // 1 sec
			TaskList = new Hashtable();
			NoFinish = 0;
			CTFinish = 0;
			NoTasks = 0;
			source=src;
			currentstate = 0;
			timelimit = timelimit1;
			lookup = lookuptable;
			over = false;
			sensorplugin = sensorplugin1;

			stime = new Hashtable();

			stime.put("1",new Long("0"));
			stime.put("2",new Long("0"));
			stime.put("3",new Long("0"));
			stime.put("4",new Long("0"));
			stime.put("5",new Long("0"));
			stime.put("9",new Long("0"));
			stime.put("10",new Long("0"));

			if (lookuptable == null) {	over=true;	}
			
			// preparing for falling behind result
		    double [] fbresult =new double[3];
		    fbresult[0]=0; // normal
 	  	    fbresult[1]=1; // mild falling behind
 	  	    fbresult[2]=2; // mild falling behind

//		    UIDService us=(UIDService) getUIDService();
			psu_fb = new InterAgentOperatingMode("FallingBehind", new OMCRangeList(fbresult),new Double(0));
			sensorplugin.publishAdd(psu_fb);

		}

		public void add(TaskPDU tpdu, long starttime) {

			if (over) {	return;	}

//			TaskList.put(((UIDStringPDU)tpdu.getUID()).toString(),new taskinfo(tpdu.getTime()-StartTime));
			NoTasks++;

//			in TIC
//			if(NoTasks%100 == 0) {	
//				checkfallingbehindness2(tpdu.getTime()-starttime, NoTasks);	// Type 2
//			}

			long t = tpdu.getTime()-starttime;

			if (t >= nextcheckpoint)
			{
				System.out.println(source + ", " + t + ", " + NoTasks);
				nextcheckpoint = t - t%1000;
				checkfallingbehindness2(nextcheckpoint, NoTasks);	// Type 2
				nextcheckpoint = nextcheckpoint + 1000;
			}
//			previoustime = t;
		}

		public void checkfallingbehindness2(long curr_time, int num_task) { // for the number of tasks

			boolean FbSV = false;
			int newlydetectedstate = currentstate;

			if (FbSV == true) // check whether SV check falling behindness.
			{

			
			} else {

				// Consult Lookup table

//				long neartime = 1000*Math.round(curr_time/1000);

				Vector thresholdlist = (Vector) lookup.get(new Long(curr_time));

				if (thresholdlist != null)
				{
					for (Enumeration e = thresholdlist.elements() ; e.hasMoreElements() ;) {
						 ConditionByNo k = (ConditionByNo) e.nextElement();
						 if (k.check(num_task, curr_time))
						 {
							newlydetectedstate = k.getLevel();
						 } 
					}

					if (newlydetectedstate != currentstate)
					{
//						 psu_fb.setValue(new Double(status)); 
//			             sensorplugin.publishChange(psu_fb); 

						currentstate = newlydetectedstate;
						psu_fb.setValue(new Double(currentstate));
						sensorplugin.publishChange(psu_fb);
						printout(source+","+num_task+","+curr_time+", Falling behind: level "+ currentstate ,FallingBehind, true);
					}
				}
			}
		}

		public void getAverageWaitingTime(UIDStringPDU uid, long time1) {

//			UIDStringPDU
//			org.cougaar.core.util.UID uid = 
			if (over) {	return;	}

			long time = time1 - StartTime;

			// add cumulative finishtime
			taskinfo tpdu = null;

			if ((tpdu = (taskinfo) TaskList.get(uid))== null)
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

					if ( t!=null) {
						if (t.finishtime < chktime)
						{
							CTFinish = CTFinish + t.finishtime - t.eventtime;
							TaskList.remove(uid.toString());
							NoFinish++;
						} else {
							wt = chktime - t.eventtime;
						}
					}
				}

				int s = TaskList.size();

				awt = (float) (wt+CTFinish)/(NoFinish+s);

				checkfallingbehindness(chktime,awt);
				CurrentTime = CurrentTime + unittime;
				chktime = CurrentTime + unittime;
			} 
		}

		public void checkfallingbehindness(long chktime, float awt) {   // for the average waiting time

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

			if (c[1] > 0 && currentstate < 2)  // Severe falling behind
			{
				// PublishChange falling behindness
				psu_fb.setValue(new Double(2));
				sensorplugin.publishChange(psu_fb);
				printout(source+","+chktime+","+awt+", S, by waiting time",FallingBehind, true);
				currentstate = 2;
			} else 	if (c[0] > 0  && c[1] < 0 && currentstate < 1) {  // Mild falling behind

				// PublishChange falling behindness
				psu_fb.setValue(new Double(1));
				sensorplugin.publishChange(psu_fb);
				printout(source+","+chktime+","+awt+", M, by waiting time",FallingBehind, true);
				currentstate = 1;
			} 

		}

		public long StartTime;
		public long CurrentTime;
		public long unittime;
		public long timelimit;
		public Hashtable stime; // Start time of each logistic item
		private Hashtable TaskList;	
		private Hashtable lookup;	
		private int NoFinish;
		private long CTFinish;  // Cumulative waiting time;
		private float mildfb;
		private float obviousfb;
		private int NoTasks;
		private String source;
		private int currentstate;
		private boolean over;
		private InterAgentOperatingMode psu_fb;
		private long previoustime;
		private long nextcheckpoint;
		SensorPlugin sensorplugin;
	};
	

class ConditionByNo
{
	public ConditionByNo(int FbLevel, float LLThreshold, float ULThreshold, int type1) {
		level = FbLevel;
		LowerLimit = LLThreshold;
		Upperlimit = ULThreshold;  // time;
		type = type1;
	}

	public boolean check(int num_task, long t) {

		if (LowerLimit < num_task && num_task <= Upperlimit) {

			return true;
		}

		return false;
	}

	public int getLevel() {
		return level;
	}

	int level;
	float LowerLimit;
	float Upperlimit;  // time;
	public int type;

};

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
}

