/*
  * <copyright>
  *  Copyright 2002 (Intelligent Automation, Inc.)
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
  */

package org.cougaar.tools.castellan.analysis;


import org.cougaar.tools.castellan.pdu.*;
import java.util.* ;
import org.cougaar.tools.castellan.util.* ;
import org.cougaar.tools.castellan.server.* ;

/**
 *  Build a log plan based on a stream of messages.
 */

public class LogPlanBuilder implements PDUSink {

    public LogPlanBuilder( PlanLogDatabase pld ) {
        this.pld = pld ;
    }

    public PlanLogDatabase getDatabase() { return pld ; }

    public Iterator getAssets() { return pld.getAssets() ; }

    public ArrayList getRootTasks() {
        ArrayList results = new ArrayList() ;
        for ( Iterator iter = pld.getTasks().iterator();
             iter.hasNext(); )
        {
            TaskLog ul = ( TaskLog ) iter.next() ;
            if ( ul instanceof MPTaskLog ) {
                MPTaskLog mpt = ( MPTaskLog ) ul ;
                if ( mpt.getNumParents() == 0 ) {
                    results.add( mpt ) ;
                }
            }
            else if ( ul.getParent() == null ) {
                results.add( ul ) ;
            }
            else if ( pld.getLog( ul.getParent() ) == null ) {
                results.add( ul ) ;
            }
        }
        return results ;
    }

    /**
     * Note: Process allocation result PDUs should be processed a second pass, after
     * all the processing is done for the standard PDUs.
     */
    public void processARPDU( AllocationResultPDU arpdu ) {

        UIDStringPDU uid = ( UIDStringPDU ) arpdu.getUID() ;
        PlanElementLog pel = getPlanElementForTask( uid ) ;
        if ( pel != null ) {
            pel.addAR( arpdu );
        }
    }

    public boolean acceptTask( TaskPDU pdu ) {
        if ( pdu.getTaskVerb().equals( "ReportForDuty" ) || pdu.getTaskVerb().equals( "ReportForService" ) ) {
            return false ;
        }
        return true ;
    }

    public void processPDU( PDU m ) {
        try {
            if ( m instanceof AssetPDU ) {
                System.out.print( "A" ) ;
                processAssetPDU( ( AssetPDU ) m ) ;
            }
            else if ( m instanceof AllocationPDU ) {
                System.out.print( "L" ) ;
                processAllocationPDU( ( AllocationPDU ) m ) ;
            }
            else if ( m instanceof MPTaskPDU ) {
                System.out.print( "M" ) ;
                processMPTaskPDU( ( MPTaskPDU ) m ) ;
            }
            else if ( m instanceof TaskPDU ) {
                System.out.print( "T" ) ;
                if ( acceptTask( ( TaskPDU ) m ) ) {
                    processTaskPDU( ( TaskPDU ) m ) ;
                }
            }
            else if ( m instanceof ExpansionPDU ) {
                System.out.print( "X" ) ;
                processExpansionPDU( ( ExpansionPDU ) m );
            }
            else if ( m instanceof AggregationPDU ) {
                System.out.print( "G" ) ;
                processAggregationPDU( ( AggregationPDU ) m );
            }
        }
        catch ( Exception e ) {
            System.out.println( "\nEXCEPTION PROCESSING MESSAGE " + m ) ;
            e.printStackTrace();
        }
    }

    protected PlanElementLog getPlanElementForTask( UIDPDU taskUID ) {
        PlanElementLog pel = pld.getPlanElementLogForTask( taskUID ) ;
        return ( PlanElementLog ) pel ;
    }

    protected AllocationLog getAllocationForTask( UIDPDU taskUID ) {
        PlanElementLog pel = pld.getPlanElementLogForTask( taskUID ) ;
        if ( pel instanceof AllocationLog ) {
            return ( AllocationLog ) pel ;
        }
        return null ;
    }

    protected AggregationLog getAggregationForTask( UIDPDU taskUID ) {
        PlanElementLog pel = pld.getPlanElementLogForTask( taskUID ) ;
        if ( pel instanceof AggregationLog ) {
            return ( AggregationLog ) pel ;
        }
        return null ;
    }

    protected ExpansionLog getExpansionForTask( UIDPDU taskUID ) {
        PlanElementLog pel = pld.getPlanElementLogForTask( taskUID ) ;
        if ( pel instanceof ExpansionLog ) {
            return ( ExpansionLog ) pel ;
        }
        return null ;
    }

