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

import org.cougaar.tools.castellan.pspace.search.*;
import org.cougaar.tools.castellan.analysis.*;
import org.cougaar.tools.castellan.pdu.UIDPDU;
import org.cougaar.tools.castellan.pdu.PropertyGroupPDU;
import org.cougaar.tools.castellan.pdu.ClusterPGPDU;
import org.cougaar.tools.castellan.pdu.UIDStringPDU;
import org.cougaar.planning.ldm.plan.Allocation;
import org.cougaar.core.agent.Cluster;

import java.util.ArrayList;

/**
 *  This strategy is used for finding a subgraph within the plan within a single
 *  agent consisting of the descendents of a single root node (task).
 */
public class IntraAgentSubgraphStrategy implements Strategy {

    /**
     * @param pld The PlanLogDatabase which will be traversed.
     * @param terminateEarly  Whether the search will terminate automatically when it encounters a
     *   node which has no PlanElementLog.
     */
    public IntraAgentSubgraphStrategy(PlanLogDatabase pld, boolean terminateEarly ) {
        this.pld = pld;
        terminateOnNoPlanElement = terminateEarly ;

    }

    public GraphNode makeNode() {
        return new PEGraphNode( null ) ;
    }

    /**
     * Expand a PEGraphNode (containing a TaskLog) into successors, each of which
     * also contain a TaskLog.
     *
     * <p> A PEGraphNode has no successors if it has either no plan element or its
     * plan element is a Allocation to a remote cluster.
     */
    public GraphNode[] expand(GraphNode n) {
        PEGraphNode pe = ( PEGraphNode ) n ;
        UniqueObjectLog log = pe.getLog() ;
        GraphNode[] result = null ;
        if ( log instanceof TaskLog )  {
            UIDStringPDU uid = ( UIDStringPDU ) log.getUID() ;
            PlanElementLog pel = pld.getPlanElementLogForTask( log.getUID() ) ;
            if ( pel == null ) {
                result = new GraphNode[0] ;
            }
            else if ( pel instanceof AllocationLog ) {
                AllocationLog al = ( AllocationLog ) pel ;
                AssetLog asl = ( AssetLog ) pld.getLog( al.getAssetUID() ) ;
                if ( asl == null ) {
                    System.out.println("=======\nWARN:: ASSET for " + al + " does not exist." );
                    result = new GraphNode[0] ;
                }
                else {
                    String targetCluster = asl.getClusterProperty();

                    // This isn't a cluster, or this task is allocated to a remote cluster.
                    // Return true immediately.
                    if ( targetCluster == null || !targetCluster.equals( log.getCluster() ) ) {
                        result = new GraphNode[0] ;
                    }
                    else {
                        TaskLog atl = ( TaskLog ) pld.getLog( al.getAllocTaskUID() );
                        if ( atl != null ) {
                            result = new PEGraphNode[] { new PEGraphNode( atl ) } ;
                        }
                        else
                            result = new GraphNode[0] ;
                    }
                }
            }
            else if ( pel instanceof ExpansionLog ) {
                ExpansionLog el = ( ExpansionLog ) pel ;
                UIDPDU[] uids = el.getChildren() ;
                if ( uids == null ) {
                    result = new GraphNode[0] ;
                }
                else {
                    ArrayList children = new ArrayList( uids.length ) ;
                    for (int i=0;i<uids.length;i++) {
                        UniqueObjectLog ul = pld.getLog( uids[i] ) ;
                        if ( ul != null ) {
                            children.add( new PEGraphNode( ul ) ) ;
                        }
                    }
                    result = new GraphNode[ children.size() ] ;
                    for (int i=0;i<children.size();i++) {
                        result[i] = ( GraphNode ) children.get(i) ;
                    }
                }
            }
            else if ( pel instanceof AggregationLog ) {
                AggregationLog al = ( AggregationLog ) pel ;
                MPTaskLog mpt = ( MPTaskLog ) pld.getLog( al.getCombinedTask() );
                if ( mpt == null ) {
                    result = new GraphNode[0] ;
                }
                else {
                    result = new PEGraphNode[] { new PEGraphNode( mpt ) } ;
                }
            }
            else {
                result = new PEGraphNode[0] ;
            }
        }
        n.setSuccessors( result );
        return result ;
    }

    public void updateParent(GraphNode n1, GraphNode n2) {
    }

    public int getNumDescendants(GraphNode n) {
        throw new UnsupportedOperationException() ;
    }

    public void initNode(GraphNode n) {
    }

    public GraphNode expand(GraphNode n, int i) {
        throw new UnsupportedOperationException() ;
    }

    /**
     * If n1 and n2 represent the same plan element or task.
     */
    public boolean isEqual(GraphNode n1, GraphNode n2) {
        return n1.isIdentical( n2 ) ;
    }

    public int compare(Object n1, Object n2) {
        return 0;
    }

    /**
     * A goal node is defined as any TaskLog _without_ an associated
     * PlanElementLog.
     */
    public boolean isGoalNode(GraphNode n) {
        if ( terminateOnNoPlanElement ) {
            PEGraphNode peg = ( PEGraphNode ) n ;
            TaskLog tl = ( TaskLog ) peg.getLog() ;
            PlanElementLog pel = pld.getPlanElementLogForTask( tl.getUID() ) ;
            if ( pel == null ) {
                return true ;
            }
            return false ;
        }
        else {
            return false ;
        }
    }

    boolean terminateOnNoPlanElement = false ;
    PlanLogDatabase pld ;
}
