package com.axiom.escript ;

public class UTF8ConstantInfo extends ConstantInfo {

    public UTF8ConstantInfo( String stringValue ) {
       super( ConstantInfo.CONSTANT_UTF8 ) ;
       this.stringValue = stringValue ;
    }

    public String getValue() { return stringValue ; }

    String stringValue ;
}