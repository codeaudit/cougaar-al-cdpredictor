/*
 * PlanElementLog.java
 *
 * Created on August 14, 2001, 2:50 PM
 */

package org.cougaar.tools.castellan.analysis;
import java.util.* ;
import org.cougaar.tools.castellan.pdu.* ;

/**
 *
 * @author  wpeng
 * @version
 */
public abstract class PlanElementLog extends UniqueObjectLog {
    
    public PlanElementLog( UIDPDU uid ) {
        super( uid ) ;
    }
    
    public void outputParamString( StringBuffer buf ) {
        super.outputParamString( buf ) ;
        buf.append( ",task=" ) ;
        buf.append( taskUID ) ;
    }
    
    public PlanElementLog( UIDPDU uid, UIDPDU newTaskUID, String cluster, long created, long createdExecution ) {
        super( uid, cluster, created, createdExecution ) ;
        this.taskUID = newTaskUID ;
    }

    /**
     * Track allocation results.
     */
    public void addAR( AllocationResultPDU arm ) {
        switch ( arm.getARType() ) {
            case AllocationResultPDU.OBSERVED :
                observedAR = addAR( observedAR, arm ) ;
                break ;
            case AllocationResultPDU.RECEIVED :
                receivedAR = addAR( receivedAR, arm ) ;
                break ;
            case AllocationResultPDU.ESTIMATED :
                estimatedAR = addAR( estimatedAR, arm ) ;
                break ;
            case AllocationResultPDU.REPORTED :
                reportedAR = addAR( reportedAR, arm ) ;
                break ;
            default :
                throw new IllegalArgumentException( "AllocationResult " + arm + " has unknown type." ) ;
        }
    }

    public int getNumAR( int type ) {
        switch ( type ) {
            case AllocationResultPDU.OBSERVED :
                return getNumAR( observedAR ) ;
            case AllocationResultPDU.RECEIVED :
                return getNumAR( receivedAR ) ;
            case AllocationResultPDU.ESTIMATED :
                return getNumAR( estimatedAR ) ;
            case AllocationResultPDU.REPORTED :
                return getNumAR( reportedAR ) ;
            default :
                throw new IllegalArgumentException( "Type " + type + " is unknown."  ) ;
        }
    }

    public AllocationResultPDU getAR( int type, int index ) {
        switch ( type ) {
            case AllocationResultPDU.OBSERVED :
                return getAR( observedAR, index ) ;
            case AllocationResultPDU.RECEIVED :
                return getAR( receivedAR, index ) ;
            case AllocationResultPDU.ESTIMATED :
                return getAR( estimatedAR, index ) ;
            case AllocationResultPDU.REPORTED :
                return getAR( reportedAR, index ) ;
            default :
                throw new IllegalArgumentException( "Type " + type + " is unknown."  ) ;
        }

    }

    protected Object addAR( Object ar, AllocationResultPDU arm ) {
        Object result ;
        if ( ar == null ) {
            result = arm ;
        }
        if ( ar instanceof ArrayList ) {
            ((  ArrayList ) ar ).add( arm ) ;
            result = ar ;
        }
        else {
            ArrayList al = new ArrayList() ;
            al.add( ar ) ;
            al.add( arm ) ;
            result = al ;
        }
        return result ;
    }

    protected Object removeAR( Object ar, int index ) {
        Object result ;
        if ( ar == null ) {
            throw new NoSuchElementException() ;
        }
        if ( ar instanceof ArrayList ) {
            ( ( ArrayList ) ar ).remove( index ) ;
            result = ar ;
        }
        else if ( index == 0 ) {
            result = null ;
        }
        else {
            throw new NoSuchElementException() ;
        }
        return result ;
    }

    protected int getNumAR( Object ar ) {
        if ( ar instanceof ArrayList ) {
            return  ( ( ArrayList ) observedAR ).size() ;
        }
        else if ( observedAR == null ) {
            return 0 ;
        }
        else
            return 1 ;
    }

    protected AllocationResultPDU getAR( Object ar, int i ) {
        if ( ar == null ) {
            return null ;
        }

        if ( ar instanceof ArrayList ) {
            return ( AllocationResultPDU ) ( ( ArrayList ) observedAR ).get(i) ;
        }
        else if ( i == 0 ) {
            return ( AllocationResultPDU ) observedAR ;
        }
        else {
            throw new NoSuchElementException( "AllocatioResult at " + i + " does not exist." ) ;
        }
    }

    public void setParent( UIDPDU newTaskUID) {
        taskUID = newTaskUID;
    }

    public UIDPDU getParent() { return taskUID ; }

    protected Object observedAR, reportedAR, receivedAR, estimatedAR ;
    protected UIDPDU taskUID ;
}
