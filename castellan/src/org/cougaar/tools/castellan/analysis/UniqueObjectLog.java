package org.cougaar.tools.castellan.analysis;
import org.cougaar.tools.castellan.pdu.* ;
import java.util.* ;

/**
 *  Base class for all logs corresponding (1-1) with UniqueObjects.
 */
public abstract class UniqueObjectLog implements Loggable {

   UniqueObjectLog( UIDPDU uid ) {
      this.uid = uid ;
   }

   UniqueObjectLog( UIDPDU uid, String cluster, long created, long createdExecution ) {
      this( uid ) ;
      this.cluster = cluster ;
      this.created = created ;
      this.createdExecution = createdExecution ;
      isFull = true;
   }

   public int hashCode() {
        return uid.hashCode() ;
   }

    public String toString() {
        StringBuffer buf = new StringBuffer() ;
        toString( buf ) ;
        return buf.toString() ;
    }

    static HashMap classToTypeMap = new HashMap() ;

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

    public void toString( StringBuffer buf ) {
        buf.append( '[' ) ;
        outputParamString( buf ) ;
        buf.append( ']' ) ;
    }

    public long getCreatedTime() {
        return created;
    }

    public long getCreatedExecutionTime() {
        return createdExecution;
    }

    void outputParamString( StringBuffer buf ) {
        buf.append( getLogType( getClass() ) ) ;
        buf.append( ",uid=" ).append( uid ) ;
        buf.append( ",cluster=" ).append( cluster ) ;
        //buf.append( actionToString( getAction() ) ).append( ',' ) ;
        //java.text.DateFormat.getDateInstance( DateFormat.SHORT ).format(
        //    new java.util.Date( getTime() ), buf, new FieldPosition( DateFormat.MILLISECOND_FIELD )  ) ;
    }

   public void setCluster( String cluster) { this.cluster = cluster ; }

   public void setCreatedTimestamp( long created, long createdExecution ) {
      this.created = created ; this.createdExecution = createdExecution ;
   }

   public void setRescindedTimestamp( long rescinded, long rescindedExecution ) {
      this.removed = rescinded ; this.removedExecution = rescindedExecution ;
   }

   public long getRescindedTimestamp() {
        return removed ;
   }

   public long getRescindedExecutionTimestamp() {
        return removedExecution ;
   }

   public void setFull( boolean value ) { isFull = value ; }

   public boolean isFull() { return isFull ; }

   public String getCluster() { return cluster ; }

   public UIDPDU getUID() { return uid ; }

   /** Timestamps for the creation of this UniqueObject. */
   long created = -1, createdExecution = -1 ;
   long removed = -1, removedExecution = -1 ;
   /** Logs which are not full are hollow, e.g. they were not logged as being created on the plan. */
   boolean isFull = false ;
   private UIDPDU uid ;
   private String cluster ;

    public void setAggregateLog( AggregateLog al ) {
       this.aggregateLog = al ;
    }

    public AggregateLog getAggregateLog() {
       return aggregateLog ;
    }

    AggregateLog aggregateLog ;
   //Vector aliases = new Vector() ;
}