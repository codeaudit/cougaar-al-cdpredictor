package org.cougaar.cpe.model;

import java.io.Serializable;

/**
 * Abstract class for all engagement between entities.
 */
public abstract class EngageByFireModel implements Serializable {

    /**
     * Get the average attrition per second.
     * @param entity
     * @return
     */
    //public abstract double getMeanAttrition( TargetEntity entity );

    //public abstract double getMaxAttrition();

    //public abstract double getMinAttrition();

    public abstract void estimateAttritionValue( TargetEntity entity, EngageByFireResult result, double start, double end );

    public abstract void estimateAttritionValue( TargetEntity entity, EngageByFireResult result, double start, double end, int engageCount );

    /**
     * Randomly compute the next attrition value.
     * @param entity
     * @param result
     */
    public abstract void nextAttritionValue( TargetEntity entity, EngageByFireResult result, double start, double end );

    public abstract void nextAttritionValue( TargetEntity entity, EngageByFireResult result, double start, double end, int engageCount );

    //public abstract int getLastAmmoConsumption() ;

    /**
     * Get the mean ammo consumption per second.
     * @return
     */
    //public abstract double getMeanAmmoConsumption() ;
}
