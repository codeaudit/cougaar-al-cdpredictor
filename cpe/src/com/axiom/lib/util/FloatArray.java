package com.axiom.lib.util ;

public class FloatArray implements NumberArray {
    public FloatArray( float[] array ) {  this.array = array ; }

    public Class getType() { return Float.TYPE ; }
    
    public int getSize() { return array.length ; }

    public int intAt( int i ) { return (int) array[i] ; }
    
    public double valueAt( int i ) { return array[i] ; }

    public float floatAt( int i ) { return array[i] ; }

    public void set( int i, double value ) { array[i] = ( float ) value ; }

    protected float[] array ;

    static final long serialVersionUID = 268799654374575611L;
}