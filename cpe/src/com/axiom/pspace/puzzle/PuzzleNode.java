package com.axiom.pspace.puzzle ;
import com.axiom.pspace.search.* ;

public class PuzzleNode extends DefaultGraphNode {
    public static final int NUM_WHITE_SQUARES = 3 ;
    public static final int NUM_BLACK_SQUARES = 3 ;
    public static final char BLACK = 'B';
    public static final char WHITE = 'W';
    public static final char EMPTY = 'E';
    
    public PuzzleNode ( PuzzleNode parent ) {
       super( parent ) ;   
    }
    
    public String toString() {
       StringBuffer result = new StringBuffer() ;
       for (int i=0;i<squares.length;i++) {
          result.append( squares[i] ) ;
       }
       result.append('(').append(fvalue).append(')');
       return result.toString() ;
    }
        
    public int hashCode() {
       int result = 0;
       for (int i=0;i<squares.length;i++) {
          result += squares[i] << ( 4 * i );
       }
       return result ;
    }
    
    public boolean isIdentical( GraphNode g ) {
        try {
            PuzzleNode p = ( PuzzleNode ) g ;
            for (int i=0;i<squares.length;i++) {
                if ( p.squares[i] != squares[i] )
                    return false ;
            }
            return true ;
        }
        catch ( ClassCastException e ) {
            return false ;   
        }
    }
    
    public int compareTo( Object o ) {
        PuzzleNode p = ( PuzzleNode ) o ;
        if ( p.fvalue < fvalue )
           return -1 ;
        else if ( p.fvalue == fvalue )
           return 0 ;
        else
           return 1 ;
    }
    
    /**
     *  Cost value, "known" costs.
     */
    int gvalue ;
    
    /**
     *  Heuristic value, estimate of goodness.
     */
    int hvalue ;
    
    /**
     *  Total value
     */
    int fvalue ;
    
    char[] squares = new char[7];
}