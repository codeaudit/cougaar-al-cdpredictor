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
import java.util.Hashtable;
import java.util.Vector;

public class kalmanFilter {

    public kalmanFilter(ArrayList alt) {
        this.alt = alt;
    }

    public Hashtable getHashtable(String supplier, String customer, String supply_class) {

        String s = supplier;
        String c = customer;
        String sc = supply_class;
        Hashtable ht = null;
        for (int i = 0; i < alt.size(); i++) {
            ht = (Hashtable) alt.get(i);
            Vector vList = (Vector) ht.get(new Integer(1));
            if (s.compareToIgnoreCase((String) vList.elementAt(0)) == 0 &&
                    c.compareToIgnoreCase((String) vList.elementAt(1)) == 0 &&
                    sc.compareToIgnoreCase((String) vList.elementAt(2)) == 0) {
                return ht;
            }
        }

        return null;
    }

    /* This method does time update on the demand model implies it apriori estimates demand
     * from the customer one time step in the future (time steps could be made multiple)
     */

    public void timeUpdate(ArrayList a) {

        ArrayList ali = a;
        if (ali != null) {
            for (int l = 0; l < ali.size(); l++) {
                String s = ((Vector) ali.get(l)).elementAt(0).toString();
                String c = ((Vector) ali.get(l)).elementAt(1).toString();
                String sc = ((Vector) ali.get(l)).elementAt(2).toString();
                long current_day = new Long(((Vector) ali.get(l)).elementAt(3).toString()).longValue();
                long last_day = new Long(((Vector) ali.get(l)).elementAt(4).toString()).longValue();
                long first_day = new Long(((Vector) ali.get(l)).elementAt(6).toString()).longValue();
                Hashtable hTable = getHashtable(s, c, sc);
                if (hTable != null) {
                    for (int i = 1; i <= hTable.size(); i++) {
                        Vector vl = (Vector) hTable.get(new Integer(i));
                        // if (first_day == new Long(vl.elementAt(3).toString()).longValue()) {
                        if (current_day == new Long(vl.elementAt(3).toString()).longValue()) {
                            // double ini_total_rate = 0;
                            if (flag == true) {
                                // for (int m = (int) first_day; m <= last_day; m++) {
                                // double ini_rate = new Double(((Vector) hTable.get(new Integer(m))).elementAt(4).toString()).doubleValue(); //Get the rate
                                // ini_total_rate = ini_total_rate + ini_rate;
                                // }
                                // Vector vTemp = (Vector) hTable.get(new Integer((int) last_day + 1));
                                // double rate = new Double(vTemp.elementAt(4).toString()).doubleValue();
                                // double rate = new Double(vl.elementAt(4).toString()).doubleValue();
                                // aprior_estimate = rate + ini_total_rate - new Double(((Vector) hTable.get(new Integer((int) first_day))).elementAt(4).toString()).doubleValue();
                                // aprior_estimate = rate - new Double(((Vector) hTable.get(new Integer((int) first_day))).elementAt(4).toString()).doubleValue();
                                long d = current_day + 1;
                                double aprior_estimate = new Double(((Vector) hTable.get(new Integer(i + 1))).elementAt(4).toString()).doubleValue();
                                Vector temp_est_vec = new Vector();
                                temp_est_vec.insertElementAt(((Vector) hTable.get(new Integer(1))).elementAt(0).toString(), 0);
                                temp_est_vec.insertElementAt(((Vector) hTable.get(new Integer(1))).elementAt(1).toString(), 1);
                                temp_est_vec.insertElementAt(((Vector) hTable.get(new Integer(1))).elementAt(2).toString(), 2);
                                temp_est_vec.insertElementAt(new Long(d), 3);
                                temp_est_vec.insertElementAt(new Long(last_day), 4);
                                temp_est_vec.insertElementAt(new Double(aprior_estimate), 5);
                                estimate_array.add(0, temp_est_vec);
                                flag = false;
                                //ini_total_rate = 0;
                                System.out.println("Aprior Estimate for day " + d + " supplier " + s + " customer " + c + " supply class " + sc + " is " + aprior_estimate);
                                break;

                            } else {
                                //for (int m = (int) first_day; m <= last_day; m++) {
                                //double ini_rate = new Double(((Vector) hTable.get(new Integer(m))).elementAt(4).toString()).doubleValue(); //Get the rate
                                //ini_total_rate = ini_total_rate + ini_rate;
                                // }
                                // Vector vTemp = (Vector) hTable.get(new Integer((int) last_day + 1));
                                //double rate = new Double(vTemp.elementAt(4).toString()).doubleValue();
                                //aprior_estimate = rate + ini_total_rate - new Double(((Vector) hTable.get(new Integer((int) first_day))).elementAt(4).toString()).doubleValue();
                                long d = current_day + 1;
                                double aprior_estimate = new Double(((Vector) hTable.get(new Integer(i + 1))).elementAt(4).toString()).doubleValue();
                                Vector temp_est_vec = new Vector();
                                temp_est_vec.insertElementAt(((Vector) hTable.get(new Integer(1))).elementAt(0).toString(), 0);
                                temp_est_vec.insertElementAt(((Vector) hTable.get(new Integer(1))).elementAt(1).toString(), 1);
                                temp_est_vec.insertElementAt(((Vector) hTable.get(new Integer(1))).elementAt(2).toString(), 2);
                                temp_est_vec.insertElementAt(new Long(d), 3);
                                temp_est_vec.insertElementAt(new Long(last_day + 1), 4);
                                temp_est_vec.insertElementAt(new Double(aprior_estimate), 5);
                                estimate_array.add(estimate_array.size(), temp_est_vec);
                                //ini_total_rate = 0;
                                System.out.println("Aprior Estimate for day " + d + " supplier " + s + " customer " + c + " supply class " + sc + " is " + aprior_estimate);
                                break;
                            }
                        }
                    }
                }
            }
        } else
            return;
    }

