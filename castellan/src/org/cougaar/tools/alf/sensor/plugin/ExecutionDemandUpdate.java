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

import java.util.*;

public class ExecutionDemandUpdate {

	  HashMap CRMap = new HashMap();
		HashMap lastDateMap = new HashMap();

    public ExecutionDemandUpdate() {
    }

	public void addDemandData(String customer, String supplyclass, String item, Values values) {
		CustomerRoleKey crk = new CustomerRoleKey(customer, supplyclass);
		if(!CRMap.containsKey(crk)) {
			CRMap.put(crk, new HashMap());
			HashMap innerItemMap = (HashMap)CRMap.get(crk);
			ArrayList valuesList = new ArrayList();
			valuesList.add(values);
			innerItemMap.put(item, valuesList);
		}
		else {
			HashMap innerItemMap = (HashMap)CRMap.get(crk);
			ArrayList valuesList = (ArrayList)innerItemMap.get(item);
			if(valuesList!= null) valuesList.add(values);
			else { valuesList = new ArrayList();
			valuesList.add(values);
			innerItemMap.put(item, valuesList);}
		}
	}

	public void TimeSort(Collection values) {
		ArrayList result;
		if(values instanceof ArrayList)	result = (ArrayList)values;
		else result = new ArrayList(values);
    Collections.sort(result, new Comparator () {
      public int compare (Object a, Object b) {
          Values v1 = (Values)a;
          Values v2 = (Values)b;
					long time1 = v1.getEndTime();
				  long time2 = v2.getEndTime();
        if (time1 < time2) return -1;
        if (time1 > time2) return +1;
        return 0;
      }});
  }

  public PredictorSupplyArrayList getExecutionDemandPerDay(){
		PredictorSupplyArrayList psal = new PredictorSupplyArrayList();
		if(!CRMap.isEmpty()) {
			for(Iterator iterator = CRMap.keySet().iterator();iterator.hasNext();){
				CustomerRoleKey crk = (CustomerRoleKey)iterator.next();
				HashMap itemMap = (HashMap)CRMap.get(crk);
				for(Iterator iter = itemMap.keySet().iterator();iter.hasNext();){
					String itemName = (String)iter.next();
					ArrayList valueList = (ArrayList)itemMap.get(itemName);
					if((valueList!= null) && (!valueList.isEmpty()) && (valueList.size() >= 1)) {
						TimeSort(valueList);
						computePerDayDemand(valueList);
						if(valueList.size()>0) {Values value = (Values)valueList.get(valueList.size()-1);
							setLastDemandValueForItem(crk, itemName, value);
						}
					}
				}
			}
			retainAllItems();
		}
		psal.add(CRMap);
		return psal;
	}

	private void computePerDayDemand(ArrayList values){
		if(values!= null){
			ArrayList newValuesList = new ArrayList();
			long temptime = -1;
			long tempPublishTime = -1;
			long tempCommitmentTime = -1;
			double tempQuantity = 0.0;
			for(Iterator iterator = values.iterator();iterator.hasNext();) {
				Values valueObject = (Values)iterator.next();
				long endtime = valueObject.getEndTime();
				long publishTime = valueObject.getPublishTime();
				long commitmentTime = valueObject.getCommitmentTime();
				double quantity = valueObject.getQuantity();
				if(endtime!= temptime) {
					if(temptime!= -1) {
						Values newValue = new Values(tempPublishTime, tempCommitmentTime, temptime, tempQuantity);
						newValuesList.add(newValue);
						temptime = endtime;
						tempQuantity = quantity;
						tempPublishTime = publishTime;
						tempCommitmentTime = commitmentTime;
					} else {
						temptime = endtime;
						tempQuantity = tempQuantity + quantity;
						tempPublishTime = publishTime;
						tempCommitmentTime = commitmentTime;
						continue;
					}
				} else {
					temptime = endtime;
					tempQuantity = tempQuantity + quantity;
					tempPublishTime = publishTime;
					tempCommitmentTime = commitmentTime;
				}
			}
			values.clear();
			values.addAll(newValuesList);
		}
	}

	private void setLastDemandValueForItem(CustomerRoleKey crk, String item, Values value){
		if(!lastDateMap.containsKey(crk)) {
			lastDateMap.put(crk,new HashMap());
			HashMap innerMap = (HashMap)lastDateMap.get(crk);
			innerMap.put(item, value);
		} else {
			HashMap innerMap = (HashMap)lastDateMap.get(crk);
			Values valueObject = (Values)innerMap.get(item);
			if(valueObject == null) System.out.println("ValueObject is null for item: "+item);
			if(valueObject!= null){
				if(valueObject.getEndTime() < value.getEndTime()){
					innerMap.remove(item);
					innerMap.put(item, value);
				}
			} else {
				innerMap.put(item, value);
			}
		}
	}

	public long getLastDemandDateForItem(String customer, String supplyclass, String itemName){
		CustomerRoleKey crk = new CustomerRoleKey(customer, supplyclass);
		if(!lastDateMap.containsKey(crk)) return -1;
		else {
			HashMap innerMap = (HashMap)lastDateMap.get(crk);
			Values valueObject = (Values)innerMap.get(itemName);
			return valueObject.getEndTime();
		}
	}


	public double getLastDemandQuantityForItem(String customer, String supplyclass, String itemName){
		CustomerRoleKey crk = new CustomerRoleKey(customer, supplyclass);
		if(!lastDateMap.containsKey(crk)) return -1;
		else {
			HashMap innerMap = (HashMap)lastDateMap.get(crk);
			Values valueObject = (Values)innerMap.get(itemName);
			return valueObject.getQuantity();
		}
	}

	/* This method allows to retain items that do not appear on a given day but still should
		 have a prediction associated
	*/
	private void retainAllItems(){
		for(Iterator iterator = lastDateMap.keySet().iterator();iterator.hasNext();){
			CustomerRoleKey crk = (CustomerRoleKey)iterator.next();
			if(CRMap.containsKey(crk)){
				HashMap itemMap = (HashMap)lastDateMap.get(crk);
				for(Iterator iter = itemMap.keySet().iterator();iter.hasNext();){
					String itemName = (String)iter.next();
					HashMap crItemMap = (HashMap)CRMap.get(crk);
					if(!crItemMap.containsKey(itemName)) {
						ArrayList tempList = new ArrayList();
						Values value = (Values)itemMap.get(itemName);
						tempList.add(value);
						crItemMap.put(itemName, tempList);
					}
				}
			} else continue;
		}
	}

	/* This method should be called after the PredictorPlugin recieves the DemandData for that day
	   and the demand Data Map is then cleared after each day of execution
	*/
	public void clearDemandDataMap(){
		CRMap.clear();
	}
}