/*
  * Yunho Hong
  * email : yyh101@psu.edu
  * PSU, August , 2003
*/

package org.cougaar.tools.alf.sensor.plugin;

import org.cougaar.core.adaptivity.InterAgentCondition;
import org.cougaar.core.agent.service.alarm.PeriodicAlarm;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.plugin.ComponentPlugin;
import org.cougaar.core.service.*;
import org.cougaar.core.util.UID;
import org.cougaar.glm.ldm.Constants;
import org.cougaar.planning.ldm.PlanningFactory;
import org.cougaar.planning.plugin.util.PluginHelper;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.plan.*;
import org.cougaar.util.ConfigFinder;
import org.cougaar.util.UnaryPredicate;
import org.cougaar.glm.plugins.TaskUtils;
import org.cougaar.logistics.plugin.inventory.MaintainedItem;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;

	public class DateComparator implements Comparator, java.io.Serializable {
		
//		String name = null;
		public DateComparator() {
//			name = new String("DateComparator");
		}

		public int compare(Object o1, Object o2) {
		    if (o1 == o2) return 0;
	    
		    Long x = (Long) o1;
		    Long y = (Long) o2;
		    long x_date = x.longValue();
		    long y_date = y.longValue();
				
	    
		    // Smaller dates are less preferable
		    if (x_date < y_date)
				return 1;
		    else if (x_date > y_date) 
				return -1;
		    else 
				return 0;
		}

    }