package org.cougaar.cpe.agents.messages;

import org.cougaar.cpe.model.UnitEntity;
import org.cougaar.tools.techspecs.events.MessageEvent;
import java.util.HashMap;

/**
 * User: Nathan Gnanasambandam
 * Date: July 15, 2004
 * Time: 11:03:30 AM
 */
public class ControlMessage extends MessageEvent {

	public ControlMessage( String entityName,  String value ) {
		setValue( value );
		this.entityName = entityName;
	}

	public String getControl() {
		return (String) getValue() ;
	}

	public String getEntityName() {
		return entityName;
	}
	
	public Object getControlParameter(String key) {
		if (controlSet.get(key)!=null)
			return controlSet.get(key);
		else return null;
	}
	
	public void putControlParameter(Object key, Object value){
		controlSet.put(key,value);
	}
	
	public void empty(){
		controlSet.clear();	
	}	

	protected String entityName ;
	protected HashMap controlSet= new HashMap(); 	
		
}