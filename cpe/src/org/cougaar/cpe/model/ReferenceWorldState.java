/*
 * <copyright>
 *  Copyright 2003-2004 Intelligent Automation, Inc.
 *  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the Cougaar Open Source License as published by
 *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
 *
 *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 *  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *  PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */

package org.cougaar.cpe.model;

import org.cougaar.cpe.model.events.*;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.*;

/**
 * Represents the reference external world used in simulation and the WorldState agent.
 */

public class ReferenceWorldState extends WorldState
{
    public static final int SENSOR_DEFAULT = 0 ;
    public static final int SENSOR_ACTIVE = 1 ;

    private int sensorModelType = SENSOR_DEFAULT ;
    private SensorModel longRangeSensor;
    private SensorModel mediumRangeSensor;
    private SensorModel shortRangeSensor ;
    private int defaultIntegrationPeriod = 20000 ;

    /**
     * The identifier for the region used to measure op tempo.
     */
    public static final String REGION_OP_TEMPO = "OpTempoRegion";
    public static final String REGION_PREDICTED_ENTRY_REGION = "PredictedEntryRegion" ;

    /**
     * Predict the entry rate 60 seconds from now.
     */
    private static final double PREDICTED_ENTRY_DELAY = 60.0;

    public ReferenceWorldState(double boardWidth, double boardHeight, double penaltyHeight, double recoveryLine, double deltaT)
    {
        super(boardWidth, boardHeight, penaltyHeight, recoveryLine, deltaT);
        initSensors();
//        longRangeSensor = new StandardSensor( VGWorldConstants.getLongSensorMaxError(), 0, ( float ) boardHeight ) ;
//        mediumRangeSensor = new StandardSensor( VGWorldConstants.getMediumSensorMaxError(), 0, VGWorldConstants.getMediumSensorRange() ) ;
//        shortRangeSensor = new StandardSensor( 0, 0, VGWorldConstants.getUnitSensorHeight() ) ;
//        sensors.add( longRangeSensor ) ;
//        sensors.add( mediumRangeSensor ) ;
//        sensors.add( shortRangeSensor ) ;
//        regions.add( new Region(new Rectangle2D.Double(0,0,boardWidth,VGWorldConstants.getUnitRangeHeight()), REGION_OP_TEMPO ) ) ;
        setDefaultMetric( new MeasuredWorldMetrics( WorldState.DEFAULT_METRIC, this, defaultIntegrationPeriod ) ) ;
    }

    public ReferenceWorldState(double boardWidth, double boardHeight, double penaltyHeight, double recoveryLine,
                               double deltaT, int sensorType)
    {
        super(boardWidth, boardHeight, penaltyHeight, recoveryLine, deltaT);
        setSensorModelType( sensorType );
        initSensors();
        setDefaultMetric( new MeasuredWorldMetrics( WorldState.DEFAULT_METRIC, this, defaultIntegrationPeriod ) ) ;
    }

    protected void initSensors() {
        longRangeSensor = makeSensor( VGWorldConstants.getLongSensorMaxError(), 0, ( float ) getBoardHeight() ) ;
        mediumRangeSensor = makeSensor( VGWorldConstants.getMediumSensorMaxError(), 0, VGWorldConstants.getMediumSensorRange() ) ;
        shortRangeSensor = makeSensor( 0, 0, VGWorldConstants.getUnitSensorHeight() ) ;
        sensors.add( longRangeSensor ) ;
        sensors.add( mediumRangeSensor ) ;
        sensors.add( shortRangeSensor ) ;
        regions.add( new Region(new Rectangle2D.Double(0,0,getBoardWidth(),VGWorldConstants.getUnitRangeHeight()), REGION_OP_TEMPO ) ) ;
        regions.add( new Region(new Rectangle2D.Double(0,0,getBoardWidth(),VGWorldConstants.getUnitRangeHeight() +
                PREDICTED_ENTRY_DELAY * VGWorldConstants.getTargetMoveRate()), REGION_PREDICTED_ENTRY_REGION ) ) ;
    }

