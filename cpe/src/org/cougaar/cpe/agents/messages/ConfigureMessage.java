package org.cougaar.cpe.agents.messages;

import org.cougaar.tools.techspecs.events.MessageEvent;
import org.cougaar.cpe.model.WorldStateModel;

public class ConfigureMessage extends MessageEvent {

    public ConfigureMessage(WorldStateModel wsm) {
        this.wsm = wsm;
        setPriority( MessageEvent.PRIORITY_HIGH );
    }

    public WorldStateModel getWorldStateModel() {
        return wsm;
    }

    WorldStateModel wsm ;
}
