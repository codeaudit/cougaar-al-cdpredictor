package com.axiom.lib.graph ;

public class GraphModelEvent extends java.util.EventObject {
    public static final int NODES_ADDED = 0 ;
    public static final int NODES_REMOVED = 1 ;
    public static final int NODES_CHANGED = 2 ;
    public static final int EDGES_ADDED = 3 ;
    public static final int EDGES_REMOVED = 4 ;

    public GraphModelEvent( Object source, int eventType ) {
        super( source ) ;
        this.eventType = eventType ;
    }

    public Object[] getNodes() { return nodes ; }

    protected Object[] nodes ;

    protected int eventType ;
}