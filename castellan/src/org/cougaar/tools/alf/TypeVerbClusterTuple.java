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

/**
 * Defines a class of tasks in terms of their agent, their task verb and type (either
 * incoming or outgoing.)
 */
class TypeVerbClusterTuple {
    protected TypeVerbClusterTuple( int type, String verb, String cluster ) {
        this.type = type ;
        this.verb = verb ;
        this.cluster = cluster ;
        updateHashCode();
    }

    /**
     * @param targetAgent If this ia an outgoing task, where this task is going.
     * @param sourceAgent if this is an incoming task, where this task came from.
     */
    public TypeVerbClusterTuple(int type, String verb, String cluster, String targetAgent, String sourceAgent) {
        this( type, verb, cluster ) ;
        this.targetAgent = targetAgent;
        this.sourceAgent = sourceAgent ;
        updateHashCode();
    }

    protected void updateHashCode() {
        hashCode += type + verb.hashCode() + cluster.hashCode() ;
        if ( targetAgent != null ) {
            hashCode += targetAgent.hashCode() ;
        }
        if ( sourceAgent != null ) {
            hashCode += sourceAgent.hashCode() ;
        }
    }

    public int hashCode() { return hashCode ; }

    public boolean equals( Object o ) {
        if ( o instanceof TypeVerbClusterTuple ) {
            TypeVerbClusterTuple p = ( TypeVerbClusterTuple ) o ;
            return ( p.type == type ) && p.verb.equals( verb ) && p.cluster.equals( cluster )
                    && ( targetAgent == p.targetAgent );
        }
        return false ;
    }

    protected int hashCode ;
    protected int type ;
    protected String verb, cluster, targetAgent, sourceAgent ;
}