    protected MPTaskLog checkMPTaskForMessage( MPTaskPDU m ) {
        Loggable o = pld.getLog( m.getUID() ) ;

        // Handle previously empty TaskLog instances
        if ( o != null && o instanceof MPTaskLog ) {
            MPTaskLog tl = ( MPTaskLog ) o ;
            if ( !tl.isFull() && m.getAction() == EventPDU.ACTION_ADD ) {
                tl.setCreatedTimestamp(m.getTime(),m.getExecutionTime());
                tl.setTaskVerb( ( ( SymStringPDU ) m.getTaskVerb() ).toString() ) ;
                // MPTask does not know its direct parent?
                //tl.setParent( m.getParent() ) ;
                //tl.setParentType( m.getParentType() ) ;
                tl.setCluster( m.getSource() ) ;

                // If a parent task has been shadowed, remove it if the message indicates
                // a parent task which is non-null, indicating that the parent is either
                // a task or an expansion.
                // checkTaskForShadowedParentAsset( m.getParent(), tl ) ;

                //if ( m.getParent() != null ) {
                //    tl.setParent( m.getParent() );
                //    tl.setParentType( m.getType() );
                //}

                tl.setFull( true );
            }
            else if ( m.getAction() == EventPDU.ACTION_REMOVE ) {
                tl.setRescindedTimestamp( m.getTime(), m.getExecutionTime() ) ;
            }
            return tl ;
        }


        if ( (o != null) && !(o instanceof MPTaskLog) ) {
            System.out.println( "Unexpected error: message for UID "
            + m.getUID() + " does not match existing TaskLog object." ) ;
            return null ;
        }

        long time = -1, executionTime = -1 ;
        if ( m.getAction() == EventPDU.ACTION_ADD ) {
            time = m.getTime() ; executionTime = m.getExecutionTime() ;
        }

        //System.out.println( "Adding asset log " + m.getUID() + " of type " + m.getAssetTypeId() ) ;
        MPTaskLog log = new MPTaskLog( m.getUID(), m.getSource(), m.getTaskVerb().toString(), time, executionTime ) ;
        addLog( log ) ;
        return log ;
    }

    protected AggregationLog checkAggregationLogForMessage( AggregationPDU am ) {
        AggregationLog al = ( AggregationLog ) getLog( am.getUID() ) ;
        if ( al == null && am.getAction() == EventPDU.ACTION_ADD ) {
            al = new AggregationLog( am.getCombinedTask(), am.getTask(), am.getUID(), am.getSource(),
            am.getTime(), am.getExecutionTime() ) ;
        }
        else if ( am.getAction() == EventPDU.ACTION_REMOVE ) {
            al.setRescindedTimestamp( am.getTime(), am.getExecutionTime() ) ;
        }

        addLog( al ) ;
        return al ;
    }

    public static boolean isSourceAsset( UIDPDU uid ) {
        if ( uid instanceof UIDStringPDU ) {
            UIDStringPDU updu = ( UIDStringPDU ) uid ;
            return updu.getId() == UIDPDU.SOURCE_ASSET ;
        }
        return false ;
    }

    public static boolean isSinkAsset( UIDPDU uid ) {
        if ( uid instanceof UIDStringPDU ) {
            UIDStringPDU updu = ( UIDStringPDU ) uid ;
            return updu.getId() == UIDPDU.SINK_ASSET ;
        }
        return false ;
    }

    public static final String SOURCEASSET = "SOURCEASSET" ;
    public static final String SINKASSET = "SINKASSET " ;

    public AssetLog getSourceAsset( TaskLog taskLog ) {
        UIDStringPDU updu = ( UIDStringPDU ) taskLog.getUID() ;
        UIDStringPDU sourceAssetUID = new UIDStringPDU( updu.getOwner(), UIDPDU.SOURCE_ASSET ) ;
        AssetLog alog = checkAsset( sourceAssetUID ) ;
        alog.setAssetTypeId( SOURCEASSET ) ;
        return alog ;
    }

    public AssetLog getSinkAsset( TaskLog taskLog ) {
        UIDStringPDU updu = ( UIDStringPDU ) taskLog.getUID() ;
        UIDStringPDU sinkAssetUID = new UIDStringPDU( updu.getOwner(), UIDPDU.SINK_ASSET ) ;
        AssetLog alog = checkAsset( sinkAssetUID ) ;
        alog.setAssetTypeId( SINKASSET ) ;
        return alog ;
    }

