package com.axiom.lib.util ;
import java.io.*;
import java.util.*;

/**
 *  No provision is made for flushing the symbol table except by
 *  rebuilding it from scratch.  The symbol table is a singleton class.
 *
 *  It maintains mappings from ids to Strings and vice versa.
 */

public class SymbolTable {
    static String getSymbol( String s ) {
        String result = ( String ) stringTable.get( s ) ;
        if ( result == null ) {
        result = s ;
        stringTable.put( s, s );
        }
        return result ;
    }

    public static Hashtable stringTable = new Hashtable();

    public static void main( String[] argv ) {
        Symbol moose = new Symbol( "moose" ) ;
        Symbol my1 = new Symbol( "my" ) ;
        Symbol my2 = new Symbol( "my" ) ;
        Symbol dog = new Symbol( "dog" ) ;

        System.out.println( my1.equals(my2) ) ;

        byte[] tmp = null ;
        // Write to file
        try {
            ByteArrayOutputStream f = new ByteArrayOutputStream();
            ObjectOutput  s  =  new  ObjectOutputStream(f);
            s.writeObject( my1 );
            s.writeObject( my2 );
            s.writeObject( dog );
            s.writeObject( moose ) ;
            s.flush();
            f.close();
            tmp = f.toByteArray() ;
        }
        catch ( IOException e ) {
             System.out.println("Error writing symbols. Exiting." );
             System.exit(0);
        }

        try {
            ByteArrayInputStream is = new ByteArrayInputStream( tmp ) ;
            ObjectInputStream s = new ObjectInputStream(is);
            Symbol s1 = ( Symbol ) s.readObject();
            Symbol s2 = ( Symbol ) s.readObject();
            Symbol s3 = ( Symbol ) s.readObject();
            Symbol s4 = ( Symbol ) s.readObject();
        }
        catch ( IOException e ) {
            System.out.println("Error reading symbols. Exiting." ) ;
            System.exit(0);
        }
        catch ( ClassNotFoundException e ) {
            e.printStackTrace();
            System.exit(0);
        }

        Symbol fox = new Symbol( "fox" );
    }
}
