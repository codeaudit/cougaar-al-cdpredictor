/*
* $Header: /opt/rep/cougaar.cvs/al-cdpredictor/castellan/src/org/cougaar/tools/castellan/ldm/FlushObject.java,v 1.1 2002-05-22 22:17:07 cvspsu Exp $
*
* $Copyright$
*
* This file contains proprietary information of Intelligent Automation, Inc.
* You shall use it only in accordance with the terms of the Agreement you
* entered into with Intelligent Automation, Inc.
*/

/*
* $Log: FlushObject.java,v $
* Revision 1.1  2002-05-22 22:17:07  cvspsu
* *** empty log message ***
*
*
*/
package org.cougaar.tools.castellan.ldm;

import org.cougaar.tools.castellan.pdu.*;

import java.io.*;

public class FlushObject implements Serializable {
    public FlushObject( long time )
    {
        this.time = time;
    }

    public long getTime()
    {
        return time;
    }

    public String toString()
    {
        return "Flush(" + EventPDU.formatTime(time) + ")" ;
    }

    private long time ;
}
