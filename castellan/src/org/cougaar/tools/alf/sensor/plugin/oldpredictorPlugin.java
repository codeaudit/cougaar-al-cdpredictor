
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
import org.cougaar.core.agent.service.alarm.PeriodicAlarm;
import org.cougaar.core.agent.*;
import org.cougaar.core.util.*;

import org.cougaar.glm.ldm.plan.*;
import org.cougaar.glm.ldm.asset.*;
import org.cougaar.glm.ldm.Constants;
import org.cougaar.glm.ldm.Constants.Preposition;

import org.cougaar.planning.ldm.plan.Role;
import org.cougaar.planning.ldm.measure.CountRate;
import org.cougaar.planning.ldm.measure.FlowRate;
import org.cougaar.planning.ldm.plan.*;
import org.cougaar.planning.ldm.asset.*;

import org.cougaar.util.UnaryPredicate;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Collection;
import java.util.Hashtable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Vector;
import java.lang.*;

import org.cougaar.lib.aggagent.test.*;

//import org.cougaar.util.CougaarEvent;
//import org.cougaar.util.CougaarEventType;


public class predictorPlugin extends ComponentPlugin {

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
    
    class TriggerFlushAlarm implements PeriodicAlarm {
    	
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
        long delay = 180000;
    }
    
    
    UnaryPredicate taskPredicate = new UnaryPredicate()
    {
        public boolean execute(Object o)
        {
        	return o instanceof Task;
        }
    };
 
 
    UnaryPredicate relayPredicate = new UnaryPredicate()
    {
            public boolean execute(Object o)
        {
            return o instanceof ArrayList;          	
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
	    taskSubscription = (IncrementalSubscription) myBS.subscribe(taskPredicate);
	    relaySubscription = (IncrementalSubscription) myBS.subscribe(relayPredicate);

        

    }
 
     
   public void execute()
   {
   	
   	Task task;
   	
   		/* Subscribe to Arraylist of Hashtables containing demand/day information
   		 * and pass a copy of that to the kalman filter
   		 * Change boolean variable status to flag the supply task subscription
   		 */
   		
		for(Enumeration et = relaySubscription.getAddedList(); et.hasMoreElements();)
		{
			arraylist = (ArrayList) et.nextElement();	
			if(arraylist!= null) 
			{
				System.out.println("ArrayList Received for agent "+cluster);
				kf = new kalmanFilter(arraylist);
				flag = true;
			}
		}
		
		/* Subscribe to the supply tasks and get the real demand per day 
       	 * Calls the support class to process the data on a day basis
       	 * Calls the Kalman Filter methods to do time update and measurement update   
         */
        
		if(flag == true) 
		{
   			for (Enumeration e = taskSubscription.getAddedList(); e.hasMoreElements();)
        	{
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
                            			if(verb.equalsIgnoreCase("Supply")==true) 
                            			{          
                        					String pol = (String)task.getPrepositionalPhrase("OfType").getIndirectObject();
                        					if(owner.equalsIgnoreCase(pe_owner)==false) 
                        					{
                        						String comp = stringManipulation(pol); 
                        						if(comp!=null) 
                        						{ 
                    								System.out.print("@");
                    	
                    								long ti = (currentTimeMillis()/86400000) - 13005;
                                				
                            						if(ti>= 14) {
                            							if(toggle == false) 
                            							{
                            								x = ti;
                            								toggle = true;
                            							}									
                          	 							long zTime = (long) ((task.getPreferredValue(AspectType.START_TIME))/86400000)-13005;                       
                            							long sTime = (long) ((task.getPreferredValue(AspectType.END_TIME))/86400000)-13005;                           		 
                          	 							double qty = (double) (task.getPreferredValue(AspectType.QUANTITY));                         	 							
                        								if(ti!=-1 && qty!= -1) 
                        								{                        								
                        									if(ti == x) 
                        									{ 
                          										sd.getSupplyQuantity(pe_owner, owner, comp, ti, sTime, qty);
                          									}
                          										
                          									else if(ti > x) 
                          									{
                          										ArrayList total_qty_alist = sd.returnDemandQuantity(pe_owner, owner, comp, ti, sTime, qty);	
                        										if(total_qty_alist!= null) 
                        										{    
                        											counter++;                     			 							 
                        											if(counter > 1) 
                        											{                        											
                        												kf.measurementUpdate(total_qty_alist);                      						
                        												kf.timeUpdate(total_qty_alist); 
                        												x = ti;
                        											}
                        											else 
                        											{
                        												kf.timeUpdate(total_qty_alist); 
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
     	else 
     	{
     		return;
     	}
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
  		/*if(s_class.compareToIgnoreCase("Consumable")==0) 
  		 {
  		    	String s_class1 = "SparePartsCustomer";
  		    	return s_class1;
  		}*/
  		
   		return null;
   	}
   	
   	  		
    private String cluster;
    private BlackboardService myBlackboardService;
    private LoggingService myLoggingService;
    private ServletService myServletService;
    private UIDService myUIDService;
    private BlackboardService myBS;
    private IncrementalSubscription relaySubscription;
    private IncrementalSubscription taskSubscription;

    AlarmService as;
    TriggerFlushAlarm alarm = null;	
    private ArrayList arraylist = new ArrayList();
    supplyDataUpdate sd = new supplyDataUpdate(); 
    private kalmanFilter kf= null;
    
    private boolean toggle = false;   
	private long x = 0; 
	private int counter = 0;
	private boolean flag = false;
}









	/*	for (Enumeration et = relaySubscription.getAddedList(); et.hasMoreElements();)
     {
        ArrayList arrayt = new ArrayList();
        tr = (TestRelay) et.nextElement();
        arrayt = (ArrayList) tr.getContent();
        if(arrayt!= null) 
        {
        	if(arraylt.isEmpty()) 
        	{
        		arraylt.add(0,arrayt);
        	}
        	else 
        	{
        		arraylt.add(arraylt.size(),arrayt);
        	}
        	System.out.println("ArrayList Transmitted");
        	System.out.println("ArrayList size " +arraylt.size()+ " for cluster "+cluster);
        	ArrayList temp1  = (ArrayList) arraylt.get(0);
        	Hashtable temp2 = (Hashtable) temp1.get(0);
        	Vector temp3 = (Vector) temp2.get(new Integer(1));
        	String temp4 = temp3.elementAt(1).toString(); 
        	String temp5 = temp3.elementAt(0).toString();    
        	System.out.println("Supplier is "+temp5+ " for cluster "+cluster);   			
        	System.out.println("Customer is "+temp4+ " for cluster "+cluster);
        	kf = new kalmanFilter(arraylt);
        	System.out.println("ArrayList added to KalmanFilter");
        }
        else
        	return;
     }
	  */