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

import org.cougaar.core.mts.MessageAddress;
import org.cougaar.tools.techspecs.qos.MeasurementChain;
import org.cougaar.tools.techspecs.ActionEventSpec;

import java.io.Serializable;

/**
 *  An input message to an action.  It represents a message sent by
 * an agent.
 */
public abstract class MessageEvent extends ActionEvent {

    public MessageEvent() {
        super( ActionEventSpec.EVENT_INPUT_MESSAGE ) ;
    }

    public void paramString( StringBuffer buf ) {
        String name = getClass().getName() ;
        int indx = name.lastIndexOf('.') ;
        buf.append( name.substring( indx + 1 ) ) ;
        buf.append( ",source=").append( getSource() ) ;
    }

    public MessageAddress getSource() {
        return source;
    }

    public void setSource(MessageAddress source) {
        this.source = source;
    }

    protected void setValue(Serializable value) {
        this.value = value;
    }

    public Serializable getValue() {
        return value;
    }

    public int getNumBytes() {
        return bytes;
    }

    public void setNumBytes(int bytes) {
        this.bytes = bytes;
    }

    public MeasurementChain getMeasurements() {
        return measurements;
    }

    public int getSeqId()
    {
        return seqId;
    }

    public void setMeasurements(MeasurementChain measurements) {
        // Don't bother cloning measurements since they are all final, anyways.
        this.measurements = (MeasurementChain) measurements.clone();
    }

    protected MessageAddress source ;
    protected Serializable value ;
    protected int bytes ;
    protected int seqId ;

    public void setSeqId(int seqId)
    {
        this.seqId = seqId ;
    }
}
