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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;


public class CreateHashtable {


    public CreateHashtable(String supplier, String customer, String supply_class) {

        this.Supplier = supplier;
        this.Customer = customer;
        this.Supply_Class = supply_class;
    }

     public CreateHashtable(String supplier, String customer, String supply_class, String item) {

        this.Supplier = supplier;
        this.Customer = customer;
        this.Supply_Class = supply_class;
        this.Item = item;
    }

    public CreateHashtable(String supplier, String customer, String supply_class, String item, long exe_time, long comit_day, long time, double quantity) {

        this.Supplier = supplier;
        this.Customer = customer;
        this.Supply_Class = supply_class;
        this.Item = item;
        this.Exe_time = exe_time;
        this.Commit_day = comit_day;
        this.Time = time;
        this.Quantity = quantity;
    }


    public void setHT() {

        hashtable = new Hashtable();
        Vector vec = new Vector();
        vec.insertElementAt(Supplier, 0);
        vec.insertElementAt(Customer, 1);
        vec.insertElementAt(Supply_Class, 2);
        hashtable.put(new Integer(1), vec);

        if (hashtable.containsKey(new Integer(1)) == true) {
            for (Enumeration e = hashtable.elements(); e.hasMoreElements();) {
                Vector temp = (Vector) e.nextElement();
                for (int i = 0; i < temp.size(); i++) {
                    //Object temp1[] = temp.toArray();
                }
            }
        } else {
            System.out.println("No Hashtable created");
        }

    }

    public Hashtable setSupplyHT() {

        hashtable = new Hashtable();
        Vector vec = new Vector();
        vec.insertElementAt(Supplier, 0);
        vec.insertElementAt(Customer, 1);
        vec.insertElementAt(Supply_Class, 2);
        vec.insertElementAt(Item, 3);
        vec.insertElementAt(new Long(Exe_time), 4);
        vec.insertElementAt(new Long(Commit_day), 5);
        vec.insertElementAt(new Long(Time), 6);
        vec.insertElementAt(new Double(Quantity), 7);

        hashtable.put(new Integer(1), vec);

        if (hashtable.containsKey(new Integer(1)) == true) {
            for (Enumeration e = hashtable.elements(); e.hasMoreElements();) {
                Vector temp = (Vector) e.nextElement();
                for (int i = 0; i < temp.size(); i++) {
                    //Object temp1[] = temp.toArray();
                }
            }

            return hashtable;
        } else {
            System.out.println("No Hashtable created");
        }

        return null;
    }


    public Vector getHT() {

        Vector vec1 = new Vector();
        vec1.insertElementAt(Supplier, 0);
        vec1.insertElementAt(Customer, 1);
        vec1.insertElementAt(Supply_Class, 2);

        return vec1;
    }

     public Vector getItemHT() {

        Vector vec1 = new Vector();
        vec1.insertElementAt(Supplier, 0);
        vec1.insertElementAt(Customer, 1);
        vec1.insertElementAt(Supply_Class, 2);
        vec1.insertElementAt(Item, 3);

        return vec1;
    }

    public Vector getSupplyHT() {

        Vector vec1 = new Vector();
        vec1.insertElementAt(Supplier, 0);
        vec1.insertElementAt(Customer, 1);
        vec1.insertElementAt(Supply_Class, 2);
        vec1.insertElementAt(Item, 3);
        vec1.insertElementAt(new Long(Exe_time), 4);
        vec1.insertElementAt(new Long(Commit_day), 5);
        vec1.insertElementAt(new Long(Time), 6);
        vec1.insertElementAt(new Double(Quantity), 7);
        return vec1;
    }

    public Hashtable returnHT() {

        if (hashtable != null) {
            return hashtable;
        }
        return null;
    }

    protected Hashtable hashtable;
    protected String Supplier;
    protected String Customer;
    protected String Supply_Class;
    protected long Exe_time;
    protected long Time;
    protected double Quantity;
    protected String Item;
    protected long Commit_day;
}

