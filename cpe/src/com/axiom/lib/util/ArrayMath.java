package com.axiom.lib.util;
import java.io.*;

/** Utilties for performing math operations on arrays of primitives.  These are mostly small
 * static methods for performing repetitive calculations.  This class should probably be
 * moved to the com.axiom.lib.math package.
 */

public class ArrayMath {

  public final static float normInnerProduct( float[] a1, float[] a2 ) {
    if ( a1.length != a2.length ) {
        throw new IllegalArgumentException( "Arrays have unequal lengths." ) ;
    }

    float result = 0 ;
    for (int i=0;i<a1.length;i++) {
        result += a1[i] * a2[i] ;
    }
    return result / ( L2Norm( a1 ) * L2Norm( a2 ) ) ;
  }

  public final static float stanimoto( float[] a1, float[] a2 ) {
    if ( a1.length != a2.length ) {
        throw new IllegalArgumentException( "Arrays have unequal lengths." ) ;
    }

    float result = 0 ;
    for (int i=0;i<a1.length;i++) {
        result += a1[i] * a2[i] ;
    }

    return result / ( L2NormSquared( a1 ) + L2NormSquared( a2 ) - result ) ;
  }

  /**
   *  Compare two integer arrays to see if they match exactly.
   *  @return Returns true if and only if the a1 and a2 are identically sized and
   *          have the same values as elements.
   */
  public final static boolean compare( int[] a1, int[] a2 ) {
     if ( a1.length != a2.length )
        return false ;

     for (int i=0;i<a1.length;i++) {
        if ( a1[i] != a2[i] ) {
           return false ;   
        }
     }
     return true ;
  }

  public final static double dot( double[] x1, double[] x2 ) {
     double sum = 0 ;
     for (int i=0;i<x1.length;i++) {
        sum += x1[i] * x2[i] ;
     }
     return sum ;
  }

  public final static double dot( float[] x1, float[] x2 ) {
     double sum = 0 ;
     for (int i=0;i<x1.length;i++) {
        sum += x1[i] * x2[i] ;
     }
     return sum ;
  }

  public final static void copy( double[][] src, double[][] dest ) {
     for (int i=0;i<src.length;i++) {
            copy( src[i], dest[i] ) ;
     }
  }

  public static int[] clone( int[] value ) {
    if ( value == null ) return null ;
    return ( int[] ) value.clone() ;
  }

  public static int[][] clone( int[][] val ) {
    if ( val == null ) return null ;
    int[][] result = new int[ val.length ][];
    for (int i=0;i<result.length;i++)
        result[i] = clone( val[i] );
    return result ;
  }

  public static double[] clone( double[] val ) {
    if ( val == null ) return null ;
    return ( double[] ) val.clone() ;
  }

  public static double[][] clone( double[][] val ) {
    double[][] result = new double[ val.length ][];
    for (int i=0;i<result.length;i++)
        result[i] = clone( val[i] );
    return result ;
  }

  public static float[] clone( float[] val ) {
    if ( val == null ) return null ;
    return ( float[] ) val.clone() ;
  }

  public static float[][] clone( float[][] val ) {
    float[][] result = new float[ val.length ][];
    for (int i=0;i<result.length;i++)
        result[i] = clone( val[i] );
    return result ;
  }

  public final static void copy( double[] src, double[] dest ) {
     try {
     for (int i=0;i<src.length;i++) {
        dest[i] = src[i] ;
     }
     }
     catch ( NullPointerException e ) {} 
  }
  
  public final static void cast( float[] a1, int[] a2 ) {
     int length = ( a1.length > a2.length ) ? a2.length : a1.length ;
     for (int i=0;i<length;i++) {
        a2[i] = ( int ) a1[i] ;
     }
  }

  public final static void cast( float[] src, double[] dest ) {
     int length = ( src.length > dest.length ) ? dest.length : src.length ;
     for (int i=0;i<length;i++) {
        dest[i] = src[i] ;
     }
  }

  public final static void cast( int[] src, float[] dest ) {
     int length = ( src.length > dest.length ) ? dest.length : src.length ;
     for (int i=0;i<length;i++) {
        dest[i] = src[i] ;
     }
  }

  public final static void cast( double[] a1, int[] a2 ) {
     int length = a1.length > a2.length ? a2.length : a1.length ;
     for (int i=0;i<length;i++) {
        a2[i] = ( int ) a1[i] ;
     }
  }  

