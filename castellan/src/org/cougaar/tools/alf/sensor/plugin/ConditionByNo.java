package org.cougaar.tools.alf.sensor.plugin;

import org.cougaar.core.plugin.ComponentPlugin;
import org.cougaar.core.service.*;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.util.UID;
//import org.cougaar.core.agent.ClusterIdentifier;
import org.cougaar.core.adaptivity.OMCRangeList;
import org.cougaar.core.service.community.CommunityService;
import org.cougaar.core.util.UniqueObject;
//import org.cougaar.core.util.XMLize; //Changed by Himanshu
//import org.cougaar.core.util.XMLizable; //Changed by Himanshu
import org.cougaar.planning.servlet.XMLize; //Added by Himanshu


import org.w3c.dom.Element;
import org.w3c.dom.Document;

import org.cougaar.multicast.AttributeBasedAddress;

import org.cougaar.planning.ldm.plan.*;
import org.cougaar.util.*;
import org.cougaar.logistics.plugin.manager.LoadIndicator;

import java.util.Iterator;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Arrays;

import org.cougaar.tools.alf.sensor.plugin.PSUSensorCondition;
import org.cougaar.tools.alf.sensor.*;


import java.io.*;
import java.util.Vector;
import java.util.Enumeration;

	
	
	public class ConditionByNo implements java.io.Serializable
	{
		public ConditionByNo(int FbLevel, float LLThreshold, float ULThreshold, int type1) {
			level = FbLevel;
			LowerLimit = LLThreshold;
			Upperlimit = ULThreshold;  // time;
			type = type1;
		}

		public boolean check(int num_task, long t) {

			if (LowerLimit < num_task && num_task <= Upperlimit) {

				return true;
			}

			return false;
		}

		public int getLevel() {
			return level;
		}

		int level;
		float LowerLimit;
		float Upperlimit;  // time;
		public int type;

	}
