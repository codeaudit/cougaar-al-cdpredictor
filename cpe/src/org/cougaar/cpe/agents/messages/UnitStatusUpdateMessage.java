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
