package org.hydra.metrics;
import java.util.* ;
import org.cougaar.util.* ;
import org.hydra.pdu.* ;

/** Aggregate tasks only on expansions with a parent equal to a certain
 * verb.
 */
public class AggregateExpansionLog extends AggregateLog {
    
    public AggregateExpansionLog( AggregateVerbTaskLog parent, String[] childVerbPattern ) {
        logParent( parent ) ;
        // All expansions' parents must match this verb.
        verb = parent.getVerb() ;
        cluster = parent.getCluster() ;
        
        // All expansions children must match the grammar [V1+,V2+,V3+]
        // This is relatively dumb but a more precise algorithm will have to wait
        // until later
        this.childVerbPattern = childVerbPattern ;
    }
    
    public String toString() {
        StringBuffer buf = new StringBuffer() ;
        buf.append( "Expansion " + id + ",verbs=" ) ;
        if ( childVerbPattern == null ) {
            buf.append( "<null>" ) ;
        }
        else {
            buf.append( '[' ) ;
            for (int i=0;i<childVerbPattern.length;i++) {
                buf.append( childVerbPattern[i] ) ;
                if ( i < childVerbPattern.length - 1 ) {
                    buf.append( "," ) ;
                }
            }
            buf.append( ']' ) ;
        }
        buf.append( ",#instances=").append( getNumLogs() ) ;
        return buf.toString();
    }

    public String[] getChildVerbs() {
        return ( String[] ) childVerbPattern.clone() ;
    }

    /** My parent's verbs. */
    public String getVerb() { return verb; }
  
    /** My parent's cluster. */
    public String getCluster() { return cluster ; }
    
    /** Match an expansion log with this aggregatioh. 
     */
    public boolean match( PlanLogDatabase pld, ExpansionLog el ) {
        UIDPDU[] childTasks = el.getChildren() ;
        
        if ( matchExact ) {
            if ( childTasks == null || childTasks.length != childVerbPattern.length ) {
                return false ;
            }

            for ( int i=0;i<childTasks.length;i++) {
                TaskLog tl = ( TaskLog ) pld.getLog( childTasks[i] ) ;
                if ( childVerbPattern == null || !childVerbPattern[i].equals(tl.getTaskVerb()) ) {
                    return false ;
                }
            }
            return true ;
            }
        else {
            // Just see if the verbs are the same.
            HashMap map1 = new HashMap() ;
            for (int i=0;i<childTasks.length;i++) {
                TaskLog tl = ( TaskLog ) pld.getLog( childTasks[i] ) ;
                if ( tl != null && tl.getTaskVerb() != null  ) {
                    map1.put( tl.getTaskVerb(), tl.getTaskVerb() ) ;
                }
            }
            HashMap map2 = new HashMap() ;
            for (int i=0;i<childVerbPattern.length;i++) {
                map2.put( childVerbPattern[i], childVerbPattern[i] ) ;
            }
            
            // Check to see if there are any verbs not shared by 
            for ( Iterator iter = map1.values().iterator(); iter.hasNext(); ) {
                Object key = iter.next() ;
                if ( map2.get( key ) != null ) {
                   map2.remove( key ) ;
                }
                else {
                    return false ;  // Could not find verb
                }
            }   
            if ( map2.size() > 0 ) {
                return false ;
            }
            else {
                return true ;
            }
        }
    }
    
    /** Aggregate only expansions with parent tasks belonging to verb/cluster and
     * matching the verb pattern.
     */
    class ExpansionMatchingPredicate implements UnaryPredicate {
        public boolean match( UIDPDU[] children ) {
            if ( childVerbPattern.length != children.length ) {
                return false ;
            }
            for (int i=0;i<childVerbPattern.length;i++) {
                if ( !childVerbPattern[i].equals( children[i] ) ) {
                    return false ;
                }
            }
            return true ;
        }
        
        public boolean execute( Object o ) {
            if ( o instanceof ExpansionLog ) {
                ExpansionLog el = ( ExpansionLog ) o ;
                UIDPDU[] children = el.getChildren() ;
                return match( children ) ;
            }
            return false ;
        }
    }
    
    private boolean matchExact = false ;
    private String verb, cluster ;
    private String[] childVerbPattern ;
}
