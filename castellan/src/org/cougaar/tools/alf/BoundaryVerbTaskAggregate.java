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
  * CHANGE RECORD
  */
package org.cougaar.tools.alf;

import org.cougaar.tools.castellan.analysis.TaskLog;
import org.cougaar.tools.castellan.analysis.AggregateVerbTaskLog;
import org.cougaar.tools.castellan.pdu.UniqueObjectPDU;
import org.cougaar.util.UnaryPredicate;

/**
 * An aggregate related to the incoming and outgoing tasks.
 */
public class BoundaryVerbTaskAggregate extends AggregateVerbTaskLog {

    public BoundaryVerbTaskAggregate(int type, String agent, String verb) {
        super( verb, agent ) ;
        this.type = type;
    }

    public BoundaryVerbTaskAggregate(int type, String agent, String verb, String source, String target ) {
        this( type, agent, verb ) ;
        this.sourceAgent = source ;
        this.targetAgent = target ;
    }

    public int getBoundaryType() {
        return type;
    }

    public void outputParamString(StringBuffer buf) {
        super.outputParamString(buf);
        buf.append( ",sourceAgent=" ).append( sourceAgent ).append( ",targetAgent=" ).append( targetAgent ) ;
        buf.append( ",type=" ).append( BoundaryConstants.toParamString( type ) ) ;
    }

    public int hashCode() {
        int retval = type + agent.hashCode() + verb.hashCode() ;
        if ( sourceAgent != null ) {
            retval += sourceAgent.hashCode() ;
        }
        if ( targetAgent != null ) {
            retval += targetAgent.hashCode() ;
        }
        return retval ;
    }

    public boolean match(UniqueObjectPDU pdu) {
        throw new UnsupportedOperationException( "Matching not enabled for " + this.getClass().getName() ) ;
    }

    protected int type ;
    protected String agent ;
    protected String verb ;
    /**
     * This is the source of the tasks for incoming agents.  If null, assume all
     * incoming tasks with the same verb are grouped together.
     */
    protected String sourceAgent ;
    /**
     * This is the source of the tasks for outgoing agents, If null, all outgoing
     * tasks with the same verb are grouped together.
     */
    protected String targetAgent ;
}