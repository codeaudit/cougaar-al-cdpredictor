package com.axiom.lib.objectgraph ;
import com.axiom.lib.util.* ;
import com.axiom.pspace.search.* ;
import java.util.* ;
import java.lang.reflect.* ;
import com.axiom.lib.util.HashSet ;


public class ObjectSearchStrategy implements Strategy {

    protected ObjectSearchStrategy() {
        leafNodes = new Hashtable( defaultLeafNodes ) ;
    }

    public void setObjectGraph( ObjectGraph og ) {
        this.objectGraph = og ;
    }

    /**
     *  Make an empty graph node.  This will be used as the "factory" node
     *  for this <code>Strategy</code>.
     */
    public GraphNode makeNode() {
        return new ObjectNode( null ) ;
    }

    /** Sets a list of terminals for the search.  If any objects in the
     * the collection are encountered, GraphNode objects with their referents will be added to the
     * list of terminals.
     */
    public void setStopList( Collection stopList ) {
        for ( Iterator iter = stopList.iterator(); iter.hasNext() ; ) {
            ObjectNode o = ( ObjectNode ) iter.next() ;

            // Eliminate potential duplicates.
            if ( getStopObject( o ) == null ) {
                stopSet.put( o, o ) ;
            }
        }
    }

    public Enumeration getStopList() {
        return stopSet.elements() ;
    }

    public void setLeafNodeClasses( Collection leafNodeClasses ) {
        Iterator iter = leafNodeClasses.iterator() ;
        while ( iter.hasNext() ) {
            Object o = iter.next() ;
            if ( o instanceof Class ) {
                leafNodes.put( o, o ) ;
            }
        }
    }

    public void setLeafNodeClasses( Enumeration classes ) {
        while ( classes.hasMoreElements() ) {
            Object o = classes.nextElement() ;
            if ( o instanceof Class ) {
                leafNodes.put( o, o ) ;
            }
        }
    }


    public Enumeration getTerminalNodes() {
        return terminals.elements() ;
    }

    protected ObjectNode getTerminal( ObjectNode on ) {
        return ( ObjectNode ) terminals.get( on ) ;
    }

    protected ObjectNode getStopObject( ObjectNode on ) {
        if ( on.getObject() == null )
            return null ;
        return ( ObjectNode ) stopSet.get( on ) ;
    }

    protected GraphNode[] expandArray( ObjectNode node ) {

        Object array = node.getObject() ;
        int arraySize = Array.getLength( array ) ;
        GraphNode[] result = new GraphNode[ arraySize ] ;
        for (int i=0;i<arraySize;i++) {
            Object ao = Array.get( array, i ) ;
            ObjectNode on = new ObjectNode( ao ) ;
            on.setParent( node );
            if ( getStopObject( on ) != null ) {
                terminals.put( on, on );
            }
            result[i] = on ;
        }
        node.setSuccessors( result ) ;
        return result ;
    }

    public boolean isLeafNode( Class c ) {
        return leafNodes.get( c ) != null ;
    }

    private Field[][] getAllFields( Class c ) {
        Class tc = c ;
        ArrayList list = new ArrayList() ;
        while ( tc != Object.class ) {
            Field[] fields = ( Field[] ) fieldTable.get( tc.getName() ) ;
            if ( fields == null ) {
                fields = tc.getDeclaredFields() ;
                AccessibleObject.setAccessible( fields, true ) ;
                ArrayList flist = new ArrayList( fields.length ) ;
                for (int j=0;j<fields.length;j++) {
                    Field f = fields[j] ;
                    if ( !f.getType().isPrimitive() &&
                         !Modifier.isStatic( f.getModifiers() ) &&
                         !isLeafNode( f.getType() ) )
                    {
                        flist.add( f ) ;
                    }
                }
                fields = new Field[ flist.size() ] ;
                for (int i=0;i<flist.size();i++) {
                    fields[i] = ( Field ) flist.get(i) ;
                }
                fieldTable.put( tc.getName(), fields ) ;
            }
            list.add( fields ) ;
            tc = tc.getSuperclass() ;
        }
        Field[][] result = new Field[list.size()][] ;
        for (int i=0;i<list.size();i++) {
            result[i] = ( Field[] ) list.get(i) ;
        }
        return result ;
    }

