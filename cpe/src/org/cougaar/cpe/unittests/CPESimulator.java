package org.cougaar.cpe.unittests;

import org.cougaar.cpe.model.*;
import org.cougaar.cpe.mplan.ManueverPlanner;
import org.cougaar.cpe.splan.SPlanner;
import org.cougaar.cpe.planning.zplan.BNAggregate;
import org.cougaar.cpe.planning.zplan.ZoneWorld;
import org.cougaar.cpe.planning.zplan.ZonePlanner;
import org.cougaar.cpe.planning.zplan.ZoneTask;

import java.util.*;

public class CPESimulator
{
    protected long totalPlanningTime = 0 ;
    protected int planningCycles = 0 ;

    protected ZoneWorld zoneWorld ;
    protected float boardWidth = 36 ;
    protected float zoneGridSize = 2 ;
    protected float boardHeight = 28 ;
    protected float penaltyHeight = 4 ;
    protected double deltaT = 5;
    protected double recoveryHeight = - VGWorldConstants.getSupplyVehicleMovementRate() * deltaT * 4 ;

    protected int numTimeDeltasPerTask = 4 ;

    /**
     * Zone planning should be multiples of numTimeDeltasPerTask.
     */
    protected int numTimeDeltasPerZoneSchedule = numTimeDeltasPerTask * 3 ;

    /**
     * Number of deltaTs per plan element.
     */
    protected int numIncrementsPerPlan = 2 ;
    protected int searchDepth = 7 ;
    protected int maxBranchFactor = 60 ;

    protected int zoneSearchDepth = 7 ;
    protected int zoneBranchFactor = 60 ;

    // Pretend it takes this long to plan.  Hence all planning at time t is for
    // elements t + delay in the future.
    public static final int ASSUMED_BN_PLANNING_DELAY = 10000 ;

    // Pretend it tasks this long to generate the supply plan.
    public static final int ASSUMED_SUPPLY_PLANNING_DELAY = ASSUMED_BN_PLANNING_DELAY + 5000 ;

    private ArrayList allSupplyEntities = new ArrayList();
    private boolean verbose = true ;
    /**
     * The number of delta Ts to run this for.
     */
    int maxDeltaTs = 1000 ;

    int numBNEntities = 3 ;
    int numCPYEntitiesPerBN = 3 ;
    int numSupplyOrganizations = 2 ;
    int numSupplyUnitsPerOrg = 3 ;

    private ArrayList allBNUnits = new ArrayList() ;
    private ArrayList allCPYUnits = new ArrayList() ;
    private long zonePlannerDelay = ( long ) ( numTimeDeltasPerZoneSchedule * deltaT * VGWorldConstants.MILLISECONDS_PER_SECOND ) ;
    private boolean isInited = false ;
    private int deltaIndex = 0 ;
    private boolean isRunning = false ;

    private ArrayList[] supplyVehicles;
    private TargetGeneratorModel targetGeneratorModel;
    private int numInitialTargetGenerationDeltas = 120 ;

