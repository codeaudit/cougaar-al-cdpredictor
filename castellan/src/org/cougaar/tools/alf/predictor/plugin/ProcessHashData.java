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

import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.AspectType;
import org.cougaar.planning.ldm.plan.AspectRate;
import org.cougaar.planning.ldm.measure.FlowRate;
import org.cougaar.planning.ldm.measure.CountRate;
import org.cougaar.glm.ldm.plan.AlpineAspectType;

import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.util.*;


public class ProcessHashData {

	private HashMap hashmap;
	private String cluster;
	private ArrayList taskList;

	public ProcessHashData(String cluster, HashMap map, ArrayList tasks){
		this.cluster = cluster;
		this.hashmap = map;
		this.taskList = tasks;
	}

	public void processTasks(){
		for(Iterator iterator = taskList.iterator();iterator.hasNext();) {
			Task task = (Task)iterator.next();
			String owner = task.getPrepositionalPhrase("For").getIndirectObject().toString();
			String comp = (String) task.getPrepositionalPhrase("OfType").getIndirectObject();
			if (comp!= null) {
				Asset as = task.getDirectObject();
				String item_name = as.getTypeIdentificationPG().getTypeIdentification();
				CustomerRoleKey crk = new CustomerRoleKey(owner, comp);
				HashMap inner_hashmap = (HashMap) hashmap.get(crk);
				ArrayList valuesList = (ArrayList)inner_hashmap.get(item_name);
				if(task.getVerb().equals("ProjectSupply")) {
					long sTime = (long) task.getPreferredValue(AspectType.START_TIME);
					long zTime = (long) task.getPreferredValue(AspectType.END_TIME);

					for (long incTime = sTime; incTime <= zTime; incTime = incTime + 86400000) {
						double rate = 0.0;
						if (comp.compareToIgnoreCase("BulkPOL") == 0) {
							AspectRate aspectrate = (AspectRate) task.getPreference(AlpineAspectType.DEMANDRATE).getScoringFunction().getBest().getAspectValue();
							FlowRate flowrate = (FlowRate) aspectrate.rateValue();
							rate = (flowrate.getGallonsPerDay());
						}
						else {
							AspectRate aspectrate = (AspectRate) task.getPreference(AlpineAspectType.DEMANDRATE).getScoringFunction().getBest().getAspectValue();
							CountRate flowrate = (CountRate) aspectrate.rateValue();
							rate = (flowrate.getUnitsPerDay());
						}
							if(valuesList!= null) {
								boolean foundEndTime = false;
								for(Iterator it = valuesList.iterator();it.hasNext();){
									Values value = (Values)it.next();
									if(value.getEndTime()== incTime) {
										double newQuantity = (value.getQuantity() + rate)/2;
										value.setQuantity(newQuantity);
										foundEndTime = true;
									}
								}
								if(!foundEndTime) {
									Values new_value = new Values(incTime, rate*4, as);
									valuesList.add(new_value);
								}
							}
							else
							{
								Values new_value = new Values(incTime, rate*4, as);
								valuesList = new ArrayList();
								valuesList.add(new_value);
								inner_hashmap.put(item_name, valuesList);
							}
						}
					}
					else {
						long endTime = (long) (task.getPreferredValue(AspectType.END_TIME));
						double quantity = task.getPreferredValue(AspectType.QUANTITY);
							if(valuesList!= null) {
								boolean foundEndTime = false;
								for(Iterator it = valuesList.iterator();it.hasNext();){
									Values value = (Values)it.next();
									if(value.getEndTime()== endTime) {
										double newQuantity = (value.getQuantity() + quantity)/2;
										value.setQuantity(newQuantity);
										foundEndTime = true;
									}
								}
								if(!foundEndTime) {
									Values newvalue = new Values(endTime, quantity, as);
									valuesList.add(newvalue);
								}
							}
							else
							{
								Values new_value = new Values(endTime, quantity, as);
								valuesList = new ArrayList();
								valuesList.add(new_value);
								inner_hashmap.put(item_name, valuesList);
							}
						}
				}
		}
	}

