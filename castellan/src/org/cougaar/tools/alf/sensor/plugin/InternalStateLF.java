package org.cougaar.tools.alf.sensor.plugin;

import org.w3c.dom.Element;
import org.w3c.dom.Document;

import org.cougaar.core.adaptivity.SensorCondition;
import org.cougaar.core.adaptivity.OMCRangeList;
import org.cougaar.core.util.UID;
import org.cougaar.core.util.UniqueObject;
//import org.cougaar.core.util.XMLize; //Changed by Himanshu
//import org.cougaar.core.util.XMLizable;
import org.cougaar.planning.servlet.XMLize; //Added by Himanshu

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Collection;

public class InternalStateLF implements java.io.Serializable, UniqueObject //, XMLizable
{  
	private UID myUID = null;

    public long PlanStartTime;
	public int hari, Thadakamala;
	public double [] Hong = {0,0,0,0,0};
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

	public void setPlanStartTimet(long v)	{	 PlanStartTime=v; 	}
	public void setGlobalsea_time(long v)	{	 globalsea_time=v;	}
	public void setGlobalair_time(long v)	{	 globalair_time=v; 	}
	public void setOnead_time(long v)		{	 onead_time=v; 		}
	public void setHari(int v)				{	 hari=v; 	}
	public void setThadakamala(int v)		{	 Thadakamala=v; 	}
	public void setHong(double [] v)			{	 Hong=v; 			}

	public long getPlanStartTimet()	{	 return PlanStartTime; 		}
	public long getGlobalsea_time()	{	 return globalsea_time;		}
	public long getGlobalair_time()	{	 return globalair_time; 	}
	public long getOnead_time()		{	 return onead_time; 		}

	public int getHari()			{	 return hari;				}
	public int getThadakamala()		{	 return Thadakamala; 		}

	public double [] getHong()		{	 return Hong; 				}
	

	public void show() {

//		System.out.println("PlanStartTime = " + PlanStartTime + ", globalsea_time = " + globalsea_time + ", globalair_time = " + globalair_time + ", onead_time = " +onead_time);

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