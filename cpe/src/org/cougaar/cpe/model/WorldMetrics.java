package org.cougaar.cpe.model;

import org.cougaar.tools.techspecs.qos.MeasurementPoint;
import org.cougaar.tools.techspecs.qos.TimePeriodMeasurementPoint;
import org.cougaar.tools.techspecs.qos.TimePeriodMeasurement;
import org.cougaar.cpe.model.events.*;
import org.cougaar.cpe.planning.zplan.ZoneTask;
import org.cougaar.cpe.planning.zplan.ZoneWorld;

import java.util.HashMap;

/**
 * This is a general way to measure score.
 */
public class WorldMetrics implements CPEEventListener
{
    private WorldState parent;

    /**
     * Associate the metric with a zone schedule.
     */
    private Plan zoneSchedule ;
    private String name;

    public WorldMetrics( String name, WorldState state, int integrationPeriod )
    {
        this.name = name ;
        this.parent = state ;
        this.integrationPeriod = integrationPeriod ;
        // Keep all data!
    }

    public String getName()
    {
        return name;
    }

    public Plan getZoneSchedule() {
        return zoneSchedule;
    }

    public void setZoneSchedule( Plan zoneSchedule) {
        this.zoneSchedule = zoneSchedule;
    }

    public void notify(CPEEvent e) {
        //System.out.println("WorldMetrics:: Processing " + e );
        if ( e instanceof TimeAdvanceEvent ) {
            processTimeAdvanceEvent( ( TimeAdvanceEvent ) e );
        }
        else if ( e instanceof ViolationEvent ) {
            processViolationEvent( ( ViolationEvent ) e );
        }
        else if ( e instanceof RegionEvent ) {
            processRegionEvent( (RegionEvent) e );
        }
        else if ( e instanceof EngageByFireEvent ) {
            processEngageByFireEvent( ( EngageByFireEvent ) e );
        }
        else if ( e instanceof PenaltyEvent ) {
            processPenaltyEvent( ( PenaltyEvent ) e );
        }
        else if ( e instanceof KillEvent ) {
            processKillEvent( ( KillEvent ) e );
        }
        else if ( e instanceof FuelConsumptionEvent ) {
            processFuelConsumptionEvent( ( FuelConsumptionEvent ) e ) ;
        }
    }

    protected void processFuelConsumptionEvent( FuelConsumptionEvent e)
    {
        accumFuelConsumption += e.getAmount() ;
        totalFuelConsumption += e.getAmount() ;
    }

    protected void initTime( long time ) {
        if ( lastIntegrationTime == -1) {
            lastIntegrationTime = time ;
        }
        lastTime = time ;
    }

    private boolean checkInZone(long time, float x, float y)
    {
        ZoneTask z = (ZoneTask) zoneSchedule.getNearestTaskForTime( time ) ;
        Interval interval = ZoneWorld.interpolateIntervals( z, time ) ;

        if ( interval.isInInterval(x) ) {
            return true ;
        }
        return false ;
    }

    public void processViolationEvent(ViolationEvent ve )
    {
        if ( zoneSchedule != null ) {
           if ( !checkInZone( ve.getTime(), ve.getxTarget(), ve.getyTarget()) ) {
               return ;
           }
        }
        initTime( ve.getTime() ) ;
        accumViolations ++ ;
        totalViolations ++ ;
    }

    public void processPenaltyEvent(PenaltyEvent pe ) {
        if ( zoneSchedule != null ) {
           if ( !checkInZone( pe.getTime(), pe.getxTarget(), pe.getyTarget()) ) {
               return ;
           }
        }

        initTime( pe.getTime() );
        accumPenalties ++ ;
        totalPenalties ++ ;
    }

    public void processEngageByFireEvent( EngageByFireEvent ef )
    {
        if ( zoneSchedule != null ) {
           if ( !checkInZone( ef.getTime(), ef.getxTarget(), ef.getyTarget()) ) {
               return ;
           }
        }

        initTime( ef.getTime() );
        EngageByFireResult er = ef.getEr() ;
        accumAttrition += er.getAttritValue() ;
        totalAttrition += er.getAttritValue() ;
    }

    public void processKillEvent( KillEvent ke ) {
        initTime( ke.getTime() );

        if ( zoneSchedule != null ) {
           if ( !checkInZone( ke.getTime(), ke.getTargetX(), ke.getTargetY()) ) {
               return ;
           }
        }

        accumKills ++ ;
        totalKills ++ ;
    }

    public void processTimeAdvanceEvent( TimeAdvanceEvent ev ) {
        if ( ev.getNewTime() - lastIntegrationTime >= integrationPeriod ) {
            lastIntegrationTime = ev.getNewTime() ;
            accumAttrition = 0 ;
            accumPenalties = 0 ;
            accumKills = 0 ;
            accumViolations = 0 ;
            accumEntries = 0 ;
            accumFuelConsumption = 0 ;
        }
        lastTime = ev.getNewTime() ;
    }

    public void processRegionEvent( RegionEvent ev ) {

        if ( zoneSchedule != null ) {
           if ( !checkInZone( ev.getTime(), ev.getNewX(), ev.getNewY() ) ) {
               return ;
           }
        }

        if ( ev.getRegion().getRegionName().equals(ReferenceWorldState.REGION_OP_TEMPO)
                && ev.getType() == RegionEvent.EVENT_REGION_ENTRY )
        {
           accumEntries++ ;
        }
    }

    long lastIntegrationTime = -1 ;
    long lastTime = -1 ;

    /**
     * Time in ms. for each measurement integration.
     */
    int integrationPeriod = 40000 ;

    /**
     * entryHeight for op tempo measurement.
     */
    protected double entryHeight ;

    protected int accumViolations ;
    protected int accumPenalties ;
    protected int accumKills ;
    protected double accumAttrition ;
    protected int accumEntries ;
    protected double accumFuelConsumption ;

    protected int totalViolations ;
    protected int totalPenalties ;
    protected int totalKills ;
    protected double totalAttrition ;
    protected double totalFuelConsumption ;

}
