/*
 * AggregationLog
 *
 * Created on August 2, 2001, 11:18 AM
 */

package org.cougaar.tools.castellan.analysis;
import org.cougaar.tools.castellan.pdu.* ;

/**
 * Should really be extension of a PlanElementLog base class (which does not yet exist.)
 *
 * @author  wpeng
 * @version
 */
public class AggregationLog extends PlanElementLog {

    public AggregationLog( UIDPDU combinedTaskUID, UIDPDU task, UIDPDU uid, String cluster, long createdTime, long createdExecutionTime ) {
        super( uid, task, cluster, createdTime, createdExecutionTime ) ;
        this.combinedTaskUID = combinedTaskUID ;
    }

    public void outputParamString(StringBuffer buf) {
        super.outputParamString( buf ) ;
        buf.append( ",mpTask=" ).append( combinedTaskUID ) ;
    }
    
    public UIDPDU getCombinedTask() { return combinedTaskUID ; }
    
    protected UIDPDU combinedTaskUID ;
}
