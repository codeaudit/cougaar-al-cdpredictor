/*
  * <copyright>
  *  Copyright 2001 (Intelligent Automation, Inc.)
  *  under sponsorship of the Defense Advanced Research Projects
  *  Agency (DARPA).
  *
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the Cougaar Open Source License as published by
  *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
  *
  *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
  *  PROVIDED "AS IS" WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
  *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
  *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
  *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
  *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
  *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
  *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
  *  PERFORMANCE OF THE COUGAAR SOFTWARE.
  *
  * </copyright>
  *
  * CHANGE RECORD
  */
package org.cougaar.tools.alf;

import org.cougaar.tools.castellan.pdu.*;
import org.cougaar.tools.castellan.analysis.*;
import org.cougaar.tools.castellan.pspace.search.GraphSearch;
import org.cougaar.tools.castellan.pspace.search.SimpleSearch;
import org.cougaar.core.agent.Cluster;

import java.util.*;

/**
 * Take observed PDUs and compile them into a simplified set of aggregates.
 * This depends on an existing PlanLogDatabase.  This is similar to
 * the AggregateTaskBuilder except that it only really cares about INJECTED, TERMINAL
 * and OUTGOING tasks.
 */
public class AgentLoadObserver {
    public AgentLoadObserver(PlanLogDatabase pld, SocietyDesc society ) {
        if ( pld == null || society == null ) {
            throw new IllegalArgumentException( "Null argument passed." ) ;
        }
        this.pld = pld;
        this.society = society ;
        /**
         * Create a strategy that terminates early.
         */
        strategy = new IntraAgentSubgraphStrategy( pld, true ) ;
        findDescendantsStrategy = new IntraAgentSubgraphStrategy( pld, false ) ;
    }

    public SocietyDesc getSociety() {
        return society;
    }

    /**
     * Whether a particular task (log) is allocated from a remote cluster.  Checks to
     * see if the owner and the cluster are different.
     */
    protected boolean isAllocFromRemoteCluster( TaskLog log ) {
        UIDStringPDU uid = ( UIDStringPDU ) log.getUID() ;
        return !uid.getOwner().equals(log.getCluster()) ;
    }

    /**
     * Perform a GraphSearch through the current PlanLogDatabase, seeing if all descendents of tl are either
     * outgoing or terminal.
     */
    public boolean isTaskCompleted( TaskLog tl ) {
        if ( ! isAllocFromRemoteCluster( tl ) ) {
            throw new IllegalArgumentException( "Task log " + tl + " with cluster " + tl.getCluster() + "must be allocated from remote cluster." ) ;
        }
        SimpleSearch search = new SimpleSearch( strategy ) ;
        search.initNode( new PEGraphNode(tl) ) ;
        search.run();

        /**
         * If no goal nodes are found, the task is complete.
         */
        return search.getGoalNode() == null ;
    }

