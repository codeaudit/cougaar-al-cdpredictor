package org.cougaar.tools.techspecs.qos;

import org.cougaar.core.mts.MessageAddress;

import java.util.HashMap;

/**
 * User: wpeng
 * Date: May 28, 2003
 * Time: 2:31:20 PM
 */
public class VectorMeasurementPoint extends MeasurementPoint {

    private MeasurementPoint[] measurementPoints;
    private MessageAddress source;

    public VectorMeasurementPoint( MessageAddress source, String name, MeasurementPoint[] mps ) {
        super(name);
        this.source = source ;
        this.measurementPoints = (MeasurementPoint[]) mps.clone() ;

        for (int i = 0; i < mps.length; i++) {
            MeasurementPoint mp = mps[i];
            if ( mp == null ) {
                throw new IllegalArgumentException( "Measurement point " + i + " is null." ) ;
            }
            nameToMeasurementPoints.put( mp.getName(), mp ) ;
        }
    }

    public void toString(StringBuffer buf) {
        super.toString(buf);
        buf.append( ", measurementPoints=" ) ;
        buf.append( measurementPoints ) ;
    }

    public MeasurementPoint[] getMeasurementPoints() {
        return measurementPoints;
    }

    public void addMeasurement( String eventName, String action, String[] names, Measurement[] ms ) {

        // Confirm the validity of all messages.
        for (int i = 0; i < names.length; i++) {
            String name = names[i];

            if ( name == null ) {
                throw new IllegalArgumentException( "Name cannot be null." ) ;
            }
            if ( ms[i] == null ) {
                continue ;  // This is okay.
            }

            MeasurementPoint mp = (MeasurementPoint) nameToMeasurementPoints.get(name) ;
            if ( mp == null ) {
                throw new IllegalArgumentException( "No measurement point \"" + name + "\" exists.") ;
            }
            if ( !mp.isValidMeasurement(ms[i]) ) {
                throw new IllegalArgumentException(
                        "Invalid measurement " + ms +
                        " for mp " + mp + ", measurement=" + ms[i] ) ;
            }
        }
        // Now, add the measurement.
        VectorMeasurement vm = new VectorMeasurement( eventName, action, source ) ;
        vm.setMeasurements( names, ms );
        super.addMeasurement( vm );
    }

    HashMap nameToMeasurementPoints = new HashMap() ;
}
