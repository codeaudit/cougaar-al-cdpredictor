package com.axiom.lib.util ;
import java.util.* ;

/**
 *  Implements a typed namespace.
 */
public class TypedNamespace implements java.io.Serializable {

    public static abstract class TypedRecord implements java.io.Serializable {

        TypedRecord( String name, Class c ) {
            this.name = name ; this.c = c ;
        }

        public int hashCode() {
            return name.hashCode() ;
        }

        abstract Object getObject() ;

        abstract void setObject( Object o ) ;

        abstract boolean isTransient() ;

        Class getType() { return c ; }

        String getName() { return name ; }

        String name ;
        Class c ;

        static final long serialVersionUID = -1181157329321886308L;
    }

    public static class PersistantTypedRecord extends TypedRecord {
        PersistantTypedRecord( String name, Class c, Object o ) {
            super( name, c ) ;
            this.o = o ;
        }

        Object getObject() { return o ; }

        void setObject( Object o ) { this.o = o ; }

        boolean isTransient() { return false ; }

        Object o ;

        static final long serialVersionUID = 7562289782194027209L;
    }

    public static class TransientTypedRecord extends TypedRecord {

        TransientTypedRecord( String name, Class c, Object o ) {
            super( name, c ) ;
            this.o = o ;
        }

        Object getObject() { return o ; }

        void setObject( Object o ) { this.o = o ; }

        boolean isTransient() { return true ; }
        transient Object o ;

        static final long serialVersionUID =  -2309958254013931188L; 
    }

    /**
     *  Add a typed object <code>o</code> with name <code>name</code> to the
     *  namespace as a new slot.  The type of the object is the class as
     *  reported by <code>getClass<code>. If a slot with the name already exists,
     *  it is replaced.
     *
     *  @param isTransient  If true, the object will not be serialized, although a
     *         slot with the name and type will remain registered.
     */
    public void add( String name, Object o, boolean isTransient ) {
        if ( o == null )
            add( name, Object.class, null, isTransient ) ;
        else
            add( name, o.getClass(), o, isTransient ) ;
    }

    public void add( String name, Object o ) {
        add( name, o, false ) ;
    }

    public void add( String name, Class c, Object o, boolean isTransient ) {
        if ( c== null ) {
            throw new IllegalArgumentException( "Class parameter must not be null." ) ;
        }
        if ( o != null && !c.isAssignableFrom( o.getClass() ) ) {
            throw new IllegalArgumentException( "Class " + c.getName() + " must be assignable from " +
                                                o.getClass().getName() ) ;
        }

        TypedRecord record ;
        if ( isTransient ) {
            record = new TransientTypedRecord( name, c, o ) ;
        }
        else {
            record = new PersistantTypedRecord( name, c, o ) ;
        }
        try {
        recordTable.put( name, record ) ;
        }
        catch ( NullPointerException e ) {
            throw new IllegalArgumentException( "Name must be non-null." ) ;
        }
    }

    public void add( String name, Class c, Object o ) {
        add( name, c, o, false ) ;
    }

    public void set( String name, Object o ) {
        TypedRecord record = ( TypedRecord ) recordTable.get( name ) ;
        if ( record != null && ( o == null || record.getType().isAssignableFrom( o.getClass() ) ) )
            record.setObject( o );
    }

    public Object get( String name ) {
        TypedRecord record = ( TypedRecord ) recordTable.get( name ) ;
        if ( record == null ) {
            throw new RuntimeException( "No such name: " + name ) ;
        }
        return record.getObject() ;
    }

    public Class getType( String name ) {
        TypedRecord record = ( TypedRecord ) recordTable.get( name ) ;
        if ( record == null ) {
            return null ;
        }
        return record.getType() ;
    }

    public Boolean isTransient( String name ) {
        TypedRecord record = ( TypedRecord ) recordTable.get( name ) ;
        if ( record == null ) {
            return null ;
        }
        return new Boolean( record.isTransient() ) ;
    }

    public void add( String name, Class c ) {
        add( name, c, null, false ) ;
    }

    public boolean remove( String name ) {
        Object o = recordTable.remove( name ) ;
        if ( o == null )
            return false ;
        return true ;
    }

    public Enumeration getKeys() {
        return recordTable.keys() ;
    }

    protected Hashtable recordTable = new Hashtable() ;

    public static void main( String[] args ) {
        TypedNamespace tn = new TypedNamespace() ;
        Vector v1 = new Vector() ;
        Vector v2 = new Vector() ;
        tn.add( "Moose", v1 );
        tn.set( "Moose", v2 );

        try {
            tn.add( "Mice", Vector.class, "Moron" );
        }
        catch ( Exception e ) {
            System.out.println( e ) ;
            e.printStackTrace();
        }

        Object o = tn.get( "Moose" ) ;
        System.out.println( "Object " + o ) ;
    }

    static final long serialVersionUID = 3460120917363945043L;
}