package org.cougaar.cpe.model.events;


/**
 * User: wpeng
 * Date: May 21, 2004
 * Time: 2:40:00 PM
 */
public class FuelShortfallEvent extends org.cougaar.cpe.model.events.CPEEvent
{
    private String entityId;
    private float shortfallAmount;

    public FuelShortfallEvent(long time, String entityId, float amount )
    {
        super(time);
        this.entityId = entityId ;
        this.shortfallAmount = amount ;
    }

    public String getEntityId()
    {
        return entityId;
    }

    public float getShortfallAmount()
    {
        return shortfallAmount;
    }
}
