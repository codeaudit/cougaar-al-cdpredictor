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
  *  7/25/01 Initial version  by Penn State/IAI
  */

package org.cougaar.tools.castellan.pdu;

import java.io.Externalizable;
import java.io.ObjectOutput;
import java.io.IOException;
import java.io.ObjectInput;

/**
 * PDU wrapping task related info.
 *
 * @author  wpeng
 * @version
 */
public class TaskPDU extends UniqueObjectPDU {

    public TaskPDU(SymbolPDU taskVerb, UIDPDU parentTaskUID, UIDPDU uid, UIDPDU directObject,
        int action, long executionTime, long time )
    {
        super( uid, TYPE_TASK, action, executionTime, time );
        this.directObject = directObject ;
        this.taskVerbSym = taskVerb ;
        this.parentTaskUID = parentTaskUID ;
    }


    public SymbolPDU getTaskVerb() { return taskVerbSym ; }

    public UIDPDU getParentTask() { return parentTaskUID ; }

    public void outputParamString( StringBuffer buf ) {
        super.outputParamString( buf ) ;
        buf.append( ",verb=");
        format( buf, taskVerbSym ) ;
        buf.append( ",parent=");
        format( buf, parentTaskUID ) ;
    }

    public void writeExternal(ObjectOutput out) throws IOException
    {
        out.writeObject( taskVerbSym );
        out.writeObject( parentTaskUID );
        out.writeObject( directObject );
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
    {
        taskVerbSym = ( SymbolPDU ) in.readObject() ;
        parentTaskUID = ( UIDPDU ) in.readObject() ;
        directObject = ( UIDPDU ) in.readObject() ;
    }

    protected SymbolPDU taskVerbSym ;
    protected UIDPDU parentTaskUID ;
    protected UIDPDU directObject ;

    static final long serialVersionUID = -6915490535562092604L;
}