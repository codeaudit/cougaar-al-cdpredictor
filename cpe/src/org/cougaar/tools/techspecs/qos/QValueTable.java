package org.cougaar.tools.techspecs.qos;

import org.cougaar.core.adaptivity.OMCRange;

import java.util.HashMap;

import org.cougaar.tools.techspecs.qos.QualitativeValue;

/**
 * User: wpeng
 * Date: May 28, 2003
 * Time: 11:36:43 PM
 */
public class QValueTable {

    public static void addQValues( QualitativeValue[] values ) {
    }

    /**
     * Map qualitative values (symbols) to integers.  The values must be sorted from
     * lowest to highest.
     * order.
     * @param values
     * @param quanValues
     */
    public static void addQValues( QualitativeValue[] values, int[] quanValues ) {
        if ( values.length != quanValues.length ) {
            throw new IllegalArgumentException( "Number of values does not match number of quantative values." ) ;
        }
    }

    /**
     * Map qualitative values (symbols) to integers.  The values must be sorted from
     * lowest to highest.
     * order.
     * @param values
     * @param quanValues
     */
    public static void addQValues( QualitativeValue[] values, double[] quanValues ) {
    }

    public static Value getQuantValueForQualValue( QualitativeValue qv ) {
        return null ;
    }

    public static int compare( Value value1, Value value2 ) {
        return 0 ;
    }

    /**
     * Map from strings to Qualitative values.
     */
    HashMap nameToQualValueTable = new HashMap() ;

    HashMap qualValueToQuanValueTable = new HashMap() ;
}
