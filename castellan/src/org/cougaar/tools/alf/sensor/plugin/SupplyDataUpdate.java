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
import java.util.Collection;


public class SupplyDataUpdate {

    public SupplyDataUpdate() {

    }

    /* The data from predictor plugin is added to the temporary hashtables for each relationship
       supplier-customer-supplyclass-itemname
     * The data is added in ascending order of end time of the task
     */

    public void getSupplyQuantity(String supp, String cust, String suppclass, String itemname, long publish_time, long commitment_time, long end_time, double quantity) {

        String supplier = supp;                       //Task supplier (agent iself)
        String customer = cust;                       //Task customer
        String supplyclass = suppclass;               //Supply Class of the Task
        String item_name = itemname;                  //Item Name of the Task
        long task_publish_date = publish_time;        //Publish Date of the Task
        long task_commitment_date = commitment_time;  //Task Commitment Date
        long task_end_date = end_time;                //End date on the task
        double supply_quantity = quantity;            //Task quantity

        boolean flag = false;

        if (alt.isEmpty()) {
            CreateHashtable ch = new CreateHashtable(supplier, customer, supplyclass, item_name, task_publish_date, task_commitment_date, task_end_date, supply_quantity);
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
                        && (temp.elementAt(3).equals(ch1.getItemHT().elementAt(3)))) {
                            flag = true;
                            hashTable = (Hashtable) alt.get(j);
                            CreateHashtable ch2 = new CreateHashtable(supplier, customer, supplyclass, item_name, task_publish_date, task_commitment_date, task_end_date, supply_quantity);
                            for (int i = 1; i <= hashTable.size(); i++) {
                                long time_prev = new Long(((Vector) hashTable.get(new Integer(i))).elementAt(6).toString()).longValue();
                                long time_new = new Long(ch2.getSupplyHT().elementAt(6).toString()).longValue();
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
                CreateHashtable ch3 = new CreateHashtable(supplier, customer, supplyclass, item_name, task_publish_date, task_commitment_date, task_end_date, supply_quantity);
                hashTable = ch3.setSupplyHT();
                alt.add(alt.size(), hashTable);
            }
        }
        System.out.println("alt size: "+alt.size());
    }

    /* This method is called when one execution day gets over
     * It returns an Arraylist containing actual demand quantity ordered per day by each customer
     * for each supply class for each item.
     * The data in the hashtables is cleared before next day starts executing for faster output
     */

    public PredictorSupplyArrayList returnDemandQuantity(String supp, String cust, String suppclass, String itemname, long publish_time, long commitment_date, long time, double quantity) {

        String supplier = supp;
        String customer = cust;
        String supplyclass = suppclass;
        String item_name = itemname;
        long task_publish_date = publish_time;
        long task_end_date = time;
        double supply_quantity = quantity;
        long commitdate = commitment_date;
        PredictorSupplyArrayList aylt = new PredictorSupplyArrayList();

        if (alt != null) {
            System.out.println("alt size1: "+alt.size());
            for (int k = 0; k < alt.size(); k++) {
                Hashtable stable = (Hashtable) alt.get(k);
                long last_day = new Long(((Vector) stable.get(new Integer(stable.size()))).elementAt(6).toString()).longValue();
                long first_day = 0;
                int count = 0;
                Hashtable hbt = new Hashtable();
                for (int n = 1; n < stable.size(); n++) {
                    first_day = new Long(((Vector) stable.get(new Integer(n))).elementAt(6).toString()).longValue();
                    double quant = new Double(((Vector) stable.get(new Integer(n))).elementAt(7).toString()).doubleValue();
                    total_quant = total_quant + quant;
                    long next_day = new Long(((Vector) stable.get(new Integer(n + 1))).elementAt(6).toString()).longValue();
                    if (first_day == next_day) {
                        if (n == stable.size() - 1) {
                            Vector temp_vec = new Vector();
                            temp_vec.insertElementAt(((Vector) stable.get(new Integer(1))).elementAt(0).toString(), 0);                         //Supplier
                            temp_vec.insertElementAt(((Vector) stable.get(new Integer(1))).elementAt(1).toString(), 1);                         //Customer
                            temp_vec.insertElementAt(((Vector) stable.get(new Integer(1))).elementAt(2).toString(), 2);                         //Supply Class
                            temp_vec.insertElementAt(((Vector) stable.get(new Integer(1))).elementAt(3).toString(), 3);                         //Item Name
                            temp_vec.insertElementAt(new Long(((Vector) stable.get(new Integer(1))).elementAt(4).toString()), 4);               //Publish_date
                            temp_vec.insertElementAt(new Long(((Vector) stable.get(new Integer(stable.size()))).elementAt(5).toString()), 5);   //Commitment_date
                            temp_vec.insertElementAt(new Long(((Vector) stable.get(new Integer(stable.size()))).elementAt(6).toString()), 6);   //End_date
                            temp_vec.insertElementAt(new Double(total_quant), 7);                                                               //Quantity
                            count++;
                            hbt.put(new Integer(count), temp_vec);
                            total_quant = 0;
                            break;
                        }
                        continue;
                    } else {
                        Vector temp_vec = new Vector();
                        temp_vec.insertElementAt(((Vector) stable.get(new Integer(1))).elementAt(0).toString(), 0);                            //Supplier
                        temp_vec.insertElementAt(((Vector) stable.get(new Integer(1))).elementAt(1).toString(), 1);                            //Customer
                        temp_vec.insertElementAt(((Vector) stable.get(new Integer(1))).elementAt(2).toString(), 2);                            //Supply Class
                        temp_vec.insertElementAt(((Vector) stable.get(new Integer(1))).elementAt(3).toString(), 3);                            //Item Name
                        temp_vec.insertElementAt(new Long(((Vector) stable.get(new Integer(1))).elementAt(4).toString()), 4);                  //Publish_date
                        temp_vec.insertElementAt(new Long(((Vector) stable.get(new Integer(n))).elementAt(5).toString()), 5);                  //Commitment_date
                        temp_vec.insertElementAt(new Long(((Vector) stable.get(new Integer(stable.size()))).elementAt(6).toString()), 6);      //End_date
                        temp_vec.insertElementAt(new Double(total_quant), 7);                                                                  //Quantity
                        count++;
                        hbt.put(new Integer(count), temp_vec);
                        total_quant = 0;
                    }
                }
                aylt.add(k, hbt);
                total_quant = 0;
            }
            alt.clear();
            System.out.println("aylt size: "+aylt.size());
            getSupplyQuantity(supplier, customer, supplyclass,  item_name, task_publish_date, commitdate, task_end_date, supply_quantity);
            return aylt;
        } else
            return null;
    }

