/*
 * PlanStrategy.java
 *
 * Created on October 3, 2001, 2:34 PM
 */

package org.hydra.metrics;
import org.hydra.pspace.search.* ;
import org.hydra.pdu.* ;
import java.util.* ;

/**
 * A generic strategy for traversing the log plan using GraphSearch approaches.
 *
 * @author  wpeng
 * @version 
 */
public class PlanStrategy implements Strategy {

    /** Creates new PlanStrategy */
    public PlanStrategy( PlanLogDatabase pld ) {
         this.pld = pld ;
    }
    
    public void addTerminal( UniqueObjectLog log ) {
        terminalMap.put( log.getUID(), log ) ;
    }
    
    protected boolean isTerminal( UniqueObjectLog log ) {
        if ( terminalMap.size() > 0 && terminalMap.get( log.getUID() ) != null ) {
            return true ;
        }
        return false ;
    }

    /**
     * Expand a node into multiple children.
     */
    public GraphNode[] expand(GraphNode n) {
        GraphNode[] result = null ;
               
        PEGraphNode penode = ( PEGraphNode ) n ;
        UniqueObjectLog l = penode.getLog() ;
        if ( isTerminal( l ) ) {
            return result ;
        }

        if ( l instanceof TaskLog ) {
            TaskLog tl = ( TaskLog ) l ;
            PlanElementLog pel  = pld.getPlanElementLogForTask( tl.getUID() ) ;
            if ( pel != null ) {
                result = new GraphNode[] { new PEGraphNode( pel ) } ;
            }
        }
        else if ( l instanceof AllocationLog  ) {
            AllocationLog al = ( AllocationLog ) l ; 
            if ( al.getAllocTaskUID() != null  ) {
                 UniqueObjectLog cl = pld.getLog( al.getAllocTaskUID() ) ;
                 if ( cl != null ) {
                    result = new GraphNode[] { new PEGraphNode( cl ) } ;
                 }
            }
        }
        else if ( l instanceof AggregationLog ) {
            AggregationLog al = ( AggregationLog ) l ;
            if ( al.getCombinedTask() != null ) {
                 UniqueObjectLog cl = pld.getLog( al.getCombinedTask() ) ;
                 if ( cl != null ) {
                    result =  new GraphNode[] { new PEGraphNode( cl ) } ;
                 }
            }
        }
        else if ( l instanceof ExpansionLog ) {
            ExpansionLog el = ( ExpansionLog ) l ;
            UIDPDU[] children = el.getChildren() ;
            ArrayList results = new ArrayList() ;
            for (int i=0;i<children.length;i++) {
                UniqueObjectLog cl = pld.getLog( children[i] ) ;
                if ( cl != null ) {
                   results.add( cl ) ;   
                }
            }
            result = new GraphNode[ results.size() ] ;
            for (int i=0;i<results.size();i++) {
                UniqueObjectLog cl = ( UniqueObjectLog ) results.get(i) ;
                if ( cl != null ) {
                    result[i] = new PEGraphNode( cl ) ;
                }
            }
        }
        if ( result != null ) {
            n.addSuccessors( result ) ;
        }
        return result ;
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
        PEGraphNode penode = ( PEGraphNode ) n ;
        penode.setDepth( 0 ) ;
    }
    
    /**
     * Compares two nodes for equality.  Should test to see whether two
     * nodes are equal.
     */
    public boolean isEqual(GraphNode n1, GraphNode n2) {
        return n1.equals( n2 ) ;
    }
    
    /** Never replace. */
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
     * Does nothing in this case.
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
        throw new UnsupportedOperationException( ) ;
    }
    
    /**
     * Optional method returning the number of descendents for a graph node.
     * This is only used by search algorithms that do not immediately expand
     * all graph nodes.
     */
    public int getNumDescendants(GraphNode n) {
        throw new UnsupportedOperationException( ) ;
    }
    
    HashMap terminalMap = new HashMap() ;
    PlanLogDatabase pld ;
}
