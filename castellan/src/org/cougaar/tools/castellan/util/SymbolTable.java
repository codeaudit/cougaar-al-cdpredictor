package org.cougaar.tools.castellan.util;

import java.util.*;
import java.io.*;

/** Each client will maintain a symbol table.
 */
public class SymbolTable implements java.io.Serializable
{
    public SymbolTable()
    {
    }

    public synchronized void clear()
    {
        idToSymbolTable.clear();
        symbolToIdTable.clear();
    }

    public final int resolveSymbol( String symbol )
    {
        if ( symbol == null ) return -1;
        Integer i = ( Integer ) symbolToIdTable.get( symbol );
        if ( i == null ) return -1;
        return i.intValue();
    }

    /**
     * Resolve an id back into a string symbol.
     */
    public final String resolveId( int id )
    {
        try
        {
            return ( String ) idToSymbolTable.get( id );
        } catch ( IndexOutOfBoundsException e )
        {
            return null;
        }
    }

    public final int addSymbol( String symbol )
    {
        if ( symbol == null ) {
            return -1 ;
        }

        int nid;
        if ( ( nid = resolveSymbol( symbol ) ) != -1 )
        {
            return nid;
        }
        nid = id;
        id = id + 1;
        symbol = symbol.intern();
        idToSymbolTable.add( symbol );
        symbolToIdTable.put( symbol, new Integer( nid ) );
        return nid;
    }

    private void writeObject( ObjectOutputStream ois ) throws java.io.IOException {
        ois.defaultWriteObject();
        // Write out the table manually to save space
        ois.writeInt( idToSymbolTable.size() );
        for (int i=0;i<idToSymbolTable.size();i++) {
            ois.writeUTF( ( String ) idToSymbolTable.get(i) );
        }
        // ois.writeInt( symbolToIdTable.size() ) ;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer() ;
        System.out.print( "[" );
        for (int i=0;i<idToSymbolTable.size();i++) {
            System.out.print( i + "=" + idToSymbolTable.get(i) );
            if ( i < idToSymbolTable.size() - 1 ) {
                System.out.print( "," );
            }
        }
        System.out.print( "]" );
        return buf.toString() ;
    }

    private void readObject( ObjectInputStream ois ) throws java.io.IOException, ClassNotFoundException {
        ois.defaultReadObject();
        int size = ois.readInt() ;
        if ( size < 0 || size > 65536) {
            throw new StreamCorruptedException( "Invalid symbol table size=" + size ) ;
        }
        idToSymbolTable = new ArrayList( size ) ;
        symbolToIdTable = new HashMap( size + ( size / 2 )) ;

        // Read the series of strings
        for (int i=0;i<size;i++) {
            String s = ois.readUTF() ;
            idToSymbolTable.add( s ) ;
            symbolToIdTable.put( s, new Integer(i) ) ;
        }
    }

    public int getCurrentId()
    {
        return id;
    }

    /** Get singleton associated with this class.
     */
    public static SymbolTable getInstance()
    {
        return instance;
    }

    private static final SymbolTable instance = new SymbolTable();
    private int id = 0;

    // Make these large to avoid rehashing!
    transient ArrayList idToSymbolTable = new ArrayList(100);
    transient HashMap symbolToIdTable = new HashMap(100);

    public static final void main( String[] args ) {

        SymbolTable table = new SymbolTable() ;
        int s1 = table.addSymbol( "Moose" ) ;
        System.out.println( "Moose=" + s1 ) ;
        int s2 = table.addSymbol( "Mice" ) ;
        System.out.println( "Mice=" + s2 ) ;
        int s3 = table.addSymbol( "Meese" ) ;
        System.out.println( "Meese=" + s3 );

        System.out.println( "Resolving mice=" + table.resolveSymbol("Mice") ) ;
        System.out.println( "Resolving moron=" + table.resolveSymbol("Moron") ) ;
        System.out.println( "Resolving 1=" + table.resolveId(1) );

        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream() ;
            ObjectOutputStream oos = new ObjectOutputStream( bos ) ;
            oos.writeObject( table );
            byte[] array = bos.toByteArray() ;
            System.out.println( "Number of bytes=" + array.length );
            ByteArrayInputStream bis = new ByteArrayInputStream( array ) ;
            ObjectInputStream ois = new ObjectInputStream( bis ) ;
            SymbolTable newTable = ( SymbolTable ) ois.readObject() ;
            System.out.print( "Table=" ) ;
            System.out.println( newTable );

            System.out.println( "Resolving mice=" + table.resolveSymbol("Mice") ) ;
            System.out.println( "Resolving moron=" + table.resolveSymbol("Moron") ) ;
            System.out.println( "Resolving 1=" + table.resolveId(1) );

        }
        catch ( Exception e ) {
            e.printStackTrace();
            return ;
        }
    }
}

