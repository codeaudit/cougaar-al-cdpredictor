package com.axiom.lib.math ;
import com.axiom.lib.util.* ;

/**
 *  Various misc. math utilities that don't find well anywhere.
 */
public abstract class MathUtils {

    /**
     *  Make a histogram with a fixed number of bins.  Each bin i consists of the
     *  number of elements in array which are >= i * scale and <= ( i + 1 ) * scale
     *  @param min Minimum value for histogram.  All elements <min will be put into
     *         the zeroth bin
     *  @param max Max value for histogram. All elements > max will be put into the
     *          bins - 1 bin.
     *  @param bins Number of bins in the histogram
     *  @return An array of size <code>bins</code> containing the histogram result.
     */
    public static int[] hist( float[] array, float min, float max, int bins ) {
        if ( bins <= 0 )
            throw new IllegalArgumentException( "Number of bins must be >= 0.");
            
        if ( min >= max ) {
            throw new IllegalArgumentException( "Min param must be < max param." );   
        }
        int[] result = new int[ bins ];
        float scale = ( max - min ) / bins ;
        
        for (int i=0;i<array.length;i++) {
            if ( array[i] < min ) {
                result[0]++; 
            }
            else if ( array[i] > max ) {
                result[bins-1]++;  
            }
            else {
                float tmp = ( ( array[i] - min ) * scale ) ;
                result[ ( int ) Math.floor( tmp )]++ ;
            } 
        }
        
        return result ;
    }

    /**
     *  Make a histogram with a fixed number of bins.  Each bin i consists of the
     *  number of elements in array which are >= i * scale and <= ( i + 1 ) * scale
     *  @param min Minimum value for histogram.  All elements <min will be put into
     *         the zeroth bin
     *  @param max Max value for histogram. All elements > max will be put into the
     *          bins - 1 bin.
     *  @param bins Number of bins in the histogram
     *  @return An array of size <code>bins</code> containing the histogram result.
     */
    public static int[] hist( int[] array, int min, int max, int bins ) {
        if ( bins <= 0 )
            throw new IllegalArgumentException( "Number of bins must be >= 0.");
            
        if ( min >= max ) {
            throw new IllegalArgumentException( "Min param must be < max param." );   
        }
        int[] result = new int[ bins ];
        float scale = ( ( float ) ( max - min + 1 ) ) / ( ( float ) bins );
        
        for (int i=0;i<array.length;i++) {
            if ( array[i] < min ) {
                result[0]++; 
            }
            else if ( array[i] > max ) {
                result[bins-1]++;  
            }
            else {
                float tmp = ( ( array[i] - min ) * scale ) ;
                result[ ( int ) Math.floor( tmp )]++ ;
            } 
        }
        
        return result ;
    }    
    
    /**
     *  Make a histogram transform consisting of a set of floating pt.
     *  numbers such that the number of elements within the new bins are 
     *  approximately equal.
     *  
     *  <p> The result consists of an array h of size bins, where each bin
     *  i is associated with an interval ( h[i+1], h[i] ). Then, a value
     *  g(m,n) is mapped into g'(m,n) simply by performing a lookup and
     *  and interpolating.
     * 
     */
    public static int[] histNorm( int[] hist, int bins ) {
        int[] result = new int[bins] ;
        int total = 0 ;
        
        for (int i=0;i<hist.length;i++) {
            total += hist[i] ;
        }
        
        int mark = 0;
        float accum = 0, prev ;
        float step = total / bins ;
        
        for (int i=0;i<hist.length;i++) {
            prev = accum ;
            accum += hist[i] ;
            int prevh = ( int ) ( prev / step ) ;
            int accumh = ( int ) ( accum / step ) ;
            
            if ( accumh >= bins ) {
                accumh = bins - 1;
            }

            if ( accumh > prevh ) {
               // Map the bins between mark and i to correspond to
               // the bin associated with accumh
               for (int j=mark+1;j<=i;j++) {
                  result[j] = accumh ;
               }
               mark = i ;
            }
        
            // Fill in the rest of the table if there are any left
            if ( i == hist.length - 1 ) {
                for (int j=mark+1;j<hist.length;j++) {
                    result[j] = accumh ;
                } 
            }
        }
                
        return result ;
    }
    
    /**
     *  Perform a histogram transform.
     *
     *  @param array Input array
     *  @param target Output array after transform
     *  @param table   A table mapping input values to target values
     */
    public static void histTrans( int[] array, int[] target, 
                                  int[] table ) 
    {
        int k = 0 ;

        for ( int i = 0 ; i < array.length ; i++ ) {
            int hvalue = table[ array[i] ];
            target[i] = hvalue ;
        }
    }

    /**
     *  Does some interpolation.
     */
    public static void histTrans( float[] array, int[] target, int[] table ) {
        int k = 0 ;

        for ( int i = 0 ; i < array.length ; i++ ) {
            int nearest = ( int ) Math.round( array[i] ) ;
            int hvalue = table[ nearest ];
            if ( nearest < array[i] && nearest < table.length - 1 ) {
                int upper = table[nearest +1 ];
                if ( upper > hvalue + 1 ) {
                    hvalue = (int) ( hvalue + (( float ) ( upper - hvalue )) * ( array[i] - nearest ) );
                }
            }
            else if ( nearest > array[i] && nearest > 0) {
                int lower = table[nearest-1];
                if ( lower < hvalue - 1 ) {
                    hvalue = (int) ( hvalue - (( float ) ( hvalue - lower )) * ( nearest - array[i] ) ) ;
                }
            }

            if ( hvalue > table[ table.length - 1] )
                hvalue = table[ table.length - 1] - 1;

            target[i] = hvalue ;
        }

    }

    public static void main( String[] args ) {
        com.axiom.lib.mat.MatEng eng = com.axiom.lib.mat.MatEng.getInstance() ;
        
    }
    
}