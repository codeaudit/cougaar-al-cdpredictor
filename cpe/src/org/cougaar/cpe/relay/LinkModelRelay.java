package org.cougaar.cpe.relay;

import org.cougaar.core.util.UID;
import org.cougaar.core.mts.MessageAddress;

/**
 * A link model relay models inter-agent links.  It simulates an agent-to-agent
 * communication link with a stochastic and/or deterministic bit rate and delay.
 *
 * <p> In addition, it serves as a QoS measurement point.  It attaches
 * timestamps to incoming and outbound messages, as well as keeping a QoS
 * history associated with this link.
 */
public class LinkModelRelay extends TargetBufferRelay {

    public LinkModelRelay(UID uid, MessageAddress source, Object c) {
        super(uid, source, c);
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public double getRate() {
        return rate;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    protected void addIncoming(Object[] ics) {
        super.addIncoming(ics);
    }

    protected boolean isActive = true ;

    /**
     * Bandwidth in bytes/sec.
     */
    protected double rate = 10000 ;

    /**
     * Delay in ms.
     */
    protected long delay = 0 ;
}