    private Field[][] getAllPersistantFields( Class c ) {
        Class tc = c ;
        ArrayList list = new ArrayList() ;
        while ( tc != Object.class ) {
            Field[] fields = ( Field[] ) fieldTable.get( tc.getName() ) ;
            if ( fields == null ) {
                fields = tc.getDeclaredFields() ;
                AccessibleObject.setAccessible( fields, true ) ;
                ArrayList flist = new ArrayList( fields.length ) ;
                for (int j=0;j<fields.length;j++) {
                    Field f = fields[j] ;
                    if ( !f.getType().isPrimitive() &&
                         !Modifier.isStatic( f.getModifiers() ) &&
                         !isLeafNode( f.getType() ) &&
                         !Modifier.isTransient( f.getModifiers() ) )
                    {
                        flist.add( f ) ;
                    }
                }
                fields = new Field[ flist.size() ] ;
                for (int i=0;i<flist.size();i++) {
                    fields[i] = ( Field ) flist.get(i) ;
                }
                fieldTable.put( tc.getName(), fields ) ;
            }
            list.add( fields ) ;
            tc = tc.getSuperclass() ;
        }
        Field[][] result = new Field[list.size()][] ;
        for (int i=0;i<list.size();i++) {
            result[i] = ( Field[] ) list.get(i) ;
        }
        return result ;
    }

    /**
     *  Expand a node into multiple children.
     */
    public GraphNode[] expand( GraphNode n ) {
        ObjectNode on = ( ObjectNode ) n ;
        Object o = on.getObject() ;

        //if ( o instanceof com.axiom.nspace.process.NSProcess ) {
        //    System.out.println( "Processing NSProcess " + o.getClass().getName() + ":" + o.toString() ) ;
        //}

        // If I am a terminal, do not expand.
        if ( o == null || getTerminal( on ) != null ) {
            on.setSuccessors( null );
            return null ;
        }

        Class c = o.getClass() ;
        // Lookup fields for class.  If not extant, make
        if ( c.isArray() && !c.getComponentType().isPrimitive() &&
             !isLeafNode( c.getComponentType() ) ) {
            return expandArray( on ) ;
        }

        Field[][] fields = getAllPersistantFields( c );

        // Expand in terms of non-primitive, non-static fields that are not
        // leaf nodes
        ArrayList list = new ArrayList( fields.length ) ;
        // For all non-primitive fields, get objects
        for (int i=0;i<fields.length;i++) {
            for (int j=0;j<fields[i].length;j++) {
                Object tmp = null ;
                try {
                tmp = fields[i][j].get( o ) ;
                }
                catch( Exception e ) {}
                if ( tmp == null || isLeafNode(tmp.getClass()) ) {
                    continue ;
                }
                ObjectNode onr = new ObjectNode( on, tmp, fields[i][j] ) ;
                ObjectNode tn = getStopObject( onr ) ;
                if ( tn != null ) {
                    terminals.put( onr, onr ) ;
                }
                list.add( onr ) ;
            }
        }

        GraphNode[] result = new GraphNode[ list.size() ] ;
        for (int i=0;i<list.size();i++) {
            result[i] = ( GraphNode ) list.get(i) ;
        }
        on.setSuccessors( result );
        return result ;
    }
    
    /**
     *  Parents should never be updated in a depth first search.
     */
    public void updateParent( GraphNode n1, GraphNode n2 ) {
        throw new IllegalArgumentException() ;
    }
    
    /**
     *  Optional method returning the number of descendents for a graph node.
     *  This is only used by search algorithms that do not immediately expand
     *  all graph nodes.
     */
    public int getNumDescendants( GraphNode n ) {
        throw new IllegalArgumentException() ;
    }

    /**
     *  Does nothing.
     */
    public void initNode( GraphNode n ) { }
   
    public GraphNode expand( GraphNode n, int i ) { throw new IllegalArgumentException() ; }

    public boolean isEqual( GraphNode n1, GraphNode n2 ) {
        ObjectNode on1 = ( ObjectNode ) n1 ;
        ObjectNode on2 = ( ObjectNode ) n2 ;
        return on1.getObject() == on2.getObject() ;
    }

    public int compare( Object n1, Object n2 ) {
        ObjectNode on1 = ( ObjectNode ) n1, on2 = ( ObjectNode ) n2 ;
        return on1.compareTo( on2 ) ;
    }

    /**
     *  Determines whether or not a node is a goal node.
     */
    public boolean isGoalNode( GraphNode n ) { return false ; }

    /**
     *  Field table indexed by class name.
     */
    protected Hashtable fieldTable = new Hashtable() ;

    protected Hashtable stopSet = new Hashtable() ;
    protected Hashtable terminals = new Hashtable() ;
    protected Hashtable leafNodes = new Hashtable() ;
    protected static Hashtable defaultLeafNodes = new Hashtable() ;

    static {
        defaultLeafNodes.put( String.class, String.class ) ;
        defaultLeafNodes.put( Integer.class, Integer.class ) ;
        defaultLeafNodes.put( Float.class, Float.class ) ;
        defaultLeafNodes.put( Class.class, Class.class ) ;
    }

    protected ObjectGraph objectGraph ;
}
