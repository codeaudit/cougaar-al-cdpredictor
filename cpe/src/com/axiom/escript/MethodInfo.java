package com.axiom.escript ;
import java.io.DataInput ;

public class MethodInfo {

    public void read( ClassObject classObject, DataInput ds ) throws java.io.IOException {
        accessFlags =  ds.readUnsignedShort();
        nameIndex = ds.readUnsignedShort() ;
        ConstantInfo cinfo = classObject.resolveConstant( nameIndex ) ;
        descriptorIndex = ds.readUnsignedShort() ;
        int attribCount = ds.readUnsignedShort() ;

        attributes = new AttributeInfo[ attribCount ] ;
        for (int i=0;i<attribCount;i++) {
            attributes[i] = readAttribute( classObject, ds ) ;
        }
    }

    protected AttributeInfo readAttribute( ClassObject classObject, DataInput ds ) throws java.io.IOException {

        int attributeNameIndex = ds.readUnsignedShort() ;
        long attributeLength = ClassReaderUtils.readU4(ds) ;

        ConstantInfo cinfo = classObject.resolveConstant( attributeNameIndex ) ;

        AttributeInfo attribInfo = null ;
        if ( cinfo instanceof UTF8ConstantInfo ) {
            String svalue = ( ( UTF8ConstantInfo ) cinfo ).getValue() ;

            if ( svalue.equals( "Code" )  ) {
               // Read a code attribute
               CodeAttribute code = new CodeAttribute() ;
               code.read( classObject, ds ) ;
               this.code = code ;
               attribInfo = code ;
            }
            else if ( svalue.equals( "Exceptions" ) ) {
               ExceptionsAttribute exceptions = new ExceptionsAttribute() ;
               exceptions.read( classObject, ds ) ;
               attribInfo = exceptions ;
               this.exceptions = exceptions ;
            }
            else
                ds.skipBytes( (int) attributeLength ) ;
        }

        return attribInfo ;
    }

    //Signature

    int accessFlags ;
    int nameIndex ;
    int descriptorIndex ;
    CodeAttribute code ;
    ExceptionsAttribute exceptions ;

    /** All attributes. */
    AttributeInfo[] attributes ;
}