package org.cougaar.cpe.util;

import java.util.ArrayList;

/**
 * User: wpeng
 * Date: Mar 18, 2003
 * Time: 4:36:16 PM
 */
public class PowerSetEnumeration {

    public PowerSetEnumeration(ArrayList listOfLists) {
        this.listOfLists = listOfLists;

        lower = new int[listOfLists.size()] ;  // Zero array
        upper = (int[]) lower.clone() ;
        for (int i=0;i<lower.length;i++) {
            upper[i] = ( ( ArrayList ) listOfLists.get(i) ).size() ;
        }
        for (int i=0;i<upper.length;i++)
            upper[i] = upper[i] - 1 ;
        index = ( int[] )lower.clone();
        if ( index.length >= 0 ) {
            index[0] = -1 ;
        }
    }

    public int getLinearIndex() {
        return lindex ;
    }

    public int[] getIndex() {
        return (int[]) index.clone() ;
    }

    public void getTuple( Object[] tuple ) {
        for ( int i=0;i<index.length;i++) {
            ArrayList l = (ArrayList) listOfLists.get(i) ;
            if ( l.size() > 0 ) {
                tuple[i] = l.get( index[i] ) ;
            }
        }
    }

    public Object[] getTuple() {
        Object[] result = new Object[ listOfLists.size() ] ;
        for ( int i=0;i<index.length;i++) {
            ArrayList l = (ArrayList) listOfLists.get(i) ;
            if ( l.size() > 0 ) {
                result[i] = l.get( index[i] ) ;
            }
        }

        return result ;
    }

    public boolean hasMoreElements() {
        for (int i=0;i<index.length;i++) {
            if ( index[i] < upper[i]  ) {
                return true ;
            }
        }
        return false ;
    }

    /** Returns current element and increments to next element.
     */
    public void nextElement() {
        //Object res = matrix.getArray()[lindex];

        // Throw exception if no next element exists.
        if ( index[0] > upper[0] )
           throw new java.util.NoSuchElementException() ;

        try {
            if ( index[0] < upper[0] ) {
                index[0] = index[0] + 1;
                lindex ++ ;
                return ;
            }
            else {
                index[0] = lower[0];
            }

            for (int i=1;i< index.length ;i++) {
                if ( index[i] < upper[i] ) {
                    index[i] += 1;
                    // Convert index into linear index
                    //lindex = matrix.linearIndex( index ) ;
                    return ;
                }
                else {
                    index[i] = lower[i];
                }
            }

            // This signals that there are no more elements in this enumeration
            index[0] = upper[0]+1 ;
        //return res ;
        }
        catch ( RuntimeException e ) {
            e.printStackTrace();
            throw e ;
        }
    }



    protected ArrayList listOfLists ;

    /**
     *  Linear index.
     */
    protected int lindex = -1;

    /**
     *  Array index.
     */
    protected int[] index ;

    /**
     *  Lower and upper indices of matrix being enumerated.
     */
    protected int[] lower, upper ;

    public static final void main( String[] args ) {
        ArrayList a = new ArrayList( ) ;
        a.add( "A" ); a.add( "B") ; a.add( "C" ) ;
        ArrayList b = new ArrayList( ) ;
        b.add( "1" ); b.add( "2" ) ; b.add( "3" ) ;
        //ArrayList c = new ArrayList( ) ;
        //c.add( "a" ) ;
        ArrayList d = new ArrayList( ) ;
        d.add( "i" ) ; d.add( "j" ) ; d.add( "k" ) ; d.add( "l" ) ;

        ArrayList l = new ArrayList() ;
        l.add( a ) ;
        l.add( b ) ;
        //l.add( c ) ;
        l.add( d ) ;

        PowerSetEnumeration pe = new PowerSetEnumeration( l ) ;
        while ( pe.hasMoreElements() ) {
            pe.nextElement();
            int[] index = pe.getIndex() ;
            Object[] tuple = pe.getTuple() ;
            System.out.print("{");
            for (int i = 0; i < tuple.length; i++) {
                System.out.print( tuple[i] + " ") ;
            }
            System.out.println("}");
        }

        ArrayList testArray2 = new ArrayList() ;
        for (int i=0;i<4;i++) {
            testArray2.add( new ArrayList() ) ;
        }
        PowerSetEnumeration enum2 = new PowerSetEnumeration( testArray2 ) ;

        System.out.println( enum2.hasMoreElements() );
    }

}
