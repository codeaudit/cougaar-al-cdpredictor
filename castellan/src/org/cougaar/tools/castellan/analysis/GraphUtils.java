/*
 * GraphShapeUtils.java
 *
 * Created on October 16, 2001, 11:06 AM
 */

package org.cougaar.tools.castellan.analysis;
import java.util.* ;
import org.cougaar.tools.castellan.pspace.search.* ;
import org.cougaar.tools.castellan.util.* ;

/**
 *
 * @author  wpeng
 * @version 
 */
public abstract class GraphUtils {

    /** Creates new GraphShapeUtils */
    public GraphUtils() {
    }
    
    /** Do two (task) subgraphs have the same shape?  
     */
    public static final boolean compareShape( PlanLogDatabase pld, Subgraph s1, Subgraph s2 ) {
        MultiTreeSet s1depth = findMaxDepth( pld, s1 ) ;
        MultiTreeSet s2depth = findMaxDepth( pld, s2 ) ;
        
        if ( s1.getNumElements() != s2.getNumElements() ) {
            return false ;
        }
        
        ArrayList d1 = new ArrayList() , d2 = new ArrayList() ;
        for ( Iterator iter = s1depth.keys(); iter.hasNext(); ) {
            d1.add( iter.next() );
        }

        for ( Iterator iter = s2depth.keys(); iter.hasNext(); ) {
            d2.add( iter.next() );
        }
        
        if ( d1.size() != d2.size() ) {
            return false ;
        }
        
        // Okay, now, try the permutations one by one in each bin until we find something that works.
        // Consider only indegree and outdegree.
        for (int i=0;i<d1.size();i++) {
            Object[] objects = s1depth.getObjects( ( Integer ) d1.get( i ) ) ;
            
        }
        return false ;
    }
    
    public static final boolean compareShapeAndVerbs( Subgraph s1, Subgraph s2 ) {
        return false ;
    }
    
    /** Compare using task verbs, plan elements, and assets.
     */
    public static final boolean compareLogPlanSubgraph( Subgraph s1, Subgraph s2 ) {
        return false ;
    }

    /** Find a subgraph which depends on the parent and connects to the child.
     */
    public Subgraph findDependentSubgraph( TaskLog parent, TaskLog child ) {
        return null ;
    }
    
    public static MultiTreeSet findMaxDepth( PlanLogDatabase pld, Subgraph s ) {
        PlanStrategy ps = new PlanStrategy( pld ) ;
        SimpleSearch ss = new SimpleSearch( ps ) ;
        
        // Get all the tasks from LogPlanBuilder which have no parent.
        ArrayList list = s.roots ;
        for (int i=0;i<list.size();i++) {
            ss.initNode( new PEGraphNode( ( UniqueObjectLog ) list.get(i) ) );
        }
        
        ss.run() ;

        MultiTreeSet set = new MultiTreeSet() ;
        for ( Enumeration e = ss.getClosedNodes() ; e.hasMoreElements() ; ) {
            PEGraphNode node = ( PEGraphNode ) e.nextElement() ;
            node.setMaxDepth( node.getDepth() ) ;
        }
        
        boolean changed = true ;
        while ( changed ) {
            changed = false ;
            for ( Enumeration e = ss.getClosedNodes() ; e.hasMoreElements() ; ) {
                PEGraphNode node = ( PEGraphNode ) e.nextElement() ;
                int max = 0 ;
                for (int i=1;i<node.getNumPredecessors();i++) {
                    PEGraphNode n = ( PEGraphNode ) node.getPredecessor( i ) ;
                    if ( n.getMaxDepth() > max ) {
                        max = n.getMaxDepth() ;
                    }
                }
                if ( max + 1 > node.getMaxDepth() ) {
                    node.setMaxDepth( max + 1 );
                    changed = true ;
                }
            }
        }
        
        for ( Enumeration e = ss.getClosedNodes() ; e.hasMoreElements() ; ) {
            PEGraphNode node = ( PEGraphNode ) e.nextElement() ;
            set.put( new Integer(node.getMaxDepth()), node ) ;
        }
        
        return set ;
    }
}