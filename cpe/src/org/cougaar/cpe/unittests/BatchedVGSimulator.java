package org.cougaar.cpe.unittests;

import java.io.*;

public class BatchedVGSimulator
{
    int maxDeltaTs = 500 ;

    public int[] searchDepths = {2,3,4,5};

    public int[] searchBreadths = {20,30,40,50} ;

    /**
     * Replan frequency must be less that searchDepths
     */
    public int[] replanFrequency = {1,2,3,4} ;

    public double[][][] scores ;
    public long[][][] planningTimes ;
    public int[][][] planningIterations ;
    public File outputFile ;
    protected FileOutputStream fos ;
    protected PrintWriter fw ;

    public BatchedVGSimulator()
    {
    }

    public BatchedVGSimulator( String fileName )
    {
        outputFile = new File( fileName ) ;
        if ( !outputFile.isDirectory() ) {
            try
            {
                fos = new FileOutputStream( outputFile ) ;
                fw = new PrintWriter( fos ) ;
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
        }
        else {
            System.err.println("Could not open directory.");
        }

    }

    public void run() {
        scores = new double[searchDepths.length][searchBreadths.length][replanFrequency.length] ;
        planningTimes = new long[searchDepths.length][searchBreadths.length][replanFrequency.length] ;
        System.out.println("");

        for (int i=0;i<searchDepths.length;i++) {
            for (int j=0;j<searchBreadths.length;j++) {
                if ( fw != null ) {
                    fw.println( "searchDepth=" + searchDepths[i] + ", searchBreadth=" + searchBreadths[j] ) ;
                }
                for (int k=0;k<replanFrequency.length;k++) {
                    if ( replanFrequency[k] >= searchDepths[i]) {
                        continue ;
                    }

                    VGSimulator sim = new VGSimulator( ) ;
                    sim.setMaxBranchFactor( searchBreadths[j]);
                    sim.setSearchDepth( searchDepths[i]);
                    sim.setReplanFrequency( replanFrequency[k] * sim.getNumDeltasPerTask());
                    sim.setVerbose( false ) ;
                    sim.setTimeAdvanceRate( -1 );
                    sim.setMaxDeltaTs( maxDeltaTs );
                    sim.run();

                    scores[i][j][k] = sim.getWorldState().getScore() ;
                    planningTimes[i][j][k] = sim.getTotalPlanningTime() ;
                    planningIterations[i][j][k] = sim.getPlanningCycles() ;
                    sim = null ;
                    System.gc();
                    if ( fw != null ) {
                        fw.print( scores[i][j][k] + " " );
                        fw.flush();
                    }
                }
                if ( fw != null ) {
                    fw.println();
                }
            }
        }

        if ( fw != null ) {
            fw.println("TOTAL PLANNING TIMES & ITERATIONS:");
            for (int i = 0; i < planningTimes.length; i++)
            {
                long[][] planningTime = planningTimes[i];
                for (int j = 0; j < planningTime.length; j++)
                {
                    long[] longs = planningTime[j];
                    for (int k = 0; k < longs.length; k++)
                    {
                        long aLong = longs[k];
                        fw.print( "(" + aLong + ", " + planningIterations[i][j][k] + ")");
                    }
                    fw.println();
                }
            }
        }
    }

    public static final void main( String[] args ) {
        BatchedVGSimulator sim ;

        if ( args.length == 0 ) {
            sim = new BatchedVGSimulator() ;
        }
        else {
            sim = new BatchedVGSimulator( args[0] ) ;
        }

        sim.run();

    }
}
