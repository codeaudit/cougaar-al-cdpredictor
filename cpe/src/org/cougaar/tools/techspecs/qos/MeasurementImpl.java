package org.cougaar.tools.techspecs.qos;

import org.cougaar.core.util.UID;
import org.cougaar.core.mts.MessageAddress;

/**
 * User: wpeng
 * Date: May 18, 2003
 * Time: 5:52:14 PM
 */
public class MeasurementImpl implements Measurement {

    public MeasurementImpl(String eventName, String actionName, MessageAddress source) {
        this.event = eventName;
        this.action = actionName;
        this.source = source;
    }

    public String getAction() {
        return action;
    }

    public String getUnitType() {
        return unitType ;
    }

    /**
     * The measurement point associated with this measurement.
     * @return
     */
    public UID getMeasurementPoint() {
        return null;
    }

    public String getEvent() {
        return event;
    }

    public MessageAddress getSource() {
        return source ;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer() ;
        buf.append( "[") ;
        toString( buf );
        buf.append( "]") ;
        return buf.toString() ;
    }

    public void toString( StringBuffer buf ) {
        Class c = getClass() ;
        int li = c.getName().lastIndexOf( '.') ;
        buf.append( c.getName().substring( li + 1 ) ) ;
        buf.append( ",action=").append( action ) ;
        buf.append( ",event=").append( event ) ;
        buf.append( ",source=").append( source ) ;
    }

    String action, event, unitType ;
    MessageAddress source ;
    UID measurementPoint ;
}
