package com.axiom.lib.graph ;

public abstract class AbstractDigraphModel implements DirectedGraphModel {

    /**
     *  Returns a list of all possible cycles ( and permutations of such. )
     */
    public Object[][] findCycles() {
        return null ;
    }

    //public Enumeration depthFirstEnumeration() ;

    public Object[][] depthSort() {
        return null ;
    }

    public Object[][] depthSort( int maxDepth ) {
        return null ;
    }

    public Object[][] costSort() {
        return null ;
    }

    public Object[][] costSort( int maxDepth ) {
        return null ;
    }
}