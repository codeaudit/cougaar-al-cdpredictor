package com.axiom.lib.util ;


/**
 *  Simple implemenation of sparse matrix.  All non-instanced elements
 *  can be assumed to be zero or ``null", depending on context.
 */
public class SparseFloatMatrix implements java.io.Serializable {
    public SparseFloatMatrix( int numRows, int numColumns ) {
        if ( numRows > 0 && numColumns > 0 ) {
            values = new float[numRows][mr] ;
            indices = new int[numRows][mr];
            size = new int[numRows] ;
            this.numColumns = numColumns ;
        }
        else {
            throw new IllegalArgumentException() ;
        }
    }

    public int getNumColumns() { return numColumns ; }

    public int getNumRows() { return values.length; } 

    public int getNumRowItems( int rowIndex ) {
        return size[rowIndex] ;
    }

    public float getRowItem( int row, int indx ) {
        if ( row >= 0 && row < values.length && indx < size[row] ) {
            return values[row][indx] ;
        }
        else {
            throw new IllegalArgumentException( "Row=" + row + ",indx=" + indx + " out of bounds." ) ;
        }
    }

    public boolean exists( int i, int j ) {
        if ( i >=  values.length || j > numColumns ) {
            throw new IllegalArgumentException( "Array out of bounds.") ;
        }
        for (int k=0;k<size[i];k++) {
            if ( indices[i][k] == j ) {
                return true ;
            }
        }
        return false ;

    }

    public void delete( int i, int j ) {
        if ( i >=  values.length || j > numColumns ) {
            throw new IllegalArgumentException( "Array out of bounds.") ;
        }
        int[] row = indices[i] ;
        for (int k=0;k<size[i];k++) {
            if ( row[k] == j ) {
                for (int ind=k;ind<size[i]-1;ind++) {
                    row[ind] = row[ind+1] ;
                }
                size[i]--;
                return ;
            }
        }
    }

    public float get( int i, int j ) {
        if ( i >=  values.length || j > numColumns ) {
            throw new IllegalArgumentException( "Array out of bounds.") ;
        }
        for (int k=0;k<size[i];k++) {
            if ( indices[i][k] == j ) {
                return values[i][k] ;
            }
        }
        return 0 ;
    }

    public void set( int i, int j, float value ) {
        if ( i >=  values.length || j > numColumns ) {
            throw new IllegalArgumentException( "Array out of bounds.") ;
        }
        for (int k=0;k<size[i];k++) {
            if ( indices[i][k] == j ) {
                values[i][k] = value;
                return ;
            }
        }
        if ( size[i] == values[i].length ) {
            float[] tmp = new float[ (int) ( values[i].length * sfactor ) ] ;
            int[] tmpi = new int[ (int) ( values[i].length * sfactor ) ] ;
            System.arraycopy( values[i], 0, tmp, 0, values[i].length ) ;
            System.arraycopy( indices[i], 0, tmpi, 0, indices[i].length ) ;
            values[i] = tmp ;
            indices[i] = tmpi ;
        }
        values[i][size[i]] = value ;
        indices[i][size[i]] = j ;
        size[i]++ ;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer() ;
        for (int i=0;i<values.length;i++) {
            for (int j=0;j<size[i];j++) {
                buf.append( "("+i+","+indices[i][j]+")="+values[i][j] ) ;
                buf.append( '\n' ) ;
            }
        }
        return buf.toString() ;
    }

    public static final void main( String[] args ) {
        SparseFloatMatrix sm = new SparseFloatMatrix( 10, 10 ) ;
        sm.set( 0, 0, 1 ) ;
        sm.set( 0, 1, 2 ) ;
        sm.set( 0, 2, 3 ) ;
        sm.set( 0, 3, 4 ) ;
        sm.set( 0, 5, 5 ) ;
        sm.set( 3, 3, 2 ) ;
        sm.set( 5, 2, 3 ) ;
        System.out.println( sm.toString() ) ;
        System.out.println( "sm(3,3)="+sm.get(3,3) ) ;
        System.out.println( "sm(4,5)="+sm.get(4,5) ) ;
    }

    private float sfactor = 1.6f ;
    private int mr = 4 ;
    int numColumns ;
    float[][] values ;
    int[][] indices ;
    int[] size ;

    protected static final long serialVersionUID=2837733988647002302L ;
}

