package org.cougaar.cpe.model;

import java.io.Serializable;

public class EngageByFireResult implements Serializable {

    public EngageByFireResult() {
    }

    public EngageByFireResult(String targetId, double attritValue, int ammoConsumed, boolean destroyed) {
        this.attritValue = attritValue;
        this.destroyed = destroyed;
        this.ammoConsumed = ammoConsumed ;
        this.targetId = targetId;
    }

    public void setAmmoConsumed(int ammoConsumed) {
        this.ammoConsumed = ammoConsumed;
    }

    public int getAmmoConsumed() {
        return ammoConsumed;
    }

    public void setAttritValue(double attritValue) {
        this.attritValue = attritValue;
    }

    public void setDestroyed(boolean destroyed) {
        this.destroyed = destroyed;
    }

    public double getAttritValue() {
        return attritValue;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public String getTargetId() {
        return targetId;
    }

    protected int ammoConsumed ;
    protected String targetId ;
    protected double attritValue ;
    protected boolean destroyed ;
}
