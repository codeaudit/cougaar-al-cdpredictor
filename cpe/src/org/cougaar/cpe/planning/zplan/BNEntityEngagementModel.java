package org.cougaar.cpe.planning.zplan;

import org.cougaar.cpe.model.*;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.io.Serializable;

/**
 * User: wpeng
 * Date: Apr 11, 2004
 * Time: 3:24:16 PM
 */
public class BNEntityEngagementModel implements Serializable
{

    public EngageByFireResult engage( ZoneWorld zw, BNAggregate agg ) {
        //
        // Find lowest N targets and deduct damage,
        //
        Rectangle2D r2d = new Rectangle2D.Float(zw.getZoneLower( (IndexedZone) agg.getCurrentZone() ), 0,
                zw.getZoneSize( (IndexedZone) agg.getCurrentZone() ), (float) VGWorldConstants.getUnitRangeHeight()  ) ;
        ArrayList results = new ArrayList() ;
        zw.getTargetsInRange( r2d, results ) ;

        for (int i=0;i<results.size();i++) {
            results.set( i, zw.getEntity( (String) results.get(i) ) ) ;
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

        // Find a (somewhat) arbitrary assignment between subordinate entities and targets based on the N lowest
        // targets.
        EngageByFireResult er = new EngageByFireResult() ;

        // This is an (over) estimate of damage and assumes that the targets can always be hit
        // by entities.  Also, the assignment is simplistic and assumes a "round robin" type of interaction,
        // i.e. the lowest target is covered with highest priority, then the second, and if there are any
        // unassigned entities, they are

        if ( results.size() > 0 ) {
            for (int i=0;i<Math.max( results.size(), agg.getNumSubEntities() ) ;i++) {
                TargetEntity targetEntity = (TargetEntity) results.get( i %  results.size() ) ;
                String aggId = agg.getSubEntityName( i % agg.getNumSubEntities() ) ;
                EntityInfo info = zw.getEntityInfo( aggId ) ;
                info.getModel().estimateAttritionValue( targetEntity, er, zw.getTime(), zw.getTime() + zw.getDeltaT() );
                targetEntity.setStrength( targetEntity.getStrength() - er.getAttritValue() );

                agg.setTotalAmmo( agg.getTotalAmmo() - er.getAmmoConsumed() ) ;
            }
        }

        return er ;
        //float fuelConsumption = (float) (agg.getNumSubEntities() * VGWorldConstants.UNIT_FUEL_CONSUMPTION_RATE) ;
        // Within the same zone, the fuel consumption is given by the dispersion of the
        // targets within the field, i.e the average distance between the n lowest targets.

    }

}
