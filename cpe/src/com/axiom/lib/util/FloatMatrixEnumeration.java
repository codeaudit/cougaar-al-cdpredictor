package com.axiom.lib.util ;

public class FloatMatrixEnumeration {

    FloatMatrixEnumeration( FloatMatrix matrix ) {
        this.matrix = matrix ;
        lower = new int[matrix.dimensions.length] ;  // Zero array
        upper = ( int[] ) matrix.dimensions.clone() ; // Clone upper array ;
        for (int i=0;i<upper.length;i++)
            upper[i] = upper[i] - 1 ;
        index = ( int[] )lower.clone();
    }

    /**
     *  Constructor for enumeration over the lower to upper indices, inclusive.
     *  The lower and upper bounds are adjusted so that they fall within the matrix.
     */

    FloatMatrixEnumeration( FloatMatrix matrix,  int[] lower, int[] upper ) {
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
        lindex = matrix.linearIndex( index ) ;
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

    public float at() {
        return matrix.getArray()[lindex];
    }

    public void set( float value ) {
        matrix.getArray()[lindex] = value ;
    }

    /** Returns current element and increments to next element.
     */
    public float nextElement() {
        float res = matrix.getArray()[lindex];

        // Throw exception if no next element exists.
        if ( index[0] > upper[0] )
           throw new java.util.NoSuchElementException() ;

        try {
        if ( index[0] < upper[0] ) {
            index[0] = index[0] + 1;
            lindex ++ ;
            return res ;
        }
        else {
            index[0] = lower[0];
        }

        for (int i=1;i< index.length ;i++) {
            if ( index[i] < upper[i] ) {
                index[i] += 1;
                // Convert index into linear index
                lindex = matrix.linearIndex( index ) ;
                return res;
            }
            else
                index[i] = lower[i];
        }

        // This signals that there are no more elements in this enumeration
        index[0] = upper[0]+1 ;
        return res ;
        }
        catch ( RuntimeException e ) {
            e.printStackTrace();
            throw e ;
        }
    }

    protected FloatMatrix matrix ;

    /**
     *  Linear index.
     */
    protected int lindex ;

    /**
     *  Array index.
     */
    protected int[] index ;

    /**
     *  Lower and upper indices of matrix being enumerated.
     */
    protected int[] lower, upper ;
}