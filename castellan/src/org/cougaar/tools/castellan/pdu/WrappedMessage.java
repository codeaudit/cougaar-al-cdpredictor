/*
* $Header: /opt/rep/cougaar.cvs/al-cdpredictor/castellan/src/org/cougaar/tools/castellan/pdu/Attic/WrappedMessage.java,v 1.1 2002-05-22 21:14:38 cvspsu Exp $
*
* $Copyright$
*
* This file contains proprietary information of Intelligent Automation, Inc.
* You shall use it only in accordance with the terms of the Agreement you
* entered into with Intelligent Automation, Inc.
*/

/*
* $Log: WrappedMessage.java,v $
* Revision 1.1  2002-05-22 21:14:38  cvspsu
* *** empty log message ***
*
*
*/
package org.cougaar.tools.castellan.pdu;

import org.cougaar.core.mts.*;

/**
 * Wraps one or more compressed and/or serialized PDU objects.
 */
public class WrappedMessage extends Message
{
    public WrappedMessage(MessageAddress aSource, MessageAddress aTarget,
                          boolean isCompressed,  byte[] array )
    {
        super(aSource, aTarget);
        this.isCompressed = isCompressed ;
        this.array = array ;
    }

    public boolean isCompressed()
    {
        return isCompressed;
    }

    public byte[] getArray() {
        return array ;
    }

    protected boolean isCompressed ;
    protected byte[] array ;
}
