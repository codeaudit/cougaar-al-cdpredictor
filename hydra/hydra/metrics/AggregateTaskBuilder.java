package org.hydra.metrics ;

import org.hydra.util.* ;
import java.util.* ;
import org.hydra.server.* ;
import org.hydra.pdu.* ;
import org.hydra.pspace.search.* ;

/** Build plan log database.
 */

public class AggregateTaskBuilder {
    
    static class VerbClusterPair {
        VerbClusterPair( String verb, String cluster ) {
            if ( verb == null || cluster == null ) {
                throw new RuntimeException( " Verb " + verb + " or cluster " + cluster + " is null." ) ;
            }
            this.verb = verb ;
            this.cluster = cluster ;
            hashCode = verb.hashCode() + cluster.hashCode() ;
        }
        
        public int hashCode() { return hashCode ; }
        
        public boolean equals( Object o ) {
            if ( o instanceof VerbClusterPair ) {
                VerbClusterPair p = ( VerbClusterPair ) o ;
                return p.verb.equals( verb ) && p.cluster.equals( cluster ) ;
            }
            return false ;
        }
        
        private int hashCode ;
        String verb, cluster ;
    }
    
    
    public AggregateTaskBuilder( LogPlanBuilder builder ) {
        this.builder = builder ;
        this.pld = builder.getDatabase() ;
    }
    
    public Iterator getAggregateTasks() {
        return aggregateVerbTaskLogTable.elements() ;
    }
    
    /** @return True if target aggregate log is reachable from source,
     *  false otherwise.a
     */
    protected boolean isReachable( AggregateLog source, AggregateLog target ) {
        if ( target == null ) { return false ; }
        
        HashMap children = new HashMap() ;
        boolean isChanged = true ;
        
        // Really, really stupid and inefficient way to conduct graph search. Starting at source,
        // traverse by adding children until the target is reached.
        children.put( source, source ) ;
        
        HashMap newList = new HashMap() ;
        while ( isChanged ) {
            isChanged = false ;
            newList.clear() ;
            for ( Iterator e = children.values().iterator();e.hasNext();) {
                AggregateLog o = ( AggregateLog ) e.next() ;
                for (int i=0;i<o.getNumChildren();i++) {
                    AggregateLog child = o.getChild(i) ;
                    if ( child == target ) {
                        return true ;
                    }
                    if ( children.get(child) == null ) {
                        isChanged = true ;
                        newList.put( child, child ) ;
                        // children.put( child, child ) ;
                    }
                }
            }
            children.putAll( newList ) ;
        }
        return false ;
    }
    
    /** Note: more than one AggregateVerbTaskLog can be associated with a single
     * verb cluster pair.  This only returns the first one.
     */
    protected AggregateVerbTaskLog getAggregateVerbTaskLog( String verb, String cluster ) {
        return ( AggregateVerbTaskLog )
        aggregateVerbTaskLogTable.get( new VerbClusterPair( verb, cluster ) ) ;
    }
    
    protected void addAggregateExpansionLog( AggregateExpansionLog l ) {
        aggregateExpansionTable.put( new VerbClusterPair( l.getVerb(), l.getCluster() ), l );
    }
    
    protected AggregateExpansionLog[] getAggregateExpansionLog( String verb, String cluster ) {
        Object[] objects = aggregateExpansionTable.getObjects( new VerbClusterPair( verb, cluster ) );
        AggregateExpansionLog[] result = new AggregateExpansionLog[ objects.length ] ;
        for (int i=0;i<objects.length;i++) {
            result[i] = ( AggregateExpansionLog ) objects[i] ;
        }
        return result ;
    }
    
    protected AggregateVerbTaskLog[] getAllAggVerbTaskLogs( String verb, String cluster ) {
        Object[] o = aggregateVerbTaskLogTable.getObjects( new VerbClusterPair( verb, cluster ) ) ;
        AggregateVerbTaskLog[] vtl = new AggregateVerbTaskLog[ o.length ] ;
        for (int i=0;i<vtl.length;i++) {
            vtl[i] = ( AggregateVerbTaskLog ) o[i] ;
        }
        return vtl ;
    }
    
    protected AggregateMPTaskLog makeNewAggregateMPTaskLog( MPTaskLog tl ) {
        AggregateMPTaskLog avtl = new AggregateMPTaskLog( tl.getTaskVerb(), tl.getCluster() ) ;
        VerbClusterPair p = new VerbClusterPair( tl.getTaskVerb(), tl.getCluster() ) ;
        aggregateVerbTaskLogTable.put( p, avtl ) ;
        return avtl ;
    }
    