    /*  This method uses the apriori estimate and the actual demand to compute the error
     *  and the aposteriori estimate and updates the demand model
     */

    public void measurementUpdate(ArrayList a) {

        double gain = 0.5;
        ArrayList ali = a;
        if (ali != null) {
            for (int l = 0; l < ali.size(); l++) {
                String s = ((Vector) ali.get(l)).elementAt(0).toString();
                String c = ((Vector) ali.get(l)).elementAt(1).toString();
                String sc = ((Vector) ali.get(l)).elementAt(2).toString();
                //long current_day = new Long(((Vector) ali.get(l)).elementAt(3).toString()).longValue();
                //long last_day = new Long(((Vector) ali.get(l)).elementAt(4).toString()).longValue();
                long curr_day = new Long(((Vector) ali.get(l)).elementAt(3).toString()).longValue();
                Hashtable hTable = getHashtable(s, c, sc);
                if (hTable != null) {
                    if (estimate_array != null) {
                        for (int n = 0; n < estimate_array.size(); n++) {
                            if ((((Vector) estimate_array.get(n)).elementAt(0).toString()).equals(s) &&
                                    (((Vector) estimate_array.get(n)).elementAt(1).toString()).equals(c) &&
                                    (((Vector) estimate_array.get(n)).elementAt(2).toString()).equals(sc)) {
                                double apri_estimate = new Double(((Vector) estimate_array.get(n)).elementAt(5).toString()).doubleValue();
                                double act_value = new Double(((Vector) ali.get(l)).elementAt(5).toString()).doubleValue();
                                if (apri_estimate != -1 && act_value != -1) {
                                    double apost = 0;
                                    double error = act_value - apri_estimate;
                                    System.out.println("Error is  " + error + " Supplier " + s + " Customer " + c + " Supply Class " + sc);
                                    if (error < 0) {
                                        apost = apri_estimate - (gain * error);
                                        System.out.println("Aposterior Estimate is " + apost + " Supplier " + s + " Customer " + c + " Supply Class " + sc);
                                    } else {
                                        apost = apri_estimate + (gain * error);
                                        System.out.println("Aposterior Estimate is " + apost + " Supplier " + s + " Customer " + c + " Supply Class " + sc);
                                    }
                                    for (int i = 1; i <= hTable.size(); i++) {
                                        Vector vl = (Vector) hTable.get(new Integer(i));
                                        //if(new Long(vl.elementAt(3).toString()).longValue() == last_day) {
                                        if (new Long(vl.elementAt(3).toString()).longValue() == curr_day) {
                                            double original_value = new Double(vl.elementAt(4).toString()).doubleValue();
                                            original_value = apost;
                                            if (original_value < 0) {
                                                original_value = 0;
                                                vl.removeElementAt(4);
                                                vl.insertElementAt(new Double(original_value), 4);
                                                break;
                                            } else {
                                                vl.removeElementAt(4);
                                                vl.insertElementAt(new Double(original_value), 4);
                                                break;
                                            }
                                        }
                                    }
                                }

                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    private ArrayList alt;
    private ArrayList estimate_array = new ArrayList();
    private boolean flag = true;
}