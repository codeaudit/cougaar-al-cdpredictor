package com.axiom.lib.util ;
import java.util.* ;

/**
 *  A HashSet is a Container which maps keys to one or more objects.   Unlike the
 *  standard Hashtable and HashSet, it allows multiple objects to be associated with
 *  a single key.
 */
public class HashSet implements java.io.Serializable {
    static class HashSetEnumeration implements Enumeration {

        HashSetEnumeration( HashSet set ) {
            e = set.table.elements() ;
            if ( !e.hasMoreElements() ) {
                hasMore = false ;
                return ;
            }

            Object temp = e.nextElement() ;
            if ( temp instanceof InternalSet ) {
                iset = ( InternalSet ) temp ;
                if ( iset.v.size() > 0 )
                    current = iset.v.elementAt(0) ;
                else
                    current = null ;
            }
            else
                current = temp ;
        }

        public boolean hasMoreElements() {
            return hasMore ;
        }

        public Object nextElement() {
            if ( !hasMore )
                throw new NoSuchElementException() ;

            Object result = current ;
            if ( iset != null && index < iset.v.size() - 1 )
                current = iset.v.elementAt(index++) ;
            else if ( !e.hasMoreElements() ) {
                hasMore = false ;
            }
            else {
                Object temp = e.nextElement() ;
                if ( temp instanceof InternalSet ) {
                    iset = ( InternalSet ) temp ;
                    index = 1 ;
                    current = iset.v.elementAt(0 ) ;
                }
                else {
                    iset = null ;
                    current = temp ;
                }
            }
            return result ;
        }

        boolean hasMore = true ;
        int index = 0 ;
        InternalSet iset ;
        Object current = null ;
        Enumeration e ;
    }

    /**
     *  Container class to insure that no "external"
     *  object will be of the same type as InternalSet.  HashSet
     *  determines whether one or more objects map to a given key
     *  by determining whether or not the object is an internal
     *  set.
     */
    protected static class InternalSet implements java.io.Serializable {
        Vector v = new Vector() ;

        static final long serialVersionUID = 4583832860997408984L ;
    }

    public HashSet() {
       this( 15 ) ;
    }
    
    public HashSet( int minSize ) {
        table = new Hashtable( minSize ) ;
    }

    public void clear() {
        count = 0 ;
        table.clear(); 
    }

    protected Object[] setToArray( InternalSet s ) {
        Object[] o = new Object[ s.v.size()] ;
        for ( int i=0;i<s.v.size();i++) {
            o[i] = s.v.elementAt(i) ;
        }
        
        return o ;
    }

    public Object[] getObjects( Object key ) {
        Object o = table.get( key ) ;
        
        if ( o == null ) {
            Object[] result = {};
            return result ;
        }
        else if ( o instanceof InternalSet ) {
            return setToArray( ( InternalSet ) o ) ;
        }
        else {
            Object[] result = { o };
            return result ;
        }             
    }

    public Enumeration elements() {
        return new HashSetEnumeration( this ) ;
    }
    
    public Enumeration keys() {
        return table.keys() ;
    }
    
    public int size() { return count ; }
    
    public Object get( Object key ) {
        Object o = table.get( key ) ;
        
        if ( o instanceof InternalSet ) {
            InternalSet set = ( InternalSet ) o ;
            if ( set.v.size() > 0 ) {
                return set.v.elementAt(0) ;   
            }
            else
                return null ;
        }
        else {
            return o ;
        }        
    }
    
    /**
     *  Associates object with key.  Does not check for duplicates.
     *  @param key  A non-null key object.
     *  @param obj  A non null object to associate with the key.
     */
    public void put( Object key, Object obj ) {
        if ( key == null || obj == null ) {
            throw new RuntimeException( "Null parameter: key" ) ;
        }

        Object o = table.get( key ) ;

        //if ( o == obj )  // Already exists
        //   return ;

        if ( o == null ) {
            table.put( key, obj ) ;
        }
        else if ( o instanceof InternalSet ) {
            // Check to see if obj is already in o
            InternalSet s = ( InternalSet ) o ;
            //for (int i=0;i<s.v.size();i++) {
            //   if ( s.v.elementAt(i) == obj ) {
            //       return ;
            //    }
            //}
            s.v.addElement( obj ) ;
        }
        else {
            InternalSet s = new InternalSet() ;
            s.v.addElement( o ) ;
            s.v.addElement( obj ) ;
            table.put( key, s ) ;
        }
        count++ ;
    }
    
    /**
     *  Returns an array of objects or null if no objects associated with
     *  <code>key</code> exist.
     */
    public Object[] remove( Object key ) {
        Object o = table.remove( key ) ;
        
        Object[] result ;
        
        if ( o != null ) {        
            if ( o instanceof InternalSet ) {
                InternalSet s = ( InternalSet ) o ;
                result = setToArray( s ) ;
                count -= s.v.size() ;
            }
            else {
                result = new Object[1] ;
                result[0] = o ;
                count --;   
            }
            
            return result ;
        }

        return null ;
    }

