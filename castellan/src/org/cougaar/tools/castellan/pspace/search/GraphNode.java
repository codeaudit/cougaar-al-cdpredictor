package org.cougaar.tools.castellan.pspace.search ;
import java.lang.Comparable ;

/**
 *  A GraphNode class that can be extended.
 */
 
public abstract class GraphNode {
    public GraphNode() {
    }

    public GraphNode( GraphNode parent ) {
        this.parent = parent ;   
    }
    
    /**
     *  Update the "backwards" pointer from this node.
     */
    public void setParent( GraphNode n ) {
        parent = n ;   
    }
    
    public boolean equals( Object o ) {
        try {
            GraphNode n = ( GraphNode ) o ;
            return isIdentical( n ) ;
        }
        catch ( ClassCastException e ) {
            return false ;   
        }
    }
        
    /**
     *  Determine whether two nodes are logically equivalent to the
     *  same"node in the unrealized graph.  For example, the same board state
     *  in a chess game may be generated through different sequences of moves,
     *  but are all logically equivalent.  This is used by the GRAPHSEARCH
     *  procedure to find equivalent nodes.
     *  <p>
     *  It does not indicate that any cost values stored in the node are
     *  the same, since multiple identical nodes may be generated through different
     *  paths during search.
     *
     */
    public abstract boolean isIdentical( GraphNode n ) ;
    
    /**
     *  Get the number of (current) successors.
     */
    public abstract int getNumSuccessors() ;
    
    /**
     *  Get the ith successor.
     */
    public abstract GraphNode getSuccessor( int i ) ;

    /**
     *  GraphNodes should implement meaningful hashCode methods if neccessary.
     */
    public abstract int hashCode() ;
    
    /**
     *  Replace a successor node at index i.
     */
    public abstract void replaceSuccessor( int i, GraphNode n ) ;
    
    /**
     *  Add an array of successors.
     */
    public abstract void addSuccessors( GraphNode[] n ) ;
    
    public abstract void setSuccessors( GraphNode[] n ) ;
    
    /**
     *  Add a successor.
     */
    public abstract void addSuccessor( GraphNode n ) ;

    // public abstract void removeSuccessor( GraphNode n ) ;

    public abstract void addPredecessor( GraphNode n ) ;

    public abstract GraphNode getPredecessor( int i ) ;

    public abstract int getNumPredecessors() ;

    public GraphNode getParent() { return parent ; }

    public int getDepth() { return depth ; }

    public void setDepth( int depth ) { this.depth = depth ; }

    protected int depth = 0 ;

    /** Parent is the pointer to the "least-cost" backwards edge.
     */
    protected GraphNode parent ;
}
