package org.cougaar.tools.castellan.ldm;

import java.util.*;
import java.io.*;

/**
 * A time-ordered series of PDUs as a serialized byte-stream.  May be compressed using ZIP compression.
 */
public class BatchMessage extends LogMessage
{
    public static final int SERIALIZED = 0 ;
    public static final int FAST_SERIALIZED = 1 ;

    public BatchMessage(byte[] messages, int type, boolean compressed)
    {
        this.messages = messages;
        if ( type != SERIALIZED && type != FAST_SERIALIZED ) {
            throw new IllegalArgumentException( "Illegal type " + type + " found." ) ;
        }

        this.type = type;
        isCompressed = compressed;
    }

    public class PDUIterator implements Iterator {

        public PDUIterator() {
            try {
            bis = new ByteArrayInputStream( messages ) ;
            ois = new ObjectInputStream( bis ) ;
            }
            catch ( StreamCorruptedException e ) {
                e.printStackTrace();
            }
            catch ( IOException e ) {
                e.printStackTrace();
            }
        }

        public boolean hasNext()
        {
            if ( ois == null ) {
                return false ;
            }
            int available ;
            //try {
                available = bis.available() ;
            //}
            //catch ( IOException e ) {
            //    return false ;
            //}
            return available > 0 ;
        }

        public Object next()
        {
            try {
            Object o = ois.readObject() ;
            return o ;
            }
            catch ( Exception e ) {
                e.printStackTrace();
                throw new RuntimeException( "Error iterating through batch message." ) ;
            }
        }

        public void remove()
        {
            throw new UnsupportedOperationException( "Cannot remove from stream." ) ;
        }

        ByteArrayInputStream bis ;
        ObjectInputStream ois ;
    }

    /**
     * A collection of PDUs
     */
    public Iterator getIterator() {
        return new PDUIterator() ;
    }

    public byte[] getByteArray() { return messages ; }

    /**
     * Whether the byte stream is compressed using zipstream compression.
     */
    public boolean isCompressed()
    {
        return isCompressed;
    }

    /**
     * Either serialized or fast serialized.
     */
    public int getBatchType()
    {
        return type;
    }

    private byte[] messages ;
    private int type = -1 ;
    private boolean isCompressed ;
}
