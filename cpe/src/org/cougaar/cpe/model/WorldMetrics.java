package org.cougaar.cpe.model;

import org.cougaar.tools.techspecs.qos.MeasurementPoint;
import org.cougaar.tools.techspecs.qos.SyntheticMeasurementPoint;
import org.cougaar.tools.techspecs.qos.SyntheticTimestampedMeasurement;

/**
 * Measure scoring rate.
 */
public class WorldMetrics
{
    MeasurementPoint fuelShortFalls = new SyntheticMeasurementPoint( "Fuel Shortfalls", Float.class ) ;
    MeasurementPoint ammoShortFalls = new SyntheticMeasurementPoint( "Ammo Shortfalls", Float.class ) ;
    MeasurementPoint attrition = new SyntheticMeasurementPoint( "Attrition", Double.class ) ;
    MeasurementPoint kills = new SyntheticMeasurementPoint( "Kills", Integer.class ) ;
    MeasurementPoint penalties = new SyntheticMeasurementPoint( "Penalties", Integer.class ) ;
    MeasurementPoint violations = new SyntheticMeasurementPoint( "Violations", Integer.class ) ;
    MeasurementPoint score = new SyntheticMeasurementPoint( "Score", Float.class ) ;
    MeasurementPoint scoringRate = new SyntheticMeasurementPoint( "ScoringRate", Float.class ) ;
    MeasurementPoint entryRate = new SyntheticMeasurementPoint( "EntryRate", Integer.class ) ;

    public WorldMetrics( int integrationPeriod )
    {
        this.integrationPeriod = integrationPeriod ;
        fuelShortFalls.setMaximumHistorySize( Integer.MAX_VALUE );
        ammoShortFalls.setMaximumHistorySize( Integer.MAX_VALUE );
        attrition.setMaximumHistorySize( Integer.MAX_VALUE );
        kills.setMaximumHistorySize( Integer.MAX_VALUE);
        penalties.setMaximumHistorySize( Integer.MAX_VALUE );
        violations.setMaximumHistorySize( Integer.MAX_VALUE );
        score.setMaximumHistorySize( Integer.MAX_VALUE );
    }

    public MeasurementPoint getAmmoShortFalls()
    {
        return ammoShortFalls;
    }

    public MeasurementPoint getAttrition()
    {
        return attrition;
    }

    public MeasurementPoint getFuelShortFalls()
    {
        return fuelShortFalls;
    }

    public MeasurementPoint getKills()
    {
        return kills;
    }

    public MeasurementPoint getPenalties()
    {
        return penalties;
    }

    public MeasurementPoint getScore()
    {
        return score;
    }

    public MeasurementPoint getViolations()
    {
        return violations;
    }

    protected void initTime( long time ) {
        if ( lastIntegrationTime == -1) {
            lastIntegrationTime = time ;
        }
        lastTime = time ;
    }

    public void processViolationEvent(WorldState.ViolationEvent ve )
    {
        initTime( ve.getTime() ) ;
        accumViolations ++ ;
    }

    public void processFireEvent( WorldState.EngageByFireEvent ef )
    {
        initTime( ef.getTime() );
        EngageByFireResult er = ef.getEr() ;
        accumAttrition += er.getAttritValue() ;
    }

    public void processTimeAdvanceEvent( WorldState.TimeAdvanceEvent ev ) {
        if ( ev.getNewTime() - lastIntegrationTime >= integrationPeriod ) {
            lastIntegrationTime = ev.getNewTime() ;

            // Integrate!
            attrition.addMeasurement( new SyntheticTimestampedMeasurement( null, null, null, ev.getOldTime(), new Double( accumAttrition )));
        }
        lastTime = ev.getNewTime() ;
    }

    long lastIntegrationTime = -1 ;
    long lastTime = -1 ;

    /**
     * Time in ms. for each measurement integration.
     */
    int integrationPeriod = 40000 ;

    protected int accumViolations ;
    protected int accumPenalties ;
    protected int accumKills ;
    protected double accumAttrition ;
}
