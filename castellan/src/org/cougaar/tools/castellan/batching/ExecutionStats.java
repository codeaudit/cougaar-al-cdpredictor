/*
 * ExecutionStats.java
 *
 * Created on May 31, 2002, 1:10 PM
 */

package org.cougaar.tools.castellan.batching;

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
   
   private EventLog myEventLog;
   private ExecutionPDU myExecution;
   private ArrayList myCompletedTasks = new ArrayList();
   private ArrayList myBatches = new ArrayList();
   private ArrayList myBatchStats = new ArrayList();
   private String myAgent;
   
   /** Creates a new instance of ExecutionStats */
   public ExecutionStats( EventLog theLog, ExecutionPDU theExecution, 
                          String theAgent, ArrayList theCompletedTasks ) {
      myEventLog = theLog;
      myExecution = theExecution;
      myAgent = theAgent;
      processCompletedTasks( theCompletedTasks );
      buildContainers();
      collectStats();
   }
 
   private void processCompletedTasks( ArrayList theCompletedTasks ){
      CompletedTask ct;
      Iterator it = theCompletedTasks.iterator();
      while( it.hasNext() ){
         ct = (CompletedTask)it.next();
         if( isCompletedDuringThisExecution( ct ) ){
            myCompletedTasks.add( ct );
         }
      }
   }
   
   private boolean isCompletedDuringThisExecution( CompletedTask theCompletedTask ){
      if( ( theCompletedTask.getCompletionTime() <= myExecution.getStartTime() ) ||
          ( theCompletedTask.getCompletionTime() >= myExecution.getStopTime() ) ){
             return false;
      }
      else{
         return true;
      }
   }
   
   private void buildContainers(){
      CompletedTask aCT;
      Iterator it = myCompletedTasks.iterator();
      while( it.hasNext() ){
         aCT = (CompletedTask)it.next();
         findBatch( aCT);
      }
   }
   private void findBatch( CompletedTask theCompletedTask ){
      ArrayList aBatch;
      Iterator it = myBatches.iterator();
      CompletedTask aTaskInBatch;
      while( it.hasNext() ){
         aBatch = (ArrayList)it.next();
         aTaskInBatch = (CompletedTask)aBatch.get(0);
         if( shouldBeInThisBatch( theCompletedTask, aTaskInBatch) ){
            aBatch.add( theCompletedTask );
            return;
         }
      }
      // If no batch was found that this completed task should 
      // belong to, create a new batch and add the task to it.
      ArrayList aNewBatch = new ArrayList(1);
      aNewBatch.add( theCompletedTask );
   }
   /**
    * This condition is set by the tasks having the same verb.  Other
    * conditions could be used in the future.
    **/ 
   private boolean shouldBeInThisBatch( CompletedTask theCompletedTask,
                                        CompletedTask theTaskInBatch){
      if( theCompletedTask.getTaskLog().getTaskVerb() == 
                  theTaskInBatch.getTaskLog().getTaskVerb() ){
        return true;
      }
      else{
         return false;
      }
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
   
   ExecutionPDU getExecution(){
      return myExecution;
   }
   
   ArrayList getCompletedTasks() {
      if( myCompletedTasks.size() == 0 ){
         return null;
      }
      else{
         return myCompletedTasks;
      }
   }
    ArrayList getBatches() {
      if( myBatches.size() == 0 ){
         return null;
      }
      else{
         return myBatches;
      }
   }

   /**
    * Determines if a CompletedTask is associated with this execution 
    * cycle for this agent.  It is if its completion time occurs during
    * the execution time interval.
    **/
   boolean isAssociated( CompletedTask theCompletedTask, ExecutionPDU theExecution ){
      if( theCompletedTask.getCompletionTime() >= theExecution.getStartTime() &&
          theCompletedTask.getCompletionTime() <= theExecution.getStopTime() ){
         return true;
      }
      else{
         return false;
      }
   }
   
   /**
    * Currently we are collecting stats on batches that completed int this agent
    * during this execution cycle.  Batches differ by Task verb.
    */
   public void collectStats(){
      ArrayList aBatch;
      Iterator it = myBatches.iterator();
      while( it.hasNext() ){
         aBatch = (ArrayList)it.next();
         BatchStats aBS = new BatchStats( aBatch );
         myBatchStats.add( aBS );
      }         
   }
      
   /**
    * Prints out the statistics associated with this execution.
    **/
   public String toString(){
       return "\n" + "\t" + "\t" + "EXECUTION STATS" + 
       "\n" + "\t" + "\t" + "\t" +
       " Time: " + getExecutionStartTime() + 
       " - " + getExecutionStopTime() +
       "\n" + "\t" + "\t" + "\t" +
       " Elapsed time: " + getExecutionElapsedTime() + 
       "\n" + "\t" + "\t" + "\t" +
       myBatchStats.toString();
   }
   
   /**
    * Prints out each EventPDU associated with this ExecutionPDU.
    **/
   public String toStringVerbose(){
      return toString() + myExecution.toString() + myBatchStats.toString();
   }      
}
