package org.cougaar.cpe.model;
import org.cougaar.util.UnaryPredicate;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.*;

public abstract class WorldState implements java.io.Serializable {

    protected WorldState() {
        targets = new ArrayList() ;
        units = new ArrayList() ;
        supplyVehicleEntities = new ArrayList(0) ;
        idToInfoMap = new HashMap() ;
    }


    public WorldState(double boardWidth, double boardHeight, double penaltyHeight) {
        this() ;
        info = new WorldStateInfo(boardWidth, boardHeight, penaltyHeight ) ;
    }

    public WorldState(double boardWidth, double boardHeight, double penaltyHeight, double recoveryLine, double deltaT ) {
        this() ;
        info = new WorldStateInfo(boardWidth, boardHeight, penaltyHeight, recoveryLine, deltaT ) ;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer() ;
        buf.append( "[WorldState t= ").append( time/1000.0 ) ;
        buf.append( ",attrit=" + accumulatedAttritionValue  ) ;
        buf.append( ",kills=" + accumulatedKills ) ;
        buf.append( ",violations=" + accumulatedViolations ) ;
        buf.append( ",penalties=" + accumulatedPenalties ) ;
        buf.append( ",score=" + getScore() ) ;
        buf.append( ",criticalCoverage=" + WorldStateUtils.computeCoverage( this, WorldStateUtils.COVERAGE_CRITICAL,
                WorldStateUtils.LIMITED_INVERSE_FUNCTION, 1.4, true ) ) ;
        buf.append( ",inRangeCoverage=" +  WorldStateUtils.computeCoverage( this, WorldStateUtils.COVERAGE_IN_RANGE,
                WorldStateUtils.LIMITED_INVERSE_FUNCTION, 1.4, true ));


        buf.append( ",\nunits=") ;
        for (int i = 0; i < units.size(); i++) {
            UnitEntity unitEntity = (UnitEntity)units.get(i);
            buf.append( unitEntity ) ;
            if (i<units.size()-1) {
                buf.append( ",") ;
            }
        }
        buf.append( ",\ntargets=[") ;
        for (int i = 0; i < targets.size(); i++) {
            buf.append( targets.get(i) ) ;
            //TargetEntity targetEntity = (TargetEntity)targets.get(i);
            //buf.append( targetEntity ) ;
            if ( i < targets.size() - 1 ) {
                buf.append( ",")  ;
            }
            buf.append( "]" ) ;
        }
        buf.append("]") ;
        return buf.toString() ;
    }

    public boolean isModel() {
        return false ;
    }

    /**
     * The simulation time in milliseconds.
     * @return
     */
    public long getTime() {
        return time;
    }

    public double getTimeInSeconds() {
        return time / 1000 ;
    }

    /**
     * The simulation step size, in seconds.
     * @return
     */
    public double getDeltaT() {
        return info.getDeltaT() ;
    }

    public long getDeltaTInMS() {
        return ( long ) ( info.getDeltaT() * VGWorldConstants.MILLISECONDS_PER_SECOND ) ;
    }

    public double getAccumulatedAttritionValue() {
        return accumulatedAttritionValue;
    }

    public double getAccumulatedKills() {
        return accumulatedKills;
    }

    public int getAccumulatedPenalties() {
        return accumulatedPenalties;
    }

    public int getAccumulatedViolations() {
        return accumulatedViolations;
    }

    public EntityInfo getEntityInfo( String id ) {
        return (EntityInfo) idToInfoMap.get(id) ;
    }

    public void setBaseTime( long time ) {
        info.setBaseTime( time );
    }

    public long getBaseTime() {
        return info.getBaseTime() ;
    }

    public double getGridPositionForPosition( double position ) {
        return Math.round( position / info.getGridSize() ) * info.getGridSize() ;
    }

