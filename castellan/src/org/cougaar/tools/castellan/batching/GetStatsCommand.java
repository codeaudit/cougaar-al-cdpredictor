/*
 * GetStatsCommand.java
 *
 * Created on May 21, 2002, 2:10 PM
 */

package org.cougaar.tools.castellan.batching;

import org.cougaar.tools.castellan.planlog.*;
import java.lang.*;
import java.util.*;

/**
 *
 * @author  bbowles
 */
 abstract public class GetStatsCommand {
   
   // ATTRIBUTES
   EventLog myEventLog;
   
   // CONSTRUCTORS
   public GetStatsCommand( EventLog theEventLog ) {
      myEventLog = theEventLog;
   }
   
   // METHODS
   abstract public Iterator getStats();
}