package org.cougaar.cpe.agents.messages;

import org.cougaar.cpe.model.UnitEntity;
import org.cougaar.tools.techspecs.events.MessageEvent;
import java.util.HashMap;
import org.cougaar.core.mts.MessageAddress;

/**
 * User: Nathan Gnanasambandam
 * Date: July 15, 2004
 * Time: 11:03:30 AM
 */
public class ControlMessage extends MessageEvent {

	public ControlMessage(String entityName, String value) {
		setValue(value);
		this.entityName = entityName;
	}

	public String getControl() {
		return (String) getValue();
	}

	public String getEntityName() {
		return entityName;
	}

	public Object getControlParameter(MessageAddress agentName, String key) {
		if (controlSet.containsKey((Object) agentName)) {
			HashMap h = (HashMap) controlSet.get((Object) agentName);
			if (h.containsKey((Object) key)) {
				return (h.get((Object) key));
			}
		}
		return null;
	}

	public void putControlParameter(Object key, Object value) {
		controlSet.put(key, value);
	}

	public void empty() {
		controlSet.clear();
	}

	public void paramString(StringBuffer buf) {
		buf.append(controlSet.toString());
	}

	public void putControlSet(HashMap h) {
		this.controlSet = h;
	}

	protected String entityName;
	protected HashMap controlSet = new HashMap();

}