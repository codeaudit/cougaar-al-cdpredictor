package com.axiom.lib.objectgraph ;
import com.axiom.lib.util.* ;
import com.axiom.pspace.search.* ;
import java.util.* ;
import java.lang.reflect.* ;

public class ObjectNode extends DefaultGraphNode {
    public ObjectNode( Object o ) {
        //if ( o == null ) {
        //    throw new IllegalArgumentException() ;
        //}
        this.o = o ;
    }

    public ObjectNode( ObjectNode parent, Object o, Field f ) {
        this( o ) ;
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

    protected void outputArrayString( Class type, int length, StringBuffer result ) {
        String cname = type.getName() ;
        int indx = cname.lastIndexOf( '[' ) ;
        if ( indx == ( cname.length() - 2 ) ) {
            char c = cname.charAt(cname.length()-1) ;
            switch ( c ) {
                case 'f':
                case 'F':
                    result.append( "float" ) ;
                break ;
                case 'c' :
                case 'C' :
                    result.append( "char" ) ;
                break ;
                case 'D' :
                    result.append( "double" ) ;
                break ;
                case 'i' :
                case 'I' :
                    result.append( "int" ) ;
                break ;
                case 'L' :
                case 'l' :
                    result.append( "long" ) ;
                break ;
                default :
                    result.append( c ) ;
                break ;
            }
        }
        else {
           result.append( cname.substring( indx+2, cname.length()-1 ) ) ;
        }
        result.append('[').append( length ).append(']' ) ; 
        for (int i=1;i<indx-1;i++) {
            result.append( "[]" ) ;
        }
    }

    public String toString() {
        StringBuffer result = new StringBuffer() ;
        if ( field != null ) { // && o != null ) {
            result.append( field.getName() ) ;
            if ( !field.getType().isArray() ) {
                result.append(':').append( field.getType().getName() ) ;
            }
            result.append( "=" ) ;
            if ( field.getType().isPrimitive() ) {
                result.append( o ) ;
            }
            else if ( field.getType().isArray() ) {
                if ( o != null ) {
                    outputArrayString( field.getType(), Array.getLength( o ), result );
                }
                else {
                    result.append( "null" ) ;
                }
            }
            else if ( field.getType() == String.class && o != null ) {
                result.append( '"' ).append( o ).append( '"' ) ;
            }
            else if ( o != null ) {
                if ( o.getClass().isArray() ) {
                    outputArrayString( o.getClass(), Array.getLength( o ), result );
                }
                else {
                   result.append( o.getClass().getName() ) ;
                }
            }
            else
                result.append( "null" ) ;
        }
        else if ( o != null ) {
            result.append( o.getClass().getName() ) ;
        }
        else
            result.append( "null" ) ;
        return result.toString() ;
    }

    public boolean equals( Object o ) {
        if ( !( o instanceof ObjectNode ) ) return false ;
        return isIdentical( ( GraphNode ) o ) ;
    }

    public boolean isIdentical( GraphNode g ) {
        ObjectNode on = ( ObjectNode ) g ;
        return on.getObject() == getObject() ;
    }

    public Object getObject() { return o ; }

    public int hashCode() { return System.identityHashCode( o ) ; }

    public Field getField() { return field ; }

    Object o ;
    Field field ;
}
