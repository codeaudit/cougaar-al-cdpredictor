package org.cougaar.cpe.model;

import org.cougaar.cpe.model.EngageByFireResult;

import java.util.ArrayList;
import java.io.Serializable;

/**
 * The results of execution for a single time delta.
 */

public class ExecutionResult extends TaskResult {

    public ExecutionResult() {
    }

    public ExecutionResult( double endX, double endY,
                           double startX, double startY)
    {
        this.endX = endX;
        this.endY = endY;
        this.startX = startX;
        this.startY = startY;
    }

    public Object clone() {
        ExecutionResult er = new ExecutionResult() ;
        er.fuelConsumption = fuelConsumption ;
        er.startX = startX ;
        er.endX = endX ;
        return er ;
    }

    public int getNumResults() {
        return engageByFireResults.size() ;
    }

    public EngageByFireResult getResult( int i ) {
        return (EngageByFireResult) engageByFireResults.get(i) ;
    }

    public EngageByFireResult getResult( String worldId ) {
        for (int i = 0; i < engageByFireResults.size(); i++) {
          EngageByFireResult result = (EngageByFireResult)engageByFireResults.get(i);
          if ( result.getTargetId().equals(worldId)) {
              return result ;
          }
        }
        EngageByFireResult result = new EngageByFireResult( worldId, 0, 0, false ) ;
        engageByFireResults.add( result ) ;
        return result ;
    }

    public double getFuelConsumption() {
        return fuelConsumption;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public void setFuelConsumption(double fuelConsumption) {
        this.fuelConsumption = fuelConsumption;
    }

    public void setEndPosition(double endX, double endY ) {
        this.endX = endX ;
        this.endY = endY ;
    }

    public double getEndY() {
        return endY;
    }

    public double getEndX() {
        return endX;
    }

    protected ArrayList engageByFireResults = new ArrayList() ;
    protected double fuelConsumption ;
    protected double startX, startY ;
    protected double endX, endY ;
    protected long startTime, endTime ;
}