    public UnitEntity addUnit( double x, double y, String id ) {
        UnitEntity entity = new UnitEntity( id, getGridPositionForPosition(x), y ) ;
        units.add( entity ) ;
        idToInfoMap.put( id, new EntityInfo( entity,
                new BinaryEngageByFireModel( VGWorldConstants.UNIT_STANDARD_HIT_PROBABILITY,
                        VGWorldConstants.UNIT_MULTI_HIT_PROBABILITY,
                        VGWorldConstants.UNIT_STANDARD_ATTRITION,
                        entity.getId().hashCode() ) )  ) ;
        return entity ;
    }

    public SupplyUnit addSupplyUnit( String id, double x, double y, ArrayList customers, ArrayList vehicles ) {
        SupplyUnit su = new SupplyUnit( id, x, y, customers, vehicles ) ;
        if ( supplyUnits == null) {
            supplyUnits = new ArrayList() ;
        }
        supplyUnits.add( su ) ;
        idToInfoMap.put( id, new EntityInfo( su, null) ) ;
        return su ;
    }

    public SupplyVehicleEntity addSupplyVehicle( double x, double y, String id ) {
        SupplyVehicleEntity entity = new SupplyVehicleEntity( id, getGridPositionForPosition(x), y ) ;
        if ( supplyVehicleEntities == null ) {
            supplyVehicleEntities = new ArrayList() ;
        }
        supplyVehicleEntities.add( entity ) ;
        idToInfoMap.put( id, new EntityInfo( entity, null ) ) ;
        return entity ;
    }

    /**
     * Adds the target and creates the appropriate sensor contacts for the target.
     *
     * @param x
     * @param y
     * @param dx
     * @param dy
     * @return
     */
    public TargetEntity addTarget( double x, double y, double dx, double dy ) {
        TargetEntity result = new TargetEntity( getNextTargetId(), getGridPositionForPosition(x), y, dx, dy, VGWorldConstants.getTargetFullStrength() ) ;
        targets.add( result ) ;
        idToInfoMap.put( result.getId(), new EntityInfo( result, null ) ) ;

        return result ;
    }

    public boolean deleteTarget( String s ) {
        boolean found = false ;
        for (int i = 0; i < targets.size(); i++) {
            TargetEntity targetEntity = (TargetEntity)targets.get(i);
            if ( targetEntity.getId().equals(s) ) {
                targets.remove( i ) ;
                found = true ;
                break ;
            }
        }
        idToInfoMap.remove( s ) ;

        return found ;
    }


    public Iterator getTargets() {
        return targets.iterator() ;
    }

    protected void getTargets( ArrayList result ) {
        // Sort targets from left to right.
        Iterator iter = getTargets() ;
        while (iter.hasNext())
        {
            TargetEntity entity = (TargetEntity) iter.next();
            result.add( entity ) ;
        }
    }

    public void getTargets( ArrayList result, UnaryPredicate predicate ) {
        Iterator iter = getTargets() ;
        while (iter.hasNext())
        {
            TargetEntity entity = (TargetEntity) iter.next();
            if ( predicate.execute( entity ) ) {
                result.add( entity ) ;
            }
        }
    }

    public Iterator getUnits() {
        return units.iterator() ;
    }

    public Iterator getSupplyVehicleEntities() {
        return supplyVehicleEntities.iterator() ;
    }

    public String getNextTargetId() {
        String result = "Target." + targetCount ;
        targetCount ++ ;

        return result ;
    }

    /**
     * The score is computed here using parameters from the info.
     *
     * @return
     */
    public double getScore() {
        return accumulatedAttritionValue * info.getAttritionFactor()
                + accumulatedKills * info.getKillScore()
                - ( accumulatedPenalties * info.getPenaltyFactor() +
                    accumulatedViolations * info.getViolationFactor() ) ;
    }

    public double getPenaltyHeight() {
        return info.getPenaltyHeight();
    }

    public double getBoardHeight() {
        return info.getBoardHeight();
    }

    public double getBoardWidth() {
        return info.getBoardWidth();
    }

