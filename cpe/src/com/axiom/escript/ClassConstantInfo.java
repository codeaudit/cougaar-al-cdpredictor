package com.axiom.escript ;

public class ClassConstantInfo extends ConstantInfo {
    public ClassConstantInfo( int index ) {
        super( ConstantInfo.CONSTANT_CLASS ) ;
        this.index = index ;
    }

    public int getNameIndex() {
        return index ;
    }

    /** Index to UTF8ConstantInfo which represents a valid class name.
     */
    int index ;
}