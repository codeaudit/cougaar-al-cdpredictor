package org.hydra.metrics ;
import java.util.* ;
import org.hydra.pspace.search.*  ;
import org.hydra.pdu.* ;
import org.hydra.server.* ;

public class AssetDependencyBuilder {
    
    public AssetDependencyBuilder( LogPlanBuilder builder ) {
        this.builder = builder ;
        this.pld = builder.getDatabase() ;
    }
    
    public void processLog( UniqueObjectLog log ) {
        // Find a subgraph which associates a set of one or more assets
        // and one or more subassets.
        // Add all of these to the processed set and mark them.
    }
        
    protected ArrayList findSubgraphs( TaskSubgraphStrategy ts ) {
        ArrayList subgraphs = new ArrayList() ;
        HashMap tasks = new HashMap() ;
        for ( Iterator iter = pld.getTasks().iterator() ; iter.hasNext() ; ) {
            TaskLog tl = ( TaskLog ) iter.next() ;
            tasks.put( tl.getUID(), tl ) ;
        }
               
        while ( tasks.size() > 0 ) {
            TaskLog root = null ;
            
            // Find a candidate root
            for ( Iterator iter = tasks.values().iterator(); iter.hasNext(); ) {
                 TaskLog tl = ( TaskLog ) iter.next() ;
                 if ( ts.isRoot( tl ) || ts.isTerminalDown( tl ) ) {  
                    root = tl ;
                    break ;
                 }
             }
            
            if ( root == null ) {
                //System.out.println( "Unexpected error : could not find a root task log." ) ;
                //for ( Iterator iter2 = tasks.values().iterator(); iter2.hasNext() ;) {
                //    TaskLog tl = (TaskLog) iter2.next() ;
                //    System.out.println( "TaskLog " + tl ) ;
                //}
                break ;
            }
            else {
                // System.out.println( "\n\n\nSTARTING SEARCH FROM " + root ) ;
                tasks.remove( root.getUID() ) ;
            }
            
             SimpleSearch ss = new SimpleSearch( ts ) ;
             ss.init( new PEGraphNode( ( TaskLog ) root ) ) ;
             ss.run() ;

             Subgraph s = new Subgraph() ;
             for (Enumeration e = ss.getClosedNodes();e.hasMoreElements();) {
                PEGraphNode gn = ( PEGraphNode ) e.nextElement() ;
                TaskLog ctl = ( TaskLog ) gn.getLog() ;
                tasks.remove( ctl.getUID() ) ;
                s.addElement( ctl ) ;
             }
             
             for ( Iterator iter = s.elements.values().iterator(); iter.hasNext() ; ) {
                TaskLog ctl = ( TaskLog ) iter.next() ;
                
                if ( ctl instanceof MPTaskLog ) {  // Do nothing
                    MPTaskLog mpt = ( MPTaskLog ) ctl ;
                    boolean found = false ;
                    for (int i=0;i<mpt.getNumParents();i++) {
                        if ( pld.getLog( mpt.getParent(i) ) != null ) {
                           found = true ;
                           break ;
                        }
                    }
                    if ( !found ) {
                        s.addRoot( ctl ) ;
                    }
                }
                // My parent is null or not in the subgraph. I must be a root.
                else {
                    TaskLog parent = ( TaskLog ) builder.getLog( ctl.getParent() ) ;
                    if ( ctl.getParent() == null  || parent == null || s.getElement( parent.getUID() ) == null ) {
                        s.addRoot( ctl ) ;
                    }
                    //TaskLog ptl = ( TaskLog ) builder.getLog( ctl.getParent() ) ;
                    //if ( ptl != null && s.getElement( ptl.getUID() ) != null ) {
                    //    s.addRoot( ptl ) ;
                    //}
                }
                
                PlanElementLog pel = pld.getPlanElementLogForTask( ctl.getUID() ) ;
                if ( pel == null ) {
                    // Hmm, do nothing since this task is not allocated to anything.
                }
                else {
                    if ( pel instanceof AllocationLog ) {
                        AllocationLog al = ( AllocationLog ) pel ;
                        if ( al.getAllocTaskUID() == null || s.getElement( al.getAllocTaskUID() ) == null ) {
                            s.addLeaf( ctl ) ;
                        }
                    }                    
                }
                
                // Am I terminal down, e.g. assigned to an allocation and/or have no plan element.
                //if ( ts.isTerminalDown( ctl ) ) {
                //    s.addLeaf( ctl ) ;
                //}
             }
             
             subgraphs.add( s ) ;
        }
        return subgraphs ;
    }
        
    // Build asset dependency graphs.
    public void buildGraph() {        
        TaskSubgraphStrategy ts = new TaskSubgraphStrategy( pld ) ;
        
        ArrayList subgraphs = findSubgraphs(ts ) ;
                
        for ( int i=0;i<subgraphs.size();i++) {
            Subgraph s = ( Subgraph ) subgraphs.get(i) ;
            // System.out.println( "\n" + s ) ;
            for ( int j=0;j < s.roots.size();j++) {
                TaskLog t1 = ( TaskLog ) s.roots.get(j) ;
                //System.out.println( "Processing root " + t1 ) ;
                PlanElementLog pel = pld.getPlanElementLogForTask( t1.getUID() ) ;
                AssetLog assetLog1 = null ;
                
                if ( t1.getParent() == null || pld.getLog( t1.getParent() ) == null ) {
                    assetLog1 = builder.getSourceAsset( t1 ) ;
                }
                else {
                    // System.out.println( "\tPlan element " + pel + " found for " + t1.getUID() ) ;
                    if ( !( pel instanceof AllocationLog ) ) {
                        continue ;
                    }
                    AllocationLog a1 = ( AllocationLog ) pel ;
                    assetLog1 = ( AssetLog ) pld.getLog( a1.getAssetUID() ) ;
                }
                
                //System.out.println( "\tAsset log " + assetLog1 + " + found for root " + t1.getUID() ) ;
                if ( assetLog1 == null ) { 
                    continue ; 
                }
             
                for (int k=0;k< s.leaves.size();k++) {
                    TaskLog t2 = ( TaskLog ) s.leaves.get(k) ;
                    AllocationLog a2 = ( AllocationLog ) pld.getPlanElementLogForTask( t2.getUID() ) ;
                    //System.out.println( "\tAllocation log " + a2 + " + found for root " + t1.getUID() ) ;
                    AssetLog assetLog2 = null ;
                    if ( a2 != null ) {
                        assetLog2 = ( AssetLog ) pld.getLog( a2.getAssetUID() ) ;
                    }
                    if ( a2 == null || ( assetLog2 == null && a2.getAssetUID() != null ) ) {
                        assetLog2 = builder.getSinkAsset( t2 ) ;
                    }
                    
                    if ( assetLog2 != null ) {
                        //System.out.println( "Processing " + assetLog1.getUID() + " to " + assetLog2.getUID() ) ;
                        assetLog1.logChildAsset( assetLog2 ) ;
                        assetLog2.logParentAsset( assetLog1 ) ;
                    }
                }
            }
        }    
        
    }
    
    boolean useRealAssets ;
    protected LogPlanBuilder builder ;
    protected PlanLogDatabase pld ;
}