package com.axiom.lib.objectgraph ;
import com.axiom.lib.util.* ;
import com.axiom.pspace.search.* ;
import java.util.* ;
import java.lang.reflect.* ;
import java.lang.ref.* ;

public class WeakRefNode extends DefaultGraphNode {
    public WeakRefNode( Object o, ReferenceQueue queue ) {
        //if ( o == null ) {
        //    throw new IllegalArgumentException() ;
        //}
        if ( o != null ) {
            ref = new WeakReference( o, queue ) ;
        }
    }

    public WeakRefNode( ObjectNode parent, Object o, Field f, ReferenceQueue q ) {
        this( o, q ) ;
        setParent( parent ) ;
        this.field = f ;
    }

    /** All nodes have equal cost.
     */
    public int compareTo( Object o ) {
        //GraphNode gn = ( GraphNode ) o ;
        //if ( gn.getDepth() < getDepth() ) {
        //    return -1 ;
        //}
        //else if ( gn.getDepth() > getDepth() ) {
        //    return 1 ;
        //}
        //else
        return 0 ;
    }

    public boolean equals( Object o ) {
        if ( !( o instanceof WeakRefNode ) ) return false ;
        return isIdentical( ( GraphNode ) o ) ;
    }

    public boolean isIdentical( GraphNode g ) {
        WeakRefNode on = ( WeakRefNode ) g ;
        return on.getObject() == getObject() ;
    }

    public Object getObject() {
        if ( ref != null ) {
            return ref.get() ;
        }
        else
            return null ;
    }

    /** This hashCode impl. is not good for searching or identification and
     *  is implemented to satisfy the GraphNode interface.
     */
    public int hashCode() { return System.identityHashCode( this ) ; }

    public Field getField() { return field ; }

    WeakReference ref ;
    Field field ;
}
