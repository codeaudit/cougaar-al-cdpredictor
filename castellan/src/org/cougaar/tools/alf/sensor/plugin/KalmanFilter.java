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
import java.lang.Math;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.FileWriter;

public class KalmanFilter implements java.io.Serializable {

    public KalmanFilter(ArrayList alt) {
        this.alt = alt;
    }

    public Hashtable getHashtable(String supplier, String customer, String supply_class, String itemname) {

        String s = supplier;
        String c = customer;
        String sc = supply_class;
        String item = itemname;
        Hashtable ht = null;
        for (int i = 0; i < alt.size(); i++) {
            ht = (Hashtable) alt.get(i);
            Vector vList = (Vector) ht.get(new Integer(1));
            if (s.compareToIgnoreCase((String) vList.elementAt(0)) == 0 &&
                    c.compareToIgnoreCase((String) vList.elementAt(1)) == 0 &&
                    sc.compareToIgnoreCase((String) vList.elementAt(2)) == 0 &&
                    item.compareToIgnoreCase((String) vList.elementAt(5)) == 0) {
                return ht;
            }
        }

        return null;
    }

    /* This method does time update on the demand model implies it apriori estimates demand
     * from the customer one time step in the future (time steps could be made multiple)
     */

    public ArrayList timeUpdate(ArrayList a) {

        ArrayList ali = a;
        if (ali != null) {
            for (int l = 0; l < ali.size(); l++) {
                Hashtable tempHTable = (Hashtable)ali.get(l);
                for(int iterTable = 1; iterTable<=tempHTable.size();iterTable++){
                    String s = ((Vector)tempHTable.get(new Integer(iterTable))).elementAt(0).toString();
                    String c = ((Vector)tempHTable.get(new Integer(iterTable))).elementAt(1).toString();
                    String sc = ((Vector)tempHTable.get(new Integer(iterTable))).elementAt(2).toString();
                    String item = ((Vector)tempHTable.get(new Integer(iterTable))).elementAt(7).toString();
                    Hashtable hTable = getHashtable(s, c, sc, item);
                    if(iterTable == 1){
                        long current_day = new Long(((Vector)tempHTable.get(new Integer(iterTable))).elementAt(3).toString()).longValue();
                        long pred_day = new Long(((Vector)tempHTable.get(new Integer(iterTable))).elementAt(6).toString()).longValue();
                        long last_day = new Long(((Vector)tempHTable.get(new Integer(iterTable))).elementAt(4).toString()).longValue();
                        if (hTable != null) {
                            for (int m = 1; m <= hTable.size(); m++) {
                                Vector vl = (Vector) hTable.get(new Integer(m));
                                if (pred_day == new Long(vl.elementAt(3).toString()).longValue()) {
                                    long d = pred_day + 1;
                                    //long d = pred_day + 86400000;
                                    double aprior_estimate = new Double(((Vector) hTable.get(new Integer(m))).elementAt(4).toString()).doubleValue();
                                    Vector temp_est_vec = new Vector();
                                    temp_est_vec.insertElementAt(((Vector) hTable.get(new Integer(1))).elementAt(0).toString(), 0);
                                    temp_est_vec.insertElementAt(((Vector) hTable.get(new Integer(1))).elementAt(1).toString(), 1);
                                    temp_est_vec.insertElementAt(((Vector) hTable.get(new Integer(1))).elementAt(2).toString(), 2);
                                    temp_est_vec.insertElementAt(new Long(d), 3);
                                    temp_est_vec.insertElementAt(new Long(last_day), 4);
                                    temp_est_vec.insertElementAt(new Double(aprior_estimate), 5);
                                    temp_est_vec.insertElementAt(((Vector) hTable.get(new Integer(1))).elementAt(5).toString(), 6);
                                    estimate_array.add(estimate_array.size(), temp_est_vec);
                                    //System.out.println("Aprior Estimate for day " + d + " supplier " + s + " customer " + c + " supply class " + sc + " is " + aprior_estimate);
                                    break;
                                }
                            }
                        }
                    }
                    else {
                        long day_val = new Long(((Vector)tempHTable.get(new Integer(iterTable))).elementAt(6).toString()).longValue();
                        if (hTable != null) {
                          for (int i = 1; i <= hTable.size(); i++) {
                              Vector v = (Vector) hTable.get(new Integer(i));
                              long day = new Long(v.elementAt(3).toString()).longValue();
                              if (day_val == day) {
                                  v.removeElementAt(4);
                                  double quantity = new Double(((Vector)tempHTable.get(new Integer(iterTable))).elementAt(5).toString()).doubleValue();
                                  v.insertElementAt(new Double(quantity),4);
                                  break;
                              } else {
                                  continue;
                              }
                          }
                        }
                    }
                }
            }
            return estimate_array;
        } else
            return null;
    }



