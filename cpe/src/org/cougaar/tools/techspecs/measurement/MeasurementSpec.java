package org.cougaar.tools.techspecs.measurement;

import org.cougaar.tools.techspecs.TechSpec;
import org.cougaar.tools.techspecs.ActionSpec;
import org.cougaar.core.adaptivity.OMCRange;

/**
 *  The specifications for a measurement associated with an action.  Each measurement is tagged with the
 * name "Agent.MeasurementName".
 */
public class MeasurementSpec extends TechSpec
{
    public MeasurementSpec(String name, ActionSpec parent, int type, int layer)
    {
        super(name, parent, TechSpec.TYPE_MEASUREMENT, layer);
        this.type = type ;
    }

    public MeasurementSpec(String name, ActionSpec parent, int type, int layer, String measurementPointName )
    {
        this( name, parent, type, layer ) ;
        this.measurementPointName = measurementPointName ;
        this.type = type ;
    }

    public ActionSpec getActionSpec() {
        return (ActionSpec) getParent() ;
    }


    public void setMeasurementPointName(String measurementPointName) {
        this.measurementPointName = measurementPointName;
    }

    public void setChainToOutput(boolean chainToOutput) {
        this.chainToOutput = chainToOutput;
    }

    /**
     * Automatically chain mesurements to output events.
     * @return
     */
    public boolean chainToOutput()
    {
        return chainToOutput;
    }

    public String getMeasurementPointName()
    {
        return measurementPointName;
    }

    /**
     * Make a timestamp measurement of the action being triggered before it starts.
     */
    public static final int TYPE_ACTION_START_TIMESTAMP = 0 ;

    /**
     * Make a timestamp measurement of the action triggered after it stops.
     */
    public static final int TYPE_ACTION_END_TIMESTAMP = 1 ;

    /**
     * Make a measurement of delay of an action, i.e the difference between the start time and stop time.
     */
    public static final int TYPE_ACTION_DELAY = 1 ;

    /**
     * Compute the delay from the measurement chain for the input.  We need to be able to extract a measurement from the chain
     * in order to compute this.
     */
    public static final int TIME_MEASUREMENT_CHAIN_DELAY = 2 ;

    /**
     * Extract an arbitrary value from the measurement chain.
     */
    public static final int TIME_MEASUREMENT_CHAIN_QUALITY = 3 ;

    /**
     * A synthetic measurement declared or measured by the component. (This
     * relies on the plugin itself to make a declaration of the quality of service.)  This is not automated, rather
     * it has to be
     */
    public static final int TYPE_SYNTHETIC = 10 ;

    /**
     * The action triggering this result.
     */
    protected ActionSpec spec ;

    /**
     * The name of the plugin specific measurement point, if one exists.  A MP is
     * simply a buffer of measurements taken over time.
     */
    protected String measurementPointName ;

    /**
     * Automatically add measurement to the MeasurementChain of the output message.
     */
    boolean chainToOutput = false  ;

    /**
     * One of TYPE_ACTION_START_TIMESTAMP, TYPE_ACTION_END_TIMESTAMP, TYPE_ACTION_DELAY, TIME_MEASUREMENT_CHAIN_DELAY, TYPE_MEASUREMENT_CHAIN_VALUE,
     * TYPE_SYNTHETIC
     */
    int type ;

    OMCRange range ;

    /**
     * A description of the measurement being made (either Timestamp, TimeDelay, NetworkBytes, MemoryAlloc, MemoryFree, Synthetic.)
     */
    String descriptor ;

    String measurementClass ;
}
