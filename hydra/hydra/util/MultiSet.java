package org.hydra.util;
import java.util.* ;

/**
 *  A container which wraps any Map so that it can assume multiple objects with the identical key.
 */
public class MultiSet implements java.io.Serializable {
    
    protected static class MultiSetIterator implements Iterator {

        MultiSetIterator( Iterator iter ) {
            e = iter  ;
            if ( !e.hasNext() ) {
                hasMore = false ;
                return ;
            }

            Object temp = e.next() ;
            if ( temp instanceof InternalSet ) {
                iset = ( InternalSet ) temp ;
                if ( iset.v.size() > 0 )
                    current = iset.v.get(0) ;
                else
                    current = null ;
            }
            else
                current = temp ;
        }

        public boolean hasNext() {
            return hasMore ;
        }
        
        public java.lang.Object next() {
            if ( !hasMore )
                throw new NoSuchElementException() ;

            Object result = current ;
            if ( iset != null && index < iset.v.size() - 1 )
                current = iset.v.get(++index) ;
            else if ( !e.hasNext() ) {
                hasMore = false ;
            }
            else {
                Object temp = e.next() ;
                if ( temp instanceof InternalSet ) {
                    iset = ( InternalSet ) temp ;
                    index = 0 ;
                    current = iset.v.get(0 ) ;
                }
                else {
                    iset = null ;
                    current = temp ;
                }
            }
            return result ;
        }
        
        public void remove() {
            throw new  UnsupportedOperationException( "Remove not implemented from MultiSets through iterator." ) ;
        }
        
        boolean hasMore = true ;
        int index = 0 ;
        InternalSet iset ;
        Object current = null ;
        Iterator e ;
    }

    /**
     *  Container class to insure that no "external"
     *  object will be of the same type as InternalSet.  HashSet
     *  determines whether one or more objects map to a given key
     *  by determining whether or not the object is an internal
     *  set.
     */
    private static class InternalSet implements java.io.Serializable {
        ArrayList v = new ArrayList() ;

        static final long serialVersionUID = 4583832860997408984L ;
    }

    protected MultiSet(Map map)  {
        this.map = map ;
    }
    
    public boolean isEmpty() {
        return ( count == 0 ) ;   
    }
    
    public boolean containsKey( Object key ) {
        return map.containsKey( key ) ;
    }
    
    public boolean containsValue( Object o ) {
        Iterator iter = elements() ;
        while ( iter.hasNext() ) {
            Object o1 = iter.next() ;
            if ( o1 == o ) {
               return true ;   
            }
        }
        return false ;
    }
        
    public void clear() {
        count = 0 ;
        map.clear(); 
    }

    protected Object[] setToArray( InternalSet s ) {
        Object[] o = new Object[ s.v.size()] ;
        for ( int i=0;i<s.v.size();i++) {
            o[i] = s.v.get(i) ;
        }
        
        return o ;
    }

    public Object[] getObjects( Object key ) {
        Object o = map.get( key ) ;
        
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

    public Iterator elements() {
        return new MultiSetIterator( map.values().iterator() ) ;
    }
    
    public Iterator keys() {
        return map.keySet().iterator() ;
    }
    
    public int size() { return count ; }
    
    /** Returns the first in a set of objects to which <code>key</code>
     * maps.
     */
    public Object get( Object key ) {
        Object o = map.get( key ) ;
        
        if ( o instanceof InternalSet ) {
            InternalSet set = ( InternalSet ) o ;
            if ( set.v.size() > 0 ) {
                return set.v.get(0) ;   
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
    public Object put( Object key, Object obj ) {
        if ( key == null || obj == null ) {
            throw new RuntimeException( "Null parameter: key" ) ;
        }

        Object o = map.get( key ) ;
        Object result = null ;
        
        //if ( o == obj )  // Already exists
        //   return ;

        if ( o == null ) {
            map.put( key, obj ) ;
        }
        else if ( o instanceof InternalSet ) {
            // Check to see if obj is already in o
            InternalSet s = ( InternalSet ) o ;
            //for (int i=0;i<s.v.size();i++) {
            //   if ( s.v.get(i) == obj ) {
            //       return ;
            //    }
            //}
            result = s.v.get(0) ;
            s.v.add( obj ) ;
        }
        else {
            InternalSet s = new InternalSet() ;
            s.v.add( o ) ;
            s.v.add( obj ) ;
            result = o ;
            map.put( key, s ) ;
        }
        count++ ;
        return result ;
    }
        
    public Object[] removeObjects( Object key ) {
        Object o = map.remove( key ) ;
        
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
        Object obj = map.get(key ) ;
        if ( obj == null )
            return false ;

        if ( obj instanceof InternalSet )
            return ( ( InternalSet ) obj ).v.indexOf(o) != -1 ;
        else return o == obj ;
    }
        
    /**
     *  Get ith element associated with <code>key</code>. Returns null i
     *  no element associated with key or indexed by i is found.
     */
    public Object get( Object key, int i ) {
        Object o = map.get( key ) ;

        if ( o == null ) {
           return null ;
        }
        
        // Return ith element of the object
        if ( o instanceof InternalSet ) {
            InternalSet set = ( InternalSet ) o ;
            if ( set.v.size() > 0 && i < set.v.size() ) {
                return set.v.get(i) ;   
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
        Object o = map.get( key ) ;

        if ( o == obj ) { 
            map.remove( key ) ;
            count-- ;
            return true ;
        }

        if ( o instanceof InternalSet ) {
            InternalSet s = ( InternalSet ) o ;
            for (int i=0;i<s.v.size();i++) {
                if ( s.v.get(i) == obj ) {
                    if ( s.v.size() == 1 ) {
                        map.remove( key ) ;  // Just remove the entire entry
                    }
                    else {
                        s.v.remove(i) ;
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
        Object o = map.get( key ) ;

        if ( o.equals( obj ) ) { 
            map.remove( key ) ;
            count-- ;
            return o ;
        }

        if ( o instanceof InternalSet ) {
            InternalSet s = ( InternalSet ) o ;
            for (int i=0;i<s.v.size();i++) {
                if ( s.v.get(i).equals( obj ) ) {
                    Object res = s.v.get(i) ;
                    if ( s.v.size() == 1 ) {
                        map.remove( key ) ;  // Just remove the entire entry
                    }
                    else {
                        s.v.remove(i) ;
                    }
                    count--;
                    return res ;
                }
            }
        }

        return null ;
    }

    protected Map map ;
    protected int count ;
    static final long serialVersionUID = 4806764605121999089L;
}