package org.cougaar.tools.techspecs.results;

/**
 * User: wpeng
 * Date: Mar 15, 2004
 * Time: 5:17:52 PM
 */
public class NetworkActionResult extends ActionResult
{
    public static final int MEAN_BYTES = 0x0001 ;
    public static final int STD_DEV_BYTES = 0x0002 ;
    public static final int MIN_BYTES = 0x0004 ;
    public static final int MAX_BYTES = 0x0008 ;


    public NetworkActionResult( int bytes )
    {
        super( ActionResult.RESOURCE_NETWORK ) ;
        setMeanNumBytes( bytes );
    }

    public float getMaxNumBytes()
    {
        checkFlag( MAX_BYTES);
        return maxNumBytes;
    }

    public void setMaxNumBytes(float maxNumBytes)
    {
        validateFlag( MAX_BYTES ) ;
        this.maxNumBytes = maxNumBytes;
    }

    public float getMeanNumBytes()
    {
        checkFlag( MEAN_BYTES);
        return meanNumBytes;
    }

    public void setMeanNumBytes(float meanNumBytes)
    {
        validateFlag( MEAN_BYTES );
        this.meanNumBytes = meanNumBytes;
    }

    public float getMinNumBytes()
    {
        checkFlag( MIN_BYTES );
        return minNumBytes;
    }

    public void setMinNumBytes(float minNumBytes)
    {
        validateFlag( MIN_BYTES );
        this.minNumBytes = minNumBytes;
    }

    public float getStdDevBytes()
    {
        checkFlag( STD_DEV_BYTES );
        return stdDevBytes;
    }

    public void setStdDevBytes(float stdDevBytes)
    {
        validateFlag( STD_DEV_BYTES );
        this.stdDevBytes = stdDevBytes;
    }

    protected float meanNumBytes ;
    protected float stdDevBytes ;
    protected float minNumBytes, maxNumBytes ;
}
