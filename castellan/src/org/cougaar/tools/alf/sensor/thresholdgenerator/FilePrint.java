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

public class FilePrint
{
		public FilePrint() { }

		public void storeThresholdFile(float [][] threshold, String agentname) {

			String fn = "lookup.txt";

			try
			{

				java.io.BufferedWriter rst = new java.io.BufferedWriter ( new java.io.FileWriter(fn, true ));
				
				rst.write("@agent " + agentname + "\n");

				for (int r=0;r<threshold.length;r++)
				{
					for (int j=0;j<threshold[0].length;j++)
					{
						if (j == 0)
						{
							rst.write((r*1000) + " " + j + " " + threshold[r][j] + " " + Float.MAX_VALUE +"\n"); // Time, LL, UL
						} else {
							if (threshold[r][j] != threshold[r][j-1])
							{
								rst.write((r*1000) + " " + j + " " + threshold[r][j] + " " + threshold[r][j-1] +"\n"  ); // Time, LL, UL
							}
						}
					}

					if (threshold[r][threshold[0].length-1] != 0)
					{
						rst.write((r*1000) + " " + (threshold[0].length) + " 0 " + threshold[r][threshold[0].length-1] +"\n"); // Time, LL, UL
					}

				}
				rst.close();
			}
			catch (java.io.IOException ioexc)
		    {
			    System.err.println ("can't write file, io error" );
		    }						

		}

		public void storeResultinFile(float [][][]series,int ntest, int mode, String [][] test, String agentname,int classno) {

			String fn = "";

			if (mode==2)
			{
				fn = "u:/results/ns"+agentname+".txt";
			} else {
				fn = "u:/results/wt"+agentname+".txt";
			}

			try
			{

				java.io.BufferedWriter rst = new java.io.BufferedWriter ( new java.io.FileWriter(fn, true ));
				
				// find the largest legnth of each time series
				int ls = 0;
				for (int i=0;i<ntest;i++) {
						if (series[i] != null)
						{
							 if (ls < series[i][0].length )
							 {
								 ls = series[i][0].length;
						     }
						}
				}

				for (int r=0;r<ls;r++)
				{
					
						for (int i=0;i<ntest;i++)
						{
								if (series[i] != null)
								{
									if (r < series[i][0].length)
									{
										rst.write(series[i][0][r]+","+series[i][1][r]+",");
									} else {
										rst.write("0, 0,");
									}
								} else {
									System.out.println("series " + i + " is null");
								}
						}
						rst.write("\n");
				}
				rst.close();
			}
			catch (java.io.IOException ioexc)
		    {
			    System.err.println ("can't write file, io error" );
		    }
		}
	};
