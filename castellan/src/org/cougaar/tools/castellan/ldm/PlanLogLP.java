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
package org.cougaar.tools.castellan.ldm;

import org.cougaar.core.domain.LogicProvider;
import org.cougaar.core.domain.EnvelopeLogicProvider;
import org.cougaar.core.domain.MessageLogicProvider;
import org.cougaar.core.domain.LogPlanLogicProvider;
import org.cougaar.core.blackboard.EnvelopeTuple;
import org.cougaar.core.blackboard.LogPlanServesLogicProvider;
import org.cougaar.core.agent.ClusterServesLogicProvider;
import org.cougaar.planning.ldm.plan.Directive;
import org.cougaar.tools.castellan.plugin.LogMessageBuffer;

import java.util.Collection;
import java.util.ArrayList;

public class PlanLogLP extends LogPlanLogicProvider implements EnvelopeLogicProvider, MessageLogicProvider {

    public PlanLogLP(LogPlanServesLogicProvider provider, ClusterServesLogicProvider provider1) {
        super(provider, provider1);
    }

    public void init() {
    }

    public void execute(EnvelopeTuple tuple, Collection collection) {
        Object o = tuple.getObject() ;

        if ( o instanceof LogMessageBuffer && buffer == null ) {
            buffer = ( LogMessageBuffer ) o ;
        }

        // Any add or change to LogMessageBuffer will cause a flush operation.
        if ( o instanceof LogMessageBuffer && ( tuple.isAdd() || tuple.isChange() ) )
        {
            // Process incoming and outgoing.
            synchronized ( buffer ) {
                // Copy any incoming tasks into the incoming queue
                if ( incoming.size() > 0 ) {
                    for (int i=0;i<incoming.size();i++) {
                        buffer.addIncoming( ( LogMessage ) incoming.get(i) );
                    }
                }
                ArrayList outgoing = buffer.getOutgoing() ;
                for ( int i=0;i<outgoing.size();i++) {
                    logplan.sendDirective( ( LogMessage ) outgoing.get(i) );
                }
                buffer.clearOutgoing();
            }
        }
    }

    /**
     * These are incoming directives.
     */
    public void execute(Directive directive, Collection collection) {
        if ( directive instanceof LogMessage ) {

            if ( buffer != null ) {
                synchronized ( buffer ) {
                    buffer.addIncoming( ( LogMessage ) directive );
                }
            }
            else {
                incoming.add( directive ) ;
            }
        }
    }

    ArrayList incoming = new ArrayList( 10 ) ;
    LogMessageBuffer buffer ;
}
