package org.hydra.metrics;
import org.hydra.pdu.*;

public class TaskLog extends UniqueObjectLog {
    public static final int TYPE_UNKNOWN = 0 ;
    public static final int TYPE_FORWARDED = 1 ;
    public static final int TYPE_EXPANSION = 4 ;
    public static final int TYPE_AGGREGATION = 5 ;

    public TaskLog( UIDPDU uid ) {
        super( uid ) ;
    }
    
    public TaskLog( UIDPDU uid, String cluster, String taskVerb, long createdTime, long createdExecutionTime ) {
        super( uid, cluster, createdTime, createdExecutionTime ) ;
        this.taskVerb = taskVerb.intern() ;
    }
    
    void outputParamString(StringBuffer buf) {
        super.outputParamString( buf ) ;
        if ( !this.getClass().equals( MPTaskLog.class ) ) {
        buf.append( ",parent=" ).append( parentTask ) ;
        buf.append( ",parType=").append( EventPDU.typeToString( parentType ) ) ;
        buf.append( ",verb=").append( taskVerb ) ;
        }
    }
    
    //public void setPlanElement( String planElement ) {
    //    this.planElement = planElement ;
   // }
    
    /** Plan element. */
    //public String getPlanElement() {
    //    return planElement ;
    //}
    
    public UIDPDU getParent() {
        return parentTask ;
    }
    
    public void setParent(UIDPDU newParentTask) {
        parentTask = newParentTask;
    }
    
    /** PARENT, EXPANSION, or AGGREGATION. */
    public int getParentType() { return parentType ; }
    
    public void setParentType( int parentType ) {
        this.parentType = parentType ;
    }
    
    public void setTaskVerb(String newTaskVerb) {
        taskVerb = newTaskVerb;
    }
    
    public String getTaskVerb() {
        return taskVerb;
    }
        
    UIDPDU parentTask ;
    String taskVerb ;
    //String planElement ;
    int parentType = TYPE_UNKNOWN ;
}