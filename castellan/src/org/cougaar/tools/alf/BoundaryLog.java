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

import java.util.Iterator;
import java.util.ArrayList;

public interface BoundaryLog {

    public void setBoundaryType( int type ) ;

    public int getBoundaryType() ;

    public void clear() ;

    /**
     * @return Incoming ancestors for this outgoing task. Only valid for
     * outgoing tasks.
     */
    public Iterator getIncomingAncestors() ;

    public void addIncomingAncestor( TaskLog tl ) ;

    public boolean isIncomingAncestor( TaskLog tl ) ;

    public Iterator getOutgoingDescendents();

    public boolean isOutgoingDescendent( TaskLog tl ) ;

    public void addOutgoingDescendent( TaskLog tl ) ;

    public Iterator getUnknownOrIncompleteDescendents() ;

    public boolean isUnknownOrIncompleteDescendent( TaskLog tl ) ;

    public void addUnknownOrIncomplete( TaskLog tl );
}