    protected AggregateVerbTaskLog makeNewAggregateVerbTaskLog( TaskLog tl ) {
        AggregateVerbTaskLog avtl = new AggregateVerbTaskLog( tl.getTaskVerb(), tl.getCluster() ) ;
        VerbClusterPair p = new VerbClusterPair( tl.getTaskVerb(), tl.getCluster() ) ;
        aggregateVerbTaskLogTable.put( p, avtl ) ;
        return avtl ;
    }
    
    protected AggregateMPTaskLog checkAggregateMPTaskLogForLog( MPTaskLog mpTaskLog ) {
        if ( ServerApp.instance().isVerbose() ) {
            ServerApp.instance().println( "\tcheckAggregateMPTaskLogForLog for " + mpTaskLog ) ;
        }
        
        // Check the parents of this MPTaskLog and their aggregates and find an
        // matching aggregate cannot reach any of the parents
        ArrayList parents = new ArrayList() ;
        for (int i=0;i<mpTaskLog.getNumParents();i++) {
            UniqueObjectLog log = pld.getLog( mpTaskLog.getParent( i ) ) ;
            AggregateLog aggParentLog = getAggregateLogForLog( log ) ;
            if ( aggParentLog == null ) {
                continue ;
            }
            if ( parents.indexOf( aggParentLog ) == -1 ) {
                parents.add( aggParentLog ) ;
            }
        }
        
        AggregateVerbTaskLog[] avtls = getAllAggVerbTaskLogs( mpTaskLog.getTaskVerb(), mpTaskLog.getCluster() ) ;
        
        AggregateMPTaskLog avtlFound = null ;
        for (int i=0;i<avtls.length;i++) {
            if ( !(avtls[i] instanceof AggregateMPTaskLog) ) {
                continue ;
            }
            avtlFound = ( AggregateMPTaskLog ) avtls[i] ;
            for (int j=0;j<parents.size();j++) {
                AggregateLog aggParentLog = ( AggregateLog ) parents.get(j) ;
                if ( avtls[i] == aggParentLog ) continue ;
                if ( isReachable( avtls[i], aggParentLog ) ) { // Can the aggParentLog be reached from this avtl?
                    avtlFound = null ;
                    break ;
                }
            }
            if ( avtlFound != null ) {
                return avtlFound ;
            }
        }
        
        //System.out.println( "Making new avtl for " + tl ) ;
        AggregateMPTaskLog avtl = makeNewAggregateMPTaskLog( mpTaskLog ) ;
        avtl.logInstance( mpTaskLog ) ;
        
        for (int i=0;i<parents.size();i++) {
            AggregateLog apl = ( AggregateLog ) parents.get(i) ;
            avtl.logParent( apl );
            apl.logChildAggregateLog( avtl, mpTaskLog ) ;
        }
        return avtl ;
    }
    
    protected AggregateLog getAggregateLogForLog( UniqueObjectLog l ) {
        if ( l == null ) return null ;
        
        if ( l instanceof ExpansionLog ) {
            return getAggregateExpansionLogForExpansionLog( ( ExpansionLog ) l ) ;
        }
        else if ( l instanceof TaskLog ) {
            return getAggregateVerbTaskLogForTaskLog( ( TaskLog ) l ) ;
        }
        else {
            throw new RuntimeException( "Log " + l + " does not have aggregate log." ) ;
        }
    }
    
    protected AggregateExpansionLog getAggregateExpansionLogForExpansionLog( ExpansionLog el ) {
        TaskLog parent = ( TaskLog ) pld.getLog( el.getParent() ) ;
        AggregateExpansionLog[] aels = getAggregateExpansionLog( parent.getTaskVerb(),
        parent.getCluster() ) ;
        
        for (int i=0;i<aels.length;i++) {
            if ( aels[i].getLoggedInstance(el.getUID()) != null ) {
                return aels[i] ;
            }
        }
        return null ;
    }
    
    protected AggregateVerbTaskLog getAggregateVerbTaskLogForTaskLog( TaskLog tl ) {
        //System.out.println( "\t---> getAggregateVerbTaskLogForTaskLog( " + tl + " ) " ) ;
        
        if ( tl == null || tl.getTaskVerb() == null || tl.getCluster() == null ) {
            return null ;
        }
        
        AggregateVerbTaskLog[] avtls = getAllAggVerbTaskLogs( tl.getTaskVerb(), tl.getCluster() ) ;
        //System.out.println( "There are a total of " + avtls.length + "avtls" ) ;
        for (int i=0;i<avtls.length;i++) {
            if ( avtls[i].getLoggedInstance( tl.getUID() ) != null ) {
                return avtls[i] ;
            }
        }
        
        return null ;
    }
    
