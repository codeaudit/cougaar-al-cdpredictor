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

public class MatlabInterface
{
		public MatlabInterface () { }

		public void showResultinMatlab(float [][][]series, float [][] threshold, int ntest, int mode, String [][] test,String agentname,int classno) {

			// Show the result in a Matlab
			MatEng eng = MatEng.getInstance();
			eng.evalString( "clear" ) ;
//			eng.evalString( "cd u:/results" ) ;
	
			// Coftnvert java array into Matlab array
			int ll = threshold[0].length;

//			System.out.println("# of levels = " + ll + " # of tests = " + ntest);

			FloatMatrix [] matmatrix = new FloatMatrix[ntest+ll];

			for (int i=0;i<ntest;i++)
			{
				if (series[i] != null)
				{
					matmatrix[i] = new FloatMatrix(series[i][0].length,2) ;

					for(int j=0;j<series[i][0].length;j++) {
						matmatrix[i].set(j,0,series[i][0][j]);  // time
						matmatrix[i].set(j,1,series[i][1][j]);  // num of tasks
					}
				} else {
//					System.out.println("series " + i + " is null");
				}
			}

			int s = threshold.length;

			for (int i=ntest;i<ntest+ll;i++)
			{
				if (threshold[i-ntest] != null)
				{
					matmatrix[i] = new FloatMatrix(s,2) ;

					for(int j=0;j<s;j++) {

						matmatrix[i].set(j,0,j*1000);  // time
						matmatrix[i].set(j,1,threshold[j][i-ntest]);  // num of tasks

					}

				} else {
					System.out.println("series " + i + " is null");
				}
			}
			
			String plot = "plot(";
			for (int i=0;i<ntest+ll;i++)
			{
				if (matmatrix[i] != null)
				{
					eng.putArray( "m"+i, matmatrix[i]) ;
					plot = plot+"m"+i+"(:,1),m"+i+"(:,2),";

					if (i < ntest)
					{
						if (test[i][2].equalsIgnoreCase("NO")==true && test[i][1].equalsIgnoreCase("N")==true)
						{
							plot = plot+"'y.'";
						} else if (test[i][2].equalsIgnoreCase("M")==true && test[i][1].equalsIgnoreCase("N")==true) {
							plot = plot+"'b.'";
						} else if (test[i][2].equalsIgnoreCase("SM")==true && test[i][1].equalsIgnoreCase("N")==true) {
							plot = plot+"'k.'";
						} else if (test[i][2].equalsIgnoreCase("SS")==true && test[i][1].equalsIgnoreCase("N")==true) {
							plot = plot+"'g.'";
						} else if (test[i][2].equalsIgnoreCase("AB")==true && test[i][1].equalsIgnoreCase("N")==true) {
							plot = plot+"'c.'";
						} else 	{
							plot = plot+"'r.'";
						} 
					}

					// Last one is threshold time series
					if (i >= ntest)
					{
						plot = plot+"'b.'";

					}

					if (i == ntest+ll-1)
					{
						plot = plot+")";
					} else {
						plot = plot+",";
					}
				}
			}

			eng.evalString( "figure" ) ;
			eng.evalString(plot);
//			System.out.println(plot);
			eng.evalString( "title('"+agentname + ", class="+classno+"')");
		}

		public void showResultinMatlab(float [][][]series,int ntest, int mode, String [][] test,String agentname,int classno) {

			// Show the result in a Matlab
			MatEng eng = MatEng.getInstance();
			eng.evalString( "clear" ) ;
//			eng.evalString( "cd u:/results" ) ;
	
			// Coftnvert java array into Matlab array
			FloatMatrix [] matmatrix = new FloatMatrix[ntest];

			for (int i=0;i<ntest;i++)
			{
				if (series[i] != null)
				{
					matmatrix[i] = new FloatMatrix(series[i][0].length,2) ;

					for(int j=0;j<series[i][0].length;j++) {
						matmatrix[i].set(j,0,series[i][0][j]);  // time
						matmatrix[i].set(j,1,series[i][1][j]);  // num of tasks
					}
				} else {
//					System.out.println("series " + i + " is null");
				}
			}

			String plot = "plot(";
			for (int i=0;i<ntest;i++)
			{
				if (matmatrix[i] != null)
				{
	
					eng.putArray( "m"+i, matmatrix[i]) ;
					plot = plot+"m"+i+"(:,1),m"+i+"(:,2),";

					if (i < ntest)
					{
					
						if (test[i][2].equalsIgnoreCase("NO")==true && test[i][1].equalsIgnoreCase("N")==true)
						{
							plot = plot+"'y.'";
						} else if (test[i][2].equalsIgnoreCase("M")==true && test[i][1].equalsIgnoreCase("N")==true) {
							plot = plot+"'b.'";
						} else if (test[i][2].equalsIgnoreCase("SM")==true && test[i][1].equalsIgnoreCase("N")==true) {
							plot = plot+"'k.'";
						} else if (test[i][2].equalsIgnoreCase("SS")==true && test[i][1].equalsIgnoreCase("N")==true) {
							plot = plot+"'g.'";
						} else if (test[i][2].equalsIgnoreCase("AB")==true && test[i][1].equalsIgnoreCase("N")==true) {
							plot = plot+"'c.'";
						} else 	{
							plot = plot+"'r.'";
						} 
					}

					// Last one is threshold time series
					if (i>=ntest)
					{
						plot = plot+"'b.'";
//							plot = plot+")";

					} else {
						plot = plot+",";
					}
				}
			}

			eng.evalString( "figure" ) ;
			eng.evalString(plot);
//			System.out.println(plot);
			eng.evalString( "title('"+agentname + ", class="+classno+"')");
		}

		public void pause() {

			BufferedReader br = new BufferedReader(new java.io.InputStreamReader(System.in)); 
		
			try { 
		         br.readLine(); 
				 br.close();
		    } catch (java.io.IOException ioe) { 
//				 System.out.println("IO error trying to read your name!"); 
		         System.exit(1); 
		    } 
		}
	};

