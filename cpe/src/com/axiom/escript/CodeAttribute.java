package com.axiom.escript ;
import java.io.DataInput ;

public class CodeAttribute extends AttributeInfo {
    public static class ExceptionTableEntry {
        int startpc ;
        int endpc ;
        int handlerpc ;
        int catchtype ;
    }

    public void read( ClassObject classObject, DataInput ds ) throws java.io.IOException {
        maxStack = ds.readUnsignedShort() ;
        maxLocals = ds.readUnsignedShort() ;
        codeLength = ClassReaderUtils.readU4( ds ) ;

        // Skip the code bytes for now.
        //ds.skipBytes( (int) codeLength ) ;
        code = new byte[ (int) codeLength ];
        for (int i=0;i<codeLength;i++) {
            code[i] = ds.readByte() ;
        }
        
        int exceptionTableLength = ds.readUnsignedShort() ;
        exceptionTable = new ExceptionTableEntry[exceptionTableLength];
        for (int i=0;i<exceptionTableLength;i++) {
           ExceptionTableEntry entry = new ExceptionTableEntry() ;
           entry.startpc = ds.readUnsignedShort() ;
           entry.endpc = ds.readUnsignedShort() ;
           entry.handlerpc = ds.readUnsignedShort() ;
           entry.catchtype = ds.readUnsignedShort() ;
           exceptionTable[i] = entry ;
        }

        int attributeCount = ds.readUnsignedShort() ;
        attributes = new AttributeInfo[attributeCount];
        for (int i=0;i<attributeCount;i++) {
            int attributeNameIndex = ds.readUnsignedShort() ;
            long attributeLength = ClassReaderUtils.readU4( ds ) ;
            ConstantInfo cinfo = classObject.resolveConstant( attributeNameIndex ) ;
            if ( cinfo != null && cinfo instanceof UTF8ConstantInfo ) {
               String value = ((UTF8ConstantInfo) cinfo).getValue() ;
               if ( value.equals("LineNumberTable") ) {
                    ds.skipBytes( (int) attributeLength ) ;
               }
               else if ( value.equals("LocalVariableTable") ) {
                    ds.skipBytes( (int) attributeLength ) ;
               }
               else {
                    ds.skipBytes( (int) attributeLength ) ;
               }
            }
        }
    }

    int maxStack ;
    int maxLocals ;
    long codeLength ;
    byte[] code ;
    ExceptionTableEntry[] exceptionTable ;
    AttributeInfo[] attributes ;
}
