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
	
	public class History implements java.io.Serializable {

		HashMap listOfSubItem;
		String name = null;
		public History(String name) {	// this name is for identification
			super();
			this.name = name;
			listOfSubItem = new HashMap();
		}

		public boolean isThisName(String name) {
			return this.name.equalsIgnoreCase(name);
		}

		public TreeMap getHistory(String nomenclature) {

			TreeMap timeSeriesOfTheItem = (TreeMap) listOfSubItem.get(nomenclature);

			if (timeSeriesOfTheItem == null){
				timeSeriesOfTheItem = new TreeMap(new DateComparator());
				listOfSubItem.put(nomenclature,timeSeriesOfTheItem);
			}

			return timeSeriesOfTheItem;
		}
	}