    /**
     * A boundary type is associated with each task and its visibility to other agents.
     * The returned type can be one of ( INCOMING | SOURCE ) [ & ( UNKNOWN | TERMINAL | OUTGOING ) ]
     * or ( INTERNAL )
     * or ( TERMINAL | OUTGOING | UNKNOWN )
     * where optional flags are given in []
     *
     * @return A set of bit flags with one or more INTERNAL, INCOMING, OUTGOING, TERMINAL, or UNKNOWN.
     */
    public int getBoundaryType( TaskLog tl ) {
        int result = 0 ;
        UIDStringPDU uid = ( UIDStringPDU ) tl.getUID() ;

        if ( !( tl instanceof MPTaskLog) ) {
            if ( tl.getParent() == null ) {
                result |= BoundaryConstants.SOURCE ;
            }
            else if ( !tl.getCluster().equals( uid.getOwner() ) ) {
                result |= BoundaryConstants.INCOMING ;
            }
        }

        PlanElementLog pel = pld.getPlanElementLogForTask( uid ) ;
        if ( pel == null ) {
            result |= BoundaryConstants.UNKNOWN ;  // I don't known where I am going since no PlanElement exists
        }
        else if ( !( pel instanceof AllocationLog ) )
        {
            if ( !( BoundaryConstants.isIncoming(result) || BoundaryConstants.isSource(result) ) ) {
                result |= BoundaryConstants.INTERNAL ;
            }
        }
        else {
            AllocationLog al = ( AllocationLog ) pel ;
            AssetLog asl = ( AssetLog ) pld.getLog( al.getAssetUID() ) ;

            // We can't find an asset log for this allocation.
            if ( asl == null ) {
                // Hmm, no asset is assigned to this allocation. Is this valid?
                result |= BoundaryConstants.UNKNOWN ;
            }
            else if ( asl.isFull() ) {
                // See if this asset describes a cluster.
                String targetCluster = asl.getClusterProperty() ;

                // Check to see if this a terminal node.
                if ( targetCluster == null ) { // I am allocated to an internal asset, non-organizational asset.
                    //System.out.println("\tNull cluster found in Asset " + asl + ", marking as terminal." ) ;
                    result |= BoundaryConstants.TERMINAL ;
                }
                // Check to see if this task is leaving this cluster, e.g. where this taskLog is from
                // and the asset's cluster do not match.
                else if ( targetCluster != null && ( !targetCluster.equals( tl.getCluster() ) ) ) {
                    result |= BoundaryConstants.OUTGOING ;
                }
                else if ( ! ( BoundaryConstants.isIncoming(result) || BoundaryConstants.isSource(result) ) ) {
                // I must be internal otherwise, since cluster == tl.getCluster()
                    result |= BoundaryConstants.INTERNAL ;
                }
                else {
                     result |= BoundaryConstants.UNKNOWN ;
                }
            }
            else {
                result |= BoundaryConstants.UNKNOWN ;
            }
        }

        return result ;
    }

    /**
     * @return An ArrayList of TaskLog elements, each of which are the ultimate descendents of
     * this taskLog.
     * @param terminalAndOutging A list of terminal tasks.
     * @param internal A list of internal tasks.
     */
    public void findLeafDescendants( TaskLog tl, ArrayList terminal, ArrayList outgoing, ArrayList internal, ArrayList unknown ) {
        if ( !( isAllocFromRemoteCluster( tl ) || tl.getParent() == null ) ) {
            throw new IllegalArgumentException( "Task log " + tl + " must be allocated from remote cluster or a source agent." ) ;
        }

        SimpleSearch search = new SimpleSearch( findDescendantsStrategy ) ;
        search.initNode( new PEGraphNode(tl) );
        search.run() ;

        for ( Enumeration enum = search.getClosedNodes() ; enum.hasMoreElements(); ) {
            PEGraphNode pen = ( PEGraphNode ) enum.nextElement() ;
            TaskLog taskLog = ( TaskLog ) pen.getLog() ;
            int type = getBoundaryType( taskLog ) ;

            // Ignore myself
            if ( taskLog == tl ) {
            }
            else if ( BoundaryConstants.isTerminal( type ) ) {
                terminal.add( taskLog ) ;
            }
            else if ( BoundaryConstants.isOutgoing( type ) ) {
                outgoing.add( taskLog ) ;
            }
            else if ( BoundaryConstants.isInternal( type ) ) {
                internal.add( taskLog ) ;
            }
            else if ( BoundaryConstants.isOutgoing( type ) ) {
                unknown.add( taskLog ) ;
            }
        }
    }

