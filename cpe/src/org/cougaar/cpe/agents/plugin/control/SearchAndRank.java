package org.cougaar.cpe.agents.plugin.control;

import com.axiom.lib.mat.*;
import com.axiom.lib.util.*;
import java.util.*;
import org.cougaar.cpe.agents.messages.ControlMessage;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.tools.techspecs.qos.ControlMeasurement;
import java.util.Collections;
import java.util.Comparator;
import java.io.*;
/*
 * @author nathan
 * talks to Matlab and Arena to help the QueueingModel plugin 
 */
public class SearchAndRank {
	public SearchAndRank(ControlMeasurement c, HashMap s) {
		this.cm = c;
		//this.scores = new HashMap(s);
		//this.cm = new ControlMeasurement("System", "Permutations", MessageAddress.getMessageAddress("search"), System.currentTimeMillis(), c.getOpmodes(), c.getTaskTimes());
	}

	public ControlMessage getTop(int i) {
		//from the ranked array of QueueingParameters pick the ith highest rank and 
		//return its ControlMessage
		//ControlMessage controlMsg = new ControlMessage("aggCntlMessage", "1");
		//HashMap t = new HashMap();

		//getting a legal control message by checking the ith element has some valid score.
		//		if ((rankedQueueingParameters.size() > 0) && (((QueueingParameters) rankedQueueingParameters.get(i - 1)).getTotalScore() != -3)) {
		//			controlMsg = ((QueueingParameters) rankedQueueingParameters.get(i - 1)).getControlMsg();
		//			if (controlMsg != null)
		//				System.out.println("Current CONTROL MESSAGE: " + controlMsg.toString());
		//		} else
		//			System.out.println("Could not identify CONTROL SET");
		//		return controlMsg;
		ControlMessage controlMsg = rankQueueingParameters();
		if (controlMsg != null) {
			System.out.println("Current CONTROL MESSAGE: " + controlMsg.toString());
		} else
			System.out.println("Could not identify CONTROL SET");
		try {
			FileOutputStream fout = new FileOutputStream("check.txt");
			String t = toString();
			fout.write(t.getBytes());
		} catch (Exception e) {

		}
		return controlMsg;

	}

	public QueueingParameters getQParas() {
		if (rankedQueueingParameters.size() > 0)
			return (QueueingParameters) rankedQueueingParameters.get(0);
		else
			return null;
	}

	/*
	 * generates the search space
	 */
	public ArrayList generateQueueingParameters(ControlMeasurement c, HashMap scores) {
		//contains search space, get from techspecs ultimately
		//System.out.println(scores);
		this.cm = c;
		this.avg_scores = scores;
		int[] rt = { 20000, 40000, 60000 };
		int[] ut = { 5000, 10000, 15000, 20000, 25000, 30000, 35000, 40000 };
		HashMap depthbreadth = new HashMap();
		int[] temp1 = { 4, 40 };
		depthbreadth.put(new Integer(1), (Object) temp1);
		int[] temp2 = { 6, 60 };
		depthbreadth.put(new Integer(2), (Object) temp2);
		int[] temp3 = { 8, 80 };
		depthbreadth.put(new Integer(3), (Object) temp3);

		//String[] zone1 = { "BN1", "CPY1", "CPY2", "CPY3" };
		//String[] zone2 = { "BN2", "CPY4", "CPY5", "CPY6" };
		//String[] zone3 = { "BN3", "CPY7", "CPY8", "CPY9" };
		//int zones[] = { 1, 2, 3 };

		queueingParameters.clear();
		HashMap temp = new HashMap(c.getOpmodes());

		int count = 0;
		for (int k = 1; k < 4; k++) //opmode
			for (int i = 0; i < rt.length; i++) //replantimer
				for (int j = 0; j < ut.length; j++) { //update timer
					if (c != null) {
						//System.out.println(count++ + " [original]: " + temp);
						//get current opmodes from measurement

						//permute necessary opmodes to search for desired opmodes and store in arraylist
						int[] db = (int[]) (depthbreadth.get(new Integer(k)));
						ArrayList a = permute(temp, rt[i], ut[j], db[0], db[1]);
						Iterator itr = a.iterator();
						while (itr.hasNext()) {
							count++;
							ScalingModel sm = new ScalingModel();
							HashMap h = (HashMap) itr.next();
							// call a method to give the scaled times
							// params current_measurement, target_opmodes
							HashMap est = sm.scale(cm, h);
							if (sm.getifEstimated()) {
								QueueingParameters qp = new QueueingParameters(System.currentTimeMillis(), h, est, scores);
								try {
									qp.getMG1Estimate();
									qp.computeScore(scores);
								} catch (Exception e) {
									e.printStackTrace();
								}
								queueingParameters.add(qp);
							}
						}

					}

				}

		//rankQueueingParameters(); //orig
		//System.out.println("AVERAGED SCORES\n" + scores);
		//System.out.println("QUEUEING PARAMETERS\n" + toString());
		//return rankedQueueingParameters; //orig
		return queueingParameters; //just all queueingParameters
	}