    /** Return an AggregateExpansionLog which is not reachable from the avtl associated with
     *  the parent of this el.
     */
    protected AggregateExpansionLog checkAggregateExpansionLog( ExpansionLog el, ArrayList dependencies ) {
        // getAggregateExpansionLogForExpansionLog( el ) ;
        
        TaskLog tl = ( TaskLog ) pld.getLog( el.getParent() ) ;
        
        // Cannot yet do anything
        if ( tl == null || tl.getTaskVerb() == null || tl.getCluster() == null ) {
            return null ;
        }
        
        // Does an aggregate verb task log exist for my parent?
        // System.out.println( "checkAggregationExpansionLog::checkAggregateVerbTaskLog for " + tl ) ;
        AggregateVerbTaskLog avtl = getAggregateVerbTaskLogForTaskLog( tl );
        
        if ( avtl == null ) {
            System.out.println( "AggregateVerbTaskLog does not exist for " + tl + " at depth " + getDepthForLog( tl.getUID() ) 
              + " with parent " + tl.getParent() + " at depth " + getDepthForLog( tl.getParent() ) ) ;
            return null ;
        }
        
        AggregateExpansionLog ael = getAggregateExpansionLogForExpansionLog( el ) ;
        if ( ael != null ) {
            return ael ;
        }
        
        // Okay nothing is currently matching.  Try to make a list of child verbs.
        UIDPDU[] tasks = el.getChildren() ;
        String[] verbs = null ;
        
        if ( tasks != null ) {
            for (int i=0;i<tasks.length;i++ ) {
                if ( tasks[i] == null ) {  // Hmm, some of these tasks are null?
                    return null ;
                }
            }
            
            verbs = new String[ tasks.length ] ;
            for (int i=0;i<verbs.length;i++) {
                TaskLog tlc = ( TaskLog ) pld.getLog( tasks[i] ) ;
                if ( tlc == null || tlc.getTaskVerb() == null ) {
                    //if ( !tlc.isFull() ) {
                    //    dependencies.add( tlc.getUID() ) ;
                    //}
                    return null ; // Need all the verbs before we can pattern match!
                }
                verbs[i] = tlc.getTaskVerb() ;
            }
        }
        
        AggregateExpansionLog[] aels = getAggregateExpansionLog( tl.getTaskVerb(), tl.getCluster() ) ;
        for (int i=0;i<aels.length;i++) {
            if ( aels[i].match( pld, el ) && !isReachable( aels[i], avtl ) ) {
                return aels[i]  ;
            }
        }
        
        // Okay, make a new aggregate expansion log.
        ael = new AggregateExpansionLog( avtl, verbs ) ;
        avtl.logChildAggregateLog( ael, el ) ;
        addAggregateExpansionLog( ael );
        return ael ;
    }
    
    /**  AggregateVerbTaskLog --> AggregationExpansionLog --> AggregateVerbTaskLog
     */
    protected void processExpansionLog( ExpansionLog el ) {
        ArrayList dependencies = new ArrayList() ;
        // After this, we should be able to safely add children to ael.
        AggregateExpansionLog ael = checkAggregateExpansionLog( el, dependencies ) ;
        if ( ael == null ) {
            return ;
        }
        ael.logInstance(el) ;  // Log el as an instance of ael
        
        // Now, find the verbs for all children of m, and log them as the child of ael
        UIDPDU[] children = el.getChildren() ;
        if ( children == null ) {  // This should never happen?
            System.out.println( "Null child tasks in " + el ) ;
            return ;
        }
        
        // Connect to child tasks
        for (int i=0;i<children.length;i++) {
            UIDPDU task = children[i] ;
            TaskLog tl = ( TaskLog ) pld.getLog( task ) ;
            AggregateVerbTaskLog childAvtl = ( AggregateVerbTaskLog ) tl.getAggregateLog() ;
            
            // Fixup any child tasks unaccountable linked to something else.
            if ( childAvtl != null ) {
                if ( childAvtl.getParent() != ael ) {
                    AggregateLog old = childAvtl.getParent() ;
                    // Remove any link between the parent and this child avtl
                    if ( old != null ) {
                        old.unlogChildAggregateLog( childAvtl, tl ) ;
                    }
                    
                    // Connect this to the expansion log
                    childAvtl.logParent( ael ) ;
                    ael.logChildAggregateLog( childAvtl, tl ) ;
                }
            }
            
            if ( tl.getTaskVerb() == null || tl.getCluster() == null ) {
                System.out.println( "Found null in verb=" + tl.getTaskVerb()
                + " and cluster= " + tl.getCluster() + " for task " + tl.getUID() ) ;
                continue ;
            }
            // Here is where we must check ancestry of this particular avtl.  In particular,
            // the avtl chosen must not be able to reach the parent through graph traversal
            
            AggregateVerbTaskLog[] avtls = getAllAggVerbTaskLogs( tl.getTaskVerb(), tl.getCluster() ) ;
            AggregateVerbTaskLog current = null ;
            
            // Check to see whether we have been logged.
            for (int j=0;j<avtls.length;j++) {
                if ( avtls[j].getLoggedInstance( tl.getUID() ) != null ) {
                    current = avtls[j] ;
                    break ;
                }
            }
            
            if ( current != null ) {
                // Tasklog is already logged.
                continue ;
            }
            
            boolean found = false ;
            for (int j=0;j<avtls.length;j++) {
                if ( !isReachable( avtls[j], ael ) ) {
                    avtls[j].logInstance( tl ) ;
                    ael.logChildAggregateLog( avtls[j], tl ) ;
                    avtls[j].logParent( ael );
                    found = true ;
                    break ;
                }
            }
            
            if ( !found ) {
                AggregateVerbTaskLog avtl = makeNewAggregateVerbTaskLog( tl ) ;
                avtl.logInstance( tl ) ;
                avtl.logParent( ael ) ;
                ael.logChildAggregateLog( avtl, tl ) ;
            }
        }
        // processQueuedMessages( m.getUID() ) ;
        // Process any messages queued on this expansion
        // System.out.println( "Done." ) ;
    }
    
