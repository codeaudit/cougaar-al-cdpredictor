package org.cougaar.tools.castellan.pspace.search ;
import java.util.Iterator ;
import java.util.LinkedList ;
import java.util.Comparator ;
import java.util.Hashtable ;
import java.util.HashMap ;
import java.util.Enumeration ;
import com.objectspace.jgl.OrderedMap ;
import com.objectspace.jgl.BinaryPredicate ;
import com.objectspace.jgl.OrderedMapIterator ;
import com.objectspace.jgl.Pair ;

class ComparatorPred implements BinaryPredicate {
    ComparatorPred( Comparator c ) {
        comparator = c ;
    }
    
    public boolean execute( Object o1, Object o2 ) {
        
        //
        //  This is a tricky bit here.  Using '<' works
        //  with jgl3.0.0 but not 3.1.  Not sure what
        //  changed were made, but changing this to '<='
        //  works in 3.1.  Solution is probably to switch
        //  to a custom tree map if possible.
        if ( comparator.compare( o1, o2 ) <= 0 ) {
            return true ;  // o1 is to the left of o2
        }
        return false ;
    }
    
    Comparator comparator ;
}

/**
 *   Implements a search using a simple representation for the databases.
 */

public class SimpleSearch extends AbstractSearch {
    
    public SimpleSearch( Strategy s ) {
       super( s );
       openList = new OrderedMap( new ComparatorPred(s), true );
    }
    
    public void initDatabase() {
       openList.clear();
       closedList.setSize(0) ;
       hashtable.clear() ;
    }
    
    public int getNumOpenNodes() {
        return openList.size() ;   
    }
    
    public void dump() {
       System.out.println( "" ) ;
       System.out.println( "There are " + openList.size() + " open nodes." ) ;
       System.out.println( "Open Nodes" ) ;
       OrderedMapIterator e = openList.begin() ;
      
       for( ; e.hasMoreElements() ; e.advance() ) {
          Pair pair = ( Pair ) e.get() ;
          System.out.println( pair.second ) ;
       }
       System.out.println() ;
       System.out.println( "There are " + closedList.size() + " open nodes." ) ;       
       System.out.println( "Closed Nodes: " ) ;
       for (int i=0;i<closedList.size();i++) {
            System.out.println( closedList.elementAt(i) ) ;
       }
    }
    
    public GraphNode getFirstOpenNode() {
        OrderedMapIterator e = openList.begin() ;
        Pair obj = (Pair) openList.remove( e ) ;
        
        return ( GraphNode ) obj.second ;
    }
    
    public GraphNode findNode( GraphNode node ) {
        return ( GraphNode ) hashtable.get( node ) ;
    }
    
    public boolean addNode( GraphNode node ) {
       hashtable.put( node, node ) ;
       return true ;
    }
    
    public void addToOpen( GraphNode n ) {
       openList.put( n, n ) ;
    }
    
    /**
     *  Can be slow in the case where many nodes have the same f(n) value
     *  because it has to linearly search through the graph.  However, this method
     *  is only called if a new, lower cost path has been found to an already expanded
     *  node.
     *
     *  @param true if the node n is found and removed, false otherwise.
     */
    public boolean removeFromOpen( GraphNode n ) {
       OrderedMapIterator e = openList.lowerBound( n ) ;
       OrderedMapIterator e2 = openList.upperBound( n ) ;
       while ( e.hasMoreElements() ) {
          Pair p = ( Pair ) e.get() ;
          if ( n == p.second ) {
             openList.remove( e ) ;  // Assume only one instance of graphnode n
             return true;
          }
          else if ( e.equals( e2 ) ) {
             return false ; 
          }
          e.advance() ;
       }
       return false ;  // Should never reach here!
    }
    
    public void addToClosed( GraphNode n ) {
       closedList.addElement( n ) ;
    }
    
    public void addToOpen( GraphNode[] n ) {
       for (int i=0;i<n.length;i++) {
          addToOpen( n[i] ) ;
       }
    }
    
    public void addNodes( GraphNode[] n ) {
       for (int i=0;i<n.length;i++) {
          addNode( n[i] ) ;
       }
    }

    public Enumeration getOpenNodes() { return openList.elements() ; }

    public Enumeration getClosedNodes() { return closedList.elements() ; }
    
    protected OrderedMap openList ;
        
    protected java.util.Vector closedList = new java.util.Vector() ;
    
    protected HashMap hashtable = new HashMap() ;    
}