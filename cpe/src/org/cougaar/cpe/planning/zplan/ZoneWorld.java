package org.cougaar.cpe.planning.zplan;

import org.cougaar.cpe.model.*;

import java.util.*;
import java.awt.geom.Rectangle2D;

/**
 * Subdivide the original world into discrete aggregated units.
 */
public class ZoneWorld extends WorldStateModel
{

    // BNEntityEngagementModel model = new BNEntityEngagementModel() ;

    public  EngageByFireResult engage( BNAggregate agg ) {
        //
        // Find lowest N targets and deduct damage,
        //
        Rectangle2D r2d = new Rectangle2D.Float( getZoneLower( (IndexedZone) agg.getCurrentZone() ), 0,
                getZoneSize( (IndexedZone) agg.getCurrentZone() ), (float) VGWorldConstants.getUnitRangeHeight()  ) ;
        ArrayList results = new ArrayList() ;
        getTargetsInRange( r2d, results ) ;

        EngageByFireResult er = new EngageByFireResult() ;

        if ( results.size() == 0 ) {
            return er ;
        }

        for (int i=0;i<results.size();i++) {
            results.set( i, getEntity( (String) results.get(i) ) ) ;
        }
        Collections.sort( results, new Comparator() {
            public int compare(Object o1, Object o2)
            {
                Entity e1 = (Entity) o1, e2 = (Entity) o2 ;
                if ( e1.getY() < e2.getY() ) {
                    return -1 ;
                }
                if ( e1.getY() > e2.getY() ) {
                    return +1 ;
                }
                return 0 ;
            }
        });


        // This is a round robin estimate of damage and assumes that the targets can always be hit
        // by entities.  Also, the assignment is simplistic and assumes a "round robin" type of interaction,
        // i.e. the lowest target is covered with highest priority, then the second lower target, and so forth.
        // It does not factor in the movement time between targets if they are dispersed.

        int ammoConsumption = 0 ;
        float attrition = 0 ;

        if ( results.size() > 0 ) {
            for (int i=0;i<Math.max( results.size(), agg.getNumSubEntities() ) ;i++) {
                TargetEntity targetEntity = (TargetEntity) results.get( i % results.size() ) ;
                String aggId = agg.getSubEntityName( i % agg.getNumSubEntities() ) ;
                EntityInfo info = getEntityInfo( aggId ) ;
                defaultEngageByFireModel.estimateAttritionValue( targetEntity, er, getTime(), getTime() + getDeltaT() );
                attrition += er.getAttritValue() ;
                ammoConsumption += er.getAmmoConsumed() ;
                accumulatedAttritionValue += er.getAttritValue() ;
                targetEntity.setStrength( targetEntity.getStrength() - er.getAttritValue() );
                if ( targetEntity.getStrength() <= 0 ) {
                    targetEntity.setActive( false );
                    targetEntity.setSuppressed( true, getTime() );
                    accumulatedKills ++ ;
                    results.remove( targetEntity ) ;
                }
                if ( results.size() == 0 ) {
                    break ;
                }
            }
        }

        er.setAmmoConsumed( ammoConsumption );
        er.setAttritValue( attrition );
        return er ;
    }

    /**
     * Returns an overestimate of the fuel consumption by looking at how dispersed the targets.
     * Note that this is not useful for fuel consumption estimates!
     *
     * @param targets
     * @return
     */
    public static float estimateTargetDispersion( ArrayList targets ) {

        float result = 0  ;
        Collections.sort( targets, new Comparator() {
            public int compare(Object o1, Object o2)
            {
                TargetEntity t1 = (TargetEntity) o1, t2 = (TargetEntity) o2 ;
                if ( t1.getY() < t2.getY() ) {
                    return -1 ;
                }
                if ( t1.getY() > t2.getY() ) {
                    return 1 ;
                }
                return 0 ;
            }
        });

        for (int i = 0; i < targets.size() - 1 ; i++) {
            TargetEntity entity = ( TargetEntity)targets.get(i);
            TargetEntity next = (TargetEntity) targets.get(i+1) ;
            result += Math.abs( entity.getX() - next.getX() ) ;
        }

        return result ;
    }

