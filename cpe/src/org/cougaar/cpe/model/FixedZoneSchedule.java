package org.cougaar.cpe.model;

/**
 * User: wpeng
 * Date: May 22, 2003
 * Time: 2:29:38 PM
 */
public class FixedZoneSchedule extends ZoneSchedule {

    public FixedZoneSchedule(Zone zone) {
        this.zone = zone;
    }

    public long getEndTime()
    {
        return Long.MAX_VALUE ;
    }

    public long getStartTime()
    {
        return Long.MIN_VALUE ;
    }

    public String toString() {
        return "ZoneSchedule [" + "zone=" + zone + "]";
    }

    public Zone getZoneForTime(long time) {
        return zone ;
    }

    public Object clone()
    {
        return new FixedZoneSchedule( ( Zone ) zone.clone() ) ;
    }

    Zone zone ;
}
