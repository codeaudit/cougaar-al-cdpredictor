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
import org.cougaar.core.service.AgentIdentificationService;
import org.cougaar.core.service.AlarmService;
import org.cougaar.core.service.BlackboardService;
import org.cougaar.core.service.LoggingService;
//import org.cougaar.core.util.UID;
import org.cougaar.glm.ldm.Constants;
import org.cougaar.glm.ldm.plan.AlpineAspectType;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.measure.CountRate;
import org.cougaar.planning.ldm.measure.FlowRate;
import org.cougaar.planning.ldm.plan.*;
import org.cougaar.util.UnaryPredicate;

import java.util.*;


public class PredictorDataPlugin extends ComponentPlugin {

    class TriggerFlushAlarm implements PeriodicAlarm {
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
            myBS.signalClientActivity();
            //myLoggingService.shout("Alarm for PredictorDataPlugin fired");
            //executeAlarm();
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
        long delay = 60000;
    }


    UnaryPredicate relationPredicate = new UnaryPredicate() {
        public boolean execute(Object o) {
            if (o instanceof HasRelationships) {
                return ((HasRelationships) o).isLocal();
            } else {
                return false;
            }
        }

    };


 /*   UnaryPredicate interAgentPredicate = new UnaryPredicate() {
        public boolean execute(Object o) {
            return o instanceof InterAgentCondition;
        }

    }; */

  /*  UnaryPredicate htPredicate = new UnaryPredicate() {
        public boolean execute(Object o) {
            return o instanceof Hashtable;
        }

    };  */

  /*  UnaryPredicate hashArrayPredicate = new UnaryPredicate() {    //change
        public boolean execute(Object o) {
            return o instanceof PredictorArrayList1;
        }

    }; */

   /* UnaryPredicate AlarmPredicate = new UnaryPredicate() {
        public boolean execute(Object o) {
            return o instanceof TriggerFlushAlarm;
        }

    }; */

    UnaryPredicate ALPredicate = new UnaryPredicate() {
        public boolean execute(Object o) {
            return o instanceof PredictorArrayList;
        }

    };

    UnaryPredicate taskPredicate = new UnaryPredicate() {
        public boolean execute(Object o) {
            if (o instanceof Task)
			{
				Task tempTask = (Task) o;
                if(tempTask.getVerb().equals(Constants.Verb.PROJECTSUPPLY)){
                if(tempTask.getPrepositionalPhrase("For").getIndirectObject().equals("47-FSB")== true){
                    return true;
                }
                else {
                    return false;
                }
            //return o instanceof Task;
            }
            }
            return false;
        }
    };


    public void setupSubscriptions() {
        myBS = getBlackboardService();
        myLoggingService = (LoggingService) getBindingSite().getServiceBroker().getService(this,
                LoggingService.class, null);
        cluster = ((AgentIdentificationService) getBindingSite().getServiceBroker().getService(this,
                AgentIdentificationService.class, null)).getName();
        as = (AlarmService) getBindingSite().getServiceBroker().getService(this, AlarmService.class, null);
        relationSubscription = (IncrementalSubscription) myBS.subscribe(relationPredicate);
        taskSubscription = (IncrementalSubscription) myBS.subscribe(taskPredicate);
        //interAgentSubscription = (IncrementalSubscription) myBS.subscribe(interAgentPredicate);
        //htSubscription = (IncrementalSubscription) myBS.subscribe(htPredicate);
        //hashArraySubscription = (IncrementalSubscription) myBS.subscribe(hashArrayPredicate);
        //ALSubscription = (IncrementalSubscription) myBS.subscribe(ALPredicate);
        //AlarmSubscription = (IncrementalSubscription) myBS.subscribe(AlarmPredicate);
       /* if (flagger == false) {
            if (!taskSubscription.isEmpty()) {
                taskSubscription.clear();
            }  */
           /* if (!interAgentSubscription.isEmpty()) {
                interAgentSubscription.clear();
            }*/
            //flagger = true;
        //}
        if (myBS.didRehydrate() == false) {
            //ht = new Hashtable();
            //myBS.publishAdd(ht);
            //myLoggingService.shout("ht saved");
            //hashArray = new PredictorArrayList1();
            //myBS.publishAdd(hashArray);
            //myLoggingService.shout("hashArray saved");
            //al = new PredictorArrayList(cluster);
            //myBS.publishAdd(al);
            //myLoggingService.shout("al saved");
            myBS.setShouldBePersisted(false);
        } else {
            //retrievehtfromBB();
            rehydrate_flag = true;
            //retrievehashArrayfromBB();   //change
            retrieveALfromBB();
            //retrieveAlarmfromBB();
            //myLoggingService.shout("retrieveALfromBB");
        }
        //myBS.setShouldBePersisted(false);
    }


