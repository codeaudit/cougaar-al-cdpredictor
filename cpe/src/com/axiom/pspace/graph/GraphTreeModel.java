package com.axiom.pspace.graph ;

/**
 *  Represent a graph as a tree.  Each node has a unique immediate parent,
 * although other parents may be shown as well.
 */
public interface GraphTreeModel {

    public Object getRoot() ;

    public Object getImmediateParent( Object o ) ;

    public boolean isLeaf( Object o ) ;

    //public int getNumParents( Object o ) ;

    //public Object getParent( Object o, int index ) ;

    public int getChildCount( Object o ) ;

    public int getIndexOfChild( Object par, Object child ) ;

    public Object getChild( Object o, int index ) ;

    public Object getEdge( Object o, int index ) ;

}