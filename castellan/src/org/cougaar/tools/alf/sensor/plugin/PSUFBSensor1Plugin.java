/*
 *  This plugin is a falling behind sensor for each agent based on the imbalance 
 *  between the performances of task arrival and task allocation.
 *  This sensor deals with ProjectSupply/Supply tasks and allocation to inventory assets.
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
import java.util.Iterator;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Arrays;
import org.cougaar.core.adaptivity.OMCRangeList;
import org.cougaar.tools.alf.sensor.plugin.PSUFBSensorCondition;

public class PSUFBSensor1Plugin extends ComponentPlugin
{
    
    UnaryPredicate taskPredicate = new UnaryPredicate()
    {
        public boolean execute(Object o)
        {
            return o instanceof Task;
        }
    };
    
    UnaryPredicate allocationPredicate = new UnaryPredicate()
    {
        public boolean execute(Object o)
        {
            return o instanceof Allocation;
        }
    };
    
    UnaryPredicate sensorPredicate = new UnaryPredicate()
    {
        public boolean execute(Object o)
        {
            return o instanceof PSUFBSensorCondition;
        }
    };
    

    public void setupSubscriptions()
    {
        ServiceBroker broker = getServiceBroker();
        bts = ( BlackboardTimestampService ) broker.getService( this, BlackboardTimestampService.class, null ) ;
        bs = getBlackboardService();
        taskSubscription = (IncrementalSubscription) bs.subscribe(taskPredicate);
        allocationSubscription = (IncrementalSubscription) bs.subscribe(allocationPredicate);
        sensorSubscription = (IncrementalSubscription) bs.subscribe(sensorPredicate);
        if (bs.didRehydrate()==false)
        {
            double[] result =new double[2];
            result[0] = 0.0; result[1] = 1.0;
    	    PSUFBSensorCondition psu_fb = new PSUFBSensorCondition("FallingBehind", new OMCRangeList(result), new Double(0));
		    bs.publishAdd(psu_fb);
		}
		cluster = getBindingSite().getAgentIdentifier().toString();
		task_series=new Hashtable();
		allocation_series=new Hashtable();
		bs.setShouldBePersisted(false);
    }


    public void execute()
    {
        Iterator iter;
        String verb, source;
        UID uid;
        Task task;
        Allocation allocation;
        AllocationResult ar;
        boolean change = false;
        
        // Gather task arrival time series for ProjectSupply and Supply Tasks.  
        
        for (iter = taskSubscription.getAddedCollection().iterator() ; iter.hasNext() ;)
        {
            task = (Task)iter.next();
            verb = task.getVerb().toString();
            source = task.getSource().toString();
            uid = task.getUID();
            if ((same(verb, "ProjectSupply")||same(verb, "Supply"))&& !same(source, cluster))
            {
                 task_series.put(uid.toString(), new Long(bts.getCreationTime(uid)));
                 change = true;
            }
        }
        
        // Gather allocation(not to organizational asset) time series.
        
        for (iter = allocationSubscription.getAddedCollection().iterator() ; iter.hasNext() ;)
        {
            allocation = (Allocation)iter.next();
            task = allocation.getTask();
            verb = task.getVerb().toString();
            if (!(same(verb, "ProjectWithdraw")||same(verb, "Withdraw"))) continue;
            source = task.getParentTaskUID().getOwner();
            if (same(source, cluster)) continue;
            if ((ar = allocation.getEstimatedResult())==null) continue;
            uid = task.getParentTaskUID();
            if (allocation_series.containsKey(uid.toString())) continue;
            if (ar.isSuccess() && ar.getConfidenceRating()==1.0)
            {
                 allocation_series.put(uid.toString(), new Long(bts.getCreationTime(allocation.getUID())));
                 change = true;
            }
        }
        
        // Gather allocation(not to organizational asset) time series.
        
        for (iter = allocationSubscription.getChangedCollection().iterator() ; iter.hasNext() ;)
        {
            allocation = (Allocation)iter.next();
            task = allocation.getTask();
            verb = task.getVerb().toString();
            if (!(same(verb, "ProjectWithdraw")||same(verb, "Withdraw"))) continue;
            source = task.getParentTaskUID().getOwner();
            if (same(source, cluster)) continue;
            if ((ar = allocation.getEstimatedResult())==null) continue;
            uid = task.getParentTaskUID();
            if (allocation_series.containsKey(uid.toString())) continue;
            if (ar.isSuccess() && ar.getConfidenceRating()==1.0)
            {
                 allocation_series.put(uid.toString(), new Long(bts.getModificationTime(allocation.getUID())));
                 change = true;
            }
        }
        if (change==true)
        {
            updateStatus();
        }
    }
 
    
    boolean same(String s1, String s2)
    {
        if (s1.equalsIgnoreCase(s2)) return true;
        else return false;
    }

    
    // Update falling behind status.   
    void updateStatus()
    {
        int i, n1 = task_series.size(), n2 = allocation_series.size();
        double x=0, y=0, status;
        long min=0;
        Object[] a;
        if (n1<2 || n2<2) return;
        if (n1 < n2)
        {
            status = 0.0;
        }
        else
        {
            a = task_series.values().toArray();
            Arrays.sort(a);
            for (i=0; i<n2; i++)
            {
                if ((min=((Long)a[i]).longValue())>0) break;
            }
            x = (((Long)a[n2-1]).longValue()-min)/1000.0;
            a = allocation_series.values().toArray();                      
            Arrays.sort(a);
            for (i=0; i<n2; i++)
            {
                if ((min=((Long)a[i]).longValue())>0) break;
            }            
            y = (((Long)a[n2-1]).longValue()-min)/1000.0;
            if (y > x * 0.8815 + 65)
            {
				status = 1.0;
		    }
		    else
		    {
		        status = 0.0;
		    }
		}
        PSUFBSensorCondition psu_fb=null;
        for (Iterator iter = sensorSubscription.iterator() ; iter.hasNext() ;)
        {
            psu_fb = (PSUFBSensorCondition)iter.next();
            break;
        }
        if (!(psu_fb.getValue().compareTo(new Double(status))==0))
        {
            psu_fb.setValue(new Double(status)); 
            bs.publishChange(psu_fb); 
            System.out.print("\n"+cluster+" "+n1+"(G) "+n2+"(A) "+x+"sec. "+y+"sec. ");
			if (status==1.0) System.out.print("Falling Behind\n");
			else System.out.print("\n");
		}
	}

    IncrementalSubscription taskSubscription;
    IncrementalSubscription allocationSubscription;   
    IncrementalSubscription sensorSubscription; 
    BlackboardService bs;
    BlackboardTimestampService bts; 
    String cluster;
    Hashtable task_series, allocation_series;

}