    public void execute() {

      /*  InterAgentCondition tr;

        for (Enumeration ent = interAgentSubscription.getAddedList(); ent.hasMoreElements();) {
            tr = (InterAgentCondition) ent.nextElement();
            String content = tr.getContent().toString();
            String f = null;
            if (content.equalsIgnoreCase("Servlet_Relay = 0") == true) {
                f = "OFF";
            } else if (content.equalsIgnoreCase("Servlet_Relay = 1") == true) {
                f = "SLEEP";
            }
            if (f != null && f == "OFF") {
                relay_added = true;
                myBS.publishChange(tr);
                break;
            } else if (f != null && f == "SLEEP") {
                relay_added = false;
                myBS.publishChange(tr);
                break;
            }
        }   */
        if(alarm!=null && alarm.hasExpired()==true){
            count_alarm++;
            if(count_alarm == 1 && rehydrate_flag == false){
                //long time = System.currentTimeMillis();
                //System.out.println(" Alarm start PDPAlarm function " +  new Date(time));
                executeAlarm();
                //myLoggingService.shout("Alarm fired in execute");
                //long end = System.currentTimeMillis();
                //System.out.println("Alarm End PDPAlarm function " +  new Date(end)
                       //+ " total time in milliseconds" +
                       //(end - time));
                alarm.cancel();
            }
        }
        if(rehydrate_flag == false){     //change
        relationshipList();
        getPlannedDemand();
        }
    }

   /* public void retrievehtfromBB(){
        if(ht == null){
            //myLoggingService.shout("ht is null");
            Collection c1 = htSubscription.getAddedCollection();
            Collection c2 = htSubscription.getChangedCollection();
            for(Iterator iter = c1.iterator();iter.hasNext() ;){
                ht = (Hashtable) iter.next();
                //myLoggingService.shout("ht size added"+ht.size());
                //myLoggingService.shout("ht Hashtable");
            }
            for(Iterator iter = c2.iterator();iter.hasNext() ;){
                ht = (Hashtable) iter.next();
                //myLoggingService.shout("ht size changed"+ht.size());
                //myLoggingService.shout("ht Hashtable");
            }
        }
    }  */

   /* public void retrievehashArrayfromBB(){           //change
      if(hashArray == null){
          //myLoggingService.shout("hashArray null");
          Collection c = myBS.query(hashArrayPredicate);
          for(Iterator iter = c.iterator();iter.hasNext() ;){
              hashArray = (PredictorArrayList1) iter.next();
              myLoggingService.shout("Number of unique relationships: "+hashArray.size());
              //myLoggingService.shout("hashArray PredictorArrayList1");
          }
            Collection c1 = hashArraySubscription.getAddedCollection();
            Collection c2 = hashArraySubscription.getChangedCollection();
            for(Iterator iter = c1.iterator();iter.hasNext() ;){
                hashArray = (ArrayList) iter.next();
                myLoggingService.shout("hashArray size added"+hashArray.size());
                myLoggingService.shout("hashArray ArrayList");
            }
            for(Iterator iter = c2.iterator();iter.hasNext() ;){
                hashArray = (ArrayList) iter.next();
                myLoggingService.shout("hashArray size changed"+hashArray.size());
                myLoggingService.shout("hashArray ArrayList");
            }
        }
    }  */

