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

import org.cougaar.tools.castellan.analysis.*;
import org.cougaar.tools.castellan.pdu.*;

public class AgentWorkflowLogPlanBuilder extends LogPlanBuilder {
    public AgentWorkflowLogPlanBuilder(PlanLogDatabase pld) {
        super(pld);
    }

    protected MPTaskLog checkMPTaskForMessage(MPTaskPDU m) {
        Loggable o = getDatabase().getLog( m.getUID() ) ;

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
        BoundaryMPTaskLog log = new BoundaryMPTaskLog( m.getUID(), m.getSource(), m.getTaskVerb().toString(), time, executionTime ) ;
        addLog( log ) ;
        return log ;
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
        BoundaryTaskLog log = new BoundaryTaskLog( uid ) ;
        addLog( log ) ;
        return log ;
    }



    /**
     * Override to create BoundaryTaskLogs.
     */
    protected TaskLog checkTaskForMessage( TaskPDU m ) {
        Loggable o = getDatabase().getLog( m.getUID() ) ;

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
            new BoundaryTaskLog( m.getUID(), m.getSource(), m.getTaskVerb().toString().intern() , time, executionTime ) ;
        log.setParent( m.getParentTask() ) ;
        // fillInParentTask( m, log ) ;
        addLog( log ) ;
        return log ;
    }


}
