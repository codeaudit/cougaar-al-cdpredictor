/*
 * <copyright>
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the Cougaar Open Source License as published by
 *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
 *
 *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 *  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *  PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */

// org.cougaar.tools.alf.sensor.plugin.LFMessage

package org.cougaar.tools.alf.sensor.plugin;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.cougaar.util.log.Logging;

import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.relay.Relay;
import org.cougaar.core.util.UID;
import org.cougaar.logistics.plugin.manager.RelayAdapter;

/**
 * Relay used to notify logistics community of changes in load status
 **/
public class LFMessage extends RelayAdapter {

  public int num_of_tasks;
  public long time;

  private String myAgentName = null;
  private String myReportingSensorClassName = null;
//  private String myLoadStatus = null;
  private transient String myToString = null;

  public LFMessage(Object reportingSensor, String agentName, UID uid, 
                       int num_of_tasks1, long time1) {
    super();
    setReportingSensorClassName(reportingSensor.getClass());
    setAgentName(agentName);
    setUID(uid);
    setNum_of_tasks(num_of_tasks1);
    setTime(time1);
  }

  /**
   * Gets the name of the Agent whose load status is reported.
   *
   * @return String Name of the agent
   */
  public String getAgentName() {
    return myAgentName;
  }

  /**
   * Sets the name of the Agent whose load status is reported.
   *
   * @param agentName String name of the agent
   */
  public void setAgentName(String agentName) {
    if ((myAgentName != null) &&
        (!myAgentName.equals("")) &&
        (!myAgentName.equals(agentName))) {
      Logging.defaultLogger().warn("Attempt to reset agent name ignored.");
    } else {
      myAgentName = agentName;
      myToString = null;
    }
  }

  /**
   * Gets the class name of the sensor which reports the load status
   *
   * @return String Class name of the reporting sensor
   */
  public String getReportingSensorClassName() {
    return myReportingSensorClassName;
  }

  /**
   * Sets the class name of the sensor which reports the load status
   *
   * @param reportingSensorClassName String class name of the reporting sensor
   */
  public void setReportingSensorClassName(String reportingSensorClassName) {
    if ((myReportingSensorClassName != null) &&
        (!myReportingSensorClassName.equals("")) &&
        (!myReportingSensorClassName.equals(reportingSensorClassName))) {
      Logging.defaultLogger().warn("Attempt to reset reporting sensor class ignored.");
    } else {
      myReportingSensorClassName = reportingSensorClassName;
      myToString = null;
    }
  }


  /**
   * Sets the class name of the sensor which reports the load status
   *
   * @param reportingSensorClass Class of the reporting sensor
   */
  public void setReportingSensorClassName(Class reportingSensorClass) {
    setReportingSensorClassName(reportingSensorClass.toString());
  }

  public int getNum_of_tasks() {
    return num_of_tasks;
  }

  public long getTime() {
    return time;
  }
  
  public void setNum_of_tasks(int ntasks) {
    num_of_tasks = ntasks;
  }

  public void setTime(long t) {
    time=t;
  }

  public boolean equal(LFMessage newLFMessage)
  {
	  if (num_of_tasks == newLFMessage.getNum_of_tasks() && time == newLFMessage.getTime())
	  {
		  return true;
	  }

	return false;
  }

  protected boolean contentChanged(RelayAdapter newLFMessage) {
    LFMessage lfmessage = (LFMessage) newLFMessage;

    // Only the load status should actually change   
    if (!equal(lfmessage)) {
      setNum_of_tasks(lfmessage.getNum_of_tasks());
	  setTime(lfmessage.getTime());
      return true;
    } else {
      return (super.contentChanged(newLFMessage));
    }
  }

  public String toString() {
    if (myToString == null) {
      myToString = getClass() + ": agent=<" + getAgentName() + ">, sensor=<" +
        getReportingSensorClassName() + ">, blackboard state=<" + getTime() + ", "+ getNum_of_tasks() + 
        ">, UID=<" + getUID() + ">";
      myToString = myToString.intern();
    }

    return myToString;
  }
}