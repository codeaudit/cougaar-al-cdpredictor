package com.axiom.lib.util ;
import java.util.* ;

public class SetUtils {

    /**
     *  Union using object identity (==) comparator.  Creates lots of garbage,
     *  but this shouldn't bother HotSpot.  Ought to be fast, ~O( n + m ) time,
     *  where n and m are the number of elements in either set.  Does not guarantee
     *  that duplicates will be eliminated.
     */
    public static Vector union( Vector a, Vector b ) {
        HashSet table = new HashSet(  a.size() + b.size() ) ;
        Vector result = new Vector() ;
        for (int i=0;i<a.size();i++) {
            Object tmp = a.elementAt(i) ;
            result.addElement( tmp ) ;
            table.put( new Integer( System.identityHashCode( tmp ) ), tmp ) ;
        }
        for (int i=0;i<b.size();i++) {
            Object tmp = b.elementAt(i) ;
            Object[] tmp2 = table.getObjects( new Integer( System.identityHashCode( tmp ) ) ) ;
            if ( tmp2 == null || !find( tmp2, tmp) ) {
                table.put( new Integer( System.identityHashCode( tmp ) ), tmp ) ; // Add this to the list
                result.addElement( tmp ) ;
            }
        }
        return result ;
    }

    /** Computes difference using identity comparator. */
    public static Vector difference( Vector a, Vector b ) {
        HashSet table = new HashSet( (int) ( a.size() + b.size() ) ) ;
        Vector result = new Vector() ;
        for (int i=0;i<b.size();i++) {
            Object tmp = b.elementAt(i) ;
            table.put( new Integer( System.identityHashCode( tmp ) ), tmp ) ;
        }
        for (int i=0;i<a.size();i++) {
            Object tmp = a.elementAt(i) ;
            Object[] tmp2 = table.getObjects( new Integer( System.identityHashCode( tmp ) ) ) ;
            if ( tmp2 == null || !find( tmp2, tmp ) )
                result.addElement( tmp ) ;
        }
        return result ;
    }

    protected static boolean find( Object[] o, Object t ) {
        for (int i=0;i<o.length;i++) {
            if ( o[i] == t )
                return true ;
        }
        return false ;
    }

    /** Computes intersection using identity comparator. */
    public static Vector intersection( Vector a, Vector b ) {
        HashSet table = new HashSet( a.size() + b.size() ) ;
        Vector result = new Vector() ;
        for (int i=0;i<a.size();i++) {
            Object tmp = a.elementAt(i) ;
            table.put( new Integer( System.identityHashCode( tmp ) ), tmp ) ;
        }
        for (int i=0;i<b.size();i++) {
            Object tmp = b.elementAt(i) ;
            Object[] tmp2 = table.getObjects( new Integer( System.identityHashCode( tmp ) ) ) ;
            if ( tmp2 != null && find( tmp2, tmp ) )
                result.addElement( tmp ) ;
        }
        return result ;
    }

    public static void main( String[] args ) {
        Vector a = new Vector() ;
        a.addElement( "Moose" ) ;
        a.addElement( "Meese" ) ;
        a.addElement( "Mice" ) ;

        Vector b = new Vector() ;
        b.addElement( "Moose" ) ;
        b.addElement( "Meese" ) ;
        b.addElement( "Apple" ) ;
        b.addElement( "Felt" ) ;

        Vector c = intersection( a, b ) ;
        Vector d = union( a, b ) ;
        Vector e = difference( a, b ) ;

        System.out.println( "Intersection: " + c ) ;
        System.out.println( "Union: " + d ) ;
        System.out.println( "Difference: " + e ) ;

    }

}