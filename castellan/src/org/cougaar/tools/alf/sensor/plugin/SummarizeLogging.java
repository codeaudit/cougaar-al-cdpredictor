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
import java.text.SimpleDateFormat;


/**
 * Title:        View Time Series
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PSU
 * @author Yunho Hong
 * @version 1.0
 */

public class SummarizeLogging {

  public void SummarizeLogging()   {  }

  public static void main(String[] args) 
  {

		boolean PlanStart = false;
		boolean setStartingTime = false;
		long StartingTime=0;

		try
		{	
			// open the file
			java.io.BufferedReader DataReader = new java.io.BufferedReader ( new java.io.FileReader(args[0]));
			
			java.io.BufferedWriter Summarized = new java.io.BufferedWriter ( new java.io.FileWriter(args[0]+".txt", false ));

			Summarized.write("Agent\tPlugin\tType\tStart Time\tEnd Time\tLapse Time\n");

			String s = null;
//			int k=0;
//			Vector v = null;

			while ((s=DataReader.readLine())!=null)
			{
				if (PlanStart == false)
				{
					if (s.indexOf("verb=GetLogSupport")!=-1)
					{
						PlanStart = true;
/*
						String t = s.substring(0,24);
//						System.out.println(t);
//						SimpleDateFormat sdf = new SimpleDateFormat();
						StringTokenizer ts = new StringTokenizer(t,"/:. ");
						String year			= ts.nextToken();
						String month		= ts.nextToken();
						String day			= ts.nextToken();
						String hr			= ts.nextToken();
						String min			= ts.nextToken();
						String sec      	= ts.nextToken();
						String milisec      = ts.nextToken();
						
						Calendar rightNow = Calendar.getInstance();

						rightNow.set(Integer.valueOf(year).intValue(), Integer.valueOf(month).intValue(), Integer.valueOf(day).intValue(), 
									 Integer.valueOf(hr).intValue(), Integer.valueOf(min).intValue(), Integer.valueOf(sec).intValue());

						StartingTime = rightNow.getTimeInMillis() + Long.valueOf(milisec).longValue();

						System.out.println(StartingTime);							
*/
/*
						DateFormat df = DateFormat.getDateInstance(DateFormat.LONG, Locale.FRANCE);

						try
						{
							System.out.println(df.parse(t).getTime());							
						}
						catch (java.text.ParseException npe)
						{
							System.err.println ("ParseException" );
						}
*/
//						break;
					}

				}
				else 
				{   if (s.indexOf("NCA: Planning Complete")!=-1)
					{
						break;
					}

					if (s.indexOf("PSU")!=-1)
					{
		    
						StringTokenizer st = new StringTokenizer(s,";");
						st.nextToken(); //String Head			= st.nextToken();
						String Agent		= st.nextToken();
						String Plugin		= st.nextToken();
						String Type			= st.nextToken();
						String EndTime		= st.nextToken();
						String StartTime	= st.nextToken();

						long endTime = Long.valueOf(EndTime).longValue()-StartingTime;

						if (setStartingTime != true)
						{
							StartingTime = endTime;
							endTime = 0;
							setStartingTime = true;
						}

						long startTime = Long.valueOf(StartTime).longValue()-StartingTime;


						long lapseTime	= endTime - startTime;
						Summarized.write(Agent+"\t"+Plugin+"\t"+Type+"\t"+startTime+"\t"+ endTime+"\t"+ lapseTime +"\n");						
		    		} 
				}
			}

			DataReader.close();
			Summarized.close();
		}
		catch (java.io.IOException ioexc)
	    {
		    ioexc.printStackTrace() ; // System.err.println ("can't read or write file, io error" );
//			System.err.println ("DatafileMaker constructor");
	    }
  }

}