	public HashMap iterateList() {
		processTasks();
		for(Iterator iterator = hashmap.keySet().iterator();iterator.hasNext();){
			CustomerRoleKey crk = (CustomerRoleKey)iterator.next();
			HashMap inner_hashmap = (HashMap)hashmap.get(crk);
			for(Iterator iter = inner_hashmap.keySet().iterator();iter.hasNext();){
				String item_name = (String)iter.next();
				ArrayList valuesList = (ArrayList)inner_hashmap.get(item_name);
				demandPerDay(valuesList);
				//String formattedName = formatItemName(item_name, crk.getCustomerName(), crk.getRoleName());
				//printList(formattedName, valuesList);
			}
		}
		return hashmap;
	}

  public void endTimeSort(Collection values) {
		ArrayList result;
		if(values instanceof ArrayList) result = (ArrayList)values;
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

	private void demandPerDay(ArrayList timeQtyValues) {
			Asset asset = null;
			endTimeSort(timeQtyValues);
			double[][] timeQtyArray = new double[timeQtyValues.size()][2];
			int k = 0;
			for(Iterator iterator2 = timeQtyValues.iterator();iterator2.hasNext();) {
				Values values = (Values)iterator2.next();
				long endtime = values.getEndTime();
				double quantity = values.getQuantity();
				asset = values.getAsset();
				timeQtyArray[k][0] = endtime;
				timeQtyArray[k][1] = quantity;
				k++;
			}
			timeQtyValues.clear();
			double sum_var = 0;
			int x = 0;
      int i = 0;
			if(timeQtyArray.length == 1){
				Values newValues = new Values((long)timeQtyArray[i][0], timeQtyArray[i][1], asset);
				timeQtyValues.add(newValues);
				return;
			}
      for (int j = i + 1; j < timeQtyArray.length; j++) {
      	if (timeQtyArray[j][0] > timeQtyArray[i][0]) {
        	for (x = j - 1; x >= i; x--) {
          	double var = timeQtyArray[x][1];
            sum_var = sum_var + var;
            if (x == 0) {
            	break;
            }
          }
					Values newValues = new Values((long)timeQtyArray[i][0], sum_var, asset);
					timeQtyValues.add(newValues);
          sum_var = 0;
	        i = j;
        } else continue;
    	}
	}


	public void printList(String name, ArrayList list){
		PrintWriter pwr;
		String maindir = System.getProperty("org.cougaar.workspace");
		String dir = maindir+"/log4jlogs/"+name+".txt";
 		try {
				if(dir!= null)	pwr = new PrintWriter(new BufferedWriter(new java.io.FileWriter(dir, false)));
				else	pwr = new PrintWriter(new BufferedWriter(new java.io.FileWriter(name + ".txt", false)));
			 for (Iterator iterator = list.iterator();iterator.hasNext();) {
						Values valObject = (Values)iterator.next();
						pwr.print(new Date(valObject.getEndTime()));
						pwr.print(",");
						pwr.print(valObject.getQuantity());
						pwr.println();
				}
				pwr.close();
		} catch (java.io.FileNotFoundException fnfe) {
				System.err.println(fnfe);
		} catch(java.io.IOException ioe){
			 System.err.println(ioe);
		 }
	}

	public String formatItemName(String name, String customer, String role){

		StringTokenizer st = new StringTokenizer(name);
	  StringBuffer tempString = new StringBuffer();
		while(st.hasMoreTokens()) {
			String token = st.nextToken();
			if(token.equals("/") || token.equals(",") || token.equals("-") || token.equals(".") || token.equals(":")) continue;
			else tempString.append(token);
		}
		StringBuffer finalString = new StringBuffer();
		char[] char_item = tempString.toString().toCharArray();
    for(int i =0; i < char_item.length; i++) {
				char temp = char_item[i];
				String char_temp = new Character(temp).toString();
				if(char_temp.equalsIgnoreCase("/") == true || char_temp.equalsIgnoreCase(".") ==true  || char_temp.equalsIgnoreCase("\"")== true
				|| char_temp.equalsIgnoreCase(",")== true || char_temp.equalsIgnoreCase("-")== true
				|| char_temp.equalsIgnoreCase(":")== true){
							continue;
				} else
				{
						finalString.append(char_temp);
				}
		}
		String outString = customer+role+finalString.toString();
    return outString;
	}

}