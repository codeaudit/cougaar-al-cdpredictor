/*
 *  This plugin is a falling behind sensor for each agent based on the imbalance 
 *  between task arrival duration and task allocation duration.
 *  This sensor deals with ProjectSupply/Supply and Transport tasks.
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
import org.cougaar.core.service.community.CommunityService;
import org.cougaar.multicast.AttributeBasedAddress;
import org.cougaar.logistics.plugin.manager.LoadIndicator;

public class PSUFBSensor1Plugin extends ComponentPlugin
{
    
    UnaryPredicate allocationPredicate = new UnaryPredicate()
    {
        public boolean execute(Object o)
        {
            if (o instanceof Allocation) {
                Allocation allocation = (Allocation) o;
                if (same(allocation.getAsset().getUID().getOwner(), cluster)) {
                    AllocationResult ar = allocation.getEstimatedResult();
                    Task task = allocation.getTask();
                    if (ar != null) {
                        if (ar.isSuccess()) {
                            String verb = task.getVerb().toString();
                            String source = null;
                            if (same(verb, "ProjectWithdraw") || same(verb, "Withdraw")) {
                                source = task.getParentTaskUID().getOwner();
                            }
                            else if (same(verb, "ProjectSupply") || same(verb, "Supply")) {
                                source = task.getUID().getOwner();
                            }
                            else if (same(verb, "Transport")) {
                                source = "XXXX";
                            }
                            if (source!= null) {
                                if (!same(source, cluster)) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
            return false;
        }
    };
    
    UnaryPredicate sensorPredicate = new UnaryPredicate()
    {
        public boolean execute(Object o)
        {
            if (o instanceof LoadIndicator) {
                LoadIndicator loadIndicator = (LoadIndicator) o;
                if (loadIndicator.getReportingSensorClassName().endsWith(sensorname) && same(loadIndicator.getAgentName(), cluster)) {
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
        allocationSubscription = (IncrementalSubscription) myBlackboardService.subscribe(allocationPredicate);
        sensorSubscription = (IncrementalSubscription) myBlackboardService.subscribe(sensorPredicate);

        if (myBlackboardService.didRehydrate()==false)
        {
            CommunityService communityService = (CommunityService) getBindingSite().getServiceBroker().getService(this, CommunityService.class, null);
            if (communityService == null) {
                myLoggingService.error("CommunityService not available.");
                return;
            }
            Collection alCommunities = communityService.listParentCommunities(cluster, "(CommunityType=AdaptiveLogistics)");
            if (alCommunities.size() == 0) {
                myLoggingService.warn(cluster + " does not belong to an AdaptiveLogistics community.");
            }
            for (Iterator iterator = alCommunities.iterator(); iterator.hasNext();) {
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
		
		if (myTimestampService == null) {
            System.out.println("\n"+cluster+" ["+sensorname+"]: TimestampService is not available.\n");
        }
		
    }


    public void execute()
    {

		if (myTimestampService == null) {
            return;
        }
        
        Iterator iter;
        Task task;
        Allocation allocation;
        int valid_alloc=0, valid_task=0;
        String status = LoadIndicator.NORMAL_LOAD;
        long event_time;
        String verb;

        if (allocationSubscription.size() < 3) return;
        
        long min_alloc_time = Long.MAX_VALUE, max_alloc_time = Long.MIN_VALUE;
        long min_task_time = Long.MAX_VALUE, max_task_time = Long.MIN_VALUE;
        
        for (iter = allocationSubscription.getCollection().iterator() ; iter.hasNext() ;)
        {
            allocation = (Allocation)iter.next();
            event_time = myTimestampService.getModificationTime(allocation.getUID());
            if (event_time > 0) {
                if (event_time > max_alloc_time) max_alloc_time = event_time;
                if (event_time < min_alloc_time) min_alloc_time = event_time;
                valid_alloc = valid_alloc + 1;
            }
            else continue;
            task = allocation.getTask();
            verb = task.getVerb().toString();
            if (same(verb, "ProjectWithdraw") || same(verb, "Withdraw")) 
                event_time = myTimestampService.getModificationTime(task.getParentTaskUID());
            else event_time = myTimestampService.getModificationTime(task.getUID());
            if (event_time > 0) {
                if (event_time > max_task_time) max_task_time = event_time;
                if (event_time < min_task_time) min_task_time = event_time;
                valid_task = valid_task + 1;
            }
            else {
                if (valid_alloc > 0) valid_alloc = valid_alloc - 1;
                continue;
            }
        }
        
        if (valid_alloc < 3 || valid_task < 3) {
            return;
        }
        
        double x = max_task_time/1000.0 - min_task_time/1000.0;
        double y = max_alloc_time/1000.0 - min_alloc_time/1000.0;
        double a = 0.8988, b = 12.537, sigma = 51.44;
 
        for (iter = sensorSubscription.getCollection().iterator(); iter.hasNext();) {
            LoadIndicator loadIndicator = (LoadIndicator) iter.next();
            if (y > a * x + b + 2 * sigma) status = LoadIndicator.SEVERE_LOAD;
            else if (y <= a * x + b + sigma) status = LoadIndicator.NORMAL_LOAD;
            else status = LoadIndicator.MODERATE_LOAD;
            if (!same(loadIndicator.getLoadStatus(), status)) {
                loadIndicator.setLoadStatus(status);
                myBlackboardService.publishChange(loadIndicator);
            }
        }
        
        System.out.println("\n"+cluster+" ["+sensorname+"]: "+valid_alloc+" tasks "+((int)(x*100))/100.0+" Sec "+((int)(y*100))/100.0+" Sec ["+status+"]");
    }
 
    
    boolean same(String s1, String s2)
    {
        if (s1.equalsIgnoreCase(s2)) return true;
        else return false;
    }


    IncrementalSubscription allocationSubscription;   
    IncrementalSubscription sensorSubscription; 
    String cluster, sensorname = "PSUFBSensor1Plugin";
    private BlackboardTimestampService myTimestampService;
    private BlackboardService myBlackboardService;
    private LoggingService myLoggingService;
    private UIDService myUIDService;
    
}
