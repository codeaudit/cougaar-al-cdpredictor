package org.hydra.metrics;
import java.util.* ;
import org.hydra.pdu.* ;
import org.hydra.server.* ;
import org.hydra.util.* ;

/** Log some compressed information about this asset.
 */
public class AssetLog extends UniqueObjectLog {
    public AssetLog( UIDPDU assetUID ) {
        super( assetUID ) ;
    }
    
    public static class AssetVerbPair {
        public AssetVerbPair( UIDPDU assetUID, String verb ) {
            if ( assetUID == null || verb == null ) {
                throw new IllegalArgumentException("AssetUID " + null + " or verb " + null + " is null valued." );
            }
            this.assetUID = assetUID; this.verb = verb ;
            hash = assetUID.hashCode() + verb.hashCode() ;
        }
        
        public UIDPDU getAsset() { return assetUID ; }
        
        public String getVerb() { return verb ; }
        
        public int hashCode() { return hash ; }
        
        public boolean equals( Object o ) {
            AssetVerbPair p = ( AssetVerbPair ) o ;
            return p.assetUID.equals( assetUID ) && p.verb.equals( verb ) ;
        }
        
        private int hash ;
        private UIDPDU assetUID ;
        private String verb ;
    }
    
    public AssetLog( UIDPDU assetUID, String cluster, String itemId, String assetTypeId, String assetTypeNomenclature, long createdTime, long createdExecutionTime ) {
        super( assetUID, cluster, createdTime, createdExecutionTime ) ;
        //super( assetUID ) ;
        this.itemId = itemId ;
        if ( assetTypeId != null ) {
            this.assetTypeId = assetTypeId.intern() ;
        }
        if ( assetTypeNomenclature != null ) {
            this.assetTypeNomenclature = assetTypeNomenclature.intern() ;
        }
    }
    
    public void outputParamString( StringBuffer buf ) {
        super.outputParamString( buf ) ;
        buf.append( ",itemId=" ).append( itemId ).append( ",assetType=" ).append( assetTypeId ) ;
        buf.append( ",numAllocations=" ).append( tasks.size() ).append( ",numParents=" ).append( getNumParents() ) ;
        buf.append( ",numChildren=" ).append( getNumChildren() ) ;
        buf.append( ']' ) ;
    }
    
    public String toString() {
        StringBuffer buf = new StringBuffer() ;
        outputParamString( buf ) ;
        return buf.toString() ;
    }
    
    /** Add asset which is explicitly a child of this one.
     */
    private void addChildAsset( AssetLog asset ) {
        int index = 0 ;
        if ( ( index = children.indexOf(asset) ) == -1 ) {
            children.addElement( asset ) ;
            childTotalAllocCount.addElement( new Integer(1) ) ;
        }
        else {
            Integer count = ( Integer ) childTotalAllocCount.get(index) ;
            childTotalAllocCount.setElementAt( new Integer( count.intValue() + 1 ), index);
        }
    }
    
    public void logParentAsset( AssetLog parentAsset ) {
        if ( parents.indexOf( parentAsset ) == -1 ) {
            parents.add( parentAsset ) ;
        }
    }

    public void logParentAsset( AssetLog parentAsset, Subgraph s ) {
        if ( parents.indexOf( parentAsset ) == -1 ) {
            parents.add( parentAsset ) ;
        }
        if ( !parentSubgraphs.isMemberOf( parentAsset.getUID(), s ) ) {
            parentSubgraphs.put( parentAsset.getUID(), s ) ;
        }
    }
    
    public Enumeration getAssetVerbPairs() {
        return verbs.keys();
    }
    
    public int getTaskCount() {
        return tasks.size() ;
    }
    
    /** Get tasks from an asset with a verb.
     */
    public int getTaskCount( UIDPDU asset, String verb ) {
        return ( ( Integer ) verbs.get(new AssetVerbPair(asset,verb) ) ).intValue() ;
    }
    
    /** Log a task as being parented from an (parentAsset,verb)
     *  @param al  Parent asset of tl's parent task
     *  @param tl  Task log to be logged on this asset.
     */
    public void logTask( AssetLog al, TaskLog tl ) {
        if ( ServerApp.instance().isVerbose() ) {
            System.out.println( "LOGGING TASK " + tl + " FOR PARENT ASSET " + al + " AND CHILD ASSET " + this ) ;
        }
                
        UIDPDU taskUID = ( UIDPDU ) tasks.get( tl.getUID() ) ;
        if ( taskUID != null ) return ;
        
        tasks.put( tl.getUID(), tl.getUID() ) ;
        // Aggregate on verbs
        AssetVerbPair pair = new AssetVerbPair( al.getUID(), tl.getTaskVerb() ) ;
        Integer i = ( Integer ) verbs.get( pair ) ;
        if ( i != null ) {
            verbs.put( pair, new Integer( i.intValue() + 1 ) ) ;
        }
        else
            verbs.put( pair, new Integer(1) ) ;
    }
    
    public void unlogTask( AssetLog al, TaskLog tl ) {
        String taskUID = ( String ) tasks.get( tl.getUID() ) ;
        if ( taskUID == null ) {
            return ;
        }
        tasks.remove( tl.getUID() ) ;
        
        AssetVerbPair pair = new AssetVerbPair( al.getUID(), tl.getTaskVerb() ) ;
        Integer i = ( Integer ) verbs.get( pair ) ;
        if ( i != null ) {
            if ( i.intValue() == 1 ) {
                verbs.remove( pair ) ;
            }
            else {
                verbs.put( pair , new Integer( i.intValue() - 1 ) ) ;
            }
        }
    }
    
