package org.hydra.util;
import java.util.* ;

/** Each client will maintain a symbol table.
 */ 
public class SymbolTable implements java.io.Serializable {
    public SymbolTable() {
    }
    
    public synchronized void clear() {
        idToSymbolTable.clear() ;
        symbolToIdTable.clear() ;
    }
    
    public final int resolveSymbol( String symbol ) {
        if ( symbol == null ) return -1 ;
        Integer i = ( Integer ) symbolToIdTable.get( symbol ) ;
        if  ( i == null ) return -1 ;
        return i.intValue() ;
    }
    
    public final String resolveId( int id ) {
        return ( String ) symbolToIdTable.get( new Integer( id ) ) ;   
    }
    
    public final int addSymbol( String symbol ) {
       Integer newId ;
       idToSymbolTable.put( newId = new Integer(id++), symbol.intern() ) ;
       symbolToIdTable.put( symbol, newId ) ;
       return newId.intValue() ;
    }
    
    public int getCurrentId() { return id ; }
    
    /** Get singleton associated with this class.
     */
    public static SymbolTable getInstance() { return instance ; }
    
    private static final SymbolTable instance = new SymbolTable() ;
    
    private int id = 0 ;
    HashMap idToSymbolTable = new HashMap() ;
    HashMap symbolToIdTable = new HashMap() ;
}

