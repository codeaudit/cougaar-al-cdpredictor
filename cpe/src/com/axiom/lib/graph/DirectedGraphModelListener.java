package com.axiom.lib.graph ;

public interface DirectedGraphModelListener {

    public void graphNodesAdded( GraphModelEvent e ) ;

    public void graphNodesRemoved( GraphModelEvent e) ;

    public void graphEdgesAdded( GraphModelEvent e ) ;

    public void graphEdgesRemoved( GraphModelEvent e ) ;
}