    public double getRecoveryLine() {
        return info.getRecoveryLine();
    }

    public boolean isOnBoard( double x, double y ) {
        return x < getLowerX() || y < getLowerY() || x > getUpperX() || y > getUpperY() ;
    }

    public double getLowerX() {
        return 0 ;
    }

    public double getUpperX() {
        return getBoardWidth() ;
    }

    public double getLowerY() {
        return 0 ;
    }

    public double getUpperY() {
        return getBoardHeight() ;
    }

    public Entity getEntity( String id ) {
        EntityInfo info = getEntityInfo( id ) ;
        if ( info != null ) {
            return info.getEntity() ;
        }
        return null ;
    }

    /**
     * Execute the simulation and advance time by one delta t.
     */
    public void updateWorldState() {
        for (int i = 0; i < targets.size(); i++) {
           TargetEntity target = (TargetEntity)targets.get(i);
           target.update( this );
        }

        for (int i = 0; i < units.size(); i++) {
           UnitEntity unitEntity = (UnitEntity)units.get(i);
           unitEntity.update( this );
        }

        for (int i = 0; i < supplyVehicleEntities.size(); i++) {
          SupplyVehicleEntity supplyEntity = (SupplyVehicleEntity)supplyVehicleEntities.get(i);
          supplyEntity.update( this );
        }

        engageByFireCount.clear();
        computePenalties();

        long oldTime = time ;
        time = time + ( long ) ( getDeltaT() * VGWorldConstants.MILLISECONDS_PER_SECOND ) ;
        TimeAdvanceEvent tav = new TimeAdvanceEvent( oldTime, time ) ;
        fireEvent( tav );
    }

    protected void computePenalties( ) {
        for (int i = 0; i < targets.size(); i++) {
           TargetEntity targetEntity = (TargetEntity)targets.get(i);
           if ( targetEntity.isActive() && targetEntity.getY() < getPenaltyHeight() && targetEntity.getY() > 0 ) {
               PenaltyEvent pe = new PenaltyEvent( getTime(), targetEntity.getId(), targetEntity.getX(), targetEntity.getY() ) ;
               fireEvent( pe );
               accumulatedPenalties++ ;
           }
        }
    }

    /**
     *
     * @param regionBoundary
     * @param rx  Translation of the
     * @param ry
     * @param tx  Point to be tested
     * @param ty
     * @return
     */
    public boolean isInRegion( Shape regionBoundary, double rx, double ry, double tx, double ty ) {
        return regionBoundary.contains( tx - rx, ty - ry ) ;
    }

    public void getTargetsInRange( Shape s, ArrayList results ) {
        for (int i=0;i<targets.size();i++) {
            TargetEntity t = (TargetEntity) targets.get(i) ;
            if ( t.isActive() && isInRegion( s, 0, 0, t.getX(), t.getY() ) ) {
                results.add( t.getId() ) ;
            }
        }
    }

    public ArrayList getTargetsInRange( UnitEntity entity ) {
        Point2D p2d = entity.getPosition() ;
        double x = entity.getX(), y = entity.getY() ;
        Shape s = entity.getRangeShape() ;
        ArrayList results = null ;
        for (int i=0;i<targets.size();i++) {
            TargetEntity t = (TargetEntity) targets.get(i) ;
            if ( t.isActive() && isInRegion( s, x, y, t.getX(), t.getY() ) ) {
                if ( results == null ) {
                    results = new ArrayList();
                }
                results.add( t.getId() ) ;
            }
        }
        return results ;
    }

    public void getEntitiesInSensorRange( UnitEntity entity, ArrayList results ) {
        double x = entity.getX(), y = entity.getY() ;
        Shape s = entity.getSensorShape() ;
        for (int i=0;i<targets.size();i++) {
            TargetEntity t = (TargetEntity) targets.get(i) ;
            if ( t.isActive() && isInRegion( s, x, y, t.getX(), t.getY() ) ) {
                if ( results == null ) {
                    results = new ArrayList();
                }
                results.add( t.getId() ) ;
            }
        }
    }


