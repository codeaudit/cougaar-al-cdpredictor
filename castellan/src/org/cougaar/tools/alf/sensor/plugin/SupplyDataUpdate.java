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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;


public class SupplyDataUpdate {

    public SupplyDataUpdate() {

    }

    /* The data from predictor plugin is added to the temporary hashtables for each relationship
     * The data is added in ascending order of end time of the task
     */

    public void getSupplyQuantity(String supp, String cust, String suppclass, long exe_time, long time, double quantity, String itemname) {

        String supplier = supp;
        String customer = cust;
        String supplyclass = suppclass;
        String item_name = itemname;
        long time_days = time;
        long exe_days = exe_time;
        double supply_quantity = quantity;
        boolean flag = false;

        if (alt.isEmpty()) {
            CreateHashtable ch = new CreateHashtable(supplier, customer, supplyclass, exe_days, time_days, supply_quantity, item_name);
            hashTable = ch.setSupplyHT();
            alt.add(0, hashTable);
            flag = true;

        } else {
            for (int j = 0; j < alt.size(); j++) {
                CreateHashtable ch1 = new CreateHashtable(supplier, customer, supplyclass, item_name);
                if (alt.get(j) != null) {
                    Vector temp = (Vector) ((Hashtable) alt.get(j)).get(new Integer(1));
                    flag = false;
                    if (temp != null) {
                        if ((temp.elementAt(0).equals(ch1.getItemHT().elementAt(0))) &&
                                (temp.elementAt(1).equals(ch1.getItemHT().elementAt(1))) &&
                                (temp.elementAt(2).equals(ch1.getItemHT().elementAt(2)))
                        && (temp.elementAt(6).equals(ch1.getItemHT().elementAt(3)))) {
                            flag = true;
                            hashTable = (Hashtable) alt.get(j);
                            CreateHashtable ch2 = new CreateHashtable(supplier, customer, supplyclass, exe_days, time_days, supply_quantity, item_name);
                            for (int i = 1; i <= hashTable.size(); i++) {
                                long time_prev = new Long(((Vector) hashTable.get(new Integer(i))).elementAt(4).toString()).longValue();
                                long time_new = new Long(ch2.getSupplyHT().elementAt(4).toString()).longValue();
                                if (time_new < time_prev) {
                                    Vector temp1 = (Vector) hashTable.get(new Integer(i));
                                    hashTable.remove(new Integer(i));
                                    hashTable.put(new Integer(i), ch2.getSupplyHT());
                                    hashTable.put(new Integer(i + 1), temp1);
                                    break;
                                } else if (i == hashTable.size()) {
                                    hashTable.put(new Integer(hashTable.size() + 1), ch2.getSupplyHT());
                                    break;
                                }
                            }

                            break;
                        }
                    }
                }
            }
            if (flag == false) {
                CreateHashtable ch3 = new CreateHashtable(supplier, customer, supplyclass, exe_days, time_days, supply_quantity, item_name);
                hashTable = ch3.setSupplyHT();
                alt.add(alt.size(), hashTable);
            }
        }
    }

    /* This method is called when one execution day gets over
     * It returns an Arraylist containing actual demand quantity ordered per day by each customer
     * for each supply class.
     * The data in the hashtables is cleared before next day starts executing for faster output
     */

