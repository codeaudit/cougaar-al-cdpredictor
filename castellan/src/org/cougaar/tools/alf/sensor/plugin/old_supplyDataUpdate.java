
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
import java.util.Vector;
import java.util.Hashtable;
import java.lang.*;
import java.io.*;


public class supplyDataUpdate {
	
	public supplyDataUpdate() {
		
	}
	
		/* The data from predictor plugin is added to the temporary hashtables for each relationship
		 * The data is added in ascending order of end time of the task
		 */

		public void getSupplyQuantity(String supp, String cust, String suppclass, long exe_time, long time, double quantity) {
			
			String supplier = supp;
			String customer = cust;
			String supplyclass = suppclass;
			long time_days = time;
			long exe_days = exe_time;
			double supply_quantity = quantity;
			boolean flag = false;
		
			if(alt.isEmpty()) {
				createHashtable ch = new createHashtable(supplier, customer, supplyclass, exe_days, time_days, supply_quantity);
				hashTable = ch.setSupplyHT();
				alt.add(0,hashTable);
				flag = true;
			
			} else 
			{
				for(int j = 0; j<alt.size(); j++) 
				{
					createHashtable ch1 = new createHashtable(supplier, customer, supplyclass);
					if(alt.get(j)!= null) 
					{
						Vector temp = (Vector) ((Hashtable) alt.get(j)).get(new Integer(1));
						flag = false;
						if(temp!= null) 
						{
							if((temp.elementAt(0).equals(ch1.getHT().elementAt(0))) &&
						  	  (temp.elementAt(1).equals(ch1.getHT().elementAt(1))) &&
						      (temp.elementAt(2).equals(ch1.getHT().elementAt(2)))) 
						  	{ 
								flag = true;
								hashTable = (Hashtable)alt.get(j); 						
								createHashtable ch2 = new createHashtable(supplier, customer, supplyclass, exe_days, time_days, supply_quantity);
								for(int i = 1; i <= hashTable.size(); i++) {
									long time_prev = new Long(((Vector)hashTable.get(new Integer(i))).elementAt(4).toString()).longValue();
									long time_new  = new Long(((Vector)ch2.getSupplyHT()).elementAt(4).toString()).longValue();
									if(time_new < time_prev) 
									{ 
										Vector temp1 = (Vector)hashTable.get(new Integer(i));
										hashTable.remove(new Integer(i));
										hashTable.put(new Integer(i),ch2.getSupplyHT());
										hashTable.put(new Integer(i+1),temp1);
										break;								
									}
									else if(i==hashTable.size())
									{	
										hashTable.put(new Integer(hashTable.size()+1),ch2.getSupplyHT());
										break;
									}						
								}
						
								break;			
						    }
						}			
					}
				}							
				if(flag == false) 
				{	
					createHashtable ch3 = new createHashtable(supplier, customer, supplyclass, exe_days, time_days, supply_quantity);
					hashTable = ch3.setSupplyHT();
					alt.add(alt.size(),hashTable);
				}
			}		
		}
	
	/* This method is called when one execution day gets over
	 * It returns an Arraylist containing actual demand quantity ordered per day by each customer
	 * for each supply class.
	 * The data in the hashtables is cleared before next day starts executing for faster output
	 */
	
	public ArrayList returnDemandQuantity(String supp, String cust, String suppclass, long exe_time, long time, double quantity) {
		
		String supplier = supp;
		String customer = cust;
		String supplyclass = suppclass;
		long exe_days = exe_time;
		long time_days = time;
		double supply_quantity = quantity;		
		ArrayList aylt = new ArrayList();
		
		if(alt!= null) 
		{
			for(int k =0; k<alt.size(); k++) 
			{
				Hashtable stable = (Hashtable) alt.get(k);
				for(int m = 1; m <=stable.size(); m++)
				{
					double quant = new Double(((Vector)stable.get(new Integer(m))).elementAt(5).toString()).doubleValue();				
					total_quant = total_quant + quant;
				}
				long first_day = new Long(((Vector)stable.get(new Integer(1))).elementAt(4).toString()).longValue();
				Vector temp_vec = new Vector();
				temp_vec.insertElementAt(((Vector)stable.get(new Integer(1))).elementAt(0).toString(),0);
				temp_vec.insertElementAt(((Vector)stable.get(new Integer(1))).elementAt(1).toString(),1);
				temp_vec.insertElementAt(((Vector)stable.get(new Integer(1))).elementAt(2).toString(),2);
				temp_vec.insertElementAt(new Long(((Vector)stable.get(new Integer(1))).elementAt(3).toString()),3);
				temp_vec.insertElementAt(new Long(((Vector)stable.get(new Integer(stable.size()))).elementAt(4).toString()),4);
				temp_vec.insertElementAt(new Double(total_quant),5);
				temp_vec.insertElementAt(new Long(first_day),6);			
				aylt.add(k,temp_vec);
				total_quant = 0;
			}	
			if(aylt!= null) 
			{
				for(int a = 0; a<aylt.size(); a++) 
				{
					String sup = ((Vector)aylt.get(a)).elementAt(0).toString();
					String cus = ((Vector)aylt.get(a)).elementAt(1).toString();
					String supclass = ((Vector)aylt.get(a)).elementAt(2).toString();
					long c_day = new Long(((Vector)aylt.get(a)).elementAt(3).toString()).longValue();
					long l_day = new Long(((Vector)aylt.get(a)).elementAt(4).toString()).longValue();
					double tot_qty = new Double(((Vector)aylt.get(a)).elementAt(5).toString()).doubleValue();
					long f_day = new Long(((Vector)aylt.get(a)).elementAt(6).toString()).longValue();
					System.out.println("Customer is" +cus+ " Supply Class is "+supclass+
					" First Day "+f_day+ " Current Day is "+c_day+ " Last Day is "+l_day+ " Total Quantity is "+tot_qty);
					try
						{
				   			pr = new PrintWriter(new BufferedWriter(new FileWriter(sup+ ".txt", true)));
								pr.print(sup);
								pr.print(",");
								pr.print(cus);
								pr.print(",");
								pr.print(supclass);
								pr.print(",");
								pr.print(c_day);
								pr.print(",");
								pr.print(l_day);
								pr.print(",");
								pr.print(tot_qty);
								pr.print(",");
								pr.print(f_day);
								pr.println();
								pr.close();
						} 
						catch (Exception e)
             			{
             				System.out.println(e);
             			}
				}
				alt.clear();
				getSupplyQuantity(supplier, customer, supplyclass, exe_days, time_days, supply_quantity);				
			}
			return aylt;					
		}
		else 
			return null;
	}		

	Hashtable hashTable;
	ArrayList alt = new ArrayList();
	int j = 0;	
	double total_quant = 0;
	PrintWriter pr;
}