	private ControlMessage rankQueueingParameters() {
		ArrayList rankedQueueingParameters_Z1 = new ArrayList(queueingParameters);
		Collections.sort(rankedQueueingParameters_Z1, new Comparator() {
			public int compare(Object o1, Object o2) {
				QueueingParameters q1 = (QueueingParameters) o1, q2 = (QueueingParameters) o2;
				double a, b;
				a = q1.getScore(1);
				b = q2.getScore(1);
				if (a == b)
					return 0;
				else if (a < b)
					return 1;
				else
					return -1;
			}
		});
		HashMap h1 = new HashMap();
		for (int i = 0; i < rankedQueueingParameters_Z1.size(); i++) {
			if (((QueueingParameters) rankedQueueingParameters_Z1.get(i)).getScore(1) != -1) {
				h1 = ((QueueingParameters) rankedQueueingParameters_Z1.get(i)).getZoneControlParams(1);
				cntlScore_est[0] = ((QueueingParameters) rankedQueueingParameters_Z1.get(i)).getScore(1);
				double[] temp = (double[]) ((QueueingParameters) rankedQueueingParameters_Z1.get(i)).getMG1();
				if (temp != null)
					MPF_MG1_est[0] = temp[0];
				break;
			}
		}

		ArrayList rankedQueueingParameters_Z2 = new ArrayList(queueingParameters);
		Collections.sort(rankedQueueingParameters_Z2, new Comparator() {
			public int compare(Object o1, Object o2) {
				QueueingParameters q1 = (QueueingParameters) o1, q2 = (QueueingParameters) o2;
				double a, b;
				a = q1.getScore(2);
				b = q2.getScore(2);
				if (a == b)
					return 0;
				else if (a < b)
					return 1;
				else
					return -1;
			}
		});

		HashMap h2 = new HashMap();
		for (int i = 0; i < rankedQueueingParameters_Z2.size(); i++) {
			if (((QueueingParameters) rankedQueueingParameters_Z2.get(i)).getScore(2) != -1) {
				h2 = ((QueueingParameters) rankedQueueingParameters_Z2.get(i)).getZoneControlParams(2);
				cntlScore_est[1] = ((QueueingParameters) rankedQueueingParameters_Z1.get(i)).getScore(2);
				double[] temp = (double[]) ((QueueingParameters) rankedQueueingParameters_Z1.get(i)).getMG1();
				if (temp != null)
					MPF_MG1_est[1] = temp[1];
				break;
			}
		}

		ArrayList rankedQueueingParameters_Z3 = new ArrayList(queueingParameters);
		Collections.sort(rankedQueueingParameters_Z3, new Comparator() {
			public int compare(Object o1, Object o2) {
				QueueingParameters q1 = (QueueingParameters) o1, q2 = (QueueingParameters) o2;
				double a, b;
				a = q1.getScore(3);
				b = q2.getScore(3);
				if (a == b)
					return 0;
				else if (a < b)
					return 1;
				else
					return -1;
			}
		});

		HashMap h3 = new HashMap();
		for (int i = 0; i < rankedQueueingParameters_Z3.size(); i++) {
			if (((QueueingParameters) rankedQueueingParameters_Z3.get(i)).getScore(3) != -1) {
				h3 = ((QueueingParameters) rankedQueueingParameters_Z3.get(i)).getZoneControlParams(3);
				cntlScore_est[2] = ((QueueingParameters) rankedQueueingParameters_Z1.get(i)).getScore(3);
				double[] temp = (double[]) ((QueueingParameters) rankedQueueingParameters_Z1.get(i)).getMG1();
				if (temp != null)
					MPF_MG1_est[2] = temp[2];
				break;
			}
		}

		HashMap totalParams = new HashMap();
		if (h1.size() > 0)
			totalParams.putAll(h1);
		if (h2.size() > 0)
			totalParams.putAll(h2);
		if (h3.size() > 0)
			totalParams.putAll(h3);

		ControlMessage cntl;
		if (totalParams.size() > 0) {
			cntl = new ControlMessage("aggCntlMessage", "1");
			cntl.putControlSet(totalParams);
			return cntl;
		}
		return null;
	}

