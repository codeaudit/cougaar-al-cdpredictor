/*
 * BatchStats.java
 *
 * Created on June 10, 2002, 1:15 AM
 */

package org.cougaar.tools.castellan.batching;

import java.util.* ;
import org.cougaar.tools.castellan.analysis.* ;
/**
 *
 * @author  bbowles
 */
public class BatchStats {
   
   private StatSet myCompletionTimeStats = new StatSet();
   
   private ArrayList myBatch;
   
   /** Creates a new instance of BatchStats */
   public BatchStats( ArrayList theBatch ) {
      myBatch = theBatch;
      collectStats();
   }
   private void collectStats(){
      CompletedTask aCT;
      long aDeltaTime = 0;
      Iterator it = myBatch.iterator();
      while( it.hasNext() ){
         aCT = (CompletedTask)it.next();
         aDeltaTime = aCT.getTimeToComplete();
         myCompletionTimeStats.addDatum( aDeltaTime );
      }
   }
   
   ArrayList getBatch() {
      return myBatch;
   }
   
   StatSet getCompletionTimeStats() {
      return myCompletionTimeStats;
   }
   
   public String toString() {
      CompletedTask ct = (CompletedTask)myBatch.get(0);
      TaskLog aTaskLog = ct.getTaskLog();
      String aTaskVerb = aTaskLog.getTaskVerb();
      return "TASK BATCH COMPLETION TIME STATS: " + "\n" + 
      "Batch on task with verb: " + aTaskVerb + "\n" +
      myCompletionTimeStats.toString();
   }
   
}
