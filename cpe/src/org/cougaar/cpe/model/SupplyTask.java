package org.cougaar.cpe.model;

public class SupplyTask extends Task {

    /**
     * Transport a load of fuel.
     */
    public static final int SUPPLY_FUEL = 0 ;

    /**
     * Transport load of ammo.
     */
    public static final int SUPPLY_AMMO = 1 ;

    /**
     * Do nothing but move back to the supply area.
     */
    public static final int ACTION_RECOVERY = 2 ;

    public SupplyTask(String to, String from,
                      long startTime, long endTime, float quantity,
                      int type)
    {
        this(startTime, endTime) ;
        this.from = from;
        this.quantity = quantity;
        this.to = to;
        switch ( type ) {
            case SUPPLY_FUEL :
            case SUPPLY_AMMO :
            case ACTION_RECOVERY :
                this.type = type ;
                break ;
            default:
                throw new IllegalArgumentException( "Type " + type + " is not valid. " ) ;
        }
    }

    public void resetTask() {
        super.resetTask();
        observedResult = null ;
    }

    public void toString(StringBuffer buf) {
        super.toString(buf);
        buf.append( ",id=" ).append( getId() ).append( ",action=" ).append( getStringForAction( type ) ) ;
        buf.append( ",from=").append( from ).append( ",to=").append( to ) ;
        if ( type == SUPPLY_AMMO || type == SUPPLY_FUEL ) {
            buf.append( ",quantity=").append( quantity ) ;
        }
    }

    public static String getStringForAction( int action ) {
        switch ( action ) {
            case SUPPLY_FUEL :
                return "Supply Fuel" ;
            case SUPPLY_AMMO :
                return "Supply Ammo" ;
            case ACTION_RECOVERY :
                return "Recovery" ;
            default:
                return "Unknown " ;
        }
    }


    protected SupplyTask(long startTime, long endTime) {
        super(startTime, endTime);
    }

    /**
     * World id of supplier.
     * @return
     */
    public String getFrom() {
        return from;
    }

    public float getQuantity() {
        return quantity;
    }

    public String getDestination() {
        return to;
    }

    public int getType() {
        return type;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public void setComplete(boolean complete) {
        isComplete = complete;
    }

    public TaskResult getObservedResult() {
        return observedResult;
    }

    public void setObservedResult(SupplyResult result) {
        this.observedResult = result;
    }

    public SupplyResult getEstimatedResult() {
        return estimatedResult;
    }

    public void setEstimatedResult(SupplyResult estimatedResult) {
        this.estimatedResult = estimatedResult;
    }

    protected SupplyResult observedResult, estimatedResult ;
    protected boolean isComplete ;
    protected String from ;
    protected String to ;
    protected float quantity ;
    protected int type ;

    public Object clone() {
        SupplyTask task = new SupplyTask(startTime,endTime) ;
        task.from = from ;
        task.to = to ;
        task.quantity = quantity ;
        task.type = type ;
        task.isComplete = isComplete ;
        task.id = id ;
        task.disposition = disposition ;
        if ( observedResult != null ) {
            task.observedResult = (SupplyResult) observedResult.clone() ;
        }
        if ( estimatedResult != null ) {
            task.estimatedResult = (SupplyResult) estimatedResult.clone() ;
        }
        return task ;
    }

    protected static long count = 0 ;
}
