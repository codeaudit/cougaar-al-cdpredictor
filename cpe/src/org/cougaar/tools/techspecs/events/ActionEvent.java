package org.cougaar.tools.techspecs.events;

import org.cougaar.tools.techspecs.qos.MeasurementChain;
import org.cougaar.tools.techspecs.ActionEventSpec;

import java.io.Serializable;

/**
 *  This encapsulates a message either an input message or a timer message.
 */
public class ActionEvent implements Serializable
{

    public static final int PRIORITY_NORMAL = 0 ;
    public static final int PRIORITY_HIGH = 1 ;

    public ActionEvent(int type)
    {
        this.type = type;
    }

    public int getType()
    {
        return type;
    }

    /**
     * This is filled in automatically by the callback action (based on generated code.)
     *
     * @param spec
     */
    public void setSpec(ActionEventSpec spec)
    {
        this.spec = spec;
    }

    public ActionEventSpec getSpec()
    {
        return spec;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer() ;
        buf.append( "[" ) ;
        paramString( buf );
        buf.append( "]") ;
        return buf.toString() ;
    }

    public void paramString( StringBuffer buf ) {
    }

    public void setPriority( int priority ) {
        this.priority = priority ;
    }

    public int getPriority()
    {
        return priority;
    }

    protected MeasurementChain measurements = new MeasurementChain();

    /**
     * The ActionEventSpec associated with this message.
     */
    transient ActionEventSpec spec ;
    int type ;
    int priority = PRIORITY_NORMAL ;

    protected long sentTimeStamp ;
    protected long receivedTimeStamp ;
    protected long startProcessedTimestamp ;
    protected long endProcessedTimestamp ;
}