    /**
     * Find the parent entry point tasklogs for a MPTask. Not yet tested.
     */
    public TaskLog[] findParentsForMPTaskLog( MPTaskLog mptl, ArrayList deps ) {
        ArrayList result = new ArrayList() ;
        for (int i=0;i<mptl.getNumParents();i++) {
            UIDPDU uid = mptl.getParent() ;
            TaskLog tl = ( TaskLog ) pld.getLog( uid ) ;
            if ( tl == null ) {
               continue ;
            }
            if ( isAllocFromRemoteCluster( tl ))  {
                result.add( tl ) ;
            }
            else {
                TaskLog[] res = findParentEntryPointTaskLog( tl, deps ) ;
                if (res != null ) {
                    for (int j=0;j<res.length;j++) {
                        result.add( res[i] ) ;
                    }
                }
            }
        }
        TaskLog[] retval = new TaskLog[ result.size() ] ;
        for (int i=0;i<result.size();i++) {
            retval[i] = ( TaskLog ) result.get(i) ;
        }
        return retval ;
    }

    /**
     * Find one or more parent logs that are either entry points or has no
     * parent itself (and thus is an "originating" task).   Ordinarily, all outgoing tasks
     * are descendants of incoming ones, but in cases where tasks originate repeatedly, this
     * might prove useful.
     *
     * <P>  This has not yet been tested.
     *
     * @return null if no parents are known, or an array of dependent task logs. Note that if
     *  only some parents are known, this may still result in a non-null return value.
     * @param  deps Pass a non-null array list.  This will be filled up with dependencies
     * (UIDs) which are not currently referenced through the pld.
     * @param outgoing A non-incoming task.
     */
    public TaskLog[] findParentEntryPointTaskLog( TaskLog outgoing, ArrayList deps ) {
        TaskLog entryPoint = null ;
        UIDStringPDU current = ( UIDStringPDU ) outgoing.getUID() ;

        if ( isAllocFromRemoteCluster( outgoing ) ) {
            throw new IllegalArgumentException( "Outgoing tasklog " + outgoing + " is also an incoming task." ) ;
        }

        if ( outgoing instanceof MPTaskLog ) {
            return findParentsForMPTaskLog( ( MPTaskLog ) outgoing, deps )  ;
        }

        current = ( UIDStringPDU ) outgoing.getParent() ;

        if ( current == null ) {
            throw new IllegalArgumentException( "Outgoing tasklog " + outgoing + " must have non-null parent." ) ;
        }

        UniqueObjectLog uoLog = pld.getLog( current ) ;

        while ( true ) {
            if ( uoLog == null ) {
                // Hmm, we are dependent on this UID.
                deps.add( current ) ;
                return null ;
            }

            if ( uoLog instanceof MPTaskLog ) {
                MPTaskLog mptl = ( MPTaskLog ) uoLog ;
                return findParentsForMPTaskLog( mptl, deps ) ;
            }
            else if ( uoLog instanceof TaskLog ) {
                TaskLog currentTaskLog = ( TaskLog ) uoLog ;
                if ( isAllocFromRemoteCluster( currentTaskLog ))  {
                    return new TaskLog[] {currentTaskLog} ;
                }
                current = ( UIDStringPDU ) currentTaskLog.getParent() ;

                // If the parent is null, just return the current task log.
                if ( current == null ) {
                    return new TaskLog[] {currentTaskLog} ;
                }
                uoLog = pld.getLog( current ) ;
            }
        }
    }

