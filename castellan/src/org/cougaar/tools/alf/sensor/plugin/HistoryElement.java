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
	
	public class HistoryElement implements java.io.Serializable {

		String nomenclature = null;
		int numOfUpdated = 0;
		int cumulationOfSuccess = 0;

		public HistoryElement(String nomenclature) {	// this name is for identification
			this.nomenclature = nomenclature;
			numOfUpdated = 0;
			cumulationOfSuccess = 0;
		}

		public boolean isThisName(String nomenclature) {
			return this.nomenclature.equalsIgnoreCase(nomenclature);
		}

		public void addResult(int successOrNot) {
			cumulationOfSuccess = cumulationOfSuccess + successOrNot;
			numOfUpdated++;
		}

		public double getConfidence() {	// because confidence estimation is for future end time.
			if (numOfUpdated==0)	{
				return 1;
			}
			return (double) cumulationOfSuccess/numOfUpdated;
		}
	}
