package org.cougaar.tools.techspecs.events;

import org.cougaar.core.mts.MessageAddress;
import org.cougaar.tools.techspecs.qos.MeasurementChain;
import org.cougaar.tools.techspecs.ActionEventSpec;

import java.io.Serializable;

/**
 *  An input message to an action.  It represents a message sent by
 * an agent.
 */
public abstract class MessageEvent extends ActionEvent {

    public MessageEvent() {
        super( ActionEventSpec.EVENT_INPUT_MESSAGE ) ;
    }

    public void paramString( StringBuffer buf ) {
        String name = getClass().getName() ;
        int indx = name.lastIndexOf('.') ;
        buf.append( name.substring( indx + 1 ) ) ;
        buf.append( ",source=").append( getSource() ) ;
    }

    public MessageAddress getSource() {
        return source;
    }

    public void setSource(MessageAddress source) {
        this.source = source;
    }

    protected void setValue(Serializable value) {
        this.value = value;
    }

    public Serializable getValue() {
        return value;
    }

    public int getNumBytes() {
        return bytes;
    }

    public void setNumBytes(int bytes) {
        this.bytes = bytes;
    }

    public MeasurementChain getMeasurements() {
        return measurements;
    }

    public void setMeasurements(MeasurementChain measurements) {
        // Don't bother cloning measurements since they are all final, anyways.
        this.measurements = (MeasurementChain) measurements.clone();
    }

    protected MessageAddress source ;
    protected Serializable value ;
    protected int bytes ;
}
