package com.axiom.lib.objectgraph ;
import com.axiom.lib.util.* ;
import com.axiom.pspace.search.* ;
import java.util.* ;
import com.axiom.lib.util.HashSet ;

/** Uses reflection to build a graph representing the
 *  in-memory graph starting at a root
 *  object.
 */

public class ObjectGraph extends AbstractSearch {

    public ObjectGraph( Object root ) {
        super( new ObjectSearchStrategy() ) ;
        ObjectSearchStrategy s = ( ObjectSearchStrategy ) getStrategy() ;
        s.setObjectGraph( this );
        this.root = root ;
        if ( root != null ) {
            init( new ObjectNode( root ) );
        }
    }

    public void setStopList( Collection c ) {
        ObjectSearchStrategy s = ( ObjectSearchStrategy ) getStrategy() ;
        s.setStopList( c ) ;
    }

    public void setLeafClasses( Collection c ) {
        ObjectSearchStrategy s = ( ObjectSearchStrategy ) getStrategy() ;
        s.setLeafNodeClasses( c );
    }

    public void initDatabase() {
       openList.clear();
       closedList.clear() ;
       hashtable.clear() ;
    }

    public int getNumOpenNodes() {
        return openList.size() ;
    }

    public void dump() {
       System.out.println( "Open Nodes" ) ;
       Iterator iterator = openList.iterator() ;

       while( iterator.hasNext() ) {
           Object o = iterator.next() ;
           System.out.println( o );
       }
       System.out.println() ;

       /**
        System.out.println( "Closed Nodes" ) ;
        for (int i=0;i<closedList.size();i++) {
            System.out.println( closedList.elementAt(i) ) ;
        }
        */
    }

    public GraphNode getFirstOpenNode() {
        return ( GraphNode)  openList.removeFirst() ;
    }

    public GraphNode findNode( GraphNode node ) {
        Object[] r = hashtable.getObjects( node ) ;
        if ( r == null ) return null ;
        for (int i=0;i<r.length;i++) {
            if ( node.isIdentical( ( GraphNode ) r[i] ) ) {
                return ( GraphNode ) r[i] ;
            }
        }
        return null ;
    }

    public boolean addNode( GraphNode node ) {
       hashtable.put( node, node ) ;
       return true ;
    }

    public void addToOpen( GraphNode n ) {
       openList.add( n ) ;
    }
    
    /**  Should never be called.
     */
    public boolean removeFromOpen( GraphNode n ) {
        throw new UnsupportedOperationException() ;
    }
    
    public void addToClosed( GraphNode n ) {
       closedList.add( n ) ;
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

    public Enumeration getOpenNodes() {
        return Collections.enumeration( openList ) ;
    }

    public Enumeration getClosedNodes() {
        return Collections.enumeration( closedList ) ;
    }

    /** A collection of ObjectNodes representing terminals, e.g. those
     * elements in the tree which are on the stopList.
     */
    public Enumeration getTerminals() {
        return ( ( ObjectSearchStrategy ) strategy ).getTerminalNodes() ;
    }

    public static void main( String[] args ) {

        Vector mice = new Vector() ;
        Vector moose = new Vector() ;
        mice.addElement( moose ) ;
        ObjectGraph og = new ObjectGraph( moose ) ;
        og.run();
        ObjectGraph og2 = new ObjectGraph( mice ) ;
        ArrayList list = new ArrayList() ;
        for ( Enumeration e = og.getClosedNodes() ;
              e.hasMoreElements() ; ) {
            list.add( e.nextElement() ) ;
        }
        og2.setStopList( list );
        og2.run() ;
        System.out.println( "Terminals:" ) ;
        for ( Enumeration e = og2.getTerminals();e.hasMoreElements(); ) {
            ObjectNode o = ( ObjectNode ) e.nextElement() ;
            System.out.println(  o.getObject().getClass().toString() + "@" + System.identityHashCode( o.getObject() ) );
        }

    }

    protected LinkedList openList = new LinkedList();

    protected LinkedList closedList = new LinkedList() ;

    protected HashSet hashtable = new HashSet() ;

    protected Object root ;
}