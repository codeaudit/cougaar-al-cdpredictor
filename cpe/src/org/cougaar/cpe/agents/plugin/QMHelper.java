package org.cougaar.cpe.agents.plugin;

import com.axiom.lib.mat.*;
import com.axiom.lib.util.*;
import java.util.*;
/*
 * @author nathan
 * talks to Matlab and Arena to help the QueueingModel plugin 
 */
public class QMHelper {
	private MatEng eng;
	private boolean waitingOnMatlab = true;
	private boolean waitingOnArena = true;
	int arenaWaitCount = 0;
	int matlabWaitCount = 0;
	HashMap controlSet = new HashMap();
	public QMHelper() {
		//open a Matlab interface
		try {
			eng = new MatEng();
			eng.open(null);
			eng.setBufSize(1024);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public HashMap getControlSet(double[][] stats) {
		//TODO ACTUALLY NOW ALL THIS WILL BE INSIDE getControlSet()			
		//TODO call matlab to get the next set of parameters: remember DO NOT BLOCK
		//matlab(stats);

		//TODO identify the best i.e. top 10 from matlab results : remember: DO NOT BLOCK
		//arena(top10);

		//TODO make controlSet by back converting times to modes: probably not, this will be done in QM

		//TODO ship the control modes to the respective subordinates
		
		
		if ((!waitingOnMatlab) && (!waitingOnArena)) {
			invokeMatlab(stats);
			waitingOnMatlab = true;
			if (checkForOutput("Matlab")) {
				waitingOnMatlab = false;
				getMatlabOutput(); // u need this
				invokeArena();
				waitingOnArena = true;
				if (checkForOutput("Arena")) {
					waitingOnArena = false;
					getArenaOutput(); //
				} else {
					arenaWaitCount++;
				}
			} else {
				matlabWaitCount++;
			}

		} else if ((waitingOnMatlab) && (!waitingOnArena)) {
			if (checkForOutput("Matlab")) {
				waitingOnMatlab = false;
				invokeArena();
				waitingOnArena = true;
				if (checkForOutput("Arena")) {
					waitingOnArena = false;
					getArenaOutput(); //u need this
				} else {
					arenaWaitCount++;
				}
			} else {
				matlabWaitCount++;
			}
		} else if ((!waitingOnMatlab) && (waitingOnArena)) {
			if (checkForOutput("Arena")) {
				waitingOnArena = false;
				getArenaOutput(); // u need this
			} else {
				arenaWaitCount++;
			}
		}
		//have to a return a HashMap that is contains the top N best candidates 
		//each candidate is double[][]
		return null;
	}

	private boolean checkForOutput(String source) {
		return true;
	}
	private void invokeArena() {
	}
	private void getArenaOutput() {

	}
	private void getMatlabOutput() {

	}

	public void invokeMatlab(double[][] stats) {
		//MatEng eng = null;
		try {
			//somehow the global doesnt work
			eng = new MatEng();
			eng.open(null);
			eng.setBufSize(1024);
			
			//TODO remove later: just to replicate for BN 2 and 3
				  for (int i=11;i<17;i++)
				  {
					  stats[i+6][0]=stats[i][0];
					  stats[i+6][1]=stats[i][1];
					  stats[i+12][0]=stats[i][0];
					  stats[i+12][1]=stats[i][1];
				  }
				  for (int i=2;i<5;i++)
				  {
					  stats[i+3][0]=stats[i][0];
					  stats[i+3][1]=stats[i][1];
					  stats[i+6][0]=stats[i][0];
					  stats[i+6][1]=stats[i][1];
				  }
			//------------------------------------------------------

			//forming the service mean and variance strings
			String Smeans = "";
			String Svariances = "";
			for (int i = 0; i < stats.length; i++) {

				if (i == 0) {
					Smeans += "SM = [" + stats[i][0] + " ";
					Svariances += "SV = [" + stats[i][1] + " ";
				} else if (i == (stats.length - 1)) {
					Smeans += stats[i][0] + " " + "]";
					Svariances += stats[i][1] + " " + "]";
				} else {
					Smeans += stats[i][0] + " ";
					Svariances += stats[i][1] + " ";
				}

			}
			System.out.println(Smeans);
			System.out.println(Svariances);
			//test: arrival means and variances
			String Ameans="AM=[1/120 0 1/60 0 0 1/60 0 0 1/60 0 0 1/30 0 1/30 0 1/30 0 1/30 0 1/30 0 1/30 0 1/30 0 1/30 0 1/30 0]";
			String Avariances="AV=[0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0]";
			System.out.println("ENG  "+ eng.toString()); 
			eng.evalString(Ameans + ";" + Avariances+";"+Smeans + ";" + Svariances);
			eng.evalString("whitt(AM,AV,SM,SV)");

			FloatMatrix fm = eng.getArray("means");

			//System.out.println("MATLAB OUTPUT: " + fm.toString());

			//TODO convert the floatmatrix into a HashMap of knob settings using the opmode-time model

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			//			if (eng!=null)
			//				eng.close();				
			System.out.println(
				"\n *------------------EXITING MATLAB-------------* @");
		}
	}

}