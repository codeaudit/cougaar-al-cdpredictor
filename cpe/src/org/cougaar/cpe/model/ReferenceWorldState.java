package org.cougaar.cpe.model;

import java.awt.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Random;

public class ReferenceWorldState extends WorldState
{
    private StandardSensor longRangeSensor;
    private StandardSensor mediumRangeSensor;
    private StandardSensor shortRangeSensor ;

    public ReferenceWorldState(double boardWidth, double boardHeight, double penaltyHeight, double recoveryLine, double deltaT)
    {
        super(boardWidth, boardHeight, penaltyHeight, recoveryLine, deltaT);
        longRangeSensor = new StandardSensor( VGWorldConstants.getLongSensorMaxError(), 0, ( float ) boardHeight ) ;
        mediumRangeSensor = new StandardSensor( VGWorldConstants.getMediumSensorMaxError(), 0, VGWorldConstants.getMediumSensorRange() ) ;
        shortRangeSensor = new StandardSensor( 0, 0, VGWorldConstants.getUnitSensorHeight() ) ;
        sensors.add( longRangeSensor ) ;
        sensors.add( mediumRangeSensor ) ;
        sensors.add( shortRangeSensor ) ;
    }

    static abstract class SensorModel {
        public abstract boolean isVisible( WorldState ws, TargetEntity entity ) ;

        public abstract void updateVisible( WorldState ws ) ;

        public abstract Iterator getVisibleContacts() ;

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
        StandardSensor sensor = null ;

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

        // Update sensors on every cycle.  This is inefficient but okay for now.
        updateSensors();
    }

    public void updateSensors()
    {
        for (int i = 0; i < sensors.size(); i++) {
            StandardSensor standardSensor = (StandardSensor) sensors.get(i);
            standardSensor.updateVisible( this ) ;
        }
    }

    private ArrayList inactiveTargets = new ArrayList() ;

    private Random random = new Random( 0xcafebabe ) ;

    private ArrayList sensors = new ArrayList() ;

}
