package com.axiom.lib.util ;

import java.util.LinkedList ;
import java.util.Comparator ;
import java.util.AbstractMap ;
import java.util.TreeMap ;
import java.util.Collection ;
import java.util.Iterator ;

/**
 *  This uses TreeMap but extends the map to accept multiple objects with the
 *  same key.  This utility relies on the JDK 1.2 Collections, implementing the
 *  AbstractMap interface.
 */
public class OrderedMap extends AbstractMap
                        implements java.io.Serializable {

    private static class MapComparator implements Comparator, java.io.Serializable {
        public MapComparator( Comparator c ) {
            comparator = c;
        }

        public int compare( Object o1, Object o2 ) {
            if ( o1 instanceof OrderedMap.MapEntry ) {
                o1 = (( OrderedMap.MapEntry ) o1).l.getFirst() ;
            }
        
            if ( o2 instanceof OrderedMap.MapEntry ) {
                o2 = (( OrderedMap.MapEntry ) o2).l.getFirst() ;
            }
            return comparator.compare( o1, o2 ) ;
        }

        Comparator comparator ;

        static final long serialVersionUID = -5960733844760958275L;
    }

    public static class MapEntry implements java.io.Serializable {
        MapEntry() {
        }

        public LinkedList l = new LinkedList() ;

        static final long serialVersionUID = 7456236069598069239L ;
    }

    public OrderedMap( Comparator comparator ) {
        map = new TreeMap( new MapComparator( comparator ) ) ;
    }

    public void clear() {
        map.clear() ;
        count = 0 ;
    }

    public int size() {
        return count ;
    }

    /**
     *  A list of objects with key <code>key</code>.
     *
     *  @return an object, or a MapEntry, or null.
     */
    public Object get( Object key ) {
        return map.get( key );
    }
    
    public Object remove( Object key ) {
        Object o = map.remove( key ) ;
        
        if ( o != null ) {
            if ( o instanceof MapEntry ) {
                count -= ( (MapEntry) o ).l.size() ;
            }
            else 
                count -- ;
        }
        return o ;
    }

    public Object removeFirst() {
        Object o = map.firstKey() ;
        Object obj = map.get(o) ;
        if ( obj instanceof MapEntry ) {
            MapEntry entry = (MapEntry) obj ;
            Object result = entry.l.removeFirst() ;
            if ( entry.l.size() == 0 ) {
                map.remove( o ) ;
            }
            count-- ;
            return result ;
        }
        else {
            count-- ;
            map.remove( o ) ;
            return obj ;
        }
    }

    public boolean remove( Object key, Object value ) {
        Object o = map.get( key ) ;
        if ( o == null ) {
           return false ;   
        }
        
        if ( o instanceof MapEntry ) {
            MapEntry m = ( MapEntry ) o ;
            if ( m.l.size() > 1 ) {
                boolean rr = m.l.remove( value ) ;
                if( rr ) {
                    count-- ;
                }
                if ( m.l.size() == 0 ) {
                    map.remove( key ) ;
                }
                return true ;
            }
            else if ( m.l.getFirst() == value ) {
                map.remove( key ) ;
                count-- ;
                return true ;
            }
            else
                return false ; // Not found!
        }
        else if ( o == value ) {
            // Remove the whole key
            map.remove( key ) ;
            count-- ;
            return true ;
        }

        return false ;
    }
    
    /**
     *  Put an object with key and value into the map.
     */
    public Object put( Object key, Object value ) {
        Object o = map.get( key ) ;
        count++; 
        if ( o == null ) {
           map.put( key, value ) ;
           return null ;
        }
        else if ( o instanceof MapEntry ) {
           (( MapEntry) o ).l.addLast( value ) ;
           return o ;
        }
        else {
           // This appears to be a bug, since putting a new object
           // should remove the old one and return it.  Instead
           // we have to add it.
           map.remove( key ) ;
           MapEntry n = new MapEntry() ;
           n.l.addLast( o );
           n.l.addLast( value ) ;
           Object obj = map.put( key, n ) ;
           return n ;
        }
    }
    
    public String toString() {
        StringBuffer result = new StringBuffer() ;
        Collection c = entrySet() ;
        
        Iterator iter = c.iterator() ;
        
        while ( iter.hasNext() ) {
           java.util.Map.Entry mentry = ( java.util.Map.Entry ) iter.next() ;
           Object obj = mentry.getValue() ;
           if ( obj instanceof MapEntry ) {
              MapEntry m = ( MapEntry ) obj ;
              result.append( mentry.getKey() ).append( "=" ) ;
              result.append( m.l.toString() ) ;
           }
           else
              result.append( mentry.toString() ) ;
           if ( iter.hasNext() ) {
               result.append( "," ) ;
           }
        }
        
        return result.toString() ;
    }

    public java.util.Set entrySet() {
        return map.entrySet() ;
    }

    public static void main( String[] argv ) {
        class IntegerComparator implements Comparator {
            public int compare( Object o1, Object o2 ) {
               Integer i1 = ( Integer ) o1 ;
               Integer i2 = ( Integer ) o2 ;
               return i1.intValue() - i2.intValue() ;
            }
        }

       OrderedMap map = new OrderedMap( new IntegerComparator() ) ;
       
       map.put( new Integer( 2 ) , "two" ) ;
       map.put( new Integer( 0 ) , "zero" ) ;
       map.put( new Integer( 1 ) , "one" ) ;
       map.put( new Integer( 1 ) , "ONE" );
       map.put( new Integer( 2 ) , "two" ) ;
       map.put( new Integer( 3 ) , "three" ) ;

       System.out.println( "Resultant map: " + map ) ;
       System.out.println("count=" + map.size() );

        map.remove( new Integer(1) ) ;
        System.out.println("Resultant map: " + map );
        System.out.println("count=" + map.size() );

        map.remove( new Integer(3) ) ;
        System.out.println("Resultant map: " + map );
        System.out.println("count=" + map.size() );
    }

    protected TreeMap map ;
    
    protected int count ;

    static final long serialVersionUID = 6860444131579894469L;
}

