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
package org.cougaar.tools.castellan.pdu;

import java.io.ObjectInputStream;
import java.util.ArrayList;

/**
 * Expressions relationships between organizations.
 */
public class RelationshipPGPDU extends PropertyGroupPDU
{

    public RelationshipPGPDU(RelationshipPDU[] relationships)
    {
        this.relationships = relationships;
    }

    public void outputParamString( StringBuffer buf ) {
        buf.append( "RelationshipPG " ) ;
        buf.append( "[" ) ;
        for (int i=0;i<relationships.length;i++) {
           relationships[i].outputParamString( buf ); ;
        }
        buf.append( "]" ) ;
    }

    public String toString()
    {
        StringBuffer buf = new StringBuffer() ;
        outputParamString( buf );
        return buf.toString() ;
    }

    protected RelationshipPDU[] relationships ;
}