    /**
     * Return a list of targets "covered" by the bounds of s in the x direction and sort by y.
     * @param x
     * @param y
     * @param s
     * @return
     */
    public ArrayList getTargetsCovered( double x, double y, Shape s ) {
        ArrayList results = null ;
        for (int i=0;i<targets.size();i++) {
            TargetEntity t = (TargetEntity) targets.get(i) ;
            if ( !t.isActive() ) {
                continue;
            }
            Rectangle2D bounds = s.getBounds2D() ;
            if ( t.isActive() &&
                    t.getX() >= bounds.getMinX() + x &&
                    t.getX() <= bounds.getMaxX() + x ) {
                if ( results == null ) {
                    results = new ArrayList();
                }
                results.add( t.getId() ) ;
            }
        }
        return results ;
    }

    /**
     * Return a list of targets "covered" by the bounds of s in the x direction and sort by y.
     * @param x
     * @param y
     * @param sensorShape The shape of the sensor range relative to (x,y) coordinates.
     * @param area The shape of the interested area in absolute coordinates.
     * @return
     */
    public ArrayList getTargetsCoveredInArea( double x, double y, Shape sensorShape, Shape area ) {
        ArrayList results = null ;
        for (int i=0;i<targets.size();i++) {
            TargetEntity t = (TargetEntity) targets.get(i) ;
            if ( !t.isActive() ) {
                continue;
            }
            Rectangle2D bounds = sensorShape.getBounds2D() ;
            if ( t.isActive() &&
                    t.getX() >= bounds.getMinX() + x &&
                    t.getX() <= bounds.getMaxX() + x )
            {
                if  ( area != null && !area.contains( t.getX(), t.getY() ) ) {
                    break ;
                }

                if ( results == null ) {
                    results = new ArrayList();
                }
                results.add( t.getId() ) ;
            }
        }
        return results ;
    }


    public void getUnitsInRange( double x, double y, Shape s, ArrayList results ) {
        if ( results == null ) {
           throw new IllegalArgumentException( "Results must be non-null" ) ;
        }
        results.clear();

        for (int i = 0; i < units.size(); i++) {
            UnitEntity unitEntity = (UnitEntity)units.get(i);
            if ( unitEntity.isActive() && isInRegion( s, x, y, unitEntity.getX(), unitEntity.getY() ) ) {
                results.add( unitEntity.getId() ) ;
            }
        }
    }

    public ArrayList getTargetsInRange( Point2D p2d, Shape s ) {
        return getTargetsInRange( p2d.getX(), p2d.getY(), s ) ;
    }

    public ArrayList getTargetsInRange( double x, double y, Shape s ) {
        ArrayList results = null ;
        for (int i=0;i<targets.size();i++) {
            TargetEntity t = (TargetEntity) targets.get(i) ;
            if ( t.isActive() && isInRegion( s, x, y, t.getX(), t.getY() ) ) {
                if ( results == null ) {
                    results = new ArrayList();
                }
                results.add( t.getId() ) ;
            }
        }
        return results ;
    }

    public ArrayList getTargetsInRange( double x, double y, Shape sensor, Shape area ) {
        ArrayList results = null ;
        for (int i=0;i<targets.size();i++) {
            TargetEntity t = (TargetEntity) targets.get(i) ;
            if ( t.isActive() && isInRegion( sensor, x, y, t.getX(), t.getY() ) ) {
                if ( area != null && !area.contains(t.getX(), t.getY() ) ) {
                    break ;
                }
                if ( results == null ) {
                    results = new ArrayList();
                }
                results.add( t.getId() ) ;
            }
        }
        return results ;
    }


