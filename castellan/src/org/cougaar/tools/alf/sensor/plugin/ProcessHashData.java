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
import java.util.*;


public class ProcessHashData {

    public ProcessHashData(ArrayList al) {

        this.arrayList = al;
    }

    public PredictorArrayList iterateList() {
        int z = -1;
        String supplier = ((Vector)((Hashtable)arrayList.get(0)).get(new Integer(1))).elementAt(0).toString();
        hashTableList = new PredictorArrayList(arrayList.size());
        for (int j = 0; j < arrayList.size(); j++) {
            String customer = ((Vector)((Hashtable)arrayList.get(j)).get(new Integer(1))).elementAt(1).toString();
            int i = 0;
            table = (Hashtable) arrayList.get(j);
            if (table != null) {
                table.remove(new Integer(1));
                Collection c = table.values();
                if (!c.isEmpty()) {
                    int size = c.size();
                    Object[][] ob = new Object[size][7];
                    boolean flag = true;
                    for (Iterator iter = c.iterator(); iter.hasNext();) {
                        if (i == 0 && flag == true) {
                            Vector vec = (Vector) iter.next();
                            for (int k = 0; k < vec.size(); k++) {
                                ob[i][k] = vec.get(k);
                            }
                            flag = false;
                        } else {
                            i = i + 1;
                            Vector vec = (Vector) iter.next();
                            for (int k = 0; k < vec.size(); k++) {
                                ob[i][k] = vec.get(k);
                            }
                        }
                    }
                    if (ob != null) {
                        Hashtable ht = demandPerDay(ob);
                        String name = null;
                        for (int m = 1; m < 2; m++) {
                            Vector pr = (Vector) ht.get(new Integer(m));
                            StringBuffer sb = new StringBuffer().append(pr.elementAt(0).toString()).
                                    append(pr.elementAt(1).toString()).append(pr.elementAt(2).toString());
                            name = sb.toString();
                        }
                        try {
                            pwr = new PrintWriter(new BufferedWriter(new FileWriter(name + ".txt", false)));
                            for (int n = 1; n < ht.size(); n++) {
                                Vector pr = (Vector) ht.get(new Integer(n));
                                pwr.print(pr.elementAt(0));
                                pwr.print(",");
                                pwr.print(pr.elementAt(1));
                                pwr.print(",");
                                pwr.print(pr.elementAt(2));
                                pwr.print(",");
                                pwr.print(pr.elementAt(3));
                                pwr.print(",");
                                pwr.print(pr.elementAt(4));
                                pwr.println();
                            }
                            pwr.close();
                        } catch (Exception e) {
                            System.out.println(e);
                        }
                        if (!ht.isEmpty()) {
                            z = z + 1;
                            hashTableList.add(z, ht);
                        }
                    }
                    //System.out.print("A");
                }
            }
        }
        if (hashTableList.isEmpty()) {
            return null;
        } else
            return hashTableList;
    }


    public void sort(Object[][] ob) {
        int k = 6;
        int j = 0;
        //System.out.println("Collection Size: " + ob.length);
        for (int i = 0; i < ob.length; i++) {
            for (j = i + 1; j < ob.length; j++) {
                if (new Long(ob[i][k].toString()).longValue() > new Long(ob[j][k].toString()).longValue()) {
                    for (int x = 3; x <= 6; x++) {
                        if (x == 5) {
                            double temp2 = new Double(ob[i][x].toString()).doubleValue();
                            ob[i][x] = ob[j][x];
                            ob[j][x] = new Double(temp2);
                        } else {
                            long temp2 = new Long(ob[i][x].toString()).longValue();
                            ob[i][x] = ob[j][x];
                            ob[j][x] = new Long(temp2);
                        }
                    }
                } else {
                    continue;
                }
            }
        }
    }


    public Hashtable demandPerDay(Object[][] ob) {
        if (ob != null) {
            sort(ob);
        /*    String name = null;
            for (int m = 1; m < 2; m++) {
                String s = ob[m][0].toString();
                String c = ob[m][1].toString();
                String sc = ob[m][2].toString();
                StringBuffer sb = new StringBuffer().append(s).
                        append(c).append(sc).append("1");
                name = sb.toString();
            } */

            newTable = new Hashtable();
            double sum_var = 0;
            int k = 6;
            int x = 0;
            int i = 0;
            for (int j = i + 1; j < ob.length; j++) {
                if (new Long(ob[j][k].toString()).longValue() > new Long(ob[i][k].toString()).longValue()) {
                    for (x = j - 1; x >= i; x--) {
                        double var = new Double(ob[x][5].toString()).doubleValue();
                        sum_var = sum_var + var;
                        if (x == 0) {
                            break;
                        }
                    }

                    vec1 = new Vector();
                    vec1.insertElementAt(ob[x][0], 0);
                    vec1.insertElementAt(ob[x][1], 1);
                    vec1.insertElementAt(ob[x][2], 2);
                    vec1.insertElementAt(ob[i][6], 3);
                    vec1.insertElementAt(new Double(sum_var), 4);
                    sum_var = 0;
                    if (newTable.isEmpty()) {
                        newTable.put(new Integer(1), vec1);
                    } else {
                        newTable.put(new Integer((newTable.size() + 1)), vec1);
                    }

                    i = j;

                } else {
                    continue;
                }
            }

            return newTable;
        }

        return null;
    }


    private PredictorArrayList hashTableList;
    private ArrayList arrayList;
    private Hashtable table;
    private Hashtable newTable;
    private Vector vec1;
    PrintWriter pwr;
}
