package com.axiom.escript ;
import java.lang.reflect.* ;

public class FieldRefConstantInfo extends RefConstantInfo {
    public FieldRefConstantInfo( int classIndex, int nameAndTypeIndex ) {
        super( CONSTANT_FIELDREF, classIndex, nameAndTypeIndex );
    }

    public void resolve() {
        super.resolve() ;
    }

    public Field getField() {
        return null ;
    }

}