/*
 *  This plugin is a falling behind sensor for specific agents based on the frequent episodes 
 *  that occur in a cluster
 *  
 */


package org.cougaar.tools.alf.sensor.plugin;

import org.cougaar.core.plugin.ComponentPlugin;
import org.cougaar.core.service.*;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.util.UID;
import org.cougaar.planning.ldm.plan.*;
import org.cougaar.util.*;
import org.cougaar.core.agent.ClusterIdentifier;
import org.cougaar.util.ConfigFinder;
import java.util.Iterator;
import java.util.Collection;
import java.util.Hashtable;
import org.cougaar.core.service.community.CommunityService;
import org.cougaar.multicast.AttributeBasedAddress;
import org.cougaar.logistics.plugin.manager.LoadIndicator;  

import java.io.File;
import java.io.*;
import java.util.Date;

public class PSUFBSensor3Plugin extends ComponentPlugin
{   
    UnaryPredicate taskPredicate = new UnaryPredicate()
    {
        public boolean execute(Object o)
        {
            return o instanceof Task;
        }
    };
    

    UnaryPredicate sensorPredicate = new UnaryPredicate()
    {
        public boolean execute(Object o)
        {
            if (o instanceof LoadIndicator) {
                LoadIndicator loadIndicator = (LoadIndicator) o;
                if (loadIndicator.getReportingSensorClassName().endsWith(sensorname) && match(loadIndicator.getAgentName(), cluster)) {
                    return true;
                }
            }
            return false;
        }
    };

    
    public void setupSubscriptions()
    {
        myTimestampService = (BlackboardTimestampService) getBindingSite().getServiceBroker().getService(this, BlackboardTimestampService.class, null);
        myBlackboardService = getBlackboardService();
        myUIDService = (UIDService) getBindingSite().getServiceBroker().getService(this, UIDService.class, null); 
        myLoggingService = (LoggingService) getBindingSite().getServiceBroker().getService(this, LoggingService.class, null); 
	    cluster = getBindingSite().getAgentIdentifier().toString();
	    taskSubscription = (IncrementalSubscription) myBlackboardService.subscribe(taskPredicate);
        sensorSubscription = (IncrementalSubscription) myBlackboardService.subscribe(sensorPredicate);

        if (myBlackboardService.didRehydrate()==false)
        {
            CommunityService communityService = (CommunityService) getBindingSite().getServiceBroker().getService(this, CommunityService.class, null);
            if (communityService == null)
			{
                myLoggingService.error("CommunityService not available.");
                return;
            }
            Collection alCommunities = communityService.listParentCommunities(cluster, "(CommunityType=AdaptiveLogistics)");
            if (alCommunities.size() == 0) 
			{
                myLoggingService.warn(cluster + " does not belong to an AdaptiveLogistics community.");
            }
            for (Iterator iterator = alCommunities.iterator(); iterator.hasNext();) 
			{
                String community = (String) iterator.next();
                LoadIndicator loadIndicator = new LoadIndicator(this, 
                                    cluster,
                                    myUIDService.nextUID(),
                                    LoadIndicator.NORMAL_LOAD);
                loadIndicator.addTarget(new AttributeBasedAddress(community, "Role", "AdaptiveLogisticsManager"));
                myBlackboardService.publishAdd(loadIndicator);
            }
		}
		myBlackboardService.setShouldBePersisted(false);
		if(myTimestampService==null)
		{
		System.out.println("TimestampService for"+" "+sensorname+" "+"in"+" "+cluster+" "+"not available");
		}
    }


