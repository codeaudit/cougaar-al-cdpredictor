package org.cougaar.cpe.model;

import java.awt.geom.Point2D;
import java.io.Serializable;

public class MoveResult implements Serializable {

    public MoveResult( double startX, double startY, double endX, double endY, double fuelConsumption ) {
        this.endX = endX;
        this.endY = endY;
        this.fuelConsumption = fuelConsumption;
        this.startX = startX;
        this.startY = startY;
        distanceMoved = Point2D.distance( startX, startY, endX, endY ) ;
    }

    public double getDistanceMoved() {
        return distanceMoved;
    }

    public double getEndX() {
        return endX;
    }

    public double getEndY() {
        return endY;
    }

    public double getFuelConsumption() {
        return fuelConsumption;
    }

    public double getStartX() {
        return startX;
    }

    public double getStartY() {
        return startY;
    }

    protected double startX, startY, endX, endY ;
    protected double distanceMoved ;
    protected double fuelConsumption ;
}