    /**  Do not make any AggregateAggregationLogs.  Just make
     *   AggregateVerbTaskLog --> AggregateMPTaskLog links.
     */
    protected void processMPTaskLog( MPTaskLog mpTaskLog ) {
        ArrayList parents = new ArrayList() ;
        for (int i=0;i<mpTaskLog.getNumParents();i++) {
            UniqueObjectLog log = pld.getLog( mpTaskLog.getParent( i ) ) ;
            AggregateLog aggParentLog = getAggregateLogForLog( log ) ;
            if ( aggParentLog == null ) {
                System.out.println( "Could not find agg. parent log for " + mpTaskLog ) ;   // Parent has not yet been processed, cannot proceed
                continue ;
            }
            if ( parents.indexOf( aggParentLog ) == -1 ) {
                parents.add( aggParentLog ) ;
            }
        }
        
        AggregateVerbTaskLog[] avtls = getAllAggVerbTaskLogs( mpTaskLog.getTaskVerb(), mpTaskLog.getCluster() ) ;
        
        AggregateMPTaskLog avtlFound = null ;
        for (int i=0;i<avtls.length;i++) {
            if ( !(avtls[i] instanceof AggregateMPTaskLog) ) {
                continue ;
            }
            avtlFound = ( AggregateMPTaskLog ) avtls[i] ;
            for (int j=0;j<parents.size();j++) {
                AggregateLog aggParentLog = ( AggregateLog ) parents.get(j) ;
                if ( avtls[i] == aggParentLog ) continue ;
                if ( isReachable( avtls[i], aggParentLog ) ) { // Can the aggParentLog be reached from this avtl?
                    avtlFound = null ;
                    break ;
                }
            }
        }
        
        if ( avtlFound == null ) {
            avtlFound = makeNewAggregateMPTaskLog( mpTaskLog ) ;
        }
        avtlFound.logInstance( mpTaskLog ) ;
        for (int i=0;i<parents.size();i++) {
            AggregateLog apl = ( AggregateLog ) parents.get(i) ;
            avtlFound.logParent( apl );
            apl.logChildAggregateLog( avtlFound, mpTaskLog ) ;
        }
    }

    protected void processAllocationLog( AllocationLog log ) {
        if ( log.getAllocTaskUID() != null ) {
            TaskLog ptl = ( TaskLog ) pld.getLog( log.getParent() ) ;
            TaskLog ctl = ( TaskLog ) pld.getLog( log.getAllocTaskUID() ) ;
            AggregateVerbTaskLog aggParentLog = getAggregateVerbTaskLogForTaskLog( ptl ) ;
            if ( ptl == null || ctl == null ) {
                return ;
            }
            AggregateVerbTaskLog[] avtls = getAllAggVerbTaskLogs( ctl.getTaskVerb(), ctl.getCluster() ) ;
            AggregateVerbTaskLog avtlFound = null ;
            for (int i=0;i<avtls.length;i++) {
                if ( avtls[i] == aggParentLog ) continue ;
                if ( isReachable( avtls[i], aggParentLog ) ) { // Can the aggParentLog be reached from this avtl?
                    continue ;
                }
                avtlFound = avtls[i] ;
                break ;
            }

            if ( avtlFound == null ) {
                avtlFound = makeNewAggregateVerbTaskLog( ctl ) ;
            }
        
            // Fall through, default case.  Making a new avtl.
            avtlFound.logInstance( ctl ) ;
            if ( aggParentLog != null ) {
                avtlFound.logParent( aggParentLog );
                aggParentLog.logChildAggregateLog( avtlFound, ctl ) ;
            }
        }
    }

