package com.axiom.escript ;
import java.io.* ;

public class InnerClassesAttribute extends AttributeInfo {

    public static class InnerClassAttribute {
        public int innerClassInfoIndex ;
        public int outerClassInfoIndex ;
        public int innerNameIndex ;
        public int innerClassAccessFlags ;
    }

    public void read( ClassObject classObject, DataInput ds )
    throws java.io.IOException
    {
        int numberOfClasses = ds.readUnsignedShort() ;
        innerClasses = new InnerClassAttribute[numberOfClasses] ;
        for (int i=0;i<numberOfClasses;i++) {
            InnerClassAttribute ia = new InnerClassAttribute() ;
            innerClasses[i] = ia ;
            ia.innerClassInfoIndex = ds.readUnsignedShort() ;
            ia.outerClassInfoIndex = ds.readUnsignedShort() ;
            ia.innerNameIndex = ds.readUnsignedShort() ;
            ia.innerClassAccessFlags = ds.readUnsignedShort() ;
        }
    }

    InnerClassAttribute[] innerClasses ;
}