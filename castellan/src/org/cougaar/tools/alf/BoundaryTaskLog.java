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

import org.cougaar.tools.castellan.analysis.TaskLog;
import org.cougaar.tools.castellan.pdu.* ;

import java.util.*;

/**
 * Represents tasks at the Cluster boundary.
 */
public class BoundaryTaskLog extends TaskLog implements BoundaryLog {
    public static class NullIterator implements Iterator {
        public boolean hasNext() {
            return false;
        }

        public Object next() {
            return null;
        }

        public void remove() {
        }
    }

    public BoundaryTaskLog( UIDPDU uid ) {
        super( uid ) ;
    }
    public BoundaryTaskLog(UIDPDU uid, String cluster, String taskVerb,
                           long createdTime, long createdExecutionTime ) {
        super(uid, cluster, taskVerb, createdTime, createdExecutionTime);
    }

    public void outputParamString(StringBuffer buf) {
        super.outputParamString(buf);
        buf.append( ",type=" ).append( BoundaryConstants.toParamString( getBoundaryType()) ) ;
    }

    public void setBoundaryType( int type ) {
        this.type = type ;
    }

    public int getBoundaryType() {
        return type ;
    }

    public void clear() {
        if ( incomingAncestors != null ) {
            incomingAncestors.clear() ;
        }
        if ( outgoingDescendents != null ) {
            outgoingDescendents.clear() ;
        }
        if ( unknownOrIncomplete != null ) {
            unknownOrIncomplete.clear() ;
        }
    }

    /**
     * @return Incoming ancestors for this outgoing task. Only valid for
     * outgoing tasks.
     */
    public Iterator getIncomingAncestors() {
        if ( incomingAncestors == null ) {
            return nullIter ;
        }
        return incomingAncestors.iterator();
    }

    public void addIncomingAncestor( TaskLog tl ) {
        if ( incomingAncestors == null ) {
            incomingAncestors = new ArrayList() ;
        }
        if ( !isIncomingAncestor(tl) ) {
            incomingAncestors.add( tl ) ;
        }
    }

    public boolean isIncomingAncestor( TaskLog tl ) {
        if ( incomingAncestors == null ) {
            return false ;
        }
        return incomingAncestors.indexOf(tl) != -1 ;
    }


    public Iterator getOutgoingDescendents() {
        if ( outgoingDescendents == null ) {
            return nullIter ;
        }
        return outgoingDescendents.iterator() ;
    }

    public boolean isOutgoingDescendent( TaskLog tl ) {
        if ( outgoingDescendents == null ) {
            return false ;
        }
        return outgoingDescendents.indexOf( tl ) != -1 ;
    }

    public void addOutgoingDescendent( TaskLog tl ) {
        if ( outgoingDescendents == null ) {
            outgoingDescendents = new ArrayList() ;
        }
        if ( !isOutgoingDescendent(tl) ) {
            outgoingDescendents.add( tl ) ;
        }
    }

    public Iterator getUnknownOrIncompleteDescendents() {
        if ( unknownOrIncomplete == null ) {
            return nullIter ;
        }
        return unknownOrIncomplete.iterator() ;
    }

    public boolean isUnknownOrIncompleteDescendent( TaskLog tl ) {
        if ( unknownOrIncomplete == null ) {
            return false;
        }
        return unknownOrIncomplete.indexOf( tl ) == -1 ;
    }

    public void addUnknownOrIncomplete( TaskLog tl ) {
        if ( unknownOrIncomplete == null ) {
            unknownOrIncomplete = new ArrayList() ;
        }
        if ( !isUnknownOrIncompleteDescendent( tl) ) {
            unknownOrIncomplete.add( tl ) ;
        }
    }

    public static final NullIterator nullIter = new NullIterator() ;
    protected int type ;
    protected ArrayList incomingAncestors ;
    protected ArrayList outgoingDescendents ;
    protected ArrayList unknownOrIncomplete ;
}