    /** Given a task, find the asset to which its plan element has been assigned.
     * This only works for tasks whose planElement is an Allocation to a particular task.
     */
    public AssetLog getAssetForTask( UIDPDU task ) {
        PlanElementLog pel = ( PlanElementLog ) getPlanElementForTask( task ) ;
        if ( pel == null || !( pel instanceof AllocationLog ) ) {
            return null ;
        }
        AllocationLog allocLog = ( AllocationLog ) pel ;
        if ( allocLog == null || allocLog.getAssetUID() == null ) { return null ; }
        AssetLog alog = checkAsset( allocLog.getAssetUID() ) ;
        return alog ;
    }

    //protected void addTask( TaskLog log ) {
    //}

    protected void addLog( UniqueObjectLog log ) {
        pld.addLog( log );
    }

    protected UniqueObjectLog getLog( UIDPDU uid ) {
        if ( uid == null ) {
            return null ;
        }
        return pld.getLog( uid ) ;
    }

    protected ExpansionLog checkExpansionForMessage( ExpansionPDU m ) {
        Object tmp = getLog( m.getUID() ) ;
        if ( tmp != null && !( tmp instanceof ExpansionLog ) ) {
            System.out.println( "UNEXPECTED ERROR: checkExpansionForMessage " + tmp + " is not an expansion log." ) ;
            return null ;
        }
        ExpansionLog o = ( ExpansionLog ) tmp ;
        if ( o != null && ( ( !o.isFull() &&  m.getAction() == EventPDU.ACTION_ADD ) ||
        ( m.getAction() == EventPDU.ACTION_CHANGE ) ) ) {
            o.setParent( m.getParentTask() ) ;
            o.setChildren( m.getTasks() );
            if ( m.getAction() == EventPDU.ACTION_ADD ) {
                o.setCreatedTimestamp( m.getTime(), m.getExecutionTime() ) ;
            }
            else if ( m.getAction() == EventPDU.ACTION_REMOVE ) {
                o.setRescindedTimestamp( m.getTime(), m.getExecutionTime() ) ;
            }
        }

        if ( o == null ) {
            o = new ExpansionLog( m.getParentTask(), m.getTasks(), m.getUID(),
            m.getSource(), m.getTime(), m.getExecutionTime() ) ;
            addLog( o );
        }

        // System.out.println( "Expansion log is " + o ) ;
        return o ;
    }

    protected TaskLog checkTask( UIDPDU uid ) {
        UniqueObjectLog o = getLog( uid ) ;
        if ( o != null && o instanceof TaskLog ) {
            return ( TaskLog ) o ;
        }
        if ( (o != null) && !(o instanceof TaskLog) ) {
            System.out.println( "Unexpected error: message for UID "
            + uid + " does not match existing TaskLog object." ) ;
            return null ;
        }
        TaskLog log = new TaskLog( uid ) ;
        addLog( log ) ;
        return log ;
    }

    private void fillInParentTask( TaskPDU m, TaskLog tl ) {
        if ( m.getParentTask() != null ) {
            tl.setParent( m.getParentTask() );

            UniqueObjectLog pl = ( UniqueObjectLog ) getLog( m.getParentTask() ) ;
            PlanElementLog pel = getPlanElementForTask(m.getParentTask() ) ;
            if ( pel != null ) {

            }
            else {
                tl.setParentType( TaskLog.TYPE_UNKNOWN ) ;
            }
            //tl.setParentType( TaskLog.TYPE_FORWARDED );
            //checkTaskForShadowedParentAsset( m.getParentTask(), tl ) ;
        }
        else {
            PlanElementLog pel = getPlanElementForTask( m.getUID() ) ;
            if ( pel != null ) {
                if ( pel instanceof ExpansionLog ) {
                    tl.setParent( pel.getUID() ) ;
                    tl.setParentType( TaskLog.TYPE_EXPANSION ) ;
                }
                else if ( pel instanceof AggregationLog ) {
                    tl.setParent( pel.getUID() ) ;
                    tl.setParentType( TaskLog.TYPE_AGGREGATION ) ;
                }
            }
        }
    }