    /**
     * Build aggregate input/output relationship graphs.
     */
    public void buildGraph() {
        Collection c = pld.getTasks() ;
        ArrayList incomingAndSource = new ArrayList();
        ArrayList outgoing = new ArrayList(), terminal = new ArrayList(),
                  unknown = new ArrayList(), internal = new ArrayList() ;

        //
        // Scan the tasklogs.  For each incoming and source type, find all its signficant descendents.
        //
        for ( Iterator iter = c.iterator();iter.hasNext(); ) {
            TaskLog o = ( TaskLog ) iter.next() ;

            if ( o instanceof MPTaskLog ) {
                 // MPTaskLogs are not
                 //System.out.println("\nSKIPPING MTTaskLog " + o );
                 continue ;
            }

            BoundaryTaskLog tl = ( BoundaryTaskLog ) o ;

            //System.out.println(" \nPROCESSING " + tl );
            int btype = getBoundaryType( tl ) ;
            tl.setBoundaryType( btype ) ;

            //if ( BoundaryConstants.isIncoming( btype ) && BoundaryConstants.isOutgoing( btype ) ) {
            //    System.out.println("FOUND RELAY TASK " + tl );
            //}
            //System.out.println( "\tBYTPE is " + BoundaryConstants.toParamString( btype ) );

            /**
             * I am incoming and terminal; just hook me to my parent.
             */
            if ( ( BoundaryConstants.isSource(btype) || BoundaryConstants.isIncoming( btype ) ) &&
                  BoundaryConstants.isTerminal( btype ) )
            {
                incomingAndSource.add( tl ) ;
                BoundaryLog ptl = ( BoundaryLog ) pld.getLog( tl.getParent() ) ;
                if ( ptl != null ) {
                    ptl.addOutgoingDescendent( tl );
                    tl.addIncomingAncestor( ( TaskLog ) ptl );
                }
            }
            /**
             * I am both a source/incoming AND an outgoing task and hence I am complete.
             */
            else if ( ( BoundaryConstants.isSource(btype) || BoundaryConstants.isIncoming( btype ) )
                    && BoundaryConstants.isOutgoing( btype ) ) {
                // I am both incoming and outgoing.  Connect myself to my parent and to my remote alloc task.
                incomingAndSource.add( tl ) ;
                BoundaryLog ptl = ( BoundaryLog ) pld.getLog( tl.getParent() ) ;
                if ( ptl != null ) {
                    ptl.addOutgoingDescendent( tl );
                    tl.addIncomingAncestor( ( TaskLog ) ptl );
                }

                // Look at any descedents from outgoing.
                AllocationLog al = ( AllocationLog ) pld.getPlanElementLogForTask( tl.getUID() ) ;
                if ( al != null ) {
                    BoundaryLog child = ( BoundaryLog ) pld.getLog( al.getAllocTaskUID() );
                    if ( child != null ) {
                        child.addIncomingAncestor( tl );
                        tl.addOutgoingDescendent( ( TaskLog ) child );
                    }
                }
            }
            // General case of incoming tasks.
            else if ( BoundaryConstants.isIncoming( btype ) || BoundaryConstants.isSource(btype) ) {
                // Mark this as an incoming and source.
                incomingAndSource.add( tl ) ;

                //System.out.println("\tFinding descendents:");
                outgoing.clear(); internal.clear(); unknown.clear(); terminal.clear() ;
                findLeafDescendants( tl, terminal, outgoing, internal, unknown );
                // Fill in descendents
                //System.out.println("\t\tOUTGOING " ) ;
                for (int i=0;i<outgoing.size();i++) {
                    //System.out.println( "\t\t" + outgoing.get(i) );
                    BoundaryLog otl = ( BoundaryLog ) outgoing.get(i) ;
                    tl.addOutgoingDescendent( ( TaskLog ) otl );
                    otl.addIncomingAncestor( tl ) ;
                    int type = getBoundaryType( ( TaskLog ) otl ) ;
                    //System.out.println("\t\t\tboundaryType=" + BoundaryConstants.toParamString( type ));
                }
                //System.out.println("\t\tTERMINAL \n" ) ;
                for (int i=0;i<terminal.size();i++) {
                    BoundaryLog ttl = ( BoundaryLog ) terminal.get(i) ;
                    //System.out.println( "\t\t" + terminal.get(i) );
                    tl.addOutgoingDescendent( ( TaskLog ) ttl );
                    ttl.addIncomingAncestor( tl ) ;
                }
                //System.out.println("#Internal=" + internal.size());

                // Fill in unknown (internal descendents that terminate with no plan element.)
                //System.out.println("\t\tUNKNOWN \n");
                for (int i=0;i<unknown.size();i++) {
                    //System.out.println("\t\t" +unknown.get(i) );
                    tl.addUnknownOrIncomplete( ( TaskLog ) unknown.get(i) );
                }

                // Fill in parent by adding myself as an outgoing descendent.
                if ( tl.getParent() != null ) {
                    BoundaryLog ptl = ( BoundaryLog ) pld.getLog( tl.getParent() ) ;
                    if ( ptl != null ) {
                        ptl.addOutgoingDescendent( tl );
                    }
                }
            }
        }

        //
        // Process all the incoming source tasks into BoundaryVerbTaskAggregates.
        //
        // System.out.println("\n\nMAKING AGGREGATES...\n");
        for (int i=0;i<incomingAndSource.size();i++) {
            BoundaryTaskLog btl = ( BoundaryTaskLog ) incomingAndSource.get(i) ;
            int btype = btl.getBoundaryType() ;
            if (  (BoundaryConstants.isIncoming(btype) || BoundaryConstants.isSource(btype) ) && !BoundaryConstants.isOutgoing(btype) ) {
                processIncomingSourceTask( btl );
            }
            else if ( (BoundaryConstants.isIncoming(btype) || BoundaryConstants.isSource(btype) ) && BoundaryConstants.isOutgoing(btype) ) {
                processForwardingTask(btl) ;
            }
            // Not sure if a special case is needed for terminating tasks.
        }
    }

