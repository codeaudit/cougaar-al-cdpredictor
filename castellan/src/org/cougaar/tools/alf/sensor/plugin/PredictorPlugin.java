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

import org.cougaar.core.adaptivity.InterAgentCondition;
import org.cougaar.core.agent.service.alarm.PeriodicAlarm;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.plugin.ComponentPlugin;
import org.cougaar.core.service.*;
import org.cougaar.core.util.UID;
import org.cougaar.glm.ldm.Constants;
import org.cougaar.logistics.plugin.inventory.TaskUtils;
import org.cougaar.logistics.servlet.CommStatus;
import org.cougaar.planning.ldm.PlanningFactory;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.plan.*;
import org.cougaar.planning.service.LDMService;
import org.cougaar.util.UnaryPredicate;
import org.cougaar.tools.alf.sensor.plugin.KalmanFilter;
import org.cougaar.tools.alf.sensor.plugin.SupplyDataUpdate;

import java.util.*;

/**
 *	programed by Himanshu Gupta
 *	June 10, 2003
 *	PSU-IAI
 **/

public class PredictorPlugin extends ComponentPlugin {

    public class TriggerFlushAlarm implements PeriodicAlarm {

        public TriggerFlushAlarm(long expTime) {
            this.expTime = expTime;
        }

        public void reset(long currentTime) {
            expTime = currentTime + delay;
            expired = false;
        }

        public long getExpirationTime() {
            return expTime;
        }

        public void expire() {
            expired = true;
            //myBS.openTransaction();
            //callPredictor();
            myBS.signalClientActivity();
            //myBS.closeTransaction();
            cancel();
        }

        public boolean hasExpired() {
            return expired;
        }

        public boolean cancel() {
            boolean was = expired;
            expired = true;
            return was;
        }

        boolean expired = false;
        long expTime;
        long delay = 86400000;
    };

    UnaryPredicate taskPredicate = new UnaryPredicate() {
        public boolean execute(Object o) {
            return o instanceof Task;
        }
    };

    UnaryPredicate servletPredicate = new UnaryPredicate() {
        public boolean execute(Object o) {
            return o instanceof InterAgentCondition;
        }

    };

    UnaryPredicate arrayListPredicate = new UnaryPredicate() {
        public boolean execute(Object o) {
            return o instanceof PredictorArrayList;
        }
    };

    UnaryPredicate supplyArrayListPredicate = new UnaryPredicate() {
        public boolean execute(Object o) {
            return o instanceof PredictorSupplyArrayList;
        }
    };

    UnaryPredicate commstatusPredicate = new UnaryPredicate() {
        public boolean execute(Object o) {
            return o instanceof CommStatus;
        }
    };

    public void setupSubscriptions() {

        cluster = ((AgentIdentificationService) getBindingSite().getServiceBroker().getService(this, AgentIdentificationService.class, null)).getName();

        myBS = getBlackboardService();
        myDomainService = (DomainService) getBindingSite().getServiceBroker().getService(this, DomainService.class, null);
        myLoggingService = (LoggingService) getBindingSite().getServiceBroker().getService(this, LoggingService.class, null);
        servletSubscription = (IncrementalSubscription) myBS.subscribe(servletPredicate);

        as = (AlarmService) getBindingSite().getServiceBroker().getService(this, AlarmService.class, null);

        if (selectedPredictor == KalmanFilter) {
            taskSubscription = (IncrementalSubscription) myBS.subscribe(taskPredicate);
            salSubscription = (IncrementalSubscription) myBS.subscribe(supplyArrayListPredicate);
            arrayListSubscription = (IncrementalSubscription) myBS.subscribe(arrayListPredicate);
            commstatusSubscription = (IncrementalSubscription) myBS.subscribe(commstatusPredicate);
            if (flagger == false) {
                if (!taskSubscription.isEmpty()) {
                    taskSubscription.clear();
                }
                if (!servletSubscription.isEmpty()) {
                    servletSubscription.clear();
                }
                flagger = true;
            }
            if (myBS.didRehydrate() == false) {
                //local_alist = new PredictorSupplyArrayList();
                //myBS.publishAdd(local_alist);
                myBS.setShouldBePersisted(false);
            }else {
                 rehydrate_flag = true;
                 retrievesalfromBB();
                 if(arraylist == null) {
                   retrievearraylistfromBB();
                }
            }

        } else
        {
            //Yunho's code
        }
    }