    /** Create and/or return a task log for a task message.  Each task is parented to either
     *  another task (by intercluster forwarding), If the parent of the task is known,
     *  fill in the parent information at this point.
     */
    protected TaskLog checkTaskForMessage( TaskPDU m ) {
        Loggable o = pld.getLog( m.getUID() ) ;

        // Handle previously empty TaskLog instances
        if ( o != null && o instanceof TaskLog ) {
            TaskLog tl = ( TaskLog ) o ;
            if ( !tl.isFull() && m.getAction() == EventPDU.ACTION_ADD ) {
                tl.setCreatedTimestamp(m.getTime(),m.getExecutionTime());
                tl.setTaskVerb( m.getTaskVerb().toString() ) ;
                tl.setParent( m.getParentTask() ) ;
                tl.setCluster( m.getSource() ) ;
                // fillInParentTask( m, tl ) ;
                tl.setFull( true );
            }
            else if ( m.getAction() == EventPDU.ACTION_REMOVE  ) { // Remove task
                tl.setRescindedTimestamp( m.getTime(), m.getExecutionTime() ) ;
            }
            return tl ;
        }

        if ( (o != null) && !(o instanceof TaskLog) ) {
            System.out.println( "Unexpected error: message for UID "
            + m.getUID() + " does not match existing TaskLog object." ) ;
            return null ;
        }

        long time = -1, executionTime = -1 ;
        if ( m.getAction() == EventPDU.ACTION_ADD ) {
            time = m.getTime() ; executionTime = m.getExecutionTime() ;
        }

        //System.out.println( "Adding asset log " + m.getUID() + " of type " + m.getAssetTypeId() ) ;
        TaskLog log =
        new TaskLog( m.getUID(), m.getSource(), m.getTaskVerb().toString().intern() , time, executionTime ) ;
        log.setParent( m.getParentTask() ) ;
        // fillInParentTask( m, log ) ;
        addLog( log ) ;
        return log ;
    }

    private AssetLog checkAsset( UIDPDU uid ) {
        Loggable o = pld.getLog( uid ) ;
        if ( o != null && o instanceof AssetLog ) {
            AssetLog al = ( AssetLog ) o ;
            return al ;
        }
        //System.out.println( "\nCREATING HOLLOW ASSET " + uid ) ;
        AssetLog assetLog = new AssetLog( uid ) ;
        addLog( assetLog ) ;
        return assetLog ;
    }

    private AssetLog checkAssetForMessage( AssetPDU am ) {
        Loggable o = pld.getLog( am.getUID() ) ;
        if ( o != null && o instanceof AssetLog ) {
            AssetLog al = ( AssetLog ) o ;
            if ( (!al.isFull() && am.getAction() == EventPDU.ACTION_ADD) ||
            am.getAction() == EventPDU.ACTION_CHANGE ) {
                // Fixup hollow and/or changed
                al.setAssetTypeId( am.getAssetTypeId() );
                if ( am.getPropertyGroups() != null ) {
                    al.setPropertyGroups( am.getPropertyGroups() );
                }
                al.setItemId( am.getItemId() );
                al.setAssetTypeNomenclature( am.getAssetTypeNomenclature() );
                al.setClassName( am.getAssetClass().toString() );
                al.setCreatedTimestamp( am.getTime(), am.getExecutionTime() );
                al.setCluster( am.getSource() );
                al.setFull(true);
            }
            return al ;
        }

        if ( (o != null) && !(o instanceof AssetLog) ) {
            ServerApp.instance().println( "Unexpected error: message for UID "
            + am.getUID() + " does not match existing Log object." ) ;
        }

        long time = -1, executionTime = -1 ;
        if ( am.getAction() == AssetPDU.ACTION_ADD ) {
            time = am.getTime() ; executionTime = am.getExecutionTime() ;
        }

        if ( ServerApp.instance().isVerbose() ) {
            System.out.println( "Adding asset log " + am.getUID() + " of type " + am.getAssetTypeId() ) ;
        }
        AssetLog assetLog = new AssetLog( am.getUID(), am.getSource(),
                am.getItemId(), am.getAssetTypeId(), am.getAssetTypeNomenclature(), time, executionTime ) ;
        assetLog.setClassName( am.getAssetClass().toString() );
        if ( am.getPropertyGroups() != null ) {
            assetLog.setPropertyGroups( am.getPropertyGroups() );
        }
        if ( am.getAction() == EventPDU.ACTION_ADD ) {
            assetLog.setFull( true );
        }
        addLog( assetLog ) ;
        return assetLog ;
    }

