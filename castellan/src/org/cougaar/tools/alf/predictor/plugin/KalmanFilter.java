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
		System.out.println("aposteriorEstimateMap size "+aposteriorEstimateMap.size());
		for(Iterator iterator = aposteriorEstimateMap.keySet().iterator();iterator.hasNext();) {
			CustomerRoleKey crk = (CustomerRoleKey)iterator.next();
			HashMap aposteriorInnerMap = (HashMap)aposteriorEstimateMap.get(crk);
			System.out.println("aposteriorInnerMap size "+aposteriorInnerMap.size());
			HashMap apriorInnerMap = new HashMap();
			for(Iterator iterator1 = aposteriorInnerMap.keySet().iterator();iterator1.hasNext();) {
				String item_name = (String)iterator1.next();
				if(item_name.equalsIgnoreCase("JP8")) System.out.println("timeUpdate JP8");
				ArrayList apostEstList = (ArrayList)aposteriorInnerMap.get(item_name);
				if(item_name.equalsIgnoreCase("JP8")) System.out.println("apostEstListJP8 size "+apostEstList.size());
				ArrayList apriorList = new ArrayList();
				int aPostListSize = apostEstList.size();
				if(aPostListSize!= 0) {
					double control_input = 0.0;
					KFValues apostValue = (KFValues)apostEstList.get(apostEstList.size()-1);
					if(item_name.equalsIgnoreCase("JP8")) System.out.println("apostValueJP8 "+apostValue.getEndTime()+
									" "+apostValue.getQuantity());
					long endTime = apostValue.getEndTime();  //represents k
					double quantity = apostValue.getQuantity(); //represents z(k)
					ArrayList storedList = getElementDataList(crk, item_name);
					if(item_name.equalsIgnoreCase("JP8")) System.out.println("storedListJP8 size "+storedList.size());
					for(int j= 0; j < storedList.size();j++) {
						Values storedValue = (Values)storedList.get(j);
						if(item_name.equalsIgnoreCase("JP8")) System.out.println("storedValueJP8 End time "+new Date(storedValue.getEndTime())+
										" Required End Time "+new Date(endTime));
						if(storedValue.getEndTime() == endTime) {
							if(item_name.equalsIgnoreCase("JP8")) System.out.println("TimeUpdate Entered storeList1");
							double originalQuantity = storedValue.getQuantity();
							control_input = quantity - originalQuantity; //represents u(k)
							if(item_name.equalsIgnoreCase("JP8")) System.out.println("control_inputJP8 "+control_input);
							//storedList.remove(storedValue);
							//storedList.add(new Values(endTime, quantity));
							storedValue.setQuantity(quantity);
							break;
						}
					}
					double apriorEstimate = 0.0;
					for(int k= 0; k < storedList.size();k++) {
						Values storedValue = (Values)storedList.get(k);
						if(storedValue.getEndTime() == (endTime+(4*86400000))) {
							if(item_name.equalsIgnoreCase("JP8")) System.out.println("TimeUpdate Entered storeList2");
							double futQuantity = storedValue.getQuantity();
							apriorEstimate = futQuantity + control_input; //represents x(k+1)
							apriorList.add(new KFValues(storedValue.getEndTime(), apriorEstimate,-1,-1));
							if(item_name.equalsIgnoreCase("JP8")) System.out.println("futQuantityJP8 "+futQuantity);
							storedValue.setQuantity(apriorEstimate);
							if(item_name.equalsIgnoreCase("JP8")) {
								System.out.println("KFApriorEst: "+" Item "+item_name+" "
								+new java.util.Date(storedValue.getEndTime())+" "+apriorEstimate);
							}
							break;
						}
					}
			/*	for(int i=0; i < apostEstList.size();i++) {
					double control_input = 0.0;
					KFValues apostValue = (KFValues)apostEstList.get(i);
					if(item_name.equalsIgnoreCase("JP8")) System.out.println("apostValueJP8 "+apostValue.getEndTime()+
									" "+apostValue.getQuantity());
					long endTime = apostValue.getEndTime();  //represents k
					double quantity = apostValue.getQuantity(); //represents z(k)
					ArrayList storedList = getElementDataList(crk, item_name);
					for(int j= 0; j < storedList.size();j++) {
						Values storedValue = (Values)storedList.get(j);
						if(storedValue.getEndTime() == endTime) {
							System.out.println("TimeUpdateStoredValue1 Entered");
							double originalQuantity = storedValue.getQuantity();
							control_input = quantity - originalQuantity; //represents u(k)
							if(item_name.equalsIgnoreCase("JP8")) System.out.println("control_inputJP8 "+control_input);
							//storedList.remove(storedValue);
							//storedList.add(new Values(endTime, quantity));
							storedValue.setQuantity(quantity);
							break;
						}
					}
					double apriorEstimate = 0.0;
					for(int k= 0; k < storedList.size();k++) {
						Values storedValue = (Values)storedList.get(k);
						if(storedValue.getEndTime() == (endTime+(4*86400000))) {
							System.out.println("TimeUpdateStoredValue2 Entered");
							double futQuantity = storedValue.getQuantity();
							if(item_name.equalsIgnoreCase("JP8")) System.out.println("futQuantityJP8 "+futQuantity);
							apriorEstimate = futQuantity + control_input; //represents x(k+1)
							apriorList.add(new KFValues(storedValue.getEndTime(), apriorEstimate,-1,-1));
							if(item_name.endsWith("JP8")) {
								System.out.println("KFApriorEst: "+" Item "+item_name+" "
								+new java.util.Date(storedValue.getEndTime())+" "+apriorEstimate);
							}
							break;
						}
					}
				}*/
				}
				apriorInnerMap.put(item_name, apriorList);
			}
			apriorEstimateMap.put(crk, apriorInnerMap);
		}
		aposteriorEstimateMap.clear();
	}

	public void measurementUpdate(PredictorSupplyArrayList psal) {
	HashMap CRMap = (HashMap)psal.get(0); 		//There is only one hashmap in the arraylist
	System.out.println("CRMapsize "+CRMap.size());
	for(Iterator iterator = CRMap.keySet().iterator();iterator.hasNext();) {
		CustomerRoleKey crk = (CustomerRoleKey)iterator.next();
		HashMap valuesMap = (HashMap)CRMap.get(crk);
		System.out.println("valuesMapSize "+valuesMap.size());
		HashMap aposteriorEstimateInnerMap = new HashMap();
		for(Iterator iter = valuesMap.keySet().iterator();iter.hasNext();) {
			String itemName = (String)iter.next();
			if(itemName.equalsIgnoreCase("JP8")) System.out.println("JP8Item In");
			ArrayList valuesList = (ArrayList)valuesMap.get(itemName);
			if(itemName.equalsIgnoreCase("JP8")) System.out.println("valuesListSize "+valuesList.size());
			ArrayList aPostEstList = new ArrayList();
			for(Iterator iter1 = valuesList.iterator();iter1.hasNext();) {
				Values value = (Values)iter1.next();
				long endTime = value.getEndTime();
				double quantity = value.getQuantity();
				if(itemName.equalsIgnoreCase("JP8")) System.out.println("endTimeJP8 "+endTime+" QuantityJP8 "+quantity);
				if(!apriorEstimateMap.containsKey(crk)) {
					aPostEstList.add(new KFValues(endTime, quantity, -1, -1));
					if(itemName.equalsIgnoreCase("JP8")) {
						System.out.println("KFApostEst: "+" Item "+itemName+" "
						+new java.util.Date(endTime)+" "+quantity+" 0 Error");
					}
				} else {
					HashMap apriorEstimateInnerMap = (HashMap)apriorEstimateMap.get(crk);
					if(apriorEstimateInnerMap!= null) {
						ArrayList apriorEstList = (ArrayList)apriorEstimateInnerMap.get(itemName);
						boolean endTimeFound = false;
						for(int i=0; i < apriorEstList.size();i++) {
							KFValues aPriorValue = (KFValues)apriorEstList.get(i);
							if(aPriorValue.getEndTime()==endTime) {
								double error = aPriorValue.getQuantity() - quantity;
								double aPostEstimate = aPriorValue.getQuantity() + 0.5 * error;
								aPostEstList.add(new KFValues(endTime, aPostEstimate, error, 0.5));
								if(itemName.equalsIgnoreCase("JP8")){
									System.out.println("KFApostEst: "+" Item JP8 "+" "
											+new java.util.Date(endTime)+" "+quantity+" "+error);
								}
								endTimeFound = true;
								break;
							}
						}
						if(!endTimeFound) {
							aPostEstList.add(new KFValues(endTime, quantity, -1, -1));
							if(itemName.equalsIgnoreCase("JP8")){
									System.out.println("KFApostEst: "+" Item JP8 "+" "
											+new java.util.Date(endTime)+" "+quantity+" No end time found");
							}
						}
					} else {
						aPostEstList.add(new KFValues(endTime, quantity, -1, -1));
						if(itemName.equalsIgnoreCase("JP8")) {
							System.out.println("KFApostEst: "+" Item "+itemName+" "
							+new java.util.Date(endTime)+" "+quantity+" 0 Error");
						}
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

	public double getApriorEstimate(CustomerRoleKey crk, String itemName, long endTime){
		if(!apriorEstimateMap.isEmpty()){
			ArrayList kfValuesList = (ArrayList)apriorEstimateMap.get(crk+"-"+itemName);
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
			ArrayList kfValuesList = (ArrayList)aposteriorEstimateMap.get(crk+"-"+itemName);
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
