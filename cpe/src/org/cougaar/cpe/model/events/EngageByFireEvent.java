package org.cougaar.cpe.model.events;
import org.cougaar.cpe.model.EngageByFireResult;

public class EngageByFireEvent extends CPEEvent {
    public EngageByFireEvent(String unitId, EngageByFireResult er, long time, float xTarget, float yTarget, float xUnit, float yUnit)
    {
        super( time ) ;
        this.unitId = unitId;
        this.er = er;
        this.xTarget = xTarget;
        this.yTarget = yTarget;
        this.xUnit = xUnit;
        this.yUnit = yUnit;
    }

    protected void outputParamString(StringBuffer buf)
    {
        buf.append( ",unit=" + unitId ) ;
        buf.append( ",target=" + er.getTargetId() ) ;
        buf.append( ",value=" + er.getAttritValue() ) ;
    }

    public float getxTarget()
    {
        return xTarget;
    }

    public float getyTarget()
    {
        return yTarget;
    }

    public float getxUnit()
    {
        return xUnit;
    }

    public float getyUnit()
    {
        return yUnit;
    }

    public EngageByFireResult getEr() {
        return er;
    }

    public String getUnitId() {
        return unitId;
    }

    String unitId ;
    EngageByFireResult er ;
    float xUnit, yUnit ;
    float xTarget, yTarget ;
}
