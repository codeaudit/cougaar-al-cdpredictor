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

import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.util.*;


public class ProcessHashData {

	private HashMap hashmap;
	private String cluster;

	public ProcessHashData(String cluster, HashMap map){
		this.cluster = cluster;
		this.hashmap = map;
	}

	public HashMap iterateList() {
		for(Iterator iterator = hashmap.values().iterator();iterator.hasNext();){
			HashMap inner_hashmap = (HashMap)iterator.next();
			for(Iterator iter = inner_hashmap.keySet().iterator();iter.hasNext();){
				String item_name = (String)iter.next();
				ArrayList valuesList = (ArrayList)inner_hashmap.get(item_name);
				demandPerDay(valuesList);
				String formattedName = formatItemName(item_name);
				printList(formattedName, valuesList);
			}
		}
		return hashmap;
	}

  public void endTimeSort(Collection values) {
    //long start = System.currentTimeMillis();
    //System.out.println(" SORT start of for loop in sort function " +  new Date(start));

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

    //long end = System.currentTimeMillis();
    //System.out.println("SORT End of for loop in sort function " +  new Date(end)
                       //+ " total time to sort in milliseconds" +
                       //(end - start));
  }

	private void demandPerDay(ArrayList timeQtyValues) {

			endTimeSort(timeQtyValues);
			double[][] timeQtyArray = new double[timeQtyValues.size()][2];
			int k = 0;
			for(Iterator iterator2 = timeQtyValues.iterator();iterator2.hasNext();) {
				Values values = (Values)iterator2.next();
				long endtime = values.getEndTime();
				double quantity = values.getQuantity();
				timeQtyArray[k][0] = endtime;
				timeQtyArray[k][1] = quantity;
				k++;
			}
			timeQtyValues.clear();
			double sum_var = 0;
			int x = 0;
      int i = 0;
      for (int j = i + 1; j < timeQtyArray.length; j++) {
      	if (timeQtyArray[j][0] > timeQtyArray[i][0]) {
        	for (x = j - 1; x >= i; x--) {
          	double var = timeQtyArray[x][1];
            sum_var = sum_var + var;
            if (x == 0) {
            	break;
            }
          }
					Values newValues = new Values((long)timeQtyArray[i][0], sum_var);
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
						pwr.print(valObject.getEndTime());
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

	public String formatItemName(String name){

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
    return finalString.toString();
	}

}