package com.axiom.escript ;

public abstract class ConstantInfo {

    public static final byte CONSTANT_CLASS  =  7 ;

    public static final byte CONSTANT_FIELDREF = 9 ;

    public static final byte CONSTANT_METHODREF = 10 ;

    public static final byte CONSTANT_INTERFACEMETHODREF = 11 ;

    public static final byte CONSTANT_STRING = 8 ;

    public static final byte CONSTANT_INTEGER = 3 ;

    public static final byte CONSTANT_FLOAT = 4 ;

    public static final byte CONSTANT_LONG = 5 ;

    public static final byte CONSTANT_DOUBLE = 6 ;

    public static final byte CONSTANT_NAMEANDTYPE = 12 ;

    public static final byte CONSTANT_UTF8 = 1 ;

    public ConstantInfo( int tag ) {
        this.tag = tag ;
    }

    // public abstract void write( java.io.DataOutput dos ) ;

    /**
     *  Constant tag.  One of the CONSTANT_ constants.
     */
    int tag ;
}