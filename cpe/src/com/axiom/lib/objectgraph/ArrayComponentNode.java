package com.axiom.lib.objectgraph;
import java.lang.reflect.* ;

public class ArrayComponentNode extends ObjectTreeNode {

    public ArrayComponentNode( ObjectTreeNode parent, Object o, int index ) {
        super( parent, o, null ) ;
        this.index = index ;
    }

    public String toString() {
        StringBuffer result = new StringBuffer() ;
        result.append('[').append(index).append("]=") ;

        ObjectTreeNode par = ( ObjectTreeNode ) getParent() ;
        Object po = par.getObject() ;

        if ( po.getClass().getComponentType().isPrimitive() ) {
            result.append( o.toString() ) ;
        }
        else if ( o != null && o.getClass().isArray() ) {
            outputArrayString( o.getClass(), Array.getLength( o ), result ) ;
        }
        else if ( o != null ) {
            result.append( o.getClass().getName() ) ;
        }
        else {
            result.append( "null" ) ;
        }

        return result.toString() ;
    }

    public int getIndex() { return index ; }

    int index = -1 ;
}