  public final static void cast( double[] a1, float[] a2 ) {
     int length = a1.length > a2.length ? a2.length : a1.length ;
     for (int i=0;i<length;i++) {
        a2[i] = ( float ) a1[i] ;
     }
  }  
    
  final static public void set( int[] array, int scalar ) {
    for (int i=0;i<array.length;i++) {
        array[i] = scalar ;
    }
  }
  
  final static public void set( float[] array, float scalar ) {
    for (int i=0;i<array.length;i++) {
        array[i] = scalar ;
    }
  }
  
  final static public void set( double[] array, double scalar ) {
    for (int i=0;i<array.length;i++) {
        array[i] = scalar ;
    }    
  }

  final static public void set( float[][] array, float scalar ) {
    for (int i=0;i<array.length;i++) {
        for (int j=0;j<array[i].length;j++) {
            array[i][j] = scalar ;
        }
    }
  }

  final static public void set( double[][] array, double scalar ) {
    for (int i=0;i<array.length;i++) {
        for (int j=0;j<array[i].length;j++) {
            array[i][j] = scalar ;
        }
    }
  }

  final static float[] seq( float lower, float upper, float stride ) {
      if ( upper <= lower ) {
         return new float[0] ;
      }
      int length = (int) Math.floor( ( ( upper - lower ) /stride ) );

      float[] result = new float[ length ];
      int j = 0 ;
      float i = lower ;
      while( i < upper ) {
         result[j]=i ;
         j++;
         i=i+stride ;
      }

      return result ;
  }

  final static public void add( float[] target, float[] array ) {
     for (int i=0;i<target.length;i++) {
         target[i] += array[i] ;
     }
  }

  final static public void add( double[] target, double value ) {
      for (int i=0;i<target.length;i++) {
        target[i] += value ;
      }
  }

  final static public void add( double[] target, double[] arg ) {
      for (int i=0;i<target.length;i++) {
        target[i] += arg[i] ;
      }
  }

  final static public void add( float[] target, float scalar ) {
     for (int i=0;i<target.length;i++) {
         target[i] += scalar ;
     }
  }

  final static public void add( int[] target, int scalar ) {
     for (int i=0;i<target.length;i++) {
         target[i] += scalar ;
     }
  }


  final static public void sub( float[] target, float[] array ) {
     for (int i=0;i<target.length;i++) {
         target[i] -= array[i] ;
     }
  }

  final static public void div( float[] target, float value ) {
     for (int i=0;i<target.length;i++) {
         target[i] /= value ;
     }
  }

  final static public void div( float[] target, float[] div ) {
     if ( target.length != div.length ) {
        throw new IllegalArgumentException("Sizes of arrays do not match." ) ;
     }

     for (int i=0;i<target.length;i++) {
         target[i] /= div[i] ;
     }
  }

  final static public void div( float[] target, float[] div1, float div2 ) {
     if ( target.length != div1.length ) {
        throw new IllegalArgumentException("Sizes of arrays do not match." ) ;
     }
     for ( int i=0;i<target.length;i++) {
         target[i] = div1[i] / div2 ;
     }
  }

  final static public void div( float[] target, float[] div1, float[] div2 ) {
     if ( target.length != div1.length || target.length != div2.length ) {
        throw new IllegalArgumentException("Sizes of arrays do not match." ) ;
     }
     for ( int i=0;i<target.length;i++) {
         target[i] = div1[i] / div2[i] ;
     }
  }

  static public double[][] transpose( double[][] a ) {
     try {
        double[][] result = new double[ a[0].length ][a.length ] ;
        int colsize = result[0].length ;
        for (int i=0;i<result.length;i++) {
            for (int j=0;j<colsize;j++) {
                result[i][j] = a[j][i] ;
            }
        }
        return result ;
     }
     catch ( NullPointerException e ) {
        throw new IllegalArgumentException( "Array is null or not full rank." ) ;
     }
     catch ( IndexOutOfBoundsException e ) {
        throw new IllegalArgumentException( "Array is not of uniform size." ) ;
     }
  }

  /** Array mult. */
  final static public void amult( double[] target, double value ) {
     for (int i=0;i<target.length;i++) {
         target[i] *= value ;
     }
  }

  /** Array mult */
  final static public void amult( double[] target, double[] mult2 ) {
     for (int i=0;i<target.length;i++) {
         target[i] *= mult2[i] ;
     }
  }

  /** Array mult */
  final static public void amult( double[][] target, double[][] mult ) {
     for (int i=0;i<target.length;i++) {
        ArrayMath.amult( target[i], mult[i] );
     }
  }