    public void retrieveALfromBB(){
      if(al == null){
          //myLoggingService.shout("al null");
          Collection c = myBS.query(ALPredicate);
          for(Iterator iter = c.iterator();iter.hasNext() ;){
              al = (PredictorArrayList) iter.next();
              //myLoggingService.shout("PredictorArrayList has total items = "+al.size());
              //myLoggingService.shout("al recieved");
              if(al.size()==0 && hashArray!= null){
                    if (alarm != null) alarm.cancel();
                    alarm = new TriggerFlushAlarm(currentTimeMillis() + 60000);
                    as.addAlarm(alarm);
                    count_alarm = 0;
                    //myLoggingService.shout("Alarm_added");
          } else if(al.size()!= 0){
                count_alarm = 1;
              if(alarm!=null){
                  alarm.cancel();
              }
              }
          }
        /*    Collection c1 = ALSubscription.getAddedCollection();
            Collection c2 = ALSubscription.getChangedCollection();
            for(Iterator iter = c1.iterator();iter.hasNext() ;){
                al = (PredictorArrayList) iter.next();
                myLoggingService.shout("al size added"+al.size());
                myLoggingService.shout("al recieved");
                 if(al.size()==0 && hashArray!= null){
                    if (alarm != null) alarm.cancel();
                    alarm = new TriggerFlushAlarm(currentTimeMillis() + 120000);
                    as.addAlarm(alarm);
                    count_alarm = 0;
                    myLoggingService.shout("Alarm_added");
          }
                count_alarm = 1;
            }
          for(Iterator iter = c2.iterator();iter.hasNext() ;){
                al = (PredictorArrayList) iter.next();
                myLoggingService.shout("al size changed"+al.size());
                myLoggingService.shout("al recieved");
               if(al.size()==0 && hashArray!= null){
                    if (alarm != null) alarm.cancel();
                    alarm = new TriggerFlushAlarm(currentTimeMillis() + 120000);
                    as.addAlarm(alarm);
                    count_alarm = 0;
                    myLoggingService.shout("Alarm_added");
          }
              count_alarm = 1;
            } */

        }
        if(al == null && hashArray!= null){
           if (alarm != null) alarm.cancel();
               alarm = new TriggerFlushAlarm(currentTimeMillis() + 60000);
               as.addAlarm(alarm);
               count_alarm = 0;
               //myLoggingService.shout("Alarm_added");
        }
    }

   /*  public void retrieveAlarmfromBB(){
      if(alarm == null){
          //myLoggingService.shout("alarm null");
            Collection c1 = AlarmSubscription.getAddedCollection();
            for(Iterator iter = c1.iterator();iter.hasNext() ;){
                alarm = (TriggerFlushAlarm) iter.next();
                //myLoggingService.shout("alarm recieved");
            }
        }
    }  */

    public void relationshipList() {
        HasRelationships org;
        Collection customers = new HashSet();
        RelationshipSchedule schedule = null;

        for (Enumeration et = relationSubscription.getChangedList(); et.hasMoreElements();) {
            org = (HasRelationships) et.nextElement();
            schedule = org.getRelationshipSchedule();
            Collection ammo_customer = schedule.getMatchingRelationships(Constants.Role.AMMUNITIONCUSTOMER); //Get a collection of ammunition customers
            customers.addAll(ammo_customer);

            Collection fuel_customer = schedule.getMatchingRelationships(Constants.Role.FUELSUPPLYCUSTOMER);
            customers.addAll(fuel_customer);

            Collection packpol_customer = schedule.getMatchingRelationships(Constants.Role.PACKAGEDPOLSUPPLYCUSTOMER);
            customers.addAll(packpol_customer);

            Collection subsistence_customer = schedule.getMatchingRelationships(Constants.Role.SUBSISTENCESUPPLYCUSTOMER);
            customers.addAll(subsistence_customer);

            Collection consumable_customer = schedule.getMatchingRelationships(Constants.Role.SPAREPARTSCUSTOMER);
            customers.addAll(consumable_customer);

            /* Iterate through the collection to get the customers and their supply class
             * Create Hashtable for each Unique relationship
             * Put the Hashtable in an ArrayList
             */

            for (Iterator iter = customers.iterator(); iter.hasNext();) {
                Relationship orgname = (Relationship) iter.next();
                Asset subOrg = (Asset) schedule.getOther(orgname);
                String role = schedule.getOtherRole(orgname).getName();
                String org_name = subOrg.getClusterPG().getMessageAddress().toString();
                boolean flag = uniqueMatch(cluster, org_name, role);
                if (flag == true) {
                    myLoggingService.debug("Supplier : " + cluster + "| Customer: " + org_name + "| Role " + role);
                    if (cluster != null && org_name != null) {
                        chashtable = new CreateHashtable(cluster, org_name, role);
                        chashtable.setHT();
                        if (hashArray.isEmpty()) {
                            hashArray.add(0, chashtable.returnHT());
                            myBS.publishAdd(hashArray);
                        } else {
                            hashArray.add((hashArray.size()), chashtable.returnHT());
                            myBS.publishChange(hashArray);
                        }
                    }
                }
            }
        }
    }

