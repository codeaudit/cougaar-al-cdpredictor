package com.axiom.escript ;

public class RefConstantInfo extends ConstantInfo {

    public RefConstantInfo( int tag, int classIndex, int nameAndTypeIndex ) {
        super( tag ) ;
        this.classIndex = classIndex ;
        this.nameAndTypeIndex = nameAndTypeIndex ;
    }

    public void resolve() {

    }

    public ClassObject getClassObject() {
        return null ;
    }

    int classIndex ;
    int nameAndTypeIndex ;
}