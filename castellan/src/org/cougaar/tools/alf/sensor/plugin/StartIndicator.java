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

public class StartIndicator extends RelayAdapter {

  private String myAgentName = null;
  private long StartTime;
  private int ClassNumber;

  public StartIndicator(String agentName, UID uid, long StartTime, int classnumber ) {
    super();
    setAgentName(agentName);
    setUID(uid);
    setStartTime(StartTime);
	ClassNumber = classnumber;
  }

  /**
   * Gets the name of the Agent whose load status is reported.
   *
   * @return String Name of the agent
   */
  public String getAgentName() {
    return myAgentName;
  }

  public long getStartTime() {
    return StartTime;
  }

  public long getClassNumber() {
    return ClassNumber;
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
    }
  }

  public void setStartTime(long StartTime1) {
		StartTime = StartTime1;
  }
}