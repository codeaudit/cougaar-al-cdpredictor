package org.cougaar.cpe.agents.plugin.control;

import com.axiom.lib.mat.*;
import com.axiom.lib.util.*;
import java.util.*;
import java.io.*;
import org.cougaar.core.mts.MessageAddress;
/*
 * @author nathan
 *  
 */
public class Score {

	//	   This class estimates score given entryRate, rTimer, pMode, and mpf considering mau.

	double entryBound = 0.1, entryInc = 0.01;
	double mpfBound = 0.1, mpfInc = 0.01;
	int nn = 3;
	int size = 216;

	double[] entryRate = new double[size];
	int[] rTimer = new int[size];
	int[] pMode = new int[size];
	double[] mpf = new double[size];
	double[][] score = new double[size][5];

	//	   kills, attritions, violations, penalties, and fuel consumption

	public Score(double[] estimated_mpf, QueueingParameters q) {

		int i, j, indexs, indexn;
		String str = null;
		String fname = "scoremodel.txt";
		BufferedReader is = null;

		try {

			is = new BufferedReader(new FileReader(fname));

			for (i = 0; i <= size - 1; i++) {

				str = is.readLine();
				indexs = 0;
				indexn = str.indexOf('\t', 0);
				entryRate[i] = Double.parseDouble(str.substring(indexs, indexn));
				indexs = indexn + 1;
				indexn = str.indexOf('\t', indexn + 1);
				rTimer[i] = Integer.parseInt(str.substring(indexs, indexn));
				indexs = indexn + 1;
				indexn = str.indexOf('\t', indexn + 1);
				pMode[i] = Integer.parseInt(str.substring(indexs, indexn));
				indexs = indexn + 1;
				indexn = str.indexOf('\t', indexn + 1);
				mpf[i] = Double.parseDouble(str.substring(indexs, indexn));

				for (j = 0; j <= 4; j++) {
					indexs = indexn + 1;
					indexn = str.indexOf('\t', indexn + 1);
					score[i][j] = Double.parseDouble(str.substring(indexs, indexn));
				}

			}

			is.close();

			this.m = estimated_mpf;
			this.q = q;

		} catch (IOException e) {
		}

	}

	public double[] getParametersAndEstimateScore(HashMap scores) {
		//BNs
		String[] bns = { "BN1", "BN2", "BN3" };
		String[] things = { "EntryRate" };
		int[] timer_bn = new int[3];
		int[] plan_depth = new int[3];

		//----revisit later
		double[] entry_rate = new double[3];
		//System.out.println(scores);
		if (scores != null) {
			Object s = scores.get((Object) "BN1.EntryRate");
			if (s != null)
				entry_rate[0] = ((Double) s).doubleValue();
			else
				entry_rate[0] = 0;
			s = scores.get((Object) "BN2.EntryRate");
			if (s != null)
				entry_rate[1] = ((Double) s).doubleValue();
			else
				entry_rate[1] = 0;
			s = scores.get((Object) "BN3.EntryRate");
			if (s != null)
				entry_rate[2] = ((Double) s).doubleValue();
			else
				entry_rate[2] = 0;
		}
		//TODO remove later
		//double[] e_rate = { 0.6, 0.6, 0.6 };
	
		//double[] mau = { 42.14, 0.76, -106.88, -2.77, -1.09 };
		double[] mau = { 15, 1, -50, -5, -0.2 };
		//---------------------------------

		for (int i = 0; i < 3; i++) {
			Object u = null;
			//timer_bn
			if ((u = q.getOpModeValue(MessageAddress.getMessageAddress(bns[i]), "ReplanPeriod")) != null) {
				timer_bn[i] = ((Integer) u).intValue();
			} else
				timer_bn[i] = 0;

			//replan_bn: time taken to plan in BNs
			if ((u = q.getOpModeValue(MessageAddress.getMessageAddress(bns[i]), "PlanningDepth")) != null) {
				plan_depth[i] = ((Integer) u).intValue();
			} else
				plan_depth[i] = 0;
		}

		//I dont know mau and entry rate at this point.Using what I got.
		//				if (m != null)
		//					System.out.println("["+
		//						timer_bn[0] + "," + timer_bn[1] + "," + timer_bn[2] + "],[" + plan_depth[0] + "," + plan_depth[1] + "," + plan_depth[2] + "],[" + m[0] + "," + m[1] + "," + m[2]+ "],[" + entry_rate[0] + "," + entry_rate[1] + "," + entry_rate[2]+"]");
		return estimateScore(entry_rate, timer_bn, plan_depth, m, mau);
		//double[] d = { 0, 0, 0 };
		//return d;

	}

	double[] estimateScore(double[] para1, int[] para2, int[] para3, double para4[], double[] para5) {

		// para1: entryRate, para2: rTimer, para3: pMode, para4: mpf, para5: mau
		// mau: kills, attritions, violations, penalties, and fuel consumption

		double eBound, mBound;
		int i, j, k, count, check;
		double[] estimatedScore = new double[3];
		double[] s = null;

		for (i = 0; i <= 2; i++) {

			eBound = entryBound;
			mBound = mpfBound;
			count = 0;
			check = 0;

			while (count < nn) {

				count = 0;
				s = new double[5];

				for (j = 0; j <= size - 1; j++) {

					if ((rTimer[j] == para2[i]) && (pMode[j] == para3[i])) {

						check++;

						if ((entryRate[j] >= para1[i] * (1 - eBound)) && (entryRate[j] <= para1[i] * (1 + eBound))) {

							if ((mpf[j] >= para4[i] * (1 - mBound)) && (mpf[j] <= para4[i] * (1 + mBound))) {

								count = count + 1;

								for (k = 0; k <= 4; k++) {
									s[k] = s[k] + score[j][k];
								}
							}
						}
					}
				}

				if ((check == 0) || (para1[i] <= 0) || (para4[i] <= 0)) {
					estimatedScore[i] = -1;
					break;
				}

				eBound = eBound + entryInc;
				mBound = mBound + mpfInc;

			}

			if ((check == 0) || (para1[i] <= 0) || (para4[i] <= 0)) {
				continue;
			}

			for (j = 0; j <= 4; j++) {
				estimatedScore[i] = estimatedScore[i] + s[j] / count * para5[j];
			}

		}

		return estimatedScore;
	}
	QueueingParameters q = null;
	double[] m = null;
}