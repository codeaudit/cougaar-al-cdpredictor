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