	private ArrayList permute(HashMap opmodes, int replan, int update, int depth, int breadth) {
		ArrayList t = new ArrayList();
		ArrayList obs = new ArrayList();

		for (int i = 0; i < 3; i++) {
			Object ob = setParamsZone(opmodes, i + 1, replan, update, depth, breadth);
			obs.add(ob);
		}

		Object h = (Object) opmodes;
		for (int i = 0; i < 3; i++) {
			if (obs.get(i) != null)
				h = setParamsZone((HashMap) h, i + 1, replan, update, depth, breadth);
		}
		//System.out.println("In Permute " + h);

		if (h != null)
			t.add(h);

		//				Object h1 = setParamsZone(opmodes, 1, replan, update, depth, breadth);
		//				Object h2 = setParamsZone(opmodes, 2, replan, update, depth, breadth);
		//				Object h3 = setParamsZone(opmodes, 3, replan, update, depth, breadth);
		//		
		//				//6 combinations: because of 3 zones
		//				if (h1 != null)
		//					t.add(h1);
		//				if (h2 != null)
		//					t.add(h2);
		//				if (h3 != null)
		//					t.add(h3);
		//		
		//				Object h4 = setParamsZone((HashMap) h1, 2, replan, update, depth, breadth);
		//				if (h4 != null)
		//					t.add(h4);
		//		
		//				Object h5 = setParamsZone((HashMap) h1, 3, replan, update, depth, breadth);
		//				if (h5 != null)
		//					t.add(h5);
		//		
		//				Object h6 = setParamsZone((HashMap) h2, 3, replan, update, depth, breadth);
		//				if (h6 != null)
		//					t.add(h6);

		//		System.out.println("@permute: size of arraylist" + t.size());
		return t;
	}

	private HashMap setParamsZone(final HashMap tempOpmode, int zone, int replantime, int updatetime, int depth, int breadth) {

		//System.out.println("[ORIGINAL]@begin: " + tempOpmode);

		if (tempOpmode != null) {
			HashMap temp = new HashMap(tempOpmode);
			switch (zone) {
				case 1 :
					temp = setOpModeValue(temp, MessageAddress.getMessageAddress("BN1"), "ReplanPeriod", replantime);
					temp = setOpModeValue(temp, MessageAddress.getMessageAddress("BN1"), "PlanningDepth", depth);
					temp = setOpModeValue(temp, MessageAddress.getMessageAddress("BN1"), "PlanningBreadth", breadth);
					temp = setOpModeValue(temp, MessageAddress.getMessageAddress("CPY1"), "WorldStateUpdatePeriod", updatetime);
					temp = setOpModeValue(temp, MessageAddress.getMessageAddress("CPY2"), "WorldStateUpdatePeriod", updatetime);
					temp = setOpModeValue(temp, MessageAddress.getMessageAddress("CPY3"), "WorldStateUpdatePeriod", updatetime);
					break;

				case 2 :
					temp = setOpModeValue(temp, MessageAddress.getMessageAddress("BN2"), "ReplanPeriod", replantime);
					temp = setOpModeValue(temp, MessageAddress.getMessageAddress("BN2"), "PlanningDepth", depth);
					temp = setOpModeValue(temp, MessageAddress.getMessageAddress("BN2"), "PlanningBreadth", breadth);
					temp = setOpModeValue(temp, MessageAddress.getMessageAddress("CPY4"), "WorldStateUpdatePeriod", updatetime);
					temp = setOpModeValue(temp, MessageAddress.getMessageAddress("CPY5"), "WorldStateUpdatePeriod", updatetime);
					temp = setOpModeValue(temp, MessageAddress.getMessageAddress("CPY6"), "WorldStateUpdatePeriod", updatetime);
					break;

				case 3 :
					temp = setOpModeValue(temp, MessageAddress.getMessageAddress("BN3"), "ReplanPeriod", replantime);
					temp = setOpModeValue(temp, MessageAddress.getMessageAddress("BN3"), "PlanningDepth", depth);
					temp = setOpModeValue(temp, MessageAddress.getMessageAddress("BN3"), "PlanningBreadth", breadth);
					temp = setOpModeValue(temp, MessageAddress.getMessageAddress("CPY7"), "WorldStateUpdatePeriod", updatetime);
					temp = setOpModeValue(temp, MessageAddress.getMessageAddress("CPY8"), "WorldStateUpdatePeriod", updatetime);
					temp = setOpModeValue(temp, MessageAddress.getMessageAddress("CPY9"), "WorldStateUpdatePeriod", updatetime);
					break;
			}
			//System.out.println("[ORIGINAL]@end  : " + tempOpmode);
			//System.out.println("[CHANGED ]@end  : " + temp);
			return temp;
		}
		return null;
	}

