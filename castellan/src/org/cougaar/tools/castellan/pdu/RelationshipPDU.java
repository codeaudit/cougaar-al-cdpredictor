/*
* $Header: /opt/rep/cougaar.cvs/al-cdpredictor/castellan/src/org/cougaar/tools/castellan/pdu/RelationshipPDU.java,v 1.1 2002-05-22 21:14:38 cvspsu Exp $
*
* $Copyright$
*
* This file contains proprietary information of Intelligent Automation, Inc.
* You shall use it only in accordance with the terms of the license you
* entered into with Intelligent Automation, Inc.
*/

/*
* $Log: RelationshipPDU.java,v $
* Revision 1.1  2002-05-22 21:14:38  cvspsu
* *** empty log message ***
*
*
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