    public SensorModel makeSensor( float maxXError, float maxYError, float sensorRange ) {
        switch ( sensorModelType ) {
            case SENSOR_DEFAULT :
                return new StandardSensor( maxXError, maxYError, sensorRange ) ;
            case SENSOR_ACTIVE :
                return new ActiveSensor( maxXError, maxYError, 0, 0, sensorRange ) ;
            default :
                throw new RuntimeException( "Unrecognized sensor type " + sensorModelType ) ;
        }
    }

    public void setSensorModelType( int type ) {
        switch ( type ) {
            case SENSOR_DEFAULT :
            case SENSOR_ACTIVE :
                sensorModelType = type ;
            default :
                throw new IllegalArgumentException( " Type " + type + " not recognized." ) ;
        }
    }

    /**
     * A sensor model that generators target contacts.
     */
    static abstract class SensorModel {
        public abstract boolean isVisible( WorldState ws, TargetEntity entity ) ;

        public abstract void updateVisible( WorldState ws ) ;

        /**
         * @return A list of target contacts.  These are reference contacts and do not have the
         *  sensor error added yet.
         */
        public abstract Iterator getVisibleContacts() ;

        public abstract TargetContact makeSensedContact( TargetContact contact ) ;
    }

    public class ActiveSensor extends SensorModel{
        private float dxError, dyError ;

        /**
         * These error parameters are all the variances of the errors for each component.
         * @param xMaxError
         * @param yMaxError
         * @param dxError
         * @param dyError
         * @param yHorizon
         */
        public ActiveSensor(float xMaxError, float yMaxError, float dxError, float dyError, float yHorizon)
        {
            this.xMaxError = xMaxError ; this.yMaxError = yMaxError ;
            this.dxError = dxError ;
            this.dyError = dyError ;
            this.yHorizon = yHorizon ;
        }

        public boolean isVisible(WorldState ws, TargetEntity entity)
        {
            if ( !entity.isActive() ) {
                return false ;
            }
            return entity.getY() <= yHorizon && entity.getY() > 0  ;
        }

        /**
         * Update what is visible from this sensor.
         *
         * @param ws
         */
        public void updateVisible(WorldState ws)
        {
            for ( Iterator iter = ws.getTargets();iter.hasNext();) {
                TargetEntity entity = (TargetEntity) iter.next() ;
                if ( isVisible( ws, entity ) ) {
                    TargetContact tc = (TargetContact) contactsByIdMap.get( entity.getId() ) ;
                    if ( tc == null ) {
                        tc = new TargetContact( entity.getId(), ws.getTime(), entity.getX(),
                                entity.getY(), entity.getDx(), entity.getDy(), 0, 0, entity.getStrength() ) ;
                        contactsByIdMap.put( entity.getId(), tc ) ;
                    }
                    else {
                        tc.setPosition( entity.getX() + tc.getXError(),
                                entity.getY() + tc.getYError() );
                    }
                }
                else { // We can't see this anymore, remove it from the contacts list
                    TargetContact tc = (TargetContact) contactsByIdMap.get( entity.getId() ) ;
                    if ( tc != null ) {
                        contactsByIdMap.remove( entity.getId() ) ;
                    }
                }
            }

            //
            // Remove all non-visible targets
            //
            for (Iterator iterator = contactsByIdMap.values().iterator(); iterator.hasNext();)
            {
                TargetContact targetContact = (TargetContact) iterator.next();
                TargetEntity entity = (TargetEntity) ws.getEntity( targetContact.getId() ) ;
                if ( entity == null || entity.isActive() == false || !isVisible( ws, entity ) ) {
                    iterator.remove();
                }
            }

        }

        /**
         * Make a sensor contact from the current reference contact list.
         * A Gaussian error is applied to all velocity and position estimates.
         *
         * @return
         */

        public TargetContact makeSensedContact(TargetContact entity)
        {
            return new TargetContact( entity.getId(), getTime(),
                    entity.getX() + ( float ) random.nextGaussian() * xMaxError ,
                    entity.getY() + ( float ) random.nextGaussian() * yMaxError ,
                    entity.getDx() + ( float ) random.nextGaussian() * dxError,
                    entity.getDy() + ( float ) random.nextGaussian() * dyError ,
                    xMaxError,
                    yMaxError,
                    entity.getStrength() );
        }

