package org.cougaar.tools.techspecs.results;

import java.io.Serializable;

/**
 * Represents an observed action committed by an action.
 *
 */
public abstract class ActionResult implements Serializable
{
    /**
     * Amount of CPU resources consumed in NIU.
     */
    public static final int RESOURCE_CPU = 0 ;

    public static final int RESOURCE_NETWORK = 1 ;

    public static final int RESOURCE_MEMORY_ALLOC = 2 ;

    public static final int RESOURCE_MEMORY_FREE = 3 ;

    /**
     * This is a observed value only and is the raw amount of (CPU) time elapsed.  This number is
     * not adjusted for CPU load.
     */
    public static final int RAW_CPU_TIME_ELAPSED = 4 ;

    /**
     * This signals a generated message of the type associated with the action.  It is assumed to
     * have occured unless there is an error condition.
     */
    public static final int OUTPUT_MESSAGE = 5 ;

    public static final int TIMER_SCHEDULE = 6 ;

    protected int validBits ;

    public ActionResult(int type)
    {
        this.type = type;
    }

    protected int type ;

    protected void checkFlag(int flag)
    {
        if ( ( flag & validBits ) == 0 ) {
           throw new RuntimeException( "Field not valid." ) ;
        }
    }

    protected void validateFlag( int flag ) {
        validBits |= flag ;
    }
//    public abstract boolean isDeclared() ;
//
//    public abstract boolean isObserved() ;

//    public void setObservedValue(int observedValue)
//    {
//        this.observedValue = observedValue;
//        isObservedValid = true ;
//    }
//
//    /**
//     * @return The number of units of resource consumed by the action as declared by the action.
//     */
//    public int getDeclaredValue()
//    {
//        return declaredValue;
//    }
//
//    /**
//     *
//     * @return The number of units of resource consumed by the action as observed.
//     */
//    public int getObservedValue()
//    {
//        return observedValue;
//    }
//
//    protected boolean isDeclaredValid = false, isObservedValid = false ;
//    protected int observedValue ;
//    protected int declaredValue ;

}