    public void retrievesalfromBB(){
        if(local_alist == null){
            //myLoggingService.shout("local_alist is null");
            Collection c = myBS.query(supplyArrayListPredicate);
            for(Iterator iter = c.iterator();iter.hasNext() ;){
                local_alist = (PredictorSupplyArrayList) iter.next();
                //myLoggingService.shout("local_alist added"+local_alist.size());
            }
            /* Collection c1 = salSubscription.getAddedCollection();
            Collection c2 = salSubscription.getChangedCollection();
            for(Iterator iter = c1.iterator();iter.hasNext() ;){
                local_alist = (PredictorSupplyArrayList) iter.next();
                myLoggingService.shout("local_alist added"+local_alist.size());
            }
            for(Iterator iter = c2.iterator();iter.hasNext() ;){
                local_alist = (PredictorSupplyArrayList) iter.next();
                myLoggingService.shout("local_alist changed"+local_alist.size());
            }  */
        }
    }

     public void retrievearraylistfromBB(){
        if(arraylist == null){
            //myLoggingService.shout("arraylist is null");
            flag = false;
            Collection c = myBS.query(arrayListPredicate);
            for(Iterator iter = c.iterator();iter.hasNext() ;){
                arraylist = (PredictorArrayList) iter.next();
                //myLoggingService.shout("PredictorArraylist added"+arraylist.size());
                if(arraylist.size()>1){
                    kf = new KalmanFilter(arraylist);
                    myBS.publishAdd(kf);
                    flag = true;
                    //myLoggingService.shout("Flag set to true");
                }
            }
           /* Collection c1 = arrayListSubscription.getAddedCollection();
            for(Iterator iter = c1.iterator();iter.hasNext() ;){
                arraylist = (PredictorArrayList) iter.next();
                myLoggingService.shout("PredictorArraylist added"+arraylist.size());
                if(arraylist.size()>1){
                    kf = new KalmanFilter(arraylist);
                    myBS.publishAdd(kf);
                    flag = true;
                }
            }  */
        }
    }

    public void callPredictor() {

        if (selectedPredictor == KalmanFilter) {
            //myLoggingService.shout("Local_alist size: " + local_alist.size());
            whilePredictorON1();
        } else {
            //Yunho's code
        }
    }

    public void execute() {
         if(selectedPredictor == KalmanFilter) {
            if(flag == false && rehydrate_flag == false){
                checkArrayListSubscription();
                //checkArrayListSubscription1();
            }
            else if(flag == false && rehydrate_flag == true){
                checkArrayListSubscription();
                //checkArrayListSubscription1();
            }
            else if(flag == true && rehydrate_flag == true)
            {
                //myLoggingService.shout("Rehydration Occurred in PredictorPlugin");
            }
            if (flag == true && !relay_added == true)
            {
                if(alarm!= null && alarm.hasExpired()== true){
                    callPredictor();
                    //myLoggingService.shout("Alarm fired in PredictorPlugin execute");
                }
                checkCommStatusSubscription();
                getActualDemand();
            }
            else
            {
                return;
            }
        }
        else
        {
            //Yunho's code
        }
    }

    public void checkArrayListSubscription() {
        for (Enumeration et = arrayListSubscription.getAddedList(); et.hasMoreElements();)
        {
            arraylist = (PredictorArrayList) et.nextElement();
            if (arraylist!= null)
            {
                myLoggingService.debug("Demand Model Received by agent " + cluster);
                myLoggingService.debug("Model size: "+arraylist.size());
                kf = new KalmanFilter(arraylist);
                myBS.publishAdd(kf);
                flag = true;
            }
            checkServletSubscription();
        }
    }

  /*   public void checkArrayListSubscription1() {
        for (Enumeration et = arrayListSubscription.getChangedList(); et.hasMoreElements();)
        {
            arraylist = (PredictorArrayList) et.nextElement();
            if (arraylist!= null)
            {
                myLoggingService.shout("Demand Model Received by agent changed " + cluster);
                myLoggingService.shout("Model size: "+arraylist.size());
                kf = new KalmanFilter(arraylist);
                myBS.publishAdd(kf);
                flag = true;
            }
            checkServletSubscription();
        }
    }    */

