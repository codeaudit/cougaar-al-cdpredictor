package com.axiom.escript ;

public class DoubleConstantInfo extends ConstantInfo {

    public DoubleConstantInfo( double value ) {
       super( ConstantInfo.CONSTANT_DOUBLE ) ;
       this.value = value ;
    }

    double getValue() { return value ; }

    double value ;
}