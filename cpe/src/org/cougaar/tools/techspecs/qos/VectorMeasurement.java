package org.cougaar.tools.techspecs.qos;

import org.cougaar.core.mts.MessageAddress;

/**
 * User: wpeng
 * Date: May 18, 2003
 * Time: 6:51:53 PM
 */
public class VectorMeasurement extends MeasurementImpl {

    public VectorMeasurement(String eventName,
                             String actionName, MessageAddress source)
    {
        super(eventName, actionName, source);
    }

    public Measurement[] getMeasurements() {
        return measurements;
    }

    public void toString(StringBuffer buf) {
        super.toString( buf ); ;
        buf.append( ",measurements=[") ;
        for (int i = 0; i < measurements.length; i++) {
            Measurement measurement = measurements[i];
            buf.append( names[i] ).append( "=") ;
            buf.append( measurements[i] ) ;
            if ( i < measurements.length - 1 ) {
                buf.append( "," ) ;
            }
        }
    }

    public void setMeasurements( String[] names, Measurement[] measurements) {
        this.names = (String[]) names.clone() ;
        this.measurements = (Measurement[]) measurements.clone();
    }

    public Measurement getMeasurement( String name ) {
        for (int i = 0; i < names.length; i++) {
            String n = names[i];
            if ( n.equals( name ) ) {
                return measurements[i] ;
            }
        }
        return null ;
    }

    private String[] names ;
    private Measurement[] measurements ;
}