  final static public void amult( float[] target, float value ) {
     for (int i=0;i<target.length;i++) {
         target[i] *= value ;
     }
  }

  final static public void amult( float[] target, float[] mult2 ) {
     for (int i=0;i<target.length;i++) {
         target[i] *= mult2[i] ;
     }
  }

  final static public void amult( float[][] target, float[][] mult ) {
     for (int i=0;i<target.length;i++) {
        ArrayMath.amult( target[i], mult[i] );
     }
  }

  final static public void amult( float[] target, float[] src, float value ) {
     if ( target.length != src.length ) {
        throw new IllegalArgumentException("Sizes of arrays do not match." ) ;
     }
     for ( int i=0;i<target.length;i++) {
         target[i] = src[i] * value ;
     }
  }

    /**
     *  src = floor( src ) ;
     */
    final static public void floor( float[] src ) {
        for (int i=0;i<src.length;i++) {
          src[i] = ( float ) Math.floor( src[i] ) ;
        }
    }

    final static public void ceil( float[] src ) {
        for (int i=0;i<src.length;i++) {
          src[i] = ( float ) Math.ceil( src[i] ) ;
        }
    }

    final static public void rand( float[] a1 ) {
        for (int i=0;i<a1.length;i++) {
            a1[i] = ( float ) Math.random() ;
        }
    }

    final static public void rand( float[][] a1 ) {
        for (int i=0;i<a1.length;i++) {
            rand( a1[i] ) ;
        }
    }

    final static public void rand( double[] a1 ) {
        for (int i=0;i<a1.length;i++) {
            a1[i] = Math.random() ;
        }
    }

    final static public void rand( double[][] a1 ) {
        for (int i=0;i<a1.length;i++) {
            rand( a1[i] ) ;
        }
    }

    /**
     *  Compute Euclidean distance between a2 and a2.
     */
  final static public float dist( float[] a1, float[] a2 ) {
     float result = 0;
     for (int i=0;i<a1.length;i++) {
        float temp = a1[i] - a2[i];
        result += temp * temp ;
     }
     return ( float ) Math.sqrt( result );
  }

    /**
     *  Compute Euclidean distance between a2 and a2.
     */
  final static public double dist( double[] a1, double[] a2 ) {
     float result = 0;
     for (int i=0;i<a1.length;i++) {
        double temp = a1[i] - a2[i];
        result += temp * temp ;
     }
     return Math.sqrt( result );
  }

    /**
     *  Compute Squared Euclidean distance between a2 and a2.
     */
  final static public float dist2( float[] a1, float[] a2 ) {
     float result = 0;
     for (int i=0;i<a1.length;i++) {
        float temp = a1[i] - a2[i];
        result += temp * temp ;
     }
     return result ;
  }

    /**
     *  Compute Squared Euclidean distance between a2 and a2.
     */
  final static public double dist2( double[] a1, double[] a2 ) {
     double result = 0;
     for (int i=0;i<a1.length;i++) {
        double temp = a1[i] - a2[i];
        result += temp * temp ;
     }
     return result ;
  }

  final static public void sub( float[] target, float scalar ) {
     for (int i=0;i<target.length;i++) {
         target[i] -= scalar ;
     }    
  }

  final static public void sub( double[] target, double scalar ) {
     for (int i=0;i<target.length;i++) {
         target[i] -= scalar ;
     }
  }

  final static public void sub( double[] target, double[] arg2 ) {
    for (int i=0;i<target.length;i++) {
        target[i] -= arg2[i] ;
    }
  }

  final static public double mean( double[] a ) {
    double result = sum( a ) ;
    return result / a.length ;
  }

  final static public double[] mean( double[][] a ) {
    double[] result = new double[a.length ];
    for (int i=0;i<a.length;i++) {
        result[i] = mean( a[i] ) ;
    }
    return result ;
  }

  final static public int sum( byte[] array ) {
    int result = 0;
    for( int i=0;i<array.length;i++)
       result += array[i];
    return result ;  
  }

  final static public int sum( int[] array ) {
    int result = 0;
    for( int i=0;i<array.length;i++)
       result += array[i];
    return result ;
  }
  
  static public int product( int[] array ) {
     int result = array[0];
     for (int i=1;i<array.length;i++) {
        result *= array[i];
     }
     return result ;
  }
  
  static public float product( float[] array ) {
     float result = array[0];
     for (int i=1;i<array.length;i++) {
        result *= array[i];
     }
     return result ;
  }

