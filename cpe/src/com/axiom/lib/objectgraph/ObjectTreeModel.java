package com.axiom.lib.objectgraph ;
import javax.swing.tree.* ;
import javax.swing.event.* ;
import java.lang.ref.* ;
import java.lang.reflect.* ;
import java.util.* ;

/** Tree model for arbitrary object graph.
 */
public class ObjectTreeModel implements TreeModel {
    private static final ObjectTreeNode NULL = new ObjectTreeNode( null ) ;

    public ObjectTreeModel() {
        this( null ) ;
    }

    public ObjectTreeModel( Object root ) {
        if ( root != null ) {
            this.root = new ObjectTreeNode( root ) ;
        }
        else {
            this.root = NULL ;
        }
    }

    public Object getRoot() {
        return root ;
    }

    public void setRootObject( Object o ) {
        if ( o != null ) {
            this.root = new ObjectTreeNode( o ) ;
        }
        else {
            this.root = NULL ;
        }
        fireTreeStructureChanged( this, new Object[]{this.root}, new int[0], new Object[0] ) ;
    }

    protected boolean filter( Field f ) {
        if ( !Modifier.isStatic( f.getModifiers() ) ) {
            return true ;
        }
        return false ;
    }

    protected Field[] getDeclaredFields( Class c ) {
        Field[] fields = ( Field[] ) fieldTable.get( c ) ;
        if ( fields == null ) {
            fields = c.getDeclaredFields() ;
            AccessibleObject.setAccessible( fields, true ) ;
            ArrayList flist = new ArrayList( fields.length ) ;
            for (int j=0;j<fields.length;j++) {
                Field f = fields[j] ;
                if ( filter( f ) ) {
                    flist.add( f ) ;
                }
            }
            fields = new Field[ flist.size() ] ;
            for (int i=0;i<flist.size();i++) {
                fields[i] = ( Field ) flist.get(i) ;
            }
            fieldTable.put( c.getName(), fields ) ;
        }
        return fields ;
    }

    protected Field[] getAllFields( Class c ) {
        Field[] fields = ( Field[] ) allFieldMap.get( c ) ;
        Class tc = c ;
        ArrayList flist = new ArrayList() ;
        if ( fields == null ) {
            while ( tc != null ) {
                Field[] f1 = getDeclaredFields( tc ) ;
                for (int i=0;i<f1.length;i++) {
                    flist.add( f1[i] ) ;
                }
                tc = tc.getSuperclass() ;
            }
            fields = new Field[ flist.size() ] ;
            for (int i=0;i<flist.size();i++) {
                fields[i] = ( Field ) flist.get(i) ;
            }
            allFieldMap.put( c, fields ) ;
        }

        return fields ;
    }

    protected ObjectTreeNode getObjectTreeNode( Object o ) {
        ObjectTreeNode node = new ObjectTreeNode( o ) ;
        return ( ObjectTreeNode ) nodeMap.get( node ) ;
    }

    protected Object getChildOfArray( ObjectTreeNode anode, int index ) {
        int length = Array.getLength( anode.getObject() ) ;
        Object o = anode.getObject() ;
        if ( anode.getNumSuccessors() != length ) {
            ObjectTreeNode[] nodes = new ObjectTreeNode[ length ] ;
            for (int i=0;i<length;i++) {
                nodes[i] = new ArrayComponentNode( anode, Array.get( o, i ), i ) ;
            }
            anode.setSuccessors( nodes );
        }
        return anode.getSuccessor( index ) ;

    }

    public Object getChild(Object parent, int index) {
        ObjectTreeNode node = ( ObjectTreeNode ) parent ;
        if ( node.getObject().getClass().isArray() ) {
            return getChildOfArray( node, index ) ;
        }

        Field[] fields = getAllFields( node.getObject().getClass() ) ;

        // Lazy expand of successors
        if ( node.getNumSuccessors() != fields.length ) {
            ObjectTreeNode[] successors = new ObjectTreeNode[ fields.length ] ;
            for (int i=0;i<fields.length;i++) {
               Field f = fields[i] ;
               Object o = null ;
               try { o = f.get( node.getObject() ) ; }
               catch ( IllegalAccessException e ) { }
               ObjectTreeNode temp = new ObjectTreeNode( node, o, f ) ;
               successors[i] = temp ;
            }
            node.setSuccessors( successors );
        }

        return node.getSuccessor( index ) ;
    }

