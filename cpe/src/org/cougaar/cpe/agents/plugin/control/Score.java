package org.cougaar.cpe.agents.plugin.control;

import com.axiom.lib.mat.*;
import com.axiom.lib.util.*;
import java.util.*;
import java.io.*;
/*
 * @author nathan
 * talks to Matlab and Arena to help the QueueingModel plugin 
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

	public Score() {

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

		} catch (IOException e) {
		}

	}

	double[] estimateScore(double[] para1, int[] para2, int[] para3, double para4[], double[] para5) {

		//	   para1: entryRate, para2: rTimer, para3: pMode, para4: mpf, para5: mau
		//	   mau: kills, attritions, violations, penalties, and fuel consumption

		double eBound, mBound;
		int i, j, k, count;
		double[] estimatedScore = new double[3];
		double[] s = null;

		for (i = 0; i <= 2; i++) {

			eBound = entryBound;
			mBound = mpfBound;
			count = 0;

			while (count < nn) {

				count = 0;
				s = new double[5];

				for (j = 0; j <= size - 1; j++) {

					if ((rTimer[j] == para2[i]) && (pMode[j] == para3[i])) {

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

				eBound = eBound + entryInc;
				mBound = mBound + mpfInc;

			}

			for (j = 0; j <= 4; j++) {
				estimatedScore[i] = estimatedScore[i] + s[j] / count * para5[j];
			}

		}

		return estimatedScore;
	}

}