package com.axiom.lib.util ;

public class DoubleArray implements NumberArray {
    public DoubleArray( NumberArray a ) {
        array = new double[a.getSize()];
        for (int i=0;i<a.getSize();i++) {
            array[i] = a.valueAt(i) ;
        }
    }

    public String toString() {
        return "[DoubleArray " + ArrayMath.toString( array ) + "]" ;
    }

    public DoubleArray( double[] array ) {  this.array = array ; }

    public Class getType() { return Double.TYPE ; }

    public int getSize() { return array.length ; }

    public int intAt( int i ) { return ( int ) array[i] ; }
    
    public double valueAt( int i ) { return array[i] ; }

    public float floatAt( int i ) { return ( float ) array[i] ; }

    public void set( int i, double val ) {
        array[i] = val ;
    }
    
    protected double[] array ;

    static final long serialVersionUID = 3824983614602454528L;
}