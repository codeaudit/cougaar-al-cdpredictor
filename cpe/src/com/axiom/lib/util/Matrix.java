package com.axiom.lib.util;
import java.util.*;
import java.io.*;
import java.text.*;

/** Multidimensional matrix of Object references.
 */

public class Matrix implements Serializable, Cloneable {
  protected static final long serialVersionUID = 1L;

  public Matrix() {
  }

  /** Constructor taking array of dimensions.
   *
   *  @param dimensions  The dimensions of the multidim. array.
   */
  public Matrix( int[] dimensions ) {
     this.dimensions = ( int[] ) dimensions.clone();
     multFactor = new int[dimensions.length];
     size = 1;
     for (int i=0;i<dimensions.length;i++) {
       multFactor[i] = size ;
       size *= dimensions[i];
     }
     array = new Object[size];
  }

  public int getRank() {
     return dimensions.length ;
  }

  public int[] getDimensions() {
     return dimensions ;
  }

  public int indexOf( int[] index ) throws MatrixException {
     int ia ;
     if ( index[0] < dimensions[0] )
       ia = index[0];
     else
       throw new MatrixException();
       
     for (int i=1;i<index.length;i++ )
     {
       if ( index[i] < dimensions[i] )
         ia += index[i] * multFactor[i] ;
       else {
        // REVISIT:  Ugly, messy code!!  Out of range messages should be localized somewhere else.
        StringBuffer sb = new StringBuffer() ;
        sb.append( "Index " ) ;
        for (int j=0;j<index.length;j++) {
            sb.append( index[i] ).append( ' ' ) ;
        }
        sb.append( " is out of range." ) ;

         throw new MatrixException( sb.toString() );

       }
     }
     return ia ;
  }
  
  public int getSize() {
     return size ;
  }
  
  public Object clone() {
     Matrix matrix = new Matrix() ;
     matrix.dimensions = ( int[] ) dimensions.clone() ;
     System.arraycopy( array, 0, matrix.array, 0, array.length ) ;
     return matrix ;
  }

  public int linearIndex( int[] index ) throws MatrixException {
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
     return ia ;
  }

  /** Returns element at index.
   */
  public Object at( int[] index ) throws MatrixException {
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

  public Object at( int index ) {
     return array[index];
  }

 /** Returns element at index.
  */

  public void set( int[] index, Object value ) throws MatrixException {
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

  /**
   *  Returns the underlying array.
   */
  public Object[] getArray() {
    return array ;
  }

  /** Overrides toString().  Prints in two-dimensional matrix format.
   */

    public String toString() {
        StringBuffer result = new StringBuffer();

        for ( MatrixEnumeration e=this.getMatrixElements(); e.hasMoreElements(); ) {
            Object element = e.at();
            result.append( element ).append( " " ) ;

            if ( e.getIndex().length > 2 ) {
                if ( e.getIndex()[0] == dimensions[0] - 1 ) {
                    result.append( '\n' );
                    if ( e.getIndex()[1] == dimensions[1] - 1 ) {
                        result.append( '\n' );
                    }
                }
            }
            e.nextElement();
        }
        return result.toString();
    }

  public MatrixEnumeration getMatrixElements() {
    return new MatrixEnumeration( this );
  }

  public MatrixEnumeration getMatrixElements( int[] lower, int[] upper ) {
    return new MatrixEnumeration( this, lower, upper );
  }

  public static void main( String argv[] ) {

    try {
      int[] dimensions = {4,4,4} ;
      Matrix testMatrix = new Matrix( dimensions );

      int[] dimensions2 = {4,4,4,4};
      Matrix testMatrix2 = new Matrix( dimensions2 ) ;

      System.out.println( testMatrix.toString() );

      int[] lower = { 1, 2, 1 };
      int[] upper = { 1, 3, 3 };
      System.out.println( "Enumerating elements from " + ArrayMath.toString( lower ) + " to " +
                          ArrayMath.toString( upper ) ) ;
      for ( MatrixEnumeration e= testMatrix.getMatrixElements(lower, upper);
          e.hasMoreElements(); ) {
        System.out.println( ArrayMath.toString( e.getIndex() ) );
        System.out.println( e.nextElement() );
      }

      System.out.println( "Testing MatrixEnumeration2 " ) ;
      MatrixEnumeration e2 = new MatrixEnumeration( testMatrix, lower, upper ) ;
      for ( ; e2.hasMoreElements(); ) {
        System.out.println( ArrayMath.toString( e2.getIndex() ) );
        System.out.println( e2.nextElement() );
      }

        System.out.println( "Testing matrix enumeration: " );
        System.out.println( testMatrix2 ) ;

      /** Test exception handling
      e2 = new MatrixEnumeration2( testMatrix, lower, upper ) ;
      while ( e2.hasMoreElements() ) {
        System.out.println( ArrayMath.toString( e2.getIndex() ) );
        System.out.println( e2.nextElement() );
      }

      e2.nextElement() ;
      */

      // Print to a file and read it back in

      System.out.println( testMatrix.toString() );
    }
    catch ( Exception e ) {
      e.printStackTrace();
    }


  }

  /* Private fields
   */

   int size = 0 ;
   protected int[] dimensions ;
   protected int[] multFactor ;
   protected Object[] array ;
   protected static NumberFormat format = new DecimalFormat();
}