/*
 * Subgraph.java
 *
 * Created on October 8, 2001, 9:57 PM
 */

package org.hydra.metrics;
import org.hydra.pdu.* ;
import java.util.* ;

/**
 *
 * @author  wpeng
 * @version
 */
public class Subgraph {
    
    /** Creates new Subgraph */
    public Subgraph() {
    }
        
    public void addRoot( TaskLog tl ) {
        if ( roots.indexOf( tl ) == -1 ) {
            roots.add( tl ) ;
        }
    }
    
    public void addLeaf( TaskLog tl ) {
        if ( leaves.indexOf( tl ) == -1 ) {
            leaves.add( tl ) ;
        }
    }
    
    public void addElement( TaskLog ul ) {
        elements.put( ul.getUID(), ul ) ;
    }
    
    public TaskLog getElement( UIDPDU uid ) {
        return ( TaskLog ) elements.get( uid ) ;
    }
    
    public int getNumElements() { 
        return elements.size() ; 
    }
    
    public Iterator elements() { 
        return elements.values().iterator() ; 
    }
    
    public ArrayList leaves = new ArrayList() ;
    public ArrayList roots = new ArrayList() ;
    public HashMap elements = new HashMap() ;
    
    public String toString( ) {
        StringBuffer buf = new StringBuffer() ;
        buf.append( "Subgraph" ) ;
        buf.append( "\n[" ) ;
        buf.append( "Roots=" ) ;
        for (Iterator iter = roots.iterator(); iter.hasNext(); ) {
            TaskLog tl = ( TaskLog ) iter.next() ;
            buf.append( "["  + tl + "]"  ) ;
            if ( iter.hasNext() ) {
                buf.append( "," ) ;
            }
        }
        buf.append( "]\n" ) ;
        buf.append( "\tLeaves=" ) ;
        for (Iterator iter = elements.values().iterator(); iter.hasNext(); ) {
            TaskLog tl = (TaskLog ) iter.next() ;
            buf.append( "["  + tl.getUID() + "," + tl.getTaskVerb() + "]"  ) ;
            if ( iter.hasNext() ) {
                buf.append( "," ) ;
            }
        }
        buf.append( "]" ) ;
        return buf.toString() ;
        /**
        ServerApp.instance().print( "[" ) ;
        for (Iterator iter = elements.values().iterator(); iter.hasNext(); ) {
            TaskLog tl = (TaskLog ) iter.next() ;
            ServerApp.instance().print( "["  + tl.getUID() + "," + tl.getTaskVerb() + "]"  ) ;
            if ( iter.hasNext() ) {
                ServerApp.instance().print( "," ) ;
            }
        }
        ServerApp.instance().println( "]" ) ;
        ServerApp.instance().println( "" ) ;
         */
    }
    
}