    public void checkServletSubscription() {
        if (!servletSubscription.isEmpty())
        {
            Enumeration e1 = servletSubscription.getAddedList();
            Enumeration e2 = servletSubscription.getChangedList();
            if (e1.hasMoreElements() == true)
            {
                getServletStatusObject(e1);
            } else if (e2.hasMoreElements() == true)
            {
                getServletStatusObject(e2);
            }
        }
    }

    public void getServletStatusObject(Enumeration ent) {
        InterAgentCondition tr = (InterAgentCondition) ent.nextElement();
        String content = tr.getContent().toString();
        setAlgorithmSelection(content);
        String f = null;
        if (content.equalsIgnoreCase("Servlet_Relay = 0") == true)
        {
            f = "OFF";
        } else if (content.equalsIgnoreCase("Servlet_Relay = 1") == true)
        {
            f = "SLEEP";
        } else if (content.equalsIgnoreCase("Servlet_Relay = 2") == true)
        {
            f = "ON";
        }
        if (f != null && f == "OFF")
        {
            relay_added = true;
            myBS.publishChange(tr);
        }
        else if (f != null && f == "SLEEP")
        {
            relay_added = false;
            myBS.publishChange(tr);
        }
        else if (f != null && f == "ON")
        {
            count++;
            if ((Math.IEEEremainder(count, 2.0) != 0))
            {
                relay_added = true;
                alarm = new TriggerFlushAlarm(currentTimeMillis());
                as.addAlarm(alarm);
                myBS.publishChange(tr);
            }
            else
            {
                relay_added = false;
                myBS.publishChange(tr);
            }
        }
    }

    public void checkCommStatusSubscription() {
        if (!commstatusSubscription.isEmpty())
        {
            Enumeration e1 = commstatusSubscription.getAddedList();
            Enumeration e2 = commstatusSubscription.getChangedList();
            if (e1.hasMoreElements() == true)
            {
                getCommStatusObject(e1);
            } else if (e2.hasMoreElements() == true)
            {
                getCommStatusObject(e2);
            }
        }
    }

     public void checkTaskSubscription() {
        if (!taskSubscription.isEmpty())
        {
            //Task task;
            Enumeration e1 = taskSubscription.getAddedList();
            Enumeration e2 = taskSubscription.getChangedList();
            if (e1.hasMoreElements() == true)
            {
              //task = (Task) e1.nextElement();
              //getActualDemand(task);
            } else if (e2.hasMoreElements() == true)
            {
                //task = (Task) e2.nextElement();
                //getActualDemand(task);
            }
        }
         else
        {
            return;
        }
    }


    public void getCommStatusObject(Enumeration e) {
        CommStatus cs = (CommStatus) e.nextElement();
        customerAgentName = cs.getConnectedAgentName();
        status = cs.isCommUp();
        //myLoggingService.shout("Communication status is: " + status);
        if (status == false)
        {
            commLossTime = cs.getCommLossTime();
            //System.out.println("Comm. Loss Time is: " + commLossTime);
            myLoggingService.debug("Communication Lost with Customer: " + customerAgentName);
            getActualDemand();
            PredictorSupplyArrayList total_qty_alist = sd.returnDemandQuantity1();
            //Collection c = new PredictorSupplyArrayList();
            //c = total_qty_alist;
            //local_alist = new PredictorSupplyArrayList(c);
            local_alist = total_qty_alist;
            myBS.publishChange(local_alist);
            //myLoggingService.shout("local_alist size in get actualdemand method " + local_alist.size());
            alarm = new TriggerFlushAlarm(currentTimeMillis());
            as.addAlarm(alarm);
            //myLoggingService.shout("Comm. Loss Alarm Added");
            comm_count++;
        } else
        {
            if (comm_count == 1)
            {
                commRestoreTime = cs.getCommRestoreTime();
                time_gap = (int) (((commRestoreTime - commLossTime)/86400000)*4);
                if(alarm.hasExpired()== false) {
                    alarm.cancel();
                    //alarm = new TriggerFlushAlarm(currentTimeMillis());
                    //as.addAlarm(alarm);
                    //comm_restore_flag = true;
                    myLoggingService.debug("Communication Re-Established with Customer: " + customerAgentName);
                    comm_count = 0;
                }
            }
        }
    }

