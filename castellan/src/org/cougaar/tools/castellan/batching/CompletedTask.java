/*
 * CompletedTask.java
 *
 * Created on June 9, 2002, 9:56 PM
 */

package org.cougaar.tools.castellan.batching;

import org.cougaar.tools.castellan.analysis.* ;

/**
 *
 * @author  bbowles
 */
public class CompletedTask {
   
   private TaskLog myTaskLog = null;
   
   private StatSet myCompletionTimeStat = null;
      
   /** Creates a new instance of CompletedTask */
   public CompletedTask( TaskLog theTaskLog, StatSet theCompletionTimeStat ) {
      myTaskLog = theTaskLog;
      myCompletionTimeStat = theCompletionTimeStat;
   }
   
   public String toString() {
      return "\n" + "\t" + "Completed Task Information: " + 
      "\n" + "\t" + myTaskLog.toString() + "\n" + 
      "# of descendants: " + myCompletionTimeStat.getCount() + 
      "\n" + "\t" + "Task completion time: \n" + 
      "\n" + "\t" + "\t" + "MIN Time = " + myCompletionTimeStat.getMin() + 
      "\n" + "\t" + "\t" + "MAX TIME = " + myCompletionTimeStat.getMax() +
      "\n" + "\t" + "\t" + "AVG Time = " + myCompletionTimeStat.getAverage() + "\n";
   }
   
   String getAgentName() {
      return myTaskLog.getCluster();
   }
   
   /**
    * The actual time when this task completed.
    **/
   long getCompletionTime() {
      return myCompletionTimeStat.getMax();
   }
   
   /**
    * Time it took for this task to complete.
    **/
   long getTimeToComplete() {
      return ( getCompletionTime() - myTaskLog.getCreatedTime() );
   }
   
   TaskLog getTaskLog(){
      return myTaskLog;
   }
}
