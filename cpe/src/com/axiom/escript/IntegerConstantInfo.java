package com.axiom.escript ;

public class IntegerConstantInfo extends ConstantInfo {
    public IntegerConstantInfo( int value ) {
        super( ConstantInfo.CONSTANT_INTEGER ) ;
        this.value = value ;
    }

    public int getValue() {
        return value ;
    }

    int value ;
}