package com.axiom.lib.util ;

public class IntArray implements NumberArray {
    
    public IntArray( int[] array ) {
        this.array = array ;
    }

    public Class getType() { return Integer.TYPE ; }
    
    public int getSize() { return array.length ; }

    public int intAt( int i ) { return array[i] ; }
    
    public double valueAt( int i ) { return array[i] ; }

    public float floatAt( int i ) { return array[i] ; }

    public void set( int i, double value ) { array[i] = ( int ) value ; }
    
    protected int[] array ;

    static final long serialVersionUID = -180230690654265670L;
}