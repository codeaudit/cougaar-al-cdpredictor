/**
 * MPTaskLog.java
 *
 * Created on August 9, 2001, 2:51 PM
 */

package org.hydra.metrics;
import org.hydra.pdu.* ;
import java.util.* ;

/**
 *
 * @author  wpeng
 * @version
 */
public class MPTaskLog extends TaskLog {
    
    /** Creates new MPTaskLog */
    public MPTaskLog( UIDPDU uid, String cluster, String taskVerb, long createdTime, long createdExecutionTime ) {
        super( uid, cluster, taskVerb, createdTime, createdExecutionTime ) ;
    }
    
    public static MPTaskLog makeFromTaskLog( TaskLog tl ) {
        MPTaskLog mpt = new MPTaskLog( tl.getUID(), tl.getCluster(), tl.getTaskVerb(), tl.created,  tl.createdExecution ) ;
        mpt.removed = tl.removed ; mpt.removedExecution = tl.removedExecution ;
        mpt.setParent( tl.getParent() ) ;
        mpt.setTaskVerb( tl.getTaskVerb() ) ;
        //mpt.setAggregateLog( tl.getAggregateLog() ) ;
        return mpt ;
    }
    
    void outputParamString(StringBuffer buf) {
        super.outputParamString( buf ) ;
        buf.append( ",#parents=" ).append( getNumParents() ) ;
        buf.append( ",verb=").append( taskVerb ) ;
    }
    
    public void addParent( UIDPDU parent ) {
        if ( parent != null && parentList.indexOf( parent ) == -1 ) {
            parentList.add( parent ) ;
            //if ( verbs.get( tl.getTaskVerb() ) == null ) {
            //    verbs.put( tl.getTaskVerb(), tl.getTaskVerb() ) ;
            //}
        }
    }
    
    /**
     * public String[] getParentVerbs() {
     * String[] result = new String[ verbs.size() ] ;
     * int i = 0 ;
     * for ( Iterator iter = verbs.values().iterator();iter.hasNext();) {
     * result[i++] = ( String ) iter.next() ;
     * }
     * return result ;
     * }
     */
    
    public UIDPDU getParent() { throw new RuntimeException( "Use getParent(int) to get MPTask parent." ) ; }
    
    public int getNumParents() { return parentList.size() ; }
    
    public UIDPDU getParent( int i ) { return ( UIDPDU) parentList.get(i) ; }
    
    ArrayList parentList = new ArrayList() ;
    TreeMap verbs = new TreeMap( new Comparator() {
        public int compare( Object o1, Object o2 ) {
            return ( ( String ) o1 ).compareTo( o2 ) ;
        }
    }) ;
}
