package com.axiom.escript ;

public class NameAndTypeInfo extends ConstantInfo {

    public NameAndTypeInfo( int nameIndex, int typeIndex ) {
        super( ConstantInfo.CONSTANT_NAMEANDTYPE ) ;
       this.nameIndex = nameIndex ;
       this.typeIndex = typeIndex ;
    }

    public int getNameIndex() {
        return nameIndex ;
    }

    public int getTypeIndex() {
        return typeIndex ;
    }

    int nameIndex ;
    int typeIndex ;
}