    public CPESimulator() {
        ws = new ReferenceWorldState( boardWidth, boardHeight, penaltyHeight, recoveryHeight, deltaT ) ;

        // The zone world is maintained to be "independent" of the ws.  It must be updated from the reference worldState
        // before every planning cycle.
        zoneWorld = new ZoneWorld( ws, zoneGridSize ) ;

        // Calculate what the visible time horizon is. (E.g. the furthest in time which is actually visible.
        double timeHorizon = ( ws.getUpperX() - ws.getLowerX() ) / VGWorldConstants.getTargetMoveRate() ;
        System.out.println("CPE Simulation Initialization");
        System.out.println("\tTime horizon=" + timeHorizon +
                ", planningHorizon=" + ws.getDeltaT() * numTimeDeltasPerTask * searchDepth + ", zonePlanningHorizon=" +
                ( zonePlannerDelay * VGWorldConstants.SECONDS_PER_MILLISECOND + zoneSearchDepth * numTimeDeltasPerZoneSchedule * ws.getDeltaT() ) );

        // Create a manuever planner per BN entity.
        manueverPlanners = new ManueverPlanner[ numBNEntities ] ;

        ws.setLogEvents( true );

        double currentZoneIndex = 0 ;
        double averageZoneSize = ( ws.getUpperX() / ( numBNEntities * zoneGridSize ) ) ;

        int cpyCount= 0 ;

        for (int i=0;i<numBNEntities;i++) {
            // Now, break the BNs into zones.
            String id = "BN" + (i+1) ;
            String[] subentities = new String[numCPYEntitiesPerBN] ;

            int lowerZoneIndex = (int ) Math.floor( currentZoneIndex ) ;
            currentZoneIndex += averageZoneSize ;
            int upperZoneIndex = (int ) Math.floor( currentZoneIndex ) - 1 ;

            // Make sure the upper zone takes up all the slack.
            if ( i == numBNEntities - 1 ) {
               upperZoneIndex = zoneWorld.getNumZones() - 1 ;
            }

            IndexedZone zone = new IndexedZone( lowerZoneIndex, upperZoneIndex ) ;
            float lower = zoneWorld.getZoneLower( zone ) ;
            float upper = zoneWorld.getZoneUpper( zone ) ;

            ArrayList subordinateEntities = new ArrayList() ;

            /*
             * Space the CPY entities evenly within the zone.
             */
            for (int j=0;j<numCPYEntitiesPerBN;j++) {
                UnitEntity entity = ws.addUnit( (j + 0.5 ) * ( upper - lower ) / numCPYEntitiesPerBN + lower , 0, "CPY" + cpyCount++ ) ;
                subordinateEntities.add( entity.getId() ) ;
                subentities[j] = entity.getId() ;
                allCPYUnits.add( entity.getId() ) ;
            }

            // Create the aggregated BN units.
            BNAggregate bnAggregate = new BNAggregate( id, subentities) ;
            bnAggregate.setCurrentZone( zone );
            zoneWorld.addAggUnitEntity( bnAggregate );
            allBNUnits.add( id ) ;

            manueverPlanners[i] = new ManueverPlanner( id, subordinateEntities ) ;
            manueverPlanners[i].setDeltaValues( numTimeDeltasPerTask, numIncrementsPerPlan );
            replanFrequency = ( searchDepth * numTimeDeltasPerTask * 3 ) / 4 ;
            // Max Depth * numDeltas = search depth in delta Ts.
            manueverPlanners[i].setMaxDepth( searchDepth );
            manueverPlanners[i].setMaxBranchFactor( maxBranchFactor );
        }

        // Initialize the zone planner.
        zonePlanner = new ZonePlanner( allBNUnits, zoneWorld, numTimeDeltasPerZoneSchedule ) ;
        zonePlanner.setMaxDepth( zoneSearchDepth  );
        zonePlanner.setMaxBranchFactor( zoneBranchFactor );

        // Count of supply units.
        int k = 0 ;
        float index = 0 ;

        // Count of CPY units to be assigned as customers
        int l = 0 ;
        float numCPYUnitsPerOrganization = ( float ) allCPYUnits.size() / ( float ) numSupplyOrganizations ;
        supplyCustomers = new ArrayList[ numSupplyOrganizations ] ;
        supplyVehicles = new ArrayList[ numSupplyOrganizations ] ;

        targetGeneratorModel = new TargetGeneratorModel(ws, 0xcafebabe, 4, 0.5f) ;

        for (int j=0;j<numSupplyOrganizations;j++) {
            supplyCustomers[j] = new ArrayList() ;
            supplyVehicles[j] = new ArrayList() ;
            for (int i=0;i<numSupplyUnitsPerOrg;i++) {
                SupplyVehicleEntity se = ws.addSupplyVehicle( (k + 0.5) * ws.getBoardWidth()/( numSupplyUnitsPerOrg * numSupplyOrganizations ),
                        ws.getRecoveryLine(), "Supply" + (k+1) ) ;
                k++ ;
                allSupplyEntities.add( se.getId() ) ;
                supplyVehicles[j].add( se.getId() ) ;
            }

            index += numCPYUnitsPerOrganization ;
            // Assign the first
            while ( l < index && l < allCPYUnits.size() ) {
                supplyCustomers[j].add( allCPYUnits.get(l) ) ;
                l++ ;
            }
        }
    }

