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
import org.cougaar.util.ConfigFinder;

import org.w3c.dom.*;

import java.util.* ;
import java.lang.Float;
import java.lang.Double;
import java.lang.Integer;
import java.lang.String;
import java.io.File;

/**
 *
 * @author  Yunho Hong
 * @version
 */

public class TheLoadForecaster {

	private java.io.BufferedWriter ForecastResult; // Log file
	private int hari;	

	SensorPlugin sensorplugin;
	InterAgentOperatingMode[] psu_lf3;

   	// Costructor
	public TheLoadForecaster (SensorPlugin plugin) {
	  
	  sensorplugin = plugin;

	  int num_agent = 133;
	  String [] agent = new String[num_agent];

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

	  psu_lf3 = new InterAgentOperatingMode[agent.length];
	  
  	  for (int i=0; i<agent.length; i++) {

		psu_lf3[i]= new InterAgentOperatingMode("PSU_Loadforecaster_Class3", new OMCRangeList(new Double(0),new Double(Double.MAX_VALUE)),new Double(0));
		psu_lf3[i].setTarget(new ClusterIdentifier(agent[i]));
		psu_lf3[i].setUID(us.nextUID());
		sensorplugin.publishAdd(psu_lf3[i]);
	  }

	  // Create Log file
	  try
	  {
			ForecastResult = new java.io.BufferedWriter ( new java.io.FileWriter("forecating.txt", true ));
	  } 
	  catch (java.io.IOException ioexc)
	  {
		    System.err.println ("can't write file, io error" );
	  }

    }

	public void forecast(String agentname, long time, String t) {
		
		if (t.equalsIgnoreCase("5") == true) {

			if (agentname.equalsIgnoreCase("1-27-FABN") == true)
			{

				// 125-ORDBN
				psu_lf3[37].setValue(new Double(1.009*time + 2.8363));				
				sensorplugin.publishChange(psu_lf3[37]);
				printout("F, class "+ t+ ", 125-ORDBN, start time =," + (1.009*time + 2.8363),true);

				// 191-ORDBN
				psu_lf3[62].setValue(new Double(1.038*time + 3.0388));				
				sensorplugin.publishChange(psu_lf3[62]);
				printout("F, class "+ t+ ", 191-ORDBN, start time =," + (1.038*time + 3.0388),true);

			} 
			else if (agentname.equalsIgnoreCase("1-6-INFBN") == true && hari == 0)
			{
				// 123-MSB
				psu_lf3[84].setValue(new Double(1.6968*time + 2.4398));				
				sensorplugin.publishChange(psu_lf3[84]);
				printout("F, class "+ t+ ", 123-MSB, start time =," + (1.6968*time + 2.4398),true);

				// 592-ORDCO
				psu_lf3[116].setValue(new Double(2.3243*time-1.0));				
				sensorplugin.publishChange(psu_lf3[116]);
				printout("F, class "+ t+ ", 592-ORDCO, start time =," + (2.3243*time-1.0),true);

				hari = 1;
			}
			else if (agentname.equalsIgnoreCase("1-35-ARBN") == true && hari == 0)
			{
				// 123-MSB
				psu_lf3[84].setValue(new Double(1.6968*time + 2.4398));				
				sensorplugin.publishChange(psu_lf3[84]);
				printout("F, class "+ t+ ", 123-MSB, start time =," + (1.6968*time + 2.4398),true);

				// 592-ORDCO
				psu_lf3[116].setValue(new Double(2.3243*time-1.0));				
				sensorplugin.publishChange(psu_lf3[116]);
				printout("F, class "+ t+ ", 592-ORDCO, start time =," + (2.3243*time-1.0),true);

				hari = 1;
			}
		}
	}
	
	public void printout(String s, boolean flag ){

		try
		{
			ForecastResult.write(s+"\n");			
			ForecastResult.flush();
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
}
