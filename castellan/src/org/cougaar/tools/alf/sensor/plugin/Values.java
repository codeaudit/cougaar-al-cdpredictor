/*
  * <copyright>
  *  Copyright 2003 (Intelligent Automation, Inc.)
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
  */

package org.cougaar.tools.alf.sensor.plugin;

import org.cougaar.core.util.*;

public class Values implements java.io.Serializable {

	private long time;
	private long publishTime;
	private long commitmentTime;
	private double quantity;
	private UID uid;

	public Values(long time, double quantity){
		this.time = time;
		this.quantity = quantity;
	}

	public Values(long commitmentTime, long endTime, double quantity) {
		this.commitmentTime = commitmentTime;
		this.time = endTime;
		this.quantity = quantity;
	}

	public Values(long publishTime, long commitmentTime, long endTime, double quantity) {
		this.publishTime = publishTime;
		this.commitmentTime = commitmentTime;
		this.time = endTime;
		this.quantity = quantity;
	}

	public Values(long publishTime, long commitmentTime, long endTime, double quantity, UID uid) {
		this.publishTime = publishTime;
		this.commitmentTime = commitmentTime;
		this.time = endTime;
		this.quantity = quantity;
		this.uid = uid;
	}

	public long getEndTime(){
		return time;
	}

	public double getQuantity(){
		return quantity;
	}

	public long getPublishTime(){
		return publishTime;
	}

	public long getCommitmentTime(){
		return commitmentTime;
	}

	public UID getUID(){
		return uid;
	}

	public String toString(){
		String string = String.valueOf(publishTime)+" "+String.valueOf(commitmentTime)+" "+String.valueOf(time)
										+" "+String.valueOf(quantity);
		return string;
	}
}