/*
 *  This class is extended from SensorCondition class for PSUFBSensor1Plugin
 */


package org.cougaar.tools.alf.sensor.plugin;

import org.cougaar.core.adaptivity.SensorCondition;
import org.cougaar.core.adaptivity.OMCRangeList;

public class PSUFBSensorCondition extends SensorCondition
{  
  public PSUFBSensorCondition(String name, OMCRangeList allowedValues) {
    super(name, allowedValues, allowedValues.getEffectiveValue());
  }
  public PSUFBSensorCondition(String name, OMCRangeList allowedValues, Comparable value) {
    super(name, allowedValues, value);
  }   
  public void setValue(Comparable newValue) {
    super.setValue(newValue);
  }
}
