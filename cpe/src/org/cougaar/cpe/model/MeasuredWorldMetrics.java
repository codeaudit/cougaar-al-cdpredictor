package org.cougaar.cpe.model;

import org.cougaar.tools.techspecs.qos.MeasurementPoint;
import org.cougaar.tools.techspecs.qos.TimePeriodMeasurementPoint;
import org.cougaar.tools.techspecs.qos.TimePeriodMeasurement;
import org.cougaar.cpe.model.events.TimeAdvanceEvent;

public class MeasuredWorldMetrics extends WorldMetrics
{
    MeasurementPoint fuelShortFalls ;
    MeasurementPoint ammoShortFalls  ;
    MeasurementPoint attrition  ;
    MeasurementPoint kills  ;
    MeasurementPoint penalties ;
    MeasurementPoint violations  ;
    MeasurementPoint score  ;
    MeasurementPoint scoringRate ;
    MeasurementPoint entryRate ;

    public MeasuredWorldMetrics(String name, WorldState state, int integrationPeriod)
    {
        super(name, state, integrationPeriod);
        fuelShortFalls = new TimePeriodMeasurementPoint( name + ".FuelShortfalls", Float.class ) ;
        ammoShortFalls = new TimePeriodMeasurementPoint( name + ".AmmoShortfalls", Float.class ) ;
        attrition = new TimePeriodMeasurementPoint( name + ".Attrition", Double.class ) ;
        kills = new TimePeriodMeasurementPoint( name + ".Kills", Integer.class ) ;
        penalties = new TimePeriodMeasurementPoint( name + ".Penalties", Integer.class ) ;
        violations = new TimePeriodMeasurementPoint( name + ".Violations", Integer.class ) ;
        score = new TimePeriodMeasurementPoint( name + ".Score", Float.class ) ;
        scoringRate = new TimePeriodMeasurementPoint( name + ".ScoringRate", Float.class ) ;
        entryRate = new TimePeriodMeasurementPoint( name + ".EntryRate", Integer.class ) ;

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

    public MeasurementPoint getEntryRate()
    {
        return entryRate;
    }

    public synchronized void processTimeAdvanceEvent(TimeAdvanceEvent ev)
    {
        //System.out.println("MeasuredWorldMetrics:: Advancing time " + ev);
        if ( ev.getNewTime() - lastIntegrationTime >= integrationPeriod ) {
            lastIntegrationTime = ev.getNewTime() ;
            attrition.addMeasurement( new TimePeriodMeasurement( ev.getOldTime(), ev.getNewTime(), new Double( accumAttrition )));
            penalties.addMeasurement( new TimePeriodMeasurement( ev.getOldTime(), ev.getNewTime(), new Integer( accumPenalties )));
            kills.addMeasurement( new TimePeriodMeasurement( ev.getOldTime(), ev.getNewTime(), new Integer( accumKills ) ) );
            violations.addMeasurement( new TimePeriodMeasurement( ev.getOldTime(), ev.getNewTime(), new Integer( accumViolations ) ) );
            entryRate.addMeasurement( new TimePeriodMeasurement( ev.getOldTime(), ev.getNewTime(), new Integer( accumEntries )));
            accumAttrition = 0 ;
            accumPenalties = 0 ;
            accumKills = 0 ;
            accumViolations = 0 ;
            accumEntries = 0 ;
        }
        lastTime = ev.getNewTime() ;
    }

}
