/*
 * LogPlanSubgraphStrategy.java
 *
 * Created on October 5, 2001, 1:35 PM
 */

package org.hydra.metrics;
import org.hydra.pspace.search.* ;
import java.util.* ;
import org.hydra.pdu.* ;

/**
 *  Used to find subgraphs of the plan which are tasks which have Allocations
 *  to assets.
 *
 * @author  wpeng
 * @version
 */
public class TaskSubgraphStrategy implements org.hydra.pspace.search.Strategy {
    
    /** Creates new LogPlanSubgraphStrategy */
    public TaskSubgraphStrategy(PlanLogDatabase pld) {
        this.pld = pld ;
    }
    
    protected boolean isRoot( TaskLog tl ) {
        if ( !(tl instanceof MPTaskLog ) && tl.getParent() == null ) {
            return true ;
        }
        
        if ( tl instanceof MPTaskLog ) {
            MPTaskLog mpt = ( MPTaskLog ) tl ;
            if ( mpt.getNumParents() == 0 ) {
                return true ;
            }
            return false ;
        }
        else if ( pld.getLog( tl.getParent() ) == null ) {
            return true ;
        }
        return false ;
    }
    
    protected boolean isLeaf( TaskLog tl ) {
        PlanElementLog pel = pld.getPlanElementLogForTask( tl.getParent() ) ;
        // Right now, we don't care what kind of asset it is, whether it is organizational or otherwise.
        if ( pel == null ) {
            return true  ;
        }
        
        if ( pel instanceof AllocationLog ) {
            AllocationLog al = ( AllocationLog ) pel ;
            if ( al.getAllocTaskUID() == null ) {
                return true ;
            }
            TaskLog ctl = ( TaskLog ) pld.getLog( al.getAllocTaskUID() ) ;
            if ( ctl == null ) {
                return true ;
            }
        }
        return false ;
    }
    
    protected boolean isTerminalUpConcrete( TaskLog tl ) {
        if ( tl.getParent() == null ) {
            return true ;
        }
                
        TaskLog parentLog = ( TaskLog ) pld.getLog( tl.getParent() ) ;
        if ( parentLog == null ) {
            return true ;
        }
        
        PlanElementLog pel = pld.getPlanElementLogForTask( tl.getParent() ) ;
        // Right now, we don't care what kind of asset it is, whether it is organizational or otherwise.
        if ( pel instanceof AllocationLog ) {
            AllocationLog al = ( AllocationLog ) pel ;
            AssetLog asl = ( AssetLog ) pld.getLog( al.getAssetUID() ) ;
            if  (asl.getAssetTypeId().equals( "UTC/RTOrg" ) ) {
                return false ;
            }
            return true  ;
        }
        
        return false ;
        
    }
    
    protected boolean isTerminalUp( TaskLog tl ) {
        if ( tl.getParent() == null ) {
            return true ;
        }
                
        TaskLog parentLog = ( TaskLog ) pld.getLog( tl.getParent() ) ;
        if ( parentLog == null ) {
            return true ;
        }
        
        PlanElementLog pel = pld.getPlanElementLogForTask( tl.getParent() ) ;
        // Right now, we don't care what kind of asset it is, whether it is organizational or otherwise.
        if ( pel instanceof AllocationLog ) {
            return true  ;
        }
        
        return false ;
    }
    
    protected boolean isTerminalDown( TaskLog tl ) {
        
        PlanElementLog pel = pld.getPlanElementLogForTask( tl.getUID() ) ;
        // Right now, we don't care what kind of asset it is, whether it is organizational or otherwise.
        if ( pel instanceof AllocationLog || pel == null ) {
            return true  ;
        }
        
        return false ;
    }
    
    protected boolean isTerminalDownConcreteAsset( TaskLog tl ) {
        PlanElementLog pel = pld.getPlanElementLogForTask( tl.getUID() ) ;
        // Right now, we don't care what kind of asset it is, whether it is organizational or otherwise.
        if ( pel == null ) {
            return true ;
        }
        if ( pel instanceof AllocationLog ) {
            AllocationLog al = ( AllocationLog ) pel ;
            AssetLog asl = ( AssetLog ) pld.getLog( al.getAssetUID() ) ;
            if  (asl.getAssetTypeId().equals( "UTC/RTOrg" ) ) {
                return false ;
            }
            return true  ;
        }
        
        return false ;
        
    }
    
