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
  
public class Threshold
  {

	  int num_of_level; // falling behind level

	  // about falling behind level
	  // Currently there are no obvious criteria to separate level. 
	  // falling behind should be determined by lateness which we can learn when we compare the due date and expected finish time.
	  // Actually, we need to find the functional relation between an intermediate state and the expected finish time.
	  // 
	  // I might define falling behindness by value function. Here, value function means that if I continue to use current policy for a given current state then, 
	  // we will get expected falling behindness.
	  //
	  // With reality, we divide area of # of tasks.
	  //

	  public Threshold(int numoflevel) { 
	  
		  num_of_level = numoflevel;
	  }

	  public float[][] findThresholdMultiplelevel(float [][][]series,int ntest, int mode, String [][] test,String agentname,int classno) {

		float [] maximal = null;
		float [] minimal = null;
		float [][] threshold = null;

		// find the time length of normal situation 
		float ll=0;
	
		for (int i=0;i<ntest;i++) {
			if (series[i] != null)
			{
				if (test[i][2].equalsIgnoreCase("NO")==true && test[i][1].equalsIgnoreCase("N")==true)
				{
					int lt = series[i][0].length - 1; 
					
					if (ll < series[i][0][lt])
					{
						ll = series[i][0][lt];
					}
				}
			}	
		}
		
		int ts = (int) (ll / 1000) + 1 + 1;

//		System.out.println("ll = " + ll + ", ts = " + ts);

		// memory allocation 
		maximal = new float[ts];
		minimal = new float[ts];
		threshold = new float[ts][num_of_level-1];

		for (int j=0;j < ts;j++ )
		{
			int ct = j*1000;
			// Instead of finding minimum value for normal case, find maximum value.
			float mv = 0; 

			for (int i=0;i<ntest-1;i++) 
			{
				if (series[i] != null)
				{
					if (test[i][2].equalsIgnoreCase("NO")==true && test[i][1].equalsIgnoreCase("N")==true)
					{
						float cmv = 0;  
						int k = 0;

						int lt = series[i][0].length; 
					    boolean con = true;

						while (lt > k && con == true)	
						{		
							if (series[i][0][k] > ct )
							{
								con = false;
							} else {
								cmv = series[i][1][k];
							}

							k++;		
 
						}

//						if (k == 1)	{	cmv = 0;					} 
//						else		{	cmv = series[i][1][k-1];	}

						if (mv < cmv)
						{
							mv = cmv;
						}

//						System.out.println(ct + " " +cmv);
					}
				}	
			}
		
			maximal[j] = mv;

			// Instead of finding maximum value for falling behind case, find minimum value.
			float bv = Float.MAX_VALUE; 

			for (int i=0;i<ntest;i++) 
			{
				if (series[i] != null)
				{
					if (test[i][2].equalsIgnoreCase("SS")==true && test[i][1].equalsIgnoreCase("N")==true)
					{
						float cbv=0;  
						int k = 0;
					
						int lt = series[i][0].length; 
					    boolean con = true;

						while (lt > k && con == true)	
						{		
							if (series[i][0][k] > ct )
							{
								con = false;
							} else {

								cbv = series[i][1][k];
							}

							k++;		
 
						}

						if (bv > cbv)
						{
							bv = cbv;
						}
					}
				}	
			}

			minimal[j] = bv;
		}

		for (int i=0;i<ts ;i++)
		{
//			System.out.print(i+" " +maximal[i] +" " + minimal[i]);

			float diff = (maximal[i] - minimal[i])/num_of_level;

			for (int j=0;j<num_of_level-1;j++)
			{
				threshold[i][j] = maximal[i] - diff*(j+1);
//				System.out.print(" " +threshold[i][j]);
			}
//			System.out.println(" ");
		}

		return threshold;
	}

	  // find a thres hold
	  public float[] findThreshold(float [][][]series,int ntest, int mode, String [][] test,String agentname,int classno) {

		float [] normal = null;
		float [] fallingbehind = null;
		float [] threshold = null;

		// find the time length of normal situation 
		float ll=0;
	
		for (int i=0;i<ntest-1;i++) {
			if (series[i] != null)
			{
				if (test[i][2].equalsIgnoreCase("NO")==true && test[i][1].equalsIgnoreCase("N")==true)
				{
					int lt = series[i][0].length - 1; 
					
					if (ll < series[i][0][lt])
					{
						ll = series[i][0][lt];
					}
				}
			}	
		}
		
		int ts = (int) (ll / 1000) + 1 + 1;

//		System.out.println("ll = " + ll + ", ts = " + ts);

		// memory allocation 
		normal = new float[ts];
		fallingbehind = new float[ts];
		threshold = new float[ts];

		for (int j=0;j < ts;j++ )
		{
			int ct = j*1000;
			// find minimum value at a specific time step in the case of normal
			float mv = Float.MAX_VALUE; 

			for (int i=0;i<ntest-1;i++) 
			{
				if (series[i] != null)
				{
					if (test[i][2].equalsIgnoreCase("NO")==true && test[i][1].equalsIgnoreCase("N")==true)
					{
						float cmv = 0;  
						int k = 0;

						int lt = series[i][0].length; 
					    boolean con = true;

						while (lt > k && con == true)	
						{		
							if (series[i][0][k] >= ct )
							{
								con = false;
							} else {

								cmv = series[i][1][k];
							}

							k++;		
 
						}

//						if (k == 1)	{	cmv = 0;					} 
//						else		{	cmv = series[i][1][k-1];	}

						if (mv > cmv)
						{
							mv = cmv;
						}

//						System.out.println(ct + " " +cmv);
					}
				}	
			}
		
			normal[j] = mv;

			// find minimum value at a specific time step in the case of normal
			float bv = 0; 

			for (int i=0;i<ntest-1;i++) 
			{
				if (series[i] != null)
				{
					if (test[i][2].equalsIgnoreCase("SS")==true && test[i][1].equalsIgnoreCase("N")==true)
					{
						float cbv=0;  
						int k = 0;
					
						int lt = series[i][0].length; 
					    boolean con = true;

						while (lt > k && con == true)	
						{		
							if (series[i][0][k] >= ct )
							{
								con = false;
							} else {

								cbv = series[i][1][k];
							}

							k++;		
 
						}

//						if (k == 1)	{	cbv = 0;					} 
//						else		{	cbv = series[i][1][k-1];	}

						if (bv < cbv)
						{
							bv = cbv;
						}
					}
				}	
			}

			fallingbehind[j] = bv;
		}

		for (int i=0;i<ts ;i++)
		{
			threshold[i] = (normal[i] + fallingbehind[i])/2;

//			System.out.println(threshold[i]+" " +normal[i] +" " + fallingbehind[i]);


		}

		return threshold;
	}
  };
