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

/**
 * Represents relationship between two organizations.
 */
 public class RelationshipPDU implements java.io.Serializable {
    public RelationshipPDU(String organizationA, String organizationB, String roleA, String roleB)
    {
        this.organizationA = organizationA;
        this.organizationB = organizationB;
        this.roleA = roleA;
        this.roleB = roleB;
    }

    public String getOrganizationA()
    {
        return organizationA;
    }

    public String getOrganizationB()
    {
        return organizationB;
    }

    public String getRoleA()
    {
        return roleA;
    }

    public String getRoleB()
    {
        return roleB;
    }

    public void outputParamString( StringBuffer buf ) {
        buf.append( "a=" ).append( organizationA ).append( ",b=" ).append( organizationB ) ;
        buf.append( ",roleA=" ).append( roleA ).append( ",roleB=" ).append( roleB ) ;
    }

    public String toString()
    {
        StringBuffer buf = new StringBuffer() ;
        outputParamString( buf );
        return buf.toString() ;
    }

    protected String organizationA ;
    protected String organizationB ;
    protected String roleA ;
    protected String roleB ;
 }
