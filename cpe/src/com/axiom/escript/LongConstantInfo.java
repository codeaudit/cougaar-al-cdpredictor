package com.axiom.escript ;

public class LongConstantInfo extends ConstantInfo {

    public LongConstantInfo( long value ) {
        super( ConstantInfo.CONSTANT_LONG ) ;
        this.value = value ;
    }

    public long getValue() {
        return value ;
    }

    long value ;
}