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
package org.cougaar.tools.alf.sensor;

import org.cougaar.core.relay.Relay;
import org.cougaar.core.util.UID;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.adaptivity.SensorCondition;

import java.util.Collections;
import java.util.Set;

/**
 * A generic way to relay a sensor condition.
 */

public class SensorConditionRelay implements Relay.Source, Relay.Target {
    public SensorConditionRelay(UID uid, MessageAddress target, MessageAddress source, SensorCondition condition, Object response ) {
        this.uid = uid;
        this.source= source ;
        this.value = condition ;
        this.response = response ;
        targets = ((target != null) ?
         Collections.singleton(target) :
         Collections.EMPTY_SET);
    }

    public String toString() {
        return "<SensorConditionRelay: Uid= " + uid + ", value=" + getCondition() + ",source=" + getSource() + ",target=" + targets + ">" ;
    }

    public Set getTargets() {
        return targets ;
    }

    public UID getUID() {
        return uid ;
    }

    public MessageAddress getSource() {
        return source ;
    }

    public void setValue( SensorCondition o ) {
        value = o ;
    }

    public SensorCondition getCondition() {
        return value ;
    }

    public Object getContent() {
        return value;
    }

    public void setUID(UID uid) {
        throw new RuntimeException( "Tried to set UID" ) ;
    }

    public Object getResponse() {
        return null;
    }

    public Relay.TargetFactory getTargetFactory() {
        return SimpleRelayFactory.INSTANCE ;
    }

    public int updateContent(Object o, Relay.Token token) {
        if ( o instanceof  SensorCondition ) {
            value = ( SensorCondition ) o ;
        }
        return Relay.CONTENT_CHANGE ;
    }

    public int updateResponse(MessageAddress address, Object o) {
        return 0;
    }

    private static final class SimpleRelayFactory
    implements TargetFactory, java.io.Serializable {

      public static final SimpleRelayFactory INSTANCE =
        new SimpleRelayFactory();

      private SimpleRelayFactory() {}

      public Relay.Target create(
          UID uid,
          MessageAddress source,
          Object content,
          Token token) {
        return new SensorConditionRelay( uid, null, source, ( SensorCondition ) content, null ) ;
      }

      private Object readResolve() {
        return INSTANCE;
      }
    };


    protected UID uid ;
    protected Set targets ;
    protected MessageAddress source ;
    protected SensorCondition value ;
    protected Object response ;

}
