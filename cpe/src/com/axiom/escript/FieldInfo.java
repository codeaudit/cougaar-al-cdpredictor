package com.axiom.escript ;

public class FieldInfo {

    public void read( ClassObject classObject, java.io.DataInput di ) throws java.io.IOException {
        accessFlags = di.readUnsignedShort() ;
        nameIndex = di.readUnsignedShort() ;
        descriptorIndex = di.readUnsignedShort() ;
        int attributesCount = di.readUnsignedShort() ;
        AttributeInfo[] tmp = new AttributeInfo[ attributesCount ];

        int count = 0 ;
        for (int i=0;i<attributesCount;i++) {
            AttributeInfo info = readAttributeInfo( classObject, di ) ;
            if ( info != null ) {
                tmp[count++] = info ;
            }
        }

        attributes = new AttributeInfo[count];
        System.arraycopy( tmp, 0, attributes, 0, count ) ;
    }

    protected AttributeInfo readAttributeInfo( ClassObject classObject, java.io.DataInput di ) throws java.io.IOException {

        int attributeNameIndex = di.readUnsignedShort() ;
        long attributeLength = ( di.readUnsignedShort() << 16 ) + di.readUnsignedShort() ;

        // See if the attribute name index is correct
        ConstantInfo info = classObject.resolveConstant( attributeNameIndex ) ;

        AttributeInfo attribinfo = null ;

        if ( info instanceof UTF8ConstantInfo ) {
            UTF8ConstantInfo uinfo = ( UTF8ConstantInfo ) info ;

            if ( uinfo.stringValue.equals("ConstantValue") ) {
                int index = di.readUnsignedShort() ;
                attribinfo = new ConstantValue( index ) ;
            }
            else { // Unrecognized
                di.skipBytes( (int) attributeLength ) ;
            }
        }
        else { // Whoa, this ought to be a UTF8ConstantInfo!
            di.skipBytes( (int) attributeLength ) ;
        }

        if ( attribinfo != null ) {
            attribinfo.attributeNameIndex = attributeNameIndex ;
        }
        return attribinfo ;
    }

    int accessFlags ;
    int nameIndex ;
    int descriptorIndex ;
    AttributeInfo[] attributes ;

}