package org.cougaar.tools.techspecs.qos;

import org.cougaar.core.mts.MessageAddress;
import java.util.*;

/**
 * User: Nathan Gnanasambandam
 * Date: 7/30/2004
 * Time: 4:30:00 PM
 * USED FOR A WHOLE BUNCH OF HISTORY FOR CONTROL PURPOSES
 * Used for a complete or partial snapshot the entire CPE conditions along with some measured values at various levels
 * Can also be used to find who much time it took to process at task and at what depth and breadth 
 */
public class ControlMeasurement extends MeasurementImpl implements Cloneable {

	public ControlMeasurement(String eventName, String actionName, MessageAddress source, long ts, HashMap opmodes, double[][] tft) {
		super(eventName, actionName, source);
		this.timeStamp = ts;
		this.opModes = opmodes;
		this.timeForTasks = tft;
		this.taskTimes = convertTimesToMap(tft); //dont know if this measurement is gonna work
	}

	public ControlMeasurement(String eventName, String actionName, MessageAddress source, long ts, HashMap opmodes, HashMap tft) {
		super(eventName, actionName, source);
		this.timeStamp = ts;
		this.opModes = opmodes;
		this.timeForTasks = null; //TODO since the hashmap is directly given, here the variances are lost, 
		this.taskTimes = tft;
	}

	public void toString(StringBuffer buf) {
		super.toString(buf);
		buf.append(",OpModes= " + opModes.toString());
		if (timeForTasks != null) {
			String times = "Task Means and Variances(ms)=[";
			for (int i = 0; i < 29; i++)
				times += i + ":(" + Math.ceil(timeForTasks[i][0]) + "," + Math.ceil(timeForTasks[i][1]) + ")";
			times += "]";
			buf.append("," + times);
		} else
			buf.append(",Mean Task Times= " + taskTimes.toString());
		buf.append(",TimeStamp= " + timeStamp);
	}

	public Object getMeanTimeForTask(MessageAddress node, String taskName) {
		if (taskTimes.containsKey((Object) node)) {
			HashMap h = (HashMap) taskTimes.get((Object) node);
			if (h.containsKey((Object) taskName)) {
				return ((Object) h.get((Object) taskName));
			}
		}
		return null;
	}

	public double[][] getTaskTimes() {
		return timeForTasks;
	}

	public HashMap getOpmodes() {
		HashMap temp = new HashMap();
		if (opModes != null) {
			Collection c = opModes.keySet();
			Iterator itr = c.iterator();
			while (itr.hasNext()) {
				Object o = itr.next();
				temp.put(o, opModes.get(o));
			}
			return temp;
		} else
			return null;

		//return (HashMap)opModes.clone(); this clone may return a shallow copy
	}

	public HashMap getMeanTaskTimes() {
		return taskTimes;
	}

	public Object getOpModeValue(MessageAddress node, String opmodeName) {
		if (opModes.containsKey((Object) node)) {
			HashMap h = (HashMap) opModes.get((Object) node);
			if (h.containsKey((Object) opmodeName)) {
				return ((Object) h.get((Object) opmodeName));
			}
		}
		return null;
	}

	public void setOpModeValue(MessageAddress node, String opmodeName, int value) {
		if (opModes.containsKey((Object) node)) {
			HashMap h = (HashMap) opModes.get((Object) node);
			if (h.containsKey((Object) opmodeName)) {
				h.put((Object) opmodeName, new Integer(value));
				opModes.put(node, h);
			}
		}
	}

