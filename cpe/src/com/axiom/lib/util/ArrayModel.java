package com.axiom.lib.util ;

/**
 *   An interface for facades over complex aggregates that need to
 *   provide access to arrays of objects.
 */

public interface ArrayModel {

    /**
     *  @throws IllegalArgumentException If the value array is not valid.
     */
    public void copyFrom( Object[] value ) throws IllegalArgumentException ;

    public void copyTo( Object[] value ) throws IllegalArgumentException ;

    public int getSize() ;
}