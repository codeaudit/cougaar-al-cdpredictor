/*
 * AgentExecStats.java
 *
 * Created on May 30, 2002, 9:46 PM
 */

package org.cougaar.tools.castellan.statistics;

import org.cougaar.tools.castellan.planlog.EventLog ;
import org.cougaar.tools.castellan.pdu.* ;
import java.util.* ;

/**
 *
 * @author  bbowles
 */
public class AgentStats {
   
   // ATTRIBUTES
   private String myAgentName;
   private int totalExecutionCount = 0;
   private int totalExecutionWithEventsCount = 0;
   private long totalExecutionTime = 0;
   private ArrayList myExecutionStats;
   private EventLog myEventLog;
   
   private long myMinExecutionTime = 0;
   private long myMaxExecutionTime = 0;
   private long myAvgExecutionTime = 0;

   int myMinIncomingTaskBatchSize = 0;
   int myMaxIncomingTaskBatchSize = 0;
   int myAvgIncomingTaskBatchSize = 0;
   int myTotalBatchCount = 0;
   
   long myMinIncomingTaskBatchCompletionTime = 0;
   long myMaxIncomingTaskBatchCompletionTime = 0;
   long myAvgIncomingTaskBatchCompletionTime = 0;

   // CONSTRUCTORS
   /** Creates a new instance of AgentStats */
   public AgentStats(EventLog theLog, String theAgentName) {
      myEventLog = theLog;
      myAgentName = theAgentName;
      myExecutionStats = new ArrayList();
   }
   
   // ACCESSOR METHODS
   public String getAgentName() {
      return myAgentName;
   }   
   public int getExecutionCount() {
      return totalExecutionCount;
   }
   public int getExecutionWithEventsCount() {
      return totalExecutionWithEventsCount;
   }
   public long getTotalExecutionTime() {
      return totalExecutionTime;
   }
   public long getMinExecutionTime() {
      return myMinExecutionTime;
   }
   public long getMaxExecutionTime() {
      return myMaxExecutionTime;
   }
   public long getAvgExecutionTime() {
      return myAvgExecutionTime;
   }
   public int getTotalBatchCount() {
      return myTotalBatchCount;
   }
   public int getMinIncomingTaskBatchSize() {
      return myMinIncomingTaskBatchSize;
   }
   public int getMaxIncomingTaskBatchSize() {
      return myMaxIncomingTaskBatchSize;
   }
   public int getAvgIncomingTaskBatchSize() {
      return myAvgIncomingTaskBatchSize;
   }
   public long getMinIncomingTaskBatchCompletionTime() {
      return myMinIncomingTaskBatchCompletionTime;
   }
   public long getMaxIncomingTaskBatchCompletionTime() {
      return myMaxIncomingTaskBatchCompletionTime;
   }
   public long getAvgIncomingTaskBatchCompletionTime() {
      return myAvgIncomingTaskBatchCompletionTime;
   }
  
   /**
    * Collect stats for each execution associated with this agent.
    **/
   public void collectStats(){
      // Get all of the plugin executions associated with this Agent.
      Iterator executionIter = myEventLog.getExecutionsByAgent( myAgentName );
      // For each execution, collect execution stats.
      ExecutionPDU execution = null;
      while( executionIter.hasNext() ){
         // Get the execution
         execution = (ExecutionPDU)executionIter.next();
         // tally agent's number of executions
         totalExecutionCount++;
         // collect stats on this execution
         ExecutionStats es = new ExecutionStats( myEventLog, execution, myAgentName );
         es.collectStats();
         // tally agent's total execution time
         totalExecutionTime = totalExecutionTime + es.getExecution().getElapsedTime();
         // get the stats, add to the list if they are nontrivial.
         // nontrivial stats are executions with events associated with them.
         if( es.getEventCount() > 0 ){
            // add execution stats to collection
            myExecutionStats.add( es );
            // tally num of non trivial executions
            totalExecutionWithEventsCount++;
         }
      }
   }
   /**
    * Get collection of stats that were collected for each execution associated
    * with this agent.
    **/
   public Iterator getStats(){
      if( myExecutionStats == null ){
         System.out.println( "AgentStats.getStats()---> myExecutionStats = null" );
         return null;
      }
      else if( myExecutionStats.isEmpty() ){
         System.out.println( "AgentStats.getStats()---> myExecutionStats = isEmpty" );
         return null;
      }
      else{
         cummulateExecutionStats();
         return myExecutionStats.iterator();
      }
   }
   