    /**
     * Expand a node into multiple children.
     */
    public GraphNode[] expand(GraphNode n) {
        GraphNode[] result = null ;
        PEGraphNode penode = ( PEGraphNode ) n ;
        
        if ( penode.isTerminal() ) {
            return null ;
        }
        
        TaskLog l = ( TaskLog ) penode.getLog() ;
        
        ArrayList list = new ArrayList() ;
        ArrayList tlist = new ArrayList() ;
        
        // Expand parents
        if ( l instanceof MPTaskLog ) {
            MPTaskLog mpt = ( MPTaskLog ) l ;
            for (int i=0;i<mpt.getNumParents();i++) {
                TaskLog tl = ( TaskLog ) pld.getLog( mpt.getParent(i) ) ;
                if ( tl != null ) {
                    //if ( isTerminalUp( tl ) ) {
                    //    tlist.add( tl ) ;
                    //}
                    //else {
                        list.add( tl ) ;
                    //}
                }
            }
        }
        else {  // Look at the parent.
            if ( l.getParent() != null ) {
                TaskLog parentLog = ( TaskLog ) pld.getLog( l.getParent() ) ;
  
                if ( isTerminalUp( l ) && parentLog != null ) {
                    tlist.add( parentLog ) ;  // Add this as a terminal.                    
                }
                else if ( parentLog != null ) {
                    list.add( parentLog ) ;                    
                }
            }
        }
        
        // Expand children
        PlanElementLog pel = pld.getPlanElementLogForTask( l.getUID() ) ;
        
        // If I am allocated to a non-null asset log, it is considered terminal.
        if ( pel instanceof AllocationLog ) {
            AllocationLog al = ( AllocationLog ) pel ;
            if ( al.getAssetUID() != null ) {
                AssetLog assetLog = ( AssetLog ) pld.getLog( al.getAssetUID() ) ;
                if ( assetLog != null ) {
                    // do Nothing, since this task has an asset log.
                }
            }
        }
        else if ( pel instanceof ExpansionLog ) {  // Make a bunch of child nodes.
            ExpansionLog el = ( ExpansionLog ) pel ;
            UIDPDU[] children = el.getChildren() ;
            for (int i=0;i<children.length;i++) {
                TaskLog ctl = ( TaskLog ) pld.getLog( children[i] ) ;
                if ( ctl != null ) {
                    list.add( ctl ) ;
                }
            }
        }
        else if ( pel instanceof AggregationLog ) {
            AggregationLog agl = ( AggregationLog ) pel ;
            TaskLog ctl = ( TaskLog ) pld.getLog( agl.getCombinedTask() ) ;
            if ( ctl != null ) {
                list.add( ctl ) ;
            }
        }
        
        PEGraphNode[] gn = new PEGraphNode[ list.size() + tlist.size() ] ;
        for (int i=0;i<list.size();i++) {
            TaskLog tl = ( TaskLog ) list.get(i) ;
            gn[i] = new PEGraphNode( tl ) ;
        }
        for (int i=0;i<tlist.size();i++) {
            TaskLog tl = ( TaskLog ) tlist.get(i) ;
            gn[i+list.size()] = new PEGraphNode( tl ) ;
            gn[i+list.size()].setTerminal( true ) ;
        }
        n.setSuccessors( gn ) ;
        return gn ;
    }
    
    /**
     * Determines whether or not a node is a goal node.
     */
    public boolean isGoalNode(GraphNode n) {
        return false ;
    }
    
    /**
     * Initialize a node generated as a initial state.  Generally, this
     * introduces information on the node usually generated during
     * exapnsion.
     */
    public void initNode(GraphNode n) {
        
    }
    
    /**
     * Compares two nodes for equality.  Should test to see whether two
     * nodes are equal.
     */
    public boolean isEqual(GraphNode n1, GraphNode n2) {
        return n1.equals( n2 ) ;        
    }
    
    public int compare(java.lang.Object obj, java.lang.Object obj1) {
        return 0 ;
    }
    
    /**
     * Make an empty graph node.  This will be used as the "factory" node
     * for this <code>Strategy</code>.
     */
    public GraphNode makeNode() {
        return new PEGraphNode( null ) ;
    }
    
    /**
     * The name of this method is misleading since it is
     * called after node <code>n1</code>'s parent has been already been
     * changed.
     *
     * <p>
     * It is important to note that since n1 and n2 are considered
     * equivalent, only <code>n1</code> will be retained by the GraphSearch
     * algorithm after <code>updateParent</code> is called.
     *
     * <p> This method should update n1 according to n2's current cost
     * and heuristic value.
     */
    public void updateParent(GraphNode n1, GraphNode n2) {
    }
    
    /**
     * Optional method for expanding the ith node associated with n.
     *
     * @param n A GraphNode to be expanded.
     * @param i The index of the successor node to expand.
     */
    public GraphNode expand(GraphNode n, int i) {
        throw new UnsupportedOperationException() ;
    }
    
    /**
     * Optional method returning the number of descendents for a graph node.
     * This is only used by search algorithms that do not immediately expand
     * all graph nodes.
     */
    public int getNumDescendants(GraphNode n) {
        throw new UnsupportedOperationException() ;
    }
        
    PlanLogDatabase pld ;
}