    public void execute()
    {
        Iterator iter;
        String verb, source;    
        Task task;

	if (myTimestampService == null) {
            myTimestampService = (BlackboardTimestampService) getBindingSite().getServiceBroker().getService(this, BlackboardTimestampService.class, null);
            if (myTimestampService == null) {
                return;
            }
            else {
                System.out.println("\n"+cluster+" ["+sensorname+"]: TimestampService is available.\n");
            }
        }
   
        for (iter = taskSubscription.getCollection().iterator() ; iter.hasNext() ;)
        {
			long t;
			long newTime=0;
            task = (Task)iter.next();
            verb = task.getVerb().toString();
            source = cluster;
            uid = task.getUID(); 
			String [] agentList = {"47-FSB","123-MSB","110-POL-SUPPLYCO","102-POL-SUPPLYCO","191-ORDBN","501-FSB","565-RPRPTCO","592-ORDCO"};
			String csource=null;

			for(int i=0;i<agentList.length;i++){
				if (source.compareToIgnoreCase(agentList[i])==0)
				{
				  csource = agentList[i];
				}
			}

			if (match(verb,"GetLogSupport") && parity==false)
			{
				starttime = myTimestampService.getCreationTime(uid);
				if (starttime<=0) 
				{
				starttime = new Date().getTime(); 
				}
				System.out.println("PSUFBSensor3Plugin in"+" "+cluster+" "+"Executing:"+" " +verb);
				parity = true;
				endtime=endtime+span;
			}
			if (parity==true)
			{
			    t = myTimestampService.getCreationTime(uid);
				if(t!=-1)
				{
				newTime = t - starttime;
				}
				if(newTime<=endtime)
				{
				    if (match(verb, "ProjectSupply"))
				    {
				        count_ps++;
				    }
				    
				    if (match(verb, "ProjectWithdraw"))
				    {
				        count_pw++;
				    }
				}
			    else
				{
					count_window++;
					System.out.println("Window Number:"+" for"+cluster+" is "+count_window);
					flag = true;					
					compute2(count_ps,count_pw);
					count_ps=0;
					count_pw=0;
                    if (match(verb, "ProjectSupply"))
				    {
				        count_ps++;
				    }				    
				    if (match(verb, "ProjectWithdraw"))
				    {
				        count_pw++;
				    }
					endtime=endtime+span;
					
					while((newTime-endtime)>endtime)
					{
						if(flag==false)
						{
							count_window1++;
					System.out.println("Window Number:"+" for"+cluster+" is "+count_window1);

							count_window=count_window1;
							compute2(count_ps,count_pw);
						}
								
						count_ps=0;
						count_pw=0;
						
						if(flag==true)
							{
								count_window1 = count_window;
							}
						flag=false;								
						endtime=endtime+span;
					}
				}			     
				
			}
			
		}
     }
        	   
    boolean match(String s1, String s2)
    {
        if (s1.equalsIgnoreCase(s2)) return true;
        else return false;
    }


 int compute2(int x, int y)
	{	   	   	  
		 int s = 2; 
		 int i; 
	       int a=0;
	       i = Math.min(x,y);
	       num = num + i;
	       k++;
			 	  
	  if(Math.IEEEremainder(k,3)==0)
		{		  
		  int t;
		  t=k;
		  t=t/3;
		  String s1 = Integer.toString(s);
		  String t1 = Integer.toString(t);
		  String st1 = t1.concat(s1);
		  a = num/3;
		  num2 = num2 +  num;	
		  System.out.println("Episode Count:"+" for"+cluster+" is "+num2); 
		  num=0;
		  dispToggle=true;
		  ValueRef(st1,a,num2);
		}
		return a;
	}