    /**
     *  Utility method for aalculating the typical fuel consumption for redistribution within a zone.
     *  Assuming the standard equal spacing.
     *
     * @param from
     * @param to
     * @param numUnits
     * @return
     */
    public static float calculateAdditionalFuelConsumption( Interval from, Interval to, int numUnits ) {
        float[] startXCoords = new float[ numUnits ] ;
        float[] endXCoords = new float[ numUnits ] ;

        float u1 = from.getXUpper(), l1 = from.getXLower(), s1 = u1 - l1 ;
        float u2 = to.getXUpper(), l2 = to.getXLower(), s2 = u2 - l2 ;

        for (int i = 0; i < endXCoords.length; i++)
        {
            startXCoords[i] = ( i + 0.5f ) * ( s1 ) / numUnits +  l1 ;
            endXCoords[i] = ( i + 0.5f ) * ( s2 ) / numUnits +  l2 ;
        }

        float fuelConsumption = 0 ;
        for (int i = 0; i < endXCoords.length; i++)
        {
            fuelConsumption += Math.abs( startXCoords[i] - endXCoords[i] ) ;
        }

        fuelConsumption *= VGWorldConstants.getUnitFuelConsumptionRate()  ;
        return fuelConsumption ;
    }

    /**
     * @param ws
     * @param zoneGridSize The size of each zone element
     */
    public ZoneWorld(WorldState ws, float zoneGridSize)
    {
        // Make sure we clone all the active units.
        super(ws, true, false );
        this.zoneGridSize = zoneGridSize ;

        // Number of zones in the grid.  Note that incomplete zones are also treated as zones.
        numZones = (int) Math.ceil( ( ws.getUpperX() - ws.getLowerX() ) / zoneGridSize ) ;
    }

    public String toString()
    {
        StringBuffer buf = new StringBuffer() ;
        buf.append( "ZoneWorld " ) ;
        buf.append( "time=" ).append( getTimeInSeconds() ).append( ' ' ) ;
        buf.append( aggEntities ) ;
        return buf.toString() ;
    }

//    public BNEntityEngagementModel getEngagementModel()
//    {
//        return model;
//    }

    public ZoneWorld( ZoneWorld zw ) {
        this( zw, zw.getZoneGridSize() ) ;

        // Clone all the aggregates underneath me
        aggEntities = new ArrayList( zw.aggEntities.size() ) ;
        for (int i=0;i<zw.getNumAggUnitEntities();i++) {
            Aggregate agg = zw.getAggUnitEntity( i ) ;
            addAggUnitEntity( ( Aggregate ) agg.clone() );
        }
    }

    public float getZoneGridSize()
    {
        return zoneGridSize ;
    }


    /**
     * Find the current actualized zone for an aggregate.
     * @param id
     * @return
     */
    public IndexedZone getContiguousZoneForAggregateUnit( String id ) {
        Aggregate agg = getAggUnitEntity( id ) ;
        if ( agg == null ) {
            throw new RuntimeException("Id " + id + " is not found.") ;
        }

        float lower = Float.MAX_VALUE , upper = Float.MIN_VALUE ;
        for (int i=0;i<agg.getNumSubEntities();i++) {
            UnitEntity unitEntity = (UnitEntity) getEntity( agg.getSubEntityName(i) ) ;
            lower = Math.min( unitEntity.getX(), lower ) ;
            upper = Math.max( unitEntity.getX(), upper ) ;

        }
        if ( upper < lower ) {
            return null ;
        }
        int lzone = getZoneIndexForXCoord( lower ) ;
        int uzone = getZoneIndexForXCoord( upper ) ;

        return new IndexedZone( lzone, uzone ) ;
    }

    public Object clone()
    {
        ZoneWorld result = new ZoneWorld( this, zoneGridSize ) ;

        // Clone all the aggregate entities within this world representation.
        for (Iterator iterator = aggEntitiesMap.values().iterator(); iterator.hasNext();)
        {
            Aggregate entity = (Aggregate) iterator.next();
            Aggregate newEntity = (Aggregate) entity.clone() ;
            result.aggEntities.add( newEntity ) ;
            result.aggEntitiesMap.put( newEntity.getId(), newEntity ) ;
        }

        return result ;
    }

    /**
     * Advances the zone world by numDeltaT.
     *
     * Execution model assumptions.
     * - BN agents are assigned to zones.
     * - BN agents have a certain number of aggregated units.
     * - Total firepower is aggregated from the number of companies multiplied by the integration timestep.
     *
     * - The target model is identical.
     *
     * - Fuel consumption rates are based on the comparative dispersion between a current set of target zones and the next
     * set of active zones.  There will be projected fuel consumption based on the size of the zone and the target density.
     */
    public void updateWorldState() {

        // Update the targets only
        for (int i = 0; i < targets.size(); i++) {
           TargetEntity target = (TargetEntity)targets.get(i);
           target.update( this );
        }
//            super.updateWorldState();

        // Now, update all the aggregate units.
        for (int j=0;j<aggEntities.size();j++) {
            Aggregate agg = (Aggregate) aggEntities.get(j) ;
            agg.update( this ) ;
        }

        time = time + ( long ) ( getDeltaT() * VGWorldConstants.MILLISECONDS_PER_SECOND ) ;
        computePenalties();
    }

    public int getZoneIndexForXCoord( float x ) {
        return (int) Math.floor( x / getZoneGridSize() ) ;
    }

