package org.cougaar.tools.castellan.pdu;

import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.core.util.UID;
import org.cougaar.tools.castellan.util.SymbolTable;

import java.util.HashMap;
import java.io.*;

public class PDUOutputBuffer implements java.io.Serializable {

    /**
    *  Each task record consists of:
    *  <li> byte encoding type and action
    *  <li> my UID (6 bytes)
    *  <li> verb symbol id (2 bytes)
    *  <li> parent task UID (6 bytes)
    *  <li> direct object UID (6 bytes)
    *  <li> aggregation level byte (2, 6, etc.?) TODO
    *  <li> system time (offset from baseTime)  (4 bytes)
    *  <li> execution time (offset from baseExecution) (4 bytes)
     *
     * @param task
     * @param action
     * @param time
     * @param executionTime
     * @return true if the task was successfully written, false if the task cannot be written into this buffer.
     *    Handle this by opening a new PDUBuffer.
     */
    public synchronized boolean addTask( Task task, int action, long time, long executionTime ) {
        if ( closed ) {
            throw new RuntimeException( "PDU Buffer is already closed.") ;
        }
        createTaskStream();
        Asset asset = task.getDirectObject() ;
        UID uid = task.getUID() ;

        int count = 0 ;
        try {
            // Write the type and action as the high 4 and low 4 types of a byte, respectively.
            outputStream.writeByte( ( UniqueObjectPDU.TYPE_TASK << 4 | action ) & 0xFF );
            count++ ;

            // Write the action type.
            // outputStream.writeByte( action ); count++ ;

            // Write my UID
            count += writeUID( uid, outputStream );

            // Write the verb
            outputStream.writeShort( getSymbolId( task.getVerb().toString() ));
            count+= 2 ;

            // Write the parent UID.
            count += writeUID( task.getParentTaskUID(), outputStream ) ;

            // Write the assetUID
            if ( asset != null ) {
                count+= writeUID( asset.getUID(), outputStream ) ;
            }
            else {
                count += writeUID( null, outputStream ) ;
            }

            // Write system time
            outputStream.writeInt( getOffsetFromBaseSystemTime( time )) ; count += 4 ;
            // Write execution time.
            outputStream.writeInt( getOffsetFromBaseExecutionTime( executionTime )); count+= 4 ;
            taskCount++ ;

            if ( count != taskRecordLength ) {
                throw new RuntimeException( "Record length " + taskRecordLength + " does not match number of bytes written " + count ) ;
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
        return true;
    }

    public PDU getNextPDU() {
        if (!closed) {
            throw new RuntimeException( "PDU buffer is not closed." ) ;
        }
        if ( buf == null ) {
            throw new RuntimeException( "Buffer is empty.") ;
        }

        try {
            if ( inputBuffer == null ) {
                inputBuffer = new ByteArrayInputStream( buf ) ;
                inputStream = new ObjectInputStream( inputBuffer ) ;
                if ( inputStream.available() == 0 ) {
                    throw new IllegalArgumentException( "No more PDUs.") ;
                }
            }
        }
        catch ( IOException e ) {
            e.printStackTrace();
        }


        return null ;
    }


    protected int writeUID( UID uid, ObjectOutputStream oos ) throws IOException {
        if ( uid != null ) {
            oos.writeShort( getSymbolId( uid.getOwner() ) );
            oos.writeInt( getOffsetFromBaseUID( uid.getId() ));
        }
        else {
            oos.writeShort( ( short ) -1 ); // Just write -1 to signify nullness.
            oos.writeInt( 0 ) ; // Ignored
        }
        return 6 ;
    }

    protected UIDStringPDU readUID( ObjectInputStream ois ) throws IOException {
        UID result = null ;
        short s = ois.readShort() ;
        int idOffset = ois.readInt() ;
        if ( s < 0 ) {
            return null ;
        }
        else {
            return new UIDStringPDU( getSymbol( s ), baseUID + idOffset ) ;
        }
    }

    public int getOffsetFromBaseSystemTime( long time ) {
        if ( baseTime == Long.MIN_VALUE ) {
            baseTime = time ;
            return 0 ;
        }
        long value = time - baseTime ;
        if ( value > Integer.MAX_VALUE || value < Integer.MIN_VALUE ) {
            throw new IllegalArgumentException( value + " exceeds integer range.") ;
        }
        return ( int ) value ;
    }


    public int getOffsetFromBaseExecutionTime( long time ) {
        if ( baseExecution == Long.MIN_VALUE ) {
            baseExecution = time ;
            return 0 ;
        }
        long value = time - baseExecution ;
        if ( value > Integer.MAX_VALUE || value < Integer.MIN_VALUE ) {
            throw new IllegalArgumentException( value + " exceeds integer range.") ;
        }
        return ( int ) value ;
    }

    public int getOffsetFromBaseUID( long id) {
       if ( baseUID == Long.MIN_VALUE ) {
           baseUID = id ;
       }
       long value = id - baseUID ;
        if ( value > Integer.MAX_VALUE || value < Integer.MIN_VALUE ) {
            throw new IllegalArgumentException( id + " exceeds integer range." ) ;
        }
        return ( int ) value ;
    }

    public String getSymbol( short id ) {
        String result = symbolTable.resolveId( id ) ;
        if ( result == null ) {
            throw new IllegalArgumentException( id + " symbol id is not found." ) ;
        }
        return result ;
    }

    public short getSymbolId( String sym ) {
        int id = symbolTable.resolveSymbol( sym) ;
        if ( id == -1 ) {
            id = symbolTable.addSymbol( sym ) ;
        }

        if ( id > Short.MAX_VALUE ) {
            throw new RuntimeException( "Exceeded max number of symbols.") ;
        }
        return ( short ) id ;
    }

    public synchronized void close() {
        // Retreive the byte array.
        buf = outputBuffer.getByteArray() ;
        closed = true ;
    }

    public boolean isClosed() {
        return closed;
    }

    private void createTaskStream() {
        if ( outputStream == null ) {
            try {
            outputStream = new ObjectOutputStream( outputBuffer ) ;
            }
            catch ( IOException e ) {
               e.printStackTrace();
            }
        }
    }

    private void writeObject( ObjectOutputStream oos ) throws IOException {
        if ( !closed ) {
            closed = true ;
        }
        oos.defaultWriteObject();
    }

    private void readObject( ObjectInputStream ois ) throws java.io.IOException, ClassNotFoundException {
        ois.defaultReadObject();
    }


    /**
     * Default buffer size is 50000 bytes.
     */
    protected long bufferSize = 50000 ;
    protected long baseTime = Long.MIN_VALUE;
    protected long baseExecution = Long.MIN_VALUE;
    protected long baseUID = Long.MIN_VALUE ;
    protected boolean closed = false ;
    protected long offset = 0 ;

    public static final int taskRecordLength = 1 + 6 + 2 + 6 + 6 + 4 + 4 ;
    protected SymbolTable symbolTable = new SymbolTable() ;

    transient MyByteArrayOutputStream outputBuffer = new MyByteArrayOutputStream( 50000 ) ;
    transient ObjectOutputStream outputStream ;
    transient ByteArrayInputStream inputBuffer ;
    transient ObjectInputStream inputStream ;
    int taskCount = 0 ;

    /**
     * The internal buffer of PDUs.
     */
    protected byte[] buf ;

    class MyByteArrayOutputStream extends ByteArrayOutputStream {
        public MyByteArrayOutputStream( int size ) {
            super( size ) ;
        }

        public MyByteArrayOutputStream() {
        }

        public byte[] getByteArray() {
            return buf ;
        }
    }

}
