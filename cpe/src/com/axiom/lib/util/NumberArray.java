package com.axiom.lib.util ;

/**
 *  Uniform interface to numerical array.
 */
public interface NumberArray extends java.io.Serializable {

    public int getSize() ;

    public int intAt( int i ) ;

    // public long longAt( int i ) ;

    // public byte byteAt( int i ) ;

    // public short shortAt( int i ) ;

    /** Returns the underlying primitive storage type.  Must be one of
     *  Integer.TYPE, Float.TYPE, Short.TYPE, Long.TYPE, Double.TYPE,
     *  or Byte.TYPE.
     */
    public Class getType() ;

    public double valueAt( int i ) ;

    public float floatAt( int i ) ;

    public void set( int index, double value ) ;
}