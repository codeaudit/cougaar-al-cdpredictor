package org.cougaar.tools.alf.sensor.plugin;

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

public class SummarizeResults {

  public void SummarizeResults()   {  }

  public static void main(String[] args) 
  {

		try
		{	
			// open the file
			java.io.BufferedReader DataReader = new java.io.BufferedReader ( new java.io.FileReader(args[0]));
			
			java.io.BufferedWriter Summarized = new java.io.BufferedWriter ( new java.io.FileWriter(args[0]+"-sum.txt", false ));

			Summarized.write("Refill,Item,Uid,Verb,Ower,Cluster,Removed,removedTime,addedTime,addedQty,addedRate,addedStart_time,addedEnd_time,changedTime,"
							+ "changedQty,changedRate,changedStart_time,changedEnd_time,addedRarsuccess,"
							+ "addedRarConfidence,addedEarsuccess,addedEarConfidence,changedRarsuccess,changedRarConfidence,changedEarsuccess,changedEarConfidence\n");

			String s = null;
			int k=0;
			Vector v = null;

			while ((s=DataReader.readLine())!=null)
			{
				
				if (s.indexOf("GetLogSupport")==-1)
				{

					StringTokenizer st = new StringTokenizer(s,",");
					String refill		= st.nextToken();
					String Litem		= st.nextToken();
					String modifier		= st.nextToken();
					String time			= st.nextToken();
					String uid			= st.nextToken();

					String verb			= "";
					String qty			= "";	String rate			= "";		String start_time		= "";
					String end_time		= "";	String rarsuccess		= "";	String rarConfidence	= "";
					String earsuccess	= "";	String earConfidence	= "";	String owner			= "";
					String cluster		= "";
					
					if (modifier.equalsIgnoreCase("added"))
					{
						String Uid= ""; String  Verb= ""; String  Ower= ""; String  Cluster = ""; String  addedTime= ""; String  addedQty= ""; String addedRate= ""; String addedStart_time= ""; String  addedEnd_time= ""; 
						String changedTime= ""; String changedQty= ""; String changedRate= ""; String changedStart_time= ""; String changedEnd_time= ""; 
						String addedRarsuccess = ""; String  addedRarConfidence = ""; String  addedEarsuccess	= ""; String  addedEarConfidence = ""; 
						String changedRarsuccess = ""; String  changedRarConfidence= ""; String  changedEarsuccess = ""; String  changedEarConfidence ="";						
						String s2, Removed="false"; String removedTime= "";
			
						java.io.BufferedReader DataReader2 = new java.io.BufferedReader ( new java.io.FileReader(args[0]));

						v = new Vector();
						while ((s2=DataReader2.readLine())!=null)
						{
							if (s2.indexOf(uid)==-1)
							{
								continue;
							} else {
								v.add(new String(s2));
							}
						}
								
						Iterator vi = v.iterator();
						for (;vi.hasNext();)
						{
							String sv = (String) vi.next();

							StringTokenizer stv = new StringTokenizer(sv,",");
							refill			= stv.nextToken();
							Litem			= stv.nextToken();
							modifier		= stv.nextToken();

							if (modifier.equalsIgnoreCase("added"))
							{
								time			= stv.nextToken();	uid				= stv.nextToken();	verb			= stv.nextToken();
								qty				= stv.nextToken();	rate			= stv.nextToken();	start_time		= stv.nextToken();
								end_time		= stv.nextToken();	rarsuccess		= stv.nextToken();	rarConfidence	= stv.nextToken();
								earsuccess		= stv.nextToken();	earConfidence	= stv.nextToken();	owner			= stv.nextToken();
								cluster			= stv.nextToken();

								Uid				= uid;		Verb			= verb;		addedTime		= time;
								addedQty		= qty;		addedRate		= rate; 	addedStart_time	= start_time;
								addedEnd_time	= end_time;	Ower		= owner;		Cluster    = cluster;
							} 
							else if (modifier.equalsIgnoreCase("changed"))
							{
								time			= stv.nextToken();	uid				= stv.nextToken();	verb			= stv.nextToken();
								qty				= stv.nextToken();	rate			= stv.nextToken();	start_time		= stv.nextToken();
								end_time		= stv.nextToken();	rarsuccess		= stv.nextToken();	rarConfidence	= stv.nextToken();
								earsuccess		= stv.nextToken();	earConfidence	= stv.nextToken();	owner			= stv.nextToken();
								cluster			= stv.nextToken();
								
								changedTime		= time;	changedQty		= qty;	changedRate		= rate;	changedStart_time	= start_time;	changedEnd_time	= end_time;
							}
							else if (modifier.equalsIgnoreCase("removed"))
							{
								time			= stv.nextToken();
								Removed = "true";
								break;
							}
							else if (modifier.equalsIgnoreCase("added-pe"))
							{
								time			= stv.nextToken();	uid				= stv.nextToken();	rarsuccess		= stv.nextToken();	
								rarConfidence	= stv.nextToken();	earsuccess		= stv.nextToken();	earConfidence	= stv.nextToken();	
								
								addedRarsuccess		= rarsuccess;	addedRarConfidence	= rarConfidence;	addedEarsuccess		= earsuccess; addedEarConfidence	= earConfidence;
							}							
							else if (modifier.equalsIgnoreCase("changed-pe"))
							{
								time			= stv.nextToken();	uid				= stv.nextToken();	rarsuccess		= stv.nextToken();	
								rarConfidence	= stv.nextToken();	earsuccess		= stv.nextToken();	earConfidence	= stv.nextToken();	
								
								changedRarsuccess		= rarsuccess;	changedRarConfidence	= rarConfidence;	changedEarsuccess		= earsuccess;	changedEarConfidence	= earConfidence;
							}
						}	

						Summarized.write(refill+","+Litem+","+Uid+","+Verb+","+ Ower+","+ Cluster +","+ Removed +","+ removedTime +","+ addedTime+","+ addedQty+","+addedRate+","+addedStart_time+","+ addedEnd_time+","+  
										 changedTime+","+changedQty+","+changedRate+","+changedStart_time+","+changedEnd_time+","+  
										 addedRarsuccess +","+ addedRarConfidence +","+ addedEarsuccess	+ ","+ addedEarConfidence +","+ 
										 changedRarsuccess	+","+ changedRarConfidence+","+ changedEarsuccess +","+ changedEarConfidence +"\n");						

						DataReader2.close();
					}
				
				} 
			}

			DataReader.close();
			Summarized.close();
		}
		catch (java.io.IOException ioexc)
	    {
		    System.err.println ("can't read or write file, io error" );
			System.err.println ("DatafileMaker constructor");
	    }
  }

}
