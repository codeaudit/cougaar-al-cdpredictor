package org.cougaar.tools.castellan.pspace.search ;
import java.util.ArrayList ;

/**
 *  Extend the default graph node.  This is built for speed and doesn't have much
 *  in the way of error checking.
 */
public abstract class DefaultGraphNode extends GraphNode {

    public DefaultGraphNode() { } 

    public DefaultGraphNode( GraphNode parent ) {
        super( parent );   
    }
    
    public DefaultGraphNode( GraphNode parent, GraphNode[] successors ) {
        super( parent );
        addSuccessors( successors ) ;
    }
    
    public int getNumSuccessors() {
        if ( successors == null ) {
            return 0 ;
        }
        return successors.length ;   
    }
    
    public void addSuccessors( GraphNode[] n ) {
        if ( successors == null ) {
            successors = new GraphNode[ n.length ] ;
            System.arraycopy( n, 0, successors, 0, n.length );
        }
        else if ( successors.length == 0 ) {
            successors = new GraphNode[n.length];
            System.arraycopy( n, 0, successors, 0, n.length ) ;
        }
        else {
            GraphNode[] temp = new GraphNode[successors.length + n.length];
            System.arraycopy( n, 0, temp, 0, n.length );
            System.arraycopy( successors, 0, temp, n.length, successors.length ) ;
            successors = temp ;
        }
    }

    public void addSuccessor( GraphNode n ) {
        if ( successors == null ) {
            successors = new GraphNode[] { n } ;
        }
        else {
            GraphNode[] temp = new GraphNode[successors.length + 1];
            System.arraycopy( successors, 0, temp, 0, successors.length );
            temp[successors.length] = n ;
            successors = temp ;
        }
    }

    public GraphNode getSuccessor( int i ) {
        return successors[i] ;
    }

    public void setSuccessors( GraphNode[] n ) {
        if ( n == null ) {
            successors = null ;
        } else {
        successors = ( GraphNode[] ) n.clone() ;
        }
    }

    public void replaceSuccessor( int i, GraphNode n ) {
        successors[i] = n ;
    }

    public void addPredecessor( GraphNode n ) {
        if ( predecessors == null ) {
            predecessors = new ArrayList() ;
        }
        predecessors.add( n ) ;
    }

    public GraphNode getPredecessor( int i ) {
        if ( predecessors == null ) {
            return null ;
        }
        return ( GraphNode ) predecessors.get(i) ;
    }

    public int getNumPredecessors() {
        if ( predecessors == null ) {
            return 0 ;
        }
        return predecessors.size() ;
    }

    protected GraphNode[] successors ;
    protected ArrayList predecessors ;
}


