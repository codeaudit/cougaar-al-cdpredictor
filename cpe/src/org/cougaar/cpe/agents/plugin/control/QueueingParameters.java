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
	public QueueingParameters(long ts, HashMap candidateOpmodes, double[][] estimatedtt) {
		super("SystemPurturbed", "Control", MessageAddress.getMessageAddress("BDE1"), ts, candidateOpmodes, estimatedtt);
		this.cm = new ControlMessage("SystemPurturbed", "Control");
		cm.putControlSet(candidateOpmodes);

		getMG1Estimate();
		computeScore();
	}

	public QueueingParameters(long ts, HashMap candidateOpmodes, HashMap estimatedtt) {
		super("SystemPurturbed", "Control", MessageAddress.getMessageAddress("BDE1"), ts, candidateOpmodes, estimatedtt);
		this.cm = new ControlMessage("SystemPurturbed", "Control");
		cm.putControlSet(candidateOpmodes);

		getMG1Estimate();
		computeScore();
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

	public void computeScore() {
		if (modelMG1AverageMPF != null) {
			Score s = new Score(modelMG1AverageMPF, this);
			score = s.getParametersAndEstimateScore();
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
	
	public ControlMessage getControlMsg(){
		return cm;
	}

	private ControlMessage cm;
	private double[] actualAverageMPF = null;
	private double[] modelMG1AverageMPF = null;
	private double[] modelWhittAverageMPF = null;
	private double[] score = null;
}