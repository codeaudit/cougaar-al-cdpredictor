package com.axiom.lib.util;
import java.util.*;

/** Progress events are emitted from Progressable objects.
 */

public class ProgressEvent extends EventObject {
   public final static String PROGRESS_ABORT = "Abort";
   public final static String PROGRESS_HALT = "Halt";
   public final static String PROGRESS_START = "Start";
   public final static String PROGRESS_RESUME = "Unhalt";
   public final static String PROGRESS_OCCURRED = "Value";
   public final static String PROGRESS_COMPLETE = "Complete";

   public ProgressEvent( Progressable p, String type ) {
      super( p );
      this.type = type ;
   }
   
   public ProgressEvent( Progressable p, int value, Object message ) {
      super( p );
      this.type = PROGRESS_OCCURRED ;
      this.value = value ;
      this.message = message ;
   }
   
   public ProgressEvent( Progressable p, String type, int value, int lower, int upper, Object message ) {
      super( p );
      this.type = type ;
      this.value = value ;
      this.lower = lower ;
      this.upper = upper ;
      this.message = message ;
   }
  
   public Progressable getProgressSource() {
      return ( Progressable ) getSource() ;
   }
   
   public String getType() {
      return type ;
   }
   
   public void setMessage( Object object ) {
      message = object ;
   }
   
   public Object getMessage() {
      return message ;
   }
   
   public int getMinimum() {
      return lower ;
   }
   
   public int getMaximum() {
      return upper ;
   }
   
   public int getValue() {
      return value ;
   }

   protected String type ;
   protected Object message ;
   protected int value, lower, upper;
}