   /**
    * Collect cumulative statistics on the executions associated with this Agent.
    **/
   private void cummulateExecutionStats(){
      long anExecutionTime = 0;
      long totalExecutionTime = 0;
      int totalExecutionCount = 0;
      
      int aTotalBatchSize = 0;
      int aTotalBatchCount = 0;
      int anAvgBatchSize = 0;
      int aBatchSize = 0;

      long aBatchCompletionTime = 0;
      long totalBatchCompletionTime = 0;
      int totalBatchCompletionCount = 0;
      
      ExecutionStats es = null;
      for( int cnt = 0; cnt < myExecutionStats.size(); cnt++ ){
         es = (ExecutionStats)myExecutionStats.get( cnt );
         anExecutionTime = es.getExecutionElapsedTime();
         // First accumulate execution time stats for agent
         // bb - might want to change this, we see things happening in an execution
         // bb -  cycle whose duration = 0.  But I am not adding those executions
         // bb - to the statistics for now.
         if( anExecutionTime != 0 ){
            if( ( myMinExecutionTime == 0 ) || ( myMinExecutionTime > anExecutionTime ) ){
               myMinExecutionTime = anExecutionTime;
            }
            if( ( myMaxExecutionTime == 0 ) || ( myMaxExecutionTime < anExecutionTime ) ){
               myMaxExecutionTime = anExecutionTime;
            }
            totalExecutionCount++;
            totalExecutionTime = totalExecutionTime + anExecutionTime;
         }
         // Second accumulate incoming (Batch) tasks stats for agent
         aBatchSize = es.getIncomingBatchSize();
         // If the batch size was zero, then there are no batching stats.
         // bb - I use '>0' test because avg batch size may have round off error.
         if( aBatchSize > 0 ){
            if( ( myMinIncomingTaskBatchSize == 0 ) || ( myMinIncomingTaskBatchSize > aBatchSize ) ){
               myMinIncomingTaskBatchSize = aBatchSize;
            }
            if( ( myMaxIncomingTaskBatchSize == 0 ) || ( myMaxIncomingTaskBatchSize < aBatchSize ) ){
               myMaxIncomingTaskBatchSize = aBatchSize;
            }
            aTotalBatchCount++;
            aTotalBatchSize = aTotalBatchSize + aBatchSize;
         }
         // Third accumulate Incoming batch completion time stats for the agent
         aBatchCompletionTime = es.getIncomingAvgTime();
         // If the batch size was zero, then there are no batching stats.
         // bb - I use '>0' test because avg batch completion time may have round off error.
         if( aBatchCompletionTime > 0 ){
            if( ( myMinIncomingTaskBatchCompletionTime == 0 ) || 
               ( myMinIncomingTaskBatchCompletionTime > aBatchCompletionTime ) ){
               myMinIncomingTaskBatchCompletionTime = aBatchCompletionTime;
            }
            if( ( myMaxIncomingTaskBatchCompletionTime == 0 ) || 
               ( myMaxIncomingTaskBatchCompletionTime < aBatchCompletionTime ) ){
               myMaxIncomingTaskBatchCompletionTime = aBatchCompletionTime;
            }
            totalBatchCompletionCount++;
            totalBatchCompletionTime = totalBatchCompletionTime + aBatchCompletionTime;
         }
      }
      // Finally, collect the cumulative averages of execution stats for agent;
      myAvgExecutionTime = Math.round( totalExecutionTime / totalExecutionCount );
      myAvgIncomingTaskBatchSize = Math.round( aTotalBatchSize / aTotalBatchCount );
      myTotalBatchCount = aTotalBatchCount;
      myAvgIncomingTaskBatchCompletionTime = 
         Math.round( totalBatchCompletionTime / totalBatchCompletionCount );
   } 
                  
   /**
    * Print out the cumulative statistics for this agent and then the statistics
    * provided for each execution.
    **/
   public String toString(){
      return "AGENT STATS [ Name: " + getAgentName() +
      "\n" + "\t" +
      " Total # of executions: "  + getExecutionCount() + 
      "\n" + "\t" +
      " Total # of executions with events: " + getExecutionWithEventsCount() + 
      "\n" + "\t" +
      " EXECUTION TIME STATS " + getTotalExecutionTime() + 
      "\n" + "\t" + "\t" +
      " Total execution time: " + getTotalExecutionTime() + 
      "\n" + "\t" + "\t" +
      " Minimum execution time: " + getMinExecutionTime() +
      "\n" + "\t" + "\t" +
      " Maximum execution time: " + getMaxExecutionTime() +
      "\n" + "\t" + "\t" +
      " Average execution time: " + getAvgExecutionTime() +
      "\n" + "\t" +
      " INCOMING TASK BATCH STATS: " + getTotalExecutionTime() + 
      "\n" + "\t" + "\t" +
      " Total # of incoming batches: " + getTotalExecutionTime() + 
      "\n" + "\t" + "\t" +
      " Minimum Batch Size: " +getMinIncomingTaskBatchSize() +
      "\n" + "\t" + "\t" +
      " Maximum Batch Size: " + getMaxIncomingTaskBatchSize() +
      "\n" + "\t" + "\t" +
      " Average Batch Size: " + getAvgIncomingTaskBatchSize() +
      "\n" + "\t" +
      " Minimum Batch Task Completion Time: " +getMinIncomingTaskBatchCompletionTime() +
      "\n" + "\t" + "\t" +
      " Maximum Batch Task Completion Time: " + getMaxIncomingTaskBatchCompletionTime() +
      "\n" + "\t" + "\t" +
      " Average Batch Task Completion Time " + getAvgIncomingTaskBatchCompletionTime() +
      "\n" + "\t" +
      " ]" + myExecutionStats.toString();
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
