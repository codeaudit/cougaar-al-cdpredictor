package org.cougaar.cpe.agents.plugin.control;

import com.axiom.lib.mat.*;
import com.axiom.lib.util.*;
import java.util.*;
import java.io.*;

import org.cougaar.core.mts.MessageAddress;
import org.cougaar.tools.techspecs.qos.ControlMeasurement;
/*
 * @author nathan
 * talks to Matlab and Arena to help the QueueingModel plugin 
 */
public class ScalingModel {
	public ScalingModel() {

	}

	int modes = 3;
	int[] mode = new int[] { 4, 6, 8 };
	double[][] slope = new double[][] { { 1, 1.2737, 1.9482 }, {
			0.443, 1, 1.4283 }, {
			0.2587, 0.5452, 1 }
	};
	double[][] constant = new double[][] { { 0, 2217, 4985.6 }, {
			-132.9, 0, 2070.6 }, {
			-322.9, -89.3, 0 }
	};

	//inly for replan time, others dont change very much for now
	public double estimateRtime(int mode1, int mode2, double ctime) {

		//	   current mode, targeted mode, planning time using current mode

		int i, index1 = 0, index2 = 0;
		double estimatedrtime=0;

		for (i = 0; i <= modes - 1; i++) {
			if (mode[i] == mode1) {
				index1 = i;
			}
			if (mode[i] == mode2) {
				index2 = i;
			}
		}

		estimatedrtime = slope[index1][index2] * ctime + constant[index1][index2];
		return estimatedrtime;

	}

	public HashMap scale(ControlMeasurement c, HashMap target_modes) {
		//we use the knowledge that only replan time will change
		//i.e. only BN1, BN2 or BN3 replan time will change
		int curr_depth, target_depth;
		double curr_time, est_time;
		HashMap temp = c.getMeanTaskTimes();

		//FOR BN1---------------------
		try {
			curr_depth = ((Integer) c.getOpModeValue(MessageAddress.getMessageAddress("BN1"), "PlanningDepth")).intValue();
			curr_time = ((Double) c.getMeanTimeForTask(MessageAddress.getMessageAddress("BN1"), "ReplanTime")).doubleValue();
			target_depth = ((Integer) getOpModeValue(target_modes, MessageAddress.getMessageAddress("BN1"), "PlanningDepth")).intValue();

			//getting the model based estimate of time
			est_time = estimateRtime(curr_depth, target_depth, curr_time);
			System.out.println("Depth= "+curr_depth+" time= " +curr_time+" target_depth= "+target_depth+ " estimated_time= "+est_time);

			setOpModeValue(temp, MessageAddress.getMessageAddress("BN1"), "PlanningDepth", est_time);
		} catch (Exception e) {
			//System.out.println(e.getStackTrace());
		}

		//FOR BN2----------------------
		try {
			curr_depth = ((Integer) c.getOpModeValue(MessageAddress.getMessageAddress("BN2"), "PlanningDepth")).intValue();
			curr_time = ((Double) c.getMeanTimeForTask(MessageAddress.getMessageAddress("BN2"), "ReplanTime")).doubleValue();
			target_depth = ((Integer) getOpModeValue(target_modes, MessageAddress.getMessageAddress("BN2"), "PlanningDepth")).intValue();

			//getting the model based estimate of time
			est_time = estimateRtime(curr_depth, target_depth, curr_time);

			setOpModeValue(temp, MessageAddress.getMessageAddress("BN2"), "PlanningDepth", est_time);
		} catch (Exception e) {
			//System.out.println(e.getStackTrace());
		}

		//FOR BN3----------------------
		try {
			curr_depth = ((Integer) c.getOpModeValue(MessageAddress.getMessageAddress("BN3"), "PlanningDepth")).intValue();
			curr_time = ((Double) c.getMeanTimeForTask(MessageAddress.getMessageAddress("BN3"), "ReplanTime")).doubleValue();
			target_depth = ((Integer) getOpModeValue(target_modes, MessageAddress.getMessageAddress("BN3"), "PlanningDepth")).intValue();

			//getting the model based estimate of time
			est_time = estimateRtime(curr_depth, target_depth, curr_time);

			setOpModeValue(temp, MessageAddress.getMessageAddress("BN3"), "PlanningDepth", est_time);
		} catch (Exception e) {
			//System.out.println(e.getStackTrace());
		}

		return temp;
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

	public HashMap setOpModeValue(HashMap opModes, MessageAddress node, String opmodeName, int value) {
		if (opModes.containsKey((Object) node)) {
			HashMap h = (HashMap) opModes.get((Object) node);
			if (h.containsKey((Object) opmodeName)) {
				h.put((Object) opmodeName, new Integer(value));
				opModes.put(node, h);
				return opModes;
			}
		}
		return null;
	}

	public HashMap setOpModeValue(HashMap opModes, MessageAddress node, String opmodeName, double value) {
		if (opModes.containsKey((Object) node)) {
			HashMap h = (HashMap) opModes.get((Object) node);
			if (h.containsKey((Object) opmodeName)) {
				h.put((Object) opmodeName, new Double(value));
				opModes.put(node, h);
				return opModes;
			}
		}
		return null;
	}

}