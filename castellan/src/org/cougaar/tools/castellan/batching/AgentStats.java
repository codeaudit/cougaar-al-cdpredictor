/*
 * AgentExecStats.java
 *
 * Created on May 30, 2002, 9:46 PM
 */

package org.cougaar.tools.castellan.batching;

import org.cougaar.tools.castellan.planlog.EventLog ;
import org.cougaar.tools.castellan.pdu.* ;
import org.cougaar.tools.castellan.analysis.* ;
import org.cougaar.tools.alf.* ;
import java.util.* ;

/**
 *
 * @author  bbowles
 */
public class AgentStats {
   
   // ATTRIBUTES
   private String myAgentName;
   private EventLog myEventLog;
   private ArrayList myExecutionStats = new ArrayList();
   private ArrayList myCompletedTasks = new ArrayList();
   private StatSet myExTimes = new StatSet();
   private PlanLogDatabase myPlanLog;
   

   // CONSTRUCTORS
   /** Creates a new instance of AgentStats */
   public AgentStats( EventLog theLog, String theAgentName, 
         PlanLogDatabase thePlanLog, ArrayList theIncomingTaskLogs) {
      myEventLog = theLog;
      myAgentName = theAgentName;
      myPlanLog = thePlanLog;
      System.out.println();
      System.out.println( "******* AgentStats.constructor() AGENT:" + myAgentName + "*******" );
      System.out.println( "AGENT # OF INCOMING TASKS: " + theIncomingTaskLogs.size() );      
      processIncomingTaskLogs( theIncomingTaskLogs );
      System.out.println( "AGENT # OF COMPLETED TASKS: " + myCompletedTasks.size() );      
      buildContainer();
      System.out.println( "# OF ExecutionStats CREATED: " + myExecutionStats.size() );      
      collectStats();
      System.out.println( "******* END: AgentStats.constructor() ************" );      
      System.out.println();
   }
   
   // METHODS
   /**
    * Take the incoming tasks and determine which ones are associated with this
    * particular agent.  If the incoming task is associated with this agent,
    * determine when this task was completed.  Put the results in a collection
    * of CompletedTask for this AgentStat.
    **/
   private void processIncomingTaskLogs( ArrayList theIncomingTaskLogs ){
      TaskLog aTaskLog;
      BoundaryLog aBoundaryLog;
      Iterator it = theIncomingTaskLogs.iterator();
      CompletedTask aCompletedTask;
      StatSet aCompletionTimeStat;
      // Look at each incoming task
      while( it.hasNext() ){
         aTaskLog = (TaskLog)it.next();
         if( belongsToAgent( aTaskLog ) ){
            // Find the completion time associated with this incoming task
            aBoundaryLog = (BoundaryLog)aTaskLog;
            aCompletionTimeStat = getCompletionTime( aBoundaryLog );
            // Create a CompletedTask and add it to the agents list
            aCompletedTask = new CompletedTask( aTaskLog, aCompletionTimeStat );
            myCompletedTasks.add( aCompletedTask );
         }
      }
   }
            
   private boolean belongsToAgent( TaskLog theTaskLog ){
      if( myAgentName.equals( theTaskLog.getCluster() ) ){
          return true;
      }
      return false;
   }

   private StatSet getCompletionTime( BoundaryLog theBoundaryLog ) {
      // Make sure the task is completed, i.e. it should not have any
      // Unknown Or Incomplete Descendents
      StatSet completionTimeStat = new StatSet();
      Iterator it = theBoundaryLog.getUnknownOrIncompleteDescendents();
      if( it.hasNext() ){
         // throw this one out, it is not completed! t = 0.
         return null;
      }
      it = theBoundaryLog.getOutgoingDescendents();
      TaskLog tl;
      UIDPDU uid;
      PlanElementLog PELog;
      long creationTime;
      while( it.hasNext() ){
         // Get the PlanElement associated(same UIDPDU) with each boundary log 
         tl = (TaskLog)it.next();
         uid = tl.getUID();
         PELog = myPlanLog.getPlanElementLogForTask( uid );
         // StatSet will keep a tally of creation times.  We will be specifically
         // interested in max ( to provide task completion time), and generally 
         // interested in max and/or avg.  They will give us some idea if the 
         // descendant tasks are processed concurrently or not.
         if( PELog != null ){
            completionTimeStat.addDatum( PELog.getCreatedTime() );
         }
      }
      return completionTimeStat;
   }
         
         