    public void setBoardParameters( float width, float height, float penaltyHeight, float recoveryHeight ) {
        this.boardWidth = width ;
        this.boardHeight = height ;
        this.penaltyHeight = penaltyHeight ;
        this.recoveryHeight = recoveryHeight ;
    }

    public void printState() {
        System.out.println("\n---------------------------------------\nWORLD CONFIGURATION ");
        System.out.println("BoardWidth=" + ws.getBoardWidth() );
        System.out.println("BoardHeight=" + ws.getBoardHeight() );
        System.out.println("RecoveryHeight=" + ws.getRecoveryLine() );
        System.out.println("DeltaT=" + ws.getDeltaT() + " secs." );
        System.out.println("PenaltyHeight=" + ws.getPenaltyHeight() );
        System.out.println("Replan Frequency=" + replanFrequency );
        System.out.println("Search depth=" + searchDepth );
        System.out.println("Branch Factor=" + maxBranchFactor );
        System.out.println("----------------------------------------");
    }

    public ZoneWorld getZoneWorld()
    {
        return zoneWorld;
    }

    public int getNumTimeDeltasPerTask()
    {
        return numTimeDeltasPerTask;
    }

    public int getSearchDepth()
    {
        return searchDepth;
    }

    public void setSearchDepth(int searchDepth)
    {
        this.searchDepth = searchDepth;
    }

    public int getMaxBranchFactor()
    {
        return maxBranchFactor;
    }

    public void setMaxBranchFactor(int maxBranchFactor)
    {
        this.maxBranchFactor = maxBranchFactor;
    }

    public int getReplanFrequency()
    {
        return replanFrequency;
    }

    public void setReplanFrequency(int replanFrequency)
    {
        this.replanFrequency = replanFrequency;
    }

    public boolean isVerbose()
    {
        return verbose;
    }

    public void setVerbose(boolean verbose)
    {
        this.verbose = verbose;
    }

    public void planAndDistribute() {
        System.out.println("\nCREATING ZONE and MANUEVER PLAN...");

        // Copy over the results from the ws before invoking the zone planner.
        WorldState longRangeSensedWorld = ws.filter( WorldStateModel.SENSOR_LONG, false, false, null ) ;

        zoneWorld.copyUnitAndTargetStatus( longRangeSensedWorld, true, false, false );

        System.out.println("ZONE PLANNER: Planning with (delayed) start time " + ( zoneWorld.getTimeInSeconds() + zonePlannerDelay/1000.0 )
                + " and horizon=" + zonePlanner.getMaxDepth() * zonePlanner.getDeltaTPerPhase() * zoneWorld.getDeltaT() + " sec.");
        zonePlanner.plan( zoneWorld, zonePlannerDelay);

        Object[][] zplans = zonePlanner.getPlans( false ) ;
        updateZonePlans( zplans ) ;

        // Get the converted plans and distribute them to the manuever planners.
        // These use Interval elements rather than IndexedZone elements within the zone Plans.  This is because the manuever planner does
        // not understand intervals yet.
        zplans = zonePlanner.getPlans( true ) ;
        for (int i = 0; i < zplans.length; i++)
        {
            Object[] zplan = zplans[i];
            System.out.println("\tZONE PLAN for " + zplan[0] + "="  + zplan[1]);
            for (int j = 0; j < manueverPlanners.length; j++)
            {
                ManueverPlanner manueverPlanner = manueverPlanners[j];
                if ( manueverPlanner.getId().equals( zplan[0] ) ) {
                    manueverPlanner.setZonePlan( ( Plan) zplan[1] ) ;
                    break ;
                }
            }
        }
        zonePlanner.release();

        //
        // Initialize the manuever planners with the initial assumed zone.  The initial assumed zone must
        // be extracted from the reference zone world.
        for (int i = 0; i < manueverPlanners.length; i++)
        {
            ManueverPlanner manueverPlanner = manueverPlanners[i];
            BNAggregate agg = (BNAggregate) zoneWorld.getAggUnitEntity( manueverPlanner.getId() ) ;
            manueverPlanner.setInitialZone( zoneWorld.getIntervalForZone( ( IndexedZone )agg.getCurrentZone() ) ) ;
        }

        // Plan with a fixed delay, e.g. assume we execute the current plans into the future and then
        // plan from there.  If ws already has active manuever plans, use those.

        for (int i=0;i<manueverPlanners.length;i++) {
            ManueverPlanner planner = this.manueverPlanners[i] ;
            System.out.println("\nPLANNING for " + planner.getId() + " with initial zone " + planner.getInitialZone() );

            // planner.getSearchStrategy().setZoneSchedule( plan );
            long startTime = System.currentTimeMillis() ;

            planner.plan( ws, ASSUMED_BN_PLANNING_DELAY );

            //System.out.println("DUMPING PLAN NODES:");
            //mp.dumpPlanNodes();
            Object[][] plans = planner.getPlans() ;
            planner.release() ;
            System.out.println("SIMULATOR:: ELAPSED PLANNING TIME = " +
                    ( System.currentTimeMillis() - startTime ) * VGWorldConstants.SECONDS_PER_MILLISECOND );

            updateManueverPlans(plans);
        }

        System.out.println("\nCREATING SUSTAINMENT PLAN FOR TIME ..." + ws.getTime() +
                ", DELAY=" +
                ( ASSUMED_BN_PLANNING_DELAY + ASSUMED_SUPPLY_PLANNING_DELAY ) );

        // Now make a sustainment plan for each organization.
        for (int i=0;i<numSupplyOrganizations;i++) {
            SPlanner sp = new SPlanner() ;
            sp.plan( ws, ASSUMED_BN_PLANNING_DELAY + ASSUMED_SUPPLY_PLANNING_DELAY, supplyCustomers[i], supplyVehicles[i] ) ;

            HashMap splans = sp.getPlans() ;

            System.out.println("SIMULATOR:: Updating supply plans at time " + ws.getTime() );
            updateSustainmentPlans(splans);
        }

    }

//    private void distributeZonePlans(Object[][] zplans)
//    {
//        for (int i = 0; i < zplans.length; i++)
//        {
//            Object[] zplan = zplans[i];
//            ManueverPlanner mp = manueverPlanners[i] ;
//            mp.getSearchStrategy().setZoneSchedule( ( Plan) zplan[1] );
//        }
//    }

    public WorldState getWorldState() {
        return ws ;
    }

    public boolean isRunning()
    {
        return isRunning;
    }


    private void updateZonePlans(Object[][] zplans)
    {
        for (int i = 0; i < zplans.length; i++)
        {
            Object[] zplan = zplans[i];
            String id = (String) zplan[0] ;
            Plan plan = (Plan) zplan[1] ;
            BNAggregate agg = (BNAggregate) zoneWorld.getAggUnitEntity( id ) ;
            agg.setZonePlan( plan );
        }
    }

    private void updateSustainmentPlans(HashMap splans)
    {
        ArrayList sortedSPlans = new ArrayList() ;
        for (Iterator iterator = splans.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            sortedSPlans.add( new Object[] { entry.getKey(), entry.getValue() } ) ;
        }
        Collections.sort( sortedSPlans, new Comparator() {
            public int compare(Object o1, Object o2)
            {
                Object[] p1 = (Object[]) o1, p2 = (Object[]) o2 ;
                return ( ( String ) p1[0]).compareTo( ( String ) p2[0] ) ;
            }
        });

        for (int i = 0; i < sortedSPlans.size(); i++) {
             Object[] objects = (Object[])sortedSPlans.get(i);

            SupplyVehicleEntity sentity = (SupplyVehicleEntity)
                    ws.getEntity( ( String ) objects[0] ) ;
            if ( verbose ) {
                System.out.println("UPDATING " + sentity.getId() );
                System.out.println("OLD PLAN " + sentity.getSupplyPlan()  ) ;
                System.out.println("NEW PLAN " + objects[1] );
            }
            sentity.updateSupplyPlan( (Plan) objects[1] );
            if ( verbose ) {
                System.out.println("MERGED PLAN " + sentity.getSupplyPlan() );
            }
        }
    }

    private void updateManueverPlans(Object[][] plans)
    {
        // Sort these guys for printing out.
        ArrayList sortedPlans = new ArrayList() ;
        for (int i = 0; i < plans.length; i++)
        {
            Object[] plan = plans[i];
            sortedPlans.add( plan ) ;
        }
        Collections.sort( sortedPlans, new Comparator() {
            public int compare(Object o1, Object o2)
            {
                Object[] p1 = (Object[]) o1, p2 = (Object[]) o2 ;
                return ( ( String ) p1[0]).compareTo( ( String ) p2[0] ) ;
            }
        });

        if ( verbose ) {
            System.out.println("SIMULATOR:: Manuever plans at time " + ws.getTime() );
        }
        for (int i=0;i<sortedPlans.size();i++) {
            Object[] pair = (Object[]) sortedPlans.get(i) ;
            EntityInfo info = ws.getEntityInfo( ( String ) ( pair[0]) ) ;
            Plan p = (Plan) pair[1] ;
            UnitEntity entity = (UnitEntity) info.getEntity() ;
            if ( verbose ) {
                System.out.println("NEW PLAN for " + entity.getId() + "=" + p );
            }
            entity.updateManeuverPlan( p );
            if ( verbose ) {
                System.out.println("MERGED PLAN for " + entity.getId() + "=" + entity.getManueverPlan() );
            }
        }
    }

    protected void doPopulateTargets() {
        // Populate the board with targets
        WorldStateModel temp = new WorldStateModel( ws, false, false, false ) ;
        for (int i=0;i<numInitialTargetGenerationDeltas;i++) {
            targetGeneratorModel.execute( temp );
            temp.updateWorldState();
        }

        // Now, copy back into the ws.

        Iterator iter = temp.getTargets() ;
        while (iter.hasNext())
        {
            TargetEntity entity = (TargetEntity) iter.next();
            ws.addEntity( ( Entity ) entity.clone() );
        }
        // Now, reset back to the beginning.
        targetGeneratorModel.resetTime( ws.getTime() );
    }

    protected void generateNewTargets() {
        // Generate any new targets
        targetGeneratorModel.execute();
    }

    public long getTotalPlanningTime()
    {
        return totalPlanningTime;
    }

    public int getPlanningCycles()
    {
        return planningCycles;
    }

    public void pause() {
        isRunning = false ;
    }

    public void run() {
        isRunning = true ;
        System.out.println("Running simulation for a total of " + ( ( maxDeltaTs * ws.getDeltaT() )  ) + " seconds.");
        Thread.currentThread().setPriority( Thread.NORM_PRIORITY - 2 );

        while ( deltaIndex < maxDeltaTs && isRunning ) {
            step();
        }
    }

    public void stop() {
        isRunning = false ;
    }

    private void init() {
        isInited = true ;
//        for (int i=0;i<numInitialTargets;i++) {
//            placeTarget();
//        }
    }

    public void step() {
        if ( !isInited ) {
            init();
            isInited = true ;
        }

        long st = System.currentTimeMillis() ;
        if ( deltaIndex % replanFrequency == 0 ) {
            System.out.println("PLANNING...");
            long startTime = System.currentTimeMillis() ;
            planAndDistribute();
            long endTime = System.currentTimeMillis() ;
            System.out.println("Planning time " + ( endTime - startTime ) / 1000 + " secs." );
            totalPlanningTime += ( endTime - startTime ) ;
            planningCycles ++ ;
            // ws.dump() ;
        }

        System.out.println("Simulating...");
        System.out.println("Iteration " + deltaIndex + ", time=" + ws.getTime() );

        // Generate new targets.
        generateNewTargets();

        synchronized ( ws ) {
            ws.updateWorldState() ;

            // Project what zone we are currently in.
            updateZoneWorld( ws, zoneWorld ) ;
        }

        if ( verbose ) {
            System.out.println( "End state =" + ws.toString() ) ;
        }

        // Increment the count.
        deltaIndex ++ ;

        // Regulate time if we are running.
        if ( timeAdvanceRate > 0 && isRunning ) {
            long et = System.currentTimeMillis() ;

            // Slow down the simulation to the time advance rate.
            long delayTime = (long) ( ws.getDeltaT()  * VGWorldConstants.MILLISECONDS_PER_SECOND / timeAdvanceRate ) ;
            if ( ( et - st ) < delayTime ) {
                try {
                    Thread.sleep( delayTime - ( et - st ) );
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        display.repaint();
    }

    private void updateZoneWorld(WorldState ws, ZoneWorld zw)
    {
        long time = ws.getTime() ;
        for (int i=0;i<zw.getNumAggUnitEntities();i++) {
            BNAggregate agg = (BNAggregate) zw.getAggUnitEntity( i ) ;
            Plan zp = agg.getZonePlan() ;
            ZoneTask t = (ZoneTask) zp.getNearestTaskForTime( time ) ;
            if ( time >= t.getStartTime() && time <= t.getEndTime() ) {
                agg.setCurrentZone( ( IndexedZone ) t.getEndZone() );
            }
            else if ( time < t.getStartTime() ) {
                agg.setCurrentZone( ( IndexedZone ) t.getStartZone() );
            }
            else if ( time > t.getEndTime() ) {
                agg.setCurrentZone( ( IndexedZone ) t.getEndZone() );
            }
            zw.copyUnitAndTargetStatus( ws, true, true, false );
        }
    }

    public int getMaxDeltaTs()
    {
        return maxDeltaTs;
    }

    public void setMaxDeltaTs(int maxDeltaTs)
    {
        this.maxDeltaTs = maxDeltaTs;
    }

    public double getTimeAdvanceRate()
    {
        return timeAdvanceRate;
    }

    public void setTimeAdvanceRate(double timeAdvanceRate)
    {
        this.timeAdvanceRate = timeAdvanceRate;
    }

    public VGFrame getDisplay()
    {
        return display;
    }

    public void setDisplay(VGFrame display)
    {
        this.display = display;
    }

    public static final void main( String[] args) {
        CPESimulator simulator = new CPESimulator() ;
        VGFrame frame = new VGFrame( simulator.getWorldState(), simulator ) ;
        simulator.setDisplay( frame );
        frame.setVisible( true );
    }


    /**
     * Replan frequency in delta Ts.
     */
    int replanFrequency = 6 ;
    int numInitialTargets = 3 ;
    double timeAdvanceRate = 4.0 ;

    /**
     * Arrival rate.
     */
    double targetPopupProb = 0.075 ;
    Random targetGenerator = new Random(0) ;
    ReferenceWorldState ws ;
    ManueverPlanner[] manueverPlanners ;

    /**
     * An array of lists of supply customers.
     */
    ArrayList[] supplyCustomers ;
    ZonePlanner zonePlanner ;

    VGFrame display ;

}