  void ValueRef(String rf, int ab, int u)
	{
	  myTableMap = new Hashtable();
	  String textFileName = sensorname.concat(cluster+".txt"); 
	  Iterator iter;
	  int id;
	  String status2 = LoadIndicator.NORMAL_LOAD;
        BufferedReader is = null;
	  ConfigFinder finder = getConfigFinder() ;
	  File ff=null;
	  String f=null;
        if ( textFileName != null && finder != null ) 
        {
           ff = finder.locateFile(textFileName);
	  }
        if(ff==null)
	  {
	   System.out.println("Text File:"+" "+textFileName+" "+"not found in configs/common directory");
	   return;
	  }
	  f = ff.toString() ;
	  
				  try
				  {	  
                           is = new BufferedReader(new FileReader(f));
 
					  while(is.ready())				  
					  {						  					  
						  int a1=0;
						  int b=0;
						  String temp = is.readLine();	
						  a1 = temp.indexOf(" ", b);	
						  int refTable[] = new int[10];
						  id =  (Integer.valueOf(temp.substring(b,a1))).intValue(); 					  
						  b=a1+1;					  			  
						  a1 = temp.indexOf(" ", b);
						  refTable[0] = (Integer.valueOf(temp.substring(b,a1))).intValue();
						  b=a1+1;
						  a1 = temp.indexOf(" ", b);
						  refTable[1]=(Integer.valueOf(temp.substring(b,a1))).intValue();
						  b=a1+1;
						  a1 = temp.indexOf(" ", b);
						  refTable[2]=(Integer.valueOf(temp.substring(b,a1))).intValue();
						  b=a1+1;
						  refTable[3]=(Integer.valueOf(temp.substring(b))).intValue();
						  myTableMap.put(new Integer(id),refTable);                                                                      
					  }
                             is.close();				  				  
				  }
				  catch (java.io.IOException ioexc)
				  {
					  System.err.println("can't read or write file, io error");
					  System.err.println("RbfRidgeRegression constructor");
				  }	
				      
					  Integer newVal1 = Integer.valueOf(rf);
					  if(myTableMap.containsKey(newVal1)==true)
					  {
					  int Table[] = new int[6];
					  Table = (int []) myTableMap.get(newVal1);
					  result rt = new result(Table);
					  double status1 = rt.deduce(ab,u);	
					  
					  if(dispToggle==true)
					  {
							for (iter = sensorSubscription.getCollection().iterator(); iter.hasNext();) 
							{
							    LoadIndicator loadIndicator = (LoadIndicator) iter.next();
								if (status1==0.0)
								{
								status2 = LoadIndicator.NORMAL_LOAD;
								}
								else if (status1==1.0)
								{
								status2 = LoadIndicator.MODERATE_LOAD;
								}
								else if(status1==2.0)
								{
								status2 = LoadIndicator.SEVERE_LOAD;
								}
								if (!match(loadIndicator.getLoadStatus(), status2)) 
								{
								loadIndicator.setLoadStatus(status2);
								myBlackboardService.publishChange(loadIndicator);
								}
							}
					dispToggle=false;
					System.out.println("\n"+"["+sensorname+"]"+" "+"indicates"+" "+cluster+" "+"is under"+" "+status2+" "+"Load");

					}
				}
			
	}

    IncrementalSubscription taskSubscription;  
    IncrementalSubscription sensorSubscription; 
    String cluster, sensorname = "PSUFBSensor3Plugin";
    private BlackboardTimestampService myTimestampService;
    private BlackboardService myBlackboardService;
    private LoggingService myLoggingService;
    private UIDService myUIDService;
    private Hashtable myTableMap;
    private boolean dispToggle = false;	
    private boolean flag = false;
    private  int count_ps=0;
    private  int count_pw=0;
    private  int count_window=0;
    private  int count_window1=0;
    private int num;
    private int num2;
    private int k;
    boolean parity = false;
    UID uid;
    long starttime=0;
    int span=5000;
    int endtime=0;	
	
}

class result
	{
		public result(int arr[])
		{
			avg = arr[0];
			llavg = arr[1];
			ulavg = arr[2];
			cumln = arr[3];
			
		}
		
		 double deduce(int avg1, int cum2)
		{
		
			double cumlm = cumln-(0.30*cumln);
			double cumls = cumln-(0.50*cumln);

			if (avg1!=-1)
			{
				if (cum2>= (int)cumlm)
			    {
				    status=0.0;
				    
			    }
			    else if (cum2< (int)cumlm && cum2>= (int)cumls)
			    {
					status=1.0;
					
			    }
				else if (cum2< (int)cumls) 
			    {
				    status=2.0;
				    
			    }
			}
                  return status;		
		}

		int avg;
		int llavg;
		int ulavg;
		int cumln;
		double cumlm;
		double cumls;
		double status;
	};


	