    protected void processForwardingTask( BoundaryTaskLog btl ) {
        // System.out.println("Processing forwarding task" + btl );
        AllocationLog allocLog = ( AllocationLog ) pld.getPlanElementLogForTask( btl.getUID() ) ;
        AssetLog assetLog = ( AssetLog ) pld.getLog( allocLog.getAssetUID() ) ;
        String targetCluster = assetLog.getClusterProperty() ;

        TaskLog ptl = ( TaskLog ) pld.getLog( btl.getParent() ) ;
        String parent = ptl.getCluster() ;

        // We don't care about the source (yet)
        BoundaryVerbTaskAggregate agg =
            checkTaskAggregate( btl.getBoundaryType(), btl.getTaskVerb().toString(), btl.getCluster(), targetCluster, null ) ;
        agg.logInstance( btl ) ;
        uidToAggregateMap.put( btl.getUID(), agg ) ;

        // Get the parent with target myself.
        BoundaryVerbTaskAggregate pagg = checkTaskAggregate( ( ( BoundaryLog ) ptl ).getBoundaryType(),
                ptl.getTaskVerb(), parent, btl.getCluster(), null ) ;
        pagg.logChildAggregateLog( agg, btl ) ;

        /**
         * Hook up the descendents as an incoming to the next.
         */
        for ( Iterator iter=btl.getOutgoingDescendents();iter.hasNext();) {
            BoundaryTaskLog ctl = ( BoundaryTaskLog ) iter.next() ;
            String target = null ;
            // Get the target cluster for the child iff the child is an relay/forwarding
            // child as well.
            if ( BoundaryConstants.isOutgoing( ctl.getBoundaryType() ) ) {
                Iterator citer = ctl.getOutgoingDescendents() ;
                if ( citer.hasNext() ) {
                    TaskLog tl = ( TaskLog ) citer.next() ;
                    target = tl.getCluster() ;
                }
            }

            // Hook up all the children
            BoundaryVerbTaskAggregate cagg =
                checkTaskAggregate( ctl.getBoundaryType(), ctl.getTaskVerb().toString(),
                                    ctl.getCluster(), target, null ) ;
            cagg.logInstance( ctl ) ;
            agg.logChildAggregateLog( cagg, ctl ) ;
            cagg.logParent( agg );
        }
    }

