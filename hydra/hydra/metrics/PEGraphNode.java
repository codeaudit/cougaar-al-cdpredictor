/*
 * PEGraphNode.java
 *
 * Created on October 3, 2001, 3:11 PM
 */

package org.hydra.metrics;
import org.hydra.pspace.search.* ;

/**
 *
 * @author  wpeng
 * @version 
 */
public class PEGraphNode extends DefaultGraphNode {

    /** Creates new PEGraphNode */
    public PEGraphNode( UniqueObjectLog pel ) {
        if ( pel == null ) {
            throw new RuntimeException( "PEGraphNode():: Null UniqueObjectLog" ) ;
        }
        if ( pel.getUID() == null ) {
            throw new RuntimeException( "UniqueObjectLog " + pel + " has no null UID." ) ;
        }
        this.pel = pel ;
    }
    
    public String toString() {
        return "[PEGraphNode, depth=" + getDepth() + ",maxDepth=" + getMaxDepth() + ",log=" + pel + "]" ;
    }
    
    public int getMaxDepth() { return maxDepth ; }
    
    public void setMaxDepth( int maxDepth ) { this.maxDepth = maxDepth ; }
    
    public UniqueObjectLog getLog() { return pel ; }
        
    /**
     * GraphNodes should implement meaningful hashCode methods if neccessary.
     */
    public int hashCode() {
        return pel.getUID().hashCode() ;
    }

    public boolean isIdentical(GraphNode n) {
        if ( !( n instanceof PEGraphNode ) ) { return false ; }
        PEGraphNode penode = ( PEGraphNode ) n ;
        return penode.pel == pel ;
    }
    
    public boolean isTerminal() { return isTerminal ; }
    
    public void setTerminal( boolean value ) { isTerminal = value ; }
    
    boolean isTerminal = false ;
    int maxDepth ;
    UniqueObjectLog pel ;
}
