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

package org.cougaar.tools.predictor.plugin;

import org.cougaar.core.util.UID;
import java.util.HashMap;

public class PredictorHashMap implements java.io.Serializable {

	private UID uid;
	public static HashMap hashmap = new HashMap();

	/* Method to add hashmap containing each unique (customer and supply class) for a given supplier
	 * The hashmap contained in the static hashmap would be filled with items as keys and
	 * the values would be Array of end time and quantity
	 */
	public void addHashMap(String customer, String supply_class) {
		CustomerRoleKey crk = new CustomerRoleKey(customer, supply_class);
		if(!hashmap.containsKey(crk)) hashmap.put(crk, new HashMap());
	}

	public HashMap getMap(){
		return hashmap;
	}

	public int size(){
		return hashmap.size();
	}

	public int hashCode() {
		  return uid.hashCode();
		}

	public boolean equals(Object o) {
		if (o == this) return true;
		if  (!(o instanceof PredictorHashMap)) return false;
		return this.uid.equals(((PredictorHashMap) o).uid);
	}

	public void setUID(UID uid)	{
		this.uid = uid;
	}

	public UID getUID(){
		return uid;
	}
}
