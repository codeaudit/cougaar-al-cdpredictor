package com.axiom.escript ;

public class StringConstantInfo extends ConstantInfo {

    public StringConstantInfo( int stringIndex ) {
        super( ConstantInfo.CONSTANT_STRING ) ;
        this.stringIndex = stringIndex ;
    }

    public int getStringIndex() {
        return stringIndex ;
    }

    int stringIndex ;
}