  static public double sum( double[] array ) {
     double result = 0;
     for (int i=0;i<array.length;i++)
        result += array[i] ;
     return result ;
  }

  static public double[] sum( double[][] a ) {
     double[] result = new double[ a.length ] ;
     for (int i=0;i<a.length;i++) {
         result[i] = sum( a[i] ) ;
     }
     return result ;
  }

  /**
   *  Returns total sum of full rank array.
   */
  static public long asum( int[][] array ) {
     long result = 0 ;
     for (int i=0;i<array.length;i++)
        result += sum( array[i] ) ;
     return result ;
  }

  static public double asum( double[][] array ) {
     double result = 0;
     for (int i=0;i<array.length;i++)
        result += sum( array[i] ) ;
     return result ;
  }

  static public double asum( float[][] array ) {
     float result = 0;
     for (int i=0;i<array.length;i++)
        result += sum( array[i] ) ;
     return result ;
  }

  static public long asum( byte[][] array ) {
     long result = 0 ;
     for (int i=0;i<array.length;i++)
        result += sum( array[i] ) ;
     return result ;
  }

  static public float sum( float[] array ) {
     float result = 0;
     for (int i=0;i<array.length;i++)
        result += array[i] ;
     return result ;
  }

  final static public float min( float[] array ) {
     float min = array[0] ;
     for (int i=1;i<array.length;i++)
        if ( array[i] < min ) 
           min = array[i] ;
     return min ;
  }

  static public float[] sum( float[][] a ) {
     float[] result = new float[ a.length ] ;
     for (int i=0;i<a.length;i++) {
         result[i] = sum( a[i] ) ;
     }
     return result ;
  }

  final static public double min( double[] array ) {
     double min = array[0] ;
     for (int i=1;i<array.length;i++)
        if ( array[i] < min ) 
           min = array[i] ;
     return min ;
  }

  final static public double[] min( double[][] a ) {
     double[] result = new double[ a.length ] ;
     for (int i=0;i<a.length;i++) {
        result[i] = min( a[i] ) ;
     }
     return result ;
  }

  final static public float max( float[] array ) {
     float max = array[0] ;
     for (int i=1;i<array.length;i++)
        if ( array[i] > max ) max = array[i] ;
     return max ;
  }

  final static public double max( double[] array ) {
     double max = array[0] ;
     for (int i=1;i<array.length;i++)
        if ( array[i] > max ) max = array[i] ;
     return max ;
  }

  final static public double[] max( double[][] a ) {
     double[] result = new double[ a.length ] ;
     for (int i=0;i<a.length;i++) {
        result[i] = max( a[i] ) ;
     }
     return result ;
  }

  final static public float min( float[][] array ) {
     float min = array[0][0] ;
     for (int i=0;i<array.length;i++)
       for (int j=0;j<array.length;j++)
          if ( array[i][j] < min ) min = array[i][j] ;
     return min ;
  }
  
  final static public void seqsum( int[][] array ) {
     for (int i=0;i<array.length;i++)
       for (int j=0;j<array[0].length;j++)
          array[i][j] = i + j ;
  }
  
  final static public void seqsum( float[][] array ) {
     for (int i=0;i<array.length;i++) 
       for (int j=0;j<array[0].length;j++)
          array[i][j] = i + j ;         
  }

  final static public float max( float[][] array ) {
     float max = array[0][0] ;
     for (int i=0;i<array.length;i++)
       for (int j=0;j<array.length;j++)
          if ( array[i][j] > max ) max = array[i][j] ;
     return max ;
  }


  final static public int max( int[] array ) {
     int max = array[0] ;
     for (int i=1;i<array.length;i++)
        if ( array[i] > max ) max = array[i] ;
     return max ;
  }

  final static public int min( int[] array ) {
     int min = array[0] ;
     for (int i=1;i<array.length;i++)
        if ( array[i] < min ) min = array[i] ;
     return min ;
  }

  final static public double min( NumberArray n ) {
    double result = n.valueAt(0) ;
    for (int i=1;i<n.getSize();i++) {
       result = Math.min( result, n.valueAt(i) ) ;
    }
    return result ;
  }

  final static public double max( NumberArray n ) {
    double result = n.valueAt(0) ;
    for (int i=1;i<n.getSize();i++) {
       result = Math.max( result, n.valueAt(i) ) ;
    }
    return result ;
  }

  final static public float L2Norm( float[] a1 ) {
      float result = 0 ;
      for (int i=0;i<a1.length;i++) {
         float val = a1[i] ;
         result += val * val ;
      }
      return ( float ) Math.sqrt( result ) ;
  }

