package org.cougaar.cpe.agents.plugin.control;

import com.axiom.lib.mat.*;
import com.axiom.lib.util.*;
import java.util.*;
import java.io.*;
import org.cougaar.cpe.agents.plugin.control.QueueingParameters;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.tools.techspecs.qos.ControlMeasurement;
/*
 * Given all queueing parameters, gives the MPF corresponding to that set of parameters
 */
public class ModelMG1 {
	public ModelMG1(QueueingParameters qm) {
		this.qm = qm;
	}

	double constant = 8418;
	double slope = 0.651;

	public double[] getEstimatedMPF() {
		//extract all necessary info from qm
		try {

			//CPYs
			String[] cpys = { "CPY1", "CPY2", "CPY3", "CPY4", "CPY5", "CPY6", "CPY7", "CPY8", "CPY9" };
			double[] timer_cpy = new double[9];
			double[] update_cpy = new double[9];
			double[] mplan_cpy = new double[9];
			for (int i = 0; i < 9; i++) {
				Object u = null;
				//timer_cpy
				if ((u = qm.getOpModeValue(MessageAddress.getMessageAddress(cpys[i]), "WorldStateUpdatePeriod")) != null) {
					timer_cpy[i] = ((Integer) u).intValue();
				} else
					timer_cpy[i] = 0;

				//update_cpy: time taken to update in CPYs
				if ((u = qm.getMeanTimeForTask(MessageAddress.getMessageAddress(cpys[i]), "StatusUpdateProcessTimer")) != null) {
					update_cpy[i] = ((Double) u).doubleValue();
				} else
					update_cpy[i] = 0;

				//mplan_cpy: time taken to process mplan in CPYs
				if ((u = qm.getMeanTimeForTask(MessageAddress.getMessageAddress(cpys[i]), "ProcessPlan")) != null) {
					mplan_cpy[i] = ((Double) u).doubleValue();
				} else
					mplan_cpy[i] = 0;
			}

			//BNs
			String[] bns = { "BN1", "BN2", "BN3" };
			double[] timer_bn = new double[3];
			double[] replan_bn = new double[3];
			double[] update_bn = new double[3];
			double[] zplan_bn = new double[3];
			for (int i = 0; i < 3; i++) {
				Object u = null;
				//timer_bn
				if ((u = qm.getOpModeValue(MessageAddress.getMessageAddress(bns[i]), "ReplanPeriod")) != null) {
					timer_bn[i] = ((Integer) u).intValue();
				} else
					timer_bn[i] = 0;

				//replan_bn: time taken to plan in BNs
				if ((u = qm.getMeanTimeForTask(MessageAddress.getMessageAddress(bns[i]), "ReplanTime")) != null) {
					replan_bn[i] = ((Double) u).doubleValue();
				} else
					replan_bn[i] = 0;

				//update_bn: time taken to update in BNs
				if ((u = qm.getMeanTimeForTask(MessageAddress.getMessageAddress(bns[i]), "ProcessUpdateBN")) != null) {
					update_bn[i] = ((Double) u).doubleValue();
				} else
					update_bn[i] = 0;

				//zplan_bn: time taken to process zplans in BNs
				if ((u = qm.getMeanTimeForTask(MessageAddress.getMessageAddress(bns[i]), "ProcessZonePlanBN")) != null) {
					zplan_bn[i] = ((Double) u).doubleValue();
				} else
					zplan_bn[i] = 0;
			}

			//System.out.println(printArray(timer_cpy));
			//System.out.println(printArray(timer_bn));

			return estimateMPF(timer_cpy, update_cpy, mplan_cpy, timer_bn, replan_bn, update_bn, zplan_bn, 0, 0, 0);
		} catch (Exception e) {
			System.out.println(e.toString());
		}

		return null;
	}

	public String printArray(double[] o) {
		String h = "";
		for (int i = 0; i < o.length; i++)
			h += o[i] + " ";
		return h;
	}

	double[] estimateMPF(
		double[] timer_cpy,
		double[] update_cpy,
		double[] mplan_cpy,
		double[] timer_bn,
		double[] replan_bn,
		double[] update_bn,
		double[] zplan_bn,
		double timer_bde,
		double zplan_bde,
		double update_bde) {

		int j, k, stable;
		double lamdau, lamda, s2, rau, ql, wtcpy, wtbn, mpf;
		double[] estimatedMPF = new double[3];

		for (j = 0; j <= 2; j++) {

			if (timer_cpy[j * 3] * timer_cpy[j * 3 + 1] * timer_cpy[j * 3 + 2] * timer_bn[j] == 0) {
				estimatedMPF[j] = -1;
				continue;
			}

			lamdau = 1 / timer_cpy[j * 3] + 1 / timer_cpy[j * 3 + 1] + 1 / timer_cpy[j * 3 + 2];
			lamda = 1 / timer_cpy[j * 3] + 1 / timer_cpy[j * 3 + 1] + 1 / timer_cpy[j * 3 + 2] + 1 / timer_bn[j];
			s2 = (lamdau * 2 * Math.pow(update_bn[j], 2) + 1 / timer_bn[j] * 2 * Math.pow(replan_bn[j], 2)) / lamda;
			rau = (lamdau * update_bn[j] + 1 / timer_bn[j] * replan_bn[j]);

			if (rau >= 1) {
				estimatedMPF[j] = -1;
				continue;
			}

			ql = rau + Math.pow(lamda, 2) * s2 / 2 / (1 - rau);
			wtbn = ql / lamda;
			mpf = 0;

			for (k = 0; k <= 2; k++) {
				lamda = 1 / timer_cpy[j * 3 + k] + 1 / timer_bn[j];
				s2 = (1 / timer_cpy[j * 3 + k] * 2 * Math.pow(update_cpy[j * 3 + k], 2) + 1 / timer_bn[j] * 2 * Math.pow(mplan_cpy[j * 3 + k], 2)) / lamda;
				rau = (1 / timer_cpy[j * 3 + k] * update_cpy[j * 3 + k] + 1 / timer_bn[j] * mplan_cpy[j * 3 + k]);
				if (rau >= 1) {
					mpf = -1;
					break;
				}
				ql = rau + Math.pow(lamda, 2) * s2 / 2 / (1 - rau);
				wtcpy = ql / lamda;
				mpf = mpf + 2 * wtcpy + wtbn + timer_cpy[j * 3 + k] / 2 + replan_bn[j];
			}

			if (mpf <= 0) {
				estimatedMPF[j] = -1;
			} else {
				mpf = mpf / 3;
				estimatedMPF[j] = slope * mpf + constant;
			}

		}

		return estimatedMPF;

	}
	private QueueingParameters qm = null;
}