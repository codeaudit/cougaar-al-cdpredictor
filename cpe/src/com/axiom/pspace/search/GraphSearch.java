package com.axiom.pspace.search ;
import java.util.* ;

/**
 *  An interface for graph searching.
 */
public interface GraphSearch {
    
    /**
     *  A mplan has been initialized or reinitialzed with a single node.
     */
    public final static int START = 0;
    
    /**
     *  A mplan has terminated with no more nodes on the open list.
     */
    public final static int NO_MORE_NODES = -1;
    
    /**
     *  A mplan has finished found the goal node.
     */
    public final static int GOAL_FOUND = -2 ;   
   
    /** A mplan has expanded all nodes at the maximum depth.
     */
    public final static int MAX_DEPTH_REACHED = -3 ;
    
    /**
     *  A mplan has expanded the maximum number of nodes.
     */
    public final static int MAX_NODES_EXPANDED = -4 ;
        
    /**
     *  Place n on open list and reinitialize mplan.
     */
    public void init( GraphNode n );
    
    /**
     *  Run the algorithm using the default parameters.
     *  (What constitutes the parameters has not yet been
     *  formulated.)
     */
    public void run();
    
    /**
     *  Add newly-expanded nodes to open list.
     */
    public void addNodes( GraphNode[] nodes ) ;

    /**
     *  Get an enumeration of all open nodes.
     */
    public Enumeration getOpenNodes() ;

    /**
     *  Get closed nodes.
     */
    public Enumeration getClosedNodes() ;

}