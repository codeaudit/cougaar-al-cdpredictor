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

import org.cougaar.core.adaptivity.InterAgentOperatingMode;
import org.cougaar.core.agent.ClusterIdentifier;
import org.cougaar.core.plugin.*;
import org.cougaar.core.service.*;
import org.cougaar.core.adaptivity.OMCRangeList;


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
	private int hari;	

	SensorPlugin sensorplugin;
	InterAgentOperatingMode[] psu_fb;
	InterAgentOperatingMode[] psu_lf3;

    	//CONSTRUCTORS
	 public TheSensor(String FileNameOfThreshold) {

	}

	public TheSensor(SensorPlugin plugin) {
	  
	  sensorplugin = plugin;
	  int length = 133;
	  String [] agent = new String[length];

	  hari = 0;

        // REAR-A NODE
        agent[0]="NATO";
        agent[1]="JSRCMDSE";
        agent[2]="USEUCOM";
        agent[3]="USAEUR";
        agent[4]="5-CORPS";
        agent[5]="HNS";
        agent[6]="5-CORPS-REAR";
        agent[7]="11-AVN-RGT";
        agent[8]="12-AVNBDE";
        agent[9]="130-ENGBDE";
        agent[10]="244-ENGBN-CBTHVY";
        agent[11]="52-ENGBN-CBTHVY";
        agent[12]="18-MPBDE";
        agent[13]="205-MIBDE";
        agent[14]="22-SIGBDE";
        agent[15]="30-MEDBDE";
        agent[16]="69-ADABDE";
        agent[17]="286-ADA-SCCO";
        agent[18]="5-CORPS-ARTY";
        agent[19]="41-FABDE";
        agent[20]="1-27-FABN";
        agent[21]="2-4-FABN-MLRS";
        agent[22]="3-13-FABN-155";
        // REAR-B NODE
        agent[23]="3-SUPCOM-HQ";
        agent[24]="19-MMC";
        agent[25]="208-SCCO";
        agent[26]="27-TCBN-MVTCTRL";
        agent[27]="7-CSG";
        agent[28]="316-POL-SUPPLYBN";
        agent[29]="515-POL-TRKCO";
        agent[30]="900-POL-SUPPLYCO";
        agent[31]="71-MAINTBN";
        agent[32]="240-SSCO";
        agent[33]="317-MAINTCO";
        agent[34]="565-RPRPTCO";
        agent[35]="597-MAINTCO";
        agent[36]="71-ORDCO";
        // REAR-C NODE
        agent[37]="125-ORDBN";
        agent[38]="452-ORDCO";
        agent[39]="529-ORDCO";
        agent[40]="181-TCBN";
        agent[41]="377-HVY-TRKCO";
        agent[42]="41-POL-TRKCO";
        agent[43]="51-MDM-TRKCO";
        agent[44]="561-SSBN";
        agent[45]="541-POL-TRKCO";
        agent[46]="584-MAINTCO";
        // REAR-D NODE
        agent[47]="21-TSC-HQ";
        agent[48]="200-MMC";
        agent[49]="7-TCGP-TPTDD";
        agent[50]="AWR-2";
        agent[51]="RSA";
        agent[52]="37-TRANSGP";
        agent[53]="28-TCBN";
        agent[54]="109-MDM-TRKCO";
        agent[55]="66-MDM-TRKCO";
        agent[56]="68-MDM-TRKCO";
        agent[57]="6-TCBN";
        agent[58]="110-POL-SUPPLYCO";
        agent[59]="416-POL-TRKCO";
        agent[60]="632-MAINTCO";
        // REAR E NODE
        agent[61]="29-SPTGP";
        agent[62]="191-ORDBN";
        agent[63]="23-ORDCO";
        agent[64]="24-ORDCO";
        agent[65]="702-EODDET";
        agent[66]="720-EODDET";
        agent[67]="51-MAINTBN";
        agent[68]="18-PERISH-SUBPLT";
        agent[69]="343-SUPPLYCO";
        agent[70]="5-MAINTCO";
        agent[71]="512-MAINTCO";
        agent[72]="574-SSCO";
        // FWD-A NODE
        agent[73]="1-AD";
        agent[74]="1-AD-DIV";
        agent[75]="1-4-ADABN";
        agent[76]="141-SIGBN";
        agent[77]="501-MIBN-CEWI";
        agent[78]="501-MPCO";
        agent[79]="69-CHEMCO";
        agent[80]="DIVARTY-1-AD";
        agent[81]="1-94-FABN";
        agent[82]="25-FABTRY-TGTACQ";
        agent[83]="DISCOM-1-AD";
        agent[84]="123-MSB";
        // FWD-B NODE
        agent[85]="1-BDE-1-AD";
        agent[86]="1-36-INFBN";
        agent[87]="1-37-ARBN";
        agent[88]="16-ENGBN";
        agent[89]="2-3-FABN";
        agent[90]="2-37-ARBN";
        agent[91]="501-FSB";
        // FWD-C NODE
        agent[92]="2-BDE-1-AD";
        agent[93]="1-35-ARBN";
        agent[94]="1-6-INFBN";
        agent[95]="2-6-INFBN";
        agent[96]="4-27-FABN";
        agent[97]="40-ENGBN";
        agent[98]="47-FSB";
        //  FWD-D NODE
        agent[99]="3-BDE-1-AD";
        agent[100]="1-13-ARBN";
        agent[101]="1-41-INFBN";
        agent[102]="125-FSB";
        agent[103]="2-70-ARBN";
        agent[104]="4-1-FABN";
        agent[105]="70-ENGBN";
        // FWD-E NODE
        agent[106]="AVNBDE-1-AD";
        agent[107]="1-1-CAVSQDN";
        agent[108]="1-501-AVNBN";
        agent[109]="127-DASB";
        agent[110]="2-501-AVNBN";
        // FWD-F NODE
        agent[111]="16-CSG";
        agent[112]="485-CSB";
        agent[113]="102-POL-SUPPLYCO";
        agent[114]="26-SSCO";
        agent[115]="588-MAINTCO";
        agent[116]="592-ORDCO";
        agent[117]="596-MAINTCO";
        agent[118]="18-MAINTBN";
        agent[119]="226-MAINTCO";
        agent[120]="227-SUPPLYCO";
        agent[121]="263-FLDSVC-CO";
        agent[122]="77-MAINTCO";
        agent[123]="106-TCBN";
        agent[124]="15-PLS-TRKCO";
        agent[125]="238-POL-TRKCO";
        agent[126]="372-CGO-TRANSCO";
        agent[127]="594-MDM-TRKCO";
        agent[128]="DLAHQ";

	  // TRANSPORT
        agent[129]="TheaterGround";
        agent[130]="GlobalSea";
	  agent[131]="CONUSGround";
	  agent[132]="TRANSCOM";

	  UIDService us=(UIDService) sensorplugin.getUIDService();

	  psu_fb = new InterAgentOperatingMode[agent.length];
	  psu_lf3 = new InterAgentOperatingMode[agent.length];

	  // falling behind result
        double [] fbresult =new double[2];
        fbresult[0]=0; // normal
	  fbresult[1]=1; // mild falling behind
//	  fbresult[2]=2; // severe falling behind

	  for (int i=0; i<agent.length; i++) {

		psu_fb[i]= new InterAgentOperatingMode("FallingBehind", new OMCRangeList(fbresult),new Double(0));
		psu_fb[i].setTarget(new ClusterIdentifier(agent[i]));
		psu_fb[i].setUID(us.nextUID());
		sensorplugin.publishAdd(psu_fb[i]);

	  }

  	  for (int i=0; i<agent.length; i++) {

		psu_lf3[i]= new InterAgentOperatingMode("PSU_Loadforecaster_Class3", new OMCRangeList(new Double(0),new Double(Double.MAX_VALUE)),new Double(0));
		psu_lf3[i].setTarget(new ClusterIdentifier(agent[i]));
		psu_lf3[i].setUID(us.nextUID());
		sensorplugin.publishAdd(psu_fb[i]);
	  }

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

/*
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
*/
			int num_tasklimit = Integer.MAX_VALUE;
			// String src, int ut, float mildfb1, float obviousfb1, float checktime, float th
			if (agent[i].compareToIgnoreCase("47-FSB") == 0)
			{
				LookupTable	= new Vector();
				// Severe
/*
				LookupTable.add(new ConditionByNo(    0,45000,3200,1)); // range (from, to), threshold, type={m,s}
				LookupTable.add(new ConditionByNo(45000,65000,3600,1)); 
				LookupTable.add(new ConditionByNo(65000,85000,3800,1)); 
				LookupTable.add(new ConditionByNo(85000,105000,4200,1)); 
				LookupTable.add(new ConditionByNo(105000,Integer.MAX_VALUE,4400,1)); 
*/
				// Mild
				LookupTable.add(new ConditionByNo(    0, 100, 9000,0)); 
				LookupTable.add(new ConditionByNo(  100, 250,11000,0)); 
//				LookupTable.add(new ConditionByNo(110000,Integer.MAX_VALUE,3000,0)); 
				num_tasklimit = Integer.MAX_VALUE;
			} 
			else 	if (agent[i].compareToIgnoreCase("TheaterGround") == 0)
			{
				LookupTable	= new Vector();
				// Severe
/*
				LookupTable.add(new ConditionByNo(    0,45000,3200,1)); // range (from, to), threshold, type={m,s}
				LookupTable.add(new ConditionByNo(45000,65000,3600,1)); 
				LookupTable.add(new ConditionByNo(65000,85000,3800,1)); 
				LookupTable.add(new ConditionByNo(85000,105000,4200,1)); 
				LookupTable.add(new ConditionByNo(105000,Integer.MAX_VALUE,4400,1)); 
*/
				// Mild
				LookupTable.add(new ConditionByNo(    0, 1000,27000,0)); 
				LookupTable.add(new ConditionByNo( 1000, 2250,29000,0)); 
				LookupTable.add(new ConditionByNo( 2250, 5000,32000,0)); 
				LookupTable.add(new ConditionByNo( 5000, 8000,42000,0)); 
//				LookupTable.add(new ConditionByNo(110000,Integer.MAX_VALUE,3000,0)); 
				num_tasklimit = Integer.MAX_VALUE;
			}
			else 	if (agent[i].compareToIgnoreCase("GlobalSea") == 0)
			{
				// Trasport, Transit task
				LookupTable	= new Vector();
/*
				// Severe
				LookupTable.add(new ConditionByNo(    0, 1,18000,1)); // range (from, to), threshold, type={m,s}
				LookupTable.add(new ConditionByNo(1,5000,28000,1)); 
				LookupTable.add(new ConditionByNo(45000,65000,3600,1)); 
				LookupTable.add(new ConditionByNo(65000,85000,3800,1)); 
				LookupTable.add(new ConditionByNo(85000,105000,4200,1)); 
				LookupTable.add(new ConditionByNo(105000,Integer.MAX_VALUE,4400,1)); 
*/
				// Mild
				LookupTable.add(new ConditionByNo(    0,  500,17000,0)); 
				LookupTable.add(new ConditionByNo(  500, 5000,25000,0)); 
				LookupTable.add(new ConditionByNo( 5000,10000,55000,0)); 
				LookupTable.add(new ConditionByNo(10000,15000,140000,0)); 
//				LookupTable.add(new ConditionByNo(110000,Integer.MAX_VALUE,3000,0)); 
				num_tasklimit = Integer.MAX_VALUE;
			}
			else 	if (agent[i].compareToIgnoreCase("CONUSGround") == 0)
			{
				LookupTable	= new Vector();
/*
				// Severe
				LookupTable.add(new ConditionByNo(    0,45000,3200,1)); // range (from, to), threshold, type={m,s}
				LookupTable.add(new ConditionByNo(45000,65000,3600,1)); 
				LookupTable.add(new ConditionByNo(65000,85000,3800,1)); 
				LookupTable.add(new ConditionByNo(85000,105000,4200,1)); 
				LookupTable.add(new ConditionByNo(105000,Integer.MAX_VALUE,4400,1)); 
*/

				// Mild
				LookupTable.add(new ConditionByNo(    0, 1000,120000,0)); 
				LookupTable.add(new ConditionByNo( 1000, 3000,140000,0)); 
				LookupTable.add(new ConditionByNo( 3000, 6000,150000,0)); 
				num_tasklimit = Integer.MAX_VALUE;
			}			
			else 	if (agent[i].compareToIgnoreCase("TRANSCOM") == 0)
			{
				LookupTable	= new Vector();

				// Mild
				LookupTable.add(new ConditionByNo(    0,  300, 8000,0)); 
				LookupTable.add(new ConditionByNo(  300, 1000, 9000,0)); 
				LookupTable.add(new ConditionByNo( 1000, 2000,10000,0)); 
				LookupTable.add(new ConditionByNo( 2000, 3000,11000,0)); 
				LookupTable.add(new ConditionByNo( 3000, 4000,14000,0)); 
				num_tasklimit = Integer.MAX_VALUE;
			}


			TallyTableMap.put(agent[i], new info(agent[i], i, 1000, num_tasklimit , LookupTable)); // unit time = 1000 msec.
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
//					System.out.println(tpdu.getSource()+","+s);
					info tinfo = (info) TallyTableMap.get(tpdu.getSource());
<<<<<<< TheSensor.java
					if (tinfo==null) return;

//					System.out.println(tpdu.getSource()+","+s);

					String t = tpdu.getDirectObject();
=======
					if (tinfo==null) return;

					System.out.println(tpdu.getSource()+","+s);

					String t = tpdu.getDirectObject();

					if (t.equalsIgnoreCase("10") != true) {

						System.out.println(tpdu.getSource()+","+s+","+t+"second");

						if (((Long)(tinfo.stime).get(t)).intValue() == 0) {
							tinfo.stime.put(t, new Long(tpdu.getTime()));
							printout("\nA: class "+ t+ ", " + tpdu.getSource() + "'s start time = " + (tinfo.StartTime - starttime), ForecastResult,true);
							forecast(tpdu.getSource(),(tinfo.StartTime - starttime),t);
						}
					}
>>>>>>> 1.4

					if (tinfo.StartTime == 0 )
					{
						if (tpdu.getAction() == 0 && tpdu.getDirectObject() != null)
						{
<<<<<<< TheSensor.java
							tinfo.StartTime = tpdu.getTime();
						//	printout("\nA: " + tpdu.getSource() + "'s start time = " + ((float)(tinfo.StartTime - starttime)/1000), ForecastResult,true);
						
=======
							tinfo.StartTime = tpdu.getTime();
							System.out.println(tpdu.getSource()+","+s+","+t+"second" + tinfo.StartTime);
/*		June 12, 2002							
							if ((tpdu.getDirectObject()).equalsIgnoreCase("1") == true)
							{
								tinfo.StartTime = tpdu.getTime();
								printout("\nA: "+ tpdu.getSource() + "'s start time = " + (tinfo.StartTime - starttime), ForecastResult,true);
>>>>>>> 1.4

						}
					}

					if (t!=null) {
						if (t.equalsIgnoreCase("5") == true) {

//							System.out.println(tpdu.getSource()+","+s+","+t+"second");

							if (((Long)(tinfo.stime).get(t)).intValue() == 0) {
//								System.out.println(tpdu.getSource()+","+s+","+t+"second");
								tinfo.stime.put(t, new Long(tpdu.getTime()));
								printout("\nA: class "+ t+ ", " + tpdu.getSource() + "'s start time = " + ((float)(tpdu.getTime() - starttime)/1000), ForecastResult,true);
								forecast(tpdu.getSource(),(tpdu.getTime() - starttime)/1000,t);
							}
*/
						}
					}

					if (tinfo.StartTime > 0 )
					{

						// check waiting time
						if (tpdu.getAction() == 0)
						{
							tinfo.add(tpdu);
						} else if (tpdu.getAction() == 1) {
//							tinfo.getAverageWaitingTime((UIDStringPDU) tpdu.getUID(), tpdu.getTime(), tpdu.getSource());
	          				}
					}
					
				}