    protected void processIncomingSourceTask( BoundaryTaskLog tl ) {
        BoundaryVerbTaskAggregate agg =
            checkTaskAggregate( tl.getBoundaryType(), tl.getTaskVerb().toString(), tl.getCluster(), null, null ) ;
        agg.logInstance( tl ) ;
        uidToAggregateMap.put( tl.getUID(), agg ) ;
        //System.out.println("PROCESSING INCOMING " + tl );
        //System.out.println("\tUsing " + agg );

        // Hook up parent task log ( should be from another cluster.)
        BoundaryTaskLog pbtl = ( BoundaryTaskLog ) pld.getLog( tl.getParent() ) ;
        if ( pbtl != null ) {
            BoundaryVerbTaskAggregate pagg =
                checkTaskAggregate( pbtl.getBoundaryType(), pbtl.getTaskVerb().toString(),
                                    pbtl.getCluster(), tl.getCluster(), null ) ;
            pagg.logChildAggregateLog( agg, tl ) ;
            agg.logParent( pagg );
        }

        // Hook up descendents.  They may be within this cluster, or may be outside this cluster
        // (if INCOMING | OUTGOING ) are both true, for example.
        for ( Iterator iter=tl.getOutgoingDescendents();iter.hasNext();) {
            TaskLog btl = ( TaskLog ) iter.next() ;
            AllocationLog allocLog = ( AllocationLog ) pld.getPlanElementLogForTask( btl.getUID() ) ;
            AssetLog assetLog = ( AssetLog ) pld.getLog( allocLog.getAssetUID() ) ;
            String targetCluster = assetLog.getClusterProperty() ;

            // Note. Outgoing tasks are classified by targetCluster as well as type.
            BoundaryVerbTaskAggregate cagg =
                checkTaskAggregate( ( ( BoundaryLog )btl ).getBoundaryType(), btl.getTaskVerb().toString(),
                                    btl.getCluster(), targetCluster, null ) ;
            // System.out.println("\tLogging child aggregate log " + cagg );
            cagg.logInstance( btl ) ;
            uidToAggregateMap.put( btl.getUID(), cagg ) ;
            agg.logChildAggregateLog( cagg, btl ) ;
            cagg.logParent( agg );
        }
    }

    /**
     * Process tasks showing up at an agent as injected from some other agent.
     * @deprecated.
     */
    private boolean processInjectedTask(TaskPDU tpdu) {
        TaskLog log = ( TaskLog ) pld.getLog( tpdu.getUID() ) ;
        UIDStringPDU uid = ( UIDStringPDU ) tpdu.getUID() ;

        if ( log == null ) {
            throw new RuntimeException( "TaskPDU " + tpdu + " does not have associated TaskLog. This should never happen." ) ;
        }

        // Get a tasklog for this task.
        BoundaryVerbTaskAggregate agg =
            checkTaskAggregate( BoundaryConstants.INCOMING, tpdu.getTaskVerb().toString(), tpdu.getSource(), null, null ) ;
        // System.out.println("Task aggregate " + agg + " used for " + tpdu );

        // Map uids to aggregate logs.
        agg.logInstance( log ) ;
        uidToAggregateMap.put( tpdu.getUID(), agg ) ;

        // Found a root.
        if ( tpdu.getParentTask() == null ) {
            return true ;
        }

        // FIND A PARENT BVTL
        // Find out who is my parent and see if there is an aggregrate log associated with him.
        TaskLog ptl = ( TaskLog ) pld.getLog( tpdu.getParentTask() ) ;

        BoundaryVerbTaskAggregate pagg =
                getTaskAggregate( BoundaryConstants.OUTGOING, ptl.getTaskVerb(), ptl.getCluster(),
                        tpdu.getSource(), null  ) ;

        // A parent task log exists.  Implies a parent agg. log should also exist but has not been processed for some
        // reason.
        if ( ptl != null ) {
            if ( pagg == null ) {
                return false ;  // Delay processing of this PDU until later.
            }

            pagg.logChildAggregateLog( agg, log ) ;
            if ( sourcePoints.get( agg ) != null ) {
                sourcePoints.remove( agg ) ;
            }
        }
        // Mark this tpdu as a source point since we know nothing about the parent.
        // Retract this later if necessary.
        else if ( pagg == null && ptl == null ) {
            addEntryPoint( agg );
        }

        // Relate parent and child aggregate logs.
        return true ;
    }