    public int getChildCount(Object parent) {
        ObjectTreeNode node = ( ObjectTreeNode ) parent ;
        if ( node.getObject() == null ) {
            return 0 ;
        }
        else if ( node.getObject().getClass().isArray() ) {
            return Array.getLength( node.getObject() ) ;
        }
        else {
            Field[] fields = getAllFields( node.getObject().getClass() ) ;
            return fields.length ;
        }
    }

    public boolean isLeaf(Object node) {
        if ( node == NULL ) return true ;
        ObjectTreeNode n = ( ObjectTreeNode ) node ;
        if ( n.getField() == null ) {
            if ( n.getObject() == null ) {
                return true ;
            }
            ObjectTreeNode pnode = ( ObjectTreeNode ) n.getParent() ;
            if ( pnode != null ) {
                Object po = pnode.getObject() ;
                if ( po.getClass().isArray() && po.getClass().getComponentType().isPrimitive() ) {
                    return true ;
                }
            }
            return false ;
        }
        else { // I am a primitive field
            return n.getField().getType().isPrimitive() ;
        }
    }

    public void valueForPathChanged(TreePath path, Object newValue) {
        //TODO: implement this javax.swing.tree.TreeModel method;
    }

    public int getIndexOfChild(Object parent, Object child) {
        ObjectTreeNode parNode = ( ObjectTreeNode ) parent ;
        ObjectTreeNode childNode = ( ObjectTreeNode ) child ;
        for (int i=0;i<parNode.getNumSuccessors();i++) {
            if ( parNode.getSuccessor(i) == childNode ) {
                return i ;
            }
        }
        return -1 ;
    }

    protected void fireTreeStructureChanged(Object source, Object[] path,
                                        int[] childIndices,
                                        Object[] children) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==TreeModelListener.class) {
                // Lazily create the event:
                if (e == null)
                    e = new TreeModelEvent(source, path,
                                           childIndices, children);
                ((TreeModelListener)listeners[i+1]).treeStructureChanged(e);
            }
        }
    }

    protected void fireTreeNodesChanged(Object source, Object[] path,
                                        int[] childIndices,
                                        Object[] children) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==TreeModelListener.class) {
                if (e == null)
                    e = new TreeModelEvent(source, path,
                                           childIndices, children);
                ((TreeModelListener)listeners[i+1]).treeNodesChanged(e);
            }
        }
    }

    protected void fireTreeNodesInserted(Object source, Object[] path,
                                        int[] childIndices,
                                        Object[] children) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==TreeModelListener.class) {
                // Lazily create the event:
                if (e == null)
                    e = new TreeModelEvent(source, path,
                                           childIndices, children);
                ((TreeModelListener)listeners[i+1]).treeNodesInserted(e);
            }
        }
    }

    protected void fireTreeNodesRemoved(Object source, Object[] path,
                                        int[] childIndices,
                                        Object[] children) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==TreeModelListener.class) {
                // Lazily create the event:
                if (e == null)
                    e = new TreeModelEvent(source, path,
                                           childIndices, children);
                ((TreeModelListener)listeners[i+1]).treeNodesRemoved(e);
            }
        }
    }


    public void addTreeModelListener(TreeModelListener l) {
        listenerList.add(TreeModelListener.class, l);
    }

    public void removeTreeModelListener(TreeModelListener l) {
        listenerList.remove(TreeModelListener.class, l);
    }

    protected EventListenerList listenerList = new EventListenerList();

    ObjectTreeNode root ;
    ReferenceQueue queue = new ReferenceQueue() ;

    /** Map classes to their declared fields.
     */
    protected Hashtable fieldTable = new Hashtable() ;

    /** Map classes to a set of fields for each superclass.
     */
    protected Hashtable allFieldMap = new Hashtable() ;

    protected Hashtable nodeMap = new Hashtable() ;
}
