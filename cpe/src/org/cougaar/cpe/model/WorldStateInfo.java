package org.cougaar.cpe.model;

import java.io.Serializable;


public class WorldStateInfo implements Serializable {

    public WorldStateInfo(double boardWidth, double boardHeight, double penaltyHeight, double deltaT) {
        this.boardHeight = boardHeight;
        this.boardWidth = boardWidth;
        if ( deltaT <= 0 ) {
            throw new IllegalArgumentException( "deltaT " + deltaT + " must be > 0." ) ;
        }
        this.deltaT = deltaT;
        this.penaltyHeight = penaltyHeight;
    }

    public WorldStateInfo(double boardWidth, double boardHeight, double penaltyHeight) {
        this.boardHeight = boardHeight;
        this.boardWidth = boardWidth;
        this.penaltyHeight = penaltyHeight;
    }

    public WorldStateInfo(double boardWidth, double boardHeight, double penaltyHeight, double recoveryLine, double deltaT ) {
        if ( deltaT <= 0 ) {
            throw new IllegalArgumentException( "deltaT " + deltaT + " must be > 0." ) ;
        }
        if ( recoveryLine > 0 ) {
            throw new IllegalArgumentException( "recoveryLine " + deltaT + " must be < 0." ) ;
        }
        this.boardHeight = boardHeight;
        this.deltaT = deltaT ;
        this.boardWidth = boardWidth;
        this.penaltyHeight = penaltyHeight;
        this.recoveryLine = recoveryLine;
    }

    public long getBaseTime() {
        return baseTime;
    }

    public void setBaseTime(long baseTime) {
        this.baseTime = baseTime;
    }

    public double getBoardHeight() {
        return boardHeight;
    }

    public double getBoardWidth() {
        return boardWidth;
    }

    public double getPenaltyHeight() {
        return penaltyHeight;
    }

    public double getDeltaT() {
        return deltaT;
    }

    public double getRecoveryLine() {
        return recoveryLine;
    }

    public double getGridSize()
    {
        return gridSize;
    }

    public float getKillScore()
    {
        return killScore;
    }

    public void setKillScore(float killScore)
    {
        this.killScore = killScore;
    }

    public float getPenaltyFactor()
    {
        return penaltyFactor;
    }

    public void setPenaltyFactor(float penaltyFactor)
    {
        this.penaltyFactor = penaltyFactor;
    }

    public float getViolationFactor()
    {
        return violationFactor;
    }

    public void setViolationFactor(float violationFactor)
    {
        this.violationFactor = violationFactor;
    }

    public float getAttritionFactor()
    {
        return attritionFactor;
    }

    public boolean isTargetRoutEnabled()
    {
        return targetRoutEnabled;
    }

    public void setTargetRoutEnabled(boolean targetRoutEnabled)
    {
        this.targetRoutEnabled = targetRoutEnabled;
    }

    /**
     * Standard simulation delta T size in seconds.
     */
    protected double deltaT = 5.0 ;
    protected long baseTime = 0 ;

    private double recoveryLine = -1;
    private double boardWidth, boardHeight, penaltyHeight ;
    private double gridSize = VGWorldConstants.WORLD_GRID_SIZE ;
    protected transient WorldMetrics metrics ;
    private float killScore = 15;
    private float penaltyFactor = 5;
    private float violationFactor = 50;
    private float attritionFactor = 1 ;
    private boolean targetRoutEnabled = true ;


}
