
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

import org.cougaar.core.plugin.ComponentPlugin;
import org.cougaar.core.service.*;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.component.ServiceBroker;

import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.agent.service.alarm.PeriodicAlarm;
import org.cougaar.core.agent.*;
import org.cougaar.core.util.*;

import org.cougaar.glm.ldm.plan.*;
import org.cougaar.glm.ldm.asset.*;
import org.cougaar.glm.ldm.Constants;

import org.cougaar.planning.ldm.plan.Role;
import org.cougaar.planning.ldm.measure.CountRate;
import org.cougaar.planning.ldm.measure.FlowRate;
import org.cougaar.planning.ldm.plan.*;
import org.cougaar.planning.ldm.asset.*;

import org.cougaar.util.UnaryPredicate;

import org.cougaar.lib.aggagent.test.*;

//import org.cougaar.util.CougaarEvent;
//import org.cougaar.util.CougaarEventType;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Collection;
import java.util.Hashtable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Vector;
import java.lang.*;



public class predictorDataPlugin extends ComponentPlugin  {

	class FlushThread extends Thread {

        public long getInterval ()
        {
            return interval;
        }

        public void setInterval ( long interval )
        {
            this.interval = interval;
        }

        public boolean isStop ()
        {
            return stop;
        }

        public void setStop ( boolean stop )
        {
            this.stop = stop;
        }

        public void run ()
        {
            while ( !stop ) {
                try {
                   sleep( interval ) ;
                }
                catch ( InterruptedException e ) {
                }

                if ( stop ) {
                    break ;
                }

                BlackboardService bs = getBlackboardService() ;
                bs.openTransaction();
                execute();
                bs.closeTransaction();
            }
        }

        long interval = 4000 ;
        boolean stop = false ;
    }
    
    class TriggerFlushAlarm implements PeriodicAlarm
    {
        public TriggerFlushAlarm(long expTime)
        {
            this.expTime = expTime;
        }

        public void reset(long currentTime)
        {
            expTime = currentTime + delay;
            expired = false;
        }

        public long getExpirationTime()
        {
            return expTime;
        }

        public void expire()
        {
            expired = true;
			getBlackboardService().openTransaction();
			getBlackboardService().closeTransaction();
			cancel();

        }

        public boolean hasExpired()
        {
            return expired;
        }

        public boolean cancel()
        {
            boolean was = expired;
            expired = true;
            return was;
        }