	public Object getOpModeValue(HashMap opModes, MessageAddress node, String opmodeName) {
		if (opModes.containsKey((Object) node)) {
			HashMap h = (HashMap) opModes.get((Object) node);
			if (h.containsKey((Object) opmodeName)) {
				return ((Object) h.get((Object) opmodeName));
			}
		}
		return null;
	}

	//only if that node and opmode is there it will set/change the opmode, otherwise it will return null
	public HashMap setOpModeValue(HashMap opModes, MessageAddress node, String opmodeName, int value) {
		// this code involves a selective recreation of the original opmodes hashmap while fusing the needed changes 

		HashMap temp = new HashMap();
		if ((opModes != null) && (opModes.containsKey((Object) node))) {
			Collection c = opModes.keySet();
			Iterator i = c.iterator();
			while (i.hasNext()) {
				Object o = i.next();
				if (!((MessageAddress) o).equals(node)) {
					temp.put(o, opModes.get(o));
				} else {
					HashMap h1 = (HashMap) opModes.get((Object) node);
					HashMap tempHash = new HashMap();
					Collection c1 = h1.keySet();
					Iterator ii = c1.iterator();
					while (ii.hasNext()) {
						Object o1 = ii.next();
						if (!((String) o1).equals(opmodeName)) {
							tempHash.put(o1, h1.get(o1));
						} else {
							tempHash.put(opmodeName, new Integer(value));
						}
					}
					temp.put(node, tempHash);
				}
			}
			return temp;
		}
		return null;
	}

	public double[] getMG1() {
		return MPF_MG1_est;
	}

	public double[] getWhitt() {
		return MPF_Whitt_est;
	}

	public double[] getArena() {
		return MPT_Arena_est;
	}

	public double[] getScore() {
		return cntlScore_est;
	}

	/*
	 * returns the arraylist ofqueueing parameters
	 * 
	 */
	public String toString() {
		String temp = "";
		temp += "Actual Control_Measurement= " + cm.toString() + "\n";
		temp += "Estimates(unranked): \n";
		Iterator itr = queueingParameters.iterator();
		int i = 0;
		//while ((itr.hasNext()) && (i++ < 10)) {
		while ((itr.hasNext())) {
			temp += ((QueueingParameters) itr.next()).toString() + "\n";
		}
		temp += "\n";
		return temp;
	}

