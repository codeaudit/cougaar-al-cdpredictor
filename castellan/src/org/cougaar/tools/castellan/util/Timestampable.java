package org.cougaar.tools.castellan.util;

/**
 * Interface for time-stamped objects.
 */
public interface Timestampable
{
    public long getTime() ;

    public long getExecutionTime() ;

    public int getTransactionId() ;
}