    private AllocationLog checkAllocationForMessage( AllocationPDU am ) {
        Loggable o = pld.getLog( am.getUID() ) ;
        if ( o != null && o instanceof AllocationLog ) {
            AllocationLog l = ( AllocationLog ) o ;
            if ( !l.isFull() && am.getAction() == EventPDU.ACTION_ADD) {
                // Fixup empty log.
                l.setAssetUID( am.getAsset() );
                l.setParent( am.getTask() );
                l.setAllocTaskUID( am.getAllocTask() ) ;
                l.setCreatedTimestamp( am.getTime(), am.getExecutionTime() );
                l.setFull(true);
            }
            else if ( am.getAction() == EventPDU.ACTION_CHANGE ) {
                l.setAssetUID( am.getAsset() );
                l.setParent( am.getTask() );
            }
            else if ( am.getAction() == EventPDU.ACTION_REMOVE ) {
                l.setRescindedTimestamp( am.getTime(), am.getExecutionTime() ) ;
            }
            return l ;
        }

        if ( (o != null) && !(o instanceof AllocationLog) ) {
            System.out.println( "Unexpected error: message for UID "
            + am.getUID() + " does not match existing Log object." ) ;
        }

        AllocationLog log = new AllocationLog( am.getUID(), am.getTask(), am.getAsset(), am.getAllocTask() ) ;
        addLog( log ) ;
        return log ;
    }

    private void processExpansionPDU( ExpansionPDU m ) {
        if ( ServerApp.instance().isVerbose() ) {
            System.out.println( "\nPROCESSING expansion message " + m  ) ;
        }

        // Make an expansion log only when the message is add.
        ExpansionLog el = checkExpansionForMessage( m ) ;
    }

    private synchronized void processAggregationPDU( AggregationPDU m ) {
        UIDPDU mpTask = m.getCombinedTask() ;
        MPTaskLog l = ( MPTaskLog ) getLog( mpTask ) ;
        if ( l == null ) {  // Wait for the MPTask to be processed first.
            enqueueMessage( mpTask, m ) ;
            return ;
        }

        if ( ServerApp.instance().isVerbose() ) {
            System.out.println( "\nPROCESSING aggregation message " + m ) ;
        }

        AggregationLog log = checkAggregationLogForMessage( m ) ;
        //TaskLog tl = ( TaskLog ) getLog( log.getParent() ) ;
        l.addParent( m.getTask() ) ;
    }

    private void processAssetPDU( AssetPDU am ) {
        // Look for an existing AssetLog.
        AssetLog log = checkAssetForMessage( am ) ;
    }

    private void processMPTaskPDU( MPTaskPDU tm ) {
        if ( ServerApp.instance().isVerbose() ) {
            System.out.println( "\nPROCESSING MPTASK message:  " + tm ) ;
        }

        MPTaskLog mp = checkMPTaskForMessage( tm ) ;
        processQueuedMessages(tm.getUID()) ;
    }

    private void processTaskPDU( TaskPDU tm ) {
        if ( ServerApp.instance().isVerbose() ) {
            System.out.println( "\nPROCESSING TASK message:  " + tm ) ;
        }

        if ( tm instanceof MPTaskPDU ) {
           System.out.println( "!" ) ;
        }

        TaskLog log = checkTaskForMessage( tm ) ;
        PlanElementLog pel = getPlanElementForTask( tm.getUID() ) ;
    }

    private void processAllocationPDU( AllocationPDU am ) {
        if ( ServerApp.instance().isVerbose() ) {
            System.out.println( "\nPROCESSING ALLOCATION MESSAGE " + am ) ;
        }

        AllocationLog allocationLog = checkAllocationForMessage(am) ;
        UIDStringPDU assetUID = ( UIDStringPDU ) am.getAsset() ;
        TaskLog taskLog = ( TaskLog ) getLog( am.getTask() ) ;
    }

    private void enqueueMessage( UIDPDU uid, PDU m ) {
        if ( ServerApp.instance().isVerbose() ) {
            System.out.println( "Enqueuing " + m + " on UID " + uid ) ;
        }
        System.out.print( "+" ) ;
        delayedSet.put( uid, m ) ;
    }

    private void processQueuedMessages( UIDPDU uid ) {
        Object[] messages = delayedSet.removeObjects( uid ) ;
        if ( messages == null ) {
            return ;
        }

        if ( messages.length > 0 ) {
            if ( ServerApp.instance().isVerbose() ) {
                System.out.println( "\tRemoving " + messages.length + " objects from the delayed processing queue for " + uid ) ;
            }
        }

        for (int i=0;i<messages.length;i++) {
            if ( ServerApp.instance().isVerbose() ) {
                System.out.println( "Processing " + messages[i] ) ;
            }
            System.out.print( '-' ) ;
            processPDU( ( PDU ) messages[i] ) ;
        }
    }

    protected MultiHashSet delayedSet = new MultiHashSet() ;
    PlanLogDatabase pld ;
}