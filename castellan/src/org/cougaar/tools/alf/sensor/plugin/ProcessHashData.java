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

import org.cougaar.planning.ldm.plan.Task;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;


public class ProcessHashData {

    public ProcessHashData(PredictorArrayList1 al) {

        this.arrayList = al;
    }

    public PredictorArrayList iterateList() {
        int z = -1;
        //String supplier = ((Vector)((Hashtable)arrayList.get(0)).get(new Integer(1))).elementAt(0).toString();
        hashTableList = new PredictorArrayList("123-MSB",arrayList.size());
        for (int j = 0; j < arrayList.size(); j++) {
            //String customer = ((Vector)((Hashtable)arrayList.get(j)).get(new Integer(1))).elementAt(1).toString();
            String customer = ((Vector)((Hashtable)arrayList.get(j)).get(new Integer(1))).elementAt(0).toString();
            int i = 0;
            table = (Hashtable) arrayList.get(j);
            if (table != null) {
                table.remove(new Integer(1));
                Collection c = table.values();
                if (!c.isEmpty()) {
                    int size = c.size();
                    //Object[][] ob = new Object[size][8];
                    Object[][] ob = new Object[size][5];
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
                        PredictorArrayList1 al = demandPerDay(ob);
                        for(int a=0; a<al.size();a++){
                            Hashtable ht = (Hashtable) al.get(a);
                       /* String name = null;
                        for (int m = 1; m < 2; m++) {
                            Vector pr = (Vector) ht.get(new Integer(m));
                            String item_pr = pr.elementAt(5).toString();
                            StringTokenizer st = new StringTokenizer(item_pr);
                            StringBuffer new_token = new StringBuffer();
                            String a_new_token = null;
                            while (st.hasMoreTokens()) {
                                String token = st.nextToken();
                                a_new_token = new_token.append(token).toString();
                            }
                            StringBuffer sb = new StringBuffer().append(pr.elementAt(0).toString()).
                                    append(pr.elementAt(1).toString()).append(pr.elementAt(2).toString()).
                                    append(a_new_token);
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
                                pwr.print(",");
                                pwr.print(pr.elementAt(5));
                                pwr.println();
                            }
                            pwr.close();
                        } catch (Exception e) {
                            System.out.println(e);
                        }*/
                        if (!ht.isEmpty()) {
                            z = z + 1;
                            hashTableList.add(z, ht);
                        }
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

    public PredictorArrayList1 sortItemType(Object[][] ob){
        //long start = System.currentTimeMillis();
        //System.out.println(" sortItemType start of for loop in sort function " +  new Date(start));
        PredictorArrayList1 item_arraylist = new PredictorArrayList1();
        //int k = 7;
        int k = 4;
        Vector item_vector = new Vector();
        for(int i = 0; i < ob.length; i++) {
               if(item_vector.isEmpty()) {
                    item_vector.insertElementAt(ob[i][k],0);
               } else if(item_vector.contains(ob[i][k])==false) {
                    item_vector.insertElementAt(ob[i][k],item_vector.size());
               } else
                     continue;
        }

        for(int j=0; j<item_vector.size();j++){
            Hashtable item_hashtable = new Hashtable();
            int count = 0;
            for(int n=0; n < ob.length; n++){
               if((item_vector.elementAt(j)).toString().equalsIgnoreCase((ob[n][k]).toString())==true) {
                   Vector temp_vector = new Vector();
                   count++;
                   temp_vector.insertElementAt(ob[n][0],0);
                   temp_vector.insertElementAt(ob[n][1],1);
                   temp_vector.insertElementAt(ob[n][2],2);
                   temp_vector.insertElementAt(ob[n][3],3);
                   temp_vector.insertElementAt(ob[n][4],4);
                   //temp_vector.insertElementAt(ob[n][5],5);
                   //temp_vector.insertElementAt(ob[n][6],6);
                   //temp_vector.insertElementAt(ob[n][7],7);
                   item_hashtable.put(new Integer(count),temp_vector);
            }  else
                   continue;
        }
         if(item_hashtable!= null){
         item_arraylist.add(item_arraylist.size(),item_hashtable);
         } else
             return null;
    }
       //long end = System.currentTimeMillis();
    //System.out.println("sortItemType End of for loop in sort function " +  new Date(end)
                      // + " total time to sort in milliseconds" +
                      // (end - start));
       return item_arraylist;
    }


  public void loraSort(Object[] ob) {
    //System.out.println("Collection Size: " + ob.length);
    //long start = System.currentTimeMillis();
    //System.out.println(" LORA SORT start of for loop in sort function " +  new Date(start));
    Arrays.sort(ob, new Comparator () {
      public int compare (Object a, Object b) {
        //Long end1 = (Long)((Object[]) a)[6];
        //Long end2 = (Long)((Object[]) b)[6];
          Long end1 = (Long)((Object[]) a)[3];
          Long end2 = (Long)((Object[]) b)[3];
        if (end1.longValue() < end2.longValue()) return -1;
        if (end1.longValue() > end2.longValue()) return +1;
        return 0;
      }});
    //long end = System.currentTimeMillis();
    //System.out.println("LORA SORT End of for loop in sort function " +  new Date(end)
                       //+ " total time to sort in milliseconds" +
                       //(end - start));
  }

  public void sort(Object[][] ob) {
        //int k = 6;
        int k = 3;
        int j = 0;
      //System.out.println("Collection Size: " + ob.length);
      //long start = System.currentTimeMillis();
      //System.out.println(" start of for loop in sort function " +  new Date(start));
        for (int i = 0; i < ob.length; i++) {
            for (j = i + 1; j < ob.length; j++) {
                if (new Long(ob[i][k].toString()).longValue() > new Long(ob[j][k].toString()).longValue()) {
                  // pull this out of inner j loop, constant check
                    //for (int x = 3; x <= 6; x++) {
                        //if (x == 5) {
                      for (int x = 2; x <= 3; x++) {
                        if (x == 2) {
                            // make this a Double or Object
                            double temp2 = new Double(ob[i][x].toString()).doubleValue();
                            ob[i][x] = ob[j][x];
                            ob[j][x] = new Double(temp2);
                        } else {
                          // ditto here
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
      //long end = System.currentTimeMillis();
      //System.out.println(" End of for loop in sort function " +  new Date(end) + " total time to sort in minutes " +
                       // ((end - start)/60000));
    }


    public PredictorArrayList1 demandPerDay(Object[][] ob) {
        PredictorArrayList1 ret_arraylist = new PredictorArrayList1();
        if (ob != null) {
            // sort(ob);
          loraSort(ob);
        /*    String name = null;
            for (int m = 1; m < 2; m++) {
                String s = ob[m][0].toString();
                String c = ob[m][1].toString();
                String sc = ob[m][2].toString();
                StringBuffer sb = new StringBuffer().append(s).
                        append(c).append(sc).append("1");
                name = sb.toString();
            }  */
            PredictorArrayList1 item_array = sortItemType(ob);
            for(int m=0; m < item_array.size();m++ ){
                Hashtable item_ht = (Hashtable)item_array.get(m);
                int size = item_ht.size();
                if(size > 0){
                //Object[][] item_ob = new Object[size][8];
                  Object[][] item_ob = new Object[size][5];
                for(int n=1; n <=size; n++){
                    item_ob[n-1][0] = ((Vector)item_ht.get(new Integer(n))).elementAt(0);
                    item_ob[n-1][1] = ((Vector)item_ht.get(new Integer(n))).elementAt(1);
                    item_ob[n-1][2] = ((Vector)item_ht.get(new Integer(n))).elementAt(2);
                    item_ob[n-1][3] = ((Vector)item_ht.get(new Integer(n))).elementAt(3);
                    item_ob[n-1][4] = ((Vector)item_ht.get(new Integer(n))).elementAt(4);
                    //item_ob[n-1][5] = ((Vector)item_ht.get(new Integer(n))).elementAt(5);
                    //item_ob[n-1][6] = ((Vector)item_ht.get(new Integer(n))).elementAt(6);
                    //item_ob[n-1][7] = ((Vector)item_ht.get(new Integer(n))).elementAt(7);
                }
            if(item_ob.length > 0){
            //call the arraylist, take each hashtable convert to an array, process it, add to the hashtable and loop again
            newTable = new Hashtable();
            double sum_var = 0;
            //int k = 6;
            int k = 3;
            int x = 0;
            int i = 0;
            for (int j = i + 1; j < item_ob.length; j++) {
                if (new Long(item_ob[j][k].toString()).longValue() > new Long(item_ob[i][k].toString()).longValue()) {
                    for (x = j - 1; x >= i; x--) {
                        //double var = new Double(item_ob[x][5].toString()).doubleValue();
                        double var = new Double(item_ob[x][2].toString()).doubleValue();
                        sum_var = sum_var + var;
                        if (x == 0) {
                            break;
                        }
                    }

                    vec1 = new Vector();
                    //vec1.insertElementAt(item_ob[x][0], 0);
                    //vec1.insertElementAt(item_ob[x][1], 1);
                    //vec1.insertElementAt(item_ob[x][2], 2);
                    //vec1.insertElementAt(item_ob[i][6], 3);
                    //vec1.insertElementAt(new Double(sum_var), 4);
                    //vec1.insertElementAt(item_ob[x][7], 5);
                    vec1.insertElementAt(item_ob[x][0], 0);
                    vec1.insertElementAt(item_ob[x][1], 1);
                    vec1.insertElementAt(item_ob[x][3], 2);
                    vec1.insertElementAt(new Double(sum_var), 3);
                    vec1.insertElementAt(item_ob[x][4], 4);
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
              ret_arraylist.add(m,newTable);
            }
                }
            }

          return ret_arraylist;
        }

      return null;
    }


  private PredictorArrayList hashTableList;
  private PredictorArrayList1 arrayList;
  private Hashtable table;
  private Hashtable newTable;
  private Vector vec1;
  PrintWriter pwr;
}
