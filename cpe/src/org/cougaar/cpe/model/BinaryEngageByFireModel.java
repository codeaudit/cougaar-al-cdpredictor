package org.cougaar.cpe.model;

import java.util.Random;

/**
 * A simple model for engaging by fire using a binary R.V.  All values are based on
 * a single delta T, so they are not delta T independent!
 */
public class BinaryEngageByFireModel extends EngageByFireModel {

    public BinaryEngageByFireModel( double probHit, double multiProbHit, double damage, int seed ) {
        this( seed ) ;
        this.probHit = probHit;
        this.probHit2 = multiProbHit ;
        this.damage = damage ;
    }

    public BinaryEngageByFireModel( int seed ) {
        random = new Random( seed ) ;
    }

    public double getMaxAttrition() {
        return damage ;
    }

    public int getLastAmmoConsumption() {
        return ammoConsumption ;
    }

    public double getMeanAmmoConsumption() {
        return ammoConsumption;
    }

    /**
     * Get the average damage/unit time.
     * @return
     */
    public double getMeanAttrition( TargetEntity entity ) {
        return probHit * damage ;
    }

    public double getMinAttrition() {
        return 0 ;
    }

    public void nextAttritionValue(TargetEntity entity, EngageByFireResult result, double start, double end, int engageCount) {
        double attritValue = 0 ;
        int totalAmmoConsumption = 0 ;
        double prob = probHit ;
        if ( engageCount > 0 ) {
            prob = probHit2 ;
        }

        double engagementTime ;
        if ( lastEngagementTime < start - engagementCycle ) {
            engagementTime = start ;
        }
        else {
            engagementTime = lastEngagementTime + engagementCycle ;
        }

        while ( engagementTime < end ) {
            lastEngagementTime = engagementTime ;
            if ( random.nextDouble() < prob )  {
               attritValue += damage ;
            }
            totalAmmoConsumption += ammoConsumption ;
            engagementTime += engagementCycle ;
        }
        result.targetId = entity.getId() ;
        result.setAmmoConsumed( totalAmmoConsumption );
        result.setAttritValue( attritValue );
    }

    public void estimateAttritionValue(TargetEntity entity, EngageByFireResult er, double start, double end, int engageCount) {
        double prob = probHit ;
        if ( engageCount > 0 ) {
            prob = probHit2 ;
        }
        double numEngagements = ( end - start ) / engagementCycle ;
        er.targetId = entity.getId() ;
        er.setAttritValue( numEngagements * prob * damage ) ;
        er.setAmmoConsumed( ( int ) ( numEngagements * ammoConsumption ) ); ;
    }

    public void estimateAttritionValue( TargetEntity entity, EngageByFireResult er, double start, double end ) {
        double numEngagements = ( end - start ) / engagementCycle ;
        er.targetId = entity.getId() ;
        er.setAttritValue( numEngagements * probHit * damage ) ;
        er.setAmmoConsumed( ( int ) ( numEngagements * ammoConsumption ) );
    }

    /**
     * Compute the next (random) attrition value for an interval of time. This is
     * only used by the WorldState and not any WorldState models, since the models
     * don't have any access to this.
     */
    public void nextAttritionValue( TargetEntity entity, EngageByFireResult
            result, double start, double end ) {
        double attritValue = 0 ;
        int totalAmmoConsumption = 0 ;

        double engagementTime ;
        if ( lastEngagementTime < start - engagementCycle ) {
            engagementTime = start ;
        }
        else {
            engagementTime = lastEngagementTime + engagementCycle ;
        }

        while ( engagementTime < end ) {
            lastEngagementTime = engagementTime ;
            if ( random.nextDouble() < probHit )  {
               attritValue += damage ;
            }
            totalAmmoConsumption += ammoConsumption ;
            engagementTime += engagementCycle ;
        }
        result.targetId = entity.getId() ;
        result.setAmmoConsumed( totalAmmoConsumption );
        result.setAttritValue( attritValue );
    }

    Random random ;
    int ammoConsumption = 1 ;

    /**
     * Damage per engagement.
     */
    double damage = 4 ;

    /**
     * Probability of hit per engagement. Default value is 0.66
     */
    double probHit = 0.66 ;

    /**
     * Probability of hit when there is more than one engagement cycle. Default value is 0.8
     */
    double probHit2 = 0.8 ;

    double lastEngagementTime = Double.NEGATIVE_INFINITY ;

    /**
     * Five second intervals for engagement.
     */
    double engagementCycle = 5 ;
}
