package com.axiom.escript ;

public class StringValueInfo extends ConstantInfo {

    public StringValueInfo( String stringValue ) {
       super( ConstantInfo.CONSTANT_UTF8 ) ;
       this.stringValue = stringValue ;
    }

    public String getString() { return stringValue ; }

    String stringValue ;
}