    public boolean isMemberOf( Object key, Object o ) {
        Object obj = table.get(key ) ;
        if ( obj == null )
            return false ;

        if ( obj instanceof InternalSet )
            return ( ( InternalSet ) obj ).v.indexOf(o) != -1 ;
        else return o == obj ;
    }
    
    /**
     *  Remove ith object associated with key.  Not yet implemented.
     */
    public boolean remove( Object key, int i ) {
        throw new RuntimeException( "Not supported." ) ;
        // return false ;
    }
    
    /**
     *  Get ith element associated with <code>key</code>. Returns null i
     *  no element associated with key or indexed by i is found.
     */
    public Object get( Object key, int i ) {
        Object o = table.get( key ) ;

        if ( o == null ) {
           return null ;
        }
        
        // Return ith element of the object
        if ( o instanceof InternalSet ) {
            InternalSet set = ( InternalSet ) o ;
            if ( set.v.size() > 0 && i < set.v.size() ) {
                return set.v.elementAt(i) ;   
            }
            else
                return null ;
        }
        else {
            if ( i == 0 ) 
                return o ;
            else
                return null ;
        }
    }
    
    /**
     *  Removes the first instance of obj corresponding to key.
     */
    public boolean remove( Object key, Object obj ) {
        Object o = table.get( key ) ;

        if ( o == obj ) { 
            table.remove( key ) ;
            count-- ;
            return true ;
        }

        if ( o instanceof InternalSet ) {
            InternalSet s = ( InternalSet ) o ;
            for (int i=0;i<s.v.size();i++) {
                if ( s.v.elementAt(i) == obj ) {
                    if ( s.v.size() == 1 ) {
                        table.remove( key ) ;  // Just remove the entire entry
                    }
                    else {
                        s.v.removeElementAt(i) ;
                    }
                    count--;
                    return true ;
                }
            }
        }
        
        return false ;
    }
   
    /**
     *  Remove first object with key <code>key</code> and for which
     *  <code>equals</code> method returns <code>true</code>.
     *
     *  @param key Key of object to be removed.
     *  @param obj Object to compare with.
     */
    public Object removeEquals( Object key, Object obj ) {
        Object o = table.get( key ) ;

        if ( o.equals( obj ) ) { 
            table.remove( key ) ;
            count-- ;
            return o ;
        }

        if ( o instanceof InternalSet ) {
            InternalSet s = ( InternalSet ) o ;
            for (int i=0;i<s.v.size();i++) {
                if ( s.v.elementAt(i).equals( obj ) ) {
                    Object res = s.v.elementAt(i) ;
                    if ( s.v.size() == 1 ) {
                        table.remove( key ) ;  // Just remove the entire entry
                    }
                    else {
                        s.v.removeElementAt(i) ;
                    }
                    count--;
                    return res ;
                }
            }
        }

        return null ;
    }
    
    public static void main( String[] argv ) {
        
        HashSet set = new HashSet();
        
        set.put( "Fruits", "Apples" );
        set.put( "Fruits", "Oranges" ) ;
        set.put( "Fruits", "Pears" ) ;
        set.put( "Fruits", "Grapes" ) ;
        set.put( "Fruits", "Oranges" ) ;
        set.put( "Vegetables", "Carrots" ) ;
        set.put( "Vegetables", "Onions" );
        set.put( "Vegetables", "Green beans" ) ;
        set.put( "Grains", "Sorghum" ) ;
        
        Object[] fruits = set.getObjects( "Fruits" ) ;
        
        Object[] grains = set.getObjects( "Grains" ) ;
        
        System.out.println( "Fruits: " ) ;
        for (int i=0;i<fruits.length;i++) {
            System.out.println( fruits[i] + " " ) ;
        }

        System.out.println( "Enumerating all elements: " ) ;
        for ( Enumeration e = set.elements();e.hasMoreElements();) {
            Object o = e.nextElement() ;
            System.out.println( o ) ;
        }

        System.out.println("Done enumeration...\n" ) ;

        System.out.println("\n Testing exception.") ;
        try {
        for ( Enumeration e = set.elements();;) {
            Object o = e.nextElement() ;
            System.out.println( o ) ;
        }
        }
        catch ( Exception e ) {
            System.out.println( e ) ;
            e.printStackTrace() ;
        }
        
        System.out.println( set.get( "Fruits", 3 ) ) ;
        
        System.out.println( set.get( "Grains", 0 ) ) ;
        
        set.remove( "Fruits", "Oranges" ) ;

        set.remove( "Vegetables" ) ;

        fruits = set.remove( "Fruits" ) ;
    }

    protected Hashtable table ;
    
    protected int count ;

    static final long serialVersionUID = 4806764605121999089L;
}