    public Iterator getEntryPoints() {
        return sourcePoints.values().iterator() ;
    }

    public Iterator getExitPoints() {
        return exitPoints.values().iterator() ;
    }

    protected void addEntryPoint( BoundaryVerbTaskAggregate agg ) {
        if ( sourcePoints.get( agg ) == null ) {
            sourcePoints.put( agg, agg ) ;
        }
    }

    protected void removeEntryPoint( BoundaryVerbTaskAggregate agg ) {
        if ( sourcePoints.get( agg ) == null ) {
            sourcePoints.remove( agg ) ;
        }
    }

    protected void addExitPoint( BoundaryVerbTaskAggregate agg ) {
        if ( exitPoints.get( agg ) == null ) {
            exitPoints.put( agg, agg ) ;
        }
    }

    /**
     * @deprecated.
     */
    protected boolean processOutgoingTask( TaskPDU pdu, ArrayList dependencies ) {
        UIDStringPDU uid = ( UIDStringPDU ) pdu.getUID() ;
        BoundaryTaskLog tl = ( BoundaryTaskLog ) pld.getLog( uid ) ;
        PlanElementLog log = (PlanElementLog) pld.getPlanElementLogForTask( uid ) ;
        if ( log == null ) {
            return false ;
        }
        // We are allocated to an asset (and hence outgoing.)
        if ( log instanceof AllocationLog ) {
            AllocationLog alloc = ( AllocationLog ) log ;
            UIDPDU assetUID = alloc.getAssetUID() ;
            if ( alloc.getAllocTaskUID() != null ) {
                // Check to see if the task is allocated to a specific asset which we know about
                AssetLog alog = ( AssetLog ) pld.getLog( assetUID ) ;
                if ( alog == null ) {
                    dependencies.add( assetUID ) ;
                    return false ;
                }

                // What agent does this asset represent, if any.
                String clusterAsset = alog.getClusterProperty() ;

                // This isn't a cluster, or the cluster  Return true immediately.
                if ( clusterAsset == null || clusterAsset.equals( tl.getCluster() ) ) {
                    return true ;
                }

                // Find the BVTL parents by tracing back.  If I cannot find any entry points, return false
                TaskLog[] entryPoints = findParentEntryPointTaskLog( tl, dependencies ) ;
                if ( entryPoints == null ) {
                    return false ;
                }

                // Now, actually create the BoundaryVerbTaskAggregate if neccessary.
                BoundaryVerbTaskAggregate ba =
                        checkTaskAggregate( BoundaryConstants.OUTGOING, tl.getTaskVerb(),
                                            tl.getCluster(), clusterAsset, null ) ;
                ba.logInstance( tl ) ;
                uidToAggregateMap.put( tl.getUID(), ba ) ;

                // Find the BVTL child by getting the organization id from the asset.
                // getTaskAggregate( BoundaryVerbTaskAggregate.INJECTED, tl.getTaskVerb(), ) ;
                for (int i=0;i<entryPoints.length;i++ ) {
//                    incoming.add( entryPoints[i] ) ;
                    BoundaryTaskLog btl = ( BoundaryTaskLog ) entryPoints[i] ;
                    if ( !btl.isOutgoingDescendent( tl ) ) {
                        btl.addOutgoingDescendent( tl );
                    }
                    tl.addIncomingAncestor( entryPoints[i] );
                }
            }
            else {
                // This is assigned to a local asset.
                // Do nothing since we don't collect any statistics for local allocations.
                return true ;
            }
        }
        return true ;
    }

    protected boolean processTask( TaskPDU pdu, ArrayList dependencies ) {
        UIDStringPDU uid = ( UIDStringPDU ) pdu.getUID() ;
        if ( !pdu.getSource().equals( uid.getOwner() ) || pdu.getParentTask() == null ) {
            // I am injected from outside.
            return processInjectedTask(pdu);
        }
        else {
            return processOutgoingTask( pdu, dependencies ) ;
        }
    }