  final static public double mag( double[] a1 ) {
      return L2Norm( a1 ) ;
  }

  final static public double L2Norm( double[] a1 ) {
      double result = 0 ;
      for (int i=0;i<a1.length;i++) {
         double val = a1[i] ;
         result += val * val ;
      }
      return Math.sqrt( result ) ;
  }

  final static public double L2NormSquared( double[] a1 ) {
      double result = 0 ;
      for (int i=0;i<a1.length;i++) {
         double val = a1[i] ;
         result += val * val ;
      }
      return result ;
  }

  final static public float L2NormSquared( float[] a1 ) {
      float result = 0 ;
      for (int i=0;i<a1.length;i++) {
         double val = a1[i] ;
         result += val * val ;
      }
      return result ;
  }

  final static public double L2Norm( double[][] a1 ) {
      double result = 0 ;
      for (int i=0;i<a1.length;i++) {
         result += L2NormSquared( a1[i] ) ;
      }
      return Math.sqrt( result ) ;
  }

    public static String toString( byte[] f ) {
        StringBuffer s = new StringBuffer() ;
        for (int i=0;i<f.length;i++) {
            s.append( Byte.toString( f[i] ) ).append(' ');
        }
        return s.toString() ;
    }

    public static String toString( int[] f ) {
        StringBuffer s = new StringBuffer() ;
        for (int i=0;i<f.length;i++) {
            s.append( Integer.toString( f[i] ) ).append(' ');
        }
        return s.toString() ;
    }


    public static String toString( long[] f ) {
        StringBuffer s = new StringBuffer() ;
        for (int i=0;i<f.length;i++) {
            s.append( Long.toString( f[i] ) ).append(' ');
        }
        return s.toString() ;
    }

    public static String toString( float[] f ) {
        StringBuffer s = new StringBuffer() ;
        for (int i=0;i<f.length;i++) {
            s.append( Float.toString( f[i] ) ).append(' ');
        }
        return s.toString() ;
    }

    public static String toString( float[] f, java.text.NumberFormat format ) {
        StringBuffer s = new StringBuffer() ;
        for (int i=0;i<f.length;i++) {
            s.append( format.format( f[i] ) ).append(' ');
        }
        return s.toString() ;
    }

    public static String toString( double[] f ) {
        StringBuffer s = new StringBuffer() ;
        for (int i=0;i<f.length;i++) {
            s.append( Double.toString( f[i] ) ).append(' ');
        }
        return s.toString() ;
    }

    public static String toString( double[] f, java.text.NumberFormat format ) {
        StringBuffer s = new StringBuffer() ;
        for (int i=0;i<f.length;i++) {
            s.append( format.format( f[i]) ).append(' ');
        }
        return s.toString() ;
    }

  
  static public void read( StreamTokenizer stream, float[] array ) 
                     throws IOException 
  {
     for ( int i=0; i< array.length; i++ ) {
       int t = stream.nextToken() ;
       if ( t != StreamTokenizer.TT_NUMBER ) 
         throw new IOException( "Unexpected EOF or not a number encountered.");
       else
         array[i] = ( float ) stream.nval ;
     }
  }

  static public void read( StreamTokenizer stream, float[][] array ) 
                     throws IOException 
  {
     int t ;
     
     for ( int i=0; i< array.length; i++ ) {
        for (int j=0;j< array[i].length;j++ ) {
           t = stream.nextToken() ;
           if ( t != StreamTokenizer.TT_NUMBER ) 
              throw new IOException( "Unexpected EOF or not a number encountered.");
           else
              array[i][j] = ( float ) stream.nval ;
        }
     }
  }
  
  static public void write( PrintWriter ps, float[] array ) {
     for (int i=0;i<array.length;i++) {
        ps.print( array[i] );
        ps.print( ' ' );      
     }
  }

  static public void write( PrintWriter ps, float[][] array ) {
     for (int i=0;i<array.length;i++) {
        for (int j=0;j<array[i].length;j++) {
           ps.print( array[i][j] );
           ps.print( ' ' );
        }
     }
  }

  public static void main( String[] args ) {
     float[] f1 = { 255, 0, 255, 128 } ;
     float[] f2 = { 128, 0, 255, 0 } ;
     float value = normInnerProduct( f1, f2 ) ;
     value = normInnerProduct( f1, f1 ) ;
     value = stanimoto( f1, f2 ) ;
     value = stanimoto( f1, f1 ) ;
  }
}

