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

public class InternalState implements java.io.Serializable, UniqueObject, XMLizable
{  
	private UID myUID = null;

	public	long StartTime;
//	public  long CurrentTime;
	public  long unittime;
	public  long timelimit;
	public	int NoFinish;
	public	long CTFinish;  // Cumulative waiting time;
	public	int NoTasks;
	public	int currentstate;
	public	boolean over;
	public	long nextcheckpoint;
	public  Collection alCommunities = null;

	public InternalState(long ut, long timelimit1, UID uid) {

		nextcheckpoint = 0;
		StartTime = -1L;
//		CurrentTime = 0;
		unittime = ut;  // 1 sec
		NoFinish = 0;
		CTFinish = 0;
		NoTasks = 0;
		currentstate = 0;
		timelimit = timelimit1;
		over = false;
		setUID(uid);
	}

	public void setNextCheckPoint(long v) {	 nextcheckpoint=v; 	}
	public void setStartTime(long v)		{	 StartTime=v; 	}
//	public void setCurrentTime(long v)		{	 CurrentTime=v; 	}

	public long getNextCheckPoint() {	return nextcheckpoint; 	}
	public long getStartTime()		{	return StartTime; 	}
//	public long getCurrentTime()		{	return CurrentTime; 	}

	public void setalCommunities(Collection alCommunities1) { 	alCommunities = alCommunities1; 	}

	public InternalState(String name, OMCRangeList allowedValues, Comparable value, UID uid) {
		setUID(uid);
	}   

	public void show() {

		System.out.println("nextcheckpoint = " + nextcheckpoint + ", StartTime = " + StartTime + ", currentstate = " +currentstate);

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