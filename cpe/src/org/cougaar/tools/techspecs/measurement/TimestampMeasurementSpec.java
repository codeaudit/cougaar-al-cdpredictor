package org.cougaar.tools.techspecs.measurement;

import org.cougaar.tools.techspecs.ActionSpec;

/**
 * Created by IntelliJ IDEA.
 * User: wpeng
 * Date: Apr 5, 2004
 * Time: 1:25:54 AM
 * To change this template use Options | File Templates.
 */
public class TimestampMeasurementSpec extends MeasurementSpec {

    public TimestampMeasurementSpec(String name, ActionSpec parent, int type, int layer) {
        super(name, parent, type, layer);
        if ( type != MeasurementSpec.TYPE_ACTION_START_TIMESTAMP |
                type != MeasurementSpec.TYPE_ACTION_END_TIMESTAMP |
                type != MeasurementSpec.TYPE_ACTION_DELAY ) {
            throw new IllegalArgumentException( "Type must be one of START, END, or DELAY" ) ;
        }
    }
}