/* 
				  else if (p instanceof ExpansionPDU)	{
					ExpansionPDU ppdu = (ExpansionPDU) p;
					if (ppdu.getAction() != 0 )	{
						return;
					}
					info tinfo = (info) TallyTableMap.get(ppdu.getSource());
					if (tinfo==null) return;
					tinfo.getAverageWaitingTime((UIDStringPDU) ppdu.getParentTask(), ppdu.getTime(), ppdu.getSource() );
				} else if (p instanceof AggregationPDU)	{
					AggregationPDU ppdu = (AggregationPDU) p;
					if (ppdu.getAction() != 0 )	{
						return;
					}
					info tinfo = (info) TallyTableMap.get(ppdu.getSource());
					if (tinfo==null) return;
					tinfo.getAverageWaitingTime((UIDStringPDU) ppdu.getTask(), ppdu.getTime(), ppdu.getSource() );
				} else if (p instanceof AllocationPDU)	{
					AllocationPDU ppdu = (AllocationPDU) p;
					if (ppdu.getAction() != 0 )	{
						return;
					}
					info tinfo = (info) TallyTableMap.get(ppdu.getSource());
					if (tinfo==null) return;
					tinfo.getAverageWaitingTime((UIDStringPDU) ppdu.getTask(), ppdu.getTime(), ppdu.getSource() );
				} 
*/
		 	}
    }

	private void forecast(String agentname, long time, String t) {
		
<<<<<<< TheSensor.java
		if (t.equalsIgnoreCase("5") == true) {

			if (agentname.equalsIgnoreCase("1-27-FABN") == true)
			{

				// 125-ORDBN
				psu_lf3[37].setValue(new Double(1.009*time + 2.8363));				
				sensorplugin.publishChange(psu_lf3[37]);
				printout("F, class "+ t+ ", 125-ORDBN, start time =," + (1.009*time + 2.8363),ForecastResult,true);

				// 191-ORDBN
				psu_lf3[62].setValue(new Double(1.038*time + 3.0388));				
				sensorplugin.publishChange(psu_lf3[62]);
				printout("F, class "+ t+ ", 191-ORDBN, start time =," + (1.038*time + 3.0388),ForecastResult,true);

			} 
			else if (agentname.equalsIgnoreCase("1-6-INFBN") == true && hari == 0)
			{
				// 123-MSB
				psu_lf3[84].setValue(new Double(1.6968*time + 2.4398));				
				sensorplugin.publishChange(psu_lf3[84]);
				printout("F, class "+ t+ ", 123-MSB, start time =," + (1.6968*time + 2.4398),ForecastResult,true);

				// 592-ORDCO
				psu_lf3[116].setValue(new Double(2.3243*time-1.0));				
				sensorplugin.publishChange(psu_lf3[116]);
				printout("F, class "+ t+ ", 592-ORDCO, start time =," + (2.3243*time-1.0),ForecastResult,true);
=======
		if (t.equalsIgnoreCase("1") == true) {
			if (agentname.equalsIgnoreCase("123-MSB") == true)
			{
>>>>>>> 1.4

<<<<<<< TheSensor.java
				hari = 1;
			}
			else if (agentname.equalsIgnoreCase("1-35-ARBN") == true && hari == 0)
			{
				// 123-MSB
				psu_lf3[84].setValue(new Double(1.6968*time + 2.4398));				
				sensorplugin.publishChange(psu_lf3[84]);
				printout("F, class "+ t+ ", 123-MSB, start time =," + (1.6968*time + 2.4398),ForecastResult,true);

				// 592-ORDCO
				psu_lf3[116].setValue(new Double(2.3243*time-1.0));				
				sensorplugin.publishChange(psu_lf3[116]);
				printout("F, class "+ t+ ", 592-ORDCO, start time =," + (2.3243*time-1.0),ForecastResult,true);
=======
				printout("F, class "+ t+ ", " + "DLAHQ, start time =, " + (1.2431*time + 30.57),ForecastResult,true);
				printout("F, class "+ t+ ", " + "227-SupplyCo, start time =, " + (1.1597*time + 19.378),ForecastResult,true);
				printout("F, class "+ t+ ", " + "343-SupplyCo, start time =, " + (1.2205*time + 23.153),ForecastResult,true);
>>>>>>> 1.4

<<<<<<< TheSensor.java
				hari = 1;
			}
		}
=======
			} 
			else if (agentname.equalsIgnoreCase("47-FSB") == true)
			{
>>>>>>> 1.4

<<<<<<< TheSensor.java
=======
				printout("F, class "+ t+ ", " + "227-SupplyCo, start time =, " + (26.676*Math.pow((double) time, 0.6217)),ForecastResult,true);
				printout("F, class "+ t+ ", " + "343-SupplyCo, start time =, " + (27.895*Math.pow((double) time, 0.6237)),ForecastResult,true);
				printout("F, class "+ t+ ", " + "DLAHQ, start time =, " + (29.447*Math.pow((double) time, 0.6188)),ForecastResult,true);
				printout("F, class "+ t+ ", " + "123-MSB, start time =, " + (26.65*Math.pow((double) time, 0.5852)),ForecastResult,true);
>>>>>>> 1.4

<<<<<<< TheSensor.java
=======
			} 
			else if (agentname.equalsIgnoreCase("1-35-ARBN") == true || agentname.equalsIgnoreCase("1-6-INFBN") == true )
			{
				printout("F, class "+ t+ ", " + "47-FSB, start time =, " + (4.6449*time + 17.341),ForecastResult,true);
			} 
		}
>>>>>>> 1.4
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

	class info 
	{
		public info(String src, int srcn, long ut, long timelimit1, Vector lookuptable) {

			hari = 0;
			StartTime = 0;
			CurrentTime = 0;
			unittime = ut;  // 1 sec
			TaskList = new Hashtable();
			NoFinish = 0;
			CTFinish = 0;
			NoTasks = 0;
			source=src;
			srcno = srcn;
			currentstate = 0;
			timelimit = timelimit1;
			lookup = lookuptable;
			if (lookuptable == null)
			{
				over=true;
			}
			
			stime = new Hashtable();

			stime.put("1",new Long("0"));
			stime.put("2",new Long("0"));
			stime.put("3",new Long("0"));
			stime.put("4",new Long("0"));
			stime.put("5",new Long("0"));
			stime.put("9",new Long("0"));
			stime.put("10",new Long("0"));
		}

		public void add(TaskPDU tpdu) {

			if (over)
			{
				return;
			}

//			TaskList.put(((UIDStringPDU)tpdu.getUID()).toString(),new taskinfo(tpdu.getTime()-StartTime));
			NoTasks++;
			if(NoTasks%100 == 0) {
				checkfallingbehindness2(tpdu.getTime()-starttime, NoTasks);
			}
		}

		public void checkfallingbehindness2(long curr_time, int num_task) {

			int []c = { -1, -1 };
			
			for (Enumeration e = lookup.elements() ; e.hasMoreElements() ;) {
				 ConditionByNo k = (ConditionByNo) e.nextElement();
				 if (k.check(num_task, curr_time))
				 {
					c[k.type] = 1;

//					 if (k.threshold < curr_time)
//					 {
//						c[k.type] = 1;
//					 } 
				 } //else {
					//c[k.type] = 0;

		             //}
			}

/*
			if (c[1] > 0 && currentstate < 2)
			{
				printout(source+","+num_task+","+curr_time+", S, by waiting time",FallingBehind, true);
				currentstate = 2;
			} else */
			
			if (c[0] <= 0 && currentstate > 0) {
				// PublishChange falling behindness
				psu_fb[srcno].setValue(new Double(0));
				sensorplugin.publishChange(psu_fb[srcno]);
				printout(source+","+num_task+","+curr_time+", not Falling behind",FallingBehind, true);
				currentstate = 0;
			}

			if (c[0] > 0  && c[1] < 0 && currentstate < 1) {
				// PublishChange falling behindness
				psu_fb[srcno].setValue(new Double(1));
				sensorplugin.publishChange(psu_fb[srcno]);

				printout(source+","+num_task+","+curr_time+", Falling behind",FallingBehind, true);
				currentstate = 1;
			} 

		}
		
		public void getAverageWaitingTime(UIDStringPDU uid, long time1, String source) {

			if (over) {	return;	}

			System.out.print("Hari !");

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

			if (c[1] > 0 && currentstate < 2)  // Severe falling behind
			{
				// PublishChange falling behindness
				psu_fb[srcno].setValue(new Double(2));
				sensorplugin.publishChange(psu_fb[srcno]);
//				state[srcno] = 2;
				printout(source+","+chktime+","+awt+", S, by waiting time",FallingBehind, true);
				currentstate = 2;
			} else 	if (c[0] > 0  && c[1] < 0 && currentstate < 1) {  // Mild falling behind

				// PublishChange falling behindness
				psu_fb[srcno].setValue(new Double(1));
				sensorplugin.publishChange(psu_fb[srcno]);
//				state[srcno] = 1;
				printout(source+","+chktime+","+awt+", M, by waiting time",FallingBehind, true);
				currentstate = 1;
			} 

		}

		public long StartTime;
		public long CurrentTime;
		public long unittime;
		public long timelimit;
		private Hashtable TaskList;	
		public Hashtable stime; // Start time of each logistic item
		private Vector lookup;	
		private int NoFinish;
		private long CTFinish;  // Cumulative waiting time;
		private float mildfb;
		private float obviousfb;
		private int NoTasks;
		private String source;
		private int srcno;
		private int currentstate;
		private boolean over;
		private int hari;

	};
}

class ConditionByNo
{
	public ConditionByNo(int from1, int to1, long threshold1, int type1) {
		from = from1;
		to = to1;
		threshold = threshold1;  // time;
		type = type1;
	}

	public boolean check(int num_task, long t) {

		if (from < num_task && num_task <= to) {

			if (threshold < t) {
				return true;
			}
		}
/*
		if (from < num_task && num_task <= to)
		{
			return true;
		} 
*/
		return false;
	}

	int from;
	int to;
	public long threshold;
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