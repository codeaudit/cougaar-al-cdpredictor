package com.axiom.lib.objectgraph;
import com.axiom.lib.util.* ;
import com.axiom.pspace.search.* ;
import java.util.* ;
import java.lang.reflect.* ;

public class ObjectTreeNode extends DefaultGraphNode {
    public ObjectTreeNode( Object o ) {
        this.o = o ;
    }

    public ObjectTreeNode( ObjectTreeNode parent, Object o, Field f ) {
        this( o ) ;
        setParent( parent ) ;
        this.field = f ; 
    }

    public Field getField() { return field ; }

    /** All nodes have equal cost.
     */
    public int compareTo( Object o ) {
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
        else if ( field == null && getParent() != null ) {
            // I must be an array component
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

    Object o ;

    Field field ;
    
    public int hashCode() { return System.identityHashCode( this ) ; }

}
