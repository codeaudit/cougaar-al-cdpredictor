package com.axiom.lib.graph ;
import java.util.* ;

/**
 *  Model for directed graphs.  Both successors and predecessors are known and tracked.
 */
public interface DirectedGraphModel {

    /**
     *  Returns one or more objects which serves as the root(s) of
     *  the model.  This can be an arbitrary node, or may be special nodes, eg.
     *  as in "sources" in a DAG.
     */
    public Object[] getRoots() ;

    /**
     *  Returns a list of nodes.
     */
    public Enumeration getNodes() ;

    /**
     *  Finds nodes which have no successors.
     */
    // public Object[] getSources() ;

    public int getNumSuccessors( Object o ) ;

    public Object getSuccessor( Object o, int index ) ;

    public int getIndexOfSuccessor( Object o, Object successor ) ;

    /** Returns a double valued cost of traversing (directed) edge
     * between o and successor.
     */
    public double getCost( Object o, int successorIndex ) ;

    public Object getPredecessor( Object o, int index ) ;

    public int getIndexOfPredecessor( Object o, Object pred ) ;

    public void addModelListener( DirectedGraphModelListener l ) ;

    public void removeModelListener( DirectedGraphModelListener l ) ;
}
