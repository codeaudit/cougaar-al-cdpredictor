package org.cougaar.cpe.util;

/**
 * User: wpeng
 * Date: Sep 14, 2003
 * Time: 5:50:12 PM
 */
public class CPUConsumer
{
    static private long[] values ;

    static {
        values = new long[65536] ;
        // values = new long[262144] ;
        for (int i = 0; i < values.length; i++)
        {
            values[i] = i ;
        }
    }

    /**
     * Consume a unit of CPU.  1000 units equals roughly 1.0 secs on a
     * 2.5 GHz P4. This is a memory intensive CPU consuming process and hence
     * is more affected by large working sets. (It takes about 512 K of memory
     * in the process.)
     * @param unitsOfCPU
     */
    public static void consumeCPUMemoryIntensive( long unitsOfCPU ) {
        long count = 0 ;
        long limit = unitsOfCPU*32000 ;
        for (int i=0;i<limit;i++) {
            count += values[i % values.length ];
        }
        value = count ;
    }

    /**
     * Consume a unit of CPU.  1000 units equals roughly 1.0 secs on a
     * 2.5 GHz P4.
     * @param unitsOfCPU
     */
    public static void consumeCPU( long unitsOfCPU ) {
        long count = 0 ;
        long limit = unitsOfCPU*200000 ;
        for (long i=0;i<limit;i++) {
            count++ ;
        }
        value = count ;
//        return count ;
    }

    public static long getValue()
    {
        return value;
    }

    private static long value = 0 ;

    public static final void main( String[] args ) {
        long startTime = System.currentTimeMillis() ;
        consumeCPU( 1000 ) ;
        long endTime = System.currentTimeMillis() ;
        System.out.println("time=" + ( ( endTime - startTime ) / 1000.0 )  );
        startTime = System.currentTimeMillis() ;
        consumeCPUMemoryIntensive( 1000 ); ;
        endTime = System.currentTimeMillis() ;
        System.out.println("time=" + ( ( endTime - startTime ) / 1000.0 ) );

        try
        {
            Thread.sleep( 1000 );
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        startTime = System.currentTimeMillis() ;
        consumeCPU( 1000 ) ;
        endTime = System.currentTimeMillis() ;
        System.out.println("time=" + ( ( endTime - startTime ) / 1000.0 )  );
        startTime = System.currentTimeMillis() ;
        consumeCPUMemoryIntensive( 1000 ); ;
        endTime = System.currentTimeMillis() ;
        System.out.println("time=" + ( ( endTime - startTime ) / 1000.0 ) );

        try
        {
            Thread.sleep( 1000 );
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        startTime = System.currentTimeMillis() ;
        consumeCPU( 1000 ) ;
        endTime = System.currentTimeMillis() ;
        System.out.println("time=" + ( ( endTime - startTime ) / 1000.0 )  );
        startTime = System.currentTimeMillis() ;
        consumeCPUMemoryIntensive( 1000 ); ;
        endTime = System.currentTimeMillis() ;
        System.out.println("time=" + ( ( endTime - startTime ) / 1000.0 ) );

    }

}