        boolean expired = false;
        long expTime;
        long delay = 250000;
    }
    
    
    UnaryPredicate relationPredicate = new UnaryPredicate()
    {
        public boolean execute(Object o)
        {
            if(o instanceof HasRelationships) 
            {
            	return ((HasRelationships)o).isLocal();           	
        	}
        	else 
        	{
        		return false;
        	}
    	}
   
    };
   
   
    UnaryPredicate servletPredicate = new UnaryPredicate()
    {
        public boolean execute(Object o)
        {
            return o instanceof TestRelay;          	
        }
         
    };
    

   
   UnaryPredicate allocationPredicate = new UnaryPredicate()
    {
        public boolean execute(Object o)
        {
        	return o instanceof Allocation;
        }
    };
    
    
    UnaryPredicate taskPredicate = new UnaryPredicate()
    {
        public boolean execute(Object o)
        {
        	return o instanceof Task;
        }
    };
 
   
    public void setupSubscriptions()
    {
    	myBS = getBlackboardService();
        myUIDService = (UIDService) getBindingSite().getServiceBroker().getService(this,
                UIDService.class, null);
        myLoggingService = (LoggingService) getBindingSite().getServiceBroker().getService(this,
                LoggingService.class, null);
        myServletService = (ServletService) getBindingSite().getServiceBroker().getService(this,
                ServletService.class, null);
        cluster = ((AgentIdentificationService) getBindingSite().getServiceBroker().getService(this,
                AgentIdentificationService.class, null)).getName();      
        as = (AlarmService) getBindingSite().getServiceBroker().getService(this, AlarmService.class, null);
	    relationSubscription = (IncrementalSubscription) myBS.subscribe(relationPredicate);
	    allocationSubscription = (IncrementalSubscription) myBS.subscribe(allocationPredicate);
	    servletSubscription = (IncrementalSubscription) myBS.subscribe(servletPredicate); 
	    taskSubscription = (IncrementalSubscription) myBS.subscribe(taskPredicate);      
    }
 
     
   public void execute()
   {
   	
   	HasRelationships org;
   	Collection customers = new HashSet();
   	Allocation allocation;
   	Task task;
   	RelationshipSchedule schedule =null;
   	TestRelay tr;
     	
    if (alarm != null) alarm.cancel();
    	
    	/* Subscribe to Predictor servlet object for turn off
    	 * If recieved change boolean status that shuts off the plugin
    	 */
    	 
   	    for (Enumeration ent = servletSubscription.getAddedList(); ent.hasMoreElements();)
        {
        	tr = (TestRelay) ent.nextElement();
        	String f = tr.getContent().toString();
        	if(f!= null && f == "foo") 
        	{
        		relay_added = true;
        		System.out.println("Predictor Servlet Off message received for agent: " +cluster);
        		break;
        	} 
        	else
     
        	return;
        }
        
        /* Subscribe to the Organizations that have relationships and store
         * that in a collection
         */
         
   		for (Enumeration et = relationSubscription.getChangedList(); et.hasMoreElements();)    
        { 
        	org = (HasRelationships) et.nextElement(); 
        	schedule = org.getRelationshipSchedule();  
      	
        	Collection ammo_customer = schedule.getMatchingRelationships(Constants.Role.AMMUNITIONCUSTOMER); //Get a collection of ammunition customers
        	customers.addAll(ammo_customer);
        	
        	Collection food_customer = schedule.getMatchingRelationships(Constants.Role.FOODCUSTOMER);
        	customers.addAll(food_customer);
        	
        	Collection fuel_customer = schedule.getMatchingRelationships(Constants.Role.FUELSUPPLYCUSTOMER); 
            customers.addAll(fuel_customer);
            
            Collection packpol_customer = schedule.getMatchingRelationships(Constants.Role.PACKAGEDPOLSUPPLYCUSTOMER); 
        	customers.addAll(packpol_customer);
        	
        	Collection spareparts_customer = schedule.getMatchingRelationships(Constants.Role.SPAREPARTSCUSTOMER); 
            customers.addAll(spareparts_customer);
            
            Collection subsistence_customer = schedule.getMatchingRelationships(Constants.Role.SUBSISTENCESUPPLYCUSTOMER); 
        	customers.addAll(subsistence_customer);
			
			/* Iterate through the collection to get the customers and their supply class
			 * Create Hashtable for each Unique relationship
			 * Put the Hashtable in an ArrayList
			 */
        	
        	for(Iterator iter = customers.iterator(); iter.hasNext();) 
        	{           	        		
        		Relationship orgname = (Relationship) iter.next();
        		Asset subOrg = (Asset)schedule.getOther(orgname);
      			String role = schedule.getOtherRole(orgname).getName();
				String org_name = subOrg.getClusterPG().getMessageAddress().toString();  
				boolean flag = uniqueMatch(cluster, org_name , role);
				if(flag==true) 
				{     		       		
        			System.out.println("Supplier : " +cluster+ "| Customer: " +org_name+ "| Role "+role);  
        			if(cluster!=null && org_name!=null) 
        			{       		
        				chashtable = new createHashtable(cluster, org_name, role);     		
        	    		chashtable.setHT();
        	 			if(hashArray.isEmpty())
        	 			{
        					hashArray.add(0,chashtable.returnHT());
        				}
        				else 
        				{
        					hashArray.add((hashArray.size()),chashtable.returnHT());
        				}
        			}
        		}       		
        	}	
        	
      } 
      
      /* Subscribe to the tasks to extract demand quantity per day and insert that
       * in the appropriate hashtable   
       **/ 
       
      if(!relay_added == true)
      {     	
      	for (Enumeration e = taskSubscription.getAddedList(); e.hasMoreElements();)        
        {   
           if(fl == false) 
           {
            	st_time = currentTimeMillis();
            	st_time1 = st_time + 360000;
            	fl = true;
           }
        	      
           	task = (Task) e.nextElement();
           	if(task!= null) 
           	{
           		PlanElement pe = task.getPlanElement();
        		if(pe!= null) 
        		{
            		UID uid = task.getUID();
					if(uid!= null) 
					{                       	
                		String owner = task.getUID().getOwner();
           				if(owner!= null) 
           				{
      	 					String pe_owner = pe.getUID().getOwner();
                        	if(pe_owner!= null) 
                        	{
               					String verb = task.getVerb().toString();                   
                            	if(verb!= null) 
                            	{  
                            		if(verb.equalsIgnoreCase("ProjectSupply")==true) 
                            		{          
                        				String pol = (String)task.getPrepositionalPhrase("OfType").getIndirectObject();
                        				if(owner.equalsIgnoreCase(pe_owner)==false) 
                        				{
                        					String comp = stringManipulation(pol); 
                        					if(comp!=null) 
                        					{                        		
                        						createHashtable chash = new createHashtable(pe_owner, owner, comp);                        		
                        						for(int j =0; j < hashArray.size(); j++) 
                        						{
                        							Vector tem = (Vector) ((Hashtable)hashArray.get(j)).get(new Integer(1));
                        							if(tem!=null) 
                        							{                       									
                        								if((chash.getHT()).equals(((Hashtable)hashArray.get(j)).get(new Integer(1))))
                        	    						{
                        									ht = (Hashtable)hashArray.get(j);
                        								}
                        										
                        							} 
                        	  
                        						} 
                        	         	
                        						if(ht!=null) {  
                        					                         						
                        							/*	System.out.println("UID is "+uid); 
                            						System.out.println("Owner is "+owner); 
                            						System.out.println("Source is "+pe_owner);
                  									System.out.println("Verb is "+verb);
                  									System.out.println("pol is "+pol);*/
                  								
                  									System.out.print("&");
                  											                   		                        			
                            						long sTime = (long) task.getPreferredValue(AspectType.START_TIME);
                            						long zTime = (long) task.getPreferredValue(AspectType.END_TIME);                      
                            						for(long i = sTime; i<=zTime; i=i+86400000) 
                            						{                                    
                            							if (owner!=null) 
                            							{
                            								if(cluster!=null) 
                            								{
                            									if(comp!=null) 
                            									{
                            										Vector values = new Vector();                           			 
                            										values.insertElementAt(pe_owner,0);                            				
                            										values.insertElementAt(owner,1);                            					
                            										values.insertElementAt(comp,2);
                            										values.insertElementAt(new Long(sTime),3);
                            										values.insertElementAt(new Long(zTime),4);				
                            										if(comp.compareToIgnoreCase("FuelSupplyCustomer")==0)
                            										{
                            											AspectRate aspectrate = (AspectRate) task.getPreference(AlpineAspectType.DEMANDRATE).getScoringFunction().getBest().getAspectValue();                   				
                            											FlowRate flowrate = (FlowRate) aspectrate.rateValue();	
                            											double rate = (flowrate.getGallonsPerDay());	
                            											values.insertElementAt(new Double(rate),5);
                            											t = (i/86400000) - 13005;	
                            											values.insertElementAt(new Long(t),6);
                            											if(rate!=-1)
                            											{                              						
                            												//System.out.println("Rate is: "+rate);
                            											}
                            						
                            										}
                            										else 
                            										{
                            											AspectRate aspectrate = (AspectRate) task.getPreference(AlpineAspectType.DEMANDRATE).getScoringFunction().getBest().getAspectValue();                   				
                            											CountRate flowrate = (CountRate) aspectrate.rateValue();	
                            											double rate = (flowrate.getUnitsPerDay());
                            											values.insertElementAt(new Double(rate),5);
                            											t = (i/86400000) - 13005;	
                            											values.insertElementAt(new Long(t),6);
                            											if(rate!=-1)
                            											{  
                            												//System.out.println("Rate is: "+rate);
                            											}                            						
                            										}
                            											
                            										 ht.put(new Integer((ht.size()+1)),values);
                            										//System.out.println("Size of hashtable for supplier " +owner+ " with customer " +source+ " for supply class " +pol+ " is: " +ht.size());
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
	
   			alarm = new TriggerFlushAlarm( currentTimeMillis() + 250000 );
			as.addAlarm(alarm);
			
			/* Currently ad-hoc way for publishing ArrayList of Hashtables
			 * containing demand data after planning is over
			 * Once Cougaar Event is available with 10.4 version, it would be implemented
			 */
			 
			if(st_time == -1) 
			{
				return;
			}
			if(st_time1 == -1) 
			{
				return;
			}
	
			if(st_time1 < currentTimeMillis()) {
				counter++;
				if(counter==1) 
				{ 
					processHashData phd = new processHashData(hashArray);
					if(phd!= null) 
					{
						ArrayList al = phd.iterateList();
						if(al!= null) 
						{	
							myBS.publishAdd(al);
						}
					}
				}
			}
		}
  	}	               	
   	else
   	{
   		return;
   	}
   	
  }
   	
   	/* This method makes sure each hashtable for a given supplier, customer 
   	 * and supply class is unique
   	 */	
	
    public boolean uniqueMatch(String a, String b, String c) 
    {
   
    boolean flag1 = true;
   	StringBuffer sb = new StringBuffer().append(a).append(b).append(c);
   	if(hasht.isEmpty()) 
   	{
   		hasht.put(new Integer(1),sb.toString());
   		flag1 = true;
   	}
   	else if(hasht.containsValue(sb.toString())==false) 
   	{
   		hasht.put(new Integer((hasht.size())+1),sb.toString());
   		flag1 = true;  		
   	}
   	else if(hasht.containsValue(sb.toString())==true) 
   	{  		
   		flag1 = false;   		
   	}
   		  		
   	return flag1;
 
   }
   	
   	/* This method just manipulates the supply class string to convert it 
   	 * into a role performed by the customer
   	 */
   	
   	public String stringManipulation(String a) 
   	{
   		
   	String s_class = a;
   	
   	if(s_class.compareToIgnoreCase("Ammunition")==0 || s_class.compareToIgnoreCase("Food")==0) 
   	{
   		String s_class1 = s_class.concat("Customer");
   		return s_class1;
    }
   	if(s_class.compareToIgnoreCase("PackagedPol")==0 || s_class.compareToIgnoreCase("Subsistence")==0) 
   	{
   		String s_class1 = s_class.concat("SupplyCustomer");
   		return s_class1;
   	}
    if(s_class.compareToIgnoreCase("BulkPol")==0) 
    {
  		String s_class1 = "FuelSupplyCustomer";
  		return s_class1;
  	}
  		    	
     if(s_class.compareToIgnoreCase("Consumable")==0) 
    {
  		String s_class1 = "SparePartsCustomer";
  		return s_class1;
  	}
  		    	
   		return null;
   	}
 
 
   		
    /* 	public boolean eventIdentifier() {
   		CougaarEvent ce = new CougaarEvent(CougaarEventType.STATUS, null, null, "PlanningComplete", true);
   		String eve = ce.getEventText();
   		if(eve.compareToIgnoreCase("PlanningComplete")==0){
   			boolean toggle = true;
   			return toggle;
   		} 
   		return false;
   	}*/
   	
   	
   				    
    private String cluster;
    private LoggingService myLoggingService;
    private ServletService myServletService;
    private UIDService myUIDService;
    private BlackboardService myBS;
    private IncrementalSubscription relationSubscription;
    private IncrementalSubscription allocationSubscription;
    private IncrementalSubscription taskSubscription;
    private IncrementalSubscription servletSubscription;    
    AlarmService as;
    TriggerFlushAlarm alarm = null;	
    
    private createHashtable chashtable; 
    private Hashtable ht;   
    private Hashtable hasht = new Hashtable();
    private boolean relay_added = false;
    ArrayList hashArray = new ArrayList();
    
    private int counter = 0;    
    private long st_time = -1;
    private boolean fl = false;
    private long st_time1 = -1;
    private long t = 0;
}





	
   			/*if(cluster.compareToIgnoreCase("NCA")==0)
   			boolean hit = eventIdentifier();
   			if(hit == true) {					
   			} else {
   				return;
   			}*/

   		
 
      	
      	/*	Collection cl= new HashSet();
						if(al!=null) {					
							for(int x = 0; x <al.size()-1; x++) {
								String supplier_str = ((Vector)((Hashtable)al.get(x)).get(new Integer(1))).elementAt(0).toString();
			          			if(cl.isEmpty()) {					
									cl.add(supplier_str);
								}
								String supplier_str1 = ((Vector)((Hashtable)al.get(x+1)).get(new Integer(1))).elementAt(0).toString();
								if(supplier_str == supplier_str1) {
									continue;
								}
								else {
								 	cl.add(supplier_str1);
								}
							}
	                		if(cl!= null) {
	                			System.out.println("Collection Size: "+cl.size()+ " for cluster "+cluster);
	                			for(Iterator iter = cl.iterator(); iter.hasNext();) {
	                		 		String supp_name = (String) iter.next();
	                		 		System.out.println("Supplier is "+supp_name+ " for cluster "+cluster);				
							 		MessageAddress ma = (MessageAddress) cluster_ma.getMessageAddress();				
							 		MessageAddress target = MessageAddress.getMessageAddress(supp_name);
							 		if(ma!= target)
							 		{				
							 			TestRelay test = new TestRelay(myUIDService.nextUID(), ma, target , al ,null);								
            				 			myBS.publishAdd(test); 
            				 		}
            				 	}
            				}           	
							
		}*/
		
		
		/*
		 *for(int j = 2; j <= ht.size(); j++) 
                            										{
                            											long t_day_val = new Long(((Vector)ht.get(new Integer(j))).elementAt(6).toString()).longValue();
                            											if(t < t_day_val) 
																		{ 
																			Vector temp1 = (Vector)ht.get(new Integer(j));
																			ht.remove(new Integer(j));
																			ht.put(new Integer(j),values);
																			ht.put(new Integer(j+1),temp1);
																			break;								
																		}
																		else if(j==ht.size())
																		{	
																			ht.put(new Integer(ht.size()+1),values);
																			break;
																		}
																		}*/