	public static void main(String[] args) {

		HashMap test = new HashMap();
		HashMap test1 = new HashMap();
		test1.put("ReplanPeriod", new Integer(60000));
		test1.put("PlanningBreadth", new Integer(40));
		test1.put("PlanningDepth", new Integer(4));
		//test.put(MessageAddress.getMessageAddress("BN1"), test1.clone());
		test.put(MessageAddress.getMessageAddress("BN2"), test1.clone());
		test.put(MessageAddress.getMessageAddress("BN3"), test1.clone());
		test1.clear();
		test1.put("WorldStateUpdatePeriod", new Integer(30000));
		test.put(MessageAddress.getMessageAddress("CPY1"), test1.clone());
		test.put(MessageAddress.getMessageAddress("CPY2"), test1.clone());
		test.put(MessageAddress.getMessageAddress("CPY3"), test1.clone());
		test.put(MessageAddress.getMessageAddress("CPY4"), test1.clone());
		test.put(MessageAddress.getMessageAddress("CPY5"), test1.clone());
		test.put(MessageAddress.getMessageAddress("CPY6"), test1.clone());
		test.put(MessageAddress.getMessageAddress("CPY7"), test1.clone());
		test.put(MessageAddress.getMessageAddress("CPY8"), test1.clone());
		test.put(MessageAddress.getMessageAddress("CPY9"), test1.clone());
		System.out.println(test.toString());

		double[][] t = { { 20000, 0 }, {
				20, 0 }, {
				6000, 0 }, {
				2, 0 }, {
				2000, 20 }, {
				6000, 0 }, {
				2, 0 }, {
				2000, 0 }, {
				6000, 0 }, {
				2, 0 }, {
				2000, 0 }, {
				2000, 0 }, {
				1000, 0 }, {
				2000, 0 }, {
				1000, 0 }, {
				2000, 0 }, {
				1000, 0 }, {
				2000, 0 }, {
				1000, 0 }, {
				2000, 0 }, {
				1000, 0 }, {
				2000, 0 }, {
				1000, 0 }, {
				2000, 0 }, {
				1000, 0 }, {
				2000, 0 }, {
				1000, 0 }, {
				2000, 0 }, {
				1000, 0 }
		};
		String[] keys =
			{
				"BN1.Kills",
				"BN1.Attrition",
				"BN1.Violations",
				"BN1.Penalties",
				"BN1.FuelConsumption.CPY1",
				"BN1.FuelConsumption.CPY2",
				"BN1.FuelConsumption.CPY3",
				"BN2.Kills",
				"BN2.Attrition",
				"BN2.Violations",
				"BN2.Penalties",
				"BN2.FuelConsumption.CPY4",
				"BN2.FuelConsumption.CPY5",
				"BN2.FuelConsumption.CPY6",
				"BN3.Kills",
				"BN3.Attrition",
				"BN3.Violations",
				"BN3.Penalties",
				"BN3.FuelConsumption.CPY7",
				"BN3.FuelConsumption.CPY8",
				"BN3.FuelConsumption.CPY9",
				"BN1.EntryRate",
				"BN2.EntryRate",
				"BN3.EntryRate" };

		double[] scores_data = { 20, 30, 40, 35, 20, 20, 20, 20, 30, 40, 35, 20, 20, 20, 20, 30, 40, 35, 20, 20, 20, 0.6, 0.6, 0.6 };
		HashMap sco = new HashMap();
		for (int i = 0; i < scores_data.length; i++)
			sco.put(keys[i], new Double(scores_data[i]));
		ControlMeasurement c_test = new ControlMeasurement("", "", MessageAddress.getMessageAddress("BDE1"), 0, test, t);

		SearchAndRank sr = new SearchAndRank(c_test, sco);
		if (sr.ifControlMeasurementOk(c_test)) {
			ArrayList h = sr.generateQueueingParameters(c_test, sco);

			// sr.permute(test,40000,5000,6,60);
			for (int i = 0; i < h.size(); i++)
				System.out.println(i + ") " + ((QueueingParameters) h.get(i)).toString());

			System.out.println("\n");
			sr.getTop(1);
		}

	}

	private boolean ifControlMeasurementOk(ControlMeasurement c) {
		String[] SubordinateList = { "BN1", "BN2", "BN3", "CPY1", "CPY2", "CPY3", "CPY4", "CPY5", "CPY6", "CPY7", "CPY8", "CPY9" };
		HashMap h = c.getOpmodes();
		boolean check = true;
		for (int i = 0; i < SubordinateList.length; i++) {

			if (h.containsKey(MessageAddress.getMessageAddress(SubordinateList[i])) == false) {
				check = false;
				break;
			}
		}
		return check;
	}

	private ControlMeasurement cm;
	private HashMap avg_scores;
	private ArrayList queueingParameters = new ArrayList();
	private ArrayList rankedQueueingParameters = new ArrayList();
	private double[] cntlScore_est = new double[3];
	private double[] MPF_MG1_est = new double[3];
	private double[] MPF_Whitt_est = new double[3];
	private double[] MPT_Arena_est = new double[3];

}