    public ArrayList getCriticalTargetsCovered( double x, double y, Shape s, Shape area ) {
        ArrayList results = null ;
        for (int i=0;i<targets.size();i++) {
            TargetEntity t = (TargetEntity) targets.get(i) ;
            if ( t.isActive() && t.getY() < getPenaltyHeight() &&
                 isInRegion( s, x, y, t.getX(), t.getY() ) )
            {
                if ( area != null && !area.contains(t.getX(), t.getY() ) ) {
                     break ;
                }

                if ( results == null ) {
                    results = new ArrayList();
                }
                results.add( t.getId() ) ;
            }
        }
        return results ;
    }

    public MoveResult moveUnit( UnitEntity entity, double x, double y ) {
        double startX = entity.getX(), startY = entity.getY();
        double distance = Point2D.distance( startX, startY, x, y ) ;
        double maxDistance = VGWorldConstants.UNIT_NORMAL_MOVEMENT_RATE * getDeltaT() + 1E-5 ;
        double maxDistanceByFuel = entity.getFuelQuantity() / VGWorldConstants.UNIT_FUEL_CONSUMPTION_RATE ;
        if ( maxDistance > maxDistanceByFuel ) {
            maxDistance = maxDistanceByFuel ;
        }

        if ( distance <= maxDistance ) {
            entity.setPosition( x, y );
            double fuelConsumption = VGWorldConstants.UNIT_FUEL_CONSUMPTION_RATE * distance ;
            entity.setFuelQuantity( entity.getFuelQuantity() - fuelConsumption );
            return new MoveResult( startX, startY, entity.getX(), entity.getY(), fuelConsumption ) ;
        }
        // Move towards the dest as far as possible.
        else {
            double ratio = maxDistance / distance ;
            double fuelConsumption = VGWorldConstants.UNIT_FUEL_CONSUMPTION_RATE * maxDistance ;
            entity.setPosition( entity.getX() + ratio *( x - entity.getX()), entity.getY() + ratio * ( y-entity.getY() ) );
            entity.setFuelQuantity( entity.getFuelQuantity() - fuelConsumption );
            return new MoveResult( startX, startY, entity.getX(), entity.getY(), fuelConsumption ) ;
        }
    }

    /**
     * Move the supply units.  Supply units move on the -y axis and cannot move
     * above y=0.  They do not track units but simply move forwards and backwards
     * to provide a visualization of delay.
     *
     * @param x
     * @param y
     */
    public void moveSupplyUnit( SupplyVehicleEntity entity, double x, double y ) {
//        if ( Point2D.distance( x, y, entity.getX(), entity.getY()) <= entity.getMovementRate() * getDeltaT() ) {
//
//        }
        entity.setX( x );
        entity.setY( y );
    }

    /**
     * Move the unit towards the destination.
     *
     * @param entity
     * @param dest
     * @return Fuel consumption.
     */
    public MoveResult moveUnit( UnitEntity entity, Point2D dest ) {
        return moveUnit( entity, dest.getX(), dest.getY() ) ;
    }

    public void moveTarget( TargetEntity entity, double x, double y ) {
        if ( entity.isActive() ) {
            // Target has moved off the bottom of the board!
            if ( y < 0 ) {
                accumulatedViolations ++ ;
                ViolationEvent ve = new ViolationEvent( getTime(), 0, entity.getId(), ( float ) x, ( float ) y ) ;
                fireEvent( ve );
                entity.setActive( false );
            }
            entity.setX( x ) ;
            entity.setY( y ) ;
        }
    }

    public void setLogEvents( boolean value ) {
        if ( value == true && events == null ) {
            events = new ArrayList() ;
        }
        logEvents = value ;
    }

    public boolean isLogEvents() {
        return logEvents;
    }

    public ArrayList getEvents() {
        synchronized ( events ) {
            return (ArrayList) events.clone() ;
        }
    }

    public ArrayList clearEvents() {
        if ( events != null ) {
            synchronized ( events ) {
                ArrayList result = (ArrayList) events.clone() ;
                events.clear();
                return result ;
            }
        }
        return null ;
    }

