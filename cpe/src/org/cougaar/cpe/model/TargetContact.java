package org.cougaar.cpe.model;

/**
 * Target contacts maintain some error values.  When a TargetContact is received by a unit,
 * the xError and yError parameters refer to the maximum error values.  (When maintained within the world state,
 * it refers to the real error.)
 */
public class TargetContact extends TargetEntity
{
    public TargetContact(String uid, long timestamp, double x, double y, double dx, double dy, float xError, float yError, double strength )
    {
        super(uid, x, y, dx, dy,strength);
        this.timeStamp = timestamp ;
        this.xError = xError ;
        this.yError = yError ;
    }

    public Object clone()
    {
        TargetContact te = new TargetContact( worldId, timeStamp, x, y, dx, dy, xError, yError, strength ) ;
        te.isSuppressed = isSuppressed ;
        te.suppressionTime = suppressionTime ;
        te.isActive = isActive ;
        return te ;
    }

    public long getTimeStamp()
    {
        return timeStamp;
    }

    public float getXError()
    {
        return xError;
    }

    public float getYError()
    {
        return yError;
    }

    public void setError( float xError, float yError ) {
        this.xError = xError ;
        this.yError = yError ;
    }

    /**
     * +/- error.
     */
    protected float xError, yError ;

    /**
     * This is the real timestamp of the contact.
     */
    protected long timeStamp ;

}
