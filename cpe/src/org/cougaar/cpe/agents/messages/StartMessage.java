package org.cougaar.cpe.agents.messages;

import org.cougaar.tools.techspecs.events.MessageEvent;

/**
 * Simple message that starts time advancing.
 */
public class StartMessage extends MessageEvent {
    public StartMessage(long baseTime) {
        this.baseTime = baseTime;
    }

    public long getBaseTime() {
        return baseTime;
    }

    long baseTime ;
}
