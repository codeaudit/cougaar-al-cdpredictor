package org.cougaar.cpe.model.events;

import org.cougaar.cpe.model.events.CPEEvent;

/**
 * User: wpeng
 * Date: May 24, 2004
 * Time: 4:15:51 PM
 */
public class PenaltyEvent extends CPEEvent {
    public PenaltyEvent( long time, String targetId, float xTarget, float yTarget)
    {
        super( time ) ;
        this.targetId = targetId;
        this.xTarget = xTarget;
        this.yTarget = yTarget;
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
    float xTarget, yTarget ;
}
