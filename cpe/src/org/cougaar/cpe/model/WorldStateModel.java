package org.cougaar.cpe.model;

import org.cougaar.cpe.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.awt.geom.Point2D;
import java.awt.*;

/**
 *
 * Modifications include:
 *
 * Filtering by sensor model.
 * A correction in the engageByFire.
 *
 */
public class WorldStateModel extends WorldState {

    public static final int SENSOR_PERFECT = 0 ;
    public static final int SENSOR_LONG = 1 ;
    public static final int SENSOR_MEDIUM = 2 ;
    public static final int SENSOR_SHORT = 3 ;

    /**
     * The WorldStateModel is a deterministic projection of the actual
     * WorldState. In projecting mode, fuel and ammo levels are not bounded below by zero.
     * We can execute with a negative value.
     */
    protected boolean isProjecting = true;

    public boolean isProjecting() {
        return isProjecting;
    }

    public WorldStateModel(WorldState ws) {
        this(ws, false);
    }

    public WorldStateModel(WorldState ws, boolean cloneUnits) {
        this(ws, cloneUnits, false);
    }

    public WorldStateModel( WorldState ws, boolean cloneTargets, boolean cloneUnits, boolean cloneSupplyEntities ) {
        this.info = ws.info;
        synchronized (ws) {
            copyUnitAndTargetStatus( ws, cloneTargets, cloneUnits, cloneSupplyEntities );
        }
    }

    public void setTime( WorldState state ) {
        time = state.getTime() ;
    }

    public WorldStateModel(WorldState ws, boolean cloneUnits, boolean cloneSupplyEntities) {
        this( ws, true, cloneUnits, cloneSupplyEntities ) ;
//        this.info = ws.info;
//        synchronized (ws) {
//            copyUnitAndTargetStatus( ws, true, cloneUnits, cloneSupplyEntities );
//        }
//            // Create all the data structures to save space?
//            this.targets = new ArrayList(ws.targets.size());
//            this.units = new ArrayList(ws.units.size());
//            this.supplyVehicleEntities = new ArrayList(ws.supplyVehicleEntities.size());
//            this.idToInfoMap = new HashMap(ws.idToInfoMap.size());
//
//            this.time = ws.time;
//            // this.deltaT = ws.deltaT ;
//            this.accumulatedAttritionValue = ws.accumulatedAttritionValue;
//            this.accumulatedKills = ws.accumulatedKills;
//            this.accumulatedPenalties = ws.accumulatedPenalties;
//            this.accumulatedViolations = ws.accumulatedViolations;
//
//            // Now clone all the targets that are still active.
//            // this.targetCount = ws.targetCount;
//            for (int i = 0; i < ws.targets.size(); i++) {
//                TargetEntity targetEntity = (TargetEntity) ws.targets.get(i);
//                if (targetEntity.isActive()) {
//                    TargetEntity newTargetEntity = (TargetEntity) targetEntity.clone();
//                    targets.add(newTargetEntity);
//                    EntityInfo info = new EntityInfo(newTargetEntity);
//                    idToInfoMap.put(targetEntity.getId(), info);
//                }
//            }
//
//            // Now, clone all the units.
//            if (cloneUnits) {
//                for (int i = 0; i < ws.units.size(); i++) {
//                    UnitEntity unitEntity = (UnitEntity) ((UnitEntity) ws.units.get(i)).clone();
//                    units.add(unitEntity);
//                    EntityInfo info = ws.getEntityInfo(unitEntity.getId());
//                    EntityInfo newInfo = new EntityInfo(unitEntity, info.getModel());
//                    idToInfoMap.put(unitEntity.getId(), newInfo);
//                }
//            }
//
//            // Now clone all the supply units and vehicles
//            if (cloneSupplyEntities) {
//                if (ws.supplyUnits != null) {
//                    supplyUnits = new ArrayList(ws.supplyUnits.size()) ;
//                    for (int i = 0; i < ws.supplyUnits.size(); i++) {
//                        SupplyUnit su = (SupplyUnit) ws.supplyUnits.get(i);
//                        su = (SupplyUnit) su.clone();
//                        EntityInfo info = new EntityInfo(su);
//                        supplyUnits.add(su);
//                        idToInfoMap.put(su.getId(), info);
//                    }
//                }
//
//                if (ws.supplyVehicleEntities != null) {
//                    supplyVehicleEntities = new ArrayList(ws.supplyVehicleEntities.size() ) ;
//                    for (int i = 0; i < ws.supplyVehicleEntities.size(); i++) {
//                        SupplyVehicleEntity se = (SupplyVehicleEntity) ws.supplyVehicleEntities.get(i);
//                        se = (SupplyVehicleEntity) se.clone();
//                        EntityInfo info = new EntityInfo(se);
//                        supplyVehicleEntities.add(se);
//                        idToInfoMap.put(se.getId(), info);
//                    }
//                }
//            }

    }