	private HashMap convertTimesToMap(double[][] d) {
		if (d != null) {
			HashMap h = new HashMap();
			HashMap temp = new HashMap();

			temp.put((Object) "ZonePlan", new Double(d[0][0]));
			temp.put((Object) "ProcessUpdateBDE", new Double(d[1][0]));
			h.put((Object) MessageAddress.getMessageAddress("BDE1"), (Object) temp.clone());
			temp.clear();

			temp.put((Object) "ReplanTime", new Double(d[2][0]));
			temp.put((Object) "ProcessUpdateBN", new Double(d[3][0]));
			temp.put((Object) "ProcessZonePlanBN", new Double(d[4][0]));
			h.put((Object) MessageAddress.getMessageAddress("BN1"), (Object) temp.clone());
			temp.clear();

			temp.put((Object) "ReplanTime", new Double(d[5][0]));
			temp.put((Object) "ProcessUpdateBN", new Double(d[6][0]));
			temp.put((Object) "ProcessZonePlanBN", new Double(d[7][0]));
			h.put((Object) MessageAddress.getMessageAddress("BN2"), (Object) temp.clone());
			temp.clear();

			temp.put((Object) "ReplanTime", new Double(d[8][0]));
			temp.put((Object) "ProcessUpdateBN", new Double(d[9][0]));
			temp.put((Object) "ProcessZonePlanBN", new Double(d[10][0]));
			h.put((Object) MessageAddress.getMessageAddress("BN3"), (Object) temp.clone());
			temp.clear();

			temp.put((Object) "ProcessPlan", new Double(d[11][0]));
			temp.put((Object) "StatusUpdateProcessTimer", new Double(d[12][0]));
			h.put((Object) MessageAddress.getMessageAddress("CPY1"), (Object) temp.clone());
			temp.clear();

			temp.put((Object) "ProcessPlan", new Double(d[13][0]));
			temp.put((Object) "StatusUpdateProcessTimer", new Double(d[14][0]));
			h.put((Object) MessageAddress.getMessageAddress("CPY2"), (Object) temp.clone());
			temp.clear();

			temp.put((Object) "ProcessPlan", new Double(d[15][0]));
			temp.put((Object) "StatusUpdateProcessTimer", new Double(d[16][0]));
			h.put((Object) MessageAddress.getMessageAddress("CPY3"), (Object) temp.clone());
			temp.clear();

			temp.put((Object) "ProcessPlan", new Double(d[17][0]));
			temp.put((Object) "StatusUpdateProcessTimer", new Double(d[18][0]));
			h.put((Object) MessageAddress.getMessageAddress("CPY4"), (Object) temp.clone());
			temp.clear();

			temp.put((Object) "ProcessPlan", new Double(d[19][0]));
			temp.put((Object) "StatusUpdateProcessTimer", new Double(d[20][0]));
			h.put((Object) MessageAddress.getMessageAddress("CPY5"), (Object) temp.clone());
			temp.clear();

			temp.put((Object) "ProcessPlan", new Double(d[21][0]));
			temp.put((Object) "StatusUpdateProcessTimer", new Double(d[22][0]));
			h.put((Object) MessageAddress.getMessageAddress("CPY6"), (Object) temp.clone());
			temp.clear();

			temp.put((Object) "ProcessPlan", new Double(d[23][0]));
			temp.put((Object) "StatusUpdateProcessTimer", new Double(d[24][0]));
			h.put((Object) MessageAddress.getMessageAddress("CPY7"), (Object) temp.clone());
			temp.clear();

			temp.put((Object) "ProcessPlan", new Double(d[25][0]));
			temp.put((Object) "StatusUpdateProcessTimer", new Double(d[26][0]));
			h.put((Object) MessageAddress.getMessageAddress("CPY8"), (Object) temp.clone());
			temp.clear();

			temp.put((Object) "ProcessPlan", new Double(d[27][0]));
			temp.put((Object) "StatusUpdateProcessTimer", new Double(d[28][0]));
			h.put((Object) MessageAddress.getMessageAddress("CPY9"), (Object) temp.clone());
			temp.clear();
			return h;
		}
		//returns null only if the array is null
		return null;
	}
	
	public long getTimeStamp(){
		return timeStamp;
	}

	protected double opTempo;
	protected long timeStamp;
	//HashMap of HashMaps: opModes will contain depth, breadth, timer values, score or freshness, even optempo for a particular node say BN1 or CPY5
	private HashMap opModes = null;
	private double[][] timeForTasks = null;
	private HashMap taskTimes = null;
}
