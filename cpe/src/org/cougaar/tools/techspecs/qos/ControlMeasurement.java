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
public class ControlMeasurement extends MeasurementImpl {

	public ControlMeasurement(String eventName, String actionName, MessageAddress source, long ts, HashMap opmodes, HashMap tft) {
		super(eventName, actionName, source);
		this.timeStamp = ts;
		this.opModes = opmodes;
		this.timeForTasks = tft;
	}

	public void toString(StringBuffer buf) {
		super.toString(buf);
		;
		buf.append(",OpModes= " + opModes.toString());
		buf.append(",timeForOpModes= " + opModes.toString());
		buf.append(",TimeStamp= " + timeStamp);
	}

	public Object getTimeForTask(MessageAddress node, String taskName) {
		if (timeForTasks.containsKey((Object) node)) {
			HashMap h = (HashMap) timeForTasks.get((Object) node);
			if (h.containsKey((Object) taskName)) {
				return ((Object) h.get((Object) taskName));
			}
		}
		return null;
	}

	public Object getOpModeValue(MessageAddress node, String opmodeName) {
		if (timeForTasks.containsKey((Object) node)) {
			HashMap h = (HashMap) timeForTasks.get((Object) node);
			if (h.containsKey((Object) opmodeName)) {
				return ((Object) h.get((Object) opmodeName));
			}
		}
		return null;
	}

	protected double opTempo;
	protected long timeStamp;
	//HashMap of HashMaps: opModes will contain depth, breadth, timer values, score or freshness, even optempo for a particular node say BN1 or CPY5
	private HashMap opModes = null;
	private HashMap timeForTasks = null;
}
