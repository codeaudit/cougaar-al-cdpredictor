/*
 * StatSet.java
 *
 * Created on June 9, 2002, 3:42 PM
 */

package org.cougaar.tools.castellan.batching;

/**
 *
 * @author  bbowles
 */
public class StatSet {
   
   private static final long NOTSET = -1;
   public int count = 0;
   public long min = NOTSET;
   public long max = NOTSET;
   public long avg = NOTSET;
   public long total = 0;
   
   /** Creates a new instance of statset */
   public StatSet() {
   }
   public long getCount(){
      return count;
   }
   public long getMin(){
      return min;
   }
   public long getMax(){
      return max;
   }
   public long getTotal(){
      return total;
   }
   
   long getAverage() {
      if( count != 0 ){
         avg = Math.round( total / count );
      }
      else{
         avg = 0;
      }
      return avg;
   }
   public String toString() {
      return " StatSet: " + 
      " Total = " + getTotal() +
      " Count = " + getCount() +
      " Average = " + getAverage() +
      " Min = " + getMin() + 
      " Max = " + getMax();
   }
   
   void addDatum( long theDatum ){
      count++;
      total = total + theDatum;
      if( min == NOTSET || min > theDatum ){
         min = theDatum;
      }
      if( max == NOTSET || max < theDatum ){
         max = theDatum;
      }
   }
   
}
