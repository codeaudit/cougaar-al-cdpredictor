package com.axiom.pspace.search ;

/**
 *  Interface for strategy defines methods for expanding and comparing nodes.
 *  Implement this interface for different mplan strategies and heuristics.
 */
public interface Strategy extends java.util.Comparator {

    /**
     *  Make an empty graph node.  This will be used as the "factory" node
     *  for this <code>Strategy</code>.
     */
    GraphNode makeNode() ;

    /**
     *  Expand a node into multiple children.
     */
    public GraphNode[] expand( GraphNode n ) ;
    
    /**
     *  The name of this method is misleading since it is
     *  called after node <code>n1</code>'s parent has been already been
     *  changed.
     *
     *  <p>
     *  It is important to note that since n1 and n2 are considered
     *  equivalent, only <code>n1</code> will be retained by the GraphSearch
     *  algorithm after <code>updateParent</code> is called.
     *
     *  <p> This method should update n1 according to n2's current cost
     *  and heuristic value.
     */
    public void updateParent( GraphNode n1, GraphNode n2 ) ;
    
    /**
     *  Optional method returning the number of descendents for a graph node.
     *  This is only used by mplan algorithms that do not immediately expand
     *  all graph nodes.
     */
    public int getNumDescendants( GraphNode n ) ;

    /**
     *  Initialize a node generated as a initial state.  Generally, this
     *  introduces information on the node usually generated during
     *  exapnsion.
     */
    public void initNode( GraphNode n ) ;
   
    /**
     *  Optional method for expanding the ith node associated with n.
     *
     *  @param n A GraphNode to be expanded.
     *  @param i The index of the successor node to expand.
     */
    public GraphNode expand( GraphNode n, int i ) ;
    
    /**
     *  Compares two nodes for equality.  Should test to see whether two
     *  nodes are equal.
     */
    public boolean isEqual( GraphNode n1, GraphNode n2 ) ;
    
    /**
     *  Compare the heuristic value for two nodes.  By convention, if n1 
     *  is "better" (less cost) than n2, then f(n1) < f(n2).  Note that
     *  this implementation does not rely on the actual values for the
     *  estimate f(n), only the relative ordering of the two nodes.  This
     *  allows us to implement strategies which do not rely on actual 
     *  computation of f(n) values.
     *
     *  @param n1 A GraphNode
     *  @param n2 A GraphNode
     */
    public int compare( Object n1, Object n2 );

    /**
     *  Determines whether or not a node is a goal node.
     */
    public boolean isGoalNode( GraphNode n );
}