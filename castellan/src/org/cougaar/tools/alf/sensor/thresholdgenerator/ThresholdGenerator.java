

package org.cougaar.tools.alf.sensor.thresholdgenerator;

import org.cougaar.tools.alf.sensor.thresholdgenerator.*;

class ThresholdGenerator
{

  int num_of_fallingbehind_level; 
  int ntest;
  String [][] test;
  boolean useMatlab;		// It indicates whether it will use matalab.

  public ThresholdGenerator(String [][] test1, boolean useMatlab1) { 
	
	useMatlab = useMatlab1;	// It indicates whether it will use matalab.
	num_of_fallingbehind_level = 3;
	test = test1;			// Experiment Database name.
  	ntest = test1.length;	// additional one for a threshold separator

  }

  public void generateThresholdForTheAgent(String agentname, int mode, int unittime, int classno, TimeSeries timeseires, MatlabInterface mtl) {
	
	float series[][][] = new float[ntest][][];	// the time series generated from each database for the agent will be contained by matrix series.

//	System.out.println("agent name = " + agentname);

	for (int i=0;i<ntest;i++)
	{
		timeseires.useDataBase(test[i][0]);
//		System.out.println(test[i][0]);

		if (mode == 1)	{
			series[i] = timeseires.generateTSofWaitingTime(agentname, classno, unittime);
		} else if (mode == 2)	{
			series[i] = timeseires.generateTSNumTasksAtCluster(agentname, classno, unittime); // Start at the time the first task comes
		} else if (mode == 3)	{
			series[i] = timeseires.NumTasksFromGLSAtCluster(agentname, classno, unittime, 1); // all the eventtime is subtracted by first GLS in the agent
		} else if (mode == 4)	{
			series[i] = timeseires.NumTasksFromGLSAtCluster(agentname, classno, unittime, 0); // all the eventtime is subtracted by starting GLS of all society
		} else {
			series[i] = timeseires.generateLoadTimeSeries(agentname, classno, unittime);
		} 
	}

//	float [] mseries = null;

//    if (series[0] == null)
//    {
//		System.out.println("series is null");
//    }


	//Plain file
	Threshold th = new Threshold(num_of_fallingbehind_level);
	FilePrint ff = new FilePrint();

	// Calculate threshold and 
	float threshold [][] = th.findThresholdMultiplelevel(series,ntest,mode,test,agentname,classno);
	
	ff.storeThresholdFile(threshold, agentname);

//	if (threshold != null)
//	{
/*
		for (int i=0;i<threshold.length; i++ )
		{
			series[ntest-1][0][i] = i*1000;
			series[ntest-1][1][i] = threshold[i];
		}
*/
//	}

	//Matlab
//	MatlabInterface mtl = new MatlabInterface();
	if (useMatlab)
	{
		mtl.showResultinMatlab(series, threshold, ntest, mode, test, agentname, classno);
	}

//	mtl.pause();

  }

};