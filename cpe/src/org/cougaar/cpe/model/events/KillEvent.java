package org.cougaar.cpe.model.events;

import org.cougaar.cpe.model.events.CPEEvent;

/**
 * User: wpeng
 * Date: May 24, 2004
 * Time: 4:16:29 PM
 */
public class KillEvent extends CPEEvent {
    private String targetId;
    private String unitId;
    private float targetX, targetY ;

    public KillEvent(long time, String unit, String targetId, float targetX, float targetY )
    {
        super(time);
        this.targetId = targetId ;
        this.unitId = unit ;
        this.targetX = targetX ;
        this.targetY = targetY ;
    }

    public float getTargetX()
    {
        return targetX;
    }

    public float getTargetY()
    {
        return targetY;
    }

    public String getTargetId()
    {
        return targetId;
    }

    public String getUnitId()
    {
        return unitId;
    }
}
