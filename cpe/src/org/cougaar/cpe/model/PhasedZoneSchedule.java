package org.cougaar.cpe.model;

import org.cougaar.cpe.planning.zplan.ZoneWorld;

import java.util.ArrayList;

/**
 *
 */
public class PhasedZoneSchedule extends ZoneSchedule
{

    long startTime, maxTime ;
    int numTimeUnitsPerIndex ;
    ArrayList zoneByIndex ;
    ZoneExecutionResult[] zoneExecutionResult ;
    float lower ;
    float zoneSize ;
    int numZones ;

    public PhasedZoneSchedule( float fieldLower, int numZones, float zoneSize, long startTime, ArrayList zones, int timeUnitsPerIndex )
    {
        this.startTime = startTime ;
        this.lower = fieldLower ; this.numZones = numZones ;
        this.zoneSize = zoneSize ;

        // Deep clone the array list.
        zoneByIndex = new ArrayList( zones.size() ) ;
        zoneExecutionResult = new ZoneExecutionResult[ zones.size() ] ;
        for (int i = 0; i < zones.size(); i++) {
            Zone z = (Zone) zones.get(i);
            zoneByIndex.add( z ) ;
        }
        this.numTimeUnitsPerIndex = timeUnitsPerIndex ;
        this.maxTime = startTime + zones.size() * numTimeUnitsPerIndex ;
    }

    public PhasedZoneSchedule( ZoneWorld zw, long startTime, int numTimeUnitsPerIndex, Zone z )
    {
        this.startTime = startTime;
        this.zoneSize = zw.getZoneGridSize() ;
        this.numZones = zw.getNumZones() ;
        this.lower = (float) zw.getLowerX();

        this.numTimeUnitsPerIndex = numTimeUnitsPerIndex;
        if ( z == null ) {
            throw new IllegalArgumentException( "Null zone not allowed." ) ;
        }
        zoneByIndex = new ArrayList(1) ;
        zoneByIndex.add( z ) ;
        zoneExecutionResult = new ZoneExecutionResult[ 1 ] ;
        this.maxTime = startTime + zoneByIndex.size() * numTimeUnitsPerIndex ;

    }

    public float getLower( IndexedZone z ) {
        return z.getStartIndex() * zoneSize + lower ;
    }

    public float getUpper( IndexedZone z ) {
        if ( z.getEndIndex() >= numZones ) {
            throw new IllegalArgumentException( "Zone is out of bounds." ) ;
        }
        return z.getEndIndex() * zoneSize + lower ;
    }

    public float getWidth( IndexedZone z ) {
        return ( z.getEndIndex() - z.getStartIndex() + 1 ) * zoneSize ;
    }

    public Object clone()
    {
        PhasedZoneSchedule ps = new PhasedZoneSchedule( lower, numZones, zoneSize, startTime, zoneByIndex, numTimeUnitsPerIndex ) ;
        return ps ;
    }

    public long getStartTime()
    {
        return startTime;
    }

    public long getEndTime()
    {
        return maxTime;
    }

    public int getNumTimeUnitsPerIndex()
    {
        return numTimeUnitsPerIndex;
    }

    public int getNumIndices() {
        return zoneByIndex.size() ;
    }

    public String toString()
    {
        StringBuffer buf = new StringBuffer() ;
        buf.append( "[ZoneSchedule start=" ).append( ( startTime / 1000.0 ) ).append( ",end=" ).append( ( maxTime / 1000.0 ) ) ;
        return buf.toString() ;
    }

    public int getIndexForTime( long time ) {
        if ( time < startTime ) {
            return -1 ;
        }
        if ( time > maxTime ) {
            return -1 ;
        }
        return (int) ( ( time - startTime ) / ( numTimeUnitsPerIndex ) ) ;
    }

    public ZoneExecutionResult getResult( int index ) {
        return zoneExecutionResult[index] ;
    }

    public void setResult( int index, ZoneExecutionResult result ) {
        zoneExecutionResult[index] = result  ;
    }

    public Zone getZoneForTime( long time )
    {
        int index = getIndexForTime(time) ;
        if ( index < 0 ) {
            return null ;
        }
        return (Zone) zoneByIndex.get( index  ) ;
    }

    public Zone getZone( int index ) {
        return (Zone) zoneByIndex.get(index) ;
    }

    public void addZone( IndexedZone z ) {
        zoneByIndex.add( z ) ;

        // Now, update the max time.
    }

    public static final void main( String[] args ) {
        ArrayList zones = new ArrayList() ;
        zones.add( new Interval( 0, 1 ) ) ;

        PhasedZoneSchedule pss = new PhasedZoneSchedule( 0, 20, 2, 1000, zones, 5000 * 8 ) ;
    }

    public long getStartTimeForZone(int index) {
        return index * getNumTimeUnitsPerIndex() + startTime ;
    }

    public long getEndTimeForZone( int index ) {
        return ( index + 1 ) * getNumTimeUnitsPerIndex() + startTime ;
    }
}
