package org.hydra.pspace.search ;
import java.util.* ;

/**
 *  An interface for graph searching.
 */
public interface GraphSearch {
    
    /**
     *  A search has been initialized or reinitialzed with a single node.
     */
    public final static int START = 0;
    
    /**
     *  A search has terminated with no more nodes on the open list.
     */
    public final static int NO_MORE_NODES = -1;
    
    /**
     *  A search has finished found the goal node.
     */
    public final static int GOAL_FOUND = -2 ;   
   
    /** A search has expanded all nodes at the maximum depth.
     */
    public final static int MAX_DEPTH_REACHED = -3 ;
    
    /**
     *  A search has expanded the maximum number of nodes.
     */
    public final static int MAX_NODES_EXPANDED = -4 ;
        
    /**
     *  Reinitialize search, placing node n on the open list.
     */
    public void init( GraphNode n );

    /**  Place n on the open list without reinitializing the search.
     */
    public void initNode( GraphNode n ) ;
    
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