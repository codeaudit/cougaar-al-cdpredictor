package org.cougaar.tools.techspecs.qos;

import org.cougaar.core.adaptivity.OMCRangeList;
import org.cougaar.core.adaptivity.OMCPoint;
import org.cougaar.core.adaptivity.OMCRange;

/**
 * User: wpeng
 * Date: May 28, 2003
 * Time: 12:27:40 PM
 */
public class OMCMeasurementPoint extends MeasurementPoint {
    private OMCRangeList list;

    public OMCMeasurementPoint(String id, OMCRangeList rl ) {
        super(id);
        this.list = rl ;
    }

    public void addMeasurement(Measurement m) {
        if ( !( m instanceof OMCMeasurement ) ) {
            throw new IllegalArgumentException( m + " is not a OMCMeasurement." ) ;
        }

        OMCMeasurement om = (OMCMeasurement) m ;
        // Check to see whether this OMCMeasurement is allowable.
        if ( list.isAllowed( om.getValue() ) ) {
            super.addMeasurement( om );
        }
        else {
            throw new IllegalArgumentException( "Measurement " + m + " cannot be added to measurementPoint " + getName() ) ;
        }

//        if ( om.getRange() instanceof OMCPoint ) {
//            OMCPoint p = (OMCPoint) om.getRange() ;
//            // See if the point is allowable.
//            OMCRange[] ranges = list.getAllowedValues() ;
//            for (int i = 0; i < ranges.length; i++) {
//                OMCRange range = ranges[i];
//                if ( range.contains( p.getMax() ) ) {
//                    super.addMeasurement( om );
//                }
//            }
//            throw new IllegalArgumentException( "Measurement " + m + " cannot be added." ) ;
//        }
//        else {
//            throw new IllegalArgumentException( "Range measurements are not supported." ) ;
//        }
    }
}
