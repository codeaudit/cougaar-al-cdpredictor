package org.cougaar.cpe.model.events;


public class AmmoShortFallEvent extends org.cougaar.cpe.model.events.CPEEvent
{
    private String unitId;
    private String targetId;
    private int amount;

    public AmmoShortFallEvent(long time, String unitId, String targetId, int amount )
    {
        super(time);
        this.unitId = unitId ;
        this.targetId = targetId ;
        this.amount = amount ;
    }

    public String getTargetId()
    {
        return targetId;
    }

    public String getUnitId()
    {
        return unitId;
    }

    public int getAmount()
    {
        return amount;
    }
}