    /**
     * Copy over the world state information from the WorldState.
     * @param ws
     */
    public void copyUnitAndTargetStatus( WorldState ws, boolean cloneActiveTargets, boolean cloneUnits, boolean cloneSupplyUnits) {
        // Just reallocate everything from scratch.
        this.targets = new ArrayList(ws.targets.size());
        this.units = new ArrayList(ws.units.size());
        this.supplyVehicleEntities = new ArrayList(ws.supplyVehicleEntities.size());
        this.idToInfoMap = new HashMap(ws.idToInfoMap.size());

        this.time = ws.time;
        // this.deltaT = ws.deltaT ;
        this.accumulatedAttritionValue = ws.accumulatedAttritionValue;
        this.accumulatedKills = ws.accumulatedKills;
        this.accumulatedPenalties = ws.accumulatedPenalties;
        this.accumulatedViolations = ws.accumulatedViolations;

        // Now clone all the targets that are still active.
        // this.targetCount = ws.targetCount;
        if ( cloneActiveTargets ) {
            for (int i = 0; i < ws.targets.size(); i++) {
                TargetEntity targetEntity = (TargetEntity) ws.targets.get(i);
                if (targetEntity.isActive()) {
                    TargetEntity newTargetEntity = (TargetEntity) targetEntity.clone();
                    targets.add(newTargetEntity);
                    EntityInfo info = new EntityInfo(newTargetEntity);
                    idToInfoMap.put(targetEntity.getId(), info);
                }
            }
        }

        // Now, clone all the units.
        if (cloneUnits) {
            for (int i = 0; i < ws.units.size(); i++) {
                UnitEntity unitEntity = (UnitEntity) ((UnitEntity) ws.units.get(i)).clone();
                units.add(unitEntity);
                EntityInfo info = ws.getEntityInfo(unitEntity.getId());
                EntityInfo newInfo = new EntityInfo(unitEntity, info.getModel());
                idToInfoMap.put(unitEntity.getId(), newInfo);
            }
        }

        // Now clone all the supply units and vehicles
        if (cloneSupplyUnits) {
            if (ws.supplyUnits != null) {
                supplyUnits = new ArrayList(ws.supplyUnits.size()) ;
                for (int i = 0; i < ws.supplyUnits.size(); i++) {
                    SupplyUnit su = (SupplyUnit) ws.supplyUnits.get(i);
                    su = (SupplyUnit) su.clone();
                    EntityInfo info = new EntityInfo(su);
                    supplyUnits.add(su);
                    idToInfoMap.put(su.getId(), info);
                }
            }

            if (ws.supplyVehicleEntities != null) {
                supplyVehicleEntities = new ArrayList(ws.supplyVehicleEntities.size() ) ;
                for (int i = 0; i < ws.supplyVehicleEntities.size(); i++) {
                    SupplyVehicleEntity se = (SupplyVehicleEntity) ws.supplyVehicleEntities.get(i);
                    se = (SupplyVehicleEntity) se.clone();
                    EntityInfo info = new EntityInfo(se);
                    supplyVehicleEntities.add(se);
                    idToInfoMap.put(se.getId(), info);
                }
            }
        }


    }


    public Object clone() {
        WorldStateModel state = new WorldStateModel(this, true);
        return state;
    }


    public boolean isModel() {
        return true;
    }

    /**
     * This is a projected version of the super method.  It does keep track of
     *
     * @param entity
     * @param target
     * @return
     */

