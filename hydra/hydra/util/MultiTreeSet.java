/*
 * MultiTreeSet.java
 *
 * Created on September 25, 2001, 3:23 PM
 */

package org.hydra.util;
import java.util.* ;

/**
 *
 * @author  wpeng
 * @version 
 */
public class MultiTreeSet extends MultiSet {

    /** Creates new MultiTreeSet */
    public MultiTreeSet() {
        super( new TreeMap() ) ;
    }
    
    public MultiTreeSet( Comparator c ) {
        super( new TreeMap( c ) ) ;
    }
    
    public Object firstKey() { return ( ( TreeMap ) map ).firstKey() ; }
    
    public Object lastKey() { return ( ( TreeMap ) map ).lastKey() ; }
    
    public Iterator subMap( Object fromKey, Object toKey ) {
        return new MultiSetIterator( ( ( TreeMap ) map ).subMap( fromKey, toKey ).values().iterator() );
    }

    public static void main( String[] argv ) {
        
        MultiTreeSet set = new MultiTreeSet();
        
        set.put( "Fruits", "Apples" );
        set.put( "Fruits", "Oranges" ) ;
        set.put( "Fruits", "Pears" ) ;
        set.put( "Fruits", "Grapes" ) ;
        set.put( "Fruits", "Oranges" ) ;
        set.put( "Vegetables", "Carrots" ) ;
        set.put( "Vegetables", "Onions" );
        set.put( "Vegetables", "Green beans" ) ;
        set.put( "Grains", "Sorghum" ) ;
        set.put( "Spices", "Cinnamon" ) ;
        set.put( "Spices", "Nutmeg" ) ;
        
        Object[] fruits = set.getObjects( "Fruits" ) ;
        
        Object[] grains = set.getObjects( "Grains" ) ;
        
        System.out.println( "Fruits: " ) ;
        for (int i=0;i<fruits.length;i++) {
            System.out.println( fruits[i] + " " ) ;
        }

        System.out.println( "Enumerating all elements: " ) ;
        for ( Iterator e = set.elements();e.hasNext();) {
            Object o = e.next() ;
            System.out.println( o ) ;
        }

        System.out.println("Done enumeration...\n" ) ;

        System.out.println("\n Testing exception.") ;
        try {
        for ( Iterator e = set.elements();e.hasNext();) {
            Object o = e.next() ;
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

        set.removeObjects( "Vegetables" ) ;
        fruits = set.removeObjects( "Fruits" ) ;
        
        
        MultiTreeSet set2 = new MultiTreeSet() ;
        for (int i=0;i<10;i++) {
            for (int j=0;j<5;j++) { 
                set2.put( new Integer(i), new Integer(j) ) ;
            }
        }
        
        System.out.println( "\nSubMap Test" ) ;
        for ( Iterator iter = set2.subMap( new Integer( 4 ) , new Integer( 8 ) ); iter.hasNext(); ) {
            System.out.println(  iter.next()  ) ;  
        }
    }
    
}
