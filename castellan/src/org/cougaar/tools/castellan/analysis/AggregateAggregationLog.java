/*
 * AggregateAggregationLog.java
 *
 * Created on July 31, 2001, 10:36 AM
 */

package org.cougaar.tools.castellan.analysis;
import org.cougaar.tools.castellan.server.* ;

/**
 *  This probably will be deprecated in favor of 
 * @author  wpeng
 * @version
 */
public class AggregateAggregationLog extends AggregateLog {
    
    /** Creates new AggregateAggregationLog */
    public AggregateAggregationLog(String[] verb) {
    }
    
    public void outputParamString( StringBuffer buf ) {
        super.outputParamString( buf ) ;
        // buf.append( ",verb=" ).append( getVerb() ) ;
    }
    
    public boolean logChildAggregateLog(AggregateLog alog, UniqueObjectLog l) {
        if ( ServerApp.instance().isVerbose() ) {
            System.out.println( "Logging " + l + " as child agg. log of " + alog ) ;
        }
        
        if ( ( alog instanceof AggregateVerbTaskLog && l instanceof TaskLog ) ||
        ( alog instanceof AggregateExpansionLog && l instanceof ExpansionLog )) {
            super.logChildAggregateLog( alog, l ) ;
            return true ;
        }
        else {
            throw new IllegalArgumentException( "Child " + alog +
            " must be AggregateVerbTaskLog or AggregateExpansionLog, "+
            "logged object must of type TaskLog or ExpansionLog" ) ;
        }
    }
    
    
    public void setCluster( String cluster ) {
        this.cluster = cluster ;
    }
    
    String cluster ;
    String[] verb ;
}
