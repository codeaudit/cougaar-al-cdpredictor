package org.cougaar.cpe.model;

public class ZoneExecutionResult extends TaskResult {
    private double attritionValue ;

    protected Zone start, end ;

    public ZoneExecutionResult( long startTime, Zone start )
    {
        this.startTime = startTime ;
        this.start = start ;
    }

    public Zone getEndZone() {
        return end;
    }

    public void setEndZone(Zone end) {
        this.end = end;
    }

    public Object clone()
    {
        Zone newStartZone = null ;
        if ( start != null ) {
            newStartZone = (Zone) start.clone() ;
        }
        ZoneExecutionResult zer = new ZoneExecutionResult( startTime, newStartZone ) ;
        if ( end != null ) {
            zer.end = ( Zone ) end.clone() ;
        }
        zer.completionTime = completionTime ;
        zer.fuelConsumption = fuelConsumption ;
        zer.ammoConsumption = ammoConsumption ;
        zer.attritionValue = attritionValue ;
        return zer ;
    }

    public int getAmmoConsumption()
    {
        return ammoConsumption;
    }

    public long getCompletionTime()
    {
        return completionTime;
    }

    public float getFuelConsumption()
    {
        return fuelConsumption;
    }

    public void setAmmoConsumption(int ammoConsumption)
    {
        this.ammoConsumption = ammoConsumption;
    }

    protected long startTime, completionTime ;
    protected float fuelConsumption ;
    protected int ammoConsumption ;

    public void setFuelConsumption( float fuelConsumption )
    {
        this.fuelConsumption = fuelConsumption ;
    }

    public void setAttritionValue(double attritValue)
    {
        this.attritionValue = attritValue ;
    }

    public double getAttritionValue()
    {
        return attritionValue;
    }

    public Zone getStartZone() {
        return start;
    }
}
