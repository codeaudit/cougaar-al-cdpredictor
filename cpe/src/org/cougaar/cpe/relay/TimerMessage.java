/*
  * <copyright>
  *  Copyright 2003-2004 (Intelligent Automation, Inc.)
  *  under sponsorship of the Defense Advanced Research Projects
  *  Agency (DARPA).
  *
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the Cougaar Open Source License as published by
  *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
  *
  *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
  *  PROVIDED "AS IS" WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
  *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
  *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
  *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
  *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
  *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
  *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
  *  PERFORMANCE OF THE COUGAAR SOFTWARE.
  *
  * </copyright>
  *
  * CHANGE RECORD
  */
package org.cougaar.cpe.relay;

import org.cougaar.tools.techspecs.events.TimerEvent;

import java.io.Serializable;

/**
 * User: wpeng
 * Date: Mar 23, 2004
 * Time: 5:07:36 PM
 */
public class TimerMessage extends TimerEvent
{
    private QueuedAlarm queuedAlarm;

    protected TimerMessage(String alarmId, long time, QueuedAlarm queuedAlarm)
    {
        super(alarmId, time);
        this.queuedAlarm = queuedAlarm;
    }

    public QueuedAlarm getQueuedAlarm()
    {
        return queuedAlarm;
    }
}
