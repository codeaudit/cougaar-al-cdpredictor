package org.cougaar.tools.alf.sensor.plugin;

import org.w3c.dom.Element;
import org.w3c.dom.Document;

import org.cougaar.core.adaptivity.SensorCondition;
import org.cougaar.core.adaptivity.OMCRangeList;
import org.cougaar.core.util.UID;
import org.cougaar.core.util.UniqueObject;
import org.cougaar.planning.servlet.XMLize; //Added by Himanshu
import java.io.Serializable;
import java.util.Hashtable;
import java.util.Collection;

public class InternalState implements java.io.Serializable, UniqueObject //, XMLizable
{  
	private UID myUID = null;

	public	long StartTime;
	public  long CurrentTime;
	public  long Correction;
	public	int NoTasks;								
	public	int currentstate;
	public	boolean over;
	public	long nextcheckpoint;
	public  Collection alCommunities = null;
	public  boolean rehydrate;

	public InternalState(long ut, long timelimit1, UID uid) {

		nextcheckpoint = 0;
		StartTime = -1L;
		CurrentTime = 0;
		currentstate = 0;
		over = false;
		NoTasks = 0;				
		Correction = 0;
		rehydrate = false;

		setUID(uid);
	}

	public void setNextCheckPoint(long v)	{	 nextcheckpoint=v; 	}
	public void setStartTime(long v)		{	 StartTime=v;		}
	public void setCurrentTime(long v)		{	 CurrentTime=v; 	}
	
	public void setCurrentState(int v)		{	 currentstate=v; 	}
	public void setOver(boolean v)			{	 over=v; 			}
	public void setalCommunities(Collection alCommunities1) { 	alCommunities = alCommunities1; 	}
	public void setNoTasks(int v)			{	 NoTasks=v; 	}

	public long getNextCheckPoint()			{	return nextcheckpoint; 	}
	public long getStartTime()				{	return StartTime; 		}
	public long getCurrentTime()			{	return CurrentTime; 	}

	public int		getCurrentState()		{	return currentstate; 	}
	public boolean	getOver()				{	return over; 			}
	public Collection getalCommunities()	{ 	return alCommunities; 	}
	public int	getNoTasks()				{ 	return NoTasks; 	}

	public InternalState(String name, OMCRangeList allowedValues, Comparable value, UID uid) {
		setUID(uid);
	}   

	public void show() {
		System.out.println("nextcheckpoint = " + nextcheckpoint + ", CurrentTime = " + CurrentTime + ", StartTime = " + StartTime + ", currentstate = " +currentstate);
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