package com.axiom.escript ;

public class ExceptionsAttribute extends AttributeInfo {

    public void read( ClassObject classObject, java.io.DataInput di ) throws java.io.IOException {
        int numberOfExceptions = di.readUnsignedShort() ;
        exceptionIndexTable = new int[numberOfExceptions];
        for (int i=0;i<numberOfExceptions;i++) {
            int value = di.readUnsignedShort() ;
            exceptionIndexTable[i] = value ;
        }
    }

    int[] exceptionIndexTable ;
}