package org.cougaar.tools.techspecs.qos;

import org.cougaar.tools.techspecs.qos.QualitativeValue;
import org.cougaar.tools.techspecs.qos.QValueTable;

/**
 * User: wpeng
 * Date: May 29, 2003
 * Time: 1:34:41 PM
 */
public class Value implements java.io.Serializable, Comparable {

    public int compareTo(Object o) {
        if ( !(o instanceof QualitativeValue) ) {
            throw new RuntimeException() ;
        }

        // QValueTable.
        return QValueTable.compare( this, (Value) o ) ;
    }
}
