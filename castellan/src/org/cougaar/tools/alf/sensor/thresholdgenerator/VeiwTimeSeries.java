package org.cougaar.tools.alf.sensor.thresholdgenerator;

import org.cougaar.tools.alf.sensor.thresholdgenerator.*;

import java.util.*;
import java.sql.*;
import java.io.*;

import org.gjt.mm.mysql.*;

import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.axiom.lib.util.* ;
import com.axiom.lib.mat.* ;

/**
 * Title:        View Time Series
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PSU
 * @author Yunho Hong
 * @version 1.0
 */

public class VeiwTimeSeries {

  public void VeiwTimeSeries()   {  }

  public static void main(String[] args) 
  {

	// I will replace this part with the object which read the database name from a file.
	String [][] test  = {	
							{"PlanLogAgent07132002_1720","N","NO"},
							{"PlanLogAgent07132002_2205","N","NO"},
							{"PlanLogAgent07142002_0023","N","NO"},
							{"PlanLogAgent07142002_0153","N","SS"},  // 11111:33333
							{"PlanLogAgent07142002_0321","N","SS"},						
							{"PlanLogAgent07142002_0423","N","SS"},
						};
	String [] agent = { 

          " 1-1-CAVSQDN      ",
          " 1-13-ARBN        ",
          " 1-27-FABN        ",
          " 1-35-ARBN        ",
          " 1-36-INFBN       ",
          " 1-37-ARBN        ",
          " 1-4-ADABN        ",
          " 1-41-INFBN       ",
          " 1-501-AVNBN      ",
          " 1-6-INFBN        ",
          " 1-94-FABN        ",
          " 1-AD             ",
          " 1-AD-DIV         ",
          " 1-BDE-1-AD       ",
          " 102-POL-SUPPLYCO ",
          " 106-TCBN         ",
          " 109-MDM-TRKCO    ",
          " 11-AVN-RGT       ",
          " 110-POL-SUPPLYCO ",
          " 12-AVNBDE        ",
          " 123-MSB          ",
          " 125-FSB          ",
          " 125-ORDBN        ",
          " 127-DASB         ",
          " 130-ENGBDE       ",
          " 141-SIGBN        ",
          " 15-PLS-TRKCO     ",
          " 16-CSG           ",
          " 16-ENGBN         ",
          " 18-MAINTBN       ",
          " 18-MPBDE         ",
          " 18-PERISH-SUBPLT ",
          " 181-TCBN         ",
          " 19-MMC           ",
          " 191-ORDBN        ",
          " 2-3-FABN         ",
          " 2-37-ARBN        ",
          " 2-4-FABN-MLRS    ",
          " 2-501-AVNBN      ",
          " 2-6-INFBN        ",
          " 2-70-ARBN        ",
          " 2-BDE-1-AD       ",
          " 200-MMC          ",
          " 205-MIBDE        ",
          " 208-SCCO         ",
          " 21-TSC-HQ        ",
          " 22-SIGBDE        ",
          " 226-MAINTCO      ",
          " 227-SUPPLYCO     ",
          " 23-ORDCO         ",
          " 238-POL-TRKCO    ",
          " 24-ORDCO         ",
          " 240-SSCO         ",
          " 244-ENGBN-CBTHVY ",
          " 25-FABTRY-TGTACQ ",
          " 26-SSCO          ",
          " 263-FLDSVC-CO    ",
          " 27-TCBN-MVTCTRL  ",
          " 28-TCBN          ",
          " 286-ADA-SCCO     ",
          " 29-SPTGP         ",
          " 3-13-FABN-155    ",
          " 3-BDE-1-AD       ",
          " 3-SUPCOM-HQ      ",
          " 30-MEDBDE        ",
          " 316-POL-SUPPLYBN ",
          " 317-MAINTCO      ",
          " 343-SUPPLYCO     ",
          " 37-TRANSGP       ",
          " 372-CGO-TRANSCO  ",
          " 377-HVY-TRKCO    ",
          " 4-1-FABN         ",
          " 4-27-FABN        ",
          " 40-ENGBN         ",
          " 41-FABDE         ",
          " 41-POL-TRKCO     ",
          " 416-POL-TRKCO    ",
          " 452-ORDCO        ",
          " 47-FSB           ",
          " 485-CSB          ",
          " 5-CORPS          ",
          " 5-CORPS-ARTY     ",
          " 5-CORPS-REAR     ",
          " 5-MAINTCO        ",
          " 501-FSB          ",
          " 501-MIBN-CEWI    ",
          " 501-MPCO         ",
          " 51-MAINTBN       ",
          " 51-MDM-TRKCO     ",
          " 512-MAINTCO      ",
          " 515-POL-TRKCO    ",
          " 52-ENGBN-CBTHVY  ",
          " 529-ORDCO        ",
          " 541-POL-TRKCO    ",
          " 561-SSBN         ",
          " 565-RPRPTCO      ",
          " 574-SSCO         ",
          " 584-MAINTCO      ",
          " 588-MAINTCO      ",
          " 592-ORDCO        ",
          " 594-MDM-TRKCO    ",
          " 596-MAINTCO      ",
          " 597-MAINTCO      ",
          " 6-TCBN           ",
          " 632-MAINTCO      ",
          " 66-MDM-TRKCO     ",
          " 68-MDM-TRKCO     ",
          " 69-ADABDE        ",
          " 69-CHEMCO        ",
          " 7-CSG            ",
          " 7-TCGP-TPTDD     ",
          " 70-ENGBN         ",
          " 702-EODDET       ",
          " 71-MAINTBN       ",
          " 71-ORDCO         ",
          " 720-EODDET       ",
          " 77-MAINTCO       ",
          " 900-POL-SUPPLYCO ",
          " AVNBDE-1-AD      ",
          " AWR-2            ",
          " CONUSGround      ",
          " DISCOM-1-AD      ",
          " DIVARTY-1-AD     ",
          " DLAHQ            ",
          " FORSCOM          ",
          " GlobalAir        ",
          " GlobalSea        ",
          " HNS              ",
          " JSRCMDSE         ",
          " NATO             ",
          " NCA              ",
          " OSC              ",
          " PlanePacker      ",
          " RSA              ",
          " ShipPacker       ",
          " TheaterGround    ",
          " TRANSCOM         ",
          " USAEUR           ",
          " USEUCOM          "

	};

	String agentname = "47-FSB";
	int classno = -1;
	int unittime = 50; // 0.5 sec
	int mode = 1;
	boolean oneByone;

	if (args.length >= 4)
	{
		agentname = args[0];
		classno = Integer.parseInt(args[1]);
		unittime = Integer.parseInt(args[2]);
		mode = Integer.parseInt(args[3]);
		oneByone = true;

	} else if (args.length == 3) {

		classno = Integer.parseInt(args[0]);
		unittime = Integer.parseInt(args[1]);
		mode = Integer.parseInt(args[2]);
		oneByone = false;

	} else {
//		System.out.println("agentname class unittime mode");
//		System.out.println("mode = 3 : use the first GLS or the first task except RFS and RFD in the agent as the base time");
//		System.out.println("mode = 4 : use starting GLS of all society in the agent as the base time");
//		System.out.println("example : 'run 47-FSB 10 0 4' or 'run 10 0 4'");

		return;
	}

	// Establish connection
	TimeSeries timeseires = new TimeSeries();
    timeseires.establishConnection();

	ThresholdGenerator genThreshold = new ThresholdGenerator(test, oneByone);	// Only in case that oneByone is true, matlab will be used.

	MatlabInterface mtl = new MatlabInterface();	//Matlab

	if (oneByone)
	{
		genThreshold.generateThresholdForTheAgent(agentname, mode, unittime, classno, timeseires, mtl); // test is the array which contains names of database.

	} else {

		for (int i=0;i<agent.length;i++ )
		{
			agentname = agent[i].trim();
			genThreshold.generateThresholdForTheAgent(agentname, mode, unittime, classno, timeseires, mtl); // test is the array which contains names of database.
		}
	}

	timeseires.closeConnection();
  }
}
