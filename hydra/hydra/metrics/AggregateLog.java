package org.hydra.metrics;
import java.util.* ;
import org.cougaar.util.* ;
import org.hydra.pdu.* ;
import org.hydra.server.* ;

/** Stores events for an aggregation of objects.
 */
public class AggregateLog implements Loggable {
    public AggregateLog() {
        id = currentId++ ;
    }
    
    public void logParent( AggregateLog parent ) {
        if ( parent != null && parents.indexOf( parent ) == -1 ) {
            parents.add( parent ) ;
            // parent.logChildAggregateLog( this ) ;
        }
    }

    public void unlogParent( AggregateLog parent ) {
        parents.remove( parent ) ;
    }
    
    private static String getLogType( Class type ) {
        String result = ( String ) classToTypeMap.get( type ) ;
        String name = type.toString() ;
        if ( result == null ) {
            int indx = type.toString().lastIndexOf( '.' ) ;
            if ( indx != -1 ) {
                result = name.substring( indx + 1 ) ;
            }
            else
                result = name ;
            classToTypeMap.put( type, result ) ;
        }
        return result ;
    }
    
    public String toString() {
        StringBuffer buf = new StringBuffer() ;
        outputParamString( buf) ;
        buf.append( "]" );
        return buf.toString() ;
    }
    
    public void toString( StringBuffer buf ) {
        buf.append( "[" ) ;
        outputParamString( buf ) ;
        buf.append( ']' ) ;
    }
    
    void outputParamString( StringBuffer buf ) {
        buf.append( getLogType( getClass() ) ) ;
        buf.append( ",#children=" ).append( getNumChildren() ) ;
        buf.append( ",#inst=" ).append( getNumLogs() ) ;
        buf.append( ",#parents=" ).append( getNumParents() ) ;
    }
    
    public int getID() { return id ; }
    
    public int getIndexOfParent( AggregateLog parent ) {
        return parents.indexOf(parent) ;
    }
    
    public int getIndexOfChild( AggregateLog child ) {
        return children.indexOf(child) ;
    }
    
    public int getNumParents() { return parents.size() ; }
    
    public AggregateLog getParent( int index ) {
        return ( AggregateLog ) parents.get( index ) ;
    }
    
    public AggregateLog getParent() {
        if ( parents.size() == 0 )
            return null ;
        return ( AggregateLog ) parents.get(0) ;
    }
    
    /** Log a non-aggregated instance.
     */
    public boolean logInstance( UniqueObjectLog o ) {
        //if ( acceptPredicate != null && acceptPredicate.execute( o ) ) {
        logs.put( o.getUID(), o ) ;
        return true ;
        //}
        //return false ;
    }
    
    public UniqueObjectLog getLoggedInstance( UIDPDU UID ) {
        return ( UniqueObjectLog ) logs.get( UID ) ;
    }
    
    public boolean unlogInstance( UniqueObjectLog o ) {
        return logs.remove( o.getUID() ) != null ;
    }
    
    public boolean logChildAggregateLog( AggregateLog alog, UniqueObjectLog instance ) {
        if ( ServerApp.instance().isVerbose()  ) {
            ServerApp.instance().println( "Logging for " + this + " \n\t and child " + alog ) ;
        }
        
        if ( children.indexOf(alog) == -1 ) {
            children.add( alog ) ;
        }
        
        // Are tracking child instances useful?
        instances.put( instance.getUID(), instance ) ;
        instance.setAggregateLog( alog ) ;
        return true ;
    }
    
    void unlogChildAggregateLog( AggregateLog alog, UniqueObjectLog childInstance ) {
        children.remove( alog ) ;
        instances.remove( childInstance.getUID() ) ;
    }
    
    public int getNumChildren() { return children.size() ; }
    
    public AggregateLog getChild( int i ) { return ( AggregateLog ) children.get(i) ; }
    
    public int getNumLogs() { return logs.size(); }
    
    public Collection getLogs() { return logs.values() ; }
    
    public void setAcceptPredicate( UnaryPredicate up ) { acceptPredicate = up ; }
    
    /** Should I aggregate on this log.
     */
    public UnaryPredicate getAcceptPredicate() { return acceptPredicate ; }
    
    /** Logs aggregated by this log.
     */
    ArrayList parents = new ArrayList() ;
    HashMap logs = new HashMap() ;
    UnaryPredicate acceptPredicate = null ;
    ArrayList  children = new ArrayList() ;
    HashMap instances = new HashMap() ;
    int id ;
    
    private static HashMap classToTypeMap = new HashMap();
    private static int currentId = 0 ;
}
