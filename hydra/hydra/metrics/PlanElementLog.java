/*
 * PlanElementLog.java
 *
 * Created on August 14, 2001, 2:50 PM
 */

package org.hydra.metrics;
import java.util.* ;
import org.hydra.pdu.* ;

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
    
    
    protected AllocationResultPDU getAR( Object ar ) {
        if ( observedAR instanceof ArrayList ) {
            return ( AllocationResultPDU ) ( ( ArrayList ) ar ).get(0) ;
        }
        else {
            return ( AllocationResultPDU ) ar ;
        }
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
            throw new NoSuchElementException( "AR at " + i + " does not exist." ) ;
        }
    }
    
    public void setParent( UIDPDU newTaskUID) {
        taskUID = newTaskUID;
    }    
    
    public UIDPDU getParent() { return taskUID ; }
    
    Object observedAR, reportedAR, receivedAR, estimatedAR ;
    UIDPDU taskUID ;
}
