/*
 * <copyright>
 *  Copyright 2003-2004 Intelligent Automation, Inc.
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

package org.cougaar.cpe.agents.messages;

import org.cougaar.cpe.model.UnitEntity;
import org.cougaar.cpe.model.WorldState;
import org.cougaar.cpe.model.WorldStateModel;

import java.util.ArrayList;

/**
 *  Sent by the WorldState to the UnitAgent and the UnitAgent to the
 *  C2Agent.
 */
public class UnitStatusUpdateMessage extends org.cougaar.tools.techspecs.events.MessageEvent {
    public UnitStatusUpdateMessage( UnitEntity entity, WorldStateModel ws ) {
        super();
        setValue( entity );
        this.perceivedWorldState = ws ;
    }

    public UnitStatusUpdateMessage( UnitEntity entity, WorldStateModel perceivedWorldState, ArrayList cpeEvents) {
        super() ;
        setValue( entity );
        this.perceivedWorldState = perceivedWorldState;
        this.cpeEvents = cpeEvents;
    }

    /**
     * This is the originating entity's WorldState.
     * @return
     */
    public org.cougaar.cpe.model.UnitEntity getEntity() {
        return ( org.cougaar.cpe.model.UnitEntity ) getValue() ;
    }

    /**
     * The perceived worldState, exclusive of the UnitEntity.
     * @return
     */
    public org.cougaar.cpe.model.WorldStateModel getWorldState() {
        return perceivedWorldState;
    }

    public void setPerceivedWorldState(org.cougaar.cpe.model.WorldStateModel perceivedWorldState) {
        this.perceivedWorldState = perceivedWorldState;
    }

    protected org.cougaar.cpe.model.WorldStateModel perceivedWorldState ;

    public ArrayList getEvents() {
        return cpeEvents;
    }

    protected ArrayList cpeEvents ;

    public static final int MESSAGE_BYTE_LENGTH = 50000 ;
}