   public String getAgentName() {
      return myAgentName;
   }   
  
   /**
    * Collect stats for each execution associated with this agent.
    **/
   public void buildContainer(){
      // Get all of the plugin executions associated with this Agent.
      Iterator executionIter = myEventLog.getExecutionsByAgent( myAgentName );
      // For each execution, collect execution stats.
      ExecutionPDU execution = null;
      while( executionIter.hasNext() ){
         // Get the execution
         execution = (ExecutionPDU)executionIter.next();
         // Create ExecutionStats
         ExecutionStats es = new ExecutionStats( myEventLog, execution, 
                             myAgentName, myCompletedTasks );
         // Add ExecutionStats to the collection
         myExecutionStats.add( es );
      }
   }
   /**
    * Get collection of stats that were collected for each execution associated
    * with this agent.
    **/
   private void collectStats(){
      cummulateExecutionStats();
   }
   
   /**
    * Get the collection of statistics from this Agent.
    **/
   public Iterator getStats(){
      return myExecutionStats.iterator();
   }
      
   /**
    * Collect cumulative statistics on the executions times associated with this Agent.
    * Typically, there are many executions where nothing actually happens, i.e.
    * execution elapsed time = 0.  These executions are not included in determining
    * execution time statistics.
    **/
   private void cummulateExecutionStats(){
      if( myExecutionStats == null ){
         System.out.println( "AgentStats.getStats()---> myExecutionStats = null" );
         return;
      }
      else if( myExecutionStats.isEmpty() ){
         System.out.println( "AgentStats.getStats()---> myExecutionStats = isEmpty" );
         return;
      }
      else{
         ExecutionStats es = null;
         long anExecutionTime = 0;
         for( int cnt = 0; cnt < myExecutionStats.size(); cnt++ ){
            es = (ExecutionStats)myExecutionStats.get( cnt );
            anExecutionTime = es.getExecutionElapsedTime();
            if( anExecutionTime != 0 ){
                myExTimes.addDatum( anExecutionTime );
            }
         }
      }
   }
                  
   /**
    * Print out the cumulative statistics for this agent and then the statistics
    * provided for each execution.
    **/
   public String toString(){
      return "\n" + "AGENT STATS for: " + getAgentName() +
      "\n" + "\t" +
      " EXECUTION TIME STATS " +  
      "\n" + "\t" + "\t" +
      " Total # of executions: "  + myExecutionStats.size() + 
      "\n" + "\t" + "\t" +
      " Total NONZERO # executions: " + myExTimes.getCount() + 
      "\n" + "\t" + "\t" +
      " Total execution time: " + myExTimes.getTotal() + 
      "\n" + "\t" + "\t" +
      " Minimum execution time: " + myExTimes.getMin() +
      "\n" + "\t" + "\t" +
      " Maximum execution time: " + myExTimes.getMax() +
      "\n" + "\t" + "\t" +
      " Average execution time: " + myExTimes.getAverage() +
      "\n" + "\t" +
      myExecutionStats.toString() + "\n";
   }
   /**
    * Print out the ExecutionPDUs associated with this agent
    **/
   public String toStringVerbose(){
      String out = toString();
      for( int cnt = 0; cnt < myExecutionStats.size(); cnt++ ){
         out = out + ((ExecutionStats)myExecutionStats.get( cnt )).toStringVerbose();
      }
      return out;
   }
}
