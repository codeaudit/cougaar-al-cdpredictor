package org.cougaar.cpe.model;

import java.io.Serializable;

/**
 * User: wpeng
 * Date: Apr 22, 2003
 * Time: 1:34:33 PM
 */
public class SupplyResult extends TaskResult {

    public SupplyResult(long startTime) {
        this.startTime = startTime;
    }

    protected SupplyResult(float quantity, int type, long startTime, long endTime ) {
        this.quantity = quantity ;
        this.type = type;
        this.startTime = startTime ;
        this.endTime = endTime ;
    }

    public String toString() {
        return "[" + SupplyTask.getStringForAction( type ) +
                ",start=" + startTime + ",end=" + endTime + ",quantity=" + quantity + "]" ;
    }

    public Object clone() {
        return new SupplyResult( quantity, type, startTime, endTime ) ;
    }

    public SupplyResult() {
    }

    public float getQuantity() {
        return quantity;
    }

    public int getType() {
        return type;
    }

    public void setResult(float quantity, int type, long endTime ) {
        this.quantity = quantity;
        this.endTime = endTime ;
        this.type = type ;
    }

    public long getStartTime()
    {
        return startTime;
    }

    protected long startTime =-1, endTime =-1;
    protected float quantity ;
    protected int type ;
}
