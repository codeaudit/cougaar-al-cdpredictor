package com.axiom.lib.util ;

/**
 *  Object reference maintains a reference to an object o and is useful for
 *  identity tables.
 */

public class ObjectRef implements java.io.Serializable {
    public ObjectRef( Object o ) {
        this.o = o ;
    }

    public Object get() { return o ; }

    public int hashCode() { return System.identityHashCode(o) ; }

    public boolean equals( Object object ) {
        if ( !( object instanceof ObjectRef ) ) return false ;
        ObjectRef or = ( ObjectRef ) object ;
        return or.get() == get() ;
    }

    private Object o ;
}