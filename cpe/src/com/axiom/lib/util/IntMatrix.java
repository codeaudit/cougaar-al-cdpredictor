package com.axiom.lib.util;
import java.util.*;
import java.io.*;
import java.text.*;

/** Multidimensional matrix of floats.  It implements the NumberArray
 *  interface to allow for linear access.
 */

public class IntMatrix implements Serializable, Cloneable, NumberArray {
    protected static final long serialVersionUID = 1L;
    
    public IntMatrix() {
    
    }

    public int getSize() {
        return array.length ;
    }
    
    public double valueAt( int i ) {
        return array[i] ;
    }

    public Class getType() { return Integer.TYPE ; }

    public float floatAt( int i ) { return array[i] ; }

    public int intAt( int i ) { return array[i] ; }
    
    public Object clone() {
        IntMatrix intMatrix = new IntMatrix( dimensions ) ;
        System.arraycopy( array, 0, intMatrix.array, 0, array.length ) ;
        return intMatrix ;
    }
  
    /** Constructor taking array of dimensions.
    *
    *  @param dimensions  The dimensions of the multidim. array.
    */
    public IntMatrix( int[] dimensions ) {
        this.dimensions = ( int[] ) dimensions.clone();
        multFactor = new int[dimensions.length];
        int size = 1;
        for (int i=0;i<dimensions.length;i++) {
        multFactor[i] = size ;
        size *= dimensions[i];
        }
        array = new int[size];
    }

    public int getRank() {
        return dimensions.length ;
    }
  
    public int[] getDimensions() {
        return dimensions ;
    }

    /**
     *  Convenience method for two-dimensional matrices.
     *  @return value at row, column
     */
    public int at( int row, int column ) {
        int ia ;
        
        if ( row < dimensions[0] && dimensions.length >= 2) {
            ia = row ;
        }
        else
            throw new MatrixException() ;
        
        if ( column < dimensions[1] ) 
            ia += column * multFactor[1];
        else
            throw new MatrixException() ;
        return array[ia] ;
    }

    public int getNumRows() {
        return dimensions[0];
    }
    
    public int getNumColumns() {
        if ( dimensions.length < 2 ) {
            return 0 ;
        }
        return dimensions[1] ;
    }

    /**
     *  Convenience method for two-dimensional matrices.
     */    
    public void set( int row, int column, int value ) {
        int ia ;
        
        if ( row < dimensions[0] && dimensions.length >= 2) {
            ia = row ;
        }
        else
            throw new MatrixException() ;
        
        if ( column < dimensions[1] ) 
            ia += column * multFactor[1];
        else
            throw new MatrixException() ;
        array[ia] = value ;
    }

    public void set( int i, double value ) {
        array[i] = ( int ) value ;
    }

  /** Returns element at index.
   */ 
  public int at( int[] index ) throws MatrixException {
     int ia ;
     if ( index[0] < dimensions[0] )
       ia = index[0];
     else
       throw new MatrixException();
       
     for (int i=1;i<index.length;i++ )
     {
       if ( index[i] < dimensions[i] )
         ia += index[i] * multFactor[i] ;
       else
         throw new MatrixException();
     }
     return array[ia] ;
  }
  
 /** Returns element at index.
  */
   
  public void set( int[] index, int value ) throws MatrixException {
     int ia = index[0];

     for (int i=1;i<index.length;i++ )
     {
       if ( index[i] < dimensions[i] )
         ia += index[i] * multFactor[i] ;
       else
         throw new MatrixException();
     }
     array[ia] = value;
  }
  
  public int max() {
    int result = array[0];
    for (int i=1;i<array.length;i++)
      if ( array[i] > result )
        result = array[i];
    return result ;
  }
  
  public int min() {
    int result = array[0];
    for (int i=1;i<array.length;i++)
      if ( array[i] < result )
        result = array[i];
    return result ;    
  }

    /**
     *  Flips a 2D matrix left-right.  It assumes the convention that the 
     *  first (0) index corresponds to the column index and the second index (1)
     *  corresponds to the row index.
     */
    public void flipLR() {
        int numRows = getNumRows(), numColumns = getNumColumns() ;
        if ( numColumns <= 1 ) {
            return ;
        }
        int tmp ;
        
        for (int i=0;i<numRows;i++) {
            for (int j=0;j< numColumns/2 ;j++) {
                tmp = at( i, j ) ;
                set( i, j, at( i, numColumns - j - 1) ) ;
                set( i, numColumns - j -1, tmp ) ; 
            }
        }
       
    }

