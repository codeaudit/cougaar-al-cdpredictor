/*
 *  This plugin is a falling behind sensor based on the imbalance 
 *  between the performances of task arrival and task allocation.
 *  This sensor deals with ProjectSupply/Supply tasks and allocation to inventory assets.
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