    public PredictorSupplyArrayList returnDemandQuantity1(String supp, String cust, String suppclass, String itemname, long publish_time, long commitment_date, long time, double quantity) {

        String supplier = supp;
        String customer = cust;
        String supplyclass = suppclass;
        String item_name = itemname;
        long task_publish_date = publish_time;
        long task_end_date = time;
        double supply_quantity = quantity;
        long commitdate = commitment_date;
        PredictorSupplyArrayList aylt = new PredictorSupplyArrayList();
        PredictorSupplyArrayList aylt_new = new PredictorSupplyArrayList();
        if(alt!= null){
            for(int i=0; i<alt.size();i++){
                Hashtable max_end_time_hash = (Hashtable) alt.get(i);
                Vector max_end_time_vector = (Vector) max_end_time_hash.get(new Integer(max_end_time_hash.size()));
                String supplier_name = max_end_time_vector.elementAt(0).toString();
                String customer_name = max_end_time_vector.elementAt(1).toString();
                String supply_class_name = max_end_time_vector.elementAt(2).toString();
                String item_class_name = max_end_time_vector.elementAt(3).toString();
                long max_publish_time = new Long(max_end_time_vector.elementAt(4).toString()).longValue();
                long max_commit_time = new Long(max_end_time_vector.elementAt(5).toString()).longValue();
                long max_end_time = new Long(max_end_time_vector.elementAt(6).toString()).longValue();
                double max_quantity = new Double(max_end_time_vector.elementAt(7).toString()).doubleValue();
                double quant = max_quantity;
                for(int j=1; j<max_end_time_hash.size();j++){
                    Vector new_max_end_time_vector = (Vector) max_end_time_hash.get(new Integer(j));
                    long new_max_end_time = new Long(new_max_end_time_vector.elementAt(6).toString()).longValue();
                    if(max_end_time == new_max_end_time){
                        double new_max_quantity = new Double(new_max_end_time_vector.elementAt(7).toString()).doubleValue();
                        quant = quant + new_max_quantity;
                    }
                }
                Vector arraylist_vector = getVectorForm(supplier_name, customer_name, supply_class_name, item_class_name, max_publish_time,
                max_commit_time, max_end_time, quant);
                aylt.add(i,arraylist_vector);
            }

             if (aylt!= null) {
                for (int b = 0; b < aylt.size()-1; b++) {
                   long larger_time = -1;
                   Vector scan_vector = (Vector) aylt.get(b);
                   String supply_class = scan_vector.elementAt(2).toString();
                   long max_time = new Long(scan_vector.elementAt(6).toString()).longValue();
                   long max_commit_time = new Long(scan_vector.elementAt(5).toString()).longValue();
                   long difference = max_time - max_commit_time;
                   for(int c = b+1; c < aylt.size(); c++){
                       Vector scan_vector1 = (Vector) aylt.get(c);
                        String supply_class1 = scan_vector1.elementAt(2).toString();
                        long max_time1 = new Long(scan_vector1.elementAt(6).toString()).longValue();
                        long max_commit_time1 = new Long(scan_vector1.elementAt(5).toString()).longValue();
                        long difference1 = max_time1 - max_commit_time1;
                       if(supply_class.equalsIgnoreCase(supply_class1)== true){
                            if(larger_time < max_time1){
                            larger_time = Math.max(max_time, max_time1);
                            //scan_vector.removeElementAt(5);
                            //scan_vector.insertElementAt(new Long(larger_time-difference),5);
                            scan_vector.removeElementAt(6);
                            scan_vector.insertElementAt(new Long(larger_time),6);
                            //scan_vector1.removeElementAt(5);
                            //scan_vector1.insertElementAt(new Long(larger_time-difference1),5);
                            scan_vector1.removeElementAt(6);
                            scan_vector1.insertElementAt(new Long(larger_time),6);
                            }
                        } else {
                            continue;
                        }
                       if(c == aylt.size()-1){
                           break;
                       }
                   }

                }
            }
            if(max_psal.isEmpty()){
                Collection c = aylt;
                max_psal.addAll(c);
            alt.clear();
            System.out.println("aylt size: "+aylt.size());
            getSupplyQuantity(supplier, customer, supplyclass,  item_name, task_publish_date, commitdate, task_end_date, supply_quantity);
            return aylt;
            } else
            {
            aylt_new = getMaxQuantities(aylt);
            }
            alt.clear();
            System.out.println("aylt size: "+aylt.size());
            getSupplyQuantity(supplier, customer, supplyclass,  item_name, task_publish_date, commitdate, task_end_date, supply_quantity);
            return aylt_new;
        } else
            return null;
    }

