package org.cougaar.cpe.planning.zplan;

import org.cougaar.cpe.model.*;

/**
 * This is an aggregate unit entity. It represents the aggregate of a set of underlying units.
 *
 */
public abstract class Aggregate extends CPEObject
{
    public static final int TYPE_PLT = 0 ;
    public static final int TYPE_CPY = 1 ;
    public static final int TYPE_BN = 2 ;
    public static final int TYPE_BDE = 3 ;
    public static final int TYPE_SUPPLY = 4 ;

    /**
     * @param id
     * @param entities A list of the entities which this entity represents. Note that the entities must be
     * acceptable base don the CPEObject type.
     *
     */
    public Aggregate(int type, String id, String[] entities)
    {
        super( id ) ;
        this.aggregateType = type ;
        this.entities = (String[]) entities.clone();
    }

    public abstract Object clone() ;

    public abstract void update( ZoneWorld zw ) ;

    public int getAggregateType()
    {
        return aggregateType;
    }

    public String getSubEntityName( int i )
    {
        return entities[i];
    }

    public int getNumSubEntities() {
        return entities.length ;
    }

    public ZoneSchedule getZoneSchedule()
    {
        return zs;
    }

    public void setZoneSchedule( ZoneSchedule zs ) {
        this.zs = zs ;
    }

    /**
     * The entities aggregated.
     */
    String[] entities ;

    ZoneSchedule zs ;
    int aggregateType ;
}