    /**
     *  Flips a 2D matrix up down in place.  It assumes the convention that the 
     *  first (0) index corresponds to column index and the second index (1)
     *  corresponds to the row index.
     */    
    public void flipUD() {
        int numRows = getNumRows(), numColumns = getNumColumns() ;
        if ( numRows <= 1 ) {
            return ;
        }
        int tmp ;
        
        for (int i=0;i<numRows/2;i++) {
            for (int j=0;j< numColumns;j++) {
                tmp = at( i, j ) ;
                set( i, j, at( numRows - i - 1, j ) ) ;
                set( numRows - i - 1, j, tmp ) ; 
            }
        }        
    }
  
  public int[] getArray() {
    return array ;  
  }
  
  public void seqdiag() {
     int[] index = new int[dimensions.length];

     array[0]= ArrayMath.sum( index );
     for (int aindex=1;aindex<array.length;aindex++) {
       for (int i=0;i<dimensions.length;i++) {
         if ( index[i] < ( dimensions[i] - 1) ) {
           index[i]++;
           break ;
         }
         else
           index[i]=0;
       }
       array[aindex] = ArrayMath.sum( index );
     }
     
  }
  
  /** Overrides toString()
   */
   
  public String toString() {
    String result = "";
 
    for ( IntMatrixEnumeration e=this.getMatrixElements(); e.hasMoreElements(); ) {
       String r = "";
       if ( e.getIndex().length > 2 ) {
          if ( e.getIndex()[0] == dimensions[0] - 1 ) {
            r = r + '\n';
            if ( e.getIndex()[1] == dimensions[1] - 1 )
              r = r + '\n';
          }
       }
       int element = e.nextElement();
       r = element + " " + r;
       result = result + r ;
    }
    return result ;
  }
  
  /** Reads an entire IntMatrix from 
   * 
   *  @return number of elements read.
   */
  
  public int read( StreamTokenizer s ) {
    Number number ;
    int i=0;
    
    try {
      for (i=0;i<array.length;i++) {
        if ( s.nextToken() != StreamTokenizer.TT_EOF ) 
        {
           if ( s.ttype == StreamTokenizer.TT_NUMBER ) {
             array[i] = ( int ) s.nval ;
           }
        }
        else
          return i ;
      }
    }
    catch ( Exception e ) {
      e.printStackTrace();
      return i ;
    }
    
    return array.length ;
  }
  
  public void zero() {
    for (int i=0;i<array.length;i++)
      array[i] = (int) 0.0 ;
  }
  
  /** Write this array, starting with lowest indices.
   */
  
  public void write( PrintWriter ps ) throws IOException {
    for (int i=0;i<array.length;i++) { 
      ps.print( array[i] );
      ps.print( ' ' );
    }
  }
  
  public IntMatrixEnumeration getMatrixElements() {
    return new IntMatrixEnumeration( this ); 
  }
   
  public IntMatrixEnumeration getMatrixElements( int[] lower, int[] upper ) {
    return new IntMatrixEnumeration( this, lower, upper );
  }
    
  public static void main( String argv[] ) {
    
    try {
      int[] dimensions = {4,4,4} ;
      IntMatrix testMatrix = new IntMatrix( dimensions );
     
      testMatrix.seqdiag();
    
      System.out.println( testMatrix.toString() );
    
      int[] lower = { 1, 2, 1 };
      int[] upper = { 1, 4, 3 };
      for ( IntMatrixEnumeration e= testMatrix.getMatrixElements(lower, upper); 
          e.hasMoreElements(); ) {
        System.out.println( ArrayMath.toString( e.getIndex() ) );
        System.out.println( e.nextElement() );   
      }
    
      // Print to a file and read it back in
      File file = new File( "test.dat" );
      FileOutputStream fs = new FileOutputStream( file );
      PrintWriter p = new PrintWriter( fs );
    
      testMatrix.write( p );
      p.flush();
      fs.close();
      
      FileInputStream is = new FileInputStream( file );
      StreamTokenizer s = new StreamTokenizer( is );
      testMatrix.zero();
      testMatrix.read( s );
      
      System.out.println( testMatrix.toString() );
    }
    catch ( Exception e ) {
      e.printStackTrace();   
    }
    
    
  }


  /* Private fields
   */

   protected int[] dimensions ;
   protected int[] multFactor ;
   protected int[] array ;
   static NumberFormat format = new DecimalFormat();
}