    public EngageByFireResult engageByFire(UnitEntity entity, String target) {
//        if (!isProjecting) {
//            return super.engageByFire(entity, target);
//        }

        EntityInfo info = (EntityInfo) idToInfoMap.get(entity.getId());
        EntityInfo tinfo = (EntityInfo) idToInfoMap.get(target);
        if (isInRegion(entity.getRangeShape(), entity.getX(), entity.getY(),
                tinfo.getEntity().getX(), tinfo.getEntity().getY())) {
            TargetEntity t = (TargetEntity) tinfo.getEntity();

            EngageByFireModel model = info.getModel();
            EngageByFireResult er = new EngageByFireResult();
            double startTime = getTime() * VGWorldConstants.SECONDS_PER_MILLISECOND;

            int engageCount = incrementEngageByFireCount( target ) ;
            model.estimateAttritionValue(t, er, startTime, startTime + getDeltaT(), engageCount - 1);
            t.setSuppressed( true, getTime() );

            /**
             * Use the average attrition rate.
             */
            double attritValue = er.getAttritValue();
            double strength = t.getStrength() - attritValue;
            if (strength > 0) {
                t.setStrength(t.getStrength() - attritValue);
                accumulatedAttritionValue += attritValue ;
                return new EngageByFireResult(t.getId(), attritValue, er.getAmmoConsumed(), false);
            } else {
                t.setActive( false );
                accumulatedKills++ ;
                return new EngageByFireResult(t.getId(), strength, er.getAmmoConsumed(), true);
            }
        } else {
            return null;
        }
    }

    /**
     * Try to move towards the destination. In projection mode, we don't care if
     * the fuel level is < 0, since resupply is assumed.
     *
     * @param entity
     * @param dest
     * @return
     */
    public MoveResult moveUnit(UnitEntity entity, Point2D dest) {
//        if (!isProjecting) {
//            return super.moveUnit(entity, dest);
//        }

        Point2D start = entity.getPosition();
        double distance = entity.getPosition().distance(dest);
        double maxDistance = VGWorldConstants.UNIT_NORMAL_MOVEMENT_RATE * getDeltaT() + 1E-5;

        if (distance <= maxDistance) {
            entity.setPosition(dest.getX(), dest.getY());
            double fuelConsumption = VGWorldConstants.UNIT_FUEL_CONSUMPTION_RATE * distance;
            entity.setFuelQuantity(entity.getFuelQuantity() - fuelConsumption);
            return new MoveResult(start.getX(), start.getY(), entity.getX(), entity.getY(), fuelConsumption);
        }
        // Move towards the dest as far as possible.
        else {
            double ratio = maxDistance / distance;
            double fuelConsumption = VGWorldConstants.UNIT_FUEL_CONSUMPTION_RATE * maxDistance;
            entity.setPosition(entity.getX() + ratio * dest.getX(), entity.getY() + ratio * dest.getY());
            entity.setFuelQuantity(entity.getFuelQuantity() - fuelConsumption);
            return new MoveResult(start.getX(), start.getY(), entity.getX(), entity.getY(), fuelConsumption);
        }
    }


    /**
     * Project the world state forward by deltaIncrement seconds.
     * @param deltaIncrements
     * @param units A list of units for which a set of plausible targets will be
     * discovered.
     * @return
     */
    public WorldStateModel projectTargets(int deltaIncrements, ArrayList units) {
        WorldStateModel state = new WorldStateModel(this, false);
        for (int j = 0; j < deltaIncrements; j++) {
            for (int i = 0; i < targets.size(); i++) {
                TargetEntity target = (TargetEntity) targets.get(i);
                target.update(this);
            }

            for (int i = 0; i < units.size(); i++) {
                UnitEntity o = (UnitEntity) units.get(i);
                o.update(state);
            }

            // For each entity project its location and get a list of targets.
            for (int i = 0; i < units.size(); i++) {
                UnitEntity o = (UnitEntity) units.get(i);
                ArrayList targets = getTargetsInRange(o.getX(), o.getY(), o.getRangeShape());
                if (targets != null) {
                    Plan p = o.getManueverPlan();
                    UnitTask t = (UnitTask) p.getTask(0);
                    for (int k = 0; k < targets.size(); k++) {
                        String s = (String) targets.get(k);
                        t.addTarget(s);
                    }
                }
            }
            time = time + (long) (getDeltaT() * VGWorldConstants.MILLISECONDS_PER_SECOND);
        }

        return state;
    }

}