    /*  This method uses the apriori estimate and the actual demand to compute the error
     *  and the aposteriori estimate and updates the demand model
     */

    public void measurementUpdate(ArrayList a) {

        double gain = 0.5;
        ArrayList ali = a;
        if (ali != null) {
            for (int l = 0; l < ali.size(); l++) {
                 Hashtable tempHTable = (Hashtable)ali.get(l);
                 if(tempHTable!=null){
                    boolean flag = tempHTable.containsKey(new Integer(1));
                    if(flag==true){
                    String s = ((Vector)tempHTable.get(new Integer(1))).elementAt(0).toString();
                    String c = ((Vector)tempHTable.get(new Integer(1))).elementAt(1).toString();
                    String sc = ((Vector)tempHTable.get(new Integer(1))).elementAt(2).toString();
                    String item = ((Vector)tempHTable.get(new Integer(1))).elementAt(7).toString();
                    Hashtable hTable = getHashtable(s, c, sc, item);
                    if (hTable != null) {
                        if (estimate_array != null) {

                            for (int n = 0; n < estimate_array.size(); n++) {
                                if ((((Vector) estimate_array.get(n)).elementAt(0).toString()).equals(s) &&
                                    (((Vector) estimate_array.get(n)).elementAt(1).toString()).equals(c) &&
                                    (((Vector) estimate_array.get(n)).elementAt(2).toString()).equals(sc) &&
                                        (((Vector) estimate_array.get(n)).elementAt(6).toString()).equals(item)) {

                                    double apri_estimate = new Double(((Vector) estimate_array.get(n)).elementAt(5).toString()).doubleValue();
                                    double act_value = new Double(((Vector) tempHTable.get(new Integer(1))).elementAt(5).toString()).doubleValue();
                                    long day_val = new Long(((Vector) tempHTable.get(new Integer(1))).elementAt(3).toString()).longValue();
                                    if (apri_estimate != -1 && act_value != -1) {
                                        double apost = 0;
                                        double error = act_value - apri_estimate;
                                        //System.out.println("Error is  " + error + " Supplier " + s + " Customer " + c + " Supply Class " + sc);
                                        if (error < 0) {
                                            apost = apri_estimate - (gain * Math.abs(error));
                                            //System.out.println("Aposterior Estimate is " + apost + " Supplier " + s + " Customer " + c + " Supply Class " + sc);
                                        } else {
                                            apost = apri_estimate + (gain * Math.abs(error));
                                            //System.out.println("Aposterior Estimate is " + apost + " Supplier " + s + " Customer " + c + " Supply Class " + sc);
                                        }
                                        try {
                                            pr = new PrintWriter(new BufferedWriter(new FileWriter(s + "error" + ".txt", true)));
                                            pr.print(s);
                                            pr.print(",");
                                            pr.print(c);
                                            pr.print(",");
                                            pr.print(sc);
                                            pr.print(",");
                                            pr.print(item);
                                            pr.print(",");
                                            pr.print(day_val);
                                            pr.print(",");
                                            pr.print(apri_estimate);
                                            pr.print(",");
                                            pr.print(apost);
                                            pr.print(",");
                                            pr.print(error);
                                            pr.println();
                                            pr.close();
                                        } catch (Exception e) {
                                            System.out.println(e);
                                        }
                                        for (int i = 1; i <= hTable.size(); i++) {
                                        Vector vl = (Vector) hTable.get(new Integer(i));
                                        //if(new Long(vl.elementAt(3).toString()).longValue() == last_day) {
                                        long curr_day = new Long(((Vector) tempHTable.get(new Integer(1))).elementAt(6).toString()).longValue();
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
           estimate_array.clear();
        } else
            return;
    }

  /*   public ArrayList allTimeUpdate(ArrayList a) {

        ArrayList ali = a;
        if (ali != null) {
            for (int l = 0; l < ali.size(); l++) {
                Hashtable tempHTable = (Hashtable)ali.get(l);
                for(int iterTable = 1; iterTable<=tempHTable.size();iterTable++){
                    String s = ((Vector)tempHTable.get(new Integer(iterTable))).elementAt(0).toString();
                    String c = ((Vector)tempHTable.get(new Integer(iterTable))).elementAt(1).toString();
                    String sc = ((Vector)tempHTable.get(new Integer(iterTable))).elementAt(2).toString();
                    Hashtable hTable = getHashtable(s, c, sc);
                        long current_day = new Long(((Vector)tempHTable.get(new Integer(iterTable))).elementAt(3).toString()).longValue();
                        long pred_day = new Long(((Vector)tempHTable.get(new Integer(iterTable))).elementAt(6).toString()).longValue();
                        long last_day = new Long(((Vector)tempHTable.get(new Integer(iterTable))).elementAt(4).toString()).longValue();
                        if (hTable != null) {
                            for (int m = 1; m <= hTable.size(); m++) {
                                Vector vl = (Vector) hTable.get(new Integer(m));
                                if (pred_day == new Long(vl.elementAt(3).toString()).longValue()) {
                                    long d = pred_day + 1;
                                    //long d = pred_day + 86400000;
                                    double aprior_estimate = new Double(((Vector) hTable.get(new Integer(m))).elementAt(4).toString()).doubleValue();
                                    Vector temp_est_vec = new Vector();
                                    temp_est_vec.insertElementAt(((Vector) hTable.get(new Integer(1))).elementAt(0).toString(), 0);
                                    temp_est_vec.insertElementAt(((Vector) hTable.get(new Integer(1))).elementAt(1).toString(), 1);
                                    temp_est_vec.insertElementAt(((Vector) hTable.get(new Integer(1))).elementAt(2).toString(), 2);
                                    temp_est_vec.insertElementAt(new Long(d), 3);
                                    temp_est_vec.insertElementAt(new Long(last_day), 4);
                                    temp_est_vec.insertElementAt(new Double(aprior_estimate), 5);
                                    estimate_array.add(estimate_array.size(), temp_est_vec);
                                    for (int i = 1; i <= hTable.size(); i++) {
                                      Vector v = (Vector) hTable.get(new Integer(i));
                                      long day = new Long(v.elementAt(3).toString()).longValue();
                                      if (pred_day == day) {
                                        v.removeElementAt(4);
                                        double quantity = new Double(((Vector)tempHTable.get(new Integer(iterTable))).elementAt(5).toString()).doubleValue();
                                        v.insertElementAt(new Double(quantity),4);
                                        break;
                                        } else {
                                          continue;
                                        }
                                    }
                                    //System.out.println("Aprior Estimate for day " + d + " supplier " + s + " customer " + c + " supply class " + sc + " is " + aprior_estimate);
                                    //break;
                                }
                            }
                        }

                    else {
                          continue;
                    }
                }

                }
                return estimate_array;
                } else return null;
    }

     public void allMeasurementUpdate(ArrayList a) {

        double gain = 0.8;
        ArrayList ali = a;
        if (ali != null) {
            for (int l = 0; l < ali.size(); l++) {
                 Hashtable tempHTable = (Hashtable)ali.get(l);
                 if(tempHTable!=null){
                  for(int scan_rows = 1; scan_rows <= tempHTable.size();scan_rows++){
                      boolean flag = tempHTable.containsKey(new Integer(1));
                      if(flag==true){
                        String s = ((Vector)tempHTable.get(new Integer(scan_rows))).elementAt(0).toString();
                        String c = ((Vector)tempHTable.get(new Integer(scan_rows))).elementAt(1).toString();
                        String sc = ((Vector)tempHTable.get(new Integer(scan_rows))).elementAt(2).toString();
                        long day_value = new Long(((Vector) tempHTable.get(new Integer(scan_rows))).elementAt(6).toString()).longValue();
                        Hashtable hTable = getHashtable(s, c, sc);
                        if (hTable != null) {
                          if (estimate_array != null) {
                            for (int n = 0; n < estimate_array.size(); n++) {
                                if ((((Vector) estimate_array.get(n)).elementAt(0).toString()).equals(s) &&
                                    (((Vector) estimate_array.get(n)).elementAt(1).toString()).equals(c) &&
                                    (((Vector) estimate_array.get(n)).elementAt(2).toString()).equals(sc)) {
                                    long day_val = new Long(((Vector) estimate_array.get(n)).elementAt(3).toString()).longValue();
                                    if(day_value == day_val) {
                                      double apri_estimate = new Double(((Vector) estimate_array.get(n)).elementAt(5).toString()).doubleValue();
                                      double act_value = new Double(((Vector) tempHTable.get(new Integer(scan_rows))).elementAt(5).toString()).doubleValue();
                                      if (apri_estimate != -1 && act_value != -1) {
                                        double apost = 0;
                                        double error = act_value - apri_estimate;
                                        //System.out.println("Error is  " + error + " Supplier " + s + " Customer " + c + " Supply Class " + sc);
                                        if (error < 0) {
                                            apost = apri_estimate - (gain * Math.abs(error));
                                            //System.out.println("Aposterior Estimate is " + apost + " Supplier " + s + " Customer " + c + " Supply Class " + sc);
                                        } else {
                                            apost = apri_estimate + (gain * Math.abs(error));
                                            //System.out.println("Aposterior Estimate is " + apost + " Supplier " + s + " Customer " + c + " Supply Class " + sc);
                                        }
                                        try {
                                            pr = new PrintWriter(new BufferedWriter(new FileWriter(s + "error" + ".txt", true)));
                                            pr.print(s);
                                            pr.print(",");
                                            pr.print(c);
                                            pr.print(",");
                                            pr.print(sc);
                                            pr.print(",");
                                            pr.print(day_val);
                                            pr.print(",");
                                            pr.print(apri_estimate);
                                            pr.print(",");
                                            pr.print(apost);
                                            pr.print(",");
                                            pr.print(error);
                                            pr.println();
                                            pr.close();
                                        } catch (Exception e) {
                                            System.out.println(e);
                                        }
                                        for (int i = 1; i <= hTable.size(); i++) {
                                          Vector vl = (Vector) hTable.get(new Integer(i));
                                          //if(new Long(vl.elementAt(3).toString()).longValue() == last_day) {
                                          //long curr_day = new Long(((Vector) tempHTable.get(new Integer(1))).elementAt(6).toString()).longValue();
                                          if (new Long(vl.elementAt(3).toString()).longValue() == day_value) {
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
        }
        }
        estimate_array.clear();
        } else
            return;
    }   */

    private ArrayList alt;
    private ArrayList estimate_array = new ArrayList();
    PrintWriter pr;
}