    protected AgentDesc checkAgentDescForName( String agentName ) {
        AgentDesc ad = society.getAgent( agentName ) ;
        if ( ad == null ) {
            ad = society.addAgent( agentName ) ;
        }
        return ad ;
    }

    /**
     * @return true if the pdu was successfully processed, false if the result is unknown.
     */
    public boolean processPDU( PDU epdu, ArrayList dependencies ) {
        if ( epdu instanceof DeclarePDU ) {
            processDeclare(epdu);
            return true ;
        }
        else if ( epdu instanceof UniqueObjectPDU ) {
            UniqueObjectPDU pdu = ( UniqueObjectPDU ) epdu ;
            checkAgentDescForName( epdu.getSource() ) ;

            UIDStringPDU uid = ( UIDStringPDU ) pdu.getUID() ;
            if ( uid == null ) {
                throw new IllegalArgumentException( "PDU " + pdu + " has null UID." ) ;
            }

            if ( epdu instanceof TaskPDU ) {
                return processTask( ( TaskPDU ) epdu, dependencies ) ;
            }
        }
        return true ;
    }

    public Iterator getAggregates() {
        return boundaryTaskAggregateMap.values().iterator() ;
    }

    public BoundaryVerbTaskAggregate getAggregateLogForTask( UIDPDU uid ) {
        return ( BoundaryVerbTaskAggregate ) uidToAggregateMap.get( uid ) ;
    }


    public synchronized BoundaryVerbTaskAggregate checkTaskAggregate( int type, String verb, String agent, String target,
                                                                      String source ) {
        TypeVerbClusterTuple tuple = new TypeVerbClusterTuple( type, verb, agent, target, source );
        BoundaryVerbTaskAggregate agg = ( BoundaryVerbTaskAggregate ) boundaryTaskAggregateMap.get( tuple ) ;

        if ( agg == null ) {
            agg = new BoundaryVerbTaskAggregate( type, agent, verb, source, target  ) ;
            boundaryTaskAggregateMap.put( tuple, agg ) ;
        }
        return agg ;
    }

    public BoundaryVerbTaskAggregate getTaskAggregate( int type, String verb, String agent, String target, String source ) {
        TypeVerbClusterTuple tuple = new TypeVerbClusterTuple( type, verb, agent, target, source );
        BoundaryVerbTaskAggregate agg = ( BoundaryVerbTaskAggregate ) boundaryTaskAggregateMap.get( tuple ) ;
        return agg ;
    }

    private void processDeclare(PDU epdu) {
        DeclarePDU dpdu = ( DeclarePDU ) epdu ;

        String agentName = dpdu.getName() ;
        String nodeName = dpdu.getNodeIdentifier() ;

        // Create a node descriptor
        NodeDesc ndesc = society.getNode( nodeName ) ;
        if ( ndesc == null ) {
            ndesc = society.addNode( nodeName ) ;
        }

        // Create an agent descriptor if one doens't exist.
        AgentDesc ad= checkAgentDescForName( agentName ) ;
        ad.addObservedNodeStart( nodeName, dpdu.getTime() );
        ndesc.addAgent( agentName ) ;
    }

    /**
     * Classes of tasks which are source points for this society.
     */
    protected HashMap sourcePoints = new HashMap() ;

    /**
     * Classes of tasks going out of this society.
     */
    protected HashMap exitPoints = new HashMap() ;

    /**
     * Classes of tasks which are assigned to assets within this society.
     */
    protected HashMap sinkPoints = new HashMap() ;

    /**
     * A list of boundary tasks.
     */
    protected HashMap boundaryTaskAggregateMap = new HashMap() ;

    protected HashMap uidToAggregateMap = new HashMap() ;

    /**
     * SocietyDesc descriptor.
     */
    protected SocietyDesc society ;

    /**
     * This keeps a complete record.
     */
    protected PlanLogDatabase pld ;

    protected IntraAgentSubgraphStrategy strategy, findDescendantsStrategy ;
}
