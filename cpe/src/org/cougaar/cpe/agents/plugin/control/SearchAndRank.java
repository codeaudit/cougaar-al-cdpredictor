package org.cougaar.cpe.agents.plugin.control;

import com.axiom.lib.mat.*;
import com.axiom.lib.util.*;
import java.util.*;
import org.cougaar.cpe.agents.messages.ControlMessage;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.tools.techspecs.qos.ControlMeasurement;
/*
 * @author nathan
 * talks to Matlab and Arena to help the QueueingModel plugin 
 */
public class SearchAndRank {
	public SearchAndRank(ControlMeasurement c) {
		//this.cm=c;
		this.cm = new ControlMeasurement("System", "Permutations", MessageAddress.getMessageAddress("search"), System.currentTimeMillis(), c.getOpmodes(), c.getTaskTimes());
	}

	public ControlMessage getTop(int i) {
		//temporary : test
		//from the ranked array of QueueingParameters pick the ith highest rank and 
		//return its ControlMessage
		ControlMessage controlMsg = new ControlMessage("Test", "1");
		HashMap t = new HashMap();

		if (Math.random() > 0.5) {

			//case 1
			t.put((Object) "ReplanPeriod", new Integer(70000));
			controlMsg.putControlParameter((Object) MessageAddress.getMessageAddress("BN1"), (Object) t.clone());

			t.clear();
			t.put((Object) "WorldStateUpdatePeriod", new Integer(20000));
			controlMsg.putControlParameter((Object) MessageAddress.getMessageAddress("CPY1"), (Object) t.clone());

			t.clear();
			t.put((Object) "WorldStateUpdatePeriod", new Integer(30000));
			controlMsg.putControlParameter((Object) MessageAddress.getMessageAddress("CPY2"), (Object) t.clone());

			t.clear();
			t.put((Object) "WorldStateUpdatePeriod", new Integer(40000));
			controlMsg.putControlParameter((Object) MessageAddress.getMessageAddress("CPY3"), (Object) t.clone());
		} else {

			//case 2
			t.put((Object) "ReplanPeriod", new Integer(60000));
			controlMsg.putControlParameter((Object) MessageAddress.getMessageAddress("BN1"), (Object) t.clone());

			t.clear();
			t.put((Object) "WorldStateUpdatePeriod", new Integer(30000));
			controlMsg.putControlParameter((Object) MessageAddress.getMessageAddress("CPY1"), (Object) t.clone());

			t.clear();
			t.put((Object) "WorldStateUpdatePeriod", new Integer(30000));
			controlMsg.putControlParameter((Object) MessageAddress.getMessageAddress("CPY2"), (Object) t.clone());

			t.clear();
			t.put((Object) "WorldStateUpdatePeriod", new Integer(30000));
			controlMsg.putControlParameter((Object) MessageAddress.getMessageAddress("CPY3"), (Object) t.clone());
		}
		System.out.println(controlMsg.toString());
		return controlMsg;
	}

	public void rankAccordingToScore(ArrayList a) {
		//do the ranking using Score.java

		//store the ranked list in QueueingParameters
	}

	/*
	 * generates the search space
	 */
	public void generateQueueingParameters(ControlMeasurement c) {
		//contains search space, get from techspecs ultimately
		this.cm = c;
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

						//purmute necessary opmodes to search for desired opmodes and store in arraylist
						int[] db = (int[]) (depthbreadth.get(new Integer(k)));

						//System.out.println(count + ": " + temp);
						//System.out.println(count + ": " + setParamsZone(temp, 1, rt[i], ut[j], db[0], db[1]));
						//System.out.println(count + ": " + temp);
						//System.out.println(count + ": " + c.getOpmodes() + "\n");

						ArrayList a = permute(temp, rt[i], ut[j], db[0], db[1]);
						Iterator itr = a.iterator();
						while (itr.hasNext()) {
							count++;
						ScalingModel sm = new ScalingModel();
						// call a method to give the scaled times
						// params current_measurement, target_opmodes
						HashMap est = sm.scale(cm, (HashMap) itr.next());

						//printing original and scaled times
						System.out.println(count + ": MEASURED"+ cm.getMeanTaskTimes());
						System.out.println(count + ": SCALED  "+ est.toString()+"\n");
						//queueingParameters.add(new QueueingParameters(System.currentTimeMillis(), (HashMap) itr.next(), est));
						//
						//System.out.println(count + " [original]: " + temp);
						//System.out.println(count + " [changed ]: " + (HashMap) itr.next() + "\n");

						}

					}

				}

		//System.out.println("QUEUEING PARAMETERS\n" + toString());
		//System.out.println("COUNT= " + count);
	}

	private ArrayList permute(HashMap opmodes, int replan, int update, int depth, int breadth) {
		ArrayList t = new ArrayList();

		Object h1 = setParamsZone(opmodes, 1, replan, update, depth, breadth);
		Object h2 = setParamsZone(opmodes, 2, replan, update, depth, breadth);
		Object h3 = setParamsZone(opmodes, 3, replan, update, depth, breadth);

		//6 combinations: because of 3 zones
		if (h1 != null)
			t.add(h1);
		if (h2 != null)
			t.add(h2);
		if (h3 != null)
			t.add(h3);

		Object h4 = setParamsZone((HashMap) h1, 2, replan, update, depth, breadth);
		if (h4 != null)
			t.add(h4);

		Object h5 = setParamsZone((HashMap) h1, 3, replan, update, depth, breadth);
		if (h5 != null)
			t.add(h5);

		Object h6 = setParamsZone((HashMap) h2, 3, replan, update, depth, breadth);
		if (h6 != null)
			t.add(h6);

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
//			//original code			
//          if ((opModes != null) && (opModes.containsKey((Object) node))) {
//			HashMap h = (HashMap) opModes.get((Object) node);
//			if (h.containsKey((Object) opmodeName)) {
//				h.put((Object) opmodeName, new Integer(value));
//				opModes.put(node, h);
//				return opModes;
//			}
//		}
		return null;
	}

	/*
	 * returns the arraylist ofqueueing parameters
	 * 
	 */
	public String toString() {
		String temp = null;
		Iterator itr = queueingParameters.iterator();
		while (itr.hasNext()) {
			temp += ((QueueingParameters) itr.next()).toString() + "\n";
		}
		return temp;
	}

	private ControlMeasurement cm;
	private ArrayList queueingParameters = new ArrayList();
	private ArrayList rankedQueueingParameters = new ArrayList();

}