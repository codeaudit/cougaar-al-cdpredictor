/*
* $Header: /opt/rep/cougaar.cvs/al-cdpredictor/castellan/src/org/cougaar/tools/castellan/pdu/Attic/TransactionPDU.java,v 1.1 2002-05-22 21:14:38 cvspsu Exp $
*
* $Copyright$
*
* This file contains proprietary information of Intelligent Automation, Inc.
* You shall use it only in accordance with the terms of the Agreement you
* entered into with Intelligent Automation, Inc.
*/

/*
* $Log: TransactionPDU.java,v $
* Revision 1.1  2002-05-22 21:14:38  cvspsu
* *** empty log message ***
*
*
*/
package org.cougaar.tools.castellan.pdu;

public class TransactionPDU extends PDU
{
    public TransactionPDU( long time )
    {
        this.time = time;
    }

    protected long time ;
}
