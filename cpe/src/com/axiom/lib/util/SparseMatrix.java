package com.axiom.lib.util ;

class SparseMatrix implements java.io.Serializable {
    public SparseMatrix( int numRows, int numColumns ) {
        if ( numRows > 0 && numColumns > 0 ) {
            values = new Object[numRows][mr] ;
            indices = new int[numRows][mr];
            size = new int[numRows] ;
            this.numColumns = numColumns ;
        }
        else {
            throw new IllegalArgumentException() ;
        }
    }

    public int getNumRowItems( int rowIndex ) {
        return size[rowIndex] ;
    }

    public Object getRowItem( int row, int indx ) {
        if ( row >= 0 && row < values.length && indx < size[row] ) {
            return values[row][indx] ;
        }
        else {
            throw new IllegalArgumentException( "Row=" + row + ",indx=" + indx + " out of bounds." ) ;
        }
    }

    public Object get( int i, int j ) {
        if ( i >=  values.length || j > numColumns ) {
            throw new IllegalArgumentException( "Array out of bounds.") ;
        }
        for (int k=0;k<size[i];k++) {
            if ( indices[i][k] == j ) {
                return values[i][k] ;
            }
        }
        return null ;
    }

    public void set( int i, int j, Object value ) {
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
            Object[] tmp = new Object[ (int) ( values[i].length * sfactor ) ] ;
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

    private float sfactor = 1.6f ;
    private int mr = 4 ;
    int numColumns ;
    Object[][] values ;
    int[][] indices ;
    int[] size ;
}
