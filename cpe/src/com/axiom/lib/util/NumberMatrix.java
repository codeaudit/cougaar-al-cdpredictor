package com.axiom.lib.util ;

public interface NumberMatrix {

    public int getRank() ;

    public int[] getSize() ;

    public Class getType() ;

    /**
     *  Get a subrange of this number matrix. lower.length and
     *  upper.length < getRank(). If any dimensions exceed size,
     *  a MatrixException is thrown.
     */
    // public NumberMatrix get( int[] lower, int[] upper ) ;
}