    protected void processTaskLog( TaskLog log ) {
        PlanElementLog pel = pld.getPlanElementLogForTask( log.getUID() ) ;

        TaskLog parentLog ;

        // My parent log does not exist.
        if ( log.getParent() == null ||
        ( parentLog = ( TaskLog ) pld.getLog( log.getParent() ) ) == null ) {
            AggregateVerbTaskLog[] avtls =
            getAllAggVerbTaskLogs( log.getTaskVerb(), log.getCluster() ) ;
            for (int i=0;i<avtls.length;i++) {
                if ( avtls[i].getNumParents() == 0 ) {
                    avtls[i].logInstance( log ) ;
                    return ;
                }
            }
            AggregateVerbTaskLog avtl = makeNewAggregateVerbTaskLog( log ) ;
            avtl.logInstance( log ) ;
            return ;
        }
    }

    public void processLog( UniqueObjectLog l ) {
        if ( l instanceof MPTaskLog ) {
            processMPTaskLog( ( MPTaskLog ) l ) ;
        }
        else if ( l instanceof TaskLog ) {
            processTaskLog( ( TaskLog ) l ) ;
        }
        else if ( l instanceof ExpansionLog ) {
            processExpansionLog( ( ExpansionLog ) l ) ;
        }
        else if ( l instanceof AllocationLog ) {
            processAllocationLog( ( AllocationLog ) l );
        }
        //else if ( l instanceof AggregationLog ) {            
        //}
    }
    
    private int getDepthForLog( UIDPDU pdu ) {
        Object o = depthTable.get( pdu ) ;
        if ( o != null ) {
            return ( ( Integer ) o ).intValue() ;
        }
        return -1 ;
    }
    
    public SimpleSearch getSearch() { return ss ; }
    
    public void buildGraph( ) {
        // Find the depth of all task elements.
        
        PlanStrategy ps = new PlanStrategy( pld ) ;
        ss = new SimpleSearch( ps ) ;
        
        // Get all the tasks from LogPlanBuilder which have no parent.
        ArrayList list = builder.getRootTasks() ;
        // System.out.println( "There are " + list.size() + " root tasks." ) ;
        for (int i=0;i<list.size();i++) {
            ss.initNode( new PEGraphNode( ( UniqueObjectLog ) list.get(i) ) );
        }
        
        ss.run() ;
        // ss.dump() ;
        
        // Find the max depth of all.
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
        
        // Make a depth table (for debugging purposes?)
        depthTable = new HashMap() ;
        for ( Enumeration e = ss.getClosedNodes() ; e.hasMoreElements() ; ) {
            PEGraphNode node = ( PEGraphNode ) e.nextElement() ;
            depthTable.put( node.getLog().getUID() , new Integer( node.getDepth() )  ) ;
        }
        
        for ( Enumeration e = ss.getClosedNodes() ; e.hasMoreElements() ; ) {
            PEGraphNode node = ( PEGraphNode ) e.nextElement() ;
            set.put( new Integer(node.getMaxDepth()), node ) ;
        }
        
        for ( Iterator e = set.keys(); e.hasNext(); ) {
            Integer key ;
            Object[] objects = set.getObjects( key = ( Integer ) e.next() ) ;
            // Process all of these objects
//            System.out.println( "\nProcessing objects at depth " + key.intValue() ) ;
            for (int i=0;i<objects.length;i++) {
                PEGraphNode node = ( PEGraphNode ) objects[i] ;
//                System.out.print( node.getLog().getUID() ) ;
//                System.out.print( "," ) ;
                processLog( node.getLog() ) ;
            }
        }
        
    }
    
    protected SimpleSearch ss ;
    protected PlanLogDatabase pld ;
    protected LogPlanBuilder builder ;
    protected HashMap depthTable ;
    
    // Mapping from verb, cluster pairs to aggregate logs.
    protected MultiHashSet aggregateVerbTaskLogTable = new MultiHashSet( 20 ) ;
    
    // Mapping from expansion verb patterns to aggregate logs.
    protected MultiHashSet aggregateExpansionTable = new MultiHashSet( 30 ) ;
    
}
