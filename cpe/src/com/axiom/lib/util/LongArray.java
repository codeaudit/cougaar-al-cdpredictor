package com.axiom.lib.util ;

public class LongArray implements NumberArray {

    public String toString() {
        return "[LongArray " + ArrayMath.toString( array ) +"]";
    }

    public LongArray( long[] array ) {  this.array = array ; }
    
    public int getSize() { return array.length ; }

    public int intAt( int i ) { return (int) array[i] ; }
    
    public double valueAt( int i ) { return array[i] ; }

    public float floatAt( int i ) { return array[i] ; }

    public Class getType() { return Long.TYPE ; }

    public void set( int i, double value ) { array[i] = ( short ) value ; }

    protected long[] array ;

    static final long serialVersionUID = 268799654374575611L;
}