        public Iterator getVisibleContacts()
        {
            return contactsByIdMap.values().iterator() ;
        }

        float yHorizon ;
        float xMaxError, yMaxError ;
        protected HashMap contactsByIdMap = new HashMap();
    }

    public class StandardSensor extends SensorModel {

        public StandardSensor(float xMaxError, float yMaxError, float yHorizon)
        {
            this.xMaxError = xMaxError;
            this.yMaxError = yMaxError;
            this.yHorizon = yHorizon;
        }

        public StandardSensor(float xMaxError, float yMaxError, float yHorizon, Shape s)
        {
            this.xMaxError = xMaxError;
            this.yMaxError = yMaxError;
            this.yHorizon = yHorizon;
            this.s = s;
        }

        public Shape getShape()
        {
            return s;
        }

        public void setShape(Shape s)
        {
            this.s = s;
        }

        public Iterator getVisibleContacts()
        {
            return contactsByIdMap.values().iterator() ;
        }

        public boolean isVisible(WorldState ws, TargetEntity entity)
        {
            if ( !entity.isActive() ) {
                return false ;
            }
            return entity.getY() <= yHorizon && entity.getY() > 0  ;
        }

        public void updateVisible(WorldState ws)
        {
            for ( Iterator iter = ws.getTargets();iter.hasNext();) {
                TargetEntity entity = (TargetEntity) iter.next() ;
                if ( isVisible( ws, entity ) ) {
                    TargetContact tc = (TargetContact) contactsByIdMap.get( entity.getId() ) ;
                    if ( tc == null ) {
                        tc = makeReferenceContact( entity ) ;
                        contactsByIdMap.put( entity.getId(), tc ) ;
                    }
                    else {
                        tc.setPosition( entity.getX() + tc.getXError(),
                                entity.getY() + tc.getYError() );
                    }
                }
                else { // We can't see this anymore, remove it from the contacts list
                    TargetContact tc = (TargetContact) contactsByIdMap.get( entity.getId() ) ;
                    if ( tc != null ) {
                        contactsByIdMap.remove( entity.getId() ) ;
                    }
                }
            }

            //
            // Remove all non-visible targets
            //
            for (Iterator iterator = contactsByIdMap.values().iterator(); iterator.hasNext();)
            {
                TargetContact targetContact = (TargetContact) iterator.next();
                TargetEntity entity = (TargetEntity) ws.getEntity( targetContact.getId() ) ;
                if ( entity == null || entity.isActive() == false || !isVisible( ws, entity ) ) {
                    //System.out.println("Removing contact " + entity.getId() + " from sensor with range " + yHorizon );
                    iterator.remove();
                }
            }
        }

        public TargetContact makeReferenceContact( TargetEntity entity ) {
            return new TargetContact( entity.getId(), getTime(), entity.getX(),
                    entity.getY(), entity.getDx(), entity.getDy(),
                    random.nextFloat() * xMaxError,
                    random.nextFloat() * yMaxError,
                    entity.getStrength() ) ;
        }

        public TargetContact makeSensedContact( TargetContact contact ) {
            return new TargetContact( contact.getId(), getTime(), contact.getX() + contact.getXError() ,
                    contact.getY() + contact.getYError(), contact.getDx(), contact.getDy(),
                    xMaxError, yMaxError, contact.getStrength() ) ;
        }

        protected HashMap contactsByIdMap = new HashMap();

        Shape s ;
        float yHorizon ;
        float xMaxError, yMaxError ;
    }

    public Iterator getInactiveTargets()
    {
        return (Iterator) inactiveTargets;
    }

