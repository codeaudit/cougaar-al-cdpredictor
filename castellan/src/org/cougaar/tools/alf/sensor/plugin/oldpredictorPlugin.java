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
  *  TORTIOUS CONDUCT ARISING OUT OF OR IN CONNECTION WITH THE USE OR
  *  PERFORMANCE OF THE COUGAAR SOFTWARE.
  *
  * </copyright>
  *
*/

package org.cougaar.tools.alf.sensor.plugin;

import org.cougaar.core.agent.*;
import org.cougaar.core.agent.service.alarm.PeriodicAlarm;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.plugin.ComponentPlugin;
import org.cougaar.core.service.*;
import org.cougaar.core.util.*;
import org.cougaar.glm.ldm.Constants;
import org.cougaar.glm.ldm.Constants.Preposition;
import org.cougaar.glm.ldm.asset.*;
import org.cougaar.glm.ldm.plan.*;
import org.cougaar.lib.aggagent.test.*;
import org.cougaar.planning.ldm.asset.*;
import org.cougaar.planning.ldm.measure.CountRate;
import org.cougaar.planning.ldm.measure.FlowRate;
import org.cougaar.planning.ldm.plan.*;
import org.cougaar.planning.ldm.plan.Role;
//import org.cougaar.util.UnaryPredicate;
import org.cougaar.util.*;
import java.io.*;

import java.util.Iterator;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Enumeration;

public class predictorPlugin extends ComponentPlugin {

	UnaryPredicate taskPredicate1 = new UnaryPredicate()	{ 	
			public boolean execute(Object o) {  
				if (o instanceof Task)
				{
					Task tempTask = (Task) o;
					Verb verb = tempTask.getVerb();
					if (verb.equals("GetLogSupport"))
					{
						return true;
					}

					if (verb.equals("Supply")||verb.equals("ProjectSupply"))
					{
						PrepositionalPhrase pp = null;
						if ((pp = tempTask.getPrepositionalPhrase("OfType"))!=null)
						{
//							if (pp.getIndirectObject().getClass().getName().equalsIgnoreCase("java.lang.String"))
//							{
								String s = (String) pp.getIndirectObject();
//								System.out.print(s+" ");
	
//								if (pp.getIndirectObject().getClass().getName().equalsIgnoreCase("org.cougaar.glm.ldm.asset.BulkPOL"))
								if (s.equalsIgnoreCase("BulkPOL")||s.equalsIgnoreCase("Ammunition"))
								{
									return true;
								} 
//							}
						}
					}
				}
				return false; 	
		} 
	};

    UnaryPredicate taskPredicate = new UnaryPredicate() {
        public boolean execute(Object o) {
            return o instanceof Task;
        }
    };


    UnaryPredicate relayPredicate = new UnaryPredicate() {
        public boolean execute(Object o) {
            return o instanceof ArrayList;
        }
    };


    public void setupSubscriptions() {

		cluster = agentId.toString(); // the cluster where this Plugin is running.

		if (selectedPredictor == KalmanFilter)
		{
		
            myBS = getBlackboardService();
            myUIDService = (UIDService) getBindingSite().getServiceBroker().getService(this,
                    UIDService.class, null);
            myLoggingService = (LoggingService) getBindingSite().getServiceBroker().getService(this,
                    LoggingService.class, null);
            myServletService = (ServletService) getBindingSite().getServiceBroker().getService(this,
                    ServletService.class, null);
//            cluster = ((AgentIdentificationService) getBindingSite().getServiceBroker().getService(this,
//                    AgentIdentificationService.class, null)).getName();
//            as = (AlarmService) getBindingSite().getServiceBroker().getService(this, AlarmService.class, null);
            taskSubscription = (IncrementalSubscription) myBS.subscribe(taskPredicate);
            relaySubscription = (IncrementalSubscription) myBS.subscribe(relayPredicate);

		} else {

			bs = getBlackboardService();
	   	    
			taskSubscription  = (IncrementalSubscription) bs.subscribe(taskPredicate1);
	   	    
			demand = new double[140];
		    for (int k=0;k<140;k++)	{ demand[k] = 0; 	}
	   	    
			 // Result file
			try
			{
				rst = new java.io.BufferedWriter ( new java.io.FileWriter(cluster+System.currentTimeMillis()+".pred.txt", true ));
			}
			catch (java.io.IOException ioexc)
	        {
			    System.err.println ("can't write file, io error" );
	        }	
			
			svmResult = new SvmResult();
		    
			ConfigFinder finder = getConfigFinder();
			String inputName = "Training.svm.data.txt.svm";
		    
			try {
		    
				File paramFile = finder.locateFile( "param.dat" ) ;
				if ( paramFile != null && paramFile.exists() ) {
		    
					svmResult.readParam(paramFile);
		    
				} else {
						System.out.println("Param model error.");
				}
		    
				if ( inputName != null && finder != null ) {
		    
					File inputFile = finder.locateFile( inputName ) ;
					if ( inputFile != null && inputFile.exists() ) {
						svmResult.readModel(inputFile);
			        } else {
						System.out.println("Input model error.");
					}
				}
		    
	        } catch ( Exception e ) {
				e.printStackTrace() ;
			}
		}

		System.out.println("PredictorPlugin start at " + cluster); 

    }


