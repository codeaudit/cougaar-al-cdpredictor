package org.cougaar.cpe.agents.messages;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Placeholder.  QoS objects are published onto the BB and maintained by interested
 * parties.
 */

public class QoSMeasurement implements Serializable {

    // String qosName ;  // A (unique) name.

    // QoSType type ;  // Measurement in units, comparison between QoS valuations.

    /**
     * A history of QoSMeasurements, indexed by time.
     */
    ArrayList history = new ArrayList() ;
}