    protected void addEvent( Object o ) {
        synchronized ( events ) {
            events.add( o ) ;
        }
    }

    public void moveTarget( TargetEntity entity, Point2D dest ) {
        moveTarget( entity, dest.getX(), dest.getY() );
    }

    public static class TimeAdvanceEvent extends CPEEvent {
        public TimeAdvanceEvent(long oldTime, long newTime)
        {
            super ( newTime ) ;
            this.oldTime = oldTime;
        }

        public long getOldTime()
        {
            return oldTime;
        }

        public long getNewTime()
        {
            return time;
        }

        private long oldTime ;
    }

    /**
     * Every target that enters a (predefined) area.
     */
    public static class EntryEvent extends CPEEvent {
        private String targetId;

        public EntryEvent(long time, float height, String targetId, float xTarget, float yTarget)
        {
            super( time ) ;
            this.height = height;
            this.xTarget = xTarget;
            this.yTarget = yTarget;
            this.targetId = targetId ;
        }

        public String getTargetId()
        {
            return targetId;
        }

        public float getHeight()
        {
            return height;
        }

        public long getTime()
        {
            return time;
        }

        public float getxTarget()
        {
            return xTarget;
        }

        public float getyTarget()
        {
            return yTarget;
        }

        private float height ;
        private float xTarget, yTarget ;
    }

    public static class ViolationEvent extends EntryEvent {
        public ViolationEvent(long time, float height, String targetId, float xTarget, float yTarget )
        {
            super(time, height, targetId, xTarget, yTarget);
        }
    }

    public static class PenaltyEvent extends CPEEvent {
        public PenaltyEvent( long time, String targetId, float xTarget, float yTarget)
        {
            super( time ) ;
            this.targetId = targetId;
            this.xTarget = xTarget;
            this.yTarget = yTarget;
        }

        public String getTargetId()
        {
            return targetId;
        }

        public float getxTarget()
        {
            return xTarget;
        }

        public float getyTarget()
        {
            return yTarget;
        }

        String targetId ;
        float xTarget, yTarget ;
    }

    public static class KillEvent extends CPEEvent {
        private String targetId;
        private String unitId;

        public KillEvent(long time, String unit, String targetId )
        {
            super(time);
            this.targetId = targetId ;
            this.unitId = unit ;
        }

        public String getTargetId()
        {
            return targetId;
        }

        public String getUnitId()
        {
            return unitId;
        }
    }

    public static class EngageByFireEvent extends CPEEvent {
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

