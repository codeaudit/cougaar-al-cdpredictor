package org.cougaar.cpe.model;

import org.cougaar.cpe.model.ExecutionResult;
import org.cougaar.cpe.model.Task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.NoSuchElementException;

public class UnitTask extends Task {

    public static final int ACTION_NONE = 0 ;

    public static final int ACTION_MOVE = 1 ;

    public static final int ACTION_TRACK_TARGET = 2 ;

    public static final int ENGAGE_NONE = 0 ;

    public static final int ENGAGE_LOWEST = 1 ;

    public static final int ENGAGE_LIST = 2 ;

    public UnitTask(long startTime, long endTime, float destX, float destY) {
        super( startTime, endTime );
        this.xTarget = destX ;
        this.yTarget = destY ;
        moveAction = ACTION_MOVE ;
    }

    public UnitTask( long startTime, long endTime, String targetId, float xTarget ) {
        super(endTime, startTime) ;
        addTarget( targetId );
        engageAction = ENGAGE_LIST ;
        moveAction = ACTION_MOVE ;
        this.xTarget = xTarget;
    }

    public UnitTask( long startTime, long endTime, String targetId ) {
        super(endTime, startTime) ;
        addTarget( targetId );
        engageAction = ENGAGE_LIST ;
        moveAction = ACTION_MOVE ;
    }

    public void toString( StringBuffer buf ) {
        super.toString( buf );
        buf.append( ",destX=" ).append(xTarget ) ;
        buf.append( ",targets=").append( targets ) ;
        buf.append( ",er=" ).append( er ) ;
    }

    public void resetTask() {
        super.resetTask();
        er = null ;
    }

    public boolean isEngageByFire() {
        return engageAction != ENGAGE_NONE ;
    }

    public void setEngageByFire(boolean engageByFire) {
        this.engageAction = ENGAGE_LIST ;
    }

    public int getEngageAction() {
        return engageAction;
    }

    public void setEngageAction(int engageAction) {
        this.engageAction = (byte) engageAction;
    }

    public void addTarget( String targetId ) {
        if ( targets == null ) {
            targets = new ArrayList() ;
        }
        if ( !targets.contains( targetId ) ) {
            targets.add( targetId ) ;
        }
    }

    public int getNumTargets() {
        if ( targets == null ) { return 0 ; }
        return targets.size() ;
    }

    public String getTarget( int i ) {
        if ( targets == null ) {
            throw new NoSuchElementException( "Targets is null," ) ;
        }
        return (String) targets.get(i) ;
    }

    public Collection getTargets() {
        if ( targets == null ) {
            return Collections.EMPTY_LIST ;
        }

        return targets ;
    }

    public double getDestination() {
        return xTarget;
    }

    public int getMoveAction() {
        return moveAction;
    }

    public void setMoveAction( int moveAction) {
        this.moveAction = (byte) moveAction;
    }

    public void setExecutionResult(ExecutionResult er) {
        this.er = er;
    }

    public TaskResult getObservedResult() {
        return er;
    }

    public double getDestX() {
        return xTarget;
    }

    public double getDestY() {
        return yTarget;
    }

    /**
     * Return a cloned version without the execution result.
     *
     */
    public Object clone() {
        UnitTask result =
                new UnitTask( startTime, endTime, xTarget, yTarget ) ;
        result.moveAction= moveAction ;
        result.engageAction = engageAction ;
        result.disposition = disposition ;
        if ( targets != null ) {
            result.targets = (ArrayList) targets.clone() ;
        }
        if ( er != null ) {
            er = (ExecutionResult) er.clone() ;
        }
        return result ;
    }

    protected float xTarget, yTarget ;
    protected byte moveAction ;
    protected byte engageAction ;
    protected ArrayList targets = null ;
    protected ExecutionResult er ;

}
