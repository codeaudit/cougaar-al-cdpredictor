package com.axiom.pspace.puzzle ;
import com.axiom.pspace.search.* ;

/**
 *  An example class demonstrating the use of the basic algorithm for solving
 *  a simple puzzle.
 */

public class PuzzleStrategy implements Strategy {
    
    /**
     *  Make an empty node.
     */
    public GraphNode makeNode() {
        return new PuzzleNode(null) ;
    }

    public GraphNode[] expand( GraphNode n ) {
        PuzzleNode pz = ( PuzzleNode ) n ;

        int emptyIndex = -1;
        int length = pz.squares.length ;
        for (int i=0;i<length;i++) {
           if ( pz.squares[i] == PuzzleNode.EMPTY ) {
              emptyIndex = i;
              break ;
           }
        }
        if ( emptyIndex == -1 ) 
           throw new RuntimeException( "Whoa! Invalid puzzle. No empty squares!" );
        
        int lower = emptyIndex - 3 ;
        int upper = emptyIndex + 3 ;
        if ( lower < 0 ) lower = 0 ;
        if ( upper > length - 1 ) upper = length - 1 ;
        
        GraphNode[] result = new GraphNode[ upper - lower ];
        int j = 0 ;
        int l = 0;
        
        for (int i=lower;i<upper+1;i++) {
            if ( i != emptyIndex ) {
                PuzzleNode p = new PuzzleNode( pz );
                for (int k=0;k<length;k++) {
                   if ( k == emptyIndex ) {
                      p.squares[k] = pz.squares[i] ;
                   }
                   else if ( k == i ) {
                      p.squares[k] = pz.squares[emptyIndex];
                   }
                   else 
                      p.squares[k] = pz.squares[k] ;
                }
                result[l++] = p ;
                int cost = i - emptyIndex ;
                if ( cost < 0 )
                   cost = -cost ;
                // Handles cases with jumps.
                if ( cost > 1 )
                   cost = cost - 1;
                // Update the known cost
                p.gvalue = pz.gvalue + cost ;
                computeHeuristic( p ) ;
            }
        }
        
        pz.setSuccessors( result );
        return result ;
    }
    
    public boolean isGoalNode( GraphNode n ) {
        PuzzleNode p = ( PuzzleNode ) n ;
        
        int count = 0;
        int i=0 ;
        while ( true ) {               
            if ( p.squares[i] == 'W' ) {
               count++ ;                              
               if ( count == 3 ) {
                  return true ;
               }
            }
            else if ( p.squares[i] == 'B' )
               return false ;
            i++ ;
        }
    }
    
    public int getNumDescendants( GraphNode n ) {
        throw new RuntimeException( "Unsupported method." ) ;   
    }
    
    public GraphNode expand( GraphNode n, int i ) {
        throw new RuntimeException( "Unsupported method." ) ;        
    }
    
    public boolean isEqual( GraphNode n1, GraphNode n2 ) {
        return n1.isIdentical( n2 ) ;
    }
    
    /**
     *  n1's parent has already been changed.  Update n1 so that
     *  it is now identical to n2.  n1 will remain on the list, while
     *  n2 will be discarded.
     */
    public void updateParent( GraphNode n1, GraphNode n2 ) {
        PuzzleNode p1 = ( PuzzleNode ) n1 ;
        PuzzleNode p2 = ( PuzzleNode ) n2 ;
        p1.fvalue = p2.fvalue ;
        p1.gvalue = p2.gvalue ;
    }
    
    public int compare( Object o1, Object o2 ) {
        PuzzleNode p1 = ( PuzzleNode ) o1 ;
        PuzzleNode p2 = ( PuzzleNode ) o2 ;

        return p1.fvalue - p2.fvalue ;
    }
    
    public void initNode( GraphNode n ) {
        PuzzleNode p = ( PuzzleNode ) n ;
        p.gvalue = 0 ;
        computeHeuristic( p ) ;
    }
    
    /**
     *  Computes the h(n) value and f(n) value ( heuristic ) for this node.
     */
    public void computeHeuristic( PuzzleNode p ) {
        int result = 0;
        int j = 0;
        int i = 0;
        while ( true ) {
            if ( i >= 3 )
               break ;
               
            while ( p.squares[j] != PuzzleNode.BLACK ) {
                j++ ;   
            }
            for (int k=j;k<p.squares.length;k++) {
                 if ( p.squares[k] == PuzzleNode.WHITE ) {
                    result++ ;
                 }
            }
            j++ ; // Advance j.
            i++ ;
        }
        
        p.hvalue = result ;
        p.fvalue = p.gvalue + p.hvalue ;
    }
    
    public static void main( String[] args ) {
        
        SimpleSearch search = new SimpleSearch( new PuzzleStrategy() );
        PuzzleNode n = new PuzzleNode(null) ;
        search.setRetainAllPredecessors( true ) ;

        char[] array = { 'B', 'B', 'B', 'W', 'W', 'W', 'E' };
        n.squares = array ;
        search.init( n ) ;
        // mplan.setMaxExpandedNodes( 10 ) ;
        search.run() ;
        
        if ( search.getState() == GraphSearch.GOAL_FOUND ) {
            GraphNode node = search.getGoalNode() ;
            System.out.println( "Goal node found!" ) ;
            System.out.println( "Tracing backwards from the goal node..." ) ;
            while ( node != null ) {
                System.out.println( node ) ;
                node = node.getParent() ;
            }
        }
        else
           System.out.println( "Error state reached." );
    }
}