package com.axiom.lib.util ;

/**
 *   An interface for facades over complex aggregates that need to
 *   provide access as arrays of double or integer parameters.
 */
public interface NumberArrayModel {

    /**
     *  @throws IllegalArgumentException If the value array is not valid.
     */
    public void copyFrom( int[] value ) throws IllegalArgumentException ;

    public void copyTo( int[] value ) throws IllegalArgumentException ;

    public void copyFrom( double[] value ) throws IllegalArgumentException ;

    public void copyTo( double[] value ) throws IllegalArgumentException ;

    public int getSize() ;
}