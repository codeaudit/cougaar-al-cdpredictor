package org.cougaar.cpe.model.events;

import org.cougaar.cpe.model.events.CPEEvent;

/**
 * User: wpeng
 * Date: May 24, 2004
 * Time: 1:54:27 PM
 */
public class ViolationEvent extends CPEEvent {
    public ViolationEvent(long time, String targetId, float xTarget, float yTarget )
    {
        super(time) ;
        this.targetId = targetId ;
        this.xTarget = xTarget ;
        this.yTarget = yTarget ;
    }

    public String getTargetId()
    {
        return targetId;
    }

    public float getxTarget()
    {
        return xTarget;
    }

    public float getyTarget()
    {
        return yTarget;
    }

    String targetId ;
    float xTarget ;
    float yTarget ;
}
