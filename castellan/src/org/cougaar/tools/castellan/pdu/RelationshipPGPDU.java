/*
* $Header: /opt/rep/cougaar.cvs/al-cdpredictor/castellan/src/org/cougaar/tools/castellan/pdu/RelationshipPGPDU.java,v 1.1 2002-05-22 21:14:38 cvspsu Exp $
*
* $Copyright$
*
* This file contains proprietary information of Intelligent Automation, Inc.
* You shall use it only in accordance with the terms of the license you
* entered into with Intelligent Automation, Inc.
*/

/*
* $Log: RelationshipPGPDU.java,v $
* Revision 1.1  2002-05-22 21:14:38  cvspsu
* *** empty log message ***
*
*
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
