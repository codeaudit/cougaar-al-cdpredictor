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
public class OpmodeNotificationMessage extends MessageEvent {

	public OpmodeNotificationMessage( MessageAddress entityName,  String value ) {
		setValue( value );
		this.entityName = entityName;
	}

	public String getVal() {
		return (String) getValue() ;
	}

	public MessageAddress getEntityName() {
		return entityName;
	}
	
	public Object getOpmode(String key) {
		if (opmodes.get(key)!=null)
			return opmodes.get(key);
		else return null;
	}
	
	public void putControlParameter(Object key, Object value){
		opmodes.put(key,value);
	}
	
	public void empty(){
		opmodes.clear();	
	}	
	
	public void setTimeForModes(double[][] measurement){
		this.timeForTasks=measurement;
	}
	
	public double[][] getTimeForModes(){
			return this.timeForTasks;
	}
	
	public HashMap getAllOpmodes(){
		return opmodes;
	}

	protected MessageAddress entityName ;
	protected HashMap opmodes= new HashMap();
	protected double[][] timeForTasks = null;
		
}