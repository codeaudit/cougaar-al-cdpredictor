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
      return "Completed Task Information: " + "\n" +
      myTaskLog.toString() + "\n" + 
      "Task completion time: \n" + 
      "# of descendants: " + myCompletionTimeStat.getCount() + "\n" +
      "MIN Time = " + myCompletionTimeStat.getMin() + "\n" +
      "MAX TIME = " + myCompletionTimeStat.getMax() + "\n" +
      "AVG Time = " + myCompletionTimeStat.getAverage() + "\n";
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