        public long getTime()
        {
            return time;
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

    /**
     * Transfer amount of material from supply entity to unit entity.
     * @param entity
     * @param sentity
     */
    public void supply( UnitEntity entity, SupplyVehicleEntity sentity, int type, float quantity, SupplyResult sr ) {
        if ( sentity.getY() >= -1E-5 ) {
            if ( !isModel()) {
                System.out.println( getTimeInSeconds() + "::" + sentity +
                    " SUPPLYING " + entity + " with " + quantity + " units of " + SupplyTask.getStringForAction( type ) );
            }
            switch ( type ) {
                case SupplyTask.SUPPLY_AMMO :
                    int ammoTransfer = 0 ;
                    if ( quantity > sentity.getAmmoQuantity() ) {
                        ammoTransfer = sentity.getAmmoQuantity() ;
                    }
                    else {
                        ammoTransfer = ( int ) quantity ;
                    }

                    if ( ammoTransfer + entity.getAmmoQuantity() > VGWorldConstants.getMaxUnitAmmoLoad() ) {
                        ammoTransfer = VGWorldConstants.getMaxUnitAmmoLoad() - entity.getAmmoQuantity() ;
                        if ( ammoTransfer < 0 ) {
                            ammoTransfer = 0 ;
                        }
                    }

                    entity.setAmmoQuantity( entity.getAmmoQuantity() + ammoTransfer ) ;
                    sentity.setAmmoQuantity( sentity.getAmmoQuantity() - ammoTransfer ) ;
                    if ( sr != null ) {
                        sr.setResult( ammoTransfer, SupplyTask.SUPPLY_AMMO, getTime());
                    }
                    break ;
                case SupplyTask.SUPPLY_FUEL :
                    float fuelTransfer = 0 ;
                    if ( quantity > sentity.getFuelQuantity() ) {
                        fuelTransfer = sentity.getFuelQuantity() ;
                    }
                    else {
                        fuelTransfer = quantity ;
                    }

                    if ( fuelTransfer + entity.getFuelQuantity() > VGWorldConstants.getMaxUnitFuelLoad() ) {
                        fuelTransfer = VGWorldConstants.getMaxUnitFuelLoad() - entity.getFuelQuantity() ;
                        if ( fuelTransfer < 0 ) {
                            fuelTransfer = 0 ;
                        }
                    }

                    entity.setFuelQuantity( entity.getFuelQuantity() + fuelTransfer ) ;
                    sentity.setFuelQuantity( sentity.getFuelQuantity() - fuelTransfer ) ;
                    if ( sr != null ) {
                        sr.setResult( fuelTransfer, SupplyTask.SUPPLY_FUEL, getTime() );
                    }
                    break ;
                default :
                    throw new IllegalArgumentException( "Type " + type + " is not recognized." ) ;
            }
        }
    }

    public int getEngageByFireCount( String id ) {
        Integer count = (Integer) engageByFireCount.get( id ) ;
        if ( count == null ) {
           return 0 ;
        }
        return count.intValue() ;
    }

    public int incrementEngageByFireCount( String id ) {
        Integer count = (Integer) engageByFireCount.get( id ) ;
        if ( count == null ) {
            engageByFireCount.put( id, new Integer(1) ) ;
            return 1 ;
        }
        else {
            engageByFireCount.put( id, new Integer( count.intValue() + 1) ) ;
            return count.intValue() + 1 ;
        }
    }

    private EngageByFireResult tempResult = new EngageByFireResult() ;

    private HashMap engageByFireCount = new HashMap() ;

//    public void engageByFire( TargetEntity entity, UnitEntity unitEntity ) {
//         if ( !entity.isRouted() ) {
//             // Fire at the unit entity which is in range.  Increment the damage counter on that entity.
//             unitEntity.setStrength( unitEntity.getStrength() - VGWorldConstants.TARGET_STANDARD_ATTRITION);
//             accumulatedDamage ++ ;
//         }
//    }

    public EngageByFireResult engageByFire( UnitEntity entity, String target ) {

        EntityInfo info = ( EntityInfo ) idToInfoMap.get( entity.getId() ) ;
        UnitEntity ue = (UnitEntity) info.getEntity() ;
        EntityInfo tinfo = ( EntityInfo ) idToInfoMap.get( target ) ;
        if ( ue.getAmmoQuantity() > 0 &&
             isInRegion( entity.getRangeShape(), entity.getX(), entity.getY(),
                tinfo.getEntity().getX(), tinfo.getEntity().getY()  ) )
        {
            TargetEntity t = (TargetEntity) tinfo.getEntity() ;
            if ( !t.isActive()) {
                System.err.println("Attempted to engage non-active target " + target );
                return null ;
            }

            int engageCount = incrementEngageByFireCount( target ) ;

            // Engage for a single deltaT.
            double startTime = getTime() * VGWorldConstants.SECONDS_PER_MILLISECOND ;
            info.getModel().nextAttritionValue( t, tempResult,
                    startTime, startTime + getDeltaT(), engageCount - 1 ) ;

            double attritValue = tempResult.getAttritValue() ;
            if ( attritValue != 0 ) {
                t.setSuppressed( true, getTime() );
                if ( attritValue > t.getStrength() ) {
                    attritValue = t.getStrength() ;
                }
                accumulatedAttritionValue += attritValue ;
                t.setStrength( t.getStrength() - attritValue );
                if ( t.getStrength() <= 0 ) {
                    t.setStrength( 0);
                    t.setActive( false );
                    KillEvent ke = new KillEvent( getTime(), entity.getId(), t.getId() ) ;
                    fireEvent( ke );
                    accumulatedKills ++ ;
                }
            }

            int consumption = tempResult.getAmmoConsumed() ;
            ue.setAmmoQuantity( ue.getAmmoQuantity() - consumption );
            if ( logEvents ) {
                events.add( new EngageByFireEvent( entity.getId(),
                            new EngageByFireResult(t.getId(), attritValue,
                                consumption, false), getTime(),
                            t.getX(), t.getY(), ue.getX(), ue.getY() ) ) ;
                fireEvent( new EngageByFireEvent( entity.getId(),
                            new EngageByFireResult(t.getId(), attritValue,
                                consumption, false), getTime(),
                            t.getX(), t.getY(), ue.getX(), ue.getY() ) ) ;
            }
            return new EngageByFireResult(t.getId(), attritValue,
                    consumption, false) ;
        }
        else {
            return null ;
        }
    }

    protected void fireEvent( CPEEvent event ) {
        for (int i = 0; i < eventListenerList.size(); i++) {
          CPEEventListener eventListener = (CPEEventListener) eventListenerList.get(i);
            try {
                eventListener.notify( event );
            }
            catch ( Exception e ) {
                e.printStackTrace();
            }
        }
    }

    public void addEventListener( CPEEventListener listener ) {
        this.eventListenerList.add( listener ) ;
    }

    protected float accumulatedAttritionValue ;

    protected float accumulatedKills ;

    /**
     * Penalities are incurred when Targets fall below the penalty line or
     * when they fall below the bottom of the board.
     */
    protected int accumulatedPenalties ;

    /**
     * Violations are accumulated when targets fall below the bottom of the map.
     */
    protected int accumulatedViolations ;

    /**
     * Damage is accumulated when targets fire at nearby targets.
     */
    protected int accumulatedDamage = 0 ;

    /**
     * Time in milliseconds.
     */
    protected long time = 0 ;

    protected WorldStateInfo info ;

    /**
     * Number of targets created thus far.
     */
    private static int targetCount = 0 ;
    protected ArrayList targets ;
    protected ArrayList units ;
    protected ArrayList supplyUnits ;
    protected ArrayList supplyVehicleEntities ;
    protected HashMap idToInfoMap ;

    protected ArrayList eventListenerList = new ArrayList() ;

    /**
     * These are a list of local events that have occured (e.g. fire events, etc.)
     */
    protected ArrayList events = null ;
    private boolean logEvents;

    public void addEntity(Entity entity) {
        if (idToInfoMap.get(entity.getId()) != null) {
            throw new RuntimeException("Entity " + entity + " already exists.");
        }

        EntityInfo info = new EntityInfo(entity, null);
        if (entity instanceof TargetEntity) {
            targets.add(entity);
        } else if (entity instanceof UnitEntity) {
            units.add(entity);
        }
        idToInfoMap.put(entity.getId(), info);
    }

    public void addEntity(Entity entity, EngageByFireModel model) {
        if (idToInfoMap.get(entity.getId()) != null) {
            throw new RuntimeException("Entity " + entity + " already exists.");
        }

        EntityInfo info = new EntityInfo(entity, model);
        if (entity instanceof TargetEntity) {
            targets.add(entity);
        } else if (entity instanceof UnitEntity) {
            units.add(entity);
        }
        idToInfoMap.put(entity.getId(), info);
    }

    protected BinaryEngageByFireModel defaultEngageByFireModel = new BinaryEngageByFireModel( VGWorldConstants.UNIT_STANDARD_HIT_PROBABILITY,
                        VGWorldConstants.UNIT_MULTI_HIT_PROBABILITY,
                        VGWorldConstants.UNIT_STANDARD_ATTRITION,
                        0 ) ;

}
