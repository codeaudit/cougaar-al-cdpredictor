package org.cougaar.cpe.model;

/**
 * User: wpeng
 * Date: Mar 18, 2004
 * Time: 11:07:52 AM
 */
public class Interval extends Zone
{
    private float xLower, xUpper;
    private float yHeight = Float.MAX_VALUE ;

    public Interval(float xLower, float xUpper) {
        this.xLower = xLower;
        this.xUpper = xUpper;
        if ( xUpper < xLower ) {
            throw new IllegalArgumentException( "xUpper " + xUpper + " must be > uLower." ) ;
        }
        //xCenter = ( xUpper - xLower ) / 2 ;
    }

    public Interval(float xLower, float xUpper, float yHeight)
    {
        this.xLower = xLower;
        this.xUpper = xUpper;
        if ( xUpper < xLower ) {
            throw new IllegalArgumentException( "xUpper " + xUpper + " must be > uLower." ) ;
        }
        // xCenter = ( xUpper - xLower ) / 2 ;
        this.yHeight = yHeight ;
    }

    public Object clone()
    {
        return new Interval( xLower, xUpper, yHeight ) ;
    }

    public String toString() {
        return "[xl=" + xLower + ",xu=" + xUpper + ",yheight=" + yHeight + "]" ;
    }

    public float getXLower() {
        return xLower;
    }

    public float getXUpper() {
        return xUpper;
    }

    public float getYHeight() {
        return yHeight;
    }

    public float getXCenter()
    {
        return ( xUpper - xLower ) / 2 + xLower ;
    }

}
