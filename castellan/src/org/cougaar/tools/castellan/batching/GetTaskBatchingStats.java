/*
 * GetTaskBatchingStats.java
 *
 * Created on June 9, 2002, 8:29 PM
 */

package org.cougaar.tools.castellan.batching;

import java.util.* ;
import org.cougaar.tools.castellan.planlog.EventLog ;
import org.cougaar.tools.castellan.pdu.* ;
import org.cougaar.tools.castellan.analysis.* ;
import org.cougaar.tools.alf.* ;

/**
 *
 * @author  bbowles
 */
public class GetTaskBatchingStats extends GetStatsCommand {
   
   private ArrayList myAgentStats = new ArrayList();
   private ArrayList myIncomingTaskBoundaryLogs = new ArrayList();
   private PlanLogDatabase myPlanLog;
   
   
   /** 
    * Creates a new instance of getTaskBatchingStats for the Society.
    */
   public GetTaskBatchingStats( EventLog theEventLog, Collection theTaskLogs,
                                PlanLogDatabase thePlanLog ){
      super( theEventLog );
      myPlanLog = thePlanLog;
      processTaskLogs( theTaskLogs );
      buildContainer();
   }
   
   /** 
    * Creates a new instance of getTaskBatchingStats for a given Agent.
    */
   public GetTaskBatchingStats( EventLog theEventLog, Collection theTaskLogs, 
         PlanLogDatabase thePlanLog, String theAgentName ) {
      super( theEventLog );
      processTaskLogs( theTaskLogs );
      buildContainer( theAgentName );
   }
   
   /**
    * There are several ways to get the agent names, this one gets them from 
    * the collection of executions in EventLog.  Probably want to change impl.
    * to something else later.
    **/
   public Collection getAgentNames() {
      // Get a list of all agents in the society
      return myEventLog.getAgents();
   }
   /*
   *  CBuild containers for every agent in the system.
   */
   public void buildContainer(){
      // Get a list of all agents in the society
      Collection agentNames = getAgentNames();
      // Build container for this list of agents
      if( agentNames.size() > 0 ){
         buildContainer( agentNames );
      }
      else{
         System.out.println( "No Agent names were found, nothing was built."); 
      }
      return;
   }

  /*
   *  Build containers for list of agents specified.
   */
   public void buildContainer( Collection theAgentNames ){
      // Create a collection of AgentStats based on # of Agents
      Iterator agentIter = theAgentNames.iterator();
      String agentName = null;
      while( agentIter.hasNext() ){
         // For each agent, build an AgentStats
         agentName = (String)agentIter.next();
         buildContainer( agentName );
      }
   }

   /*
   *  Build containers for individual agent in the system.
   */
   public void buildContainer( String theAgentName ){
      // Create an AgentStats
      AgentStats as = new AgentStats( myEventLog, theAgentName, myPlanLog,
                                      myIncomingTaskBoundaryLogs );
      // Add the AgentStats to the collection.
      if( as != null ){
         myAgentStats.add( as );
      }
      else{
         System.out.println( "AgentStats is null, something is probably wrong.");
      }
   }

   /**
    * Iterate through all the TaskLogs and pull out those that represent
    * tasks coming into the agent.
    **/
   private void processTaskLogs( Collection theTaskLogs ) {      
      Iterator it = theTaskLogs.iterator();
      BoundaryLog bl;
      while( it.hasNext() ){
         bl = (BoundaryLog)it.next();
         if( isIncoming( bl ) ){
            // Add to list of incoming (and source) logs
            myIncomingTaskBoundaryLogs.add( bl );
         }
      }
   }
   /**
    * Determines if the Task associated with this log is an incoming Task
    * to this Agent (or an original Task ?? i.e. no parent Task)
    **/
   private boolean isIncoming( BoundaryLog theBoundaryLog ){
      int boundaryType = theBoundaryLog.getBoundaryType();
      if( ( BoundaryConstants.isIncoming( boundaryType ) ) || 
          ( BoundaryConstants.isSource( boundaryType ) ) ){
          return true;
      }
      else{
         return false;
      }
   }
       
   /**
    * Used to supply a printout of the comprehensive list of Task batching
    * statistics generated for the society.
    **/
   public String toString() {
      return " Task Batching Statistics: " +
      "\n" + myAgentStats.toString() + "\n";      
   }   
   
   /**
    * Used to supply an iterator to the collection of high level Task Batching
    * Statistics generated for each Agent (selected) in the society.
    **/
   public Iterator getStats() {
      return myAgentStats.iterator();
   }  
   public int getNumStats() {
      return myAgentStats.size();
   }  
}
