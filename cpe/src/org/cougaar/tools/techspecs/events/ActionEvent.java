/*
 * <copyright>
 *  Copyright 2004 Intelligent Automation, Inc.
 *  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the Cougaar Open Source License as published by
 *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
 *
 *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 *  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *  PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */

package org.cougaar.tools.techspecs.events;

import org.cougaar.tools.techspecs.qos.MeasurementChain;
import org.cougaar.tools.techspecs.ActionEventSpec;

import java.io.Serializable;

/**
 *  This encapsulates a message either an input message or a timer message.
 */
public class ActionEvent implements Serializable
{

    public static final int PRIORITY_NORMAL = 0 ;
    public static final int PRIORITY_HIGH = 1 ;

    public ActionEvent(int type)
    {
        this.type = type;
    }

    public int getType()
    {
        return type;
    }

    /**
     * This is filled in automatically by the callback action (based on generated code.)
     *
     * @param spec
     */
    public void setSpec(ActionEventSpec spec)
    {
        this.spec = spec;
    }

    public ActionEventSpec getSpec()
    {
        return spec;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer() ;
        buf.append( "[" ) ;
        paramString( buf );
        buf.append( "]") ;
        return buf.toString() ;
    }

    public void paramString( StringBuffer buf ) {
    }

    public void setPriority( int priority ) {
        this.priority = priority ;
    }

    public int getPriority()
    {
        return priority;
    }

    protected MeasurementChain measurements = new MeasurementChain();

    /**
     * The ActionEventSpec associated with this message.
     */
    transient ActionEventSpec spec ;
    int type ;
    int priority = PRIORITY_NORMAL ;

    protected long sentTimeStamp ;
    protected long receivedTimeStamp ;
    protected long startProcessedTimestamp ;
    protected long endProcessedTimestamp ;
}
