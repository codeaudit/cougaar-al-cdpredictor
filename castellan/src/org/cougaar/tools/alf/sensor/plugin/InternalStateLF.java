package org.cougaar.tools.alf.sensor.plugin;

import org.w3c.dom.Element;
import org.w3c.dom.Document;

import org.cougaar.core.adaptivity.SensorCondition;
import org.cougaar.core.adaptivity.OMCRangeList;
import org.cougaar.core.util.UID;
import org.cougaar.core.util.UniqueObject;
import org.cougaar.core.util.XMLize;
import org.cougaar.core.util.XMLizable;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Collection;

public class InternalStateLF implements java.io.Serializable, UniqueObject, XMLizable
{  
	private UID myUID = null;

    public long PlanStartTime;
	public int hari, Thadakamala;
	public long [] Hong = {0,0,0,0};
	public long globalsea_time, globalair_time, onead_time;

	public InternalStateLF(UID uid) {

		PlanStartTime = 0;
		hari = 0;
		Thadakamala = 0;
		globalsea_time = 0;
		globalair_time = 0; 
		onead_time = 0;

		setUID(uid);
	}

	public void show() {

		System.out.println("PlanStartTime = " + PlanStartTime + ", globalsea_time = " + globalsea_time + ", globalair_time = " + globalair_time + ", onead_time = " +onead_time);

	}

	public UID getUID() {
		return myUID;
	}

	public void setUID(UID uid) {
	  if (myUID != null) {
		RuntimeException rt = new RuntimeException("Attempt to call setUID() more than once.");
	    throw rt;
	  }
	  myUID = uid;
	}

	public Element getXML(Document doc) {
		 return XMLize.getPlanObjectXML(this, doc);
	}
}