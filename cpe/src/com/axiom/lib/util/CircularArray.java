package com.axiom.lib.util ;
import java.util.* ;

/**
 * Implements a circular array of fixed size.
 */
public class CircularArray implements java.io.Serializable {

    public CircularArray( int size ) {
        if ( size <= 0 ) throw new RuntimeException( "Size must be >= 0" ) ;
        buf = new Object[ size ] ;
    }

    public int getCapacity() { return buf.length ; }

    public int getSize() { return count ; }

    public void add( Object object ) {
        if (count>0) {
            head = head + 1 ;
            head %= buf.length ;
        }
        buf[head]= object ;
        if ( count < buf.length )
            count++;
        else
            tail = (head + 1) % buf.length ;
    }

    public Object at( int i ) {
        if ( i >= count || i < 0 ) throw new ArrayIndexOutOfBoundsException(i) ;

        return buf[ ( tail + i ) % buf.length ] ;
    }

    public Object getFirst() {
        if ( count == 0 ) throw new NoSuchElementException() ;
        return buf[ head ];
    }

    public Object getLast() {
        if ( count == 0 ) throw new NoSuchElementException() ;
        return buf[ tail ] ;
    }

    public Object removeLast()
    {
        int index;
        if ( count == 0 )
            throw new NoSuchElementException() ;

        index = tail ;
        if (count > 1) {
            tail += 1 ;
            tail %= buf.length ;
        }
        count--;
        return buf[index] ;
    }

    public String toString() {
        StringBuffer sbuf = new StringBuffer() ;
        sbuf.append( '[' ) ;
        for (int i=0;i<count;i++) {
            int index = ( tail + i ) % buf.length ;
            sbuf.append( buf[index] );
            if ( i < count - 1 ) sbuf.append(',') ;
        }
        sbuf.append( ']' ) ;
        return sbuf.toString() ;
    }

    int count = 0 ;
    int tail = 0 , head = 0 ;
    Object[] buf ;

    public static void main( String[] args ) {

        System.out.println("\nTesting circular buffer... " ) ;
        CircularArray carr = new CircularArray( 5 ) ;
        carr.add( "The" ) ;
        carr.add( "quick" ) ;
        carr.add( "brown" ) ;

        System.out.println( "Contents: " + carr ) ;

        carr.add( "fox" ) ;
        carr.add( "jumped" ) ;
        carr.add( "over" ) ;
        carr.add( "the" ) ;
        carr.add( "lazy" ) ;
        carr.add( "dog" ) ;
        
        System.out.println( "Contents: \n" + carr ) ;

        System.out.println( "Indexed addressing: " ) ;
        for (int i=0;i<carr.getSize()+1;i++) {
            try {
            System.out.println( "i: " + i + " " + carr.at(i) ) ;
            }
            catch ( Throwable e ) {
                e.printStackTrace() ;
            }
        }

        System.out.println( "\nEmptying buffer: " ) ;
        for (int i=0;i<6;i++) {
            try {
                String s = ( String ) carr.removeLast() ;
                System.out.println(s) ;
            }
            catch ( Throwable e ) {
                e.printStackTrace() ;
            }
        }

    }
}
