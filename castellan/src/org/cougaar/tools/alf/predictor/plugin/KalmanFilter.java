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

package org.cougaar.tools.alf.predictor.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.sql.Date;

public class KalmanFilter implements java.io.Serializable {

	private HashMap dataModelMap;
	public HashMap apriorEstimateMap = new HashMap(); 		//Key: Crk+itemName, value: ArrayList Values
	public HashMap aposteriorEstimateMap = new HashMap(); //Key: Crk+itemName, value: ArrayList Values

	public KalmanFilter(HashMap dataModelMap) {
		this.dataModelMap = dataModelMap;
	}

	public void timeUpdate() {
		for (Iterator iterator = aposteriorEstimateMap.keySet().iterator(); iterator.hasNext();) {
			CustomerRoleKey crk = (CustomerRoleKey) iterator.next();
			HashMap aposteriorInnerMap = (HashMap) aposteriorEstimateMap.get(crk);
			HashMap apriorInnerMap = new HashMap();
			for (Iterator iterator1 = aposteriorInnerMap.keySet().iterator(); iterator1.hasNext();) {
				String item_name = (String) iterator1.next();
				ArrayList apostEstList = (ArrayList) aposteriorInnerMap.get(item_name);
				ArrayList apriorList = new ArrayList();
				int aPostListSize = apostEstList.size();
				if (aPostListSize != 0) {
					double control_input = 0.0;
					KFValues apostValue = (KFValues) apostEstList.get(apostEstList.size() - 1);
					long endTime = apostValue.getEndTime();  //represents k
					double quantity = apostValue.getQuantity(); //represents z(k)
					ArrayList storedList = getElementDataList(crk, item_name);
					for (int j = 0; j < storedList.size(); j++) {
						Values storedValue = (Values) storedList.get(j);
						if (storedValue.getEndTime() == endTime) {
							double originalQuantity = storedValue.getQuantity();
							control_input = quantity - originalQuantity; //represents u(k)
							storedValue.setQuantity(quantity);
							break;
						}
					}
					double apriorEstimate = 0.0;
					for (int k = 0; k < storedList.size(); k++) {
						Values storedValue = (Values) storedList.get(k);
						if (storedValue.getEndTime() == (endTime + (4 * 86400000))) {
							double futQuantity = storedValue.getQuantity();
							apriorEstimate = futQuantity + control_input; //represents x(k+1)
							apriorList.add(new KFValues(storedValue.getEndTime(), apriorEstimate, -1, -1));
							storedValue.setQuantity(apriorEstimate);
							break;
						}
					}
				}
				apriorInnerMap.put(item_name, apriorList);
			}
			apriorEstimateMap.put(crk, apriorInnerMap);
		}
		aposteriorEstimateMap.clear();
	}

	public void measurementUpdate(ArrayList psal) {
		HashMap CRMap = (HashMap) psal.get(0); 		//There is only one hashmap in the arraylist
		for (Iterator iterator = CRMap.keySet().iterator(); iterator.hasNext();) {
			CustomerRoleKey crk = (CustomerRoleKey) iterator.next();
			HashMap valuesMap = (HashMap) CRMap.get(crk);
			HashMap aposteriorEstimateInnerMap = new HashMap();
			for (Iterator iter = valuesMap.keySet().iterator(); iter.hasNext();) {
				String itemName = (String) iter.next();
				ArrayList valuesList = (ArrayList) valuesMap.get(itemName);
				ArrayList aPostEstList = new ArrayList();
				for (Iterator iter1 = valuesList.iterator(); iter1.hasNext();) {
					Values value = (Values) iter1.next();
					long endTime = value.getEndTime();
					double quantity = value.getQuantity();
					if (!apriorEstimateMap.containsKey(crk)) {
						aPostEstList.add(new KFValues(endTime, quantity, -1, -1));
					} else {
						HashMap apriorEstimateInnerMap = (HashMap) apriorEstimateMap.get(crk);
						if (apriorEstimateInnerMap != null) {
							ArrayList apriorEstList = (ArrayList) apriorEstimateInnerMap.get(itemName);
							boolean endTimeFound = false;
							if (apriorEstList != null) {
								for (int i = 0; i < apriorEstList.size(); i++) {
									KFValues aPriorValue = (KFValues) apriorEstList.get(i);
									if (aPriorValue.getEndTime() == endTime) {
										double error = aPriorValue.getQuantity() - quantity;
										double aPostEstimate = aPriorValue.getQuantity() + 0.5 * error;
										aPostEstList.add(new KFValues(endTime, aPostEstimate, error, 0.5));
										endTimeFound = true;
										break;
									}
								}
							}
							if (!endTimeFound) {
								aPostEstList.add(new KFValues(endTime, quantity, -1, -1));
							}
						} else {
							aPostEstList.add(new KFValues(endTime, quantity, -1, -1));
						}
					}
				}
				aposteriorEstimateInnerMap.put(itemName, aPostEstList);
			}
			aposteriorEstimateMap.put(crk, aposteriorEstimateInnerMap);
		}
		apriorEstimateMap.clear();
		timeUpdate();
	}

	public double getApriorEstimate(CustomerRoleKey crk, String itemName, long endTime) {
		if (!apriorEstimateMap.isEmpty()) {
			ArrayList kfValuesList = (ArrayList) apriorEstimateMap.get(crk + "-" + itemName);
			for (Iterator iterator = kfValuesList.iterator(); iterator.hasNext();) {
				KFValues kfValues = (KFValues) iterator.next();
				if (kfValues.getEndTime() == endTime) {
					return kfValues.getQuantity();
				}
			}
			return -1;
		} else
			return -1;
	}

	public double getAposteriorEstimate(CustomerRoleKey crk, String itemName, long endTime) {
		if (!aposteriorEstimateMap.isEmpty()) {
			ArrayList kfValuesList = (ArrayList) aposteriorEstimateMap.get(crk + "-" + itemName);
			for (Iterator iterator = kfValuesList.iterator(); iterator.hasNext();) {
				KFValues kfValues = (KFValues) iterator.next();
				if (kfValues.getEndTime() == endTime) {
					return kfValues.getQuantity();
				}
			}
			return -1;
		} else
			return -1;
	}

	public ArrayList getElementDataList(CustomerRoleKey crk, String item_name) {
		HashMap valuesMap = (HashMap) dataModelMap.get(crk);
		if (valuesMap != null)
			return (ArrayList) valuesMap.get(item_name);
		else
			return null;
	}

	public class KFValues implements java.io.Serializable {
		private long endTime;
		private double quantity;
		private double error;
		private double gain;

		public KFValues(long endTime, double quantity, double error, double gain) {
			this.endTime = endTime;
			this.quantity = quantity;
			this.error = error;
			this.gain = gain;
		}

		public long getEndTime() {
			return endTime;
		}

		public double getQuantity() {
			return quantity;
		}

		public double getError() {
			return error;
		}

		public double getGain() {
			return gain;
		}

	}
}
