package org.cougaar.tools.techspecs.results;

import java.util.BitSet;

/**
 *  Declared CPU consumption levels in NIU.
 */
public class CPUActionResult extends ActionResult
{
    public static final int MEAN_NIU_CONSUMED = 0x0001 ;
    public static final int MIN_NIU_CONSUMED = 0x0002 ;
    public static final int MAX_NIU_CONSUMED = 0x0004 ;
    public static final int STD_DEVIATION_NIU_CONSUMED = 0x0008 ;

    public CPUActionResult(float meanNIUConsumed)
    {
        super( ActionResult.RESOURCE_CPU ) ;
        setMeanNIUConsumed( meanNIUConsumed );
    }

    public CPUActionResult(float meanNIUConsumed, float minNIUConsumed, float maxNIUConsumed)
    {
        super( ActionResult.RESOURCE_CPU ) ;
        setMeanNIUConsumed( meanNIUConsumed );
        setMinNIUConsumed( minNIUConsumed );
        setMaxNIUConsumed( maxNIUConsumed );
    }

    public float getMaxNIUConsumed()
    {
        checkFlag( MAX_NIU_CONSUMED ) ;
        return maxNIUConsumed;
    }


    public void setMaxNIUConsumed(float maxNIUConsumed)
    {
        validateFlag( MAX_NIU_CONSUMED );
        this.maxNIUConsumed = maxNIUConsumed;
    }

    public float getMeanNIUConsumed()
    {
        checkFlag( MEAN_NIU_CONSUMED );
        return meanNIUConsumed;
    }

    public void setMeanNIUConsumed(float meanNIUConsumed)
    {
        validateFlag( MEAN_NIU_CONSUMED);
        this.meanNIUConsumed = meanNIUConsumed;
    }

    public float getMinNIUConsumed()
    {
        checkFlag( MIN_NIU_CONSUMED );
        return minNIUConsumed;
    }

    public void setMinNIUConsumed(float minNIUConsumed)
    {
        validateFlag( MIN_NIU_CONSUMED );
        this.minNIUConsumed = minNIUConsumed;
    }

    public float getStdDeviationNIUConsumed()
    {
        checkFlag( STD_DEVIATION_NIU_CONSUMED );
        return stdDeviationNIUConsumed;
    }

    public void setStdDeviationNIUConsumed(float stdDeviationNIUConsumed)
    {
        validateFlag( STD_DEVIATION_NIU_CONSUMED );
        this.stdDeviationNIUConsumed = stdDeviationNIUConsumed;
    }

    protected float meanNIUConsumed ;
    protected float minNIUConsumed ;
    protected float maxNIUConsumed ;
    protected float stdDeviationNIUConsumed ;
}