    public void getPlannedDemand() {
        Task task;
        //long time = System.currentTimeMillis();
        //System.out.println(" getPlannedDemand function " +  new Date(time));
        if (!relay_added == true) {
            for (Enumeration e = taskSubscription.getAddedList(); e.hasMoreElements();) {
                task = (Task) e.nextElement();
                //if (task != null) {
                    //UID uid = task.getUID();
                    //if (uid != null) {
                        //String verb = task.getVerb().toString();
                        //if (verb != null) {
                            //if (verb.equalsIgnoreCase("ProjectSupply") == true) {
                                String owner = task.getPrepositionalPhrase("For").getIndirectObject().toString();
                                //if (owner != null) {
                                    String pol = (String)task.getPrepositionalPhrase("OfType").getIndirectObject();
                                    //if (owner.equalsIgnoreCase(cluster) == false) {
                                        String comp = stringManipulation(pol);
                                        if (comp != null) {
                                            if (alarm != null) alarm.cancel();
                                            alarm = new TriggerFlushAlarm(currentTimeMillis() + 60000);
                                            as.addAlarm(alarm);
                                            //myBS.publishAdd(alarm);
                                            CreateHashtable chash = new CreateHashtable(cluster, owner, comp);
                                            if(hashArray!= null) {
                                            for (int j = 0; j < hashArray.size(); j++) {
                                                Vector tem = (Vector) ((Hashtable) hashArray.get(j)).get(new Integer(1));
                                                if (tem != null) {
                                                    if ((chash.getHT()).equals(((Hashtable) hashArray.get(j)).get(new Integer(1)))) {
                                                        ht = (Hashtable) hashArray.get(j);
                                                        if(j == 0){
                                                            myBS.publishAdd(ht);
                                                        }
                                                    }
                                                }
                                            }
                                            }
                                            if (ht!= null) {
                                                long sTime = (long) task.getPreferredValue(AspectType.START_TIME);
                                                long zTime = (long) task.getPreferredValue(AspectType.END_TIME);
                                                //add the item type
                                                Asset as = task.getDirectObject();
                                                if(as!= null) {
                                                String item_name = as.getTypeIdentificationPG().getNomenclature();
                                                for (long i = sTime; i <= zTime; i = i + 86400000) {
                                                    if (owner != null) {
                                                        if (cluster != null) {
                                                            if (comp != null) {
                                                                Vector values = new Vector();
                                                                //values.insertElementAt(cluster, 0);
                                                                //values.insertElementAt(owner, 1);
                                                                //values.insertElementAt(comp, 2);
                                                                //values.insertElementAt(new Long(sTime), 3);
                                                                //values.insertElementAt(new Long(zTime), 4);
                                                                values.insertElementAt(owner, 0);
                                                                values.insertElementAt(comp, 1);
                                                                if (comp.compareToIgnoreCase("FuelSupplyCustomer") == 0) {
                                                                    AspectRate aspectrate = (AspectRate) task.getPreference(AlpineAspectType.DEMANDRATE).getScoringFunction().getBest().getAspectValue();
                                                                    FlowRate flowrate = (FlowRate) aspectrate.rateValue();
                                                                    double rate = (flowrate.getGallonsPerDay());
                                                                    //values.insertElementAt(new Double(rate), 5);
                                                                    values.insertElementAt(new Double(rate), 2);
                                                                    t = i / 86400000;
                                                                    //values.insertElementAt(new Long(t), 6);
                                                                    //values.insertElementAt(item_name, 7);
                                                                    values.insertElementAt(new Long(t), 3);
                                                                    values.insertElementAt(item_name, 4);
                                                                } else {
                                                                    AspectRate aspectrate = (AspectRate) task.getPreference(AlpineAspectType.DEMANDRATE).getScoringFunction().getBest().getAspectValue();
                                                                    CountRate flowrate = (CountRate) aspectrate.rateValue();
                                                                    double rate = (flowrate.getUnitsPerDay());
                                                                    //values.insertElementAt(new Double(rate), 5);
                                                                    values.insertElementAt(new Double(rate), 2);
                                                                    t = i / 86400000;
                                                                    //System.out.println("t is "+t);
                                                                    //values.insertElementAt(new Long(t), 6);
                                                                    //values.insertElementAt(item_name, 7);
                                                                    values.insertElementAt(new Long(t), 3);
                                                                    values.insertElementAt(item_name, 4);
                                                                }

                                                                ht.put(new Integer((ht.size() + 1)), values);
                                                            }
                                                        }
                                                    }
                                                }
                                                }
                                                myBS.publishChange(ht);
                                                myBS.publishChange(hashArray);
                                            }
                                        }
                                    //}
                                //}
                            //}
                        //}
                    //}
                //}

            }
            //long end = System.currentTimeMillis();
                //System.out.println("getPlannedDemand function " +  new Date(end)
                      // + " total time in milliseconds" +
                      // (end - time));
        } else
            return;
    }

