package org.cougaar.tools.castellan.analysis;
import java.util.* ;
import org.cougaar.util.* ;
import org.cougaar.tools.castellan.server.* ;
import java.lang.reflect.* ;

/** Aggregate tasks only on verb/cluster tasks which are identifical.
 */
public class AggregateVerbTaskLog extends AggregateLog {

    public static final int TYPE_NORMAL = 1 ;
    public static final int TYPE_EXPANSION = 2 ;
    public static final int TYPE_AGGREGATION = 3 ;

    public AggregateVerbTaskLog( String verb, String cluster ) {
        if ( verb == null || cluster == null ) {
            throw new IllegalArgumentException( "Verb " + verb + " or cluster " + cluster + " is null." ) ;
        }
        setVerbCluster( verb.intern(), cluster.intern() );
    }

    public void outputParamString( StringBuffer buf ) {
       super.outputParamString( buf ) ;
       buf.append( ",verb=" ).append( getVerb() ) ;
       buf.append( ",cluster=" ).append( getCluster()) ;
    }

    //public String toString() {
    //    return "[this= " + id + ",Aggregate verb= " + getVerb() + ", cluster=" + getCluster()
    //    + ", #children=" + getNumChildren() + ", #instances=" + getNumLogs() + "]" ;
    //}

    public String getVerb() {
        return verb ;
    }

    public String getCluster() {
        return cluster ;
    }

    static class AcceptVerbPredicate implements UnaryPredicate {
        AcceptVerbPredicate( String verb, String cluster ) {
            this.verb = verb ;
            this.cluster = cluster ;
        }

        public boolean execute( Object o ) {
            if ( o instanceof TaskLog ) {
                TaskLog tl = ( TaskLog ) o ;
                return tl.getTaskVerb().equals( verb ) && tl.getCluster().equals( cluster ) ;
            }
            return false ;
        }

        String verb, cluster ;
    }

    /**  @param log Either an AggregateVerbTaskLog (with TYPE_NORMAL, e.g. used for intra cluster routing),
     *  AggregateExpansionTaskLog(TYPE_EXAPANSION) or AggregateAggregationLog (TYPE_AGGREGATION).  If
     */
    public boolean logChildAggregateLog( AggregateLog alog, UniqueObjectLog l ) {
        if ( ServerApp.instance() != null && ServerApp.instance().isVerbose() ) {
           ServerApp.instance().println( "Logging " + l + " as child agg. log of " + alog ) ;
        }

        if ( ( alog instanceof AggregateVerbTaskLog && l instanceof TaskLog ) ||
        ( alog instanceof AggregateExpansionLog && l instanceof ExpansionLog )) {
            super.logChildAggregateLog( alog, l ) ;
            return true ;
        }
        else {
            throw new IllegalArgumentException( "Child " + alog +
            " must be AggregateVerbTaskLog or AggregateExpansionLog, "+
            "logged object must of type TaskLog or ExpansionLog" ) ;
        }
    }

    protected void setVerbCluster( String verb, String cluster ) {
        this.verb = verb ;
        this.cluster = cluster ;
        setAcceptPredicate( new AcceptVerbPredicate( verb, cluster ) ) ;
    }

    String verb, cluster ;

    AggregateLog parentLog ;
}
