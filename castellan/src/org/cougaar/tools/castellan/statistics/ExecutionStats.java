/*
 * ExecutionStats.java
 *
 * Created on May 31, 2002, 1:10 PM
 */

package org.cougaar.tools.castellan.statistics;

import org.cougaar.tools.castellan.analysis.* ;
import org.cougaar.tools.castellan.pdu.* ;
import org.cougaar.tools.castellan.planlog.* ;
import org.cougaar.tools.alf.* ;
import java.util.*;
import java.lang.* ;
import java.io.*;

/**
 *
 * @author  bbowles
 */
public class ExecutionStats {
   
   private PlanLogDatabase  myPLD;
   private EventLog myEventLog;
   private ExecutionPDU myExecution;
   private ArrayList myEvents; 
   private int myTaskCount = 0;
   private ArrayList incomingBatch = new ArrayList();
   private ArrayList processBatch = new ArrayList();
   private ArrayList droppedTasks = new ArrayList(); // can be TaskPDU or TaskLog!
   private String myAgent;
   // Statistics for time it takes to complete the processing of tasks that 
   // enter the system in a batch.
   private long myIncomingMinTime = 0;
   private long myIncomingMaxTime = 0;
   private long myIncomingAvgTime = 0;

   
   /** Creates a new instance of ExecutionStats */
   public ExecutionStats( EventLog theLog, ExecutionPDU theExecution, String theAgent ) {
      myEventLog = theLog;
//      myALO = new AgentLoadObserver( myEventLog );
      myExecution = theExecution;
      myEvents = new ArrayList();
      myAgent = theAgent;
   }
   
   long getExecutionStartTime(){
      return myExecution.getStartTime();
   }
   
   long getExecutionStopTime(){
      return myExecution.getStopTime();
   }
   
   long getExecutionElapsedTime(){
      return myExecution.getElapsedTime();
   }
   
   Iterator getIncomingBatch(){
      return incomingBatch.iterator();
   }

   int getIncomingBatchSize(){
      return incomingBatch.size();
   }
   
   ExecutionPDU getExecution(){
      return myExecution;
   }
   
   ArrayList getEvents() {
      if( myEvents.size() == 0 ){
         return null;
      }
      else{
         return myEvents;
      }
   }
   
   int getEventCount() {
      return myEvents.size();
   }
   
   int getTaskCount() {
      return myTaskCount;
   }
   
   long getIncomingMinTime(){
      return myIncomingMinTime;
   }

   long getIncomingMaxTime(){
      return myIncomingMaxTime;
   }

   long getIncomingAvgTime(){
      return myIncomingAvgTime;
   }

   /**
    * Determines if an event is associated with an specific execution cycle of 
    * the agent.
    **/
   boolean isAssociated( EventPDU theEvent, ExecutionPDU theExecution ){
      if( theEvent.getTransactionId() == theExecution.getTransactionId() ){
         return true;
      }
      else{
         return false;
      }
   }
   
   /**
    * Collect all of the events associated with this execution time and collect
    * statistics on the different types of events.  Currently Task stats are
    * collected that identify the batching behavior of Tasks entering the Agent
    * average, min, max size of batches and average, min, max time to complete.
    */
   public void collectStats(){
      // Get all of the EventPDUs that occurred during this ExecutionPDU's time interval
      Iterator eventIter = myEventLog.getEvents( 
               myExecution.getStartTime(), myExecution.getStopTime() );
      // Find all EventPDU's that are related to this execution.
      EventPDU event;
      while( eventIter.hasNext() ){
         event = null;
         // Get the next EventPDU in the Container
         event = ( EventPDU )eventIter.next();
         // Check if the EventPDU occurred during this ExecutionPDU's time interval.
         if( isAssociated( event, myExecution ) ){
            // add event to collection of associated myEvents
            myEvents.add( event );
            // collect data on task events
            // test if the event is a task
            if( event instanceof TaskPDU ){
               collectTasks( (TaskPDU)event );
            }
         }
      }
   }
   
   /**
    * Determines where theTask comes from: generated outside of this agent,
    * inside of this agent, or indeterminent. Collects tasks into different
    * categories.
    */
   private void collectTasks( TaskPDU theTask ){
      // Check that a transaction ocurred for this task, if not, this task
      // was dropped somehow and should not be included in stats.
      if( theTask.getTransactionId() == -1 ){
         //  Keep list of dropped tasks, do not add to stats. 
         droppedTasks.add( theTask );
      }
      // tally the number of tasks associated with this execution.
      myTaskCount++;
      // Determine where this task originated and process based on origin.
      if( theTask.getSource() == myAgent ){
         // task generated from inside this agent
         processBatch.add( theTask );
      }
      else{
         // Incoming Task from another agent
         incomingBatch.add( theTask );
      }
   }
   
