/*
* $Header: /opt/rep/cougaar.cvs/al-cdpredictor/castellan/src/org/cougaar/tools/castellan/ldm/LogMessage.java,v 1.1 2002-05-22 22:17:07 cvspsu Exp $
*
* $Copyright$
*
* This file contains proprietary information of Intelligent Automation, Inc.
* You shall use it only in accordance with the terms of the Agreement you
* entered into with Intelligent Automation, Inc.
*/

/*
* $Log: LogMessage.java,v $
* Revision 1.1  2002-05-22 22:17:07  cvspsu
* *** empty log message ***
*
*
*/
package org.cougaar.tools.castellan.ldm;

import org.cougaar.core.mts.*;
import org.cougaar.core.util.*;
import org.cougaar.core.blackboard.*;
import org.cougaar.planning.ldm.plan.*;

import java.io.*;

/**
 *  All logging messages sent through the Blackboard are derived from this
 *  abstract class.
 */

public abstract class LogMessage extends DirectiveImpl
{
    protected LogMessage()
    {
    }

    public void setSourceAgent( String s  ) {
        this.source = s ;
    }

    public String getSourceAgent()
    {
        return source;
    }

    /**
     * Wether the LogMessageSenderLP has sent this (local) message yet.
     */
    public boolean isSent()
    {
        return isSent;
    }

    public void setSent(boolean sent)
    {
        isSent = sent;
    }

    /**
     * @return whether or not this LogMessage originates in this cluster.
     */
    public boolean isLocal()
    {
        return isLocal;
    }

    public void setLocal(boolean local)
    {
        isLocal = local;
    }

    /**
     * @return Whether the ReceiveLogMessageLP has processed this yet.
     */
    public boolean isRead()
    {
        return isRead;
    }

    public void setRead(boolean read)
    {
        isRead = read;
    }

    private String source ;
    private transient boolean isLocal = true ;
    private transient boolean isRead = false ;
    private transient boolean isSent = false ;
}
