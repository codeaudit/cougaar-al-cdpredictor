package com.axiom.lib.util;
import java.util.*;
import java.io.*;
import java.text.*;

public class IntMatrixEnumeration {
    IntMatrixEnumeration( IntMatrix matrix ) {
       this.matrix = matrix ;
       lower = new int[matrix.dimensions.length] ;  // Zero array
       upper = ( int[] ) matrix.dimensions.clone() ; // Clone upper array ;
       for (int i=0;i<upper.length;i++)
         upper[i] = upper[i] - 1 ;
       index = ( int[] )lower.clone();
    }
    
    /** Get an enumeration, inclusive of lower and upper bounds.  If any element of
     *  lower bounds is <0, it is set to zero.  If any element of upper bound is
     *  greater than the size in that dimension, it is set to dimension -1.  If the
     *  length of lower or upper is less than the dimesionality of the matrix, the upper
     *  dimensions will be assumed to be zero.  If the length is greater than the matrix,
     *  the result will be truncated.
     */
    
    IntMatrixEnumeration( IntMatrix matrix,  int[] lower, int[] upper ) {
       this.matrix = matrix ;
       this.lower = (int[]) lower.clone(); this.upper = (int[]) upper.clone();
       // Check bounds
       for (int i=0;i<lower.length;i++)
          if ( this.lower[i] < 0 )
            this.lower[i] = 0;
       for (int i=0;i<upper.length;i++)
          if ( this.upper[i] >= matrix.dimensions[i] )
            this.upper[i] = matrix.dimensions[i] - 1;
       for (int i=0;i<this.lower.length;i++)
          if ( this.lower[i] > this.upper[i] )
             this.lower[i] = this.upper[i] ;
       index = (int[]) this.lower.clone();
    }
    
    public int[] getIndex() {
       return (int[]) index.clone() ;
    }

    public int[] getLower() {
       return (int[]) lower.clone() ;
    }

    public int[] getUpper() {
       return (int[]) upper.clone() ;   
    }
    
    public boolean hasMoreElements() {
       for (int i=0;i<index.length;i++)
         if ( index[i] > upper[i]  )
           return false ;
         else
           return true ;
           
       return true ;
    }
    
    /** Returns current element and increments to next element.
     */
    
    public int nextElement() {
      int element = 0 ;
      try {
        element = matrix.at(index);
        if ( index[0] < upper[0] ) {
          index[0] = index[0] + 1;
          return element;
        }
        else index[0] = lower[0];

        for (int i=1;i< index.length ;i++) {
          if ( index[i] < upper[i] ) {
            index[i] += 1;
            return element ;
          }
          else
            index[i] = lower[i];
        }
        index[0] = upper[0]+1 ;
      }
      catch ( Exception e ) {
        e.printStackTrace();       
      }
      
      return element;
    }
    
    IntMatrix matrix ;
    int[] index ;
    int[] lower, upper ;
  }
 