    public void execute() {

        Task task;

        /* Subscribe to Arraylist of Hashtables containing demand/day information
         * and pass a copy of that to the kalman filter
         * Change boolean variable status to flag the supply task subscription
         */

		if (selectedPredictor == KalmanFilter ) {

			for (Enumeration et = relaySubscription.getAddedList(); et.hasMoreElements();) {
		        arraylist = (ArrayList) et.nextElement();
			    if (arraylist != null) {
				    System.out.println("ArrayList Received for agent " + cluster);
					kf = new kalmanFilter(arraylist);
	                flag = true;
		        }
			}

			/* Subscribe to the supply tasks and get the real demand per day
		        * Calls the support class to process the data on a day basis
			    * Calls the Kalman Filter methods to do time update and measurement update
			*/
			KalmanFilter();
		} 	else 	{
			Predictor();
		} 
        
    }

    public void KalmanFilter() {

        Task task;

        /* Subscribe to the supply tasks and get the real demand per day
            * Calls the support class to process the data on a day basis
            * Calls the Kalman Filter methods to do time update and measurement update
        */

        if (flag == true) {
            for (Enumeration e = taskSubscription.getAddedList(); e.hasMoreElements();) {
                task = (Task) e.nextElement();
                if (task != null) {
                    String owner = task.getUID().getOwner();
                    if (owner != null) {
                        String verb = task.getVerb().toString();
                        if (verb != null) {
                            if (verb.equalsIgnoreCase("Supply") == true) {
                                String pol = (String) task.getPrepositionalPhrase("OfType").getIndirectObject();
                                if (owner.equalsIgnoreCase(cluster) == false) {
                                    String comp = stringManipulation(pol);
                                    if (comp != null) {
                                        System.out.print("@");
                                        long ti = (currentTimeMillis() / 86400000) - 13005;
                                        if (ti >= 12) {
                                            if (toggle == false) {
                                                x = ti;
                                                toggle = true;
                                            }
                                            long zTime = (long) ((task.getPreferredValue(AspectType.START_TIME)) / 86400000) - 13005;
                                            long sTime = (long) ((task.getPreferredValue(AspectType.END_TIME)) / 86400000) - 13005;
                                            double qty = (double) (task.getPreferredValue(AspectType.QUANTITY));
                                            if (ti != -1 && qty != -1) {
                                                if (ti == x) {
                                                    sd.getSupplyQuantity(cluster, owner, comp, ti, sTime, qty);
                                                } else if (ti > x) {
                                                    ArrayList total_qty_alist = sd.returnDemandQuantity(cluster, owner, comp, ti, sTime, qty);
                                                    if (total_qty_alist != null) {
                                                        counter++;
                                                        if (counter > 1) {
                                                            kf.measurementUpdate(total_qty_alist);
                                                            kf.timeUpdate(total_qty_alist);
                                                            x = ti;
                                                        } else {
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

        } else {
            return;
        }
    }

    /* This method just manipulates the supply class string to convert it
   	 * into a role performed by the customer
   	 */

	private void Predictor() {

		checkTaskSubscription();

		if (changed)
		{
			predictNextDemand();
			changed = false;
		}

	}

	private void checkTaskSubscription()
	{
		if (!taskSubscription.isEmpty()) {
				
			Collection c1 = taskSubscription.getAddedCollection();
			if (c1!=null)
			{
				int nAddedTasks = c1.size();
			    Iterator addedTaskIterator = c1.iterator();   
				printOut(nAddedTasks, addedTaskIterator, "added",currentTimeMillis());
			}
		}
	}

	private void predictNextDemand()
	{

		double [] aRow;

		aRow = new double[11];

		if (current_time >= 10)
		{
			int ct = (int) current_time;

			try
			{
				double yy =0;
				for (int h=0;h<10;h++)
				{
					aRow[h] = demand[ct-10+h+1];
					yy = yy + aRow[h];	// for moving average
					rst.write(aRow[h]+"\t"); 
				}

				aRow[10] = ct+1;
	
				double y = svmResult.f(aRow); // svm
				yy = yy/10;	// moving average

				rst.write(aRow[10]+"\t"+y+"\t"+yy+"\n"); 
				rst.flush();
			}
			catch (java.io.IOException ioexc)
			{
				System.err.println ("can't write file, io error" );
			}					

		}
	}

	private void printOut(int nTasks, Iterator taskIter, String modifier, long nowTime)
	{
		for (int i = 0; i < nTasks; i++) {
			Task ti = (Task)taskIter.next();

			PrepositionalPhrase pp = ti.getPrepositionalPhrase("OfType");

			String oftype = null;

			if (pp != null)
			{
				oftype = (String) pp.getIndirectObject();
			} else {
				System.out.println ("null Prepositional Phrase" );
				continue;
			}

			PrepositionalPhrase pp2 = ti.getPrepositionalPhrase("Refill");
			String refill = "Refill";

			if (pp2 == null)
			{
				refill = "null";
			} 

			Verb v = ti.getVerb();

			double start_time=0, start_time2=0;
			double end_time=0, end_time2=0;
			double qty = 0, rate = 0, rarConfidence=-1,earConfidence=-1;
			String rarsuccess=" ",earsuccess=" ";
			AllocationResult rar=null, ear=null;

			if (v.equals("GetLogSupport")==false)
			{
//				System.out.println(v.toString());
				start_time = (long) (ti.getPreferredValue(AspectType.START_TIME) / 86400000) - baseTime;
//   			start_time2 = ti.getPreference(AspectType.START_TIME).getScoringFunction().getBest().getValue();
	
				end_time = (long) (ti.getPreferredValue(AspectType.END_TIME) / 86400000) - baseTime;
//				end_time2 = ti.getPreference(AspectType.END_TIME).getScoringFunction().getBest().getValue();
					
				if (v.equals("Supply"))
				{
					qty = ti.getPreferredValue(AspectType.QUANTITY);
				} 
				else if (v.equals("ProjectSupply"))
				{
					AspectRate aspectrate = (AspectRate) ti.getPreference(AlpineAspectType.DEMANDRATE).getScoringFunction().getBest().getAspectValue(); 

					if (oftype.equals("BulkPOL"))
					{
						FlowRate flowrate = (FlowRate) aspectrate.rateValue();
						rate = flowrate.getGallonsPerDay();  // from LogSupplyProjector.java
					} else {
						CountRate countrate = (CountRate) aspectrate.rateValue();
						rate = countrate.getEachesPerDay();  // from LogSupplyProjector.java
					}

					qty = rate*(end_time-start_time)/(double) org.cougaar.glm.plugins.TimeUtils.MSEC_PER_DAY;
				}
				
				if (ti.getPlanElement()!=null)
				{
					if ((rar = ti.getPlanElement().getReportedResult())!=null)
					{
						if (rar.isSuccess())	{ rarsuccess = "success"; } else { rarsuccess = "fail";}
						rarConfidence = rar.getConfidenceRating();
					}
					if ((ear = ti.getPlanElement().getEstimatedResult())!=null)
					{
						if (ear.isSuccess())	{ earsuccess = "success"; } else { earsuccess = "fail";}
						earConfidence = ear.getConfidenceRating();
					}
				}
			}
	
			UID uid = ti.getUID();
			
//			try
//			{
				if (refill.equalsIgnoreCase("Refill") && oftype.equalsIgnoreCase("Ammunition") && v.equals("Supply") && uid.getOwner().equalsIgnoreCase("1-35-ARBN"))
				{
					changed = true;
					Double d = new Double(end_time);
//					current_time = (long) (d.longValue()-1124668800000L)/(3600*24*1000);
					current_time = (long) (d.longValue()/ 86400000) - baseTime;
					int ct = (int) current_time;
					demand[ct] = demand[ct] + qty;
				}
//			}
//			catch (java.io.IOException ioexc)
//			{
//				System.err.println ("can't write file, io error" );
//		    }					
		} // for
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
        /*	if(s_class.compareToIgnoreCase("Consumable")==0)
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
	private IncrementalSubscription planelementSubscription;

//    AlarmService as;
//    TriggerFlushAlarm alarm = null;
    
	private ArrayList arraylist = new ArrayList();
    supplyDataUpdate sd = new supplyDataUpdate();
    private kalmanFilter kf = null;

    private boolean toggle = false;
    private long x = 0;
    private int counter = 0;
    private boolean flag = false;
	private final int MovingAverage			= 1;
	private final int SupportVectorMachine	= 2;
	private final int KalmanFilter			= 3;
	private int selectedPredictor= KalmanFilter;
    boolean changed = false;
	long current_time = -1;
	long nextTime = 0;
	long baseTime = 13005; // August 10th 2005 
	double [] demand;
	java.io.BufferedWriter rst = null;
	SvmResult svmResult = null;
	private	BlackboardService bs;
}