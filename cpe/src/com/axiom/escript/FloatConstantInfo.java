package com.axiom.escript ;

public class FloatConstantInfo extends ConstantInfo{

    public FloatConstantInfo( float value ) {
        super( ConstantInfo.CONSTANT_FLOAT ) ;
        this.value = value ;
    }

    public float getValue() {
        return value ;
    }
    
    float value ;
}