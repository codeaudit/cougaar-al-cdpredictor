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
      
      boolean myPrintTest = false;
      if( !theCompletedTasks.isEmpty() ){
          processCompletedTasks( theCompletedTasks );
          if( !myCompletedTasks.isEmpty() ){
              myPrintTest = true;
          }
      }
      if( myPrintTest ){
          System.out.println( "\t" + "********* ExecutionStats.constructor() ********" );
          System.out.println( "\t" + "AGENT # OF CompletedTasks ENTERED: " 
            + theCompletedTasks.size() );
          System.out.println( "\t" + "EXECUTION # OF CompletedTasks: " 
            + myCompletedTasks.size() );
      }
      buildContainers();
      if( !myBatches.isEmpty() ){
          System.out.println( "\t" + "\t" + "EXECUTION # OF Batches CREATED: " 
            + myBatches.size() );
      }
      
      collectStats();
      if( !myBatchStats.isEmpty() ){
          System.out.println( "\t" + "\t" + "EXECUTION # OF BatchStats CREATED: " 
            + myBatchStats.size() );
      }
      if( myPrintTest ){
          System.out.println( "\t" + "****** END: ExecutionStats.constructor() ******" );
          System.out.println();
      }
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
      long aCompletionTime = theCompletedTask.getCompletionTime();
      if( ( aCompletionTime >= myExecution.getStartTime() ) &&
          ( aCompletionTime <= myExecution.getStopTime() ) ){
              System.out.println( "\t" + "\t" + "\t" + "Task completed during this execution:" );
              System.out.println( "\t" + "\t" + "\t" + myExecution.getStartTime() + " <= " +
                   aCompletionTime + " <= " + myExecution.getStopTime() );                   
              return true;
      }
      else{
         return false;
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
      // Go through the current list of myBatches to see where this CompletedTask belongs.
      while( it.hasNext() ){
         // Get a Batch out of the collection.
         aBatch = (ArrayList)it.next();
         // Pull out a Task in the batch to compare the new CompletedTask with
         aTaskInBatch = (CompletedTask)aBatch.get(0);
         // Compare the two CompleteTask s to see if the new one should be 
         // added to this Batch.
         if( shouldBeInThisBatch( theCompletedTask, aTaskInBatch) ){
            aBatch.add( theCompletedTask );
            return;
         }
      }
      // If no batch was found that this completed task should 
      // belong to, create a new batch and add the task to it.
      ArrayList aNewBatch = new ArrayList(1);
      aNewBatch.add( theCompletedTask );
      // Add the new Batch to the collection of myBatches
      myBatches.add( aNewBatch );
   }
   /**
    * This condition is set by the tasks having the same verb.  Other
    * conditions could be used in the future.
    **/ 
   private boolean shouldBeInThisBatch( CompletedTask theCompletedTask,
                                        CompletedTask theTaskInBatch){
      String theVerbForThisBatch = theTaskInBatch.getTaskLog().getTaskVerb();                                      
      if( theVerbForThisBatch.equals( theCompletedTask.getTaskLog().getTaskVerb() ) ){
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
    * Currently we are collecting stats on batches that completed in this agent
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
    * Prints out the statistics associated with this execution. Only add this
    * ExecutionStat if something non trivial happens, i.e.
    *  - Task Batching occurred.
    *  - Leave it at that for now.
    **/
   public String toString(){
       if( myBatchStats.size() == 0 ){
           return "";
       }
       //if( getExecutionElapsedTime() == 0 ){
       //    return "N/A";
       //}
       return "\n" + "\t" + "\t" + "EXECUTION STATS" + 
       "\n" + "\t" + "\t" + "\t" +
       " Time: " + getExecutionStartTime() + 
       " - " + getExecutionStopTime() +
       "\n" + "\t" + "\t" + "\t" +
       " Elapsed time: " + getExecutionElapsedTime() + 
       "\n" + "\t" + "\t" + "\t" +
       myBatchStats.toString() + "\n";
   }
   
   /**
    * Prints out each EventPDU associated with this ExecutionPDU.
    **/
   public String toStringVerbose(){
      return toString() + myExecution.toString() + myBatchStats.toString();
   }      
}
