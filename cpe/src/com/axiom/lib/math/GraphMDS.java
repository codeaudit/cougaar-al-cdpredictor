package com.axiom.lib.math ;
import java.util.Hashtable ;
import java.util.* ;

public class GraphMDS extends RowScaledMDS {

    public void setCells(MDSCell[] cells) {
        super.setCells( cells);

        Vector[] adjCells = new Vector[X.length] ;
        for (int i=0;i< cells.length; i++) {

        }
    }

    /**
     *  Compute stress, which is defined as
     *  S = sqrt( SS / T ) = sqrt( sum( dist - dhat)^2 / sum( dist^2) )
     */
    public void computeStress( MDSCell[] cells, Stress stress ) {
       double SS = 0, T = 0, S = 0 ;

       for (int i=0;i<cells.length;i++) {
           OrderedCell cell = ( OrderedCell ) cells[i] ;

           if ( !cell.epsilon ) {
             double dist = cell.dist ;
             T += dist * dist ;
             double tmp =  dist - cell.dhat ;
             SS += tmp * tmp ;
           }
           else { // Check to see if my dist value is greater than the
             // dist value of my parent maxCell
             //OrderedCell parent = cell.maxCell ;
             //double dist = ArrayMath.dist( X[cell.i], X[cell.j] ) ;
             
           }
       }

       stress.S = ( double ) Math.sqrt( SS / T ) ;
       stress.T = T ;
       stress.SS = SS ;
    }

    protected int[][] neighbors ;
    /**
     *  Epsilon weight assigned to value
     */
    protected float epsilon ;

}
