package com.axiom.lib.graph ;
import java.util.* ;
import java.util.LinkedList ;

public abstract class DefaultDigraphModel implements DirectedGraphModel {

    public abstract static class Node {

        protected void addSuccessor( Node o ) {
            if ( !( o instanceof Node ) )
                return ;
            if ( successors.indexOf(o) == -1 ) {
                successors.addElement( o ) ;
                Node n = ( Node ) o ;
                n.addPredecessor( this ) ;
            }

        }

        public int getNumSuccessors() { return successors.size() ; }

        public Node getSuccessor( int index ) {
            return ( Node ) successors.elementAt(index ) ;
        }

        public int getNumPredecessors() { return predeccessors.size() ; }

        public Node getPredeccessor(int index ) { return ( Node ) predeccessors.elementAt(index) ; }

        private void addPredecessor( Node o ) {
            if ( predeccessors.indexOf(o ) == -1 )
                predeccessors.addElement( o ) ;
        }

        protected Vector successors  = new Vector() ;

        protected Vector predeccessors = new Vector() ;
    }

    public void addNode( Node n) {
        this.nodeTable.put( new Integer( System.identityHashCode(n) ) , n ) ;
    }

    public void addSuccessor( Node parent, Node o ) {
        if ( nodeTable.get( new Integer( System.identityHashCode(parent) ) ) != parent
             || nodeTable.get( new Integer( System.identityHashCode(o) ) ) != o )
            return ;
        parent.addSuccessor( o ) ;
    }

    public void setRoot( Node o ) {
        this.roots = new Node[] { o } ;
    }

    public Object[] getRoots() {
        return roots ;
    }
    
    public double getCost( Object o, int i ) {
        return 1 ;
    }

    public int getNumSuccessors( Object n ) {
        Node node = ( Node ) n ;
        return node.getNumSuccessors() ;
    }

    public Object getSuccessor( Object n, int index ) {
        Node node = ( Node ) n ;
        return node.getSuccessor( index ) ;
    }

    public int getIndexOfSuccessor( Object n, Object s ) {
        Node node = ( Node ) n ;
        return node.successors.indexOf( s ) ;
    }

    public Object getPredeccessor( Object n, int index ) {
        Node node = ( Node ) n ;
        return node.predeccessors.elementAt(index) ;
    }

    public int getIndexOfPredeccessor( Object n, Object s ) {
        Node node = ( Node ) n ;
        return node.predeccessors.indexOf( s ) ;
    }

    public Enumeration getNodes() {
        return nodeTable.elements() ;
    }

    public void addModelListener( DirectedGraphModelListener gl ) {
        listeners.addElement( gl ) ;
    }

    public void removeModelListener( DirectedGraphModelListener gl ) {
        listeners.removeElement( gl ) ;
    }

    public Object[] getSources() {
        Vector results = new Vector() ;

        for (Enumeration e = getNodes();e.hasMoreElements();) {
            Object o = e.nextElement() ;
            if ( getNumSuccessors( o ) == 0 )
                results.addElement( o ) ;
        }

        Object[] result ;
        results.copyInto( result = new Object[ results.size() ] ) ;
        return result ;
    }

    /**
     *  Returns a list of all possible directed cycles (and permutations.)
     */
    public Object[][] findCycles() {
        

        return null ;
    }

    //public Enumeration depthFirstEnumeration() ;

    /**
     *  Returns a "minimum" depth sorted list of nodes.  The roots are assumed to be at depth 0
     *  Note that not all nodes are necessarily returned by the depth sort, since
     *  not all nodes will be reachable from any set of roots.
     *
     *  <p> The depth of each node is defined as the minimum depth required to reach it
     *  from any of the roots.
     */
    public synchronized Object[][] depthSort() {

        Vector order = new Vector() ;
        LinkedList closed = new LinkedList() ;
        LinkedList open = new LinkedList() ;
        Hashtable cost = new Hashtable() ;

        for (int i=0;i<roots.length;i++) {
            closed.add( roots[i] ) ;
            int scount = getNumSuccessors( roots[i] ) ;
            for (int j=0;j<scount;j++) {
                Object s = getSuccessor( roots[i], j ) ;
                open.add( s ) ;
            }
        }

        while ( open.size() > 0 ) {
            // Pick a node off the open list.
        }

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

    protected Vector listeners = new Vector(4) ;
    protected Node[] roots ;

    /** Table hashed by identity hash code. */
    protected Hashtable nodeTable = new Hashtable() ;
}