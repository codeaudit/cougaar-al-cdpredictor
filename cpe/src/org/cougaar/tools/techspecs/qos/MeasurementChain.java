package org.cougaar.tools.techspecs.qos;

import java.util.ArrayList;
import java.io.Serializable;

/**
 * This is a list of timestamp measurements accumulated through a single
 * chain of events.
 */

public class MeasurementChain implements Serializable {

    public synchronized void addMeasurement( Measurement tm ) {
        measurements.add( tm ) ;
    }

    public Object clone() {
        MeasurementChain result = new MeasurementChain() ;
        result.measurements.add( measurements ) ;
        return result ;
    }

    public synchronized String toString() {
        StringBuffer result = new StringBuffer() ;
        result.append( "[ MeasurementChain  ") ;
        for (int i = 0; i < measurements.size(); i++) {
            Measurement measurement = (Measurement)measurements.get(i);
            result.append( measurement ) ;
            if ( i < measurements.size() - 1 ) {
                result.append( ",\n")  ;
            }
        }
        result.append( "]" ) ;
        return result.toString() ;
    }

    public int getNumMeasurements() {
        return measurements.size() ;
    }

    public void removeFirstMeasurement() {
        measurements.remove(0) ;
    }

    public Measurement getMeasurement(int i) {
        return ( Measurement ) measurements.get(i) ;
    }

    private ArrayList measurements = new ArrayList() ;
}