    public PredictorSupplyArrayList getMaxQuantities(PredictorSupplyArrayList psal){
       Collection c = psal;
       PredictorSupplyArrayList max_psal_new = new PredictorSupplyArrayList();
       max_psal_new.addAll(c);
       if(!max_psal.isEmpty()){
       for(int i = 0; i < max_psal.size();i++){
           Vector max_psal_vector = (Vector) max_psal.get(i);
           String customer = max_psal_vector.elementAt(1).toString();
           String supply_class = max_psal_vector.elementAt(2).toString();
           long max_psal_time = new Long(max_psal_vector.elementAt(6).toString()).longValue();
           long max_psal_commit_time = new Long(max_psal_vector.elementAt(5).toString()).longValue();
           long difference = max_psal_time - max_psal_commit_time;
           for(int j=0; j < max_psal_new.size();j++){
              Vector max_psal_vector_j = (Vector) max_psal_new.get(j);
              String customer_j = max_psal_vector_j.elementAt(1).toString();
              String supply_class_j = max_psal_vector_j.elementAt(2).toString();
              if(customer.equalsIgnoreCase(customer_j)== true && supply_class.equalsIgnoreCase(supply_class_j)==true){
                  long max_psal_time_j = new Long(max_psal_vector_j.elementAt(6).toString()).longValue();
                  long max_psal_commit_time_j = new Long(max_psal_vector_j.elementAt(5).toString()).longValue();
                  long difference1 = max_psal_time_j - max_psal_commit_time_j;
                  if(max_psal_time > max_psal_time_j){
                      max_psal_time_j = max_psal_time;
                      //max_psal_vector_j.removeElementAt(5);
                      //max_psal_vector_j.insertElementAt(new Long(max_psal_time_j-difference1),5);
                      max_psal_vector_j.removeElementAt(6);
                      max_psal_vector_j.insertElementAt(new Long(max_psal_time_j),6);

                  } else {
                      max_psal_time = max_psal_time_j;
                      //max_psal_vector.removeElementAt(5);
                      //max_psal_vector.insertElementAt(new Long(max_psal_time-difference),5);
                      max_psal_vector.removeElementAt(6);
                      max_psal_vector.insertElementAt(new Long(max_psal_time),6);
                  }
              }
           }
       }
           return max_psal_new;
    }else
       {
           System.out.println("max_psal is empty");
           return null;
       }
    }

    public Vector getVectorForm(String supp, String cust, String suppclass, String itemname, long publish_time, long commitment_date, long time, double quantity) {
      Vector vector = new Vector();
      vector.insertElementAt(supp, 0);
      vector.insertElementAt(cust, 1);
      vector.insertElementAt(suppclass, 2);
      vector.insertElementAt(itemname, 3);
      vector.insertElementAt(new Long(publish_time),4);
      vector.insertElementAt(new Long(commitment_date),5);
      vector.insertElementAt(new Long(time),6);
      vector.insertElementAt(new Double(quantity),7);
      return vector;
    }

      public void printToFile(String su, String cu, String sucl, String itm, long cday, long commitdate, long fday, double qty, boolean toggle){
        toggle = print_flag;
        if(toggle == true){
           try {
               pr = new PrintWriter(new BufferedWriter(new FileWriter(su + "error" + ".txt", true)));
               pr.print(su);
               pr.print(",");
               pr.print(cu);
               pr.print(",");
               pr.print(sucl);
               pr.print(",");
               pr.print(itm);
               pr.print(",");
               pr.print(cday);
               pr.print(",");
               pr.print(commitdate);
               pr.print(",");
               pr.print(fday);
               pr.print(",");
               pr.print(qty);
               pr.println();
               pr.close();
           } catch (Exception e) {
               System.out.println(e);
            }
        }
    }
    Hashtable hashTable;
    ArrayList alt = new ArrayList();
    int j = 0;
    double total_quant = 0;
    PrintWriter pr;
    boolean print_flag = false;
    PredictorSupplyArrayList max_psal = new PredictorSupplyArrayList();
}
