package com.axiom.lib.util ;

public class ShortArray implements NumberArray {
    public ShortArray( short[] array ) {  this.array = array ; }
    
    public int getSize() { return array.length ; }

    public int intAt( int i ) { return (int) array[i] ; }
    
    public double valueAt( int i ) { return array[i] ; }

    public float floatAt( int i ) { return array[i] ; }

    public Class getType() { return Short.TYPE ; }

    public void set( int i, double value ) { array[i] = ( short ) value ; }

    protected short[] array ;

    static final long serialVersionUID = 268799654374575611L;
}