    public Object[] getChildSubgraphs( AssetLog childAsset ) {
        return childSubgraphs.getObjects( childAsset.getUID() ) ;
    }
    
    public Object[] getParentSubgraphs( AssetLog parentAsset ) {
        return parentSubgraphs.getObjects( parentAsset.getUID() ) ;
    }
        
    public void logChildAsset( AssetLog childAsset, Subgraph s ) {
        // For now simply log it, and increment the count.
        if ( ServerApp.instance().isVerbose() ) {
            System.out.println( "\n\tADDING to asset [" + getUID() + "," + getAssetTypeId() + "] dependent asset [" +
            childAsset.getUID() + "," + childAsset.getAssetTypeId() + ']' ) ;
        }
        addChildAsset( childAsset ) ;
        if ( !childSubgraphs.isMemberOf( childAsset.getUID(), s ) ) {
            childSubgraphs.put( childAsset.getUID(), s ) ;
        }
    }    
    
    public void logChildAsset( AssetLog childAsset ) {
        // For now simply log it, and increment the count.
        if ( ServerApp.instance().isVerbose() ) {
            System.out.println( "\n\tADDING to asset [" + getUID() + "," + getAssetTypeId() + "] dependent asset [" +
            childAsset.getUID() + "," + childAsset.getAssetTypeId() + ']' ) ;
        }
        addChildAsset( childAsset ) ;
    }
    
    protected void removeChildAsset( AssetLog childAsset ) {
        if ( ServerApp.instance().isVerbose() ) {
            System.out.println( "\n\tREMOVING from asset [" + getUID() + "," + getAssetTypeId() + "] dependent asset [" +
                childAsset.getUID() + "," + childAsset.getAssetTypeId() + ']' ) ;
        }
        int index = children.indexOf(childAsset) ;
        if ( index != -1 ) {
            children.remove(childAsset) ;
            childTotalAllocCount.remove( index ) ;
        }
    }
    
    protected void removeParentAsset( AssetLog parentAsset ) {
        parents.remove(parentAsset) ;
    }
    
    public AssetLog getParent() {
        if ( parents.size() > 0 ) {
            return ( AssetLog ) parents.elementAt(0) ;
        }
        return null ;
    }
    
    public int getNumParents() { return parents.size() ; }
    
    public AssetLog getParent( int index ) {
        if ( index >= 0 && index < parents.size() ) {
            return ( AssetLog ) parents.elementAt(index) ;
        }
        return null ;
    }
    
    public int getIndexOfChild( AssetLog child ) {
        return children.indexOf( child ) ;
    }
    
    public int getNumChildren() { return children.size() ; }
    
    public AssetLog getChild(int index) {
        if ( index >= 0 && index < children.size() ) {
            return ( AssetLog ) children.elementAt(index) ;
        }
        return null ;
    }
    
    public int getNumChildAlloc( int index ) {
        return ( ( Integer ) childTotalAllocCount.get(index) ).intValue() ;
    }
    
    public String getAssetTypeId() { return assetTypeId ; }
    
    public void setAssetTypeId(String newAssetTypeId) {
        assetTypeId = newAssetTypeId;
    }
    
   /** Identifies the "real" object, e.g an identifier that remains unique over th
    *  lifetime of the physical asset/organization/etc.  For example, a car's serial
    *  number does not change, even if the Asset representing it changes. An organization's
    *  identity does not change even though the agent representing it does.
    */
    public String getRealIdentifier() { return null ; }
    
   /** FUTURE Chain other elements to this Log?  Generally applies to assets and other
    *  physical entities.
    *  What if an Asset object is "destroyed" , e.g. its node crashes, and reconstituted later?
    *  at another site with a different UID?
    *  There should be able to be a way to track the `real' idenity of the Asset , not
    *  just its Cougaar UID.
    */    
    
    // An IntVector of symbol ids.
    
    // # of Allocations made to this asset (can use this to "shadow" role schedule?
    
    // # of Allocations scheduled (e.g. estAR is assigned to allocation)
    // This is a sequence of deltas to allocation results  (e.g
    //  (timestamp, delta) values.
    
    // Max planning horizon values (see role schedule)
    
    // Utilization (as a percentage of planning horizon)
    
    // Allocations removed
    
    // e.g. if there is an explicit or implicit functional dependency, through workflow or
    // organizational assignment.
    //  E.g.  A ShipperAsset
    //  Vector fromAssets ;
    
    String assetTypeId, itemId, assetTypeNomenclature ;
    
    // Assets upon which this is dependent.
    Vector parents = new Vector(1) ;
    MultiHashSet parentSubgraphs = new MultiHashSet() ;
    
    /** Number of allocations to each child. */
    Vector childTotalAllocCount = new Vector() ;
    Hashtable tasks = new Hashtable() ;
    // int allocCount ;
    
    /** Child (dependent) assets. */
    Hashtable verbs = new Hashtable( 5 ) ;
    Vector children = new Vector() ;
    MultiHashSet childSubgraphs = new MultiHashSet() ;
}
