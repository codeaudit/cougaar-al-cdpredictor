package org.cougaar.tools.alf.sensor.plugin;

import org.w3c.dom.Element;
import org.w3c.dom.Document;

import org.cougaar.core.adaptivity.SensorCondition;
import org.cougaar.core.adaptivity.OMCRangeList;
import org.cougaar.core.util.UID;
import org.cougaar.core.util.UniqueObject;
import org.cougaar.core.util.XMLize;
import org.cougaar.core.util.XMLizable;

public class PSUSensorCondition extends SensorCondition implements UniqueObject, XMLizable
{  
	private UID myUID = null;

	public PSUSensorCondition(String name, OMCRangeList allowedValues, UID uid) {
		super(name, allowedValues, allowedValues.getEffectiveValue());
		setUID(uid);
	}

	public PSUSensorCondition(String name, OMCRangeList allowedValues, Comparable value, UID uid) {
		super(name, allowedValues, value);
		setUID(uid);
	}   

	public void setValue(Comparable newValue) {
		super.setValue(newValue);
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