    /**
     * Create a filtered version of this world state.
     *
     * @param sensorType SENSOR_PERFECT, SENSOR_LONG, SENSOR_MEDIUM and SENSOR_SHORT.
     * @param cloneUnits
     * @param cloneSupplyEntities
     * @param sensorShape
     * @return
     */
    public WorldStateModel filter( int sensorType, boolean cloneUnits, boolean cloneSupplyEntities, Shape sensorShape ) {
        SensorModel sensor = null ;

        // Substitute contact information back if necessary.
        switch ( sensorType ) {
            case WorldStateModel.SENSOR_PERFECT :
                return new WorldStateModel( this, true, cloneUnits, cloneSupplyEntities ) ;
            case WorldStateModel.SENSOR_LONG :
                sensor = longRangeSensor ;
                break ;
            case WorldStateModel.SENSOR_MEDIUM :
                sensor = mediumRangeSensor ;
                break ;
            case WorldStateModel.SENSOR_SHORT :
                sensor = shortRangeSensor ;
                break ;
            default :
                throw new IllegalArgumentException( "Sensor type " + sensorType + " does not exist." ) ;
        }

        WorldStateModel wsm = new WorldStateModel( this, false, cloneUnits, cloneSupplyEntities ) ;
        if ( sensor != null ) {
            Iterator contacts = sensor.getVisibleContacts() ;
            while (contacts.hasNext())
            {
                TargetContact targetContact = (TargetContact) contacts.next();
                wsm.addEntity( sensor.makeSensedContact( targetContact ) );
            }
        }

        return wsm ;
    }

    public void moveInactiveTargets() {
        for (Iterator iterator = targets.iterator(); iterator.hasNext();)
        {
            TargetEntity targetEntity = (TargetEntity) iterator.next();
            if ( !targetEntity.isActive() ) {
                iterator.remove();
                inactiveTargets.add( targetEntity ) ;
            }
        }
    }

    public void updateWorldState()
    {
        super.updateWorldState();

        moveInactiveTargets();

        // Update trajectories.
        updateTrajectories() ;

        // Update sensors on every cycle.  This is inefficient but okay for now.
        updateSensors();
    }

    private void updateTrajectories()
    {
        for (int i = 0; i < targets.size(); i++) {
            TargetEntity targetEntity = (TargetEntity)targets.get(i);
            EntityInfo info = getEntityInfo( targetEntity.getId() ) ;
            EntityHistory entityHistory = info.getHistory() ;
            if ( info.getHistory() == null ) {
                info.setHistory( entityHistory = new EntityHistory() );
            }
            entityHistory.addTrajectoryRecord( getTime(), targetEntity.getX(), targetEntity.getY(),
                    targetEntity.getDx(), targetEntity.getDy(), targetEntity.getStrength() );
        }
    }

    public void updateSensors()
    {
        for (int i = 0; i < sensors.size(); i++) {
            StandardSensor standardSensor = (StandardSensor) sensors.get(i);
            standardSensor.updateVisible( this ) ;
        }
    }

    /**
     *
     * @param scoringZone The name of the associated scoring zone.  This is useful primarily for testing purposes.
     * @param integrationPeriod The number of ms to integrate over in recording the measurements.
     * @param zs
     */
    public void setScoringZoneSchedule( String scoringZone, Plan zs, int integrationPeriod ) {
        MeasuredWorldMetrics metrics;
        if ( scoringZone == null ) {
            throw new RuntimeException( "ScoringZone must be non null." ) ;
        }

        if ( zoneNameToMetricsMap == null ) {
            zoneNameToMetricsMap = new HashMap() ;
        }
        // Create the scoring zone plan
        if ( ( metrics = (MeasuredWorldMetrics) zoneNameToMetricsMap.get(scoringZone) ) == null ) {
            zoneNameToMetricsMap.put( scoringZone, metrics = new MeasuredWorldMetrics( scoringZone, this, integrationPeriod ) ) ;
        }

        metrics.setZoneSchedule( zs );
        addEventListener( metrics );
    }

    public boolean isModel()
    {
        return false ;
    }

    /**
     * A set of entry sets for scoring zones by
     * @return
     */
    public Set getScoringZones() {
        return zoneNameToMetricsMap.entrySet() ;
    }

    public void removeScoringZone( String s ) {
        zoneNameToMetricsMap.remove( s ) ;
    }

    public MeasuredWorldMetrics getScoringZone( String name ) {
        return (MeasuredWorldMetrics) zoneNameToMetricsMap.get( name ) ;
    }

    private transient HashMap zoneNameToMetricsMap ;

    private ArrayList inactiveTargets = new ArrayList() ;

    private Random random = new Random( 0xcafebabe ) ;

    private ArrayList sensors = new ArrayList() ;

}