   /*
    * Processes incomingBatch of Tasks and collect batch stats.
    */
   private void processIncomingTasks( ArrayList tasks ){
      long minTime = 0;
      long maxTime = 0;
      double avgTime = 0;
      long deltaTime = 0;
      long completionTime = 0;
      long enterTime = 0;
      long totalTime = 0;
      TaskPDU theTask = null;
      buildPLD( myEventLog );
      // iteratae through the set of tasks
      for( int cnt = 0; cnt < tasks.size(); cnt++ ){
         completionTime = 0;
         enterTime = 0;
         // Get the task
         theTask = (TaskPDU)tasks.get( cnt );
         // Get the task start time
         enterTime = theTask.getExecutionTime();
         // Determine task completion time
         completionTime = getTaskCompletionTime( theTask );
         // Only process stats on tasks that have a nonzero completion time.
         if( completionTime > 0 ){
            deltaTime = completionTime - enterTime;
            // Tally total task execution time
            totalTime = totalTime + deltaTime;
            // Get the min execution time
            if( myIncomingMinTime == 0 ){
               myIncomingMinTime = deltaTime;
            }
            else if( deltaTime < myIncomingMinTime ){
               myIncomingMinTime = enterTime;
            }
            // Get the max execution time
            if( myIncomingMaxTime == 0 ){
               myIncomingMaxTime = deltaTime;
            }
            else if( deltaTime > myIncomingMaxTime ){
               myIncomingMaxTime = deltaTime;
            }
         }
         else{
            // Some type of error occurred with this task.  Tag it.
            droppedTasks.add( theTask );
         }
      }
      int numTasks = tasks.size();
      if( numTasks == 0 ){
         myIncomingAvgTime = 0;
      }
      else{
         myIncomingAvgTime = Math.round( totalTime / tasks.size() );
      }
   }
   
   /**
    * Build the tool necessary for traversing the task dependancy tree.
    **/
   private  void buildPLD( EventLog theLog ){
        myPLD = new PlanLogDatabase() ;
        AgentWorkflowLogPlanBuilder builder = new AgentWorkflowLogPlanBuilder( myPLD  ) ;
        SocietyDesc sd = new SocietyDesc() ;
        //myALO = new AgentLoadObserver( myPLD, sd ) ;
        // Needed as input argument for processPDU(), but I am not using it yet.
        ArrayList dummy = new ArrayList();
        // Build PlanLogDatabase
        for (Iterator iter = theLog.getEvents(); iter.hasNext(); ) {
            PDU pdu = ( PDU ) iter.next() ;
//            myALO.processPDU( pdu, dummy );
        }
   }
       
   /**
    * Determine the leaf dependancies for each task and then determines when the
    * last leaf dependancy was allocated assets.  This is our approximation of 
    * when an incoming task's completion time - from time of arrival to time 
    * last dependent was processed.
    **/
   private long getTaskCompletionTime( TaskPDU theTask ){
      // Get Task Log of theTask
      TaskLog tl = (TaskLog)myPLD.getLog( theTask.getUID() );
      // Find leaf descendents of Task
      ArrayList terminal = new ArrayList();
      ArrayList outgoing = new ArrayList();      
      ArrayList internal = new ArrayList();
      ArrayList unknown = new ArrayList();      
//      myALO.findLeafDescendants( tl, terminal, outgoing, internal, unknown);
      // If any unknown tasks exists, it should be added to the error list.
      for( int cnt = 0; cnt < internal.size(); cnt++ ){
         droppedTasks.add( unknown.get( cnt ) );
      }
      // Retrieve the PlanLog Element for each terminalAndOutgoing TaskLog
      TaskLog leaf;
      long maxTime = 0;
      long createdTime = 0;
      for( int cnt = 0; cnt < terminal.size(); cnt++ ){
         leaf = (TaskLog)terminal.get( cnt );
         PlanElementLog pel = myPLD.getPlanElementLogForTask( leaf.getUID() );
         // Find the latest time that a leaf task was completed.  This is the 
         // estimate for completion time of the incoming Task.
         if ( pel != null ){
            createdTime = pel.getCreatedTime();
            if( (maxTime == 0) || (maxTime < createdTime) ){
               maxTime = createdTime;
            }
         }
      }
      return maxTime;
   }

   /**
    * Prints out the statistics associated with this execution.
    **/
   public String toString(){
      int incomingSize = 0;
      int processSize = 0;
      int dropSize = 0;
      if( incomingBatch != null ){
         incomingSize = incomingBatch.size();
      }
      if( processBatch != null ){
         processSize = processBatch.size();
      }
      if( droppedTasks != null ){
         dropSize = droppedTasks.size();
      }
       return "\n" + "\t" + "\t" + "EXECUTION STATS" + 
       "\n" + "\t" + "\t" + "\t" +
       " Time: " + getExecutionStartTime() + 
       " - " + getExecutionStopTime() +
       "\n" + "\t" + "\t" + "\t" +
       " Elapsed time: " + getExecutionElapsedTime() + 
       "\n" + "\t" + "\t" + "\t" +
       " Total # events: " + getEventCount() + 
       "\n" + "\t" + "\t" + "\t" +
       " Total # tasks: " + getTaskCount() + 
       "\n" + "\t" + "\t" + "\t" +
       " INCOMING TASKS: " + 
       "\n" + "\t" + "\t" + "\t" + "\t" + 
       "Batch size = " + incomingSize +
       "\n" + "\t" + "\t" + "\t" + "\t" + 
       " Task Completion Times: " + 
       "\n" + "\t" + "\t" + "\t" + "\t" + 
       "AVG: " + getIncomingAvgTime() + 
       "\n" + "\t" + "\t" + "\t" + "\t" + 
       "MIN: " + getIncomingMinTime() + 
       "\n" + "\t" + "\t" + "\t" + "\t" + 
       "MAX: " + getIncomingMaxTime() + 
       "\n" + "\t" + "\t" + "\t" +
       " Tasks being processed: " + "Batch size = " + processSize +
       "\n" + "\t" + "\t" + "\t" +
       " Tasks that were somehow dropped: " + "Batch size = " + dropSize +
       "]";
   }
   
   /**
    * Prints out each EventPDU associated with this ExecutionPDU.
    **/
   public String toStringVerbose(){
      return toString() + myExecution.toString() + myEvents.toString();
   }      
}