    public void getActualDemand() {
        Task task;
        if(status == false){
            //myLoggingService.shout("status is false before getActualDemand");
        }
        for (Enumeration e = taskSubscription.getAddedList(); e.hasMoreElements();) {
            task = (Task) e.nextElement();
            if (task != null) {
                String verb = task.getVerb().toString();
                if (verb != null) {
                    if (verb.equalsIgnoreCase("Supply") == true) {
                        String owner = (String) task.getPrepositionalPhrase("For").getIndirectObject();
                        if (owner != null && owner.equalsIgnoreCase("47-FSB")== true) {
                            if(!TaskUtils.isMyRefillTask(task, cluster)){
                            String pol = (String) task.getPrepositionalPhrase("OfType").getIndirectObject();
                                String uid = task.getUID().toString();
                            if (owner.equalsIgnoreCase(cluster) == false) {
                                String comp = stringManipulation(pol);
                                if (comp != null) {
                                    Asset as = task.getDirectObject();
                                    if (as != null) {
                                        Hashtable assetlist = storeAsset(as);
                                        if(assetlist.isEmpty()){
                                            myBS.publishAdd(assetlist);
                                        } else{
                                            myBS.publishChange(assetlist);
                                        }
                                        String item_name = as.getTypeIdentificationPG().getNomenclature();
                                        long ti = currentTimeMillis() / 86400000;
                                        if (ti >= 0) {
                                            if (toggle == false) {
                                                x = ti;
                                                toggle = true;
                                            }
                                            long sTime = (long) (task.getPreferredValue(AspectType.END_TIME)) / 86400000;
                                            double qty = task.getPreferredValue(AspectType.QUANTITY);
                                            long commitment_date = task.getCommitmentDate().getTime() / 86400000;
                                            if (ti != -1 && qty != -1) {
                                                    if (ti == x) {
                                                        if(status == false){
                                                            //myLoggingService.shout("status is false inside getActualDemand");
                                                        }
                                                        sd.getSupplyQuantity(cluster, owner, comp, item_name, ti, commitment_date, sTime, qty, uid);
                                                    } else if (ti > x) {
                                                        sd.returnDemandQuantity(cluster, owner, comp, item_name, ti, commitment_date, sTime, qty, uid);
                                                        if (status == true) {
                                                        PredictorSupplyArrayList total_qty_alist = sd.returnDemandQuantity1();
                                                        sd.getSupplyQuantity(cluster, owner, comp, item_name, ti, commitment_date, sTime, qty, uid);
                                                        count_supplyarraylist++;
                                                        if (count_supplyarraylist == 1) {
                                                            Collection c = new PredictorSupplyArrayList();
                                                            c = total_qty_alist;
                                                            local_alist = new PredictorSupplyArrayList(c);
                                                            for(int a =0; a < local_alist.size(); a++){
                                                                Vector local_vector = (Vector) local_alist.get(a);
                                                                String supply_class_name = local_vector.elementAt(2).toString();
                                                                String item_class_name = local_vector.elementAt(3).toString();
                                                                //myLoggingService.shout("Task UID for Supply Class: "+supply_class_name+ " for item: "+ item_class_name+ "is: "+local_vector.elementAt(8).toString());
                                                            }
                                                            //myLoggingService.shout("local_alist size in get actualdemand method " + local_alist.size());
                                                            myBS.publishAdd(local_alist);

                                                        } else {
                                                            //Collection c = new PredictorSupplyArrayList();
                                                            //c = total_qty_alist;
                                                            //local_alist = new PredictorSupplyArrayList(c);
                                                            local_alist = total_qty_alist;
                                                            for(int a =0; a < local_alist.size(); a++){
                                                                Vector local_vector = (Vector) local_alist.get(a);
                                                                String supply_class_name = local_vector.elementAt(2).toString();
                                                                String item_class_name = local_vector.elementAt(3).toString();
                                                                //myLoggingService.shout("Task UID for Supply Class: "+supply_class_name+ " for item: "+ item_class_name+ "is: "+local_vector.elementAt(8).toString());
                                                            }
                                                            //myLoggingService.shout("local_alist size in get actualdemand method1 " + local_alist.size());

                                                            myBS.publishChange(local_alist);

                                                        }
                                                        if (total_qty_alist != null) {
                                                            counter++;
                                                            if (counter > 1) {
                                                                kf.measurementUpdate(total_qty_alist);
                                                                ArrayList prediction_arraylist = kf.timeUpdate(total_qty_alist);
                                                                myBS.publishChange(kf);
                                                                myBS.publishChange(prediction_arraylist);
                                                                x = ti;
                                                            } else {
                                                                ArrayList prediction_arraylist = kf.timeUpdate(total_qty_alist);
                                                                myBS.publishChange(kf);
                                                                myBS.publishAdd(prediction_arraylist);
                                                                x = ti;
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        }
                    }
                }
            }
        }
    }


    public Hashtable storeAsset(Asset as) {
        Asset as1 = as;
        String itemname = as1.getTypeIdentificationPG().getNomenclature();
        if (asset_list.isEmpty()) {
            asset_list.put(new Integer(1), as1);
            return asset_list;
        } else {
            for (int i = 1; i <= asset_list.size(); i++) {
                String test = ((Asset) asset_list.get(new Integer(i))).getTypeIdentificationPG().getNomenclature();
                if (test.equalsIgnoreCase(itemname) == false) {
                    asset_list.put(new Integer(asset_list.size() + 1), as1);
                    break;
                } else
                    continue;
            }
            return asset_list;
        }
    }

    public Asset getAsset(Vector v) {

        for (int i = 1; i <= asset_list.size(); i++) {
            Asset as2 = (Asset) asset_list.get(new Integer(i));
            String item = as2.getTypeIdentificationPG().getNomenclature();
            if (item.equalsIgnoreCase(v.elementAt(2).toString()) == true) {
                return as2;
            } else
                continue;
        }
        return null;
    }

    public long getCustomerLeadTime(long lastSupplyDay, long commLossDay) {
        clt = lastSupplyDay*86400000 - commLossDay;
        return clt;
    }

    public long getPredictorGap(long lastSupplyDay, long clt) {
        gap = currentTimeMillis() + clt - lastSupplyDay*86400000;
        //System.out.println("GAPNOINT "+gap);
        gap = (int) (gap/86400000);
        //System.out.println("GAPWITHINT "+gap);
        //gap = gap*4*86400000;
        gap = gap*86400000;
        return gap;
    }

    public long getOrderShipTime(long lastSupplyDay, long commitmentDay) {
        ost = lastSupplyDay*86400000 - commitmentDay*86400000;
        return ost;
    }

    public void setPredictionCommitmentDate(long lastSupplyDay, long ordershiptime) {
        pcd = lastSupplyDay*86400000 + gap - ordershiptime;
    }

    public long getPredictionCommitmentDate() {
        return pcd;
    }

    public void whilePredictorON1() {
        //if(status == false || comm_restore_flag == true){
        if(status == false){
        if (relay_added == false) {
                   if (alarm!= null) {
                        alarm.cancel();
                    }
                    if(comm_restore_flag == false){
                    //myLoggingService.shout("Comm_restore_flag status in Alarm is: "+comm_restore_flag);
                    alarm = new TriggerFlushAlarm(currentTimeMillis() + 86400000);
                    as.addAlarm(alarm);
                    myBS.publishChange(local_alist);
                    //myLoggingService.shout("PREDICTOR_ALARM_FIRED");
                    //myLoggingService.shout("Predictor_Array_List_1 Size: " + local_alist.size());
                   }
                    //myLoggingService.shout("Comm_restore_flag status is: "+comm_restore_flag);                       //Checks the comm. status loss condition and if the predictor is ON
                    ArrayList final_al = new ArrayList();                                                           // New ArrayList for storing comm. loss time data
                    //myLoggingService.shout("Predictor_Array_List Size: " + local_alist.size());
                    //myLoggingService.shout("Time Gap Value "+time_gap);
                    if (!local_alist.isEmpty()) {                                                                   //Iterate through the latest demand tasks
                        for (int al_iter = 0; al_iter < local_alist.size(); al_iter++) {
                            Vector values_vector = (Vector) local_alist.get(al_iter);
                            String customer_copy = values_vector.elementAt(1).toString();                           //Customer
                            if(customerAgentName.equalsIgnoreCase(customer_copy)==true){
                            String supplyclass_copy = values_vector.elementAt(2).toString();                        //Supply Class
                            String itemname_copy = values_vector.elementAt(3).toString();                           //Item Name

                            long commit_day = new Long(values_vector.elementAt(5).toString()).longValue();          //Commitment Date
                            long last_day = new Long(values_vector.elementAt(6).toString()).longValue();            //Last Supply Date

                            //myLoggingService.shout("Last date value for supplyclass: " + supplyclass_copy + " is " + new Date(last_day * 86400000));

                            long customer_lead_time = getCustomerLeadTime(last_day, commLossTime);                  // Customer Lead Time
                            long pred_gap = getPredictorGap(last_day, customer_lead_time);                          // Predictor Gap
                            long order_ship_time = getOrderShipTime(last_day, commit_day);                          //Order Ship Time
                            setPredictionCommitmentDate(last_day, order_ship_time);

                            double quantity = new Double(values_vector.elementAt(7).toString()).doubleValue();      //Demand Quantity
                            String uid_name = values_vector.elementAt(8).toString();
                            Vector commloss_vec = createVector(customer_copy, supplyclass_copy, itemname_copy, last_day * 86400000, customer_lead_time, pred_gap, order_ship_time, getPredictionCommitmentDate(), quantity);

                            //myLoggingService.shout("UID is: "+uid_name+ " Customer: " + customer_copy + " Supply Class: " + supplyclass_copy
                                  //  + " Item: " + itemname_copy + " Commitment Date: " + new Date(commit_day * 86400000) + " Last Supply Date: "
                                   // + new Date(last_day * 86400000) + " Customer Lead Time: " + customer_lead_time / 86400000 + " No of Prediction" +
                                   // " Gap Days: " + pred_gap/86400000 + " Order Ship Date: " + order_ship_time/86400000+ "Quantity: "+quantity);

                            final_al.add(al_iter, commloss_vec);
                            }
                            else
                            {
                                continue;
                            }
                        }

                        for (int al_iter1 = 0; al_iter1 < final_al.size(); al_iter1++) {
                            Vector arraylist_vector = (Vector) final_al.get(al_iter1);
                            long gap_value = new Long(arraylist_vector.elementAt(5).toString()).longValue();
                            //myLoggingService.shout("GapValue is: "+gap_value/86400000);
                            if(time_gap == -1 || time_gap <= (int)gap_value){
                            if((int) gap_value/86400000 >= 1)  {
                                if((((int) gap_value/86400000) % 4) == 0 || ((int) gap_value/86400000) == 1)  {
                                 String customer = arraylist_vector.elementAt(0).toString();
                                 String supply_class = arraylist_vector.elementAt(1).toString();
                                 String item_class = arraylist_vector.elementAt(2).toString(); //Long parselong to be used in future
                                 long supply_date = new Long(arraylist_vector.elementAt(3).toString()).longValue();
                                 long commitmentday = new Long(arraylist_vector.elementAt(7).toString()).longValue();
                                 double quantity = new Double(arraylist_vector.elementAt(8).toString()).doubleValue();
                                 Vector temp_vector = new Vector();
                                 if(((int) gap_value/86400000) == 1){
                                    temp_vector = vectorForPredictorTask(customer, supply_class, item_class, commitmentday + 3*86400000, supply_date/86400000 + ((int)(gap_value/86400000)) + 3, quantity);
                                 }
                                    else {
                                 //myLoggingService.shout("Quantity before Adding Task: "+quantity);
                                 //Vector temp_vector = vectorForPredictorTask(customer, supply_class, item_class, commitmentday , supply_date/86400000 + (int)(gap_value/86400000), quantity);
                                 temp_vector = vectorForPredictorTask(customer, supply_class, item_class, commitmentday + 4*86400000, supply_date/86400000 + ((int)(gap_value/86400000)) + 4, quantity);
                                 }
                                 //if(supply_class.equalsIgnoreCase("AmmunitionCustomer")== true){
                                    if(temp_vector!= null){
                                 NewTask new_task = getNewTask(temp_vector);

                                 if (new_task!= null)
                                 {
                                      myBS.publishAdd(new_task);
                                      myLoggingService.debug(cluster + ": NEW TASK ADDED PPOUTPUT" + new_task);
                                 }
                                 else
                                 {
                                     myLoggingService.debug(cluster + ": NO TASK COULD BE PUBLISHED " + new_task);
                                 }
                                    }
                           // }
                             //   else {
                             //        continue;
                             //    }
                            }else
                                {
                                    break;
                                }
                            }
                            }
                            else
                            {
                                myLoggingService.debug("GAP HAS ZERO VALUE HENCE NO PREDICTOR TASK");
                            }
                        }
                    }
                    comm_restore_flag = false;
        }
        else
        {
           myLoggingService.debug("COMMUNICATION SERVICE IS ON OR THE PREDICTOR IS TURNED OFF");
        }
        }

    }

    public Vector vectorForPredictorTask(String customer, String supplyclass, String itemname, long commitmentdate, long end_date, double quantity) {
        Vector vector = new Vector();
        vector.insertElementAt(customer, 0);
        vector.insertElementAt(supplyclass, 1);
        vector.insertElementAt(itemname, 2);
        vector.insertElementAt(new Long(commitmentdate), 3);
        vector.insertElementAt(new Long(end_date), 4);
        vector.insertElementAt(new Double(quantity), 5);
        return vector;
    }

    public Vector createVector(String a, String b, String c, long d, long e, long f, long g, long h, double i) {
        Vector genVector = new Vector();
        genVector.insertElementAt(a, 0);
        genVector.insertElementAt(b, 1);
        genVector.insertElementAt(c, 2);
        genVector.insertElementAt(new Long(d), 3);
        genVector.insertElementAt(new Long(e), 4);
        genVector.insertElementAt(new Long(f), 5);
        genVector.insertElementAt(new Long(g), 6);
        genVector.insertElementAt(new Long(h), 7);
        genVector.insertElementAt(new Double(i), 8);
        return genVector;
    }

    public NewTask getNewTask(Vector v) {

        PlanningFactory pf = (PlanningFactory) myDomainService.getFactory("planning");
        NewTask nt = pf.newTask();

        nt.setVerb(forecastVerb);

        NewPrepositionalPhrase npp = pf.newPrepositionalPhrase();
        npp.setPreposition(Constants.Preposition.FOR);
        npp.setIndirectObject(v.elementAt(0));
        nt.addPrepositionalPhrase(npp);

        NewPrepositionalPhrase npp1 = pf.newPrepositionalPhrase();
        npp1.setPreposition(Constants.Preposition.OFTYPE);
        String supplyclass = stringReverseManipulation(v.elementAt(1).toString());
        npp1.setIndirectObject(supplyclass);
        nt.addPrepositionalPhrase(npp1);

        nt.setCommitmentDate(new Date(new Long(v.elementAt(3).toString()).longValue()));

        AspectValue av = AspectValue.newAspectValue(AspectType.END_TIME, new Long(v.elementAt(4).toString()).longValue()*86400000);
        Preference np = pf.newPreference(av.getAspectType(), ScoringFunction.createStrictlyAtValue(av));
        nt.addPreference(np);

        AspectValue av1 = AspectValue.newAspectValue(AspectType.QUANTITY, new Double(v.elementAt(5).toString()).doubleValue());
        Preference np1 = pf.newPreference(av1.getAspectType(), ScoringFunction.createStrictlyAtValue(av1));
        nt.addPreference(np1);

        String sclass = stringPlay(supplyclass);
        Asset asset = getAsset(v);
        if (asset != null) {
            UID uid = asset.getUID();
            long id = uid.getId();
            UID newuid = new UID(v.elementAt(0).toString(), id);
            asset.setUID(newuid);
            nt.setDirectObject(asset);
        }
        return nt;
    }

    public void disposition(NewTask newtask) {
        PlanningFactory pf = (PlanningFactory) myDomainService.getFactory("planning");
        AspectValue av_array[] = new AspectValue[2];
        av_array[0] = AspectValue.newAspectValue(AspectType.END_TIME,
                TaskUtils.getPreference(newtask, AspectType.END_TIME));
        av_array[1] = AspectValue.newAspectValue(AspectType.QUANTITY,
                TaskUtils.getPreference(newtask, AspectType.QUANTITY));
        AllocationResult dispAR =
                pf.newAllocationResult(1.0, false, av_array);
        Disposition disp = pf.createDisposition(newtask.getPlan(), newtask, dispAR);
        myBS.publishAdd(disp);

    }

     public String stringManipulation(String a) {

        String s_class = a;
        if (s_class.compareToIgnoreCase("Ammunition") == 0 || s_class.compareToIgnoreCase("Food") == 0) {
            String s_class1 = s_class.concat("Customer");
            return s_class1;
        }
        if (s_class.compareToIgnoreCase("PackagedPol") == 0 || s_class.compareToIgnoreCase("Subsistence") == 0) {
            String s_class1 = s_class.concat("SupplyCustomer");
            return s_class1;
        }
        if (s_class.compareToIgnoreCase("BulkPol") == 0) {
            String s_class1 = "FuelSupplyCustomer";
            return s_class1;
        }
         if (s_class.compareToIgnoreCase("Consumable") == 0) {
            String s_class1 = "SparePartsCustomer";
            return s_class1;
        }
        return null;
    }

    public String stringReverseManipulation(String a) {

        String s_class = a;
        if (s_class.compareToIgnoreCase("AmmunitionCustomer") == 0) {
            String s_class1 = "Ammunition";
            return s_class1;
        }
        if (s_class.compareToIgnoreCase("PackagedPolSupplyCustomer") == 0) {
            String s_class1 = "PackagedPol";
            return s_class1;
        }
        if (s_class.compareToIgnoreCase("SubsistenceSupplyCustomer") == 0) {
            String s_class1 = "Subsistence";
            return s_class1;
        }
        if (s_class.compareToIgnoreCase("FuelSupplyCustomer") == 0) {
            String s_class1 = "BulkPOL";
            return s_class1;
        }
        if (s_class.compareToIgnoreCase("SparePartsCustomer") == 0) {
            String s_class1 = "Consumable";
            return s_class1;
        }

        return null;
    }

    public String stringPlay(String a) {
        if (a.equalsIgnoreCase("PackagedPol") == true) {
            a = "PackagedPOL";
            return a;
        }
        if (a.equalsIgnoreCase("Subsistence") == true) {
            a = "ClassISubsistence";
            return a;
        } else
            return a;
    }

      public void setAlgorithmSelection(String radio_selection) {
        String content1 = radio_selection;
        if (content1.equalsIgnoreCase("Algorithm_Relay = 1") == true) {
            selectedPredictor = MovingAverage;
            //myLoggingService.shout("Predictor with Moving Average");
        }
        if (content1.equalsIgnoreCase("Algorithm_Relay = 2") == true) {
            selectedPredictor = SupportVectorMachine;
            //myLoggingService.shout("Predictor with Support Vector Machine");
        }
        if (content1.equalsIgnoreCase("Algorithm_Relay = 3") == true) {
            selectedPredictor = KalmanFilter;
            //myLoggingService.shout("Predictor with Kalman Filter");
        }
    }

    private String cluster;
    private LoggingService myLoggingService;
    private DomainService myDomainService;
    LDMService ldms;
    private BlackboardService myBS;
    private IncrementalSubscription arrayListSubscription;
    private IncrementalSubscription taskSubscription;
    private IncrementalSubscription commstatusSubscription;
    private IncrementalSubscription servletSubscription;
    private IncrementalSubscription salSubscription;

    AlarmService as;
    TriggerFlushAlarm alarm = null;
    private ArrayList arraylist = null;
    SupplyDataUpdate sd = new SupplyDataUpdate();
    private KalmanFilter kf = null;
    private boolean relay_added = false;

    private boolean toggle = false;
    private long x = 0;
    private int counter = 0;
    private boolean flag = false;
    private boolean flagger = false;
    double count = 0;

    private final Verb forecastVerb = org.cougaar.logistics.ldm.Constants.Verb.Supply;

    private final int MovingAverage = 1;
    private final int SupportVectorMachine = 2;
    private final int KalmanFilter = 3;
    private int selectedPredictor = KalmanFilter;
    private Hashtable asset_list = new Hashtable();

    boolean changed = false;
    long commLossTime = -1;
    long commRestoreTime = -1;
    boolean status = true;
    long pcd = -1;
    long clt = -1;
    long gap = -1;
    long ost = -1;
    int comm_count = 0;
    int count_supplyarraylist = 0;
    boolean comm_restore_flag = false;
    String customerAgentName;
    //ArrayList retain_alist = new ArrayList();
    PredictorSupplyArrayList local_alist = null;   //change
    boolean rehydrate_flag = false;
    long time_gap = -1;
}
