package org.hydra.metrics;
import org.hydra.pdu.* ;

/** Allocations only stick around long enough to connect assets together.
 */
public class AllocationLog extends PlanElementLog {
    AllocationLog( UIDPDU uid ) {
        super( uid ) ;
    }
    
    AllocationLog( UIDPDU uid, UIDPDU taskUID, UIDPDU assetUID, UIDPDU allocTaskUID ) {
        super( uid ) ;
        this.taskUID = taskUID ;
        this.assetUID = assetUID ;
        this.allocTaskUID = allocTaskUID ;
    }
    
    public void outputParamString(StringBuffer buffer) {
        super.outputParamString( buffer ) ;
        buffer.append( ',' ).append( "task=" ).append( taskUID ) ;
        buffer.append( ',' ).append( "asset=" ).append( assetUID ) ;
        buffer.append( ',' ).append( "allocTask=" ).append(allocTaskUID ) ;
    }
    
    public UIDPDU getAllocTaskUID() { return allocTaskUID ; }
        
    public UIDPDU getAssetUID() { return assetUID ; }
    
    public void setAllocTaskUID( UIDPDU uid ) { this.allocTaskUID = uid ; }
    
    public void setAssetUID( UIDPDU newAssetUID) {
        assetUID = newAssetUID;
    }   
    
    UIDPDU assetUID, allocTaskUID ;
}
