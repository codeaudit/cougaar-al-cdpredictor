package org.cougaar.cpe.model;

import org.cougaar.tools.techspecs.qos.MeasurementPoint;
import org.cougaar.tools.techspecs.qos.TimePeriodMeasurementPoint;
import org.cougaar.tools.techspecs.qos.TimePeriodMeasurement;
import org.cougaar.cpe.model.events.TimeAdvanceEvent;
import org.cougaar.cpe.model.events.FuelConsumptionEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Collection;

public class MeasuredWorldMetrics extends WorldMetrics
{
    MeasurementPoint fuelShortFalls ;
    MeasurementPoint ammoShortFalls  ;
    TimePeriodMeasurementPoint fuelConsumption ;
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
        fuelConsumption = new TimePeriodMeasurementPoint( name + ".FuelConsumption", Double.class ) ;

        fuelShortFalls.setMaximumHistorySize( Integer.MAX_VALUE );
        ammoShortFalls.setMaximumHistorySize( Integer.MAX_VALUE );
        attrition.setMaximumHistorySize( Integer.MAX_VALUE );
        kills.setMaximumHistorySize( Integer.MAX_VALUE);
        penalties.setMaximumHistorySize( Integer.MAX_VALUE );
        violations.setMaximumHistorySize( Integer.MAX_VALUE );
        score.setMaximumHistorySize( Integer.MAX_VALUE );
        fuelConsumption.setMaximumHistorySize( Integer.MAX_VALUE );
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

    public TimePeriodMeasurementPoint getFuelConsumption()
    {
        return fuelConsumption;
    }

    public Collection getFuelConsumptionByUnit()
    {
        return fuelConsumptionByUnit.values() ;
    }

    protected void processFuelConsumptionEvent(FuelConsumptionEvent e)
    {
        super.processFuelConsumptionEvent(e);
        String unitId = e.getUnitId() ;

        MeasurementPoint unitFuelConsumption = (MeasurementPoint) fuelConsumptionByUnit.get(unitId) ;
        if ( unitFuelConsumption == null ) {
            unitFuelConsumption = new TimePeriodMeasurementPoint( getName() + ".FuelConsumption." + unitId, Double.TYPE ) ;
            fuelConsumptionByUnit.put( unitId, unitFuelConsumption ) ;
        }

        Double value = (Double) accumFuelConsumptionByUnit.get( unitId ) ;
        if ( value == null ) {
            value = new Double( e.getAmount() );
        }
        else {
            value = new Double( e.getAmount() + value.doubleValue() ) ;
        }
        accumFuelConsumptionByUnit.put( unitId, value ) ;
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
            fuelConsumption.addMeasurement( new TimePeriodMeasurement( ev.getOldTime(), ev.getNewTime(), new Double( accumFuelConsumption ) ) );
            for (Iterator iterator = fuelConsumptionByUnit.entrySet().iterator(); iterator.hasNext();)
            {
                Map.Entry entry = (Map.Entry) iterator.next() ;
                TimePeriodMeasurementPoint timePeriodMeasurementPoint = (TimePeriodMeasurementPoint) entry.getValue() ;
                Double value = (Double) accumFuelConsumptionByUnit.get( entry.getKey() ) ;
                timePeriodMeasurementPoint.addMeasurement(
                        new TimePeriodMeasurement( ev.getOldTime(), ev.getNewTime(), value ) );
            }

            accumFuelConsumptionByUnit.clear();
            accumFuelConsumption = 0 ;
            accumAttrition = 0 ;
            accumPenalties = 0 ;
            accumKills = 0 ;
            accumViolations = 0 ;
            accumEntries = 0 ;
        }
        lastTime = ev.getNewTime() ;
    }


    protected HashMap fuelConsumptionByUnit = new HashMap() ;
    protected HashMap accumFuelConsumptionByUnit = new HashMap() ;
}