    public ArrayList returnDemandQuantity(String supp, String cust, String suppclass, long exe_time, long time, double quantity, String itemname) {

        String supplier = supp;
        String customer = cust;
        String supplyclass = suppclass;
        String item_name = itemname;
        long exe_days = exe_time;
        long time_days = time;
        double supply_quantity = quantity;
        ArrayList aylt = new ArrayList();

        if (alt != null) {
            for (int k = 0; k < alt.size(); k++) {
                Hashtable stable = (Hashtable) alt.get(k);
                long first_day = 0;
                int count = 0;
                Hashtable hbt = new Hashtable();
                for (int n = 1; n < stable.size(); n++) {
                    first_day = new Long(((Vector) stable.get(new Integer(n))).elementAt(4).toString()).longValue();
                    double quant = new Double(((Vector) stable.get(new Integer(n))).elementAt(5).toString()).doubleValue();
                    total_quant = total_quant + quant;
                    long next_day = new Long(((Vector) stable.get(new Integer(n + 1))).elementAt(4).toString()).longValue();
                    if (first_day == next_day) {
                        if (n == stable.size() - 1) {
                            Vector temp_vec = new Vector();
                            temp_vec.insertElementAt(((Vector) stable.get(new Integer(1))).elementAt(0).toString(), 0);
                            temp_vec.insertElementAt(((Vector) stable.get(new Integer(1))).elementAt(1).toString(), 1);
                            temp_vec.insertElementAt(((Vector) stable.get(new Integer(1))).elementAt(2).toString(), 2);
                            temp_vec.insertElementAt(new Long(((Vector) stable.get(new Integer(1))).elementAt(3).toString()), 3);
                            temp_vec.insertElementAt(new Long(((Vector) stable.get(new Integer(stable.size()))).elementAt(4).toString()), 4);
                            temp_vec.insertElementAt(new Double(total_quant), 5);
                            temp_vec.insertElementAt(new Long(first_day), 6);
                            temp_vec.insertElementAt(((Vector) stable.get(new Integer(1))).elementAt(6).toString(), 7);
                            count++;
                            hbt.put(new Integer(count), temp_vec);
                            total_quant = 0;
                            break;
                        }
                        continue;
                    } else {
                        Vector temp_vec = new Vector();
                        temp_vec.insertElementAt(((Vector) stable.get(new Integer(1))).elementAt(0).toString(), 0);
                        temp_vec.insertElementAt(((Vector) stable.get(new Integer(1))).elementAt(1).toString(), 1);
                        temp_vec.insertElementAt(((Vector) stable.get(new Integer(1))).elementAt(2).toString(), 2);
                        temp_vec.insertElementAt(new Long(((Vector) stable.get(new Integer(1))).elementAt(3).toString()), 3);
                        temp_vec.insertElementAt(new Long(((Vector) stable.get(new Integer(stable.size()))).elementAt(4).toString()), 4);
                        temp_vec.insertElementAt(new Double(total_quant), 5);
                        temp_vec.insertElementAt(new Long(first_day), 6);
                        temp_vec.insertElementAt(((Vector) stable.get(new Integer(1))).elementAt(6).toString(), 7);
                        count++;
                        hbt.put(new Integer(count), temp_vec);
                        total_quant = 0;
                    }
                }
                aylt.add(k, hbt);
                total_quant = 0;
            }
            if (aylt != null) {
                for (int a = 0; a < aylt.size(); a++) {

                    Hashtable tempHash = (Hashtable) aylt.get(a);
                    for (int int_count = 1; int_count <= tempHash.size(); int_count++) {
                        String sup = ((Vector) tempHash.get(new Integer(int_count))).elementAt(0).toString();
                        String cus = ((Vector) tempHash.get(new Integer(int_count))).elementAt(1).toString();
                        String supclass = ((Vector) tempHash.get(new Integer(int_count))).elementAt(2).toString();
                        long c_day = new Long(((Vector) tempHash.get(new Integer(int_count))).elementAt(3).toString()).longValue();
                        double tot_qty = new Double(((Vector) tempHash.get(new Integer(int_count))).elementAt(5).toString()).doubleValue();
                        long f_day = new Long(((Vector) tempHash.get(new Integer(int_count))).elementAt(6).toString()).longValue();
                        String items = ((Vector) tempHash.get(new Integer(int_count))).elementAt(7).toString();
                        try {
                            pr = new PrintWriter(new BufferedWriter(new FileWriter(sup + ".txt", true)));
                            pr.print(sup);
                            pr.print(",");
                            pr.print(cus);
                            pr.print(",");
                            pr.print(supclass);
                            pr.print(",");
                            pr.print(c_day);
                            pr.print(",");
                            pr.print(f_day);
                            pr.print(",");
                            pr.print(tot_qty);
                            pr.print(",");
                            pr.print(items);
                            pr.println();
                            pr.close();
                        } catch (Exception e) {
                            System.out.println(e);
                        }
                    }
                }
            }
            alt.clear();
            getSupplyQuantity(supplier, customer, supplyclass, exe_days, time_days, supply_quantity, item_name);
            return aylt;
        } else
            return null;
    }

    Hashtable hashTable;
    ArrayList alt = new ArrayList();
    int j = 0;
    double total_quant = 0;
    PrintWriter pr;
}
