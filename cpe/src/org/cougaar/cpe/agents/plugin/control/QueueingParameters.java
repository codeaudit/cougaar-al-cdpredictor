package org.cougaar.cpe.agents.plugin.control;

import com.axiom.lib.mat.*;
import com.axiom.lib.util.*;
import java.util.*;
import org.cougaar.tools.techspecs.qos.ControlMeasurement;
import org.cougaar.core.mts.MessageAddress;

import org.cougaar.cpe.agents.messages.ControlMessage;
/*
 * @author nathan
 * talks to Matlab and Arena to help the QueueingModel plugin 
 */
public class QueueingParameters extends ControlMeasurement {
	public QueueingParameters(long ts, HashMap candidateOpmodes, double[][] estimatedtt, HashMap scores) {

		super("SystemPurturbed", "Control", MessageAddress.getMessageAddress("BDE1"), ts, candidateOpmodes, estimatedtt);
		this.cm = new ControlMessage("SystemPurturbed", "Control");
		cm.putControlSet(candidateOpmodes);
		
	}

	public QueueingParameters(long ts, HashMap candidateOpmodes, HashMap estimatedtt, HashMap scores) {

		super("SystemPurturbed", "Control", MessageAddress.getMessageAddress("BDE1"), ts, candidateOpmodes, estimatedtt);
		this.cm = new ControlMessage("SystemPurturbed", "Control");
		cm.putControlSet(candidateOpmodes);
		
	}

	public void getMG1Estimate() {
		ModelMG1 mg1 = new ModelMG1(this);
		double[] d = mg1.getEstimatedMPF();
		if (d != null) {
			modelMG1AverageMPF = d;
		}
	}

	public void getWhittEstimate() {
		//modelWhittMPF = 0; //set to calculated value later
	}

	public void computeScore(HashMap scores) {
		if ((modelMG1AverageMPF[0] != -1) || (modelMG1AverageMPF[1] != -1) || (modelMG1AverageMPF[2] != -1)) {
			Score s = new Score(modelMG1AverageMPF, this.getOpmodes());
			score = s.getParametersAndEstimateScore(scores);
			//System.out.println("TOTAL SCORE: "+getTotalScore());
		}
	}

	public void toString(StringBuffer buf) {
		super.toString(buf);
		if (actualAverageMPF != null)
			buf.append(", Average MPF from Measurement[zone1,zone2,zone3]= [" + actualAverageMPF[0] + "," + actualAverageMPF[1] + "," + actualAverageMPF[2] + "]");
		if (modelMG1AverageMPF != null)
			buf.append(", Average MPF from MG1[zone1,zone2,zone3]= [" + modelMG1AverageMPF[0] + "," + modelMG1AverageMPF[1] + "," + modelMG1AverageMPF[2] + "]");
		if (modelWhittAverageMPF != null)
			buf.append(", Average MPF from Whitt[zone1,zone2,zone3]= [" + modelWhittAverageMPF[0] + "," + modelWhittAverageMPF[1] + "," + modelWhittAverageMPF[2] + "]");
		if (score != null)
			buf.append(", SCORE Estimate[zone1,zone2,zone3]= [" + score[0] + "," + score[1] + "," + score[2] + "]");
	}

	public double getAvgScore() {
		if (score != null)
			return ((score[0] + score[1] + score[2]) / 3);
		return -1;
	}

	public double getTotalScore() {
		if (score != null)
			return (score[0] + score[1] + score[2]);
		return -1;
	}

	public ControlMessage getControlMsg() {
		return cm;
	}

	public Object getValue(String descriptor, int index) {
		if (descriptor.equalsIgnoreCase("MG1"))
			return new Double(modelMG1AverageMPF[index]);
		else if (descriptor.equalsIgnoreCase("WHITT"))
			return new Double(modelWhittAverageMPF[index]);
		else if (descriptor.equalsIgnoreCase("ACTUAL"))
			return new Double(actualAverageMPF[index]);
		else if (descriptor.equalsIgnoreCase("SCORE"))
			return new Double(score[index]);
		else
			return null;
	}

	public double[] getMG1() {
		return modelMG1AverageMPF;
	}

	public double[] getWhitt() {
		return modelWhittAverageMPF;
	}

	public double[] getArena() {
		return modelArenaAverageMPF;
	}

	public double[] getScore() {
		return score;
	}

	public double getScore(int zone) {
		return score[zone - 1];
	}

	public HashMap getZoneControlParams(int zone) {
		String[] zone1 = { "BN1", "CPY1", "CPY2", "CPY3" };
		String[] zone2 = { "BN2", "CPY4", "CPY5", "CPY6" };
		String[] zone3 = { "BN3", "CPY7", "CPY8", "CPY9" };
		HashMap temp = new HashMap();
		if (zone == 1) {

			for (int i = 0; i < zone1.length; i++) {
				temp.put(MessageAddress.getMessageAddress(zone1[i]), cm.getAgentControlParams(MessageAddress.getMessageAddress(zone1[i])));
			}
		} else if (zone == 2) {

			for (int i = 0; i < zone2.length; i++) {
				temp.put(MessageAddress.getMessageAddress(zone2[i]), cm.getAgentControlParams(MessageAddress.getMessageAddress(zone2[i])));
			}
		} else if (zone == 3) {
			for (int i = 0; i < zone3.length; i++) {
				temp.put(MessageAddress.getMessageAddress(zone3[i]), cm.getAgentControlParams(MessageAddress.getMessageAddress(zone3[i])));
			}
		}

		return temp;
	}
	

	private ControlMessage cm;
	private double[] actualAverageMPF = { -1, -1, -1 };
	private double[] modelMG1AverageMPF = { -1, -1, -1 };
	private double[] modelWhittAverageMPF = { -1, -1, -1 };
	private double[] modelArenaAverageMPF = { -1, -1, -1 };
	private double[] score = { -1, -1, -1 }; //estimated
}