package org.cougaar.tools.techspecs.qos;

import org.cougaar.core.mts.MessageAddress;
import java.util.HashMap;

/**
 * User: Nathan Gnanasambandam
 * Date: July 7, 2004
 * Time: 6:23:20 PM
 */
public class ControlMeasurementPoint extends MeasurementPoint {

	public ControlMeasurementPoint(String name) {
		super(name);
	}

	public void addMeasurement( String eventName, String action, MessageAddress source,  HashMap opmodeMap, double[][] times) {
		HashMap timeMap= convertTimesToMap(times);		
		ControlMeasurement cm = new ControlMeasurement(eventName, action, source,System.currentTimeMillis(),opmodeMap,timeMap) ;
		super.addMeasurement( cm );
	}
	
	//TODO finsih this function
	private HashMap convertTimesToMap(double[][] d){
		return null;
	}

}
