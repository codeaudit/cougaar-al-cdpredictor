package org.cougaar.tools.alf.sensor.plugin;

import java.util.*;
import java.sql.*;
import java.io.*;

/**
 * Title:        Support Vector Machine 
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PSU
 * @author Yunho Hong
 * @version 1.0
 */

public class SvmResult implements java.io.Serializable {

	int dim = 0, number = 0;
	double gamma = 0;
	double sv[][]=null, y[]=null, a[]=null;
	double b=0;

	public void SvmResult()   {  }

	public void readModel(File filename)
	{
		try
		{	
			// open the file
			java.io.BufferedReader ModelReader = new java.io.BufferedReader ( new java.io.FileReader(filename)); // svm model	
			
			String s = "";

			// Read Model
			boolean haveToReadHead = true;
			int ci=0;

			while ((s=ModelReader.readLine())!=null)
			{
				StringTokenizer st = new StringTokenizer(s," ");

				if (haveToReadHead)
				{
					String item = st.nextToken();	// System.out.println(item);
					String value = st.nextToken();	// System.out.println(value);

					if (item.equals("dimension"))	{
						dim = Integer.valueOf(value).intValue();
					}
					else if (item.equals("number"))	{
						number = Integer.valueOf(value).intValue();
						sv	=	new double[number][dim];
						y	=	new double[number];
						a	=	new double[number];
					}
					else if (item.equals("b"))	{	
						b = Double.valueOf(value).doubleValue();
					}
					else if (item.equals("format"))	{	
						haveToReadHead = false; 	
					}
				} else {
					  int ix = 0;
					  while (st.hasMoreTokens()) {
						 if (ix == dim)
						 {
							 y[ci] = Double.valueOf(st.nextToken()).doubleValue();
							 ix++;
						 } else if (ix == dim+1)
						 {
							 a[ci] = Double.valueOf(st.nextToken()).doubleValue();
 							 ix++;
						 } else {
							 sv[ci][ix] = Double.valueOf(st.nextToken()).doubleValue();
							 ix++;
						 }
				     }
					 ci++;
				}
			}

			ModelReader.close();
		}
		catch (java.io.IOException ioexc)
	    {
		    ioexc.printStackTrace() ; // System.err.println ("can't read model file, io error" );
	    }
	}

	public void readParam(File filename)
	{
		try
		{	
			// open the file
			java.io.BufferedReader ModelReader = new java.io.BufferedReader ( new java.io.FileReader(filename)); // param file
					
			String s = "";
			String type = "";

			// Read parameters
			while ((s=ModelReader.readLine())!=null)
			{
				StringTokenizer st = new StringTokenizer(s," ");
				String item = st.nextToken();				
				if (item.equals("type"))	{ 
					String value = st.nextToken();
					type = value;					
				}
				else if (item.equals("gamma"))	{	
					String value = st.nextToken();
					gamma = Double.valueOf(value).doubleValue();
				}
			}
			
			ModelReader.close();
		}
		catch (java.io.IOException ioexc)
	    {
		    ioexc.printStackTrace() ; // System.err.println ("can't read param file, io error" );
	    }
	}

	public void testResults(String filename, String resultfile)
	{
		// Read the test data and calculate the estimated values.
		double yy=0, yyy=0;
		double xx[]=new double[dim];
		int count=0;
		double mae=0;

		try
		{	
			// open the file
			java.io.BufferedReader ModelReader = new java.io.BufferedReader ( new java.io.FileReader(filename));		  // Test data file
			java.io.BufferedWriter ResultData = new java.io.BufferedWriter ( new java.io.FileWriter(resultfile, false )); // Result file

			String s = "";

			while ((s=ModelReader.readLine())!=null)
			{
				StringTokenizer st = new StringTokenizer(s," ");
				int ix = 0;
				while (st.hasMoreTokens()) {
					 if (ix == dim)
					 {
						 yy = Double.valueOf(st.nextToken()).doubleValue();
						 ix++;
					 } else {
						 xx[ix] = Double.valueOf(st.nextToken()).doubleValue();
						 ix++;
					 }
				}
				count++;

//				for (int i=0;i<dim;i++)		{	System.out.print(xx[i]+" ");	}
//				System.out.print("\n");

				yyy = f(xx);				
//				System.out.println("yyy="+yyy);

				double diff = (yyy-yy);
				ResultData.write(yyy+","+yy+","+diff+"\n");
				ResultData.flush();
				mae = mae + Math.abs(diff);
			}

			ResultData.write("Mean absolute errors = "+ mae/count);

			ModelReader.close();
			ResultData.close();

		}
		catch (java.io.IOException ioexc)
	    {
		    ioexc.printStackTrace() ; // System.err.println ("can't read test file, io error" );
	    }
	}

	public double f(double [] xx)	// the smaller index is past.
	{
		double yyyy=0;
		for (int i=0;i<number;i++)
		{
			if (a[i]!=0)
			{
				double kkk = k(xx,sv[i]);
				yyyy = yyyy + a[i]*kkk;
			}
/*
			try
			{
				byte [] buffer = new byte[1];
				System.in.read(buffer);			
			}
			catch (java.io.IOException ioexc)
			{
				System.err.println ("can't read, io error" );
			}
*/
		}

		return yyyy+b;
	}

	private double k(double [] xx, double[] sv) // kernel function
	{
		double kkk = 0;

		kkk = Math.exp(-1*gamma*norm2(xx,sv));

		return kkk;
	}

	private double norm2(double [] xx, double[] sv) // kernel function
	{
		double normsqr =0;

		for (int i=0;i<dim;i++)
		{
			double t = xx[i]-sv[i];
			normsqr = normsqr + t*t;
		}

		return normsqr;
	}

	public static void main(String[] args) {

		SvmResult svmresult = new SvmResult();
		File paramFile = new File(args[0]);
		svmresult.readParam(paramFile);
		File modelFile = new File(args[1]);
		svmresult.readModel(modelFile);

		double [] demand = new double[3];

		demand[0] = 4.15	            ;
		demand[1] =	4.14	            ;	
		demand[2] =	4.14	            ;	

//		System.out.println(svmresult.f(demand));
	}
}