    public float getZoneLower( IndexedZone zone ) {
        return zone.getStartIndex() * getZoneGridSize() ;
    }

    public float getZoneUpper( IndexedZone zone ) {
        return ( zone.getEndIndex() + 1 ) * getZoneGridSize() ;
    }

    public float getZoneSize( IndexedZone zone ) {
        return getZoneUpper( zone ) - getZoneLower( zone ) ;
    }

    public void addAggUnitEntity( Aggregate entity ) {
        aggEntitiesMap.put( entity.getId(), entity ) ;
        aggEntities.add( entity ) ;
    }

    public int getNumAggUnitEntities(){
        return aggEntities.size() ;
    }

    public Aggregate getAggUnitEntity(int index ) {
        return (Aggregate) aggEntities.get(index) ;
    }

    public Aggregate getAggUnitEntity(String name) {
        return (Aggregate) aggEntitiesMap.get( name ) ;
    }

    ArrayList aggEntities = new ArrayList() ;
    HashMap aggEntitiesMap = new HashMap() ;

    /**
     * The x width of zones.
     */
    float zoneGridSize ;

    int numZones ;

    public int getNumZones()
    {
        return numZones ;
    }

    public static final void main( String[] args ) {

        Interval i1 = new Interval( 10, 20 ) ;
        Interval i2 = new Interval( 12, 18 ) ;
        float consumption = calculateAdditionalFuelConsumption( i1, i2, 3) ;
        System.out.println("Consumption from interval " + i1 + " to " + i2 + " is " + consumption + " fuel units.");
    }

    public Interval getIntervalForZone(IndexedZone zone)
    {
        return new Interval( getZoneLower( zone), getZoneUpper( zone ) ) ;
    }

    /**
     * Performs a linear interpolation between the start zone and the end zone.  If the time is before the currentTask,
     * the startZone is returned.  If it is after the current task, the end zone is returned.
     *
     * @param currentTask
     * @param time
     * @return
     */
    public static Interval interpolateIntervals(ZoneTask currentTask, long time )
    {
        Interval currentZone = null, nextZone = null ;

        // Compute the current and next zone intervals based on the ( fieldLower and zoneGridSize )
        currentZone = (Interval) currentTask.getStartZone() ;
        nextZone = (Interval) currentTask.getEndZone() ;
        return interpolateIntervals( currentZone, nextZone, currentTask.getStartTime(), currentTask.getEndTime(), time ) ;

//        if ( time < currentTask.getStartTime() ) {
//            return currentZone ;
//        }
//        if ( time > currentTask.getEndTime() ) {
//            return nextZone ;
//        }
//
//        float ratio =  ( time - currentTask.getStartTime() ) / ( currentTask.getEndTime() - currentTask.getStartTime() ) ;
//        float leftValue = ( nextZone.getXLower() - currentZone.getXLower() ) * ratio + currentZone.getXLower() ;
//        float rightValue = ( nextZone.getXUpper() - currentZone.getXUpper() ) * ratio + currentZone.getXUpper() ;
//        currentZoneInterval = new Interval( leftValue, rightValue ) ;
//        return currentZoneInterval;
    }

    public Interval interpolateIntervalForTask( ZoneTask currentTask, long time ) {
        Interval current = null, next = null ;
        if ( currentTask.getStartZone() instanceof IndexedZone ) {
            current = getIntervalForZone( ( IndexedZone ) currentTask.getStartZone() ) ;
        }
        else {
            current = (Interval) currentTask.getStartZone() ;
        }
        if ( currentTask.getEndZone() instanceof IndexedZone ) {
            next = getIntervalForZone( ( IndexedZone )  currentTask.getEndZone() ) ;
        }
        else {
            next = (Interval) currentTask.getEndZone() ;
        }

        return interpolateIntervals( current,  next, currentTask.getStartTime(), currentTask.getEndTime(), time ) ;
    }

    public static Interval interpolateIntervals( Interval currentZone, Interval nextZone, long startTime, long endTime, long time )
    {
        Interval currentZoneInterval;

        // Compute the current and next zone intervals based on the ( fieldLower and zoneGridSize )
        if ( time < startTime ) {
            return currentZone ;
        }
        if ( time > endTime ) {
            return nextZone ;
        }

        float ratio =  ( ( float ) time - ( float ) startTime ) / ( ( float ) endTime - ( float ) startTime ) ;
        float leftValue = ( nextZone.getXLower() - currentZone.getXLower() ) * ratio + currentZone.getXLower() ;
        float rightValue = ( nextZone.getXUpper() - currentZone.getXUpper() ) * ratio + currentZone.getXUpper() ;
        currentZoneInterval = new Interval( leftValue, rightValue ) ;
        return currentZoneInterval;
    }

}

