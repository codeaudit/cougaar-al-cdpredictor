package com.axiom.lib.util ;
import java.io.*;

/**
 *  A Symbol represents a string token.  A symbol.
 */

public class Symbol implements Serializable {
    public Symbol( String s ) {
       this.s = SymbolTable.getSymbol( s ) ;
    }

    public String toString() {
       return s ;
    }

    public boolean isEqual( Symbol s ) {
       return s.s == this.s ;
    }

    public int hashCode() {
       return s.hashCode();
    }

    public boolean equals( Object object ) {
       if ( object instanceof Symbol ) {
          String s = ( ( Symbol ) object).s ;
          if ( this.s == s )
            return true ;
       }
       return false ;
    }

    private void writeObject(java.io.ObjectOutputStream out)
      throws IOException
    {
        out.defaultWriteObject() ;
        out.writeObject( s );
    }

    private void readObject( ObjectInputStream in )
      throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        String str = ( String ) in.readObject();
        this.s = SymbolTable.getSymbol( str ) ;
        this.hashCode = s.hashCode() ;
    }

    protected static final long serialVersionUID = 3951131848957623531L;
    private transient String s ;
    private transient int hashCode ;
}