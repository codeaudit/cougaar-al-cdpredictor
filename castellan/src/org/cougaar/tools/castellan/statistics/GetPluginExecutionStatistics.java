/*
 * AgentPluginExecution.java
 *
 * Created on May 21, 2002, 2:20 PM
 */

package org.cougaar.tools.castellan.statistics;

import java.util.*;
import java.lang.* ;
import java.io.*;

import org.cougaar.tools.castellan.pdu.* ;
import org.cougaar.tools.castellan.planlog.* ;

/**
 *
 * @author  bbowles
 */
public class GetPluginExecutionStatistics extends GetStatsCommand {
   
   // ATTRIBUTES
   int agentCount = 0;
   ArrayList myAgentStats;

   // CONSTRUCTORS
   /** Creates a new instance of AgentPluginExecution */
   public GetPluginExecutionStatistics( EventLog theEventLog ) {
      super( theEventLog );
      myAgentStats = new ArrayList();
   }
   
   /**
    * Get collection of stats that were collected for each Agent.
    **/
   public Iterator getStats(){
      
      if( myAgentStats == null ){
         return null;
      }
      else if( myAgentStats.isEmpty() ){
         return null;
      }
      else{
         return myAgentStats.iterator();
      }
   }
   public int getNumStats(){
      if( myAgentStats != null ){
         return myAgentStats.size();
      }
      System.out.println( " # of agent stats = 0" );
      return 0;
   }
   
   /*
   *  Collect statistics for every agent in the system.
   */
   public void collectStats(){
      // Get a list of all agents in the society
      Collection agentNames = myEventLog.getAgents();
      // Collect stats on this list of agents
      if( agentNames.size() > 0 ){
         collectStats( agentNames );
      }
      return;
   }

  /*
   *  Collect statistics for list of agents specified.
   */
   public void collectStats( Collection theAgentNames ){
      // Create a collection of AgentStats based on # of Agents
      Iterator agentIter = theAgentNames.iterator();
      String agentName = null;
      while( agentIter.hasNext() ){
         // For each agent, collect AgentStats
         agentName = (String)agentIter.next();
         collectStats( agentName );
      }
   }

   /*
   *  Collect statistics for individual agent in the system.
   */
   public void collectStats( String theAgentName ){
      // Create an AgentStats
      AgentStats as = new AgentStats( myEventLog, theAgentName );
      // Collect statistics on the agent.
      as.collectStats();
      // Add the AgentStats to the collection.
      if( as != null ){
         myAgentStats.add( as );
      }
      else{
         System.out.println( "AgentStats is null, something is probably wrong.");
      }
    }
}