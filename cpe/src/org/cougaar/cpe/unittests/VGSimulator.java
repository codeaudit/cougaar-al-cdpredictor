package org.cougaar.cpe.unittests;

import org.cougaar.cpe.*;
import org.cougaar.cpe.splan.SPlanner;
import org.cougaar.cpe.model.*;
import org.cougaar.cpe.mplan.ManueverPlanner;

import java.util.*;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * Class for testing all planning and execution code centralized on one computer.
 *
 * User: wpeng
 * Date: Mar 20, 2003
 * Time: 3:23:11 PM
 */
public class VGSimulator {

    protected long totalPlanningTime = 0 ;
    protected int planningCycles = 0 ;

    protected double boardWidth = 24 ;
    protected double boardHeight = 15 ;
    protected double penaltyHeight = 4 ;
    protected double deltaT = 5;
    protected double recoveryHeight = - VGWorldConstants.getSupplyVehicleMovementRate() * deltaT * 4 ;

    protected int numDeltasPerTask = 4 ;
    protected int numIncrementsPerPlan = 2 ;
    protected int searchDepth = 6 ;
    protected int maxBranchFactor = 60 ;

    // Pretend it takes this long to plan.  Hence all planning at time t is for
    // elements t + delay in the future.
    public static final int ASSUMED_PLANNING_DELAY = 10000 ;

    // Pretend it tasks this long to generate the supply plan.
    public static final int ASSUMED_SUPPLY_PLANNING_DELAY = 5000 ;

    private ArrayList supplyEntities = new ArrayList();
    private boolean verbose = true ;
    /**
     * The number of delta Ts to run this for.
     */
    int maxDeltaTs = 1000 ;
    int numEntities = 3 ;

    int numSupplyUnits = 4 ;


    public VGSimulator() {
        ws = new ReferenceWorldState( boardWidth, boardHeight, penaltyHeight, recoveryHeight, deltaT ) ;


        ws.setLogEvents( true );
        ArrayList subordinateEntities = new ArrayList() ;
        for (int i=0;i<numEntities;i++) {
            UnitEntity entity = ws.addUnit( (i + 0.5 ) * ws.getBoardWidth()/numEntities, 0, "Company." + i ) ;
            subordinateEntities.add( entity.getId() ) ;
            // Each entity is assigned a zone proportional to its position on the board.
            Zone z = new Interval( ( float) ( i * ws.getBoardWidth() / numEntities) ,
                    ( float ) ( (i+1) * ws.getBoardWidth() / numEntities ) , ( float ) ws.getBoardHeight()  ) ;
            entity.setZoneSchedule( new FixedZoneSchedule( z ) );
        }

        mp = new ManueverPlanner( "BDE.1", subordinateEntities ) ;

        for (int i=0;i<numSupplyUnits;i++) {
            SupplyVehicleEntity se = ws.addSupplyVehicle( (i + 0.5) * ws.getBoardWidth()/numSupplyUnits, ws.getRecoveryLine(), "Supply." + i ) ;
            supplyEntities.add( se.getId() ) ;
        }

        // Number of deltas per planning cycle.
        mp.setDeltaValues( numDeltasPerTask, numIncrementsPerPlan );
        replanFrequency = ( searchDepth * numDeltasPerTask * 3 ) / 4 ;
        // Max Depth * numDeltas = search depth in delta Ts.
        mp.setMaxDepth( searchDepth );
        mp.setMaxBranchFactor( maxBranchFactor );

    }

    public void setBoardParameters( double width, double height, double penaltyHeight, double recoveryHeight ) {
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

    public int getNumDeltasPerTask()
    {
        return numDeltasPerTask;
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
        long startTime = System.currentTimeMillis() ;
        System.out.println("\nCREATING MANUEVER PLAN...");

        // Plan with a fixed delay, e.g. assume we execute the current plans into the future and then
        // plan from there.  If ws already has active manuever plans, use those.
        mp.plan( ws, ASSUMED_PLANNING_DELAY );

        //System.out.println("DUMPING PLAN NODES:");
        //mp.dumpPlanNodes();
        Object[][] plans = mp.getPlans() ;
        mp.release() ;
        System.out.println("SIMULATOR:: ELAPSED PLANNING TIME= " +
                ( System.currentTimeMillis() - startTime ) * VGWorldConstants.SECONDS_PER_MILLISECOND );

        updateManueverPlans(plans);

        System.out.println("\nCREATING SUSTAINMENT PLAN FOR TIME ..." + ws.getTime() +
                ", DELAY=" +
                ( ASSUMED_PLANNING_DELAY + ASSUMED_SUPPLY_PLANNING_DELAY ) );
        // Now make a sustainment plan.
        SPlanner sp = new SPlanner() ;
        sp.plan( ws, ASSUMED_PLANNING_DELAY + ASSUMED_SUPPLY_PLANNING_DELAY,
                mp.getSubordinateUnits(), supplyEntities  ) ;

        HashMap splans = sp.getPlans() ;

        System.out.println("SIMULATOR:: Updating supply plans at time " + ws.getTime() );
        updateSustainmentPlans(splans);
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

    protected void populateTargets() {
    }


    protected void generateNewTargets() {
        // Generate any new targets
        if ( targetGenerator.nextDouble() <= targetPopupProb ) {
            placeTarget();
        }
    }

    private void placeTarget() {
        double targetLocation = targetGenerator.nextDouble() *
                ws.getBoardWidth() ;
        TargetEntity t = ws.addTarget( targetLocation, ws.getBoardHeight(),
                0, -VGWorldConstants.getTargetMoveRate() ) ;
        System.out.println("Generated target at " + t );
    }

    public long getTotalPlanningTime()
    {
        return totalPlanningTime;
    }

    public int getPlanningCycles()
    {
        return planningCycles;
    }

    public void run() {
        System.out.println("Running simulation for a total of " + ( ( maxDeltaTs * ws.getDeltaT() )  ) + " seconds.");
        Thread.currentThread().setPriority( Thread.NORM_PRIORITY - 2 );
        //planAndDistribute();
        for (int i=0;i<numInitialTargets;i++) {
            placeTarget();
        }

        for (int i=0;i<maxDeltaTs;i++) {
            long st = System.currentTimeMillis() ;
            if ( i % replanFrequency == 0 ) {
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
            System.out.println("Iteration " + i + ",Time=" + ws.getTime() );
            generateNewTargets();

            // Update the world state.
//            if ( i == 111 ) {
//                System.out.println("DEBUG.");
//            }

            synchronized ( ws ) {
                ws.updateWorldState() ;
            }
            if ( verbose ) {
                System.out.println( "End state =" + ws.toString() ) ;
            }

            if ( timeAdvanceRate > 0 ) {
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

    public static final void main( String[] args) {
        VGSimulator simulator = new VGSimulator() ;
//        VGFrame frame = new VGFrame( simulator.getWorldState() ) ;
//        frame.setVisible( true );
        simulator.run();
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
    WorldState ws ;
    ManueverPlanner mp ;

    public WorldState getWorldState() {
        return ws ;
    }
}
