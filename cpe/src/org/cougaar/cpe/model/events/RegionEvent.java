package org.cougaar.cpe.model.events;

import org.cougaar.cpe.model.events.CPEEvent;
import org.cougaar.cpe.model.Region;

/**
 * Every target that cross a specific boundary.
 */
public class RegionEvent extends CPEEvent {
    private String targetId;

    public static final int EVENT_REGION_ENTRY = 0 ;

    public static final int EVENT_REGION_EXIT = 1 ;

    public RegionEvent(long time, int type, String targetId, float xTarget, float yTarget, float oldX, float oldY, Region r )
    {
        super( time ) ;
        this.type = type ;
        this.newX = xTarget;
        this.newY = yTarget;
        this.oldX = oldX ;
        this.oldY = oldY ;
        this.targetId = targetId ;
        this.region = r ;
    }

    protected void outputParamString(StringBuffer buf)
    {
        buf.append( ",target=" ).append( targetId ) ;
        buf.append( ",region=" ).append( region.getRegionName() ) ;
    }

    public String getTargetId()
    {
        return targetId;
    }

    public int getType()
    {
        return type;
    }

    public Region getRegion()
    {
        return region;
    }

    public float getNewX()
    {
        return newX;
    }

    public float getNewY()
    {
        return newY;
    }

    public float getOldX()
    {
        return oldX;
    }

    public float getOldY()
    {
        return oldY;
    }

    private Region region;
    private float newX, newY ;
    private float oldX, oldY ;
    private int type;

}
