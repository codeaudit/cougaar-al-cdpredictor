package org.cougaar.cpe.agents.plugin.control;

import com.axiom.lib.mat.*;
import com.axiom.lib.util.*;
import java.util.*;
import java.io.*;
import org.cougaar.cpe.agents.plugin.control.QueueingParameters;
/*
 * Given all queueing parameters, gives the MPF corresponding to that set of parameters
 * 
 */
public class ModelMG1 {
	public ModelMG1(QueueingParameters qm) {

	}
	public double getMPF() {
		return 0;
	}

	double constant = 8418;
	double slope = 0.651;

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

}