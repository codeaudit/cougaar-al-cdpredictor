package org.cougaar.cpe.model;

/**
 * This describes the closed interval [xlower,xUpper].
 */
public class Interval extends Zone
{
    private float xLower, xUpper;

    public Interval(float xLower, float xUpper) {
        this.xLower = xLower;
        this.xUpper = xUpper;
        if ( xUpper < xLower ) {
            throw new IllegalArgumentException( "xUpper " + xUpper + " must be > uLower." ) ;
        }
        //xCenter = ( xUpper - xLower ) / 2 ;
    }

    public boolean isInInterval( float value ) {
        return value >= xLower && value <= xUpper ;
    }

    public Object clone()
    {
        return new Interval( xLower, xUpper ) ;
    }

    public String toString() {
        return "[xl=" + xLower + ",xu=" + xUpper +"]" ;
    }

    public float getXLower() {
        return xLower;
    }

    public float getXUpper() {
        return xUpper;
    }

    public float getXCenter()
    {
        return ( xUpper - xLower ) / 2 + xLower ;
    }

}
