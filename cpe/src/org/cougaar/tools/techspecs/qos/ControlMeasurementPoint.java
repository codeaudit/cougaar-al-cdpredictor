package org.cougaar.tools.techspecs.qos;

import org.cougaar.core.mts.MessageAddress;
import java.util.HashMap;
import java.util.*;

/**
 * User: Nathan Gnanasambandam
 * Date: July 7, 2004
 * Time: 6:23:20 PM
 */
public class ControlMeasurementPoint extends MeasurementPoint {

	public ControlMeasurementPoint(String name) {
		super(name);
	}

	public void addMeasurement(String eventName, String action, MessageAddress source, HashMap opmodeMap, double[][] times) {
		//HashMap timeMap= convertTimesToMap(times);		
		ControlMeasurement cm = new ControlMeasurement(eventName, action, source, System.currentTimeMillis(), opmodeMap, times);		
		super.addMeasurement(cm);
	}

	
	public void toString(StringBuffer buf) {
		super.toString(buf);
		Iterator itr = this.getMeasurements(5);
		String s = "\n\n";
		if (itr != null) {
			while (itr.hasNext()) {
				ControlMeasurement cm = (ControlMeasurement) itr.next();
				s += cm.toString() + "\n";
			}
		}
		buf.append(s);
	}

}
