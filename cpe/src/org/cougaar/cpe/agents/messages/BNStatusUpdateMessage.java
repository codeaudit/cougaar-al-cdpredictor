package org.cougaar.cpe.agents.messages;

import org.cougaar.tools.techspecs.events.MessageEvent;
import org.cougaar.cpe.model.WorldStateModel;

public class BNStatusUpdateMessage  extends MessageEvent {

    public BNStatusUpdateMessage(String bnUnitId, WorldStateModel wsm) {
        this.bnUnitId = bnUnitId;
        this.wsm = wsm;
    }

    public WorldStateModel getWorldStateModel() {
        return wsm;
    }

    public String getBnUnitId() {
        return bnUnitId;
    }

    protected WorldStateModel wsm ;

    protected String bnUnitId ;
}

