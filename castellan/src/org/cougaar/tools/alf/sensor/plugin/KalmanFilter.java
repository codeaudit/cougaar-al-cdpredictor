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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class KalmanFilter implements java.io.Serializable {

	private HashMap dataModelMap;
	public HashMap apriorEstimateMap = new HashMap(); 		//Key: Crk+itemName, value: ArrayList Values
	public HashMap aposteriorEstimateMap = new HashMap(); //Key: Crk+itemName, value: ArrayList Values

  public KalmanFilter(HashMap dataModelMap) {
  	this.dataModelMap = dataModelMap;
  }

	public void timeUpdate(){
			for(Iterator iterator = aposteriorEstimateMap.keySet().iterator();iterator.hasNext();){
				String key = (String)iterator.next();
				ArrayList postEstValuesList = (ArrayList)aposteriorEstimateMap.get(key);
				ArrayList apriorEstValuesList = new ArrayList();
				for(Iterator iterator1 = postEstValuesList.iterator();iterator1.hasNext();) {
					KFValues postEstKFValues = (KFValues)iterator1.next();
					long endTime = postEstKFValues.getEndTime();
					double quantity = postEstKFValues.getQuantity();
					apriorEstValuesList.add(new KFValues(endTime+4*86400000, quantity, -1, -1));
				}
				apriorEstimateMap.put(key, apriorEstValuesList);
			}
	}

	public void measurementUpdate(PredictorSupplyArrayList psal) {
	HashMap CRMap = (HashMap)psal.get(0); 		//There is only one hashmap in the arraylist
	for(Iterator iterator = CRMap.keySet().iterator();iterator.hasNext();) {
		CustomerRoleKey crk = (CustomerRoleKey)iterator.next();
		HashMap valuesMap = (HashMap)CRMap.get(crk);
		for(Iterator iter = valuesMap.keySet().iterator();iter.hasNext();){
			String itemName = (String)iter.next();
			ArrayList valuesList = (ArrayList)valuesMap.get(itemName);
			ArrayList aPostEstList = new ArrayList();
			for(Iterator iter1 = valuesList.iterator();iter1.hasNext();) {
				Values value = (Values)iter1.next();
				long endTime = value.getEndTime();
				double quantity = value.getQuantity();
				if(apriorEstimateMap.isEmpty()) {
					ArrayList storedList = getElementDataList(crk, itemName);
					for(int i= 0; i < storedList.size();i++) {
						Values storedValue = (Values)storedList.get(i);
						if(storedValue.getEndTime() == endTime) {
							storedList.remove(storedValue);
							storedList.add(new Values(endTime, quantity));
							aPostEstList.add(new KFValues(endTime, quantity, -1, -1));
							break;
						}
					}
				} else {
					ArrayList storedList = getElementDataList(crk, itemName);
					for(int i=0; i < storedList.size();i++){
						Values storedValue = (Values)storedList.get(i);
						if(storedValue.getEndTime() == endTime) {
							storedList.remove(storedValue);
							storedList.add(new Values(endTime, quantity));
							break;
						}
					}
					ArrayList aPriorEstList = (ArrayList)apriorEstimateMap.get(crk+itemName);
					for(int i=0; i < aPriorEstList.size();i++){
						KFValues aPriorValue = (KFValues)aPriorEstList.get(i);
						if(aPriorValue.getEndTime()==endTime){
							double error = aPriorValue.getQuantity() - quantity;
							double aPostEstimate = aPriorValue.getQuantity() + 0.5 * error;
							aPostEstList.add(new KFValues(endTime, aPostEstimate, error, 0.5));
						}
					}
					//Check if the endTime matches the apriorestimate end time
					//if yes then compute aposteriorestimate
				}
			}
			aposteriorEstimateMap.put(crk+itemName,aPostEstList);
		}
	}
		apriorEstimateMap.clear();
		timeUpdate();
}

	public double getApriorEstimate(CustomerRoleKey crk, String itemName, long endTime){
		if(!apriorEstimateMap.isEmpty()){
			ArrayList kfValuesList = (ArrayList)apriorEstimateMap.get(crk+itemName);
			for(Iterator iterator = kfValuesList.iterator();iterator.hasNext();){
				KFValues kfValues = (KFValues)iterator.next();
				if(kfValues.getEndTime()== endTime) {
					return kfValues.getQuantity();
				}
			}
			return -1;
		}
		else return -1;
	}

	public double getAposteriorEstimate(CustomerRoleKey crk, String itemName, long endTime){
		if(!aposteriorEstimateMap.isEmpty()){
			ArrayList kfValuesList = (ArrayList)aposteriorEstimateMap.get(crk+itemName);
			for(Iterator iterator = kfValuesList.iterator();iterator.hasNext();){
				KFValues kfValues = (KFValues)iterator.next();
				if(kfValues.getEndTime()== endTime) {
					return kfValues.getQuantity();
				}
			}
			return -1;
		}
		else return -1;
	}

	public ArrayList getElementDataList(CustomerRoleKey crk, String item_name){
		HashMap valuesMap = (HashMap)dataModelMap.get(crk);
		if(valuesMap!= null)
		return (ArrayList)valuesMap.get(item_name);
		else return null;
	}

	public class KFValues implements java.io.Serializable {
		private long endTime;
		private double quantity;
		private double error;
		private double gain;

		public KFValues(long endTime, double quantity, double error, double gain){
			this.endTime = endTime;
			this.quantity = quantity;
			this.error = error;
			this.gain = gain;
		}

		public long getEndTime(){
			return endTime;
		}

		public double getQuantity(){
			return quantity;
		}

		public double getError(){
			return error;
		}

		public double getGain(){
			return gain;
		}

	}
}