    public void executeAlarm() {
        counter++;
        if (counter == 1) {
            ProcessHashData phd = new ProcessHashData(hashArray);
            if (phd!= null) {
                al = phd.iterateList();
                if (al!= null) {
                    myBS.publishAdd(al);
                }
            }
        } else
            return;
    }

    /* This method makes sure each hashtable for a given supplier, customer
     * and supply class is unique
     */

    public boolean uniqueMatch(String a, String b, String c) {

        boolean flag1 = true;
        StringBuffer sb = new StringBuffer().append(a).append(b).append(c);
        if (hasht.isEmpty()) {
            hasht.put(new Integer(1), sb.toString());
            flag1 = true;
        } else if (hasht.containsValue(sb.toString()) == false) {
            hasht.put(new Integer((hasht.size()) + 1), sb.toString());
            flag1 = true;
        } else if (hasht.containsValue(sb.toString()) == true) {
            flag1 = false;
        }

        return flag1;

    }

    /* This method just manipulates the supply class string to convert it
     * into a role performed by the customer
     */

    public String stringManipulation(String a) {

        String s_class = a;

        final String ammo = "AmmunitionCustomer";
        final String packpol = "PackagedPOLSupplyCustomer";
        final String subs = "SubsistenceSupplyCustomer";
        final String bulkpol = "FuelSupplyCustomer";
        final String consumable = "SparePartsCustomer";

      /*  if (s_class.compareToIgnoreCase("Ammunition") == 0 || s_class.compareToIgnoreCase("Food") == 0) {
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

         if(s_class.compareToIgnoreCase("Consumable")==0)
        {
  		    String s_class1 = "SparePartsCustomer";
  		    return s_class1;
  	    }  */
        if (s_class.compareToIgnoreCase("Ammunition") == 0) {
            return ammo;
        }
        if (s_class.compareToIgnoreCase("Subsistence") == 0) {
            return subs;
        }
        if (s_class.compareToIgnoreCase("PackagedPol") == 0) {
            return packpol;
        }
        if (s_class.compareToIgnoreCase("BulkPol") == 0) {
            return bulkpol;
        }

         if(s_class.compareToIgnoreCase("Consumable")==0)
        {
  		    return consumable;
  	    }

        return null;
    }

    private String cluster;
    private LoggingService myLoggingService;
    private BlackboardService myBS;
    private IncrementalSubscription relationSubscription;
    private IncrementalSubscription taskSubscription;
    //private IncrementalSubscription interAgentSubscription;
    //private IncrementalSubscription htSubscription;
    //private IncrementalSubscription hashArraySubscription;
    //private IncrementalSubscription ALSubscription;
    //private IncrementalSubscription AlarmSubscription;
    private AlarmService as;
    private TriggerFlushAlarm alarm = null;

    private CreateHashtable chashtable;
    private Hashtable ht = new Hashtable();
    private Hashtable hasht = new Hashtable();
    PredictorArrayList1 hashArray = new PredictorArrayList1();

    private int counter = 0;
    private long t = 0;
    private boolean relay_added = false;
    //private boolean flagger = false;
    private PredictorArrayList al = null;
    int count_alarm = 0;
    boolean rehydrate_flag = false;

}




  /* private boolean isLevel2(Task task) {
       Asset actualAsset = task.getDirectObject();
       if (actualAsset instanceof AggregateAsset) {
         actualAsset = ((AggregateAsset) actualAsset).getAsset();
       }
       return (getAssetIdentifier(actualAsset